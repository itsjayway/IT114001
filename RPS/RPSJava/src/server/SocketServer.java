package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import client.Game;
import utils.Debug;

public class SocketServer {
	int port = 3000;
	public static boolean isRunning = false;
	private List<RoomT> rooms = new ArrayList<RoomT>();
	private RoomT lobby;// here for convenience
	private List<RoomT> isolatedPrelobbies = new ArrayList<RoomT>();
	private final static String PRELOBBY = "PreLobby";
	protected final static String LOBBY = "Lobby";
	private List<Game> games = new ArrayList<Game>();

	private void start(int port) {
		this.port = port;
		Debug.log("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			isRunning = true;
			// create a lobby on start
			RoomT.setServer(this);
			lobby = new RoomT(LOBBY);// , this);
			rooms.add(lobby);
			while (SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					Debug.log("Client connecting...");
					// Server thread is the server's representation of the client
					ServerThread thread = new ServerThread(client, lobby);
					thread.start();
					// create a dummy room until we get further client details
					// technically once a user fully joins this lobby will be destroyed
					// but we'll track it in an array so we can attempt to clean it up just in case
					RoomT prelobby = new RoomT(PRELOBBY);// , this);
					prelobby.addClient(thread);
					isolatedPrelobbies.add(prelobby);

					Debug.log("Client added to clients pool");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				cleanup();
				Debug.log("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void cleanupRoom(RoomT r) {
		isolatedPrelobbies.remove(r);
	}

	private void cleanup() {
		Iterator<RoomT> rooms = this.rooms.iterator();
		while (rooms.hasNext()) {
			RoomT r = rooms.next();
			try {
				r.close();
			} catch (Exception e) {
				// it's ok to ignore this one
			}
		}
		Iterator<RoomT> pl = isolatedPrelobbies.iterator();
		while (pl.hasNext()) {
			RoomT r = pl.next();
			try {
				r.close();
			} catch (Exception e) {
				// it's ok to ignore this one
			}
		}
		try {
			lobby.close();
		} catch (Exception e) {
			// ok to ignore this too
		}
	}

	protected RoomT getLobby() {
		return lobby;
	}

	protected List<String> getRooms() {
		// not the most efficient way to do it, but it works
		List<String> roomNames = new ArrayList<String>();
		Iterator<RoomT> iter = rooms.iterator();
		while (iter.hasNext()) {
			RoomT r = iter.next();
			if (r != null && r.getName() != null) {
				roomNames.add(r.getName());
			}
		}
		return roomNames;
	}

	/***
	 * Special helper to join the lobby and close the previous room client was in if
	 * it's marked as Prelobby. Mostly used for prelobby once the server receives
	 * more client details.
	 * 
	 * @param client
	 */
	protected void joinLobby(ServerThread client) {
		RoomT prelobby = client.getCurrentRoom();
		if (joinRoom(LOBBY, client)) {
			prelobby.removeClient(client);
			Debug.log("Added " + client.getClientName() + " to Lobby; Prelobby should self destruct");
		} else {
			Debug.log("Problem moving " + client.getClientName() + " to lobby");
		}
	}

	/***
	 * Helper function to check if room exists by case insensitive name
	 * 
	 * @param roomName The name of the room to look for
	 * @return matched Room or null if not found
	 */
	private RoomT getRoom(String roomName) {
		for (int i = 0, l = rooms.size(); i < l; i++) {
			RoomT r = rooms.get(i);
			if (r == null || r.getName() == null) {
				continue;
			}
			if (r.getName().equalsIgnoreCase(roomName)) {
				return r;
			}
		}
		return null;
	}

	/***
	 * Attempts to join a room by name. Will remove client from old room and add
	 * them to the new room.
	 * 
	 * @param roomName The desired room to join
	 * @param client   The client moving rooms
	 * @return true if reassign worked; false if new room doesn't exist
	 */
	protected synchronized boolean joinRoom(String roomName, ServerThread client) {
		if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
			return false;
		}
		RoomT newRoom = getRoom(roomName);
		RoomT oldRoom = client.getCurrentRoom();
		if (newRoom != null) {
			if (oldRoom != null) {
				Debug.log(client.getClientName() + " leaving room " + oldRoom.getName());
				oldRoom.removeClient(client);
			}
			Debug.log(client.getClientName() + " joining room " + newRoom.getName());
			newRoom.addClient(client);
			return true;
		}
		return false;
	}

//	protected synchronized boolean joinGame(String gameName, ServerThread client) {
//		if (gameName == null)
//			return false;
//		Game new = 
//	}

	/***
	 * Attempts to create a room with given name if it doesn't exist already.
	 * 
	 * @param roomName The desired room to create
	 * @return true if it was created and false if it exists
	 */
	protected synchronized boolean createNewRoom(String roomName) {
		if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
			return false;
		}
		if (getRoom(roomName) != null) {
			// TODO can't create room
			Debug.log("Room already exists");
			return false;
		} else {
			RoomT room = new RoomT(roomName);// , this);
			rooms.add(room);
			Debug.log("Created new room: " + roomName);
			return true;
		}
	}
//	protected synchronized boolean createNewGame(String gameName) {
//		if (gameName == null)
//	}

	public static void main(String[] args) {
		// let's allow port to be passed as a command line arg
		// in eclipse you can set this via "Run Configurations"
		// -> "Arguments" -> type the port in the text box -> Apply
		int port = -1;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			// ignore this, we know it was a parsing issue
		}
		if (port > -1) {
			Debug.log("Starting Server");
			SocketServer server = new SocketServer();
			Debug.log("Listening on port " + port);
			server.start(port);
			Debug.log("Server Stopped");
		}
	}

	public void joinGame(String game, ServerThread client) {
		// TODO Auto-generated method stub

	}
}