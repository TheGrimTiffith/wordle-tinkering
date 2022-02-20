package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.WordleDictionary
import kotlin.test.Test

class DecisionTreeTests {
    @Test
    fun `test simple decision tree` () {
        println("| Max Candidates (N) | Best Start word | Guesses (SUM) | Guesses (AVG) | calculation time (ms) |")
        println("|---|---|---|---|---|")
        for (maxCandidates in 1 .. 20) {
            val start = System.currentTimeMillis()
            val game = DecisionTree(WordleDictionary.getDictionary(), maxCandidates).newGame();
            val chosenStartWord = game.makeChoice().joinToString(separator = "")
            println("| $maxCandidates | $chosenStartWord | ${game.totalGuessesFromThisPoint()} | ${game.averageGuessesFromThisPoint()} | ${System.currentTimeMillis() - start}")
        }
    }
}