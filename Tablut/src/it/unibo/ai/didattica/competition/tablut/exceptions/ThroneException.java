package it.unibo.ai.didattica.competition.tablut.exceptions;

import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.Serial;

/**
 * This exception represent an action that is trying to move a pawn into the throne
 * @author A.Piretti
 *
 */
public class ThroneException extends Exception {

	@Serial
    private static final long serialVersionUID = 1L;
	
	public ThroneException(Action a)
	{
		super("Player "+a.getTurn().toString()+" is tryng to go into the castle: "+ a);
	}	

}
