package com.fafflegriff.wordle

class AvailableLetters {
    // as we're only looking at a-z, no need to use a Map, can use direct addressing from char
    private val letterCounts : IntArray

    constructor(word: CharArray){
        letterCounts = IntArray(26)
        word.forEach { letterCounts[index(it)]++ }
    }

    constructor(word: List<Char>){
        letterCounts = IntArray(26)
        word.forEach { letterCounts[index(it)]++ }
    }

    private constructor(source: AvailableLetters) {
        letterCounts = source.letterCounts.copyOf()
    }

    fun copy(): AvailableLetters = AvailableLetters(this)

    fun hasOutstandingLetters() : Boolean {
        for (letterCount in letterCounts) {
            if (letterCount > 0) return true
        }
        return false
    }

    fun wordContainsLetter(character: Char) : Boolean {
        return letterCounts[index(character)] > 0
    }

    fun testAndDecrementLetter(character: Char) : Boolean {
        val idx = index(character)
        return if (letterCounts[idx] > 0) {
            letterCounts[idx]--
            true
        } else false
    }

    private fun index(character: Char) : Int = character - 'a'
}