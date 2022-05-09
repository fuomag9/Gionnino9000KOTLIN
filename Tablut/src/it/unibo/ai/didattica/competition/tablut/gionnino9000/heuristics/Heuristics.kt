package it.unibo.ai.didattica.competition.tablut.gionnino9000.heuristics

import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import java.util.*

abstract class Heuristics(protected val state: State?) {
    // Matrix of camps
    private val camps = arrayOf(
        intArrayOf(0, 3),
        intArrayOf(0, 4),
        intArrayOf(0, 5),
        intArrayOf(1, 4),
        intArrayOf(3, 0),
        intArrayOf(3, 8),
        intArrayOf(4, 0),
        intArrayOf(4, 1),
        intArrayOf(4, 4),
        intArrayOf(4, 7),
        intArrayOf(4, 8),
        intArrayOf(5, 0),
        intArrayOf(5, 8),
        intArrayOf(7, 4),
        intArrayOf(8, 3),
        intArrayOf(8, 4),
        intArrayOf(8, 5)
    )

    open fun evaluateState(): Double {
        return 0.toDouble()
    }

    /**
     * @return the position of the King
     */
    fun kingPosition(state: State): IntArray {
        val pos = IntArray(2)
        val board = state.board
        for (i in board!!.indices) {
            for (j in board.indices) {
                if (state.getPawn(i, j)!!.equalsPawn("K")) {
                    pos[0] = i
                    pos[1] = j
                    return pos
                }
            }
        }
        return pos
    }

    /**
     * @return the number of adjacent pawns that are target(BLACK, WHITE or King)
     */
    fun checkAdjacentPawns(state: State, pos: IntArray, target: String?): Int {
        var count = 0
        val board = state.board
        if (board!![pos[0] - 1]!![pos[1]]!!.equalsPawn(target)) count++
        if (board[pos[0] + 1]!![pos[1]]!!.equalsPawn(target)) count++
        if (board[pos[0]]!![pos[1] - 1]!!.equalsPawn(target)) count++
        if (board[pos[0]]!![pos[1] + 1]!!.equalsPawn(target)) count++
        return count
    }

    /**
     * @return the number of adjacent squares the king can move in
     */
    fun getKingMovement(state: State, pos: IntArray): Int {
        var count = 4
        val board = state.board
        val space = intArrayOf(pos[0] - 1, pos[1])
        // vertical checks
        if (isPositionOccupied(state, space) || Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, space) }
        ) count--
        space[0] = pos[0] + 1
        if (isPositionOccupied(state, space) || Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, space) }
        ) count--

        // horizontal checks
        space[0] = pos[0]
        space[1] = pos[1] - 1
        if (isPositionOccupied(state, space) || Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, space) }
        ) count--
        space[1] = pos[1] + 1
        if (isPositionOccupied(state, space) || Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, space) }
        ) count--
        return count
    }

    /**
     * @return true if a camp is adjacent to pos
     */
    fun checkAdjacentCamp(pos: IntArray): Boolean {
        if (Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, intArrayOf(pos[0] - 1, pos[1])) }
        ) return true
        if (Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, intArrayOf(pos[0] + 1, pos[1])) }
        ) return true
        return if (Arrays.stream(camps)
                .anyMatch { camp: IntArray? -> Arrays.equals(camp, intArrayOf(pos[0], pos[1] - 1)) }
        ) true else Arrays.stream(camps).anyMatch { camp: IntArray? ->
            Arrays.equals(
                camp, intArrayOf(
                    pos[0], pos[1] + 1
                )
            )
        }
    }

    /**
     * @return true if king is on a center tile
     */
    fun isKingOnCenter(kingPosition: IntArray): Boolean {
        return kingPosition[0] > 2 && kingPosition[0] < 6 && kingPosition[1] > 2 && kingPosition[1] < 6
    }

    /**
     * @return the escapes which king can reach in the following order [ up, down, left, right ]
     */
    fun getKingEscapes(state: State?, kingPosition: IntArray): IntArray {
        val escapes = IntArray(4)
        if (!isKingOnCenter(kingPosition)) {
            if (!(kingPosition[1] > 2 && kingPosition[1] < 6) && !(kingPosition[0] > 2 && kingPosition[0] < 6)) {
                val tempV = countFreeColumn(state, kingPosition)
                val tempH = countFreeRow(state, kingPosition)
                escapes[0] = tempV[0]
                escapes[1] = tempV[1]
                escapes[2] = tempH[0]
                escapes[3] = tempH[1]
            }
            if (kingPosition[1] > 2 && kingPosition[1] < 6) {
                val tempH = countFreeRow(state, kingPosition)
                escapes[2] = tempH[0]
                escapes[3] = tempH[1]
            }
            if (kingPosition[0] > 2 && kingPosition[0] < 6) {
                val tempV = countFreeColumn(state, kingPosition)
                escapes[0] = tempV[0]
                escapes[1] = tempV[1]
            }
            return escapes
        }
        return escapes
    }

    /**
     * @return free rows from given position [ left, right ]
     */
    fun countFreeRow(state: State?, position: IntArray): IntArray {
        val row = position[0]
        val column = position[1]
        val currentPosition = IntArray(2)
        val freeWays = IntArray(2)
        freeWays[0] = 1
        freeWays[1] = 1
        currentPosition[0] = row
        // left side
        for (i in column - 1 downTo 0) {
            currentPosition[1] = i
            if (isPositionOccupied(state, currentPosition) || Arrays.stream(camps)
                    .anyMatch { camp: IntArray? -> Arrays.equals(camp, currentPosition) }
            ) {
                freeWays[0] = 0
                break
            }
        }

        // right side
        for (i in column + 1..8) {
            currentPosition[1] = i
            if (isPositionOccupied(state, currentPosition) || Arrays.stream(camps)
                    .anyMatch { camp: IntArray? -> Arrays.equals(camp, currentPosition) }
            ) {
                freeWays[1] = 0
                break
            }
        }
        return freeWays
    }

    /**
     * @return number of free columns from given position [ up, down ]
     */
    fun countFreeColumn(state: State?, position: IntArray): IntArray {
        val row = position[0]
        val column = position[1]
        val currentPosition = IntArray(2)
        val freeWays = IntArray(2)
        freeWays[0] = 1
        freeWays[1] = 1
        currentPosition[1] = column
        // upside
        for (i in row - 1 downTo 0) {
            currentPosition[0] = i
            if (isPositionOccupied(state, currentPosition) || Arrays.stream(camps)
                    .anyMatch { camp: IntArray? -> Arrays.equals(camp, currentPosition) }
            ) {
                freeWays[0] = 0
                break
            }
        }

        // downside
        for (i in row + 1..8) {
            currentPosition[0] = i
            if (isPositionOccupied(state, currentPosition) || Arrays.stream(camps)
                    .anyMatch { camp: IntArray? -> Arrays.equals(camp, currentPosition) }
            ) {
                freeWays[1] = 0
                break
            }
        }
        return freeWays
    }

    /**
     * @return true if a position is occupied, false otherwise
     */
    fun isPositionOccupied(state: State?, position: IntArray): Boolean {
        return state!!.getPawn(position[0], position[1]) != Pawn.EMPTY
    }

    /**
     * @return number of positions needed to eat king in the current state
     */
    fun getNumbToEatKing(state: State): Int {
        val kingPosition = kingPosition(state)
        return if (kingPosition[0] == 4 && kingPosition[1] == 4) {
            4
        } else if (isKingOnCenter(kingPosition)) {
            3
        } else if (checkAdjacentCamp(kingPosition)) {
            1
        } else {
            2
        }
    }

    /**
     * @param state the current state of the board
     * @param enemy color of the opposite pawn
     * @param position position
     *
     * @return return true or false whether an enemy pawn can go to that position from mentioned side
     */
    fun checkUpside(state: State, enemy: Pawn, position: IntArray): Boolean {
        for (i in position[0] downTo 0) {
            if (isPositionOccupied(state, position)) {
                return state.board!![i]!![position[1]]!!.equalsPawn(enemy.toString())
            }
            if ((position[1] == 0 || position[1] == 8) && i == 3) return false
            if ((position[1] == 1 || position[1] == 7) && i == 4) return false
        }
        return false
    }

    /**
     * @param state the current state of the board
     * @param enemy color of the opposite pawn
     * @param position position
     *
     * @return return true or false whether an enemy pawn can go to that position from mentioned side
     */
    fun checkDownside(state: State, enemy: Pawn, position: IntArray): Boolean {
        for (i in position[0]..8) {
            if (isPositionOccupied(state, position)) {
                return state.board!![i]!![position[1]]!!.equalsPawn(enemy.toString())
            }
            if ((position[1] == 0 || position[1] == 8) && i == 5) return false
            if ((position[1] == 1 || position[1] == 7) && i == 4) return false
        }
        return false
    }

    /**
     * @param state the current state of the board
     * @param enemy color of the opposite pawn
     * @param position position
     *
     * @return return true or false whether an enemy pawn can go to that position from mentioned side
     */
    fun checkRightSide(state: State, enemy: Pawn, position: IntArray): Boolean {
        for (i in position[1]..8) {
            if (isPositionOccupied(state, position)) {
                return state.board!![position[0]]!![i]!!.equalsPawn(enemy.toString())
            }
            if ((position[0] == 0 || position[0] == 8) && i == 5) return false
            if ((position[0] == 1 || position[0] == 7) && i == 4) return false
        }
        return false
    }

    /**
     * @param state the current state of the board
     * @param enemy color of the opposite pawn
     * @param position position
     *
     * @return return true or false whether an enemy pawn can go to that position from mentioned side
     */
    fun checkLeftSide(state: State, enemy: Pawn, position: IntArray): Boolean {
        for (i in position[1] downTo 0) {
            if (isPositionOccupied(state, position)) {
                return state.board!![position[0]]!![i]!!.equalsPawn(enemy.toString())
            }
            if ((position[0] == 0 || position[0] == 8) && i == 3) return false
            if ((position[0] == 1 || position[0] == 7) && i == 4) return false
        }
        return false
    }

    /**
     * @param state the current state of the board
     * @param position the position of the pawn
     *
     * @return return a true or false whether a pawn can be captured
     */
    fun canBeCaptured(state: State, position: IntArray, pawn: Pawn): Boolean {
        if ((position[0] == 0 || position[0] == 8) && (position[1] == 0 || position[1] == 8)) return false
        val enemy =
            if (pawn.equalsPawn(Pawn.WHITE.toString()) || pawn.equalsPawn(Pawn.KING.toString())) Pawn.BLACK else Pawn.WHITE

        // if is king and on center we have special cases
        if (pawn.equalsPawn(Pawn.KING.toString()) && isKingOnCenter(position)) {
            val needed = getNumbToEatKing(state)

            // if king is in danger (1 more enemy pawn to kill)
            return if (checkAdjacentPawns(state, position, enemy.toString()) == needed - 1) {
                // search for empty space
                val space = intArrayOf(position[0] - 1, position[1])

                // vertical checks
                if (isPositionOccupied(state, space) && space[0] != 4 && space[1] != 4) return checkLeftSide(
                    state,
                    enemy,
                    space
                ) || checkRightSide(state, enemy, space)
                space[0] = position[0] + 1
                if (isPositionOccupied(state, space) && space[0] != 4 && space[1] != 4) return checkLeftSide(
                    state,
                    enemy,
                    space
                ) || checkRightSide(state, enemy, space)

                // horizontal checks
                space[0] = position[0]
                space[1] = position[1] - 1
                if (isPositionOccupied(state, space) && space[0] != 4 && space[1] != 4) return checkUpside(
                    state,
                    enemy,
                    space
                ) || checkDownside(state, enemy, space)
                space[1] = position[1] + 1
                checkUpside(state, enemy, space) || checkDownside(state, enemy, space)
            } else false
        }
        return if (verticalCapturePossible(position, enemy)) true else horizontalCapturePossible(position, enemy)
    }

    /**
     * @return true if an Enemy pawn can capture a pawn in given Position vertically
     */
    private fun verticalCapturePossible(position: IntArray, enemy: Pawn): Boolean {
        val targetPos = IntArray(2)
        val checkPos = IntArray(2)
        if (position[0] == 0 || position[0] == 8) return false

        // upside
        targetPos[0] = position[0] - 1
        targetPos[1] = position[1]
        if (Arrays.stream(camps).anyMatch { camp: IntArray? -> Arrays.equals(camp, targetPos) } || isPositionOccupied(
                state,
                targetPos
            ) && state!!.getPawn(
                targetPos[0], targetPos[1]
            )!!.equalsPawn(enemy.toString())) {
            checkPos[0] = position[0] + 1
            checkPos[1] = position[1]
            if (!isPositionOccupied(state, checkPos) && (state?.let { checkLeftSide(it, enemy, checkPos) }!! || checkRightSide(
                    state,
                    enemy,
                    checkPos
                ))
            ) {
                return true
            }
        }

        // downside
        targetPos[0] = position[0] + 1
        targetPos[1] = position[1]
        if (Arrays.stream(camps).anyMatch { camp: IntArray? -> Arrays.equals(camp, targetPos) } || isPositionOccupied(
                state,
                targetPos
            ) && state!!.getPawn(
                targetPos[0], targetPos[1]
            )!!.equalsPawn(enemy.toString())) {
            checkPos[0] = position[0] - 1
            checkPos[1] = position[1]
            return !isPositionOccupied(state, checkPos) && (state?.let { checkLeftSide(it, enemy, checkPos) }!! || checkRightSide(
                state, enemy, checkPos
            ))
        }
        return false
    }

    /**
     * @return true if an Enemy pawn can capture a pawn in given Position horizontally
     */
    private fun horizontalCapturePossible(position: IntArray, enemy: Pawn): Boolean {
        val targetPos = IntArray(2)
        val checkPos = IntArray(2)
        if (position[1] == 0 || position[1] == 8) return false
        // left side present
        targetPos[0] = position[0]
        targetPos[1] = position[1] - 1
        if (Arrays.stream(camps).anyMatch { camp: IntArray? -> Arrays.equals(camp, targetPos) } || isPositionOccupied(
                state,
                targetPos
            ) && state!!.getPawn(
                targetPos[0], targetPos[1]
            )!!.equalsPawn(enemy.toString())) {
            checkPos[0] = position[0]
            checkPos[1] = position[1] + 1
            if (!isPositionOccupied(state, checkPos) && (state?.let { checkUpside(it, enemy, checkPos) }!! || checkDownside(
                    state,
                    enemy,
                    checkPos
                ))
            ) {
                return true
            }
        }

        // right side present
        targetPos[0] = position[0]
        targetPos[1] = position[1] + 1
        if (Arrays.stream(camps).anyMatch { camp: IntArray? -> Arrays.equals(camp, targetPos) } || isPositionOccupied(
                state,
                targetPos
            ) && state!!.getPawn(
                targetPos[0], targetPos[1]
            )!!.equalsPawn(enemy.toString())) {
            checkPos[0] = position[0]
            checkPos[1] = position[1] - 1
            return !isPositionOccupied(state, checkPos) && (state?.let { checkUpside(it, enemy, checkPos) }!! || checkDownside(
                state,
                enemy,
                checkPos
            ))
        }
        return false
    }
}