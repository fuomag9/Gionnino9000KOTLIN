package it.unibo.ai.didattica.competition.tablut.client

import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import java.io.IOException
import java.util.*

/**
 *
 * @author A. Piretti, Andrea Galassi
 */
class TablutRandomClient @JvmOverloads constructor(
    player: String,
    name: String = "random",
    private val game: Int = 4,
    timeout: Int = 60,
    ipAddress: String? = "localhost"
) : TablutClient(player, name, timeout, ipAddress) {

    override fun run() {
        try {
            declareName()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var state: State?
        var rules: Game? = null
        when (game) {
            1 -> {
                state = StateTablut()
                rules = GameTablut()
            }
            2 -> {
                state = StateTablut()
                rules = GameModernTablut()
            }
            3 -> {
                state = StateBrandub()
                rules = GameTablut()
            }
            4 -> {
                state = StateTablut()
                state.turn=Turn.WHITE
                rules = GameAshtonTablut(99, 0, "garbage", "fake", "fake")
                println("Ashton Tablut game")
            }
            else -> {
                println("Error in game selection")
                System.exit(4)
            }
        }
        val pawns: MutableList<IntArray> = ArrayList()
        val empty: MutableList<IntArray> = ArrayList()
        println("You are player " + player.toString() + "!")
        while (true) {
            try {
                read()
            } catch (e1: IOException) {
                // TODO Auto-generated catch block
                e1.printStackTrace()
                System.exit(1)
            }
            println("Current state:")
            state = currentState
            println(state.toString())
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            if (player == Turn.WHITE) {
                // Mio turno
                if (currentState?.turn == Turn.WHITE) {
                    var buf: IntArray
                    if (state != null) {
                        for (i in state.board?.indices!!) {
                            for (j in state.board!!.indices) {
                                if (state.getPawn(i, j)!!.equalsPawn(Pawn.WHITE.toString())
                                    || state.getPawn(i, j)!!.equalsPawn(Pawn.KING.toString())
                                ) {
                                    buf = IntArray(2)
                                    buf[0] = i
                                    buf[1] = j
                                    pawns.add(buf)
                                } else if (state.getPawn(i, j)!!.equalsPawn(Pawn.EMPTY.toString())) {
                                    buf = IntArray(2)
                                    buf[0] = i
                                    buf[1] = j
                                    empty.add(buf)
                                }
                            }
                        }
                    }
                    var selected: IntArray
                    var found = false
                    var a: Action? = null
                    a = Action("z0", "z0", Turn.WHITE)
                    while (!found) {
                        selected = if (pawns.size > 1) {
                            pawns[Random().nextInt(pawns.size - 1)]
                        } else {
                            pawns[0]
                        }
                        val from = currentState!!.getBox(selected[0], selected[1])
                        selected = empty[Random().nextInt(empty.size - 1)]
                        val to = currentState!!.getBox(selected[0], selected[1])
                        a = Action(from, to, Turn.WHITE)
                        found = try {
                            rules!!.checkMove(state, a)
                            true
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    }
                    println("Mossa scelta: " + Objects.requireNonNull(a))
                    try {
                        write(a)
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    pawns.clear()
                    empty.clear()
                } else if (state?.turn == Turn.BLACK) {
                    println("Waiting for your opponent move... ")
                } else if (state?.turn == Turn.WHITEWIN) {
                    println("YOU WIN!")
                    System.exit(0)
                } else if (state?.turn == Turn.BLACKWIN) {
                    println("YOU LOSE!")
                    System.exit(0)
                } else if (state?.turn == Turn.DRAW) {
                    println("DRAW!")
                    System.exit(0)
                }
            } else {

                // Mio turno
                if (currentState?.turn == Turn.BLACK) {
                    var buf: IntArray
                    if (state != null) {
                        for (i in state.board?.indices!!) {
                            for (j in state.board!!.indices) {
                                if (state.getPawn(i, j)!!.equalsPawn(Pawn.BLACK.toString())) {
                                    buf = IntArray(2)
                                    buf[0] = i
                                    buf[1] = j
                                    pawns.add(buf)
                                } else if (state.getPawn(i, j)!!.equalsPawn(Pawn.EMPTY.toString())) {
                                    buf = IntArray(2)
                                    buf[0] = i
                                    buf[1] = j
                                    empty.add(buf)
                                }
                            }
                        }
                    }
                    var selected: IntArray
                    var found = false
                    var a: Action? = null
                    a = Action("z0", "z0", Turn.BLACK)
                    while (!found) {
                        selected = pawns[Random().nextInt(pawns.size - 1)]
                        val from = currentState!!.getBox(selected[0], selected[1])
                        selected = empty[Random().nextInt(empty.size - 1)]
                        val to = currentState!!.getBox(selected[0], selected[1])
                        a = Action(from, to, Turn.BLACK)
                        println("try: " + Objects.requireNonNull(a))
                        found = try {
                            rules!!.checkMove(state, a)
                            true
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    }
                    println("Mossa scelta: $a")
                    try {
                        write(a)
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    pawns.clear()
                    empty.clear()
                } else if (state?.turn == Turn.WHITE) {
                    println("Waiting for your opponent move... ")
                } else if (state?.turn == Turn.WHITEWIN) {
                    println("YOU LOSE!")
                    System.exit(0)
                } else if (state?.turn == Turn.BLACKWIN) {
                    println("YOU WIN!")
                    System.exit(0)
                } else if (state?.turn == Turn.DRAW) {
                    println("DRAW!")
                    System.exit(0)
                }
            }
        }
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val gametype = 4
            var role = ""
            val name = "random"
            var ipAddress = "localhost"
            var timeout = 60
            // TODO: change the behavior?
            if (args.size < 1) {
                println("You must specify which player you are (WHITE or BLACK)")
                System.exit(-1)
            } else {
                println(args[0])
                role = args[0]
            }
            if (args.size == 2) {
                println(args[1])
                timeout = args[1].toInt()
            }
            if (args.size == 3) {
                ipAddress = args[2]
            }
            println("Selected client: " + args[0])
            val client = TablutRandomClient(role, name, gametype, timeout, ipAddress)
            client.run()
        }
    }
}