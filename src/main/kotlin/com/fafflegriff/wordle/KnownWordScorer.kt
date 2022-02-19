package com.fafflegriff.wordle

class KnownWordScorer(word: String) : Scorer {
    private val word: CharArray

    init {
        this.word = word.toCharArray()
    }

    override fun score(guess: CharArray): Array<Result> = ScoringLogic.score(guess, word)
}