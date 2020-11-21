package client;

import java.io.IOException;

import javax.swing.JTextField;

class Interaction {
	SocketClient client;

	public Interaction() {

	}

	public void connect(String host, String port, JTextField errorField) throws IOException {
		// thread just so we don't lock up main UI
		Thread connectionThread = new Thread() {
			@Override
			public void run() {
				client = SocketClient.INSTANCE;
				try {
					client.start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					client.connect(host, port);
					client.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					errorField.setText(e.getMessage());
					errorField.getParent().setVisible(true);
				} // this terminates when client is closed

				System.out.println("Connection thread finished");
			}
		};
		connectionThread.start();
	}

	public void sendChoice(String choice) {
		SocketClient.INSTANCE.sendChoice(choice);
	}

	public boolean isClientConnected() {
		if (client == null) {
			return true;// just so loop doesn't die early
		}
		return SocketClient.INSTANCE.isStillConnected();
	}

	public String getMessage() {
		if (client == null) {
			return null;
		}
		return null;
	}
}