package com.fafflegriff.wordle

import java.io.File

class WordleDictionary {
    companion object Instance {
        private val dictionary = File("C:\\Users\\timgr\\IdeaProjects\\KotlinGradle\\src\\main\\resources\\dictionary.txt").readLines();
        fun getDictionary(): List<String> = dictionary
    }
}