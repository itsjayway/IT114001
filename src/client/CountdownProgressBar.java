package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/* 
 
 based on example provided by Andrew Thompson
 https://stackoverflow.com/questions/5931933/how-to-make-timer-countdown-along-with-progress-bar

*/

class CountDownProgressBar {

	Timer time;
	JProgressBar progressBar;

	CountDownProgressBar(int timeInSecs) {
		int timeInMs = timeInSecs * 100;
		progressBar = new JProgressBar(JProgressBar.VERTICAL, 0, 15);
		progressBar.setValue(15);
		ActionListener listener = new ActionListener() {
			int counter = 15;

			public void actionPerformed(ActionEvent ae) {
				counter--;
				progressBar.setValue(counter);
				if (counter < 1) {
					time.stop();
				}
			}
		};
		time = new Timer(timeInMs, listener);
		time.start();
		JOptionPane.showMessageDialog(null, progressBar);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int seconds = 15;
				new CountDownProgressBar(seconds);
			}
		});
	}
}