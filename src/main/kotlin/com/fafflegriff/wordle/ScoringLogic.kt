package com.fafflegriff.wordle

object ScoringLogic {

    /**
     * Scores the guess against the provided hidden word
     */
    fun score(guess:CharArray, hiddenWord: CharArray) :Array<Result> {
        // CORRECT should always be taken ahead of IN_WRONG_PLACE - as such we have to do a multi-pass solution, first
        // evaluating CORRECT, then doing left-to-right promotion of IN_WRONG_PLACE greedily

        val availableLetters = AvailableLetters(hiddenWord)
        val wrongPlaceCandidateIndexes = mutableListOf<Int>()
        val result = Array(guess.size) {
            if (guess[it] == hiddenWord[it]) {
                availableLetters.testAndDecrementLetter(guess[it])
                Result.CORRECT
            } else if (availableLetters.wordContainsLetter(guess[it])){
                wrongPlaceCandidateIndexes.add(it)
                Result.NOT_IN_WORD
            } else Result.NOT_IN_WORD
        }

        // now greedily evaluate all letters that were in the wrong place
        for (candidateIndex in wrongPlaceCandidateIndexes) {
            val letter = guess[candidateIndex]
            if (availableLetters.testAndDecrementLetter(letter)) {
                result[candidateIndex] = Result.IN_WRONG_PLACE
            }
        }
        return result
    }
}