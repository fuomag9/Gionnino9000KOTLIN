package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action that is climbing over a pawn
 * @author A.Piretti
 */
class ClimbingException(a: Action) : Exception("A pawn is tryng to climb over another pawn: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}