package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action that is trying to move out of the board
 * @author A.Piretti
 */
class BoardException(a: Action) : Exception("The move is out of the board: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}