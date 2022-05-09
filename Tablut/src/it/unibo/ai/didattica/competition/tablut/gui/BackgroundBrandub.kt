package it.unibo.ai.didattica.competition.tablut.gui

import java.awt.Image
import java.io.IOException
import java.io.Serial
import java.util.*
import javax.imageio.ImageIO

class BackgroundBrandub : Background() {
    init {
        try {
            var input = Gui::class.java.getResourceAsStream("resources/brandub1.png")
            super.background = ImageIO.read(Objects.requireNonNull(input))
            super.background = super.background!!.getScaledInstance(283, 292, Image.SCALE_DEFAULT)
            input = Gui::class.java.getResourceAsStream("resources/black3.png")
            super.black = ImageIO.read(Objects.requireNonNull(input))
            super.black = super.black!!.getScaledInstance(34, 34, Image.SCALE_DEFAULT)
            input = Gui::class.java.getResourceAsStream("resources/White1.png")
            super.white = ImageIO.read(Objects.requireNonNull(input))
            super.white = super.white!!.getScaledInstance(32, 32, Image.SCALE_DEFAULT)
            input = Gui::class.java.getResourceAsStream("resources/ImmagineRe.png")
            super.king = ImageIO.read(Objects.requireNonNull(input))
            super.king = super.king!!.getScaledInstance(32, 32, Image.SCALE_DEFAULT)
        } catch (ie: IOException) {
            println(ie.message)
        }
    }

    companion object {
        /**
         *
         */
        @Serial
        private val serialVersionUID = 1L
    }
}