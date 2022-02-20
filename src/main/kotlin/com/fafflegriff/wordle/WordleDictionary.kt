package com.fafflegriff.wordle

import java.io.File

class WordleDictionary {
    companion object Instance {
        private val dictionary = WordleDictionary::class.java.getResourceAsStream("/dictionary.txt").bufferedReader().readLines()
        fun getDictionary(): List<String> = dictionary
    }
}