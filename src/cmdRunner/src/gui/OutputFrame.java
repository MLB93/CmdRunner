package gui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OutputFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void show(String text, String title) {
		OutputFrame frame = new OutputFrame(text, title);
		frame.setVisible(true);
	}

	private OutputFrame(String text, String title) {
		JTextArea textArea = new JTextArea(text);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.GREEN);
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(750, 450);
		setTitle(title);
		setIconImage(TrayIconManager.IMAGE);
	}

}
