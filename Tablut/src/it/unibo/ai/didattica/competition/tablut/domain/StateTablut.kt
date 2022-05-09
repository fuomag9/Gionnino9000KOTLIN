package it.unibo.ai.didattica.competition.tablut.domain

import java.io.Serial
import java.io.Serializable

/**
 * This class represents a state of a match of Tablut (classical or second
 * version)
 *
 * @author A.Piretti
 */
class StateTablut : State(), Serializable {
    init {
        board = Array(9) { arrayOfNulls(9) }
        for (i in 0..8) {
            for (j in 0..8) {
                board!![i]!![j] = Pawn.EMPTY
            }
        }
        board!![4]!![4] = Pawn.THRONE
        turn = Turn.BLACK
        board!![4]!![4] = Pawn.KING
        board!![2]!![4] = Pawn.WHITE
        board!![3]!![4] = Pawn.WHITE
        board!![5]!![4] = Pawn.WHITE
        board!![6]!![4] = Pawn.WHITE
        board!![4]!![2] = Pawn.WHITE
        board!![4]!![3] = Pawn.WHITE
        board!![4]!![5] = Pawn.WHITE
        board!![4]!![6] = Pawn.WHITE
        board!![0]!![3] = Pawn.BLACK
        board!![0]!![4] = Pawn.BLACK
        board!![0]!![5] = Pawn.BLACK
        board!![1]!![4] = Pawn.BLACK
        board!![8]!![3] = Pawn.BLACK
        board!![8]!![4] = Pawn.BLACK
        board!![8]!![5] = Pawn.BLACK
        board!![7]!![4] = Pawn.BLACK
        board!![3]!![0] = Pawn.BLACK
        board!![4]!![0] = Pawn.BLACK
        board!![5]!![0] = Pawn.BLACK
        board!![4]!![1] = Pawn.BLACK
        board!![3]!![8] = Pawn.BLACK
        board!![4]!![8] = Pawn.BLACK
        board!![5]!![8] = Pawn.BLACK
        board!![4]!![7] = Pawn.BLACK
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): StateTablut {
        val stateTablut = super.clone() as StateTablut
        val result = StateTablut()
        val oldboard = board
        val newboard = result.board
        for (i in board!!.indices) {
            System.arraycopy(oldboard!![i], 0, newboard!![i], 0, board!![i]!!.size)
        }
        result.board=(newboard)
        result.turn=(turn)
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (this.javaClass != obj.javaClass) return false
        val other = obj as StateTablut
        if (board == null) {
            if (other.board != null) return false
        } else {
            if (other.board == null) return false
            if (board!!.size != other.board!!.size) return false
            if (board!![0]!!.size != other.board!![0]!!.size) return false
            for (i in other.board!!.indices) for (j in other.board!![i]!!.indices) if (board!![i]!![j] != other.board!![i]!![j]) return false
        }
        return turn == other.turn
    }

    companion object {
        @Serial
        private val serialVersionUID = 1L
    }
}