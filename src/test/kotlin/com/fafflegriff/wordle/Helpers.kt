package com.fafflegriff.wordle

object Helpers {
    fun result(result: String) : Array<Result> {
        return Array(result.length) {
            when(result[it]) {
                'G' -> Result.CORRECT
                'Y' -> Result.IN_WRONG_PLACE
                '-' -> Result.NOT_IN_WORD
                else -> throw IllegalArgumentException("Only valid inputs are G -> CORRECT, Y -> IN_WRONG_PLACE, R -> NOT_IN_WORD, not ${result[it]}")
            }
        }
    }
}