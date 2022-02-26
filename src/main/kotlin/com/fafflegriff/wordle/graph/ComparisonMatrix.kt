package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.ScoringLogic

class ComparisonMatrix(answersDictionary: List<String>, otherInputWordsDictionary: List<String>) {
    val dictionaryStrings: List<String>
    val dictionary: Array<CharArray>
    val firstNonAnswerOrdinal: Short
    val wordSimilarityClusters: Array<Map<UByte, List<Short>>>

    init {
        // concatenate the additional valid guess words dictionary after the answers dictionary, we'll keep a marker to
        // determine whether we have any valid answers remaining in a discovery branch, if not - we can prune the branch
        this.dictionaryStrings = answersDictionary.plus(otherInputWordsDictionary)
        this.dictionary = dictionaryStrings.map { it.toCharArray() }.toTypedArray()
        this.firstNonAnswerOrdinal = answersDictionary.size.toShort()

        // from the similiarity matrix, we can now roll up based on similarity equality (ordinal) in order to produce
        // clusters in the form <from-word> [index] <similarity> [ordinal] sorted list [to-words]
        val wordSimilarityClusters = Array(dictionary.size) { mutableMapOf<UByte, MutableList<Short>>() }

        // build into the similarity cluster
        for (fromId in dictionary.indices) {
            val fromWord = dictionary[fromId]
            val similarityClusters = wordSimilarityClusters[fromId]
            // we only need to build similarities from *all* words to the *valid answers*
            for (toId in 0 until firstNonAnswerOrdinal) {
                if (fromId != toId) {
                    val similarity = Similarity.encodeResult(ScoringLogic.score(fromWord, dictionary[toId]))
                    similarityClusters.computeIfAbsent(similarity) { mutableListOf() }.add(toId.toShort())
                }
            }
        }

        // now go through and sort the to-id clusters such that they can be merge-joined efficiently
        for (word in wordSimilarityClusters) {
            for (similarityCluster in word.entries) {
                similarityCluster.value.sort()
            }
        }

        this.wordSimilarityClusters = wordSimilarityClusters as Array<Map<UByte, List<Short>>>
    }
}