package it.unibo.ai.didattica.competition.tablut.gionnino9000.heuristics

import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import java.util.*

/**
 * Heuristics for the evaluation of a white player state.
 *
 * Description: TO-DO [...]
 *
 * @author Gionnino9000
 */
class WhiteHeuristics(state: State?) : Heuristics(state) {
    private val WHITE_ALIVE = 0
    private val BLACK_EATEN = 1
    private val KING_MOVEMENT = 2
    private val SAFE_PAWNS = 3

    // Weights for evaluation in the following order: WhiteAlive, BlackEaten, BestTiles, NumKingEscapes
    private val gameWeights: Array<Double?>

    init {
        gameWeights = arrayOfNulls(4)
        gameWeights[WHITE_ALIVE] = 35.0
        gameWeights[BLACK_EATEN] = 18.0
        gameWeights[KING_MOVEMENT] = 5.0
        gameWeights[SAFE_PAWNS] = 42.0
    }

    /**
     * @return the evaluation of the current state using a weighted sum
     */
    override fun evaluateState(): Double {
        var stateValue = 0.0
        val kingPos = kingPosition(state!!)

        // If king can be captured PRUNE THOSE MFS
        if (canBeCaptured(state, kingPos, Pawn.KING)) return Double.NEGATIVE_INFINITY
        val numbOfBlack = state.getNumberOf(Pawn.BLACK)
        val numbOfWhite = state.getNumberOf(Pawn.WHITE)
        // Values for the weighted sum
        val numberOfWhiteAlive: Double = numbOfWhite.toDouble() / GameAshtonTablut.NUM_WHITE
        val numberOfBlackEaten: Double =
            (GameAshtonTablut.NUM_BLACK - numbOfBlack).toDouble() / GameAshtonTablut.NUM_BLACK
        val kingMovEval = evalKingMovement(kingPos)
        val evalKingEsc = evalKingEscapes(kingPos)
        val safePawns = pawnsSafety.toDouble()
        if (safePawns > 0) stateValue += safePawns / numbOfWhite * gameWeights[SAFE_PAWNS]!!
        stateValue += numberOfWhiteAlive * gameWeights[WHITE_ALIVE]!!
        stateValue += numberOfBlackEaten * gameWeights[BLACK_EATEN]!!
        stateValue += kingMovEval * gameWeights[KING_MOVEMENT]!!
        stateValue += evalKingEsc

        // Flag to enable console print
        val print = false
        if (print) {
            println("White pawns alive: $numberOfWhiteAlive")
            println("Number of black pawns eaten: $numberOfBlackEaten")
            println("King mobility eval: $kingMovEval")
            println("Eval king escapes: $evalKingEsc")
            println("|GAME|: value is $stateValue")
        }
        return stateValue
    }

    /**
     * @param kPos The king position
     *
     * @return a greater value if the king can move in one or more directions
     */
    private fun evalKingMovement(kPos: IntArray?): Double {
        val `val` = getKingMovement(state!!, kPos!!)
        if (`val` == 0) return 0.3
        return if (`val` == 1) 1.0 else 1.2
    }

    /**
     * @return the number of white pawns that can't be captured
     */
    private val pawnsSafety: Int
        get() {
            var safe = 0
            val board = state!!.board
            for (i in board!!.indices) {
                for (j in board[i]!!.indices) {
                    if (board[i]!![j]!!.equalsPawn(Pawn.WHITE.toString())) {
                        safe += if (canBeCaptured(state!!, intArrayOf(i, j), Pawn.WHITE)) 0 else 1
                    }
                }
            }
            return safe
        }

    /**
     * @param kPos The king Position
     *
     * @return a positive value for a SURE king escape (greater if there are more than once). If there are no escapes, 0.0
     */
    private fun evalKingEscapes(kPos: IntArray?): Double {
        val escapes = getKingEscapes(state, kPos!!)
        val numEsc = Arrays.stream(escapes).sum()
        if (numEsc > 1) return 200.0 else if (numEsc == 1) {
            // Up escape
            if (escapes[0] == 1) {
                for (i in kPos[0] - 1 downTo 0) {
                    val checkPos = intArrayOf(i, kPos[1])
                    if (state?.let { checkLeftSide(it, Pawn.BLACK, checkPos) }!! || checkRightSide(state, Pawn.BLACK, checkPos)) {
                        return 0.0
                    }
                }
                return 80.0
            }
            // Down escape
            if (escapes[1] == 1) {
                for (i in kPos[0] + 1..8) {
                    val checkPos = intArrayOf(i, kPos[1])
                    if (state?.let { checkLeftSide(it, Pawn.BLACK, checkPos) }!! || checkRightSide(state, Pawn.BLACK, checkPos)) {
                        return 0.0
                    }
                }
                return 80.0
            }
            // Left escape
            if (escapes[2] == 1) {
                for (i in kPos[1] - 1 downTo 0) {
                    val checkPos = intArrayOf(kPos[0], i)
                    if (state?.let { checkUpside(it, Pawn.BLACK, checkPos) }!! || checkDownside(state, Pawn.BLACK, checkPos)) {
                        return 0.0
                    }
                }
                return 80.0
            }
            // Right escape
            if (escapes[3] == 1) {
                for (i in kPos[1] + 1..8) {
                    val checkPos = intArrayOf(kPos[0], i)
                    if (state?.let { checkUpside(it, Pawn.BLACK, checkPos) }!! || checkDownside(state, Pawn.BLACK, checkPos)) {
                        return 0.0
                    }
                }
                return 80.0
            }
        }
        return 0.0
    }
}