package it.unibo.ai.didattica.competition.tablut.tester

import kotlin.system.exitProcess

/**
 * Software to test a particular configuration.
 * Insert the cell coordinates as "A1" "B4" and so on. Provide moves commands from a cell A1 to another B1 as "A1 B1"
 * @author Andrea Piretti
 */
class Tester(game: Int) {
    private var theTestGui: TestGui? = null

    init {
        theTestGui = TestGui(game)
    }

    fun run() {
        while (true) {
        }
    }

    companion object {
        private var gameChosen = 0
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                try {
                    gameChosen = args[0].toInt()
                    if (gameChosen < 0 || gameChosen > 4) {
                        println("Error format not allowed!")
                        exitProcess(1)
                    }
                } catch (e: Exception) {
                    println("The error format is not correct!")
                    exitProcess(1)
                }
            } else {
                println("Usage: java Tester <game>")
                exitProcess(1)
            }

            //LANCIO IL MOTORE PER UN SERVER
            val tester = Tester(gameChosen)
            tester.run()
        }
    }
}