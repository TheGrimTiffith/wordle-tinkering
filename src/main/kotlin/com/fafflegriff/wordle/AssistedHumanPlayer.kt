package com.fafflegriff.wordle

class AssistedHumanPlayer(private val player: Player, private val display: Display) : Player, Display {

    // words that still fit all the constraints
    private var candidateTracker = CandidateTracker()

    // used letters with known long locations

    private val knownLettersWrongLocations = mutableMapOf<Char, BooleanArray>()
    private var lastGuess: CharArray = charArrayOf()

    override fun guess(turn: Int): CharArray {
        val candidates = candidateTracker.getCandidatesAsStrings()
        println("> candidates (${candidates.size}): $candidates")
        lastGuess = player.guess(turn)
        return lastGuess
    }

    override fun showResults(attempt: Int, result: Array<Result>) {
        candidateTracker.updateResults(lastGuess, result)
        display.showResults(attempt, result)
    }



}