package it.unibo.ai.didattica.competition.tablut.client

import java.io.IOException

object TablutRandomWhiteClient {
    @Throws(ClassNotFoundException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var array = arrayOf("WHITE")
        if (args.isNotEmpty()) {
            array = arrayOf("WHITE", args[0])
        }
        TablutRandomClient.main(array)
    }
}