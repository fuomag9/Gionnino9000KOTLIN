package it.unibo.ai.didattica.competition.tablut.gionnino9000.search

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn

/**
 * "Custom" implementation of AIMA Iterative Deepening MinMax search with Alpha-Beta Pruning.
 * Maximal computation time is specified in seconds.
 * This configuration redefines the method eval() using getUtility() method in [GameAshtonTablut].
 *
 * @see aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
 *
 *
 * @author Gionnino9000
 */
class TavolettaSearch(game: GameAshtonTablut, utilMin: Double, utilMax: Double, time: Int) :
    IterativeDeepeningAlphaBetaSearch<State?, Action, Turn?>(game, utilMin, utilMax, time) {
    /**
     * Method that estimates the value for states. This implementation returns the utility value for
     * terminal states and heuristic value for non-terminal states.
     *
     * @param state the current state
     * @param player the player who has to make the next move (turn)
     *
     * @return the score of this state (double)
     */
    override fun eval(state: State?, player: Turn?): Double {
        // Needed to make heuristicEvaluationUsed = true, if the state evaluated isn't terminal
        super.eval(state, player)

        // Return heuristic value for the given state
        return game.getUtility(state, player)
    }

    /**
     * Method controlling the search. It is based on minmax with iterative deepening and tries to make to a good decision in limited time.
     * It is overrided to print metrics.
     *
     * @param state the current state
     *
     * @return the chosen action
     */
    override fun makeDecision(state: State?): Action {
        val a = super.makeDecision(state)
        println(TEAM_NAME + ": " + PLAYER_NAME + " dice che ha esplorato " + metrics[METRICS_NODES_EXPANDED] + " nodi, raggiungendo una profondità di " + metrics[METRICS_MAX_DEPTH])
        return a
    }

    companion object {
        const val TEAM_NAME = "Gionnino9000"
        const val PLAYER_NAME = "Tavoletta"
    }
}