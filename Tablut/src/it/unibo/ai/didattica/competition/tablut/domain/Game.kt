package it.unibo.ai.didattica.competition.tablut.domain

import it.unibo.ai.didattica.competition.tablut.exceptions.*

/**
 * Contains the rules of the game
 *
 */
interface Game {
    /**
     * This method checks an action in a state: if it is correct the state is
     * going to be changed, if it is wrong it throws a specific exception
     *
     * @param state
     * the state of the game
     * @param a
     * the action to be analyzed
     * @return the new state of the game
     * @throws BoardException
     * try to move a pawn out of the board
     * @throws ActionException
     * the format of the action is wrong
     * @throws StopException
     * try to not move any pawn
     * @throws PawnException
     * try to move an enemy pawn
     * @throws DiagonalException
     * try to move a pawn diagonally
     * @throws ClimbingException
     * try to climb over another pawn
     * @throws ThroneException
     * try to move a pawn into the throne boxe
     * @throws OccupiedException
     * try to move a pawn into an ccupited box
     */
    @Throws(
        BoardException::class,
        ActionException::class,
        StopException::class,
        PawnException::class,
        DiagonalException::class,
        ClimbingException::class,
        ThroneException::class,
        OccupiedException::class,
        CloneNotSupportedException::class
    )
    fun checkMove(state: State?, a: Action): State?
    fun endGame(state: State?)
}