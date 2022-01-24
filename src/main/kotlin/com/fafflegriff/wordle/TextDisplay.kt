package com.fafflegriff.wordle

class TextDisplay : Display {
    override fun showResults(attempt: Int, result: Array<Result>) {
        val printableResult = result.map {
            when(it) {
                Result.CORRECT        -> 'G'
                Result.IN_WRONG_PLACE -> 'Y'
                Result.NOT_IN_WORD    -> '-'
            }
        }.joinToString(" ")
        println("[$attempt] $printableResult")
    }
}