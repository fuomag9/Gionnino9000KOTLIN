package it.unibo.ai.didattica.competition.tablut.client

import java.io.IOException

object TablutHumanWhiteClient {
    @Throws(ClassNotFoundException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val array = arrayOf("WHITE")
        TablutHumanClient.main(array)
    }
}