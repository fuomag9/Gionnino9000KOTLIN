package it.unibo.ai.didattica.competition.tablut.tester

import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.Game
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.gui.Gui
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JRadioButton
import javax.swing.JTextField

class CheckerMove(theGui: Gui?, field: JTextField, state: State?, ret: TestGuiFrame, game: Game?, jr: JRadioButton) :
    ActionListener {
    private var theGui: Gui? = null
    private val posizione: JTextField
    private var state: State?
    private val ret: TestGuiFrame
    private val game: Game?
    private val turno: JRadioButton

    init {
        this.theGui = theGui
        posizione = field
        this.state = state
        this.ret = ret
        this.game = game
        turno = jr
    }

    override fun actionPerformed(e: ActionEvent) {
        val t: Turn = if (turno.isSelected) {
            state?.turn =(Turn.BLACK)
            Turn.BLACK
        } else {
            state?.turn =(Turn.WHITE)
            Turn.WHITE
        }
        if (posizione.text.length != 5) {
            println(
                "Wrong format of the move. Write moves as \"A1 A2\" where A1 is the starting cell and A2 the destination cell"
            )
        } else {
            val da = "" + posizione.text[0] + posizione.text[1]
            val a = "" + posizione.text[3] + posizione.text[4]
            posizione.text = ""
            val az: Action = try {
                Action(da, a, t)
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
            try {
                state = game!!.checkMove(state, az)
            } catch (e1: Exception) {
                // TODO Auto-generated catch block
                println("Mossa non consentita")
                println(e1.message)
            }
            ret.state=(state)
            theGui!!.update(state)
        }
    }
}