package it.unibo.ai.didattica.competition.tablut.domain

import java.io.Serial
import java.io.Serializable

/**
 * This class represents a state of a match of the smallest version of tablut
 * @author A.Piretti
 */
class StateBrandub : State(), Serializable {
    init {
        board = Array(7) { arrayOfNulls(7) }
        for (i in 0..6) {
            for (j in 0..6) {
                board!![i]!![j] = Pawn.EMPTY
            }
        }
        board!![3]!![3] = Pawn.THRONE
        turn = Turn.BLACK
        board!![3]!![3] = Pawn.KING
        board!![3]!![4] = Pawn.WHITE
        board!![3]!![2] = Pawn.WHITE
        board!![4]!![3] = Pawn.WHITE
        board!![2]!![3] = Pawn.WHITE
        board!![3]!![5] = Pawn.BLACK
        board!![3]!![6] = Pawn.BLACK
        board!![6]!![3] = Pawn.BLACK
        board!![5]!![3] = Pawn.BLACK
        board!![3]!![0] = Pawn.BLACK
        board!![3]!![1] = Pawn.BLACK
        board!![0]!![3] = Pawn.BLACK
        board!![1]!![3] = Pawn.BLACK
    }

    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}