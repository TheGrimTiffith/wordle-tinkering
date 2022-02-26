package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.Result

/**
 * Encodes a scoring result into an ordinal space
 */
object Similarity {

    /**
     * Encodes a 5 character wordle scored result into an ordinal between 0 .. 242
     */
    fun encodeResult(result: Array<Result>) : UByte {
        if (result.size != 5) {
            throw IllegalStateException("Wordle solver only currently supports 5 letter words, not ${result.size}")
        }

        return (result[0].value + result[1].value * 3u + result[2].value * 9u + result[3].value * 27u + result[4].value * 81u).toUByte()
    }

    /**
     * Decodes a 5 character wordle encoding between 0 .. 242 into the equivalent result structure. where
     * 0   = -----
     * 80  = GGGG-
     * 242 = GGGGG
     */
    fun decodeResult(encoding: UByte) : Array<Result> {
        if (encoding > 242u) {
            throw IllegalStateException("5 letter result encoding expected between 0..242, but got $encoding")
        }

        // doing things in UByte's is pretty painful, given we're only using for storage density - we expand to UInt
        // then cast back to UByte where necessary
        var buffer = encoding.toUInt()
        val fifthBit: UByte = (buffer / 81u).toUByte()
        buffer -= fifthBit * 81u
        val forthBit = (buffer / 27u).toUByte()
        buffer -= forthBit * 27u
        val thirdBit = (buffer / 9u).toUByte()
        buffer -= thirdBit * 9u
        val secondBit = (buffer / 3u).toUByte()
        buffer -= secondBit * 3u
        return arrayOf(Result.forValue(buffer.toUByte()), Result.forValue(secondBit), Result.forValue(thirdBit), Result.forValue(forthBit), Result.forValue(fifthBit))
    }

    fun size(): Int = 243

}