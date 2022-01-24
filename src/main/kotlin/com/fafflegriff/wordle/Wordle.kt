package com.fafflegriff.wordle

class Wordle(val player: Player, val checker: Scorer, val display: Display) {

    fun play(): Int {
        // play for up to 6 guesses - if the word can't be guessed in 6 guesses, the player looses
        for (i in 1..6) {
            val guess = player.guess(i)
            val result = checker.score(guess)
            display.showResults(i, result)
            if (result.filter { it == Result.CORRECT }.size == 5) {
                return i
            }
        }
        return 0
    }
}

interface NewGameDecider {
    fun playAgain(result: Int): Boolean
}

class FixedCountNewGameDecider(private val numberOfGames: Int): NewGameDecider {
    private var playedGames = 0

    override fun playAgain(result: Int): Boolean = playedGames++ < numberOfGames
}

class ManualInputNewGameDecider: NewGameDecider {
    override fun playAgain(result: Int): Boolean {
        println("Did you win? $result - would you like to play again Y = Yes, everything else -> No / Quits)")
        print(">")
        return parseKeepPlaying(readLine())
    }

    private fun parseKeepPlaying(input: String?) : Boolean =
        when(input) {
            "y", "Y","yes", "true" -> true
            else -> false
        }
}


fun main() {
    // val word = "choco"
    var gamesPlayed = 0
    var gamesWon = 0
    val winDistribution = Array(6) { 0 }
    var keepPlaying = true
    // val newGameDecider: NewGameDecider = ManualInputNewGameDecider()
    val newGameDecider = FixedCountNewGameDecider(1000000)
    do {
        val word = WordleDictionary.getDictionary().random()
        val scorer = KnownWordScorer(word)
        //val scorer = ManualScorer()
        // val player = AssistedHumanPlayer(CommandLineHumanPlayer(), TextDisplay())
        val player = MachinePlayer()
        val game = Wordle(player, scorer, player)
        val result = game.play()
        gamesPlayed++
        if (result > 0) {
            gamesWon++
            winDistribution[result - 1]++
        }
        keepPlaying = newGameDecider.playAgain(result)
    } while (keepPlaying)
    println("played=$gamesPlayed, win % = ${gamesWon/(gamesPlayed * 1.0) * 100.0}")
    println("Guess Distribution:")
    winDistribution.forEachIndexed {
        idx, count -> println("${idx + 1}: $count")
    }
}
