package it.unibo.ai.didattica.competition.tablut.client

import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 *
 * @author A. Piretti, Andrea Galassi
 */
class TablutHumanClient(player: String) : TablutClient(player, "humanInterface") {
    override fun run() {
        println("You are player " + player.toString() + "!")
        var actionStringFrom: String?
        var actionStringTo: String?
        var action: Action
        val `in` = BufferedReader(InputStreamReader(System.`in`))
        try {
            declareName()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (player == Turn.WHITE) {
            println("You are player " + player.toString() + "!")
            while (true) {
                try {
                    read()
                    println("Current state:")
                    println(currentState.toString())
                    if (currentState?.turn == Turn.WHITE) {
                        println("Player " + player.toString() + ", do your move: ")
                        println("From: ")
                        actionStringFrom = `in`.readLine()
                        println("To: ")
                        actionStringTo = `in`.readLine()
                        action = Action(actionStringFrom, actionStringTo, player)
                        write(action)
                    } else if (currentState?.turn == Turn.BLACK) {
                        println("Waiting for your opponent move... ")
                    } else if (currentState?.turn == Turn.WHITEWIN) {
                        println("YOU WIN!")
                        System.exit(0)
                    } else if (currentState?.turn == Turn.BLACKWIN) {
                        println("YOU LOSE!")
                        System.exit(0)
                    } else if (currentState?.turn == Turn.DRAW) {
                        println("DRAW!")
                        System.exit(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    System.exit(1)
                }
            }
        } else {
            println("You are player " + player.toString() + "!")
            while (true) {
                try {
                    read()
                    println("Current state:")
                    println(currentState.toString())
                    if (currentState?.turn == Turn.BLACK) {
                        println("Player " + player.toString() + ", do your move: ")
                        println("From: ")
                        actionStringFrom = `in`.readLine()
                        println("To: ")
                        actionStringTo = `in`.readLine()
                        action = Action(actionStringFrom, actionStringTo, player)
                        write(action)
                    } else if (currentState?.turn == Turn.WHITE) {
                        println("Waiting for your opponent move... ")
                    } else if (currentState?.turn == Turn.WHITEWIN) {
                        println("YOU LOSE!")
                        System.exit(0)
                    } else if (currentState?.turn == Turn.BLACKWIN) {
                        println("YOU WIN!")
                        System.exit(0)
                    } else if (currentState?.turn == Turn.DRAW) {
                        println("DRAW!")
                        System.exit(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    System.exit(1)
                }
            }
        }
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 0) {
                println("You must specify which player you are (WHITE or BLACK)!")
                System.exit(-1)
            }
            println("Selected this: " + args[0])
            val client: TablutClient = TablutHumanClient(args[0])
            client.run()
        }
    }
}