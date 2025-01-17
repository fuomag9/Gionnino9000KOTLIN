package it.unibo.ai.didattica.competition.tablut.gionnino9000.heuristics

import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import java.util.*

/**
 * Heuristics for the evaluation of a black player state.
 * Description: TO-DO [...]
 *
 * @author Gionnino9000
 */
class BlackHeuristics(state: State?) : Heuristics(state) {
    private val WHITE_EATEN = 0
    private val BLACK_ALIVE = 1
    private val BLACK_SUR_K = 2
    private val RHOMBUS_POS = 3
    private val BLOCKED_ESC = 3

    // Weights for evaluation in the following order: WhiteEaten, BlackAlive, BlackSurroundingKing, RhombusPos
    private val earlyGameWeights: Array<Double?> = arrayOfNulls(4)
    private val lateGameWeights: Array<Double?>

    // Matrix of favourite black positions in the initial stages to block the escape ways
    private val rhombus = arrayOf(
        intArrayOf(1, 2),
        intArrayOf(1, 6),
        intArrayOf(2, 1),
        intArrayOf(2, 7),
        intArrayOf(6, 1),
        intArrayOf(6, 7),
        intArrayOf(7, 2),
        intArrayOf(7, 6)
    )

    init {
        earlyGameWeights[WHITE_EATEN] = 45.0
        earlyGameWeights[BLACK_ALIVE] = 35.0
        earlyGameWeights[BLACK_SUR_K] = 15.0
        earlyGameWeights[RHOMBUS_POS] = 5.0
        lateGameWeights = arrayOfNulls(4)
        lateGameWeights[WHITE_EATEN] = 40.0
        lateGameWeights[BLACK_ALIVE] = 30.0
        lateGameWeights[BLACK_SUR_K] = 25.0
        lateGameWeights[BLOCKED_ESC] = 5.0
    }

    /**
     * @return the evaluation of the current state using a weighted sum
     */
    override fun evaluateState(): Double {
        val kingPos = kingPosition(state!!)
        var stateValue = 0.0
        var lateGame = false
        val numbOfWhite = state.getNumberOf(Pawn.WHITE)
        if (numbOfWhite <= 4) lateGame = true

        // Values for the weighted sum
        val numberOfBlackAlive: Double = state.getNumberOf(Pawn.BLACK).toDouble() / GameAshtonTablut.NUM_BLACK
        val numberOfWhiteEaten: Double =
            (GameAshtonTablut.NUM_WHITE - numbOfWhite).toDouble() / GameAshtonTablut.NUM_WHITE
        val surroundKing = checkAdjacentPawns(state, kingPos, Turn.BLACK.toString()).toDouble() / getNumbToEatKing(
            state
        )
        val whiteInDanger = pawnsAggression.toDouble()
        val PAWNS_AGGRESSION_WEIGHT = 2.0
        if (whiteInDanger > 0) stateValue += whiteInDanger / numbOfWhite * PAWNS_AGGRESSION_WEIGHT

        // Flag to enable console print
        val print = false
        if (print) {
            println("Black pawns alive: $numberOfBlackAlive")
            println("Number of pawns near to the king:$surroundKing")
            println("Number of white pawns eaten: $numberOfWhiteEaten")
        }
        if (!lateGame) { // Early Game
            // Number of tiles in rhombus
            val TILES_IN_RHOMBUS = 8
            val pawnsInRhombus = rhombusValue.toDouble() / TILES_IN_RHOMBUS
            stateValue += numberOfWhiteEaten * earlyGameWeights[WHITE_EATEN]!!
            stateValue += numberOfBlackAlive * earlyGameWeights[BLACK_ALIVE]!!
            stateValue += surroundKing * earlyGameWeights[BLACK_SUR_K]!!
            stateValue += pawnsInRhombus * earlyGameWeights[RHOMBUS_POS]!!
            if (print) {
                println("Number on rhombus pos: $pawnsInRhombus")
                println("|EARLY_GAME|: value is $stateValue")
            }
        } else { // Late Game
            val blockingPawns = blockingPawns().toDouble()
            stateValue += numberOfWhiteEaten * lateGameWeights[WHITE_EATEN]!!
            stateValue += numberOfBlackAlive * lateGameWeights[BLACK_ALIVE]!!
            stateValue += surroundKing * lateGameWeights[BLACK_SUR_K]!!
            stateValue += blockingPawns * lateGameWeights[BLOCKED_ESC]!!
            stateValue += canCaptureKing()
            if (print) {
                println("Blocking pawns: $blockingPawns")
                println("|LATE_GAME|: value is $stateValue")
            }
        }
        return stateValue
    }

    /**
     * @return the number of black pawns on tiles if we have enough pawns
     */
    private val rhombusValue: Int
        get() = if (state!!.getNumberOf(Pawn.BLACK) >= 10) {
            pawnsInRhombus()
        } else {
            0
        }

    /**
     * @return the number of black pawns on rhombus configuration
     */
    private fun pawnsInRhombus(): Int {
        var count = 0
        for (position in rhombus) {
            if (state!!.getPawn(position[0], position[1])!!.equalsPawn(Pawn.BLACK.toString())) {
                count++
            }
        }
        return count
    }

    /**
     * @return the number of pawns blocking king escape
     */
    private fun blockingPawns(): Int {
        return 4 - Arrays.stream(getKingEscapes(state, kingPosition(state!!))).sum()
    }

    /**
     * Used to add value to the position if we create win threats
     *
     * @return 10.0 if the king can be captured, 0.0 otherwise
     */
    private fun canCaptureKing(): Double {
        return if (canBeCaptured(state!!, kingPosition(state), Pawn.KING)) +10.0 else 0.0
    }

    /**
     * @return the number of white pawns that can be captured
     */ // Add pawn per quadrant distribution evaluation ?
    private val pawnsAggression: Int
        private get() {
            var capturable = 0
            val board = state?.board
            for (i in board!!.indices) {
                for (j in board[i]!!.indices) {
                    if (board[i]!![j]!!.equalsPawn(Pawn.WHITE.toString())) {
                        capturable += if (canBeCaptured(state!!, intArrayOf(i, j), Pawn.WHITE)) 1 else 0
                    }
                }
            }
            return capturable
        }
}