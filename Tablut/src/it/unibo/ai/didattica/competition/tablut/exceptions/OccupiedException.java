package it.unibo.ai.didattica.competition.tablut.exceptions;

import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.Serial;

/**
 * This exception represent an action that is moving to an occupited box
 * @author A.Piretti
 *
 */
public class OccupiedException extends Exception {

@Serial
private static final long serialVersionUID = 1L;
	
	public OccupiedException(Action a)
	{
		super("Move into a box occupited form another pawn: "+a.toString());
	}

	
}
