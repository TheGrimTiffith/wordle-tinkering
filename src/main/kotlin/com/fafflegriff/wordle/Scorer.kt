package com.fafflegriff.wordle

interface Scorer {

    fun score(guess: String): Array<Result> = score(guess.toCharArray())

    fun score(guess: CharArray): Array<Result>

}