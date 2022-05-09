package it.unibo.ai.didattica.competition.tablut.domain

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Abstract class for a State of a game We have a representation of the board
 * and the turn
 *
 * @author Andrea Piretti
 */
abstract class State {
    /**
     * Turn represent the player that has to move or the end of the game(A win
     * by a player or a draw)
     *
     * @author A.Piretti
     */
    enum class Turn(private val turn: String) {
        WHITE("W"), BLACK("B"), WHITEWIN("WW"), BLACKWIN("BW"), DRAW("D");

        fun equalsTurn(otherName: String?): Boolean {
            return otherName != null && turn == otherName
        }

        override fun toString(): String {
            return turn
        }
    }

    /**
     *
     * Pawn represents the content of a box in the board
     *
     * @author A.Piretti
     */
    enum class Pawn(private val pawn: String) {
        EMPTY("O"), WHITE("W"), BLACK("B"), THRONE("T"), KING("K");

        fun equalsPawn(otherPawn: String?): Boolean {
            return otherPawn != null && pawn == otherPawn
        }

        override fun toString(): String {
            return pawn
        }
    }

    var board: Array<Array<Pawn?>?>? = null
    var turn: Turn? = null
    private fun boardString(): String {
        val result = StringBuilder()
        for (pawns in board!!) {
            for (j in board!!.indices) {
                result.append(pawns!![j].toString())
                if (j == 8) {
                    result.append("\n")
                }
            }
        }
        return result.toString()
    }

    override fun toString(): String {

        // board
        return """
            ${boardString()}-
            ${turn.toString()}
            """.trimIndent()
    }

    /**
     * this function tells the pawn inside a specific box on the board
     *
     * @param row
     * represents the row of the specific box
     * @param column
     * represents the column of the specific box
     * @return is the pawn of the box
     */
    fun getPawn(row: Int, column: Int): Pawn? {
        return board!![row]!![column]
    }

    /**
     * this function remove a specified pawn from the board
     *
     * @param row
     * represents the row of the specific box
     * @param column
     * represents the column of the specific box
     */
    fun removePawn(row: Int, column: Int) {
        board!![row]!![column] = Pawn.EMPTY
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (this.javaClass != obj.javaClass) return false
        val other = obj as State
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

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (board == null) 0 else Arrays.deepHashCode(board)
        result = prime * result + if (turn == null) 0 else turn.hashCode()
        return result
    }

    fun getBox(row: Int, column: Int): String {
        val ret: String
        val col = (column + 97).toChar()
        ret = col.toString() + "" + (row + 1)
        return ret
    }

    @Throws(CloneNotSupportedException::class)
    open fun clone(): State {
        val stateclass: Class<out State> = this.javaClass
        val cons: Constructor<out State>
        var result: State? = null
        try {
            cons = stateclass.getConstructor(stateclass)
            result = cons.newInstance()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        val oldboard = board
        val newboard = Objects.requireNonNull(result)!!.board
        for (i in board!!.indices) {
            System.arraycopy(oldboard!![i], 0, newboard!![i], 0, board!![i]!!.size)
        }
        result!!.board = newboard
        result.turn = turn
        return result
    }

    /**
     * Counts the number of checkers of a specific color on the board. Note: the king is not taken into account for white, it must be checked separately
     * @param color The color of the checker that will be counted. It is possible also to use EMPTY to count empty cells.
     * @return The number of cells of the board that contains a checker of that color.
     */
    fun getNumberOf(color: Pawn): Int {
        var count = 0
        for (pawns in board!!) {
            for (pawn in pawns!!) {
                if (pawn == color) count++
            }
        }
        return count
    }
}