package it.unibo.ai.didattica.competition.tablut.exceptions;

import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.Serial;

/**
 * This exception represent an action that is moving a pawn diagonally
 * @author A.Piretti
 *
 */
public class DiagonalException extends Exception {

	@Serial
    private static final long serialVersionUID = 1L;
	
	public DiagonalException(Action a)
	{
		super("Diagonal move is not allowed: "+a.toString());
	}

}
