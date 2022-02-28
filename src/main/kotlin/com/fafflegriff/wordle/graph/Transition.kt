package com.fafflegriff.wordle.graph

import java.util.*

data class Transition(val encodedTransition: UByte, val remainingCandidates: BitSet, val candidateCount: Int)
