package com.fafflegriff.wordle

class ManualScorer : Scorer {
    override fun score(guess: CharArray): Array<Result> {
        var input: Array<Result>?
        do {
            print("score guess [${guess.joinToString("")}] >")
            input = translateToResult(readLine()  ?: "_____")
        } while (null == input)
        return input
    }

    private fun translateToResult(input: String) : Array<Result>? {
        if (input.length != 5) {
            println("Input must be 5 characters long of [G, Y, -] but was ${input.length}")
            return null
        }
        return try {
             Array(input.length) {
                when (input[it]) {
                    'G' -> Result.CORRECT
                    'Y' -> Result.IN_WRONG_PLACE
                    '-' -> Result.NOT_IN_WORD
                    else -> throw IllegalArgumentException("Only valid inputs are G -> CORRECT, Y -> IN_WRONG_PLACE, - -> NOT_IN_WORD, not ${input[it]}")
                }
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}