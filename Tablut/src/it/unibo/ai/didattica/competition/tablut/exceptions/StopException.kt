package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action that is trying to do nothing
 * @author A.Piretti
 */
class StopException(a: Action) : Exception("Action not allowed, a pawn need to move: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}