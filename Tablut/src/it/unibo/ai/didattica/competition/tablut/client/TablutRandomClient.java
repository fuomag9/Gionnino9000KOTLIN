package it.unibo.ai.didattica.competition.tablut.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
 * 
 * @author A. Piretti, Andrea Galassi
 *
 */
public class TablutRandomClient extends TablutClient {

	private final int game;

	public TablutRandomClient(String player, String name, int gameChosen, int timeout, String ipAddress) throws IOException {
		super(player, name, timeout, ipAddress);
		game = gameChosen;
	}
	
	public TablutRandomClient(String player, String name, int timeout, String ipAddress) throws IOException {
		this(player, name, 4, timeout, ipAddress);
	}
	
	public TablutRandomClient(String player, int timeout, String ipAddress) throws IOException {
		this(player, "random", 4, timeout, ipAddress);
	}

	public TablutRandomClient(String player) throws IOException {
		this(player, "random", 4, 60, "localhost");
	}


	public static void main(String[] args) throws IOException {
		int gametype = 4;
		String role = "";
		String name = "random";
		String ipAddress = "localhost";
		int timeout = 60;
		// TODO: change the behavior?
		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.exit(-1);
		} else {
			System.out.println(args[0]);
			role = (args[0]);
		}
		if (args.length == 2) {
			System.out.println(args[1]);
			timeout = Integer.parseInt(args[1]);
		}
		if (args.length == 3) {
			ipAddress = args[2];
		}
		System.out.println("Selected client: " + args[0]);

		TablutRandomClient client = new TablutRandomClient(role, name, gametype, timeout, ipAddress);
		client.run();
	}

	@Override
	public void run() {

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		State state;

		Game rules = null;
		switch (this.game) {
			case 1 -> {
				state = new StateTablut();
				rules = new GameTablut();
			}
			case 2 -> {
				state = new StateTablut();
				rules = new GameModernTablut();
			}
			case 3 -> {
				state = new StateBrandub();
				rules = new GameTablut();
			}
			case 4 -> {
				state = new StateTablut();
				state.setTurn(Turn.WHITE);
				rules = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
				System.out.println("Ashton Tablut game");
			}
			default -> {
				System.out.println("Error in game selection");
				System.exit(4);
			}
		}

		List<int[]> pawns = new ArrayList<>();
		List<int[]> empty = new ArrayList<>();

		System.out.println("You are player " + this.getPlayer().toString() + "!");

		while (true) {
			try {
				this.read();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			}
			System.out.println("Current state:");
			state = this.getCurrentState();
			System.out.println(state.toString());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			if (this.getPlayer().equals(Turn.WHITE)) {
				// Mio turno
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) {
					int[] buf;
					for (int i = 0; i < state.getBoard().length; i++) {
						for (int j = 0; j < state.getBoard().length; j++) {
							if (state.getPawn(i, j).equalsPawn(State.Pawn.WHITE.toString())
									|| state.getPawn(i, j).equalsPawn(State.Pawn.KING.toString())) {
								buf = new int[2];
								buf[0] = i;
								buf[1] = j;
								pawns.add(buf);
							} else if (state.getPawn(i, j).equalsPawn(State.Pawn.EMPTY.toString())) {
								buf = new int[2];
								buf[0] = i;
								buf[1] = j;
								empty.add(buf);
							}
						}
					}

					int[] selected;

					boolean found = false;
					Action a = null;
					a = new Action("z0", "z0", Turn.WHITE);

					while (!found) {
						if (pawns.size() > 1) {
							selected = pawns.get(new Random().nextInt(pawns.size() - 1));
						} else {
							selected = pawns.get(0);
						}

						String from = this.getCurrentState().getBox(selected[0], selected[1]);

						selected = empty.get(new Random().nextInt(empty.size() - 1));
						String to = this.getCurrentState().getBox(selected[0], selected[1]);

						a = new Action(from, to, Turn.WHITE);


						try {
							rules.checkMove(state, a);
							found = true;
						} catch (Exception e) {
							throw new RuntimeException(e);
						}

					}

					System.out.println("Mossa scelta: " + Objects.requireNonNull(a));
					try {
						this.write(a);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pawns.clear();
					empty.clear();

				}
				// Turno dell'avversario
				else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
					System.out.println("Waiting for your opponent move... ");
				}
				// ho vinto
				else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}
				// ho perso
				else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}
				// pareggio
				else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			} else {

				// Mio turno
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {
					int[] buf;
					for (int i = 0; i < state.getBoard().length; i++) {
						for (int j = 0; j < state.getBoard().length; j++) {
							if (state.getPawn(i, j).equalsPawn(State.Pawn.BLACK.toString())) {
								buf = new int[2];
								buf[0] = i;
								buf[1] = j;
								pawns.add(buf);
							} else if (state.getPawn(i, j).equalsPawn(State.Pawn.EMPTY.toString())) {
								buf = new int[2];
								buf[0] = i;
								buf[1] = j;
								empty.add(buf);
							}
						}
					}

					int[] selected;

					boolean found = false;
					Action a = null;
					a = new Action("z0", "z0", Turn.BLACK);

					while (!found) {
						selected = pawns.get(new Random().nextInt(pawns.size() - 1));
						String from = this.getCurrentState().getBox(selected[0], selected[1]);

						selected = empty.get(new Random().nextInt(empty.size() - 1));
						String to = this.getCurrentState().getBox(selected[0], selected[1]);

						a = new Action(from, to, Turn.BLACK);


						System.out.println("try: " + Objects.requireNonNull(a));
						try {
							rules.checkMove(state, a);
							found = true;
						} catch (Exception e) {
							throw new RuntimeException(e);
						}

					}

					System.out.println("Mossa scelta: " + a);
					try {
						this.write(a);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pawns.clear();
					empty.clear();

				}

				else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
					System.out.println("Waiting for your opponent move... ");
				} else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			}
		}

	}
}
