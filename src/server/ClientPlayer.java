package server;

import client.Player;

public class ClientPlayer {
	public ClientPlayer(ServerThread client, Player playerIn) {
		this.client = client;
		this.player = playerIn;
		choiced = playerIn.choice;
	}

	public ServerThread client;
	public Player player;
	public String choiced;
}