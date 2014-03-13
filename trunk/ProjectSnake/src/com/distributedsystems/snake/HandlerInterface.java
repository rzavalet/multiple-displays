package com.distributedsystems.snake;

import com.distributedsystems.middleware.PeerConnection;
import com.distributedsystems.middleware.PeerMessage;

public interface HandlerInterface {
	void handleMessage(PeerConnection connection, PeerMessage message);
}
