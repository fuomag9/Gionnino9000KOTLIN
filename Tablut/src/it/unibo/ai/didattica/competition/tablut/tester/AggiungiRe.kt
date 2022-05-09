package it.unibo.ai.didattica.competition.tablut.tester

import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.gui.Gui
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JTextField

class AggiungiRe(
    private val theGui: Gui,
    private val posizione: JTextField,
    private val state: State?,
    private val ret: TestGuiFrame
) : ActionListener {
    override fun actionPerformed(e: ActionEvent) {
        val casella = posizione.text
        posizione.text = ""
        val a: Action
        a = Action(casella, casella, Turn.WHITE)
        val column = a.columnFrom
        val row = a.rowFrom
        state!!.board?.get(row)?.set(column, Pawn.KING)
        theGui.update(state)
        ret.state=(state)
    }
}