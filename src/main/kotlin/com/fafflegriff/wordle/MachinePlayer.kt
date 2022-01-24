package com.fafflegriff.wordle

class MachinePlayer: Player, Display {
    // words that still fit all the constraints
    private var candidateTracker = CandidateTracker()
    private var lastGuess: CharArray = charArrayOf()

    override fun guess(turn: Int): CharArray {
        // initial strategy - pick randomly
        lastGuess = candidateTracker.candidates.random()
        return lastGuess
    }

    override fun showResults(attempt: Int, result: Array<Result>) {
        candidateTracker.updateResults(lastGuess, result)
    }
}