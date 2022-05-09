package it.unibo.ai.didattica.competition.tablut.domain

import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.exceptions.*
import java.io.File
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

/**
 *
 * This class represents the pure game, with all the rules;
 * it check the move, the state of the match and is a pawn is eliminated or not
 * @author A.Piretti
 */
class GameTablut @JvmOverloads constructor(private val movesDraw: Int = 0) : Game {
    private var movesWithutCapturing = 0
    var gameLog: File? = null
    private val loggGame: Logger

    init {
        val gameLogName = Date().time.toString() + "_gameLog.txt"
        gameLog = File(gameLogName)
        var fh: FileHandler? = null
        try {
            fh = FileHandler(gameLogName, true)
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(1)
        }
        loggGame = Logger.getLogger("GameLog")
        loggGame.addHandler(fh)
        fh!!.formatter = SimpleFormatter()
        loggGame.level = Level.FINE
        loggGame.fine("Inizio partita")
    }

    /**
     * This method checks an action in a state: if it is correct the state is going to be changed,
     * if it is wrong it throws a specific exception
     *
     * @param state the state of the game
     * @param a the action to be analyzed
     * @return the new state of the game
     * @throws BoardException try to move a pawn out of the board
     * @throws ActionException the format of the action is wrong
     * @throws StopException try to not move any pawn
     * @throws PawnException try to move an enemy pawn
     * @throws DiagonalException try to move a pawn diagonally
     * @throws ClimbingException try to climb over another pawn
     * @throws ThroneException try to move a pawn into the throne box
     * @throws OccupiedException try to move a pawn into an ccupited box
     */
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
        //this.loggGame.fine(a.toString());
        //controllo la mossa
        if (a.to!!.length != 2 || a.from!!.length != 2) {
            loggGame.warning("Formato mossa errato")
            throw ActionException(a)
        }
        val columnFrom = a.columnFrom
        val columnTo = a.columnTo
        val rowFrom = a.rowFrom
        val rowTo = a.rowTo

        //controllo se sono fuori dal tabellone
        if (state != null) {
            if (columnFrom > state.board!!.size - 1 || rowFrom > state.board!!.size - 1 || rowTo > state.board!!.size - 1 || columnTo > state.board!!.size - 1 || columnFrom < 0 || rowFrom < 0 || rowTo < 0 || columnTo < 0) {
                loggGame.warning("Mossa fuori tabellone")
                throw BoardException(a)
            }
        }

        //controllo che non vada sul trono
        if (state!!.getPawn(rowTo, columnTo)!!.equalsPawn(Pawn.THRONE.toString())) {
            loggGame.warning("Mossa sul trono")
            throw ThroneException(a)
        }

        //controllo la casella di arrivo
        if (!state.getPawn(rowTo, columnTo)!!.equalsPawn(Pawn.EMPTY.toString())) {
            loggGame.warning("Mossa sopra una casella occupata")
            throw OccupiedException(a)
        }

        //controllo se cerco di stare fermo
        if (rowFrom == rowTo && columnFrom == columnTo) {
            loggGame.warning("Nessuna mossa")
            throw StopException(a)
        }

        //controllo se sto muovendo una pedina giusta
        if (state.turn!!.equalsTurn(Turn.WHITE.toString())) {
            if (!state.getPawn(rowFrom, columnFrom)!!.equalsPawn("W") && !state.getPawn(rowFrom, columnFrom)!!
                    .equalsPawn("K")
            ) {
                loggGame.warning("Giocatore " + a.turn + " cerca di muovere una pedina avversaria")
                throw PawnException(a)
            }
        }
        if (state.turn!!.equalsTurn(Turn.BLACK.toString())) {
            if (!state.getPawn(rowFrom, columnFrom)!!.equalsPawn("B")) {
                loggGame.warning("Giocatore " + a.turn + " cerca di muovere una pedina avversaria")
                throw PawnException(a)
            }
        }

        //controllo di non muovere in diagonale
        if (rowFrom != rowTo && columnFrom != columnTo) {
            loggGame.warning("Mossa in diagonale")
            throw DiagonalException(a)
        }

        //controllo di non scavalcare pedine
        if (rowFrom == rowTo) {
            if (columnFrom > columnTo) {
                for (i in columnTo until columnFrom) {
                    if (!state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.EMPTY.toString())) {
                        loggGame.warning("Mossa che scavalca una pedina")
                        throw ClimbingException(a)
                    }
                }
            } else {
                for (i in columnFrom + 1..columnTo) {
                    if (!state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.EMPTY.toString())) {
                        loggGame.warning("Mossa che scavalca una pedina")
                        throw ClimbingException(a)
                    }
                }
            }
        } else {
            if (rowFrom > rowTo) {
                for (i in rowTo until rowFrom) {
                    if (!state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.EMPTY.toString()) && !state.getPawn(
                            i,
                            columnFrom
                        )!!
                            .equalsPawn(Pawn.THRONE.toString())
                    ) {
                        loggGame.warning("Mossa che scavalca una pedina")
                        throw ClimbingException(a)
                    }
                }
            } else {
                for (i in rowFrom + 1..rowTo) {
                    if (!state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.EMPTY.toString()) && !state.getPawn(
                            i,
                            columnFrom
                        )!!
                            .equalsPawn(Pawn.THRONE.toString())
                    ) {
                        loggGame.warning("Mossa che scavalca una pedina")
                        throw ClimbingException(a)
                    }
                }
            }
        }

        //se sono arrivato qui, muovo la pedina
        movePawn(state, a)

        //a questo punto controllo lo stato per eventuali catture
        if (state.turn!!.equalsTurn("W")) {
            checkCaptureBlack(state, a)
        }
        if (state.turn!!.equalsTurn("B")) {
            checkCaptureWhite(state, a)
        }
        loggGame.fine("Stato: $state")
        return state
    }

    /**
     * This method move the pawn in the board
     * @param state is the initial state
     * @param a is the action of a pawn
     * @return is the new state of the game with the moved pawn
     */
    private fun movePawn(state: State?, a: Action): State {
        val pawn = state!!.getPawn(a.rowFrom, a.columnFrom)
        val newBoard = state.board
        //State newState = new State();
        loggGame.fine("Movimento pedina")
        //libero il trono o una casella qualunque
        if (newBoard!!.size == 9) {
            if (a.columnFrom == 4 && a.rowFrom == 4) {
                newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.THRONE
            } else {
                newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.EMPTY
            }
        }
        if (newBoard!!.size == 7) {
            if (a.columnFrom == 3 && a.rowFrom == 3) {
                newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.THRONE
            } else {
                newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.EMPTY
            }
        }

        //metto nel nuovo tabellone la pedina mossa
        newBoard!![a.rowTo]!![a.columnTo] = pawn
        //aggiorno il tabellone
        state.board=(newBoard)
        //cambio il turno
        if (state.turn!!.equalsTurn(Turn.WHITE.toString())) {
            state.turn=(Turn.BLACK)
        } else {
            state.turn=(Turn.WHITE)
        }
        return state
    }

    /**
     * This method check if a pawn is captured and if the game ends
     * @param state the state of the game
     * @param a the action of the previous moved pawn
     * @return the new state of the game
     */
    private fun checkCaptureWhite(state: State?, a: Action): State? {
        //controllo se mangio a destra
        if (state != null) {
            if (a.columnTo < state.board!!.size - 2 && state!!.getPawn(a.rowTo, a.columnTo + 1)!!
                    .equalsPawn("B") && (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("W") || state.getPawn(
                    a.rowTo,
                    a.columnTo + 2
                )!!
                    .equalsPawn("T") || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("K"))
            ) {
                state.removePawn(a.rowTo, a.columnTo + 1)
                movesWithutCapturing = -1
                loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
            }
        }
        //controllo se mangio a sinistra
        if (a.columnTo > 1 && state!!.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("B") && (state.getPawn(
                a.rowTo,
                a.columnTo - 2
            )!!
                .equalsPawn("W") || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T") || state.getPawn(
                a.rowTo,
                a.columnTo - 2
            )!!
                .equalsPawn("K"))
        ) {
            state.removePawn(a.rowTo, a.columnTo - 1)
            movesWithutCapturing = -1
            loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo, a.columnTo - 1))
        }
        //controllo se mangio sopra
        if (a.rowTo > 1 && state!!.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("B") && (state.getPawn(
                a.rowTo - 2,
                a.columnTo
            )!!
                .equalsPawn("W") || state.getPawn(a.rowTo - 2, a.columnTo)!!
                .equalsPawn("T") || state.getPawn(a.rowTo - 2, a.columnTo)!!
                .equalsPawn("K"))
        ) {
            state.removePawn(a.rowTo - 1, a.columnTo)
            movesWithutCapturing = -1
            loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo - 1, a.columnTo))
        }
        //controllo se mangio sotto
        if (state != null) {
            if (a.rowTo < state.board!!.size - 2 && state!!.getPawn(a.rowTo + 1, a.columnTo)!!
                    .equalsPawn("B") && (state.getPawn(a.rowTo + 2, a.columnTo)!!
                    .equalsPawn("W") || state.getPawn(a.rowTo + 2, a.columnTo)!!
                    .equalsPawn("T") || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("K"))
            ) {
                state.removePawn(a.rowTo + 1, a.columnTo)
                movesWithutCapturing = -1
                loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo + 1, a.columnTo))
            }
        }
        //controllo se ho vinto
        if (state != null) {
            if (a.rowTo == 0 || a.rowTo == state.board!!.size - 1 || a.columnTo == 0 || a.columnTo == state.board!!.size - 1) {
                if (state!!.getPawn(a.rowTo, a.columnTo)!!.equalsPawn("K")) {
                    state.turn=(Turn.WHITEWIN)
                    loggGame.fine("Bianco vince con re in " + a.to)
                }
            }
        }

        //controllo il pareggio
        if (state != null) {
            if (movesWithutCapturing >= movesDraw && (state.turn!!.equalsTurn("B") || state.turn!!.equalsTurn("W"))) {
                state.turn=(Turn.DRAW)
                loggGame.fine("Stabilito un pareggio per troppe mosse senza mangiare")
            }
        }
        movesWithutCapturing++
        return state
    }

    /**
     * This method check if a pawn is captured and if the game ends
     * @param state the state of the game
     * @param a the action of the previous moved pawn
     * @return the new state of the game
     */
    private fun checkCaptureBlack(state: State?, a: Action): State {
        //controllo se mangio a destra
        if (state != null) {
            if (a.columnTo < state.board!!.size - 2 && (state!!.getPawn(a.rowTo, a.columnTo + 1)!!
                    .equalsPawn("W") || state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("K")) && (state.getPawn(
                    a.rowTo,
                    a.columnTo + 2
                )!!
                    .equalsPawn("B") || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T"))
            ) {
                //nero-re-trono N.B. No indexOutOfBoundException perch� se il re si trovasse sul bordo il giocatore bianco avrebbe gi� vinto
                if (state.getPawn(a.rowTo, a.columnTo + 1)!!
                        .equalsPawn("K") && state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T")
                ) {
                    //ho circondato su 3 lati il re?
                    if (state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("B") && state.getPawn(
                            a.rowTo - 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("B")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                    }
                }
                //nero-re-nero
                if (state.getPawn(a.rowTo, a.columnTo + 1)!!
                        .equalsPawn("K") && state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("B")
                ) {
                    //mangio il re?
                    if (!state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("T") && !state.getPawn(
                            a.rowTo - 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("T")
                    ) {
                        if (!(a.rowTo * 2 + 1 == 9 && state.board!!.size == 9) && !(a.rowTo * 2 + 1 == 7 && state.board!!.size == 7)) {
                            state.turn=(Turn.BLACKWIN)
                            loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                        }
                    }
                    //ho circondato su 3 lati il re?
                    if (state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("B") && state.getPawn(
                            a.rowTo - 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("T")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                    }
                    if (state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("T") && state.getPawn(
                            a.rowTo - 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("B")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                    }
                }
                //nero-bianco-trono/nero
                if (state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("W")) {
                    state.removePawn(a.rowTo, a.columnTo + 1)
                    movesWithutCapturing = -1
                    loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
                }
            }
        }
        //controllo se mangio a sinistra
        if (a.columnTo > 1 && (state!!.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("W") || state.getPawn(
                a.rowTo,
                a.columnTo - 1
            )!!
                .equalsPawn("K")) && (state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("B") || state.getPawn(
                a.rowTo,
                a.columnTo - 2
            )!!
                .equalsPawn("T"))
        ) {
            //trono-re-nero
            if (state.getPawn(a.rowTo, a.columnTo - 1)!!
                    .equalsPawn("K") && state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T")
            ) {
                //ho circondato su 3 lati il re?
                if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B") && state.getPawn(
                        a.rowTo - 1,
                        a.columnTo - 1
                    )!!
                        .equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
            //nero-re-nero
            if (state.getPawn(a.rowTo, a.columnTo - 1)!!
                    .equalsPawn("K") && state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("B")
            ) {
                //mangio il re?
                if (!state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("T") && !state.getPawn(
                        a.rowTo - 1,
                        a.columnTo - 1
                    )!!
                        .equalsPawn("T")
                ) {
                    if (!(a.rowTo * 2 + 1 == 9 && state.board!!.size == 9) && !(a.rowTo * 2 + 1 == 7 && state.board!!.size == 7)) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                    }
                }
                //ho circondato su 3 lati il re?
                if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B") && state.getPawn(
                        a.rowTo - 1,
                        a.columnTo - 1
                    )!!
                        .equalsPawn("T")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
                if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("T") && state.getPawn(
                        a.rowTo - 1,
                        a.columnTo - 1
                    )!!
                        .equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
            //trono/nero-bianco-nero
            if (state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("W")) {
                state.removePawn(a.rowTo, a.columnTo - 1)
                movesWithutCapturing = -1
                loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo - 1))
            }
        }
        //controllo se mangio sopra
        if (a.rowTo > 1 && (state!!.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("W") || state.getPawn(
                a.rowTo - 1,
                a.columnTo
            )!!
                .equalsPawn("K")) && (state.getPawn(a.rowTo - 2, a.columnTo)!!
                .equalsPawn("B") || state.getPawn(a.rowTo - 2, a.columnTo)!!
                .equalsPawn("T"))
        ) {
            //nero-re-trono 
            if (state.getPawn(a.rowTo - 1, a.columnTo)!!
                    .equalsPawn("K") && state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("T")
            ) {
                //ho circondato su 3 lati il re?
                if (state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("B") && state.getPawn(
                        a.rowTo - 1,
                        a.columnTo + 1
                    )!!
                        .equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo - 1, a.columnTo))
                }
            }
            //nero-re-nero
            if (state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("K") && state.getPawn(a.rowTo - 2, a.columnTo)!!
                    .equalsPawn("B")
            ) {
                //ho circondato su 3 lati il re?
                if (state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("B") && state.getPawn(
                        a.rowTo - 1,
                        a.columnTo + 1
                    )!!
                        .equalsPawn("T")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
                if (state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("T") && state.getPawn(
                        a.rowTo - 1,
                        a.columnTo + 1
                    )!!
                        .equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
                //mangio il re?
                if (!state.getPawn(a.rowTo - 1, a.columnTo - 1)!!.equalsPawn("T") && !state.getPawn(
                        a.rowTo - 1,
                        a.columnTo + 1
                    )!!
                        .equalsPawn("T")
                ) {
                    if (!(a.rowTo * 2 + 1 == 9 && state.board!!.size == 9) && !(a.rowTo * 2 + 1 == 7 && state.board!!.size == 7)) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                    }
                }
            }
            //nero-bianco-trono/nero
            if (state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("W")) {
                state.removePawn(a.rowTo - 1, a.columnTo)
                movesWithutCapturing = -1
                loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo - 1, a.columnTo))
            }
        }
        //controllo se mangio sotto
        if (state != null) {
            if (a.rowTo < state.board!!.size - 2 && (state!!.getPawn(a.rowTo + 1, a.columnTo)!!
                    .equalsPawn("W") || state.getPawn(a.rowTo + 1, a.columnTo)!!
                    .equalsPawn("K")) && (state.getPawn(a.rowTo + 2, a.columnTo)!!
                    .equalsPawn("B") || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T"))
            ) {
                //nero-re-trono
                if (state.getPawn(a.rowTo + 1, a.columnTo)!!
                        .equalsPawn("K") && state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T")
                ) {
                    //ho circondato su 3 lati il re?
                    if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B") && state.getPawn(
                            a.rowTo + 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("B")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                }
                //nero-re-nero
                if (state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("K") && state.getPawn(a.rowTo + 2, a.columnTo)!!
                        .equalsPawn("B")
                ) {
                    //ho circondato su 3 lati il re?
                    if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("B") && state.getPawn(
                            a.rowTo + 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("T")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                    if (state.getPawn(a.rowTo + 1, a.columnTo - 1)!!.equalsPawn("T") && state.getPawn(
                            a.rowTo + 1,
                            a.columnTo + 1
                        )!!
                            .equalsPawn("B")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                    //mangio il re?
                    if (!state.getPawn(a.rowTo + 1, a.columnTo + 1)!!.equalsPawn("T") && !state.getPawn(
                            a.rowTo + 1,
                            a.columnTo - 1
                        )!!
                            .equalsPawn("T")
                    ) {
                        if (!(a.rowTo * 2 + 1 == 9 && state.board!!.size == 9) && !(a.rowTo * 2 + 1 == 7 && state.board!!.size == 7)) {
                            state.turn=(Turn.BLACKWIN)
                            loggGame.fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                        }
                    }
                }
                //nero-bianco-trono/nero
                if (state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("W")) {
                    state.removePawn(a.rowTo + 1, a.columnTo)
                    movesWithutCapturing = -1
                    loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo + 1, a.columnTo))
                }
            }
        }
        //controllo il re completamente circondato
        if (state!!.getPawn(4, 4)!!.equalsPawn(Pawn.KING.toString()) && state.board!!.size == 9) {
            if (state.getPawn(3, 4)!!.equalsPawn("B") && state.getPawn(4, 3)!!.equalsPawn("B") && state.getPawn(5, 4)!!
                    .equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B")
            ) {
                state.turn=(Turn.BLACKWIN)
                loggGame.fine("Nero vince con re catturato sul trono")
            }
        }
        if (state.getPawn(3, 3)!!.equalsPawn(Pawn.KING.toString()) && state.board!!.size == 7) {
            if (state.getPawn(3, 4)!!.equalsPawn("B") && state.getPawn(4, 3)!!.equalsPawn("B") && state.getPawn(2, 3)!!
                    .equalsPawn("B") && state.getPawn(3, 2)!!.equalsPawn("B")
            ) {
                state.turn=(Turn.BLACKWIN)
                loggGame.fine("Nero vince con re catturato sul trono")
            }
        }
        //controllo regola 11
        if (state.board!!.size == 9) {
            if (a.columnTo == 4 && a.rowTo == 2) {
                if (state.getPawn(3, 4)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K") && state.getPawn(
                        4,
                        3
                    )!!
                        .equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B") && state.getPawn(5, 4)!!
                        .equalsPawn("B")
                ) {
                    state.removePawn(3, 4)
                    movesWithutCapturing = -1
                    loggGame.fine("Pedina bianca rimossa in: " + state.getBox(3, 4))
                }
            }
            if (a.columnTo == 4 && a.rowTo == 6) {
                if (state.getPawn(5, 4)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K") && state.getPawn(
                        4,
                        3
                    )!!
                        .equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B") && state.getPawn(3, 4)!!
                        .equalsPawn("B")
                ) {
                    state.removePawn(5, 4)
                    movesWithutCapturing = -1
                    loggGame.fine("Pedina bianca rimossa in: " + state.getBox(5, 4))
                }
            }
            if (a.columnTo == 2 && a.rowTo == 4) {
                if (state.getPawn(4, 3)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K") && state.getPawn(
                        3,
                        4
                    )!!
                        .equalsPawn("B") && state.getPawn(5, 4)!!.equalsPawn("B") && state.getPawn(4, 5)!!
                        .equalsPawn("B")
                ) {
                    state.removePawn(4, 3)
                    movesWithutCapturing = -1
                    loggGame.fine("Pedina bianca rimossa in: " + state.getBox(4, 3))
                }
            }
            if (a.columnTo == 6 && a.rowTo == 4) {
                if (state.getPawn(4, 5)!!.equalsPawn("W") && state.getPawn(4, 4)!!.equalsPawn("K") && state.getPawn(
                        4,
                        3
                    )!!
                        .equalsPawn("B") && state.getPawn(5, 4)!!.equalsPawn("B") && state.getPawn(3, 4)!!
                        .equalsPawn("B")
                ) {
                    state.removePawn(4, 5)
                    movesWithutCapturing = -1
                    loggGame.fine("Pedina bianca rimossa in: " + state.getBox(4, 5))
                }
            }
        }


        //controllo il pareggio
        if (movesWithutCapturing >= movesDraw && (state.turn!!.equalsTurn("B") || state.turn!!.equalsTurn("W"))) {
            state.turn=(Turn.DRAW)
            loggGame.fine("Stabilito un pareggio per troppe mosse senza mangiare")
        }
        movesWithutCapturing++
        return state
    }

    override fun endGame(state: State?) {
        loggGame.fine("Stato: " + state.toString())
    }
}