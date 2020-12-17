package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

import client.GamePanelNew;
import client.Player;
import client.RoomsPanel;
import client.SocketClient;
import core.BaseGamePanel;

public class Room extends BaseGamePanel implements AutoCloseable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8960110950374519812L;
	private static SocketServer server;// used to refer to accessible server functions
	private String name;
	private final static Logger log = Logger.getLogger(Room.class.getName());

	// Commands
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String READY = "ready";

	private final static String ROCK = "rock";
	private final static String PAPER = "paper";
	private final static String SCISSORS = "scissors";
	private final static String autoLossCode = "uzocgmgxqciavrfxnjlotpvkpiueapmbmavcvqdpknqzbkcpwvhfykufbyhmdzlnwweigmfcdlfnfpasvzcwtlmvmdpytkduarphfjpuahwcyznjemblphbqzcjqqvzr";

	private List<ClientPlayer> clients = new ArrayList<ClientPlayer>();
	static Dimension gameAreaSize = new Dimension(400, 600);

	public Room(String name, boolean delayStart) {
		super(delayStart);
		this.name = name;
		isServer = true;
	}

	public Room(String name) {
		this.name = name;
		isServer = true;
	}

	public static void setServer(SocketServer server) {
		Room.server = server;
	}

	public String getName() {
		return name;
	}

	private static Point getRandomStartPosition() {
		Point startPos = new Point();
		startPos.x = (int) (Math.random() * gameAreaSize.width);
		startPos.y = (int) (Math.random() * gameAreaSize.height);
		return startPos;
	}

	protected synchronized void addClient(ServerThread client) {
		client.setCurrentRoom(this);
		boolean exists = false;
		// since we updated to a different List type, we'll need to loop through to find
		// the client to check against
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client == client) {
				exists = true;
				if (c.player == null) {
					log.log(Level.WARNING, "Client " + client.getClientName() + " player was null, creating");
					Player p = new Player();
					p.setName(client.getClientName());
					c.player = p;
					syncClient(c);
				}
				break;
			}
		}

		if (exists) {
			log.log(Level.INFO, "Attempting to add a client that already exists");
		} else {
			// create a player reference for this client
			// so server can determine position
			Player p = new Player();
			p.setName(client.getClientName());
			// add Player and Client reference to ClientPlayer object reference
			ClientPlayer cp = new ClientPlayer(client, p);
			clients.add(cp);// this is a "merged" list of Clients (ServerThread) and Players (Player)
			// objects
			// that's so we don't have to keep track of the same client in two different
			// list locations
			syncClient(cp);

		}
	}

	private void syncClient(ClientPlayer cp) {
		if (cp.client.getClientName() != null) {
			cp.client.sendClearList();
			sendConnectionStatus(cp.client, true, "joined the room " + getName());
			// calculate random start position
			Point startPos = Room.getRandomStartPosition();
			cp.player.setPosition(startPos);
			// tell our client of our server determined position
			cp.client.sendPosition(cp.client.getClientName(), startPos);
			// tell everyone else about our server determiend position
			sendPositionSync(cp.client, startPos);
			// get the list of connected clients (for ui panel)
			updateClientList(cp.client);
			// get dir/pos of existing players
			updatePlayers(cp.client);
		}
	}

	/***
	 * Syncs the existing players in the room with our newly connected player
	 * 
	 * @param client
	 */
	private synchronized void updatePlayers(ServerThread client) {
		// when we connect, send all existing clients current position and direction so
		// we can locally show this on our client
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client != client) {
				boolean messageSent = client.sendDirection(c.client.getClientName(), c.player.getDirection());
				if (messageSent) {
					messageSent = client.sendPosition(c.client.getClientName(), c.player.getPosition());
				}
			}
		}
	}

	/**
	 * Syncs the existing clients in the room with our newly connected client
	 * 
	 * @param client
	 */
	private synchronized void updateClientList(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client != client) {
				boolean messageSent = client.sendConnectionStatus(c.client.getClientName(), true, null);
			}
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			if (c.client == client) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.client.getClientName() + " from " + getName());
			}
		}
		if (clients.size() > 0) {
			sendConnectionStatus(client, false, "left the room " + getName());
		} else {
			cleanupEmptyRoom();
		}
	}

	private void cleanupEmptyRoom() {
		// If name is null it's already been closed. And don't close the Lobby
		if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
			return;
		}
		try {
			log.log(Level.INFO, "Closing empty room: " + name);
			close();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	protected void joinRoom(String room, ServerThread client) {
		server.joinRoom(room, client);
	}

	protected void joinLobby(ServerThread client) {
		server.joinLobby(client);
	}

	/***
	 * Helper function to process messages to trigger different functionality.
	 * 
	 * @param message The original message being sent
	 * @param client  The sender of the message (since they'll be the ones
	 *                triggering the actions)
	 */
	protected void createRoom(String room, ServerThread client) {
		if (server.createNewRoom(room)) {
			sendMessage(client, "Created a new room");
			joinRoom(room, client);
		}
	}

	String[] gamma = new String[2];

	private String processCommands(String message, ServerThread client) {
		String response = null;
		try {
			if (message.indexOf(COMMAND_TRIGGER) > -1) { // parse chat-based commands
				String[] comm = message.split(COMMAND_TRIGGER);
				log.log(Level.INFO, message);
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];
				if (command != null) {
					command = command.toLowerCase();
				}
				String roomName;
				ClientPlayer cp = null;
				switch (command) {
				case CREATE_ROOM:
					roomName = comm2[1];
					createRoom(roomName, client);
					break;
				case "printarray":
					for (ClientPlayer e : cpArr) {
						log.log(Level.INFO, "Player: " + e.player.getName() + " & Choice: " + e.client.choice);
					}
					break;
				case JOIN_ROOM: // /joinroom
					roomName = comm2[1];
					joinRoom(roomName, client);
					break;
				case ROCK: //
					log.log(Level.SEVERE, "ROCK");
					client.choice = message.substring(1);
					break;
				case PAPER:
					log.log(Level.SEVERE, "PAPER"); // RPS cases come from RPSInput line 117
					client.choice = message.substring(1);
					break;
				case SCISSORS:
					log.log(Level.SEVERE, "SCISSORS");
					client.choice = message.substring(1);
					break;
//				case FLIP:
//					response = "(╯°□°）╯︵ ┻━┻";
//				case ROLL:
//					int dice1 = (int) (Math.random() * 6 + 1);
//					System.out.println("I rolled a " + dice1 + "!");
//					System.out.println(message);
				case autoLossCode:
					log.log(Level.INFO, "TIME_RAN_OUT");
					client.choice = "none";
					cp = getCP(client);
					if (cp != null) {
						cp.player.setReady(true);
						readyCheck();
					}
					break;
				case READY:
					// TODO: add check to see if more than one player in room
					if (clients.size() > 1) {
						log.log(Level.INFO, "clients pool size: " + clients.size());

						cp = getCP(client);
						if (cp != null) {
							cp.player.setReady(true);
							readyCheck();
						}
						response = "Ready to go!";
					} else {
						sendSystemMessage("Waiting for more players...");
					}
					break;
				default:
					response = message;
					break;

				}
			} else {
				response = message;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	static int ready = 0;
	static ArrayList<ClientPlayer> cpArr = new ArrayList<ClientPlayer>();
	public static String prevWinner;

	private void readyCheck() {
		Iterator<ClientPlayer> iter = clients.iterator();
		int total = clients.size();

		// TODO: Inputs are stored, so map it to Game.java or PlayGame.Gameplay thru
		// server
		while (iter.hasNext()) {
			ClientPlayer cp = iter.next();

			if (cp.client.choice != null && cp != null && cp.player.isReady()) {
				System.out.println(cp.client.getClientName() + "'s choice: " + cp.client.choice);

				ready++;
			} else if (cp.client.choice == null) {

			}
			log.log(Level.INFO, "ready var: " + ready);

			if (ready > 1 && ready == cpArr.size()) {

				// *THE JUICE* //

				System.out.println("Got two inputs! Time to process...");
				int winner = PlayGame.Gameplay(cpArr);
				if (winner == 0) {
					System.out.println("TIE");
				}

				String winnerMessage = cpArr.get(winner - 1).player.getName() + " is the winner!";
				prevWinner = cpArr.get(winner - 1).player.getName();
				sendSystemMessage(winnerMessage);

				ready = 0; // reset condition checking to allow repeating
				cpArr.clear();
				log.log(Level.INFO, "ready -> 0, cpArr cleared...");
			}

			if (!(cpArr.contains(cp))) { // attempt at forming an array separate from that of Room.clients
				cpArr.add(cp);
				System.out.println(cp.player.getName() + " added to cpArr");
			}

			/*
			 * attempt at creating a "chain" of games:
			 *
			 * try for(int curr = 0; i < cpArr.size(); i++)
			 * Playgame.Gameplay(cpArr.get(curr).client.choice,
			 * cpArr.get(curr+1).client.choice)
			 * 
			 * catch IndexOutOfBounds Exception
			 * 
			 */
		}
	}

	private ClientPlayer getCP(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer cp = iter.next();
			if (cp.client == client) {
				return cp;
			}
		}
		return null;
	}

	protected void sendSystemMessage(String message) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.send("[Announcer]", message);
			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.client.getId());
			}
		}
	}

	JPanel textArea;
	Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
	GamePanelNew game;
	String username;
	RoomsPanel roomsPanel;
	JMenuBar menu;

	public void createDrawingPanel() {
		game = new GamePanelNew();
		game.setPreferredSize(new Dimension((int) (windowSize.width * .6), windowSize.height));
		textArea.getParent().getParent().getParent().add(game, BorderLayout.WEST);

		SocketClient.INSTANCE.registerCallbackListener(game);
	}

	protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			boolean messageSent = c.client.sendConnectionStatus(client.getClientName(), isConnect, message);
			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.client.getId());
			}
		}
	}

	/***
	 * Takes a sender and a message and broadcasts the message to all clients in
	 * this room. Client is mostly passed for command purposes but we can also use
	 * it to extract other client info.
	 * 
	 * @param sender  The client sending the message
	 * @param message The message to broadcast inside the room
	 */
	protected void sendMessage(ServerThread sender, String message) {
		log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");
		String resp = processCommands(message, sender);
		if (resp == null) {
			return;
		}
		message = resp;
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.send(sender.getClientName(), message);
			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.client.getId());
			}
		}
	}

	/**
	 * Broadcasts this client/player direction to all connected clients/players
	 * 
	 * @param sender
	 * @param dir
	 */
	protected void sendDirectionSync(ServerThread sender, Point dir) {
		boolean changed = false;
		// first we'll find the clientPlayer that sent their direction
		// and update the server-side instance of their direction
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			// update only our server reference for this client
			// if we don't have this "if" it'll update all clients (meaning everyone will
			// move in sync)
			if (client.client == sender) {
				changed = client.player.setDirection(dir.x, dir.y);
				break;
			}
		}
		// if the direction is "changed" (it should be, but check anyway)
		// then we'll broadcast the change in direction to all clients
		// so their local movement reflects correctly
		if (changed) {
			iter = clients.iterator();
			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				boolean messageSent = client.client.sendDirection(sender.getClientName(), dir);
				if (!messageSent) {
					iter.remove();
					log.log(Level.INFO, "Removed client " + client.client.getId());
				}
			}

		}
	}

	/**
	 * Broadcasts this client/player position to all connected clients/players
	 * 
	 * @param sender
	 * @param pos
	 */
	protected void sendPositionSync(ServerThread sender, Point pos) {
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.sendPosition(sender.getClientName(), pos);
			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.client.getId());
			}
		}
	}

	public List<String> getRooms() {
		return server.getRooms();
	}

	/***
	 * Will attempt to migrate any remaining clients to the Lobby room. Will then
	 * set references to null and should be eligible for garbage collection
	 */
	@Override
	public void close() throws Exception {
		int clientCount = clients.size();
		if (clientCount > 0) {
			log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");
			Iterator<ClientPlayer> iter = clients.iterator();
			Room lobby = server.getLobby();
			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				lobby.addClient(client.client);
				iter.remove();
			}
			log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");
		}
		server.cleanupRoom(this);
		name = null;
		isRunning = false;
		// should be eligible for garbage collection now
	}

	@Override
	public void awake() {

	}

	@Override
	public void start() {

		log.log(Level.INFO, getName() + " start called");
	}

	long frame = 0;

	void checkPositionSync(ClientPlayer cp) {
		// determine the maximum syncing needed
		// you do NOT need it every frame, if you do it could cause network congestion
		// and
		// lots of bandwidth that doesn't need to be utilized
		if (frame % 120 == 0) {// sync every 120 frames (i.e., if 60 fps that's every 2 seconds)
			// check if it's worth sycning the position
			// again this is to save unnecessary data transfer
			if (cp.player.changedPosition()) {
				sendPositionSync(cp.client, cp.player.getPosition());
			}
		}

	}

	@Override
	public void update() {
		// We'll make the server authoritative
		// so we'll calc movement/collisions and send the action to the clients so they
		// can visually update. Client's won't be determining this themselves
		Iterator<ClientPlayer> iter = clients.iterator();
		while (iter.hasNext()) {
			ClientPlayer p = iter.next();
			if (p != null) {
				// have the server-side player calc their potential new position
				p.player.move();
				// determine if we should sync this player's position to all other players
				checkPositionSync(p);
			}
		}

	}

	// don't call this more than once per frame
	private void nextFrame() {
		// we'll do basic frame tracking so we can trigger events
		// less frequently than each frame
		// update frame counter and prevent overflow
		if (Long.MAX_VALUE - 5 <= frame) {
			frame = Long.MIN_VALUE;
		}
		frame++;
	}

	@Override
	public void lateUpdate() {
		nextFrame();
	}

	@Override
	public void draw(Graphics g) {
		// this is the server, we won't be using this unless you're adding this view to
		// the Honor's student extra section
	}

	@Override
	public void quit() {
		// don't call close here
		log.log(Level.WARNING, getName() + " quit() ");
	}

	@Override
	public void attachListeners() {
		// no listeners either since server side receives no input
	}

}