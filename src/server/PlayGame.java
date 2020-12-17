package server;

import java.util.ArrayList;

public class PlayGame {
	final int TIME = 15; // time in seconds for each player to make a decision

	public void startGame() {

	}

	public static int Gameplay(int player1, int player2) {
		if (toString(player1).equals("rock")) {
			if (toString(player2).equals("paper")) // player2 lose
				return 2;
			else
				return 1;
		}
		if (toString(player1).equals("paper")) {
			if (toString(player2).equals("scissors")) // player2 wins
				return 2;
			else // player 1 wins
				return 1;
		}
		if (toString(player1).equals("scissors")) {
			if (toString(player2).equals("rock")) // p1 win
				return 2;
			else // p0 win
				return 1;
		}
		return 0;
	}

	public static int Gameplay(String player1, String player2) {
		if (player1.toLowerCase().equals("rock")) {
			if (player2.toLowerCase().equals("paper")) // player2 lose
				return 2;
			else
				return 1;
		}
		if (player1.toLowerCase().equals("paper")) {
			if (player2.toLowerCase().equals("scissors")) // player2 wins
				return 2;
			else // player 1 wins
				return 1;
		}
		if (player1.toLowerCase().equals("scissors")) {
			if (player2.toLowerCase().equals("rock")) // p1 win
				return 2;
			else // p0 win
				return 1;
		}
		return 0;
	}

	public static int Gameplay(String[] in) {
		if (in[0].toLowerCase().equals("rock")) {
			if (in[1].toLowerCase().equals("paper")) // in[1] lose
				return 2;
			else
				return 1;
		}
		if (in[0].toLowerCase().equals("paper")) {
			if (in[1].toLowerCase().equals("scissors")) // in[1] wins
				return 2;
			else // player 1 wins
				return 1;
		}
		if (in[0].toLowerCase().equals("scissors")) {
			if (in[1].toLowerCase().equals("rock")) // p1 win
				return 2;
			else // p0 win
				return 1;
		}
		return 0;
	}

	public static int Gameplay(ArrayList<ClientPlayer> in) {
		if (in.get(0).client.choice == "none") {
			return 2;
		} else if (in.get(1).client.choice == "none") {
			return 1;
		}

		if (in.get(0).client.choice.toLowerCase().equals("rock")) {
			if (in.get(1).client.choice.toLowerCase().equals("paper")) // in.get(1) lose
				return 2;
			else
				return 1;
		}

		if (in.get(0).client.choice.equals("paper")) {
			if (in.get(1).client.choice.equals("scissors")) // in.get(1) wins
				return 2;
			else // player 1 wins
				return 1;
		}
		if (in.get(0).client.choice.equals("scissors")) {
			if (in.get(1).client.choice.equals("rock")) // p1 win
				return 2;
			else // p0 win
				return 1;
		}
		return 0;
	}

	static String toString(int in) {
		switch (in) {
		case -1:
			return "rock";
		case 0:
			return "paper";
		case 1:
			return "scissors";
		default:
			return "INVALID INPUT";
		}
	}
}
