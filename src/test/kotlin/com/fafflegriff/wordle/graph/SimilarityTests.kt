package com.fafflegriff.wordle.graph

import com.fafflegriff.wordle.Helpers.result
import org.junit.Assert
import kotlin.test.Test
import kotlin.test.assertEquals

class SimilarityTests {

    @Test
    fun `test similarity encodings`() {
        assertEquals(0, Similarity.encodeResult(result("-----")))
        assertEquals(242, Similarity.encodeResult(result("GGGGG")))
        assertEquals(80, Similarity.encodeResult(result("GGGG-")))
        assertEquals(161, Similarity.encodeResult(result("GGGGY")))
        assertEquals(210, Similarity.encodeResult(result("-YGYG"))) // 0 + 3 + 18 + 27 + 162 = 210
    }

    @Test
    fun `test dimilarity decodings`() {
        Assert.assertArrayEquals(result("-----"), Similarity.decodeResult(0))
        Assert.assertArrayEquals(result("GGGGG"), Similarity.decodeResult(242))
        Assert.assertArrayEquals(result("GGGG-"), Similarity.decodeResult(80))
        Assert.assertArrayEquals(result("GGGGY"), Similarity.decodeResult(161))
        Assert.assertArrayEquals(result("-YGYG"), Similarity.decodeResult(210))
    }
}