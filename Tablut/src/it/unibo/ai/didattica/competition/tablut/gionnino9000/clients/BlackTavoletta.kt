package it.unibo.ai.didattica.competition.tablut.gionnino9000.clients

import java.io.IOException

object BlackTavoletta {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var arguments = arrayOf("BLACK", "60", "localhost", "debug")
        if (args.size > 0) {
            arguments = arrayOf("BLACK", args[0])
        }
        Tavoletta.main(arguments)
    }
}