package com.fafflegriff.wordle

import java.io.File

class WordleDictionary {
    companion object Instance {
        private val dictionary = File("src/main/resources/dictionary.txt").readLines()
        fun getDictionary(): List<String> = dictionary
    }
}