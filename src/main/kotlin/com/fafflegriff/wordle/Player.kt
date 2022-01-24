package com.fafflegriff.wordle

interface Player {
    fun guess(turn: Int): CharArray
}