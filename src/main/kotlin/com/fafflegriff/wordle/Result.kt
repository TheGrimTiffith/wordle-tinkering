package com.fafflegriff.wordle

enum class Result(val value: UByte, val character: Char) {
    CORRECT(2u, 'G'), IN_WRONG_PLACE(1u, 'Y'), NOT_IN_WORD(0u, '-');

    companion object {
        fun forValue(value: UByte) : Result =
            when (value) {
                0u.toUByte() -> NOT_IN_WORD
                1u.toUByte() -> IN_WRONG_PLACE
                2u.toUByte() -> CORRECT
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