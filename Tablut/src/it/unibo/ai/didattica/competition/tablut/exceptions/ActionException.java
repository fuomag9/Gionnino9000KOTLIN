package it.unibo.ai.didattica.competition.tablut.exceptions;

import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.Serial;

/**
 * This exception represent an action with the wrong format
 * @author A.Piretti
 *
 */
public class ActionException extends Exception {

	@Serial
    private static final long serialVersionUID = 1L;
	
	public ActionException(Action a)
	{
		super("The format of the action is not correct: "+a.toString());
	}

}
