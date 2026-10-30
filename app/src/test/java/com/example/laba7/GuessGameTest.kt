package com.example.laba7

import com.example.laba7.screens.GuessOutcome
import com.example.laba7.screens.evaluateGuess
import org.junit.Assert.assertEquals
import org.junit.Test

class GuessGameTest {
    @Test
    fun validatesInputAndChecksTheGuess() {
        assertEquals(GuessOutcome.EMPTY, evaluateGuess("", 5))
        assertEquals(GuessOutcome.NOT_A_NUMBER, evaluateGuess("abc", 5))
        assertEquals(GuessOutcome.OUT_OF_RANGE, evaluateGuess("0", 5))
        assertEquals(GuessOutcome.OUT_OF_RANGE, evaluateGuess("11", 5))
        assertEquals(GuessOutcome.INCORRECT, evaluateGuess("4", 5))
        assertEquals(GuessOutcome.CORRECT, evaluateGuess("5", 5))
    }
}
