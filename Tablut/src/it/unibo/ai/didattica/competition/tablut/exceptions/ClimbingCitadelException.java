package it.unibo.ai.didattica.competition.tablut.exceptions;

import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.Serial;

public class ClimbingCitadelException extends Exception {

	/**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;
	
	public ClimbingCitadelException(Action a)
	{
		super("A pawn is tryng to climb over a citadel: "+a.toString());
	}

}
