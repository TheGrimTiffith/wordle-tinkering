package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.Result

/**
 * Encodes a scoring result into an ordinal space
 */
object Similarity {

    /**
     * Encodes a 5 character wordle scored result into an ordinal between 0 .. 242
     */
    fun encodeResult(result: Array<Result>) : Int {
        if (result.size != 5) {
            throw IllegalStateException("Wordle solver only currently supports 5 letter words, not ${result.size}")
        }

        return result[0].value + result[1].value * 3 + result[2].value * 9 + result[3].value * 27 + result[4].value * 81
    }

    /**
     * Decodes a 5 character wordle encoding between 0 .. 242 into the equivalent result structure. where
     * 0   = -----
     * 80  = GGGG-
     * 242 = GGGGG
     */
    fun decodeResult(encoding: Int) : Array<Result> {
        if (encoding !in 0 .. 242) {
            throw IllegalStateException("5 letter result encoding expected between 0..242, but got $encoding")
        }

        var buffer = encoding
        val fifthBit = buffer / 81
        buffer -= fifthBit * 81
        val forthBit = buffer / 27
        buffer -= forthBit * 27
        val thirdBit = buffer / 9
        buffer -= thirdBit * 9
        val secondBit = buffer / 3
        buffer -= secondBit * 3
        return arrayOf(Result.forValue(buffer), Result.forValue(secondBit), Result.forValue(thirdBit), Result.forValue(forthBit), Result.forValue(fifthBit))
    }

    fun size(): Int = 243

}