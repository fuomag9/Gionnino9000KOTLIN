package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action that is trying to move a pawn into the throne
 * @author A.Piretti
 */
class ThroneException(a: Action) : Exception("Player " + a.turn.toString() + " is tryng to go into the castle: " + a) {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}