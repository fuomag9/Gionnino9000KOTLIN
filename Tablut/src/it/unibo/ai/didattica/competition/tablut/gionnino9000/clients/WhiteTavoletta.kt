package it.unibo.ai.didattica.competition.tablut.gionnino9000.clients

import java.io.IOException

object WhiteTavoletta {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var arguments = arrayOf("WHITE", "60", "localhost", "debug")
        if (args.size > 0) {
            arguments = arrayOf("WHITE", args[0])
        }
        Tavoletta.main(arguments)
    }
}