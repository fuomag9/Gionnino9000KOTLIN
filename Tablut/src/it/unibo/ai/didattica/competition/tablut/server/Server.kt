package it.unibo.ai.didattica.competition.tablut.server

import com.google.gson.Gson
import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.gui.Gui
import it.unibo.ai.didattica.competition.tablut.util.Configuration
import it.unibo.ai.didattica.competition.tablut.util.StreamUtils
import org.apache.commons.cli.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Paths
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.system.exitProcess

/**
 * this class represent the server of the match: 2 clients with TCP connection
 * can connect and start to play
 *
 * @author A.Piretti, Andrea Galassi
 */
class Server(
    /**
     * Number of seconds allowed for a decision
     */
    private val time: Int,
    /**
     * Number of states kept in memory for the detection of a draw
     */
    private var moveCache: Int,
    /**
     * Errors allowed
     */
    private val errors: Int,
    /**
     * Integer that represents the game type
     */
    private var gameC: Int,
    /**
     * Whether the gui must be enabled or not
     */
    private val enableGui: Boolean
) : Runnable {
    /**
     * State of the game
     */
    private var state: State? = null

    /**
     * JSON string used to communicate
     */
    private var theGson: String? = null

    /**
     * Repeated positions allowed
     */
    private val repeated = 0

    private var game: Game? = null

    /**
     * Counter for the errors of the black player
     */
    private var blackErrors = 0

    /**
     * Counter for the errors of the white player
     */
    private var whiteErrors = 0
    private val gson: Gson = Gson()
    private var theGui: Gui? = null

    private fun initializeGUI(state: State?) {
        theGui = Gui(gameC)
        theGui!!.update(state)
    }

    /**
     * This class represents the stream who is waiting for the move from the
     * client (JSON format)
     *
     * @author A.Piretti
     */
    private inner class TCPInput(private val theStream: DataInputStream) : Runnable {
        override fun run() {
            theGson = try {
                StreamUtils.readString(theStream)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * This class represents the socket waiting for a connection
     * @author Andrea Galassi
     */
    private class TCPConnection(private val serversocket: ServerSocket) : Runnable {
        var socket: Socket? = null
            private set

        override fun run() {
            socket = try {
                serversocket.accept()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * This method starts the proper game. It waits the connections from 2
     * clients, check the move and update the state. There is a timeout that
     * interrupts games that last too much
     */
    override fun run() {
        val hourlimit = 10
        var endgame = false
        val logs_folder = "logs"
        var p = Paths.get(logs_folder + File.separator + Date().time + "_systemLog.txt")
        p = p.toAbsolutePath()
        val sysLogName = p.toString()
        val loggSys = Logger.getLogger("SysLog")
        try {
            File(logs_folder).mkdirs()
            println(sysLogName)
            val systemLog = File(sysLogName)
            if (!systemLog.exists()) {
                systemLog.createNewFile()
            }
            val fh: FileHandler = FileHandler(sysLogName, true)
            loggSys.addHandler(fh)
            fh.formatter = SimpleFormatter()
            loggSys.level = Level.FINE
            loggSys.fine("Accensione server")
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
        when (gameC) {
            1 -> loggSys.fine("Partita di ClassicTablut")
            2 -> loggSys.fine("Partita di ModernTablut")
            3 -> loggSys.fine("Partita di Brandub")
            4 -> loggSys.fine("Partita di Tablut")
            else -> {
                println("Error in game selection")
                exitProcess(4)
            }
        }
        val starttime = Date()
        var t: Thread
        val whiteMove: DataInputStream
        val blackMove: DataInputStream
        var whiteState: DataOutputStream? = null
        var blackState: DataOutputStream? = null
        println("Waiting for connections...")
        var whiteName = "WP"
        var blackName = "BP"
        var tin: TCPInput?
        var Turnwhite: TCPInput? = null
        var Turnblack: TCPInput? = null
        var tc: TCPConnection

        // ESTABLISH CONNECTIONS AND NAME READING
        try {
            val socketWhite = ServerSocket(Configuration.whitePort)
            val socketBlack = ServerSocket(Configuration.blackPort)


            // ESTABLISHING CONNECTION
            tc = TCPConnection(socketWhite)
            t = Thread(tc)
            t.start()
            loggSys.fine("Waiting for white connection..")
            // timeout for connection
            try {
                var counter = 0
                while (counter < connectionTimeout && t.isAlive) {
                    Thread.sleep(1000)
                    counter++
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (t.isAlive) {
                println("Timeout!!!!")
                loggSys.warning("Closing system for timeout!")
                exitProcess(0)
            }
            val white: Socket? = tc.socket
            loggSys.fine("White player connected")
            whiteMove = DataInputStream(white!!.getInputStream())
            whiteState = DataOutputStream(white.getOutputStream())
            Turnwhite = TCPInput(whiteMove)

            // NAME READING
            t = Thread(Turnwhite)
            t.start()
            loggSys.fine("Lettura nome player bianco in corso..")
            // timeout for name declaration
            try {
                var counter = 0
                while (counter < time && t.isAlive) {
                    Thread.sleep(1000)
                    counter++
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (t.isAlive) {
                println("Timeout!!!!")
                loggSys.warning("Chiusura sistema per timeout")
                exitProcess(0)
            }
            whiteName = gson.fromJson(theGson, String::class.java)
            // SECURITY STEP: dropping unproper characters
            var temp = StringBuilder()
            run {
                var i = 0
                while (i < whiteName.length && i < 10) {
                    val c = whiteName[i]
                    if (Character.isAlphabetic(c.code) || Character.isDigit(c)) temp.append(c)
                    i++
                }
            }
            whiteName = temp.toString()
            println("White player name:\t$whiteName")
            loggSys.fine("White player name:\t$whiteName")


            // ESTABLISHING CONNECTION
            tc = TCPConnection(socketBlack)
            t = Thread(tc)
            t.start()
            loggSys.fine("Waiting for Black connection..")
            // timeout for connection
            try {
                var counter = 0
                while (counter < connectionTimeout && t.isAlive) {
                    Thread.sleep(1000)
                    counter++
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (t.isAlive) {
                println("Timeout!!!!")
                loggSys.warning("Closing system for timeout!")
                exitProcess(0)
            }
            val black: Socket? = tc.socket
            loggSys.fine("Accettata connessione con client giocatore Nero")
            blackMove = DataInputStream(black!!.getInputStream())
            blackState = DataOutputStream(black.getOutputStream())
            Turnblack = TCPInput(blackMove)

            // NAME READING
            t = Thread(Turnblack)
            t.start()
            loggSys.fine("Lettura nome player nero in corso..")
            try {
                // timer for the move
                var counter = 0
                while (counter < time && t.isAlive) {
                    Thread.sleep(1000)
                    counter++
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            // timeout for name declaration
            if (t.isAlive) {
                println("Timeout!!!!")
                loggSys.warning("Chiusura sistema per timeout")
                exitProcess(0)
            }
            blackName = gson.fromJson(theGson, String::class.java)
            // SECURITY STEP: dropping unproper characters
            temp = StringBuilder()
            var i = 0
            while (i < blackName.length && i < 10) {
                val c = blackName[i]
                if (Character.isAlphabetic(c.code) || Character.isDigit(c)) temp.append(c)
                i++
            }
            println("Black player name:\t$blackName")
            loggSys.fine("Black player name:\t$blackName")
            blackName = temp.toString()
        } catch (e: IOException) {
            println("Socket error....")
            loggSys.warning("Errore connessioni")
            loggSys.warning("Chiusura sistema")
            exitProcess(1)
        }
        when (gameC) {
            1 -> {
                state = StateTablut()
                this.game = GameTablut(moveCache)
            }
            2 -> {
                state = StateTablut()
                this.game = GameModernTablut(moveCache)
            }
            3 -> {
                state = StateBrandub()
                this.game = GameTablut(moveCache)
            }
            4 -> {
                state = StateTablut()
                (state as StateTablut).turn = Turn.WHITE
                this.game = GameAshtonTablut(state as StateTablut, repeated, moveCache, "logs", whiteName, blackName)
            }
            else -> {
                println("Error in game selection")
                exitProcess(4)
            }
        }
        if (enableGui) {
            initializeGUI(state)
        }
        println("Clients connected..")

        // SEND INITIAL STATE
        tin = Turnwhite
        try {
            theGson = gson.toJson(state)
            StreamUtils.writeString(whiteState, theGson)
            StreamUtils.writeString(blackState, theGson)
            loggSys.fine("Invio messaggio ai giocatori")
            if (enableGui) {
                theGui!!.update(state)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            loggSys.fine("Errore invio messaggio ai giocatori")
            loggSys.warning("Chiusura sistema")
            exitProcess(1)
        }

        // GAME CYCLE
        while (!endgame) {
            // RECEIVE MOVE

            // System.out.println("State: \n"+state.toString());
            println("Waiting for " + state!!.turn + "...")

            // create the process that listen the answer
            t = Thread(tin)
            t.start()
            loggSys.fine("Lettura mossa player " + state!!.turn + " in corso..")
            try {
                // timer for the move
                var counter = 0
                while (counter < time && t.isAlive) {
                    Thread.sleep(1000)
                    counter++
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            // loss for timeout
            if (t.isAlive) {
                println("Timeout!!!!")
                println("Player " + state!!.turn.toString() + " has lost!")
                loggSys.warning("Timeout! Player " + state!!.turn + " lose!")
                loggSys.warning("Chiusura sistema per timeout")
                exitProcess(0)
            }

            // APPLY MOVE
            // translate the string into an action object
            /**
             * Action chosen by a player
             */
            val move = gson.fromJson(theGson, Action::class.java)
            loggSys.fine("Move received.\t$move")
            move.turn = state?.turn
            println("Suggested move: $move")
            try {
                // aggiorna tutto e determina anche eventuali fine partita
                state = this.game?.checkMove(state, move)
            } catch (e: Exception) {
                // exception means error, therefore increase the error counters
                if (state!!.turn?.equalsTurn("B")!!) {
                    blackErrors++
                    if (blackErrors > errors) {
                        println("TOO MANY ERRORS FOR BLACK PLAYER; PLAYER WHITE WIN!")
                        e.printStackTrace()
                        loggSys.warning("Chiusura sistema per troppi errori giocatore nero")
                        state!!.turn = (Turn.WHITEWIN)
                        this.game?.endGame(state)
                    } else {
                        println("Error for black player...")
                    }
                }
                if (state!!.turn!!.equalsTurn("W")) {
                    whiteErrors++
                    if (whiteErrors > errors) {
                        println("TOO MANY ERRORS FOR WHITE PLAYER; PLAYER BLACK WIN!")
                        e.printStackTrace()
                        loggSys.warning("Chiusura sistema per troppi errori giocatore bianco")
                        state?.turn = (Turn.BLACKWIN)
                        this.game?.endGame(state)
                    } else {
                        println("Error for white player...")
                    }
                }
            }

            // TODO: in case of more errors allowed, it is fair to send the same
            // state once again?
            // In case not, the client should always read and act when is their
            // turn

            // GAME TOO LONG, TIMEOUT
            val ti = Date()
            val hoursoccurred = (ti.time - starttime.time) / 60 / 60 / 1000
            if (hoursoccurred > hourlimit) {
                println("TIMEOUT! END OF THE GAME...")
                loggSys.warning("Chiusura programma per timeout di $hourlimit ore")
                state!!.turn = (Turn.DRAW)
            }

            // SEND STATE TO PLAYERS
            try {
                theGson = gson.toJson(state)
                StreamUtils.writeString(whiteState, theGson)
                StreamUtils.writeString(blackState, theGson)
                loggSys.fine("Invio messaggio ai client")
                if (enableGui) {
                    theGui!!.update(state)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                loggSys.warning("Errore invio messaggio ai client")
                loggSys.warning("Chiusura sistema")
                exitProcess(1)
            }
            when (state?.turn) {
                Turn.WHITE -> tin = Turnwhite
                Turn.BLACK -> tin = Turnblack
                Turn.BLACKWIN -> {
                    this.game?.endGame(state)
                    println("END OF THE GAME")
                    println("RESULT: PLAYER BLACK WIN")
                    endgame = true
                }
                Turn.WHITEWIN -> {
                    this.game?.endGame(state)
                    println("END OF THE GAME")
                    println("RESULT: PLAYER WHITE WIN")
                    endgame = true
                }
                Turn.DRAW -> {
                    this.game?.endGame(state)
                    println("END OF THE GAME")
                    println("RESULT: DRAW")
                    endgame = true
                }
                else -> {
                    loggSys.warning("Chiusura sistema")
                    exitProcess(4)
                }
            }
        }
        exitProcess(0)
    }

    companion object {
        /**
         * Timeout for waiting for a client to connect
         */
        const val connectionTimeout = 300

        /**
         * Server initialiazer.
         *
         * @param args
         * the time for the move, the size of the cache for monitoring
         * draws, the number of errors allowed, the type of game, whether
         * the GUI should be used or not
         */
        @JvmStatic
        fun main(args: Array<String>) {
            var time = 60
            var moveCache = -1
            var repeated = 0
            var errors = 0
            var gameChosen = 4
            var enableGui = true
            val parser: CommandLineParser = DefaultParser()
            val options = Options()
            options.addOption("t", "time", true, "time must be an integer (number of seconds); default: 60")
            options.addOption(
                "c",
                "cache",
                true,
                "cache must be an integer, negative value means infinite; default: infinite"
            )
            options.addOption("e", "errors", true, "errors must be an integer >= 0; default: 0")
            options.addOption("s", "repeatedState", true, "repeatedStates must be an integer >= 0; default: 0")
            options.addOption(
                "r",
                "game rules",
                true,
                "game rules must be an integer; 1 for Tablut, 2 for Modern, 3 for Brandub, 4 for Ashton; default: 4"
            )
            options.addOption("g", "enableGUI", false, "enableGUI if option is present")
            val formatter = HelpFormatter()
            formatter.printHelp("java Server", options)
            try {
                val cmd = parser.parse(options, args)
                if (cmd.hasOption("t")) {
                    val timeInsert = cmd.getOptionValue("t")
                    try {
                        time = timeInsert.toInt()
                        if (time < 1) {
                            println("Time format not allowed!")
                            formatter.printHelp("java Server", options)
                            exitProcess(1)
                        }
                    } catch (e: NumberFormatException) {
                        println("The time format is not correct!")
                        formatter.printHelp("java Server", options)
                        exitProcess(1)
                    }
                }
                if (cmd.hasOption("c")) {
                    val moveCacheInsert = cmd.getOptionValue("c")
                    try {
                        moveCache = moveCacheInsert.toInt()
                    } catch (e: NumberFormatException) {
                        println("Number format is not correct!")
                        formatter.printHelp("java Server", options)
                        exitProcess(1)
                    }
                }
                if (cmd.hasOption("e")) {
                    try {
                        errors = cmd.getOptionValue("e").toInt()
                    } catch (e: NumberFormatException) {
                        println("The error format is not correct!")
                        formatter.printHelp("java Server", options)
                        exitProcess(1)
                    }
                }
                if (cmd.hasOption("s")) {
                    try {
                        repeated = cmd.getOptionValue("s").toInt()
                        if (repeated < 0) {
                            println("he RepeatedStates value is not allowed!")
                            formatter.printHelp("java Server", options)
                            exitProcess(1)
                        }
                    } catch (e: NumberFormatException) {
                        println("The RepeatedStates format is not correct!")
                        formatter.printHelp("java Server", options)
                        exitProcess(1)
                    }
                }
                if (cmd.hasOption("r")) {
                    try {
                        gameChosen = cmd.getOptionValue("r").toInt()
                        if (gameChosen < 0 || gameChosen > 4) {
                            println("Game format not allowed!")
                            formatter.printHelp("java Server", options)
                            exitProcess(1)
                        }
                    } catch (e: NumberFormatException) {
                        println("The game format is not correct!")
                        formatter.printHelp("java Server", options)
                        exitProcess(1)
                    }
                }
                enableGui = cmd.hasOption("g")
            } catch (exp: ParseException) {
                println("Unexpected exception:" + exp.message)
            }

            // Start the server
            val engine = Server(time, moveCache, errors, gameChosen, enableGui)
            engine.run()
        }
    }
}