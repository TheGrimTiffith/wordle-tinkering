package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.WordleDictionary
import kotlin.test.Test

class DecisionTreeTests {
    @Test
    fun `test simple decision tree` () {
        val simpleDictionary = listOf("turbo", "teddy", "chirp", "femur", "music", "email", "goofy", "drive", "fauna", "newer",
                                      "cabby", "union", "slink", "moose", "theft", "train", "kayak", "testy", "truck", "guilt")
        val tree = DecisionTree(simpleDictionary)
        // val tree = DecisionTree(WordleDictionary.getDictionary())
        tree.newGame().print()
    }
}