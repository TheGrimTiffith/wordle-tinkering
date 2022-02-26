package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.Result
import kotlin.math.log2

/**
 * The Decision tree provides recommendations of word guesses, given what has already been provided, based on the
 * average or percentile of guesses that will be required to get to the answer, given what is known.
 */
class DecisionTree(private val comparisonMatrix: ComparisonMatrix, var maxCandidatesToConsider: Int) {

    private val root: DecisionNode

    init {
        // stitch the clusters into a *transition* graph - we start from the root position where *all* values are
        // valid and all words are available
        val allDictionary = comparisonMatrix.dictionary.indices.map { it.toShort() }
        val remainingPossibilities = (0 until comparisonMatrix.firstNonAnswerOrdinal).map { it.toShort() }
        root = buildChoice(allDictionary, remainingPossibilities, 1)
    }

    /**
     * Create a new game using the decision tree
     */
    fun newGame() : Game = Game()

    private data class Candidate(val choice: Short, val transitions: Map<UByte, List<Short>>) : Comparable<Candidate> {
        val averageInformation: Double

        init {
            var sum = 0

            // capture the sum and the max across each scoring transition for the candidate.
            for (transition in transitions.values) {
                sum += transition.size
            }

            val doubleSum = sum.toDouble()
            var averageInformation = 0.0
            // now calculate the average information
            for (transition in transitions.values) {
                val probability: Double = transition.size.toDouble() / doubleSum
                val information = log2(1/probability)
                averageInformation += probability * information
            }
            this.averageInformation = averageInformation
        }

        override fun compareTo(other: Candidate): Int = when {
            averageInformation > other.averageInformation -> 1
            averageInformation < other.averageInformation -> -1
            else -> 0
        }
    }

    private fun buildChoice(availableChoices: List<Short>, remainingPossibilities: List<Short>, depth: Int) : DecisionNode {
        // base condition hit - we have a single, terminal choice
        if (remainingPossibilities.size == 1) {
            return DecisionNode(remainingPossibilities[0], emptyMap(), depth)
        }

        val candidates = mutableListOf<Candidate>()
        for (choice in availableChoices) {
            //  pull it's outbound similarity clusters
            val unfilteredTransitions = comparisonMatrix.wordSimilarityClusters[choice.toInt()]
            // filter each similarity cluster based on what is still *possible* [ intersection ]
            var reducedCount = 0
            val filteredTransitions: Map<UByte, List<Short>> = unfilteredTransitions.mapNotNull {
                val validPossibilities = intersectSortedLists(remainingPossibilities, it.value)
                if (validPossibilities.size < it.value.size && validPossibilities.isNotEmpty()) {
                    reducedCount++
                }
                // NOTE - we don't want to transition across a branch that eliminates all possibility of getting to a valid result (fully disjoint) not do a
                // transition that provides no reduction in candidates (this handles both the self transition i.e. 'WORDS' -> 'WORDS' as well as no match anagrams
                // such as 'SHEEP' and 'PEESH' where result words doesn't overlap, such as 'GUILT' (I'm sure there are better / real anagrams)
                if (validPossibilities.isEmpty()) null else Pair(it.key, validPossibilities)
            }.toMap()

            // providing there is at least one valid transition and at least one reduction in candidate cardinality - keep the option
            if (filteredTransitions.isNotEmpty() && (reducedCount > 0 || depth == 1)) {
                candidates.add(Candidate(choice, filteredTransitions))
            }
        }

        // produce the full set of remaining impactful choices from the candidates
        val choicesRemainingPostFilter = candidates.map { it.choice }

        // order choices and filter to best N candidates
        val filteredCandidates = if (candidates.size > maxCandidatesToConsider) {
            candidates.sortDescending()
            candidates.take(maxCandidatesToConsider)
        } else candidates

        // now recurse and build the deep inspection for each of the candidates
        val transitionDepth = depth + 1
        val choices = filteredCandidates.map {
            val transitionsToChoices = it.transitions.map {
                    transition -> Pair(transition.key, buildChoice(choicesRemainingPostFilter, transition.value, transitionDepth))
            }.toMap()
            DecisionNode(it.choice, transitionsToChoices, depth)
        }

        // return the optimal decision based on the available choices
        return choices.sortedBy { it.allGuesses }[0]
    }

    private fun intersectSortedLists(left: List<Short>, right: List<Short>) : List<Short> {
        val intersection = ArrayList<Short>(if (left.size < right.size) left.size else right.size)
        var leftIdx = 0
        var rightIdx = 0
        do {
            val leftVal = left[leftIdx]
            val rightVal = right[rightIdx]
            when {
                leftVal < rightVal -> leftIdx++
                leftVal > rightVal -> rightIdx++
                else -> {
                    intersection.add(leftVal)
                    leftIdx++
                    rightIdx++
                }
            }
        } while (leftIdx < left.size && rightIdx < right.size)

        return intersection
    }

    private class DecisionNode(val guessWordId: Short, val resultTransitions: Map<UByte, DecisionNode>, depth: Int) {
        val maxDepth: Int
        val allGuesses: Int
        val nodeCount: Int

        init {
            var tempMaxDepth = 0
            var tempAllGuesses = 0
            var tempNodeCount = 0
            for (transition in resultTransitions.values) {
                // transitions will often be sparse, as we're trading memory for direct decision addressibility (at the moment)
                // if this proves too memory intensive, we'll drop to a LinkedHash
                if (transition.maxDepth > tempMaxDepth) {
                    tempMaxDepth = transition.maxDepth
                }
                tempAllGuesses += transition.allGuesses
                tempNodeCount += transition.nodeCount
            }
            maxDepth = tempMaxDepth + 1         // must include self depth
            nodeCount = tempNodeCount + 1       // likewise must include self depth
            allGuesses = tempAllGuesses + depth // has to include the guess cost of it's assigned guess word
        }

        fun transition(scoring: List<Result>): DecisionNode {
            val encodedTransition = Similarity.encodeResult(scoring.toTypedArray())
            return resultTransitions[encodedTransition]
                ?: throw IllegalStateException("Expected a valid choice for $scoring from $guessWordId but found none")
        }

        fun print(indent: String, dictionary:List<String>) {
            println("$indent${dictionary[guessWordId.toInt()]} [maxDepth=$maxDepth, sumGuesses=$allGuesses]")
            for (transition in resultTransitions.entries) {
                // increase tab by 2 for transitions
                val decoded = Similarity.decodeResult(transition.key)
                val decodedCharList = decoded.map { it.character }
                val decodedAsString = decodedCharList.joinToString("")
                println("$indent  $decodedAsString")
                // increase tab by further 2 for choices underneath the transitions
                transition.value.print("$indent    ", dictionary)
            }
        }
    }

    inner class Game {
        private var decisionNode: DecisionNode = root

        fun makeChoice() : CharArray {
            // set as new root for the graph, ready for the result to transition
            return comparisonMatrix.dictionary[decisionNode.guessWordId.toInt()]
        }

        fun transitionOnResult(scoring: List<Result>) {
            decisionNode = decisionNode.transition(scoring)
        }

        fun print() {
            decisionNode.print("", comparisonMatrix.dictionaryStrings)
        }

        fun totalGuessesFromThisPoint() : Int {
            return decisionNode.allGuesses
        }

        fun averageGuessesFromThisPoint() : Double {
            return decisionNode.allGuesses.toDouble() / decisionNode.nodeCount.toDouble()
        }
    }
}