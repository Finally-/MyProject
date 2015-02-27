import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

public interface ImageFactory {

	Image IMG_INIT = getImage("init");
	Image IMG_PRESS = getImage("pressed");
	Image IMG_MINE = getImage("mine");
	Image IMG_BOOM = getImage("boom");
	Image IMG_FLAG = getImage("flag");
	Image IMG_CONFUSE = getImage("confuse");
	Image IMG_WRONG = getImage("wrong");
	Image IMG_NUMBER[] = new Image[9];

	static Image getImage(String imageName) {
		return new ImageIcon("src/image/" + imageName + ".png").getImage();
	}

	static void drawImage(Graphics g, String imageName) {
		drawImage(g, getImage(imageName));
	}

	static void drawImage(Graphics g, Image img) {
		g.drawImage(img, 0, 0, null);
	}
}
