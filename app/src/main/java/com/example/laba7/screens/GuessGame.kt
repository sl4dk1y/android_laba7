package com.example.laba7.screens

enum class GuessOutcome { EMPTY, NOT_A_NUMBER, OUT_OF_RANGE, INCORRECT, CORRECT }

fun evaluateGuess(input: String, target: Int): GuessOutcome {
    if (input.isBlank()) return GuessOutcome.EMPTY
    val number = input.toIntOrNull() ?: return GuessOutcome.NOT_A_NUMBER
    if (number !in 1..10) return GuessOutcome.OUT_OF_RANGE
    return if (number == target) GuessOutcome.CORRECT else GuessOutcome.INCORRECT
}
