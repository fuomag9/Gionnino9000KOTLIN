package it.unibo.ai.didattica.competition.tablut.client

import java.io.IOException

object TablutRandomBlackClient {
    @Throws(ClassNotFoundException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var array = arrayOf("BLACK")
        if (args.isNotEmpty()) {
            array = arrayOf("BLACK", args[0])
        }
        TablutRandomClient.main(array)
    }
}