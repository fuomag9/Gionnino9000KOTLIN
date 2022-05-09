package it.unibo.ai.didattica.competition.tablut.domain;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Abstract class for a State of a game We have a representation of the board
 * and the turn
 * 
 * @author Andrea Piretti
 *
 */
public abstract class State {

	/**
	 * Turn represent the player that has to move or the end of the game(A win
	 * by a player or a draw)
	 * 
	 * @author A.Piretti
	 */
	public enum Turn {
		WHITE("W"), BLACK("B"), WHITEWIN("WW"), BLACKWIN("BW"), DRAW("D");
		private final String turn;

		Turn(String s) {
			turn = s;
		}

		public boolean equalsTurn(String otherName) {
			return otherName != null && turn.equals(otherName);
		}

		public String toString() {
			return turn;
		}
	}

	/**
	 * 
	 * Pawn represents the content of a box in the board
	 * 
	 * @author A.Piretti
	 *
	 */
	public enum Pawn {
		EMPTY("O"), WHITE("W"), BLACK("B"), THRONE("T"), KING("K");
		private final String pawn;

		Pawn(String s) {
			pawn = s;
		}

		public boolean equalsPawn(String otherPawn) {
			return otherPawn != null && pawn.equals(otherPawn);
		}

		public String toString() {
			return pawn;
		}

	}

	protected Pawn[][] board;
	protected Turn turn;

	public State() {
		super();
	}

	public Pawn[][] getBoard() {
		return board;
	}

	public String boardString() {
		StringBuilder result = new StringBuilder();
		for (Pawn[] pawns : this.board) {
			for (int j = 0; j < this.board.length; j++) {
				result.append(pawns[j].toString());
				if (j == 8) {
					result.append("\n");
				}
			}
		}
		return result.toString();
	}

	@Override
	public String toString() {

		// board

		return "" +
				this.boardString() +
				"-" +
				"\n" +

				// TURNO
				this.turn.toString();
	}

	public String toLinearString() {

		// board

		return "" +
				this.boardString().replace("\n", "") +
				this.turn.toString();
	}

	/**
	 * this function tells the pawn inside a specific box on the board
	 * 
	 * @param row
	 *            represents the row of the specific box
	 * @param column
	 *            represents the column of the specific box
	 * @return is the pawn of the box
	 */
	public Pawn getPawn(int row, int column) {
		return this.board[row][column];
	}

	/**
	 * this function remove a specified pawn from the board
	 * 
	 * @param row
	 *            represents the row of the specific box
	 * @param column
	 *            represents the column of the specific box
	 * 
	 */
	public void removePawn(int row, int column) {
		this.board[row][column] = Pawn.EMPTY;
	}

	public void setBoard(Pawn[][] board) {
		this.board = board;
	}

	public Turn getTurn() {
		return turn;
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (this.board == null) {
			if (other.board != null)
				return false;
		} else {
			if (other.board == null)
				return false;
			if (this.board.length != other.board.length)
				return false;
			if (this.board[0].length != other.board[0].length)
				return false;
			for (int i = 0; i < other.board.length; i++)
				for (int j = 0; j < other.board[i].length; j++)
					if (!this.board[i][j].equals(other.board[i][j]))
						return false;
		}
		return this.turn == other.turn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.board == null) ? 0 : Arrays.deepHashCode(this.board));
		result = prime * result + ((this.turn == null) ? 0 : this.turn.hashCode());
		return result;
	}

	public String getBox(int row, int column) {
		String ret;
		char col = (char) (column + 97);
		ret = col + "" + (row + 1);
		return ret;
	}

	public State clone() throws CloneNotSupportedException {
		State state = (State) super.clone();
		Class<? extends State> stateclass = this.getClass();
		Constructor<? extends State> cons;
		State result = null;
		try {
			cons = stateclass.getConstructor(stateclass);
			result = cons.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		Pawn[][] oldboard = this.getBoard();
		Pawn[][] newboard = Objects.requireNonNull(result).getBoard();

		for (int i = 0; i < this.board.length; i++) {
			System.arraycopy(oldboard[i], 0, newboard[i], 0, this.board[i].length);
		}

		result.setBoard(newboard);
		result.setTurn(this.turn);
		return result;
	}

	/**
	 * Counts the number of checkers of a specific color on the board. Note: the king is not taken into account for white, it must be checked separately
	 * @param color The color of the checker that will be counted. It is possible also to use EMPTY to count empty cells.
	 * @return The number of cells of the board that contains a checker of that color.
	 */
	public int getNumberOf(Pawn color) {
		int count = 0;
		for (Pawn[] pawns : board) {
			for (Pawn pawn : pawns) {
				if (pawn == color)
					count++;
			}
		}
		return count;
	}

}
