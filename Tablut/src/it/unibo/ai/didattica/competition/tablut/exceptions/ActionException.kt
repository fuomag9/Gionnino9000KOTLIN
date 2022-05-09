package it.unibo.ai.didattica.competition.tablut.exceptions

import it.unibo.ai.didattica.competition.tablut.domain.Action
import java.io.Serial

/**
 * This exception represent an action with the wrong format
 * @author A.Piretti
 */
class ActionException(a: Action) : Exception("The format of the action is not correct: $a") {
    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}