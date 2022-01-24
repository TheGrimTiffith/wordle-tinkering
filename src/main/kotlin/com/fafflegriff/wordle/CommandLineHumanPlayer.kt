package com.fafflegriff.wordle

class CommandLineHumanPlayer : Player {
    override fun guess(turn: Int): CharArray {
        var input: String
        do {
            print("enter guess $turn >")
            input = readLine()  ?: "_____"
        } while (!isValid(input))
        return input.toCharArray()
    }

    fun isValid(input: String) : Boolean = input.length == 5
}