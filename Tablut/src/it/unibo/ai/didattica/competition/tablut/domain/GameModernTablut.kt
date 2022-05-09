package it.unibo.ai.didattica.competition.tablut.domain

import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.exceptions.*

/**
 * Tablut che segue le regole moderne
 *
 */
class GameModernTablut @JvmOverloads constructor(private val movesDraw: Int = 0) : Game {
    private var movesWithutCapturing = 0
    @Throws(
        BoardException::class,
        ActionException::class,
        StopException::class,
        PawnException::class,
        DiagonalException::class,
        ClimbingException::class,
        ThroneException::class,
        OccupiedException::class
    )
    override fun checkMove(state: State?, a: Action): State {
        // this.loggGame.fine(a.toString());
        // controllo la mossa
        if (a.to!!.length != 2 || a.from!!.length != 2) {
            // this.loggGame.warning("Formato mossa errato");
            throw ActionException(a)
        }
        val columnFrom = a.columnFrom
        val columnTo = a.columnTo
        val rowFrom = a.rowFrom
        val rowTo = a.rowTo

        // controllo se sono fuori dal tabellone
        if (state != null) {
            if (columnFrom > state.board!!.size - 1 || rowFrom > state.board!!.size - 1 || rowTo > state.board!!.size - 1 || columnTo > state.board!!.size - 1 || columnFrom < 0 || rowFrom < 0 || rowTo < 0 || columnTo < 0) {
                // this.loggGame.warning("Mossa fuori tabellone");
                throw BoardException(a)
            }
        }

        // controllo che non vada sul trono
        if (state!!.getPawn(rowTo, columnTo)!!.equalsPawn(Pawn.THRONE.toString())) {
            // this.loggGame.warning("Mossa sul trono");
            throw ThroneException(a)
        }

        // controllo la casella di arrivo
        if (!state.getPawn(rowTo, columnTo)!!.equalsPawn(Pawn.EMPTY.toString())) {
            // this.loggGame.warning("Mossa sopra una casella occupata");
            throw OccupiedException(a)
        }

        // controllo se cerco di stare fermo
        if (rowFrom == rowTo && columnFrom == columnTo) {
            // this.loggGame.warning("Nessuna mossa");
            throw StopException(a)
        }

        // controllo se sto muovendo una pedina giusta
        if (state.turn?.equalsTurn(Turn.WHITE.toString())!!) {
            if (!state.getPawn(rowFrom, columnFrom)!!.equalsPawn("W")
                && !state.getPawn(rowFrom, columnFrom)!!.equalsPawn("K")
            ) {
                // this.loggGame.warning("Giocatore "+a.turn+" cerca di
                // muovere una pedina avversaria");
                throw PawnException(a)
            }
        }
        if (state.turn!!.equalsTurn(Turn.BLACK.toString())) {
            if (!state.getPawn(rowFrom, columnFrom)!!.equalsPawn("B")) {
                // this.loggGame.warning("Giocatore "+a.turn+" cerca di
                // muovere una pedina avversaria");
                throw PawnException(a)
            }
        }

        // controllo di non muovere in diagonale
        if (rowFrom != rowTo && columnFrom != columnTo) {
            // this.loggGame.warning("Mossa in diagonale");
            throw DiagonalException(a)
        }

        // controllo di non scavalcare pedine
        if (rowFrom == rowTo) {
            if (columnFrom > columnTo) {
                for (i in columnTo until columnFrom) {
                    if (!state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.EMPTY.toString())
                        && !state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.THRONE.toString())
                    ) {
                        // this.loggGame.warning("Mossa che scavalca una
                        // pedina");
                        throw ClimbingException(a)
                    }
                }
            } else {
                for (i in columnFrom + 1..columnTo) {
                    if (!state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.EMPTY.toString())
                        && !state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.THRONE.toString())
                    ) {
                        // this.loggGame.warning("Mossa che scavalca una
                        // pedina");
                        throw ClimbingException(a)
                    }
                }
            }
        } else {
            if (rowFrom > rowTo) {
                for (i in rowTo until rowFrom) {
                    if (!state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.EMPTY.toString())
                        && !state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.THRONE.toString())
                    ) {
                        // this.loggGame.warning("Mossa che scavalca una
                        // pedina");
                        throw ClimbingException(a)
                    }
                }
            } else {
                for (i in rowFrom + 1..rowTo) {
                    if (!state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.EMPTY.toString())
                        && !state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.THRONE.toString())
                    ) {
                        // this.loggGame.warning("Mossa che scavalca una
                        // pedina");
                        throw ClimbingException(a)
                    }
                }
            }
        }

        // se sono arrivato qui, muovo la pedina
        movePawn(state, a)

        // a questo punto controllo lo stato per eventuali catture
        if (state.turn!!.equalsTurn("W")) {
            checkCaptureBlack(state, a)
        }
        if (state.turn!!.equalsTurn("B")) {
            checkCaptureWhite(state, a)
        }

        // this.loggGame.fine("Stato: "+state.toString());
        return state
    }

    private fun movePawn(state: State?, a: Action): State {
        val pawn = state!!.getPawn(a.rowFrom, a.columnFrom)
        val newBoard = state.board
        // State newState = new State();

        // libero il trono o una casella qualunque
        if (a.columnFrom == 4 && a.rowFrom == 4) {
            newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.THRONE
        } else {
            newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.EMPTY
        }

        // metto nel nuovo tabellone la pedina mossa
        newBoard[a.rowTo]!![a.columnTo] = pawn
        // aggiorno il tabellone
        state.board=(newBoard)
        // cambio il turno
        if (state.turn?.equalsTurn(Turn.WHITE.toString())!!) {
            state.turn = (Turn.BLACK)
        } else {
            state.turn = (Turn.WHITE)
        }
        return state
    }

    private fun checkCaptureWhite(state: State?, a: Action): State? {
        // controllo se mangio a destra
        if (state != null) {
            if (a.columnTo < state.board!!.size - 2 && state!!.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("B")
                && (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("W")
                        || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T")
                        || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("K"))
            ) {
                state.removePawn(a.rowTo, a.columnTo + 1)
                movesWithutCapturing = -1
                // this.loggGame.fine("Pedina nera rimossa in:
                // "+state.getBox(a.getRowTo(), a.getColumnTo()+1));
            }
        }
        // controllo se mangio a sinistra
        if (a.columnTo > 1 && state!!.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("B")
            && (state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T")
                    || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("K"))
        ) {
            state.removePawn(a.rowTo, a.columnTo - 1)
            movesWithutCapturing = -1
            // this.loggGame.fine("Pedina nera rimossa in:
            // "+state.getBox(a.getRowTo(), a.getColumnTo()-1));
        }
        // controllo se mangio sopra
        if (a.rowTo > 1 && state!!.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("B")
            && (state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("T")
                    || state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("K"))
        ) {
            state.removePawn(a.rowTo - 1, a.columnTo)
            movesWithutCapturing = -1
            // this.loggGame.fine("Pedina nera rimossa in:
            // "+state.getBox(a.getRowTo()-1, a.getColumnTo()));
        }
        // controllo se mangio sotto
        if (state != null) {
            if (a.rowTo < state.board!!.size - 2 && state!!.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("B")
                && (state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("W")
                        || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T")
                        || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("K"))
            ) {
                state.removePawn(a.rowTo + 1, a.columnTo)
                movesWithutCapturing = -1
                // this.loggGame.fine("Pedina nera rimossa in:
                // "+state.getBox(a.getRowTo()+1, a.getColumnTo()));
            }
        }
        // controllo se ho vinto
        if ((a.rowTo == 0 && a.columnTo == 0 || a.rowTo == 8 && a.columnTo == 0 || a.columnTo == 8 && a.rowTo == 0 || a.columnTo == 8) && a.rowTo == 8) {
            if (state!!.getPawn(a.rowTo, a.columnTo)!!.equalsPawn("K")) {
                state.turn = (Turn.WHITEWIN)
            }
        }

        // controllo il pareggio
        if (state != null) {
            if (movesWithutCapturing >= movesDraw
                && (state.turn!!.equalsTurn("B") || state.turn!!.equalsTurn("W"))
            ) {
                state.turn = (Turn.DRAW)
                // this.loggGame.fine("Stabilito un pareggio per troppe mosse senza
                // mangiare");
            }
        }
        movesWithutCapturing++
        return state
    }

    // TODO da controllare dove indexOutOfBound se controllo di mangiare il re
    private fun checkCaptureBlack(state: State?, a: Action): State? {
        // controllo se mangio a destra
        if (state != null) {
            if (a.columnTo < state.board!!.size - 2 && (state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("W")
                        || state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("K"))
                && (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("B")
                        || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T"))
            ) {
                // nero-re-trono N.B. No indexOutOfBoundException perch� se il re si
                // trovasse sul bordo il giocatore bianco avrebbe gi� vinto
                if (state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("K")
                    && state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T")
                ) {
                    // ho circondato il re?
                    if (state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("B")
                        && state.getPawn(a.rowTo - 1, a.columnTo + 1)!!.equalsPawn("B")
                    ) {
                        state.turn = (Turn.BLACKWIN)
                        // this.loggGame.fine("Nero vince con re catturato in:
                        // "+state.getBox(a.getRowTo(), a.getColumnTo()+1));
                    }
                }
                // nero-re-nero
                if (state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("K")
                    && state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("B")
                ) {
                    // mangio il re?
                    if ((state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("T")
                                || state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("B"))
                        && (state.getPawn(a.rowTo - 1, a.columnTo + 1)!!.equalsPawn("T")
                                || state.getPawn(a.rowTo - 1, a.columnTo + 1)!!.equalsPawn("B"))
                    ) {
                        state.turn = (Turn.BLACKWIN)
                        // this.loggGame.fine("Nero vince con re catturato in:
                        // "+state.getBox(a.getRowTo(), a.getColumnTo()+1));
                    }
                }
                // nero-bianco-trono/nero
                if (state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("W")) {
                    state.removePawn(a.rowTo, a.columnTo + 1)
                    movesWithutCapturing = -1
                    // this.loggGame.fine("Pedina bianca rimossa in:
                    // "+state.getBox(a.getRowTo(), a.getColumnTo()+1));
                }
            }
        }
        // controllo se mangio a sinistra
        if (a.columnTo > 1 && (state!!.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("K"))
            && (state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("B")
                    || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T"))
        ) {
            // trono-re-nero
            if (state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("K")
                && state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T")
            ) {
                // ho circondato il re?
                if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B")
                    && state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("B")
                ) {
                    state.turn = (Turn.BLACKWIN)
                    // this.loggGame.fine("Nero vince con re catturato in:
                    // "+state.getBox(a.getRowTo(), a.getColumnTo()+1));
                }
            }
            // nero-re-nero
            if (state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("K")
                && state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("B")
            ) {
                // mangio il re?
                if ((state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("T")
                            || state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B"))
                    && (state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("T")
                            || state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("B"))
                ) {
                    state.turn = (Turn.BLACKWIN)
                    // this.loggGame.fine("Nero vince con re catturato in:
                    // "+state.getBox(a.getRowTo(), a.getColumnTo()+1));
                }
            }
            // trono/nero-bianco-nero
            if (state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("W")) {
                state.removePawn(a.rowTo, a.columnTo - 1)
                movesWithutCapturing = -1
                // this.loggGame.fine("Pedina bianca rimossa in:
                // "+state.getBox(a.getRowTo(), a.getColumnTo()-1));
            }
        }
        // controllo se mangio sopra
        if (a.rowTo > 1 && (state!!.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("K"))
            && (state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("B")
                    || state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("T"))
        ) {
            // nero-re-trono
            if (state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("K")
                && state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("T")
            ) {
                // ho circondato re?
                if (state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("B")
                    && state.getPawn(a.rowTo - 1, a.columnTo + 1)!!.equalsPawn("B")
                ) {
                    state.turn = (Turn.BLACKWIN)
                    // this.loggGame.fine("Nero vince con re catturato in:
                    // "+state.getBox(a.getRowTo()-1, a.getColumnTo()));
                }
            }
            // nero-re-nero
            if (state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("K")
                && state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("B")
            ) {
                // mangio il re?
                if ((state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("T")
                            || state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("B"))
                    && (state.getPawn(a.rowTo - 1, a.columnTo + 1)!!.equalsPawn("T")
                            || state.getPawn(a.rowTo - 1, a.columnTo + 1)!!.equalsPawn("B"))
                ) {
                    state.turn = (Turn.BLACKWIN)
                    // this.loggGame.fine("Nero vince con re catturato in:
                    // "+state.getBox(a.getRowTo(), a.getColumnTo()-1));
                }
            }
            // nero-bianco-trono/nero
            if (state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("W")) {
                state.removePawn(a.rowTo - 1, a.columnTo)
                movesWithutCapturing = -1
                // this.loggGame.fine("Pedina bianca rimossa in:
                // "+state.getBox(a.getRowTo()-1, a.getColumnTo()));
            }
        }
        // controllo se mangio sotto
        if (state != null) {
            if (a.rowTo < state.board!!.size - 2 && (state!!.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("W")
                        || state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("K"))
                && (state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("B")
                        || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T"))
            ) {
                // nero-re-trono
                if (state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("K")
                    && state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T")
                ) {
                    // ho circondato re?
                    if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B")
                        && state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("B")
                    ) {
                        state.turn = (Turn.BLACKWIN)
                        // this.loggGame.fine("Nero vince con re catturato in:
                        // "+state.getBox(a.getRowTo()-1, a.getColumnTo()));
                    }
                }
                // nero-re-nero
                if (state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("K")
                    && state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("B")
                ) {
                    // mangio il re?
                    if ((state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("T")
                                || state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B"))
                        && (state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("T")
                                || state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("B"))
                    ) {
                        state.turn = (Turn.BLACKWIN)
                        // this.loggGame.fine("Nero vince con re catturato in:
                        // "+state.getBox(a.getRowTo(), a.getColumnTo()-1));
                    }
                }
                // nero-bianco-trono/nero
                if (state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("W")) {
                    state.removePawn(a.rowTo + 1, a.columnTo)
                    movesWithutCapturing = -1
                    // this.loggGame.fine("Pedina bianca rimossa in:
                    // "+state.getBox(a.getRowTo()+1, a.getColumnTo()));
                }
            }
        }
        // controllo regola 11
        if (state != null) {
            if (state.board!!.size == 9) {
                if (a.columnTo == 4 && a.rowTo == 2) {
                    if (state!!.getPawn(3, 4)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K")
                        && state.getPawn(4, 3)!!.equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B")
                        && state.getPawn(5, 4)!!.equalsPawn("B")
                    ) {
                        state.removePawn(3, 4)
                        movesWithutCapturing = -1
                        // this.loggGame.fine("Pedina bianca rimossa in:
                        // "+state.getBox(3, 4));
                    }
                }
                if (a.columnTo == 4 && a.rowTo == 6) {
                    if (state!!.getPawn(5, 4)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K")
                        && state.getPawn(4, 3)!!.equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B")
                        && state.getPawn(3, 4)!!.equalsPawn("B")
                    ) {
                        state.removePawn(5, 4)
                        movesWithutCapturing = -1
                        // this.loggGame.fine("Pedina bianca rimossa in:
                        // "+state.getBox(5, 4));
                    }
                }
                if (a.columnTo == 2 && a.rowTo == 4) {
                    if (state!!.getPawn(4, 3)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K")
                        && state.getPawn(3, 4)!!.equalsPawn("B") && state.getPawn(5, 4)!!.equalsPawn("B")
                        && state.getPawn(4, 5)!!.equalsPawn("B")
                    ) {
                        state.removePawn(4, 3)
                        movesWithutCapturing = -1
                        // this.loggGame.fine("Pedina bianca rimossa in:
                        // "+state.getBox(4, 3));
                    }
                }
                if (a.columnTo == 6 && a.rowTo == 4) {
                    if (state!!.getPawn(4, 5)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K")
                        && state.getPawn(4, 3)!!.equalsPawn("B") && state.getPawn(5, 4)!!.equalsPawn("B")
                        && state.getPawn(3, 4)!!.equalsPawn("B")
                    ) {
                        state.removePawn(4, 5)
                        movesWithutCapturing = -1
                        // this.loggGame.fine("Pedina bianca rimossa in:
                        // "+state.getBox(4, 5));
                    }
                }
            }
        }

        // controllo il pareggio
        if (state != null) {
            if (movesWithutCapturing >= movesDraw
                && (state.turn!!.equalsTurn("B") || state.turn!!.equalsTurn("W"))
            ) {
                state.turn = (Turn.DRAW)
                // this.loggGame.fine("Stabilito un pareggio per troppe mosse senza
                // mangiare");
            }
        }
        movesWithutCapturing++
        return state
    }

    // TODO: Implement this
    override fun endGame(state: State?) {}
}