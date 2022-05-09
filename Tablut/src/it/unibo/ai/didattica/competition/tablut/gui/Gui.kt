package it.unibo.ai.didattica.competition.tablut.gui

import it.unibo.ai.didattica.competition.tablut.domain.State
import javax.swing.JFrame

/**
 *
 * This class represent an instrument that control the graphics
 * @author A.Piretti
 */
class Gui(private val game: Int) {
    var frame: Background? = null

    init {
        initGUI()
        show()
    }

    /**
     * Update the graphic whit a new state of the game
     * @param aState represent the new state of the game
     */
    fun update(aState: State?) {
        frame!!.setaState(aState)
        frame!!.repaint()
    }

    /**
     * Initialization
     */
    private fun initGUI() {
        when (game) {
            1, 4, 2 -> {
                frame = BackgroundTablut()
                (frame as BackgroundTablut).defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                (frame as BackgroundTablut).setSize(280, 300)
            }
            3 -> {
                frame = BackgroundBrandub()
                (frame as BackgroundBrandub).defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                (frame as BackgroundBrandub).setSize(280, 300)
            }
            else -> {
                println("Error in GUI init")
                System.exit(4)
            }
        }
    }

    /**
     * Display the window
     */
    private fun show() {
        when (game) {
            1 -> {
                frame!!.setSize(370, 395)
                frame!!.title = "ClassicTablut"
                frame!!.isVisible = true
            }
            2 -> {
                frame!!.setSize(370, 395)
                frame!!.title = "ModernTablut"
                frame!!.isVisible = true
            }
            3 -> {
                frame!!.setSize(300, 330)
                frame!!.title = "Brandub"
                frame!!.isVisible = true
            }
            4 -> {
                frame!!.setSize(370, 395)
                frame!!.title = "Tablut"
                frame!!.isVisible = true
            }
            else -> {
                println("Error in GUI show")
                System.exit(4)
            }
        }
    }
}