package com.fafflegriff.wordle

import com.fafflegriff.wordle.Helpers.result
import org.junit.Assert
import org.junit.Test

class KnownWordScorerTests {

    @Test
    fun `test guess no matches`() {
        val scorer = KnownWordScorer("fiche")
        Assert.assertArrayEquals(result("-----"), scorer.score("kayla"))
    }

    @Test
    fun `test double letter still counted as wrong`() {
        val scorer = KnownWordScorer("winge")
        val result = scorer.score("eagle")
        Assert.assertArrayEquals(result("--Y-G"), result )
    }

    @Test
    fun `test multiple letters in wrong place, 1 overlapping`() {
        val scorer = KnownWordScorer("toomp")
        val result = scorer.score("blooo")
        Assert.assertArrayEquals(result("--GY-"), result)
    }

    @Test
    fun `test correct place wins over misplaced`() {
        val scorer = KnownWordScorer("stump")
        val result = scorer.score("plump")
        Assert.assertArrayEquals(result("--GGG"), result)
    }
}