package com.fafflegriff.wordle

class WordleDictionary {
    companion object Instance {
        private val answersDictionary = WordleDictionary::class.java.getResourceAsStream("/answers-dictionary.txt").bufferedReader().readLines()
        private val otherInputWords = WordleDictionary::class.java.getResourceAsStream("/input-words-dictionary.txt").bufferedReader().readLines()
        fun getAnswersDictionary(): List<String> = answersDictionary
        fun getOtherInputWordsDictionary(): List<String> = otherInputWords
    }
}