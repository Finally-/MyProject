import static java.awt.EventQueue.invokeLater;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public final class MainFrame extends JFrame {

	private static MineField field = new MineField(9, 9, 10);

	public MainFrame() {
		setTitle("Mine");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setResizable(false);
		add(field);
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		invokeLater(MainFrame::new);
	}
}
