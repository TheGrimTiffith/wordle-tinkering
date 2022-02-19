package com.fafflegriff.wordle

enum class Result(val value: Int, val character: Char) {
    CORRECT(2, 'G'), IN_WRONG_PLACE(1, 'Y'), NOT_IN_WORD(0, '-');

    companion object {
        fun forValue(value: Int) : Result =
            when (value) {
                0 -> NOT_IN_WORD
                1 -> IN_WRONG_PLACE
                2 -> CORRECT
                else -> throw IllegalStateException("only values 0..2 are valid, not $value")
            }

        fun forChar(char: Char) : Result =
            when (char) {
                '-' -> NOT_IN_WORD
                'G', 'g' -> IN_WRONG_PLACE
                'Y', 'y' -> CORRECT
                else -> throw IllegalStateException("only characters [-,Y,G] are valid, not $char")
            }
    }
}