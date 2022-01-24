package com.fafflegriff.wordle

import com.fafflegriff.wordle.Helpers.result
import org.junit.Assert.assertEquals
import org.junit.Test



class CandidateTrackerTests {
    @Test
    fun `test candidate reduction`() {
        val tracker = CandidateTracker(listOf("blurt", "quirk", "twirl", "furry", "third", "blurb", "flirt", "churn", "myrrh", "curry", "hurry", "whirl", "chirp"))
        tracker.updateResults("curry".toCharArray(), result("G--G-"))
        val candidates = tracker.getCandidatesAsStrings()
        assertEquals(listOf("chirp"), candidates)
    }
}