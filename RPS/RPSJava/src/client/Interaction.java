package client;

class Interaction {
	static SocketClient client;

	public Interaction() {

	}

	public static void sendChoice(String choice) {
		client.sendChoice(choice);
	}

	public boolean isClientConnected() {
		if (client == null) {
			return true;// just so loop doesn't die early
		}
		return client.isStillConnected();
	}

}