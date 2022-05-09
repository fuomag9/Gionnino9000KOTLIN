package it.unibo.ai.didattica.competition.tablut.domain

import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.gionnino9000.heuristics.BlackHeuristics
import it.unibo.ai.didattica.competition.tablut.gionnino9000.heuristics.WhiteHeuristics
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.system.exitProcess

/**
 * Game engine inspired by the Ashton Rules of Tablut
 *
 * @author A. Piretti, Andrea Galassi (extended by Gionnino9000)
 */
class GameAshtonTablut(
    state: State,
    /**
     * Number of repeated states that can occur before a draw
     */
    private val repeated_moves_allowed: Int,
    /**
     * Number of states kept in memory. negative value means infinite.
     */
    private val cache_size: Int, logs_folder: String,
    whiteName: String, blackName: String
) : Game, Cloneable, aima.core.search.adversarial.Game<State?, Action, Turn?> {

    /**
     * Counter for the moves without capturing that have occurred
     */
    private var movesWithutCapturing = 0
        private set
    private var gameLog: File? = null
    private var fh: FileHandler? = null
    private val loggGame: Logger
    private val citadels: MutableList<String?>

    // private List<String> strangeCitadels;
    private val drawConditions: MutableList<State?>

    constructor(
        repeated_moves_allowed: Int,
        cache_size: Int,
        logs_folder: String,
        whiteName: String,
        blackName: String
    ) : this(StateTablut(), repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName)

    init {
        var p = Paths.get(
            logs_folder + File.separator + "_" + whiteName + "_vs_" + blackName + "_"
                    + Date().time + "_gameLog.txt"
        )
        p = p.toAbsolutePath()
        val gameLogName = p.toString()
        val gamefile = File(gameLogName)
        try {
            val f = File(logs_folder)
            f.mkdirs()
            if (!gamefile.exists()) {
                gamefile.createNewFile()
            }
            gameLog = gamefile
            fh = null
            fh = FileHandler(gameLogName, true)
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
        loggGame = Logger.getLogger("GameLog")
        loggGame.useParentHandlers = false
        loggGame.addHandler(fh)
        fh!!.formatter = SimpleFormatter()
        loggGame.level = Level.OFF
        loggGame.fine("Players:\t$whiteName\tvs\t$blackName")
        loggGame.fine("Repeated moves allowed:\t$repeated_moves_allowed\tCache:\t$cache_size")
        loggGame.fine("Inizio partita")
        loggGame.fine("Stato:\n$state")
        drawConditions = ArrayList()
        citadels = ArrayList()
        // this.strangeCitadels = new ArrayList<String>();
        citadels.add("a4")
        citadels.add("a5")
        citadels.add("a6")
        citadels.add("b5")
        citadels.add("d1")
        citadels.add("e1")
        citadels.add("f1")
        citadels.add("e2")
        citadels.add("i4")
        citadels.add("i5")
        citadels.add("i6")
        citadels.add("h5")
        citadels.add("d9")
        citadels.add("e9")
        citadels.add("f9")
        citadels.add("e8")
    }

    /**
     * Method to check wheter an action is allowed or not for a given state. If it is not, throws and Exception.
     *
     * @param state        Current state of the game
     * @param action    The action to be checked
     *
     * @return            the resulting State obtained by performing the given
     */
    @Throws(CloneNotSupportedException::class)
    override fun checkMove(state: State?, action: Action): State? {
        if (isPossibleMove(state, action)) {

            // se sono arrivato qui, muovo la pedina
            movePawn(state, action)

            // a questo punto controllo lo stato per eventuali catture
            if (state?.turn?.equalsTurn("W")!!) {
                checkCaptureBlack(state, action)
            } else if (state.turn!!.equalsTurn("B")) {
                checkCaptureWhite(state, action)
            }

            // if something has been captured, clear cache for draws
            if (movesWithutCapturing == 0) {
                drawConditions.clear()
                loggGame.fine("Capture! Draw cache cleared!")
            }

            // controllo pareggio
            var trovati = 0
            for (s in drawConditions) {
                println(s.toString())
                if (s == state) {
                    trovati++
                    if (trovati > repeated_moves_allowed) {
                        state.turn=(Turn.DRAW)
                        loggGame.fine("Partita terminata in pareggio per numero di stati ripetuti")
                        break
                    }
                } // DEBUG: //
            }
            if (trovati > 0) {
                loggGame.fine("Equal states found: $trovati")
            }
            if (cache_size >= 0 && drawConditions.size > cache_size) {
                drawConditions.removeAt(0)
            }
            drawConditions.add(state.clone())
            loggGame.fine("Current draw cache size: " + drawConditions.size)
            loggGame.fine("Stato:\n$state")
            println("Stato:\n$state")
            return state
        }
        return null
    }

    private fun checkCaptureWhite(state: State, a: Action): State {
        // controllo se mangio a destra
        if (a.columnTo < state.board!!.size - 2 && state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("B")
            && (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T")
                    || state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("K")
                    || (citadels.contains(state.getBox(a.rowTo, a.columnTo + 2))
                    && !(a.columnTo + 2 == 8 && a.rowTo == 4)
                    && !(a.columnTo + 2 == 4 && a.rowTo == 0)
                    && !(a.columnTo + 2 == 4 && a.rowTo == 8)
                    && !(a.columnTo + 2 == 0 && a.rowTo == 4)))
        ) {
            state.removePawn(a.rowTo, a.columnTo + 1)
            movesWithutCapturing = -1
            loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
        }
        // controllo se mangio a sinistra
        if (a.columnTo > 1 && state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("B")
            && (state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T")
                    || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("K")
                    || (citadels.contains(state.getBox(a.rowTo, a.columnTo - 2))
                    && !(a.columnTo - 2 == 8 && a.rowTo == 4)
                    && !(a.columnTo - 2 == 4 && a.rowTo == 0)
                    && !(a.columnTo - 2 == 4 && a.rowTo == 8)
                    && !(a.columnTo - 2 == 0 && a.rowTo == 4)))
        ) {
            state.removePawn(a.rowTo, a.columnTo - 1)
            movesWithutCapturing = -1
            loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo, a.columnTo - 1))
        }
        // controllo se mangio sopra
        if (a.rowTo > 1 && state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("B")
            && (state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("T")
                    || state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("K")
                    || (citadels.contains(state.getBox(a.rowTo - 2, a.columnTo))
                    && !(a.columnTo == 8 && a.rowTo - 2 == 4)
                    && !(a.columnTo == 4 && a.rowTo - 2 == 0)
                    && !(a.columnTo == 4 && a.rowTo - 2 == 8)
                    && !(a.columnTo == 0 && a.rowTo - 2 == 4)))
        ) {
            state.removePawn(a.rowTo - 1, a.columnTo)
            movesWithutCapturing = -1
            loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo - 1, a.columnTo))
        }
        // controllo se mangio sotto
        if (a.rowTo < state.board!!.size - 2 && state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("B")
            && (state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("W")
                    || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T")
                    || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("K")
                    || (citadels.contains(state.getBox(a.rowTo + 2, a.columnTo))
                    && !(a.columnTo == 8 && a.rowTo + 2 == 4)
                    && !(a.columnTo == 4 && a.rowTo + 2 == 0)
                    && !(a.columnTo == 4 && a.rowTo + 2 == 8)
                    && !(a.columnTo == 0 && a.rowTo + 2 == 4)))
        ) {
            state.removePawn(a.rowTo + 1, a.columnTo)
            movesWithutCapturing = -1
            loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.rowTo + 1, a.columnTo))
        }
        // controllo se ho vinto
        if (a.rowTo == 0 || a.rowTo == state.board!!.size - 1 || a.columnTo == 0 || a.columnTo == state.board!!.size - 1) {
            if (state.getPawn(a.rowTo, a.columnTo)!!.equalsPawn("K")) {
                state.turn=(Turn.WHITEWIN)
                loggGame.fine("Bianco vince con re in " + a.to)
            }
        }
        // TODO: implement the winning condition of the capture of the last
        // black checker
        movesWithutCapturing++
        return state
    }

    private fun checkCaptureBlackKingLeft(state: State?, a: Action) {
        // ho il re sulla sinistra
        if (a.columnTo > 1 && state!!.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("K")) {
            //System.out.println("Ho il re sulla sinistra");
            // re sul trono
            if (state.getBox(a.rowTo, a.columnTo - 1) == "e5") {
                if (state.getPawn(3, 4)!!.equalsPawn("B") && state.getPawn(4, 3)!!.equalsPawn("B")
                    && state.getPawn(5, 4)!!.equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
            // re adiacente al trono
            if (state.getBox(a.rowTo, a.columnTo - 1) == "e4") {
                if (state.getPawn(2, 4)!!.equalsPawn("B") && state.getPawn(3, 3)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
            if (state.getBox(a.rowTo, a.columnTo - 1) == "f5") {
                if (state.getPawn(5, 5)!!.equalsPawn("B") && state.getPawn(3, 5)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
            if (state.getBox(a.rowTo, a.columnTo - 1) == "e6") {
                if (state.getPawn(6, 4)!!.equalsPawn("B") && state.getPawn(5, 3)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
            // sono fuori dalle zone del trono
            if (state.getBox(a.rowTo, a.columnTo - 1) != "e5"
                && state.getBox(a.rowTo, a.columnTo - 1) != "e6"
                && state.getBox(a.rowTo, a.columnTo - 1) != "e4"
                && state.getBox(a.rowTo, a.columnTo - 1) != "f5"
            ) {
                if (state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("B")
                    || citadels.contains(state.getBox(a.rowTo, a.columnTo - 2))
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo - 1))
                }
            }
        }
    }

    private fun checkCaptureBlackKingRight(state: State, a: Action) {
        // ho il re sulla destra
        if (a.columnTo < state.board!!.size - 2
            && state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("K")
        ) {
            //System.out.println("Ho il re sulla destra");
            // re sul trono
            if (state.getBox(a.rowTo, a.columnTo + 1) == "e5") {
                if (state.getPawn(3, 4)!!.equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B")
                    && state.getPawn(5, 4)!!.equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                }
            }
            // re adiacente al trono
            if (state.getBox(a.rowTo, a.columnTo + 1) == "e4") {
                if (state.getPawn(2, 4)!!.equalsPawn("B") && state.getPawn(3, 5)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                }
            }
            if (state.getBox(a.rowTo, a.columnTo + 1) == "e6") {
                if (state.getPawn(5, 5)!!.equalsPawn("B") && state.getPawn(6, 4)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                }
            }
            if (state.getBox(a.rowTo, a.columnTo + 1) == "d5") {
                if (state.getPawn(3, 3)!!.equalsPawn("B") && state.getPawn(5, 3)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                }
            }
            // sono fuori dalle zone del trono
            if (state.getBox(a.rowTo, a.columnTo + 1) != "d5"
                && state.getBox(a.rowTo, a.columnTo + 1) != "e6"
                && state.getBox(a.rowTo, a.columnTo + 1) != "e4"
                && state.getBox(a.rowTo, a.columnTo + 1) != "e5"
            ) {
                if (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("B")
                    || citadels.contains(state.getBox(a.rowTo, a.columnTo + 2))
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo, a.columnTo + 1))
                }
            }
        }
    }

    private fun checkCaptureBlackKingDown(state: State?, a: Action) {
        // ho il re sotto
        if (state != null) {
            if (a.rowTo < state.board!!.size - 2
                && state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("K")
            ) {
                //System.out.println("Ho il re sotto");
                // re sul trono
                if (state.getBox(a.rowTo + 1, a.columnTo) == "e5") {
                    if (state.getPawn(5, 4)!!.equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B")
                        && state.getPawn(4, 3)!!.equalsPawn("B")
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame
                            .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                }
                // re adiacente al trono
                if (state.getBox(a.rowTo + 1, a.columnTo) == "e4") {
                    if (state.getPawn(3, 3)!!.equalsPawn("B") && state.getPawn(3, 5)!!.equalsPawn("B")) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame
                            .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                }
                if (state.getBox(a.rowTo + 1, a.columnTo) == "d5") {
                    if (state.getPawn(4, 2)!!.equalsPawn("B") && state.getPawn(5, 3)!!.equalsPawn("B")) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame
                            .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                }
                if (state.getBox(a.rowTo + 1, a.columnTo) == "f5") {
                    if (state.getPawn(4, 6)!!.equalsPawn("B") && state.getPawn(5, 5)!!.equalsPawn("B")) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame
                            .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                }
                // sono fuori dalle zone del trono
                if (state.getBox(a.rowTo + 1, a.columnTo) != "d5"
                    && state.getBox(a.rowTo + 1, a.columnTo) != "e4"
                    && state.getBox(a.rowTo + 1, a.columnTo) != "f5"
                    && state.getBox(a.rowTo + 1, a.columnTo) != "e5"
                ) {
                    if (state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("B")
                        || citadels.contains(state.getBox(a.rowTo + 2, a.columnTo))
                    ) {
                        state.turn=(Turn.BLACKWIN)
                        loggGame
                            .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo + 1, a.columnTo))
                    }
                }
            }
        }
    }

    private fun checkCaptureBlackKingUp(state: State?, a: Action) {
        // ho il re sopra
        if (a.rowTo > 1 && state!!.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("K")) {
            //System.out.println("Ho il re sopra");
            // re sul trono
            if (state.getBox(a.rowTo - 1, a.columnTo) == "e5") {
                if (state.getPawn(3, 4)!!.equalsPawn("B") && state.getPawn(4, 5)!!.equalsPawn("B")
                    && state.getPawn(4, 3)!!.equalsPawn("B")
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo - 1, a.columnTo))
                }
            }
            // re adiacente al trono
            if (state.getBox(a.rowTo - 1, a.columnTo) == "e6") {
                if (state.getPawn(5, 3)!!.equalsPawn("B") && state.getPawn(5, 5)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo - 1, a.columnTo))
                }
            }
            if (state.getBox(a.rowTo - 1, a.columnTo) == "d5") {
                if (state.getPawn(4, 2)!!.equalsPawn("B") && state.getPawn(3, 3)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo - 1, a.columnTo))
                }
            }
            if (state.getBox(a.rowTo - 1, a.columnTo) == "f5") {
                if (state.getPawn(4, 6)!!.equalsPawn("B") && state.getPawn(3, 5)!!.equalsPawn("B")) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo - 1, a.columnTo))
                }
            }
            // sono fuori dalle zone del trono
            if (state.getBox(a.rowTo - 1, a.columnTo) != "d5"
                && state.getBox(a.rowTo - 1, a.columnTo) != "e6"
                && state.getBox(a.rowTo - 1, a.columnTo) != "f5"
                && state.getBox(a.rowTo - 1, a.columnTo) != "e5"
            ) {
                if (state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("B")
                    || citadels.contains(state.getBox(a.rowTo - 2, a.columnTo))
                ) {
                    state.turn=(Turn.BLACKWIN)
                    loggGame
                        .fine("Nero vince con re catturato in: " + state.getBox(a.rowTo - 1, a.columnTo))
                }
            }
        }
    }

    private fun checkCaptureBlackPawnRight(state: State, a: Action) {
        // mangio a destra
        if (a.columnTo < state.board!!.size - 2
            && state.getPawn(a.rowTo, a.columnTo + 1)!!.equalsPawn("W")
        ) {
            if (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("B")) {
                state.removePawn(a.rowTo, a.columnTo + 1)
                movesWithutCapturing = -1
                loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
            }
            if (state.getPawn(a.rowTo, a.columnTo + 2)!!.equalsPawn("T")) {
                state.removePawn(a.rowTo, a.columnTo + 1)
                movesWithutCapturing = -1
                loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
            }
            if (citadels.contains(state.getBox(a.rowTo, a.columnTo + 2))) {
                state.removePawn(a.rowTo, a.columnTo + 1)
                movesWithutCapturing = -1
                loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
            }
            if (state.getBox(a.rowTo, a.columnTo + 2) == "e5") {
                state.removePawn(a.rowTo, a.columnTo + 1)
                movesWithutCapturing = -1
                loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo + 1))
            }
        }
    }

    private fun checkCaptureBlackPawnLeft(state: State, a: Action) {
        // mangio a sinistra
        if (a.columnTo > 1 && state.getPawn(a.rowTo, a.columnTo - 1)!!.equalsPawn("W")
            && (state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("B")
                    || state.getPawn(a.rowTo, a.columnTo - 2)!!.equalsPawn("T")
                    || this.citadels.contains(state.getBox(a.rowTo, a.columnTo - 2))
                    || (state.getBox(a.rowTo, a.columnTo - 2) == "e5"))) {
            state.removePawn(a.rowTo, a.columnTo - 1)
            this.movesWithutCapturing = -1
            this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo, a.columnTo - 1))
        }
    }

    private fun checkCaptureBlackPawnUp(state: State, a: Action) {
        // controllo se mangio sopra
        if (a.rowTo > 1 && state.getPawn(a.rowTo - 1, a.columnTo)!!.equalsPawn("W")
            && (state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("B")
                    || state.getPawn(a.rowTo - 2, a.columnTo)!!.equalsPawn("T")
                    || this.citadels.contains(state.getBox(a.rowTo - 2, a.columnTo))
                    || (state.getBox(a.rowTo - 2, a.columnTo) == "e5"))) {
            state.removePawn(a.rowTo - 1, a.columnTo)
            this.movesWithutCapturing = -1
            this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo - 1, a.columnTo))
        }
    }

    private fun checkCaptureBlackPawnDown(state: State, a: Action) {
        // controllo se mangio sotto
        if (a.rowTo < state.board!!.size - 2
            && state.getPawn(a.rowTo + 1, a.columnTo)!!.equalsPawn("W")
            && (state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("B")
                    || state.getPawn(a.rowTo + 2, a.columnTo)!!.equalsPawn("T")
                    || this.citadels.contains(state.getBox(a.rowTo + 2, a.columnTo))
                    || (state.getBox(a.rowTo + 2, a.columnTo) == "e5"))) {
            state.removePawn(a.rowTo + 1, a.columnTo)
            this.movesWithutCapturing = -1
            this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.rowTo + 1, a.columnTo))
        }
    }

    private fun checkCaptureBlack(state: State, a: Action): State {
        checkCaptureBlackPawnRight(state, a)
        checkCaptureBlackPawnLeft(state, a)
        checkCaptureBlackPawnUp(state, a)
        checkCaptureBlackPawnDown(state, a)
        checkCaptureBlackKingRight(state, a)
        checkCaptureBlackKingLeft(state, a)
        checkCaptureBlackKingDown(state, a)
        checkCaptureBlackKingUp(state, a)
        movesWithutCapturing++
        return state
    }

    private fun movePawn(state: State?, a: Action): State {
        val pawn = state!!.getPawn(a.rowFrom, a.columnFrom)
        val newBoard = state.board
        // State newState = new State();
        loggGame.fine("Movimento pedina")
        // libero il trono o una casella qualunque
        if (a.columnFrom == 4 && a.rowFrom == 4) {
            newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.THRONE
        } else {
            newBoard!![a.rowFrom]!![a.columnFrom] = Pawn.EMPTY
        }

        // metto nel nuovo tabellone la pedina mossa
        newBoard[a.rowTo]!![a.columnTo] = pawn
        // aggiorno il tabellone
        state.board= (newBoard)
        // cambio il turno
        if (state.turn?.equalsTurn(Turn.WHITE.toString())!!) {
            state.turn=(Turn.BLACK)
        } else {
            state.turn=(Turn.WHITE)
        }
        return state
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * Auxiliary method used to check wheter an action is allowed or not for a given state.
     *
     * @param state        Current state of the game
     * @param action    The action to be checked
     *
     * @return            true if the action is allowed, false otherwise.
     */
    private fun isPossibleMove(state: State?, action: Action): Boolean {
        loggGame.fine(action.toString())
        // controllo la mossa
        if (action.to!!.length != 2 || action.from!!.length != 2) {
            return false
        }
        val columnFrom = action.columnFrom
        val columnTo = action.columnTo
        val rowFrom = action.rowFrom
        val rowTo = action.rowTo

        // controllo se sono fuori dal tabellone
        if (state != null) {
            if (columnFrom > state.board!!.size - 1 || rowFrom > state.board!!.size - 1 || rowTo > state.board!!.size - 1 || columnTo > state.board!!.size - 1 || columnFrom < 0 || rowFrom < 0 || rowTo < 0 || columnTo < 0) {
                return false
            }
        }

        // controllo che non vada sul trono
        if (state!!.getPawn(rowTo, columnTo)!!.equalsPawn(Pawn.THRONE.toString())) {
            return false
        }

        // controllo la casella di arrivo
        if (!state.getPawn(rowTo, columnTo)!!.equalsPawn(Pawn.EMPTY.toString())) {
            return false
        }
        if (citadels.contains(state.getBox(rowTo, columnTo))
            && !citadels.contains(state.getBox(rowFrom, columnFrom))
        ) {
            return false
        }
        if (citadels.contains(state.getBox(rowTo, columnTo))
            && citadels.contains(state.getBox(rowFrom, columnFrom))
        ) {
            if (rowFrom == rowTo) {
                if (columnFrom - columnTo > 5 || columnFrom - columnTo < -5) {
                    return false
                }
            } else {
                if (rowFrom - rowTo > 5 || rowFrom - rowTo < -5) {
                    return false
                }
            }
        }

        // controllo se cerco di stare fermo
        if (rowFrom == rowTo && columnFrom == columnTo) {
            return false
        }

        // controllo se sto muovendo una pedina giusta
        if (state.turn!!.equalsTurn(Turn.WHITE.toString())) {
            if (!state.getPawn(rowFrom, columnFrom)!!.equalsPawn("W")
                && !state.getPawn(rowFrom, columnFrom)!!.equalsPawn("K")
            ) {
                return false
            }
        }
        if (state.turn?.equalsTurn(Turn.BLACK.toString())!!) {
            if (!state.getPawn(rowFrom, columnFrom)!!.equalsPawn("B")) {
                return false
            }
        }

        // controllo di non muovere in diagonale
        if (rowFrom != rowTo && columnFrom != columnTo) {
            return false
        }

        // controllo di non scavalcare pedine
        if (rowFrom == rowTo) {
            if (columnFrom > columnTo) {
                for (i in columnTo until columnFrom) {
                    if (!state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.EMPTY.toString())) {
                        return if (state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.THRONE.toString())) {
                            false
                        } else {
                            false
                        }
                    }
                    if (citadels.contains(state.getBox(rowFrom, i))
                        && !citadels.contains(state.getBox(action.rowFrom, action.columnFrom))
                    ) {
                        return false
                    }
                }
            } else {
                for (i in columnFrom + 1..columnTo) {
                    if (!state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.EMPTY.toString())) {
                        return if (state.getPawn(rowFrom, i)!!.equalsPawn(Pawn.THRONE.toString())) {
                            false
                        } else {
                            false
                        }
                    }
                    if (citadels.contains(state.getBox(rowFrom, i))
                        && !citadels.contains(state.getBox(action.rowFrom, action.columnFrom))
                    ) {
                        return false
                    }
                }
            }
        } else {
            if (rowFrom > rowTo) {
                for (i in rowTo until rowFrom) {
                    if (!state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.EMPTY.toString())) {
                        return if (state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.THRONE.toString())) {
                            false
                        } else {
                            false
                        }
                    }
                    if (citadels.contains(state.getBox(i, columnFrom))
                        && !citadels.contains(state.getBox(action.rowFrom, action.columnFrom))
                    ) {
                        return false
                    }
                }
            } else {
                for (i in rowFrom + 1..rowTo) {
                    if (!state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.EMPTY.toString())) {
                        return if (state.getPawn(i, columnFrom)!!.equalsPawn(Pawn.THRONE.toString())) {
                            false
                        } else {
                            false
                        }
                    }
                    if (citadels.contains(state.getBox(i, columnFrom))
                        && !citadels.contains(state.getBox(action.rowFrom, action.columnFrom))
                    ) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /////////////////////////////////////////////////////////////////////////////////////
    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    override fun endGame(state: State?) {
        loggGame.fine(
            """
    Stato:
    ${state.toString()}
    """.trimIndent()
        )
    }

    /**
     * Get the player who must make the next move
     *
     * @param state Current state of the game
     *
     * @return The turn of the game (W = WHITE, B = BLACK)
     */
    override fun getPlayer(state: State?): Turn? {
        return state?.turn
    }

    /**
     * Method that compute a list of all possible actions for the current player according to the rules of the game
     *
     * @param state Current state of the game
     *
     * @return List of all the Action allowed from current state for each pawn of the player
     */
    override fun getActions(state: State?): List<Action> {
        val turn = state?.turn
        val possibleActions: MutableList<Action> = ArrayList()

        // Loop through rows
        if (state != null) {
            for (i in state.board!!.indices) {
                // Loop through columns
                for (j in state.board!!.indices) {
                    val p = state.getPawn(i, j)

                    // If pawn color  is equal of turn color
                    if (p.toString() == turn.toString() || p == Pawn.KING && turn == Turn.WHITE) {
                        // Search on top of pawn
                        for (k in i - 1 downTo 0) {
                            // Break if pawn is out of citadels, and it is moving on a citadel
                            if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(k, j))) {
                                break
                            } else if (state.getPawn(k, j)!!.equalsPawn(Pawn.EMPTY.toString())) {
                                val from = state.getBox(i, j)
                                val to = state.getBox(k, j)
                                var action: Action? = null
                                action = Action(from, to, turn)

                                // Check if action is admissible and if it is, add it to list possibleActions
                                try {
                                    if (isPossibleMove(state.clone(), Objects.requireNonNull(action))) possibleActions.add(
                                        action
                                    )
                                } catch (e: CloneNotSupportedException) {
                                    throw RuntimeException(e)
                                }
                            } else break // There is a pawn in the same column, and it cannot be crossed
                        }

                        // Search on bottom of pawn
                        for (k in i + 1 until state.board!!.size) {
                            // Break if pawn is out of citadels, and it is moving on a citadel
                            if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(k, j))) {
                                break
                            } else if (state.getPawn(k, j)!!.equalsPawn(Pawn.EMPTY.toString())) {
                                val from = state.getBox(i, j)
                                val to = state.getBox(k, j)
                                var action: Action? = null
                                action = Action(from, to, turn)

                                // Check if action is admissible and if it is, add it to list possibleActions
                                try {
                                    if (isPossibleMove(state.clone(), Objects.requireNonNull(action))) possibleActions.add(
                                        action
                                    )
                                } catch (e: CloneNotSupportedException) {
                                    throw RuntimeException(e)
                                }
                            } else break // There is a pawn in the same column, and it cannot be crossed
                        }

                        // Search on left of pawn
                        for (k in j - 1 downTo 0) {
                            // Break if pawn is out of citadels, and it is moving on a citadel
                            if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(i, k))) {
                                break
                            } else if (state.getPawn(i, k)!!.equalsPawn(Pawn.EMPTY.toString())) {
                                val from = state.getBox(i, j)
                                val to = state.getBox(i, k)
                                var action: Action? = null
                                action = Action(from, to, turn)

                                // Check if action is admissible and if it is, add it to list possibleActions
                                try {
                                    if (isPossibleMove(state.clone(), Objects.requireNonNull(action))) possibleActions.add(
                                        action
                                    )
                                } catch (e: CloneNotSupportedException) {
                                    throw RuntimeException(e)
                                }
                            } else break // There is a pawn in the same row, and it cannot be crossed
                        }

                        // Search on right of pawn
                        for (k in j + 1 until state.board!!.size) {
                            // Break if pawn is out of citadels, and it is moving on a citadel
                            if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(i, k))) {
                                break
                            } else if (state.getPawn(i, k)!!.equalsPawn(Pawn.EMPTY.toString())) {
                                val from = state.getBox(i, j)
                                val to = state.getBox(i, k)
                                var action: Action? = null
                                action = Action(from, to, turn)

                                // Check if action is admissible and if it is, add it to list possibleActions
                                try {
                                    if (isPossibleMove(state.clone(), Objects.requireNonNull(action))) possibleActions.add(
                                        action
                                    )
                                } catch (e: CloneNotSupportedException) {
                                    throw RuntimeException(e)
                                }
                            } else break // There is a pawn in the same row, and it cannot be crossed
                        }
                    }
                }
            }
        }
        return possibleActions
    }

    /**
     * Method that performs an action in a given state and returns the resulting state
     *
     * @param state Current state
     * @param action Action admissible on the given state
     *
     * @return State obtained after performing the action
     */
    override fun getResult(state: State?, action: Action): State {
        // Move pawn
        var state = state
        state = try {
            movePawn(state!!.clone(), action)
        } catch (e: CloneNotSupportedException) {
            throw RuntimeException(e)
        }

        // Check the state for any capture
        if (state?.turn?.equalsTurn("W")!!) {
            checkCaptureBlack(state, action)
        } else if (state.turn!!.equalsTurn("B")) {
            checkCaptureWhite(state, action)
        }
        return state
    }

    /**
     * Check if a state is terminal, since a player has either won or drawn (i.e. the game ends)
     *
     * @param state Current state of the game
     *
     * @return true if the current state is terminal, otherwise false
     */
    override fun isTerminal(state: State?): Boolean {
        return state?.turn == Turn.WHITEWIN || state?.turn == Turn.BLACKWIN || state?.turn == Turn.DRAW
    }

    /**
     * Method to evaluate a state using heuristics
     *
     * @param state    Current state
     * @param turn    Player that want to find the best moves in the search space
     *
     * @return Evaluation of the state
     */
    override fun getUtility(state: State?, turn: Turn?): Double {
        // Terminal state
        if (turn != null) {
            if ((turn == Turn.BLACK && state?.turn == Turn.BLACKWIN) || (turn == Turn.WHITE && state?.turn == Turn.WHITEWIN)) return Double.POSITIVE_INFINITY // Win
            else if (turn == Turn.BLACK && state?.turn == Turn.WHITEWIN || turn == Turn.WHITE && state?.turn == Turn.BLACKWIN) return Double.NEGATIVE_INFINITY
        } // Lose

        // Non-terminal state => get Heuristics for the current state
        val heuristics = if (turn == Turn.WHITE) WhiteHeuristics(state) else BlackHeuristics(state)
        return heuristics.evaluateState()
    }

    /* Not used in AlphaBetaSearch */
    override fun getInitialState(): State? {
        return null
    }

    /* Not used in AlphaBetaSearch */
    override fun getPlayers(): Array<Turn> {
        return Turn.values()
    }

    companion object {
        /**
         * Initial number of pawns for each player
         */
        const val NUM_BLACK = 16
        const val NUM_WHITE = 8
    }
}