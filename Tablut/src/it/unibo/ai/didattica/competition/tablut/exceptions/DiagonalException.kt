package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action that is moving a pawn diagonally
 * @author A.Piretti
 */
class DiagonalException(a: Action) : Exception("Diagonal move is not allowed: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}