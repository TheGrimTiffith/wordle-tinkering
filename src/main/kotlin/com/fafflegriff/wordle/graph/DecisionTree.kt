package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.Result
import com.fafflegriff.wordle.ScoringLogic
import kotlin.math.log2

/**
 * The Decision tree provides recommendations of word guesses, given what has already been provided, based on the
 * average or percentile of guesses that will be required to get to the answer, given what is known.
 */
class DecisionTree(dictionary: List<String>, var maxCandidatesToConsider: Int) {
    private val dictionaryStrings: List<String>
    private val dictionary: Array<CharArray>
    private val root: DecisionNode

    init {
        // translate dictionary to a CharArray
        this.dictionaryStrings = dictionary
        this.dictionary = dictionary.map { it.toCharArray() }.toTypedArray()

        val selfSimilarity = Similarity.encodeResult(arrayOf(Result.CORRECT, Result.CORRECT, Result.CORRECT, Result.CORRECT, Result.CORRECT))

        // build N^2 similarity matrix between each entry in the dictionary, e.g. from:'SPITS' to:'FREED' is '-----'.

        // build sub-clustering by similarity
        val similarityMatrix = Array(dictionary.size) { Array(dictionary.size) { selfSimilarity } }

        // now update with actual calculated values for the adjacency graph
        for (from in dictionary.indices) {
            val fromWord = this.dictionary[from]
            for (to in dictionary.indices) {
                similarityMatrix[from][to] = when {
                    from != to -> Similarity.encodeResult(ScoringLogic.score(fromWord, this.dictionary[to]))
                    else -> selfSimilarity
                }
            }
        }

        // from the similiarity matrix, we can now roll up based on similarity equality (ordinal) in order to produce
        // clusters in the form <from-word> [index] <similarity> [ordinal] sorted list [to-words]
        val wordSimilarityClusters = Array(dictionary.size) { mutableMapOf<Int, MutableList<Int>>() }

        // build into the similarity cluster
        for (fromId in similarityMatrix.indices) {
            val similarities = similarityMatrix[fromId]
            val similarityClusters = wordSimilarityClusters[fromId]
            for (toId in similarities.indices) {
                if (fromId != toId) {
                    val similarity = similarities[toId]
                    similarityClusters.computeIfAbsent(similarity) { mutableListOf() }.add(toId)
                }
            }
        }

        // now go through and sort the to-id clusters such that they can be merge-joined efficiently
        for (word in wordSimilarityClusters) {
            for (similarityCluster in word.entries) {
                similarityCluster.value.sort()
            }
        }

        // now stitch the clusters into a *transition* graph - we start from the root position where *all* values are
        // valid and all words are available
        // TODO - provide separate valid guess words from the subset of answer words
        val allDictionary = dictionary.indices.map { it }
        root = buildChoice(allDictionary, allDictionary, wordSimilarityClusters as Array<Map<Int, List<Int>>>, 1)
    }

    /**
     * Create a new game using the decision tree
     */
    fun newGame() : Game = Game()

    private data class Candidate(val choice: Int, val transitions: Map<Int, List<Int>>) : Comparable<Candidate> {
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

    private fun buildChoice(availableChoices: List<Int>, remainingPossibilities: List<Int>, similarityClusters: Array<Map<Int, List<Int>>>, depth: Int) : DecisionNode {
        // base conditions hit either we found a singleton answer OR we hit a depth below the max depth
        if (remainingPossibilities.size == 1) {
            return DecisionNode(availableChoices[0], emptyMap(), depth)
        }

        val candidates = mutableListOf<Candidate>()
        for (choice in availableChoices) {
            // TODO - how to think about intersection between [remaining hidden word possibilities] and the filtered
            //  edge outcomes

            //  pull it's outbound similarity clusters
            val unfilteredTransitions = similarityClusters[choice]
            // filter each similarity cluster based on what is still *possible* [ intersection ]
            val filteredTransitions: Map<Int, List<Int>> = unfilteredTransitions.mapNotNull {
                val validPossibilities = intersectSortedLists(remainingPossibilities, it.value)
                // NOTE - we don't want to transition across a branch that eliminates all possibility of getting to a valid result (fully disjoint) not do a
                // transition that provides no reduction in candidates (this handles both the self transition i.e. 'WORDS' -> 'WORDS' as well as no match anograms
                // such as 'SHEEP' and 'PEESH' where result words doesn't overlap, such as 'GUILT' (I'm sure there are better / real anagrams)
                if (validPossibilities.isEmpty() || validPossibilities.size == remainingPossibilities.size) null else Pair(it.key, validPossibilities)
            }.toMap()

            // providing there is at least one valid transition
            if (filteredTransitions.isNotEmpty()) {
                candidates.add(Candidate(choice, filteredTransitions))
            }
        }
        // order choices and filter to best N candidates
        val filteredCandidates = if (candidates.size > maxCandidatesToConsider) {
            candidates.sortDescending()
            candidates.take(maxCandidatesToConsider)
        } else candidates

        // now recurse and build the deep inspection for each of the candidates
        val choices = filteredCandidates.map { buildDecisionNode(it.choice, it.transitions, similarityClusters, depth) }

        // return the optimal decision based on the available choices
        return choices.sortedBy { it.allGuesses }[0]
    }

    private fun buildDecisionNode(wordId: Int, transitions: Map<Int, List<Int>>, transitionClusters: Array<Map<Int, List<Int>>>, depth: Int) : DecisionNode {
        val transitionDepth = depth + 1
        val transitionsToChoices = transitions.map { Pair(it.key, buildChoice(it.value, it.value, transitionClusters, transitionDepth)) }.toMap()
        return DecisionNode(wordId, transitionsToChoices, depth)
    }

    private fun intersectSortedLists(left: List<Int>, right: List<Int>) : List<Int> {
        val intersection = mutableListOf<Int>()
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

    private class DecisionNode(val guessWordId: Int, val resultTransitions: Map<Int, DecisionNode>, depth: Int) {
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
            println("$indent${dictionary[guessWordId]} [maxDepth=$maxDepth, sumGuesses=$allGuesses]")
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
            return dictionary[decisionNode.guessWordId]
        }

        fun transitionOnResult(scoring: List<Result>) {
            decisionNode = decisionNode.transition(scoring)
        }

        fun print() {
            decisionNode.print("", dictionaryStrings)
        }

        fun totalGuessesFromThisPoint() : Int {
            return decisionNode.allGuesses
        }

        fun averageGuessesFromThisPoint() : Double {
            return decisionNode.allGuesses.toDouble() / decisionNode.nodeCount.toDouble()
        }
    }
}