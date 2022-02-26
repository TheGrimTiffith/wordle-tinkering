package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.Helpers.result
import org.junit.Assert
import kotlin.test.Test
import kotlin.test.assertEquals

class SimilarityTests {

    @Test
    fun `test similarity encodings`() {
        assertEquals(0u, Similarity.encodeResult(result("-----")))
        assertEquals(242u, Similarity.encodeResult(result("GGGGG")))
        assertEquals(80u, Similarity.encodeResult(result("GGGG-")))
        assertEquals(161u, Similarity.encodeResult(result("GGGGY")))
        assertEquals(210u, Similarity.encodeResult(result("-YGYG"))) // 0 + 3 + 18 + 27 + 162 = 210
    }

    @Test
    fun `test dimilarity decodings`() {
        Assert.assertArrayEquals(result("-----"), Similarity.decodeResult(0u))
        Assert.assertArrayEquals(result("GGGGG"), Similarity.decodeResult(242u))
        Assert.assertArrayEquals(result("GGGG-"), Similarity.decodeResult(80u))
        Assert.assertArrayEquals(result("GGGGY"), Similarity.decodeResult(161u))
        Assert.assertArrayEquals(result("-YGYG"), Similarity.decodeResult(210u))
    }
}