package core;

import java.util.Scanner;

import client.RPSInput;

public class Game {
	public static void main(String[] args) {
		boolean playing = true;
		Scanner in = new Scanner(System.in);
		RPSInput UI = new RPSInput();
		UI.setVisible(true);

		while (playing) {

			System.out.println("-1: rock, 0: paper, 1: scissors");
			System.out.println("player 1 input: ");
			int player1 = UI.getChoice();
			System.out.println("player 2 input: ");
			int player2 = UI.getChoice();
			int result = PlayGame.Gameplay(player1, player2);

			if (PlayGame.Gameplay(player1, player2) == 1)
				System.out.println("Player 1 wins!");
			else
				System.out.println("Player 2 wins!");
			System.out.println("Continue? (Y/N): ");
			String test = in.next();
			if (test.toUpperCase().equals("N"))
				playing = false;
		}
		System.out.println("End.");
	}
}