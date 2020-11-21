package practice;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class TestFrame {
	private static int width;
	private static int height;
	private static JButton button;
	private static JFrame frame;

	public static void main(String[] args) {
		TestFrame er = new TestFrame(400, 400);
		er.setUpGUI();
		er.setUpButtonListeners();

	}

	public TestFrame(int w, int h) {
		frame = new JFrame();
		width = w;
		height = h;
		button = new JButton("CLICK ME");
	}

	public void setUpGUI() {
		frame.setSize(width, height);
		frame.setTitle("My Java App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(button);
		frame.setVisible(true);
	}

	public void setUpButtonListeners() {
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("click");
			}
		};
		button.addActionListener(buttonListener);
	}
}
