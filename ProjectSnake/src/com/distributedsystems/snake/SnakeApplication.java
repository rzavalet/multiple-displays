package com.distributedsystems.snake;

import com.distributedsystems.middleware.PeerInformation;

import android.app.Application;

public class SnakeApplication extends Application {

	private String myId;
	private int myPort;
	private PeerInformation tracker;
	private int typeOfGame;
	
	public int getTypeOfGame() {
		return typeOfGame;
	}

	public void setTypeOfGame(int typeOfGame) {
		this.typeOfGame = typeOfGame;
	}

	public String getMyId() {
		return myId;
	}

	public void setMyId(String myId) {
		this.myId = myId;
	}

	public int getMyPort() {
		return myPort;
	}

	public void setMyPort(int myPort) {
		this.myPort = myPort;
	}

	public PeerInformation getTracker() {
		return tracker;
	}

	public void setTracker(PeerInformation tracker) {
		this.tracker = tracker;
	}


}
