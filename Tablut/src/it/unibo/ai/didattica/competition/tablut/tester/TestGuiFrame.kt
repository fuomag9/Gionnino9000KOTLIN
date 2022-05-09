package it.unibo.ai.didattica.competition.tablut.tester

import it.unibo.ai.didattica.competition.tablut.domain.*
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import it.unibo.ai.didattica.competition.tablut.gui.Gui
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Label
import java.io.Serial
import javax.swing.*
import kotlin.system.exitProcess

class TestGuiFrame(game: Int) : JFrame() {
    var state: State? = null
    private var theGame: Game? = null

    init {
        when (game) {
            1 -> {
                val board = Array<Array<Pawn?>?>(9) { arrayOfNulls(9) }
                for (i in 0..8) {
                    for (j in 0..8) {
                        board[i]!![j] = Pawn.EMPTY
                    }
                }
                board[4]!![4] = Pawn.THRONE
                state = StateTablut()
                theGame = GameTablut(10)
                (state as StateTablut).board=(board)
            }
            2 -> {
                val board1 = Array<Array<Pawn?>?>(9) { arrayOfNulls(9) }
                for (i in 0..8) {
                    for (j in 0..8) {
                        board1[i]!![j] = Pawn.EMPTY
                    }
                }
                board1[4]!![4] = Pawn.THRONE
                state = StateTablut()
                theGame = GameModernTablut(10)
                (state as StateTablut).board=(board1)
            }
            3 -> {
                val board2 = Array<Array<Pawn?>?>(7) { arrayOfNulls(7) }
                for (i in 0..6) {
                    for (j in 0..6) {
                        board2[i]!![j] = Pawn.EMPTY
                    }
                }
                board2[3]!![3] = Pawn.THRONE
                state = StateBrandub()
                theGame = GameTablut(10)
                (state as StateBrandub).board=(board2)
            }
            4 -> {
                val board3 = Array<Array<Pawn?>?>(9) { arrayOfNulls(9) }
                for (i in 0..8) {
                    for (j in 0..8) {
                        board3[i]!![j] = Pawn.EMPTY
                    }
                }
                board3[4]!![4] = Pawn.THRONE
                state = StateTablut()
                (state as StateTablut).turn=(Turn.WHITE)
                theGame = GameAshtonTablut(0, -1, "test", "testW", "testB")
                (state as StateTablut).board=(board3)
            }
            else -> {
                println("Error in game selection")
                exitProcess(4)
            }
        }
        val theGui = Gui(game)
        theGui.update(state)
        val pannello1 = JPanel()
        val pannello2 = JPanel()
        val nere = JTextField("")
        val turno = JRadioButton("Nero")
        val turno2 = JRadioButton("Bianco")
        val bg = ButtonGroup()
        bg.add(turno)
        bg.add(turno2)
        val bianche = JTextField("")
        val re = JTextField("")
        val azione = JTextField("")
        val aggNere = JButton("Aggiungi nera")
        val aggBianche = JButton("Aggiungi bianca")
        val aggRe = JButton("Aggiungi re")
        val aggAzione = JButton("Testa azione")
        pannello1.layout = GridLayout(5, 0)
        val l1 = Label("Aggiungi pedina nera: ")
        aggNere.addActionListener(AggiungiNero(theGui, nere, state, this))
        aggBianche.addActionListener(AggiungiBianco(theGui, bianche, state, this))
        aggRe.addActionListener(AggiungiRe(theGui, re, state, this))
        aggAzione.addActionListener(CheckerMove(theGui, azione, state, this, theGame, turno))
        pannello1.add(l1)
        pannello1.add(nere)
        pannello1.add(aggNere)
        val l2 = Label("Aggiungi pedina bianca: ")
        pannello1.add(l2)
        pannello1.add(bianche)
        pannello1.add(aggBianche)
        val l3 = Label("Aggiungi re: ")
        pannello1.add(l3)
        pannello1.add(re)
        pannello1.add(aggRe)
        val l4 = Label("Mossa: ")
        pannello1.add(l4)
        pannello1.add(azione)
        pannello1.add(aggAzione)
        pannello2.add(Label("Turno:"))
        turno.isSelected = true
        pannello2.add(turno)
        pannello2.add(turno2)
        this.add(pannello1, BorderLayout.NORTH)
        this.add(pannello2, BorderLayout.SOUTH)
    }

    companion object {
        /**
         *
         */
        @Serial
        private val serialVersionUID = 1L
    }
}