package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action of a player that is trying to move an enemy or an empty pawn
 * @author A.Piretti
 */
class PawnException(a: Action) : Exception("The player is tryng to move a wrong pawn: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}