package it.unibo.ai.didattica.competition.tablut.gui

import it.unibo.ai.didattica.competition.tablut.domain.State
import java.awt.Graphics
import java.awt.Image
import java.io.Serial
import javax.swing.JFrame

abstract class Background : JFrame() {
    protected var background: Image? = null
    protected var black: Image? = null
    protected var white: Image? = null
    protected var king: Image? = null
    protected var aState: State? = null

    fun setaState(aState: State?) {
        this.aState = aState
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.drawImage(background, 10, 30, null)
        for (i in aState?.board?.indices!!) {
            for (j in aState?.board!!.indices) {
                if (aState!!.getPawn(i, j)!!.equalsPawn("B")) {
                    val posX = 34 + i * 37
                    val posY = 12 + j * 37
                    g.drawImage(black, posY, posX, null)
                }
                if (aState!!.getPawn(i, j)!!.equalsPawn("W")) {
                    val posX = 35 + i * 37
                    val posY = 12 + j * 37
                    g.drawImage(white, posY, posX, null)
                }
                if (aState!!.getPawn(i, j)!!.equalsPawn("K")) {
                    val posX = 34 + i * 37
                    val posY = 12 + j * 37
                    g.drawImage(king, posY, posX, null)
                }
            }
        }
        g.dispose()
    }

    companion object {
        /**
         *
         */
        @Serial
        private val serialVersionUID = 1L
    }
}