package client;

import java.awt.Point;

public interface Event {
	void onClientConnect(String clientName, String message);

	void onClientDisconnect(String clientName, String message);

	void onMessageReceive(String clientName, String message);

	void onChangeRoom();

	void onChoiceReceive(String clientName, String choice);

	void onSyncDirection(String clientName, Point direction);

	void onSyncPosition(String clientName, Point position);

	void onGetRoom(String roomName);

	void onSetCountdown(String message, int duration);

}