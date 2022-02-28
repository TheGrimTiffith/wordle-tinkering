package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.ScoringLogic
import java.util.BitSet

class ComparisonMatrix(answersDictionary: List<String>, otherInputWordsDictionary: List<String>) {
    val dictionaryStrings: List<String>
    val dictionary: Array<CharArray>
    val firstNonAnswerOrdinal: Int
    val wordSimilarityClusters: Array<Array<Transition>>

    init {
        // concatenate the additional valid guess words dictionary after the answers dictionary, we'll keep a marker to
        // determine whether we have any valid answers remaining in a discovery branch, if not - we can prune the branch
        this.dictionaryStrings = answersDictionary.plus(otherInputWordsDictionary)
        this.dictionary = dictionaryStrings.map { it.toCharArray() }.toTypedArray()
        this.firstNonAnswerOrdinal = answersDictionary.size

        // from the similiarity matrix, we can now roll up based on similarity equality (ordinal) in order to produce
        // clusters in the form <from-word> [index] <similarity> [ordinal] sorted list [to-words]
        this.wordSimilarityClusters = Array(dictionary.size) {
            val fromWord = dictionary[it]
            val similarityClusters = mutableMapOf<UByte, BitSet>()
            // we only need to build similarities from *all* words to the *valid answers*
            for (toId in 0 until firstNonAnswerOrdinal) {
                if (it != toId) {
                    val similarity = Similarity.encodeResult(ScoringLogic.score(fromWord, dictionary[toId]))
                    similarityClusters.computeIfAbsent(similarity) { BitSet(answersDictionary.size) }.set(toId)
                }
            }
            similarityClusters.entries.map { entry -> Transition(entry.key, entry.value, entry.value.cardinality()) }.toTypedArray()
        }
    }
}