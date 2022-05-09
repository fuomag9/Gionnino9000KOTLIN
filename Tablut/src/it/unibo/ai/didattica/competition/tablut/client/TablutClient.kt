package it.unibo.ai.didattica.competition.tablut.client

import com.google.gson.Gson
import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.util.Configuration
import it.unibo.ai.didattica.competition.tablut.util.StreamUtils
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.security.InvalidParameterException

/**
 * Classe astratta di un client per il gioco Tablut
 *
 * @author Andrea Piretti
 */
abstract class TablutClient @JvmOverloads constructor(
    player: String,
    name: String,
    timeout: Int = 60,
    ipAddress: String? = "localhost"
) : Runnable {
    var player: Turn? = null
    var name: String
    private val `in`: DataInputStream
    private val out: DataOutputStream
    private val gson: Gson
    var currentState: State? = null
    val timeout: Int
    /**
     * Creates a new player initializing the sockets and the logger
     *
     * @param player
     * The role of the player (black or white)
     * @param name
     * The name of the player
     * @param timeout
     * The timeout that will be taken into account (in seconds)
     * @param ipAddress
     * The ipAddress of the server
     */
    /**
     * Creates a new player initializing the sockets and the logger. Timeout is
     * set to be 60 seconds. The server is supposed to be communicating on the
     * same machine of this player.
     *
     * @param player
     * The role of the player (black or white)
     * @param name
     * The name of the player
     */
    /**
     * Creates a new player initializing the sockets and the logger. The server
     * is supposed to be communicating on the same machine of this player.
     *
     * @param player
     * The role of the player (black or white)
     * @param name
     * The name of the player
     * @param timeout
     * The timeout that will be taken into account (in seconds)
     */
    init {
        val port: Int
        this.timeout = timeout
        gson = Gson()
        if (player.equals("white", ignoreCase = true)) {
            this.player = Turn.WHITE
            port = Configuration.whitePort
        } else if (player.equals("black", ignoreCase = true)) {
            this.player = Turn.BLACK
            port = Configuration.blackPort
        } else {
            throw InvalidParameterException("Player role must be BLACK or WHITE")
        }
        val playerSocket = Socket(ipAddress, port)
        out = DataOutputStream(playerSocket.getOutputStream())
        `in` = DataInputStream(playerSocket.getInputStream())
        this.name = name
    }

    /**
     * Write an action to the server
     */
    @Throws(IOException::class)
    fun write(action: Action?) {
        StreamUtils.writeString(out, gson.toJson(action))
    }

    /**
     * Write the name to the server
     */
    @Throws(IOException::class)
    fun declareName() {
        StreamUtils.writeString(out, gson.toJson(name))
    }

    /**
     * Read the state from the server
     */
    @Throws(IOException::class)
    fun read() {
        currentState = gson.fromJson(StreamUtils.readString(`in`), StateTablut::class.java)
    }
}