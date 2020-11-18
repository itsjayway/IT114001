package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import core.BaseGamePanel;

public class GamePanelNew extends BaseGamePanel implements Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1121202275148798015L;
	List<Player> players;
	Player myPlayer;
	String playerUsername;// caching it so we don't lose it when room is wiped
	List<Player> gameplayers;
	private final static Logger log = Logger.getLogger(GamePanel.class.getName());

	public void setPlayerName(String name) {
		playerUsername = name;
		if (myPlayer != null) {
			myPlayer.setName(playerUsername);
		}
	}

	@Override
	public synchronized void onClientConnect(String clientName, String message) {
		// TODO Auto-generated method stub
		System.out.println("Connected on Game Panel: " + clientName);
		boolean exists = false;
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			Player p = new Player();
			p.setName(clientName);
			players.add(p);
			if (clientName.equals(playerUsername)) {
				System.out.println("Reset myPlayer");
				myPlayer = p;
			}
		}
	}

	@Override
	public void onClientDisconnect(String clientName, String message) {

		// TODO Auto-generated method stub
		System.out.println("Disconnected on Game Panel: " + clientName);
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && !p.getName().equals(playerUsername) && p.getName().equalsIgnoreCase(clientName)) {
				iter.remove();
				break;
			}
		}
	}

	@Override
	public void onMessageReceive(String clientName, String message) {
		// TODO Auto-generated method stub
		System.out.println("Message on Game Panel");

	}

	@Override
	public void onChangeRoom() {
		// don't clear, since we're using iterators to loop, remove via iterator
		// players.clear();
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			// if (p != myPlayer) {
			iter.remove();
			// }
		}
		myPlayer = null;
		System.out.println("Cleared players");
	}

	@Override
	public void awake() {
		players = new ArrayList<Player>();
	}

	@Override
	public void start() {
		JFrame frame = new JFrame("Rock-Paper-Scissors");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLayout(new BorderLayout());
		// create panel
		JPanel rps = new JPanel();
		rps.setPreferredSize(new Dimension(400, 400));
		rps.setLayout(new BorderLayout());
		// create text area for messages
		JTextArea textArea = new JTextArea();
		// don't let the user edit this directly
		textArea.setEditable(false);
		textArea.setText("");
		// create panel to hold multiple controls
		JPanel attemptsArea = new JPanel();
		attemptsArea.setLayout(new BorderLayout());
		// add text area to history/attempts
		attemptsArea.add(textArea, BorderLayout.CENTER);
		attemptsArea.setBorder(BorderFactory.createLineBorder(Color.black));
		// add history/attempts to panel
		rps.add(attemptsArea, BorderLayout.CENTER);
		// create panel to hold multiple controls
		JPanel userInput = new JPanel();
		JButton rock = new JButton();
		rock.setText("Rock");
		rock.setPreferredSize(new Dimension(100, 30));
		rock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Interaction.sendChoice("rock");
			}
		});
		// create paper button
		JButton paper = new JButton();
		paper.setText("Paper");
		paper.setPreferredSize(new Dimension(100, 30));
		paper.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Interaction.sendChoice("paper");
			}
		});
		// create scissors button
		JButton scissors = new JButton();
		scissors.setText("Scissors");
		scissors.setPreferredSize(new Dimension(100, 30));
		scissors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Interaction.sendChoice("scissors");
			}
		});

		JButton close = new JButton();
		close.setText("Exit");
		close.setPreferredSize(new Dimension(90, 20));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Interaction.sendChoice("exit");
			}
		});

		userInput.add(rock);
		userInput.add(paper);
		userInput.add(scissors);
		// add panel to rps panel
		rps.add(userInput, BorderLayout.SOUTH);
		// add rps panel to frame
		frame.add(rps, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Gets the current state of input to apply movement to our player
	 */

	@Override
	public void lateUpdate() {
		// stuff that should happen at a slightly different time than stuff in normal
		// update()

	}

	@Override
	public synchronized void draw(Graphics g) {

	}

	@SuppressWarnings("unused")
	private synchronized void drawPlayers(Graphics g) {

	}

	@SuppressWarnings("unused")
	private void drawText(Graphics g) {

	}

	@Override
	public void quit() {
		log.log(Level.INFO, "GamePanel quit");
	}

	@Override
	public void attachListeners() {
		InputMap im = this.getRootPane().getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");
		ActionMap am = this.getRootPane().getActionMap();

		am.put("up_pressed", new MoveAction(KeyEvent.VK_W, true));
		am.put("up_released", new MoveAction(KeyEvent.VK_W, false));

		am.put("down_pressed", new MoveAction(KeyEvent.VK_S, true));
		am.put("down_released", new MoveAction(KeyEvent.VK_S, false));

		am.put("left_pressed", new MoveAction(KeyEvent.VK_A, true));
		am.put("left_released", new MoveAction(KeyEvent.VK_A, false));

		am.put("right_pressed", new MoveAction(KeyEvent.VK_D, true));
		am.put("right_released", new MoveAction(KeyEvent.VK_D, false));
	}

	@Override
	public void onSyncDirection(String clientName, Point direction) {
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println("Syncing direction: " + clientName);
				p.setDirection(direction.x, direction.y);
				System.out.println("From: " + direction);
				System.out.println("To: " + p.getDirection());
				break;
			}
		}
	}

	@Override
	public void onSyncPosition(String clientName, Point position) {
		System.out.println("Got position for " + clientName);
		Iterator<Player> iter = players.iterator();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println(clientName + " set " + position);
				p.setPosition(position);
				break;
			}
		}
	}

	@Override
	public void onGetRoom(String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}
}