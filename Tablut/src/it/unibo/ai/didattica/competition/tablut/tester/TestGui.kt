package it.unibo.ai.didattica.competition.tablut.tester

import javax.swing.JFrame

class TestGui(game: Int) {
    init {
        val frame = TestGuiFrame(game)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(600, 195)
        frame.title = "Tester"
        frame.isVisible = true
    }
}