package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.WordleDictionary
import java.lang.management.ManagementFactory
import kotlin.test.Test

class DecisionTreeTests {
    @Test
    fun `test simple decision tree` () {
        println("Building Comparison Matrix (N x M)")
        val comparisonMatrixStart = System.currentTimeMillis()
        val comparisonMatrix = ComparisonMatrix(WordleDictionary.getAnswersDictionary(), WordleDictionary.getOtherInputWordsDictionary())
        println("Completed Building Comparison Matrix in ${System.currentTimeMillis() - comparisonMatrixStart}ms")

        println("| Max Candidates (N) | Best Start word | Guesses (SUM) | Guesses (AVG) | calculation time (ms) |")
        println("|---|---|---|---|---|")
        for (maxCandidates in 1 .. 20) {
            val start = System.currentTimeMillis()
            val game = DecisionTree(comparisonMatrix, maxCandidates).newGame();
            val chosenStartWord = game.makeChoice().joinToString(separator = "")
            println("| $maxCandidates | $chosenStartWord | ${game.totalGuessesFromThisPoint()} | ${game.averageGuessesFromThisPoint()} | ${System.currentTimeMillis() - start}")
        }
    }
}