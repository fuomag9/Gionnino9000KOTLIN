package it.unibo.ai.didattica.competition.tablut.gui;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.Objects;

import javax.imageio.ImageIO;


public class BackgroundTablut extends Background {

	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 1L;
	
	public BackgroundTablut()
	{
		super();
		try
		{
			InputStream input = Gui.class.getResourceAsStream("resources/board2.png");
			super.background = ImageIO.read(Objects.requireNonNull(input));
			super.background = super.background.getScaledInstance(355,360,Image.SCALE_DEFAULT);
			input = Gui.class.getResourceAsStream("resources/black3.png");
			super.black = ImageIO.read(Objects.requireNonNull(input));
			super.black = super.black.getScaledInstance(34, 34, Image.SCALE_DEFAULT);
			input = Gui.class.getResourceAsStream("resources/White1.png");
			super.white = ImageIO.read(Objects.requireNonNull(input));
			super.white = super.white.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
			input = Gui.class.getResourceAsStream("resources/ImmagineRe.png");
			super.king = ImageIO.read(Objects.requireNonNull(input));
			super.king = super.king.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
		}
		catch(IOException ie)
		{
			System.out.println(ie.getMessage());
		}
	}

}
