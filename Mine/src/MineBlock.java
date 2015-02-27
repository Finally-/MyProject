import static java.lang.String.valueOf;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public final class MineBlock extends JComponent {

	public static final int SIZE = 17;

	public static final int BLOCK_INIT = 0;
	public static final int BLOCK_FLAG = 1;
	public static final int BLOCK_CONFUSE = 2;
	public static final int BLOCK_PRESS = 3;
	public static final int BLOCK_MINE = 4;
	public static final int BLOCK_BOOM = 5;
	public static final int BLOCK_WRONG = 6;
	public static final int BLOCK_NUMBER = 7;

	public static final int MARK_MINE = -1;

	private final int rowNum;
	private final int colNum;

	private final List<MineBlock> neighbors = new ArrayList<>(8);

	private int mark = 0;

	private int status = BLOCK_INIT;

	public MineBlock(int rowNum, int colNum) {
		this.rowNum = rowNum;
		this.colNum = colNum;
	}

	public int getRowNum() {
		return rowNum;
	}

	public int getColNum() {
		return colNum;
	}

	public List<MineBlock> getNeighbors() {
		return neighbors;
	}

	public int getMark() {
		return mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isCheckable() {
		return status == BLOCK_INIT || status == BLOCK_CONFUSE;
	}

	@Override
	protected void paintComponent(Graphics g) {
		switch (status) {
		case BLOCK_INIT:
			ImageFactory.drawImage(g, ImageFactory.IMG_INIT);
			break;
		case BLOCK_FLAG:
			ImageFactory.drawImage(g, ImageFactory.IMG_FLAG);
			break;
		case BLOCK_CONFUSE:
			ImageFactory.drawImage(g, ImageFactory.IMG_CONFUSE);
			break;
		case BLOCK_PRESS:
			ImageFactory.drawImage(g, ImageFactory.IMG_PRESS);
			break;
		case BLOCK_MINE:
			ImageFactory.drawImage(g, ImageFactory.IMG_MINE);
			break;
		case BLOCK_BOOM:
			ImageFactory.drawImage(g, ImageFactory.IMG_BOOM);
			break;
		case BLOCK_WRONG:
			ImageFactory.drawImage(g, ImageFactory.IMG_WRONG);
			break;
		case BLOCK_NUMBER:
			ImageFactory.drawImage(g, ImageFactory.IMG_NUMBER[mark]);
			break;
		}
	}

	@Override
	public String toString() {
		return valueOf(mark);
	}
}
