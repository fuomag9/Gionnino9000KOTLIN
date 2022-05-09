package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action that is moving to an occupited box
 * @author A.Piretti
 */
class OccupiedException(a: Action) : Exception("Move into a box occupited form another pawn: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}