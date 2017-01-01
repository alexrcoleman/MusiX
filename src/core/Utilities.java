package core;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utilities {
	public static BufferedImage IMAGE_CLOSE, IMAGE_ITUNES_PLUS, IMAGE_ITUNES_MINUS, IMAGE_CLIPBOARD, IMAGE_MP3,
			IMAGE_YOUTUBE;

	static {
		try {
			IMAGE_CLOSE = ImageIO.read(Utilities.class.getResourceAsStream("resources/close.png"));
			IMAGE_ITUNES_PLUS = ImageIO.read(Utilities.class.getResourceAsStream("resources/iTunes_Plus.png"));
			IMAGE_ITUNES_MINUS = ImageIO.read(Utilities.class.getResourceAsStream("resources/iTunes_Minus.png"));
			IMAGE_CLIPBOARD = ImageIO.read(Utilities.class.getResourceAsStream("resources/clipboard.png"));
			IMAGE_MP3 = ImageIO.read(Utilities.class.getResourceAsStream("resources/Music Folder.png"));
			IMAGE_YOUTUBE = ImageIO.read(Utilities.class.getResourceAsStream("resources/YoutubeLogo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
