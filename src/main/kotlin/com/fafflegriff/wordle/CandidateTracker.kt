package com.fafflegriff.wordle

class CandidateTracker (words: List<String> = WordleDictionary.getAnswersDictionary()) {

    var candidates = words.map { it.toCharArray() }
    private val knownWrong: Array<Int> = Array(5) { 0 }

    companion object {
        private const val BITMASK_A = 0b00000000000000000000000001
        private const val BITMASK_B = 0b00000000000000000000000010
        private const val BITMASK_C = 0b00000000000000000000000100
        private const val BITMASK_D = 0b00000000000000000000001000
        private const val BITMASK_E = 0b00000000000000000000010000
        private const val BITMASK_F = 0b00000000000000000000100000
        private const val BITMASK_G = 0b00000000000000000001000000
        private const val BITMASK_H = 0b00000000000000000010000000
        private const val BITMASK_I = 0b00000000000000000100000000
        private const val BITMASK_J = 0b00000000000000001000000000
        private const val BITMASK_K = 0b00000000000000010000000000
        private const val BITMASK_L = 0b00000000000000100000000000
        private const val BITMASK_M = 0b00000000000001000000000000
        private const val BITMASK_N = 0b00000000000010000000000000
        private const val BITMASK_O = 0b00000000000100000000000000
        private const val BITMASK_P = 0b00000000001000000000000000
        private const val BITMASK_Q = 0b00000000010000000000000000
        private const val BITMASK_R = 0b00000000100000000000000000
        private const val BITMASK_S = 0b00000001000000000000000000
        private const val BITMASK_T = 0b00000010000000000000000000
        private const val BITMASK_U = 0b00000100000000000000000000
        private const val BITMASK_V = 0b00001000000000000000000000
        private const val BITMASK_W = 0b00010000000000000000000000
        private const val BITMASK_X = 0b00100000000000000000000000
        private const val BITMASK_Y = 0b01000000000000000000000000
        private const val BITMASK_Z = 0b10000000000000000000000000
        private       val BITMASKS = intArrayOf(BITMASK_A, BITMASK_B, BITMASK_C, BITMASK_D, BITMASK_E, BITMASK_F,
            BITMASK_G, BITMASK_H, BITMASK_I, BITMASK_J, BITMASK_K, BITMASK_L,
            BITMASK_M, BITMASK_N, BITMASK_O, BITMASK_P, BITMASK_Q, BITMASK_R,
            BITMASK_S, BITMASK_T, BITMASK_U, BITMASK_V, BITMASK_W, BITMASK_X,
            BITMASK_Y, BITMASK_Z)


        fun bitmaskFor(char: Char) : Int = BITMASKS[char - 'a']
    }

    fun updateResults(guess: CharArray, result: Array<Result>) {
        // first build the letter tracker, we don't have to hold state from previous interactions as we're making the
        // assumption that we will have applied all constraints to the candidate set, as such we will NEVER go backwards
        // in either a) matched letters or b) letters in wrong place. As such the only information we need to carry across
        // between guesses other than the candidate set is the negation matrix; the set of letters + positions we know NOT
        // to be valid. As such we can build our Available Letters tracker straight from the result joined to the guess
        val available = AvailableLetters(result.mapIndexedNotNull { idx, result -> if (result == Result.IN_WRONG_PLACE) guess[idx] else null })

        // merge the result with last guess and apply to the candidate matrix
        val correctlyPlaced = arrayOfNulls<Char?>(5)
        result.forEachIndexed { index, result ->
            val charAtIndex = guess[index]
            when(result) {
                Result.CORRECT -> correctlyPlaced[index] = charAtIndex
                Result.IN_WRONG_PLACE -> {
                    knownWrong[index] = knownWrong[index] or bitmaskFor(charAtIndex)
                }
                Result.NOT_IN_WORD -> {
                    // add into each position of the knownWrong
                    // providing the word isn't known to contain the character *at all* we can take out completely
                    if (!available.wordContainsLetter(charAtIndex)) {
                        val charMask = bitmaskFor(charAtIndex)
                        for (i in knownWrong.indices) {
                            knownWrong[i] = knownWrong[i] or charMask
                        }
                    }
                }
            }
        }

        // loop over all remaining candidates and apply filters such that only valid candidates remain
        candidates = candidates.filter {
            val remainingTracker = available.copy()
            it.forEachIndexed {
                    idx, char ->
                // if an exactly matched char isn't in the right place -> fail
                if (correctlyPlaced[idx] != null) {
                    if (correctlyPlaced[idx] != char) return@filter false
                }
                // otherwise ensure the value is not invalid for location
                else if (knownWrong[idx] and bitmaskFor(char) > 0) return@filter false
                // otherwise decrement the tracker headlessly - it's OK to exceed the tracker as we don't know all
                // letters yet and are only tracking those needing to be assigned.
                else {
                    remainingTracker.testAndDecrementLetter(char)
                }
            }
            // if there was no reason to knock out, and full count of 'should be present in other places' letters are
            // met -> word should be retained
            return@filter !remainingTracker.hasOutstandingLetters()
        }
    }

    fun getCandidatesAsStrings(): List<String> {
        return candidates.map { it.joinToString("") }
    }
}