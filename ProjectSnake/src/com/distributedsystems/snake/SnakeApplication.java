package com.distributedsystems.snake;

import android.app.Application;

public class SnakeApplication extends Application {

	private PeerClient peerClient;

	public PeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(PeerClient peerClient) {
		this.peerClient = peerClient;
	}


}
