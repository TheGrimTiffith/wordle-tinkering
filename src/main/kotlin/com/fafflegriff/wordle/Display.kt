package com.fafflegriff.wordle

interface Display {
    fun showResults(attempt: Int, result: Array<Result>)
}