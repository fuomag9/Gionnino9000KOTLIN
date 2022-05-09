package it.unibo.ai.didattica.competition.tablut.exceptions;

import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.Serial;

public class CitadelException extends Exception {

	/**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;
	
	public CitadelException(Action a)
	{
		super("Move into a citadel: "+a.toString());
	}

}
