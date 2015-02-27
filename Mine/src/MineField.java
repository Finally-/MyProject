import static java.util.Collections.shuffle;
import static java.util.stream.IntStream.range;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public final class MineField extends JComponent {

	public static final int FIELD_INIT = 0;
	public static final int FIELD_START = 1;
	public static final int FIELD_SUCCESS = 2;
	public static final int FIELD_FAIL = 3;

	private final int rowCount;
	private final int colCount;

	private final int mineCount;

	private int status = FIELD_INIT;

	private final MineBlock[][] blocks;

	private final List<MineBlock> mines;

	private final List<MineBlock> flags = new LinkedList<>();

	public MineField(int rowCount, int colCount, int mineCount) {
		this.rowCount = rowCount;
		this.colCount = colCount;
		this.mineCount = mineCount;
		blocks = new MineBlock[rowCount][colCount];
		mines = new LinkedList<>();
		setLayout(new GridLayout(rowCount, colCount));
		range(0, rowCount).forEach(i -> range(0, colCount).forEach(j -> init(i, j)));
	}

	private void init(int rowNum, int colNum) {
		MineBlock block = blocks[rowNum][colNum] = new MineBlock(rowNum, colNum);
		block.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				MineField.this.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				MineField.this.mousePressed(e);
			}
		});
		add(block);
		addNeighbor(block, rowNum - 1, colNum - 1);
		addNeighbor(block, rowNum - 1, colNum);
		addNeighbor(block, rowNum - 1, colNum + 1);
		addNeighbor(block, rowNum, colNum - 1);
	}

	private void addNeighbor(MineBlock block, int rowNum, int colNum) {
		if (rowNum >= 0 && rowNum < rowCount && colNum >= 0 && colNum < colCount) {
			block.getNeighbors().add(blocks[rowNum][colNum]);
			blocks[rowNum][colNum].getNeighbors().add(block);
		}
	}

	private void mousePressed(MouseEvent event) {
		MineBlock block = (MineBlock) event.getSource();
		switch (status) {
		case FIELD_INIT:
		case FIELD_START:
			if (isLeftMouseButton(event)) {
				if (block.isCheckable())
					block.setStatus(MineBlock.BLOCK_PRESS);
				if (isRightMouseButton(event))
					block.getNeighbors().stream().filter(b -> b.isCheckable())
							.forEach(b -> b.setStatus(MineBlock.BLOCK_PRESS));
			} else if (isRightMouseButton(event)) {
				switch (block.getStatus()) {
				case MineBlock.BLOCK_INIT:
					block.setStatus(MineBlock.BLOCK_FLAG);
					flags.add(block);
					break;
				case MineBlock.BLOCK_FLAG:
					block.setStatus(MineBlock.BLOCK_CONFUSE);
					flags.remove(block);
					break;
				case MineBlock.BLOCK_CONFUSE:
					block.setStatus(MineBlock.BLOCK_INIT);
					break;
				}
			}
		}
		repaint();
	}

	private void mouseClicked(MouseEvent event) {
		MineBlock block = (MineBlock) event.getSource();
		if (isLeftMouseButton(event)) {
			int status = MineField.this.status;
			if (status == FIELD_INIT) {
				if (block.isCheckable()) {
					List<Boolean> mines = new ArrayList<>(rowCount * colCount - 1);
					range(0, mineCount).forEach(i -> mines.add(false));
					range(mineCount, mines.size()).forEach(i -> mines.add(true));
					shuffle(mines);
					mines.add(block.getRowNum() * colCount + block.getColNum(), false);
					range(0, mines.size()).filter(mines::get).forEach(i -> {
						blocks[i / colCount][i % colCount].setMark(MineBlock.MARK_MINE);
						MineField.this.mines.add(block);
						blocks[i / colCount][i % colCount].getNeighbors().forEach(b -> b.setMark(b.getMark() + 1));
					});
					MineField.this.status = FIELD_START;
				}
			}
			if (status == FIELD_START) {
				switch (block.getStatus()) {
				case MineBlock.MARK_MINE:
					block.setStatus(MineBlock.BLOCK_BOOM);
					// TODO
					break;
				case 0:
				default:
				}
			}
			// TODO Auto-generated method stub
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(colCount * MineBlock.SIZE, rowCount * MineBlock.SIZE);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(rowCount * (colCount + 1));
		range(0, colCount).forEach(i -> {
			range(0, rowCount).forEach(j -> builder.append(blocks[i * colCount + j]));
			builder.append("\n");
		});
		return builder.toString();
	}
}
