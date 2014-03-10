package com.distributedsystems.snake;

import java.io.IOException;

import android.content.Context;

public class PeerClient {

	private PeerNode peerNode;
	private SnakeView snakeView = null;
	
	private boolean shutdown = false;
	private static final boolean debug = true;
	
	/************ HERE ARE THE HANDLE METHODS **********/
	
	private class PeerName implements HandlerInterface {
		private String myId;
		
		public PeerName(String peer) {
			myId = peer;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			PeerMessage messageName = null;
			Debug.print("... Replying with peer name: " + myId, debug);
			
			messageName = new PeerMessage(PeerNode.REPLY, myId);
			try {
				connection.sendData(messageName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private class PeerList implements HandlerInterface {
		private PeerNode peer;
		
		public PeerList(PeerNode peer) {
			this.peer = peer;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			PeerMessage messageList = null;
			Debug.print("Listing peers", debug);
			
			messageList = new PeerMessage(PeerNode.REPLY, String.valueOf(peer.getNumberOfPeers()));
			try {
				connection.sendData(messageList);
				for (String currentPeerId : peer.getPeerKeys()) {
					PeerInformation currentPeerInformation= peer.getPeer(currentPeerId);
					messageList = new PeerMessage(PeerNode.REPLY, 
							currentPeerInformation.getPeerId() + " " + currentPeerInformation.getHost() + 
							" " + currentPeerInformation.getPort());
					connection.sendData(messageList);
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private class AddPeer implements HandlerInterface {
		private PeerNode peer;
		
		public AddPeer(PeerNode peer) {
			this.peer = peer;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			String[] dataList = message.getMessageData().split(" ");
			String peerId = dataList[0];
			String host = dataList[1];
			String port = dataList[2];
			
			PeerInformation peerInformation = new PeerInformation(peerId, host, Integer.valueOf(port));
			
			if (peer.getPeerKeys().contains(peerId) == false) {
				peer.insertPeer(peerInformation);
			}
						
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Peer added: " + peerId);
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
	}
	
	private class StartGame implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			System.out.println("*** STARTING GAME");
		}
	}
	
	private class EndGame implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			System.out.println("*** END GAME");
		}
	}
	
	private class MoveRight implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			System.out.println("*** MOVING RIGHT");
			snakeView.moveSnake(Snake.MOVE_RIGHT);
		}
	}
	
	private class MoveLeft implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			System.out.println("*** MOVING LEFT");
			snakeView.moveSnake(Snake.MOVE_LEFT);
		}
	}
	
	private class MoveUp implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			System.out.println("*** MOVING UP");
			snakeView.moveSnake(Snake.MOVE_UP);
		}
	}
	
	private class MoveDown implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			System.out.println("*** MOVING DOWN");
			snakeView.moveSnake(Snake.MOVE_DOWN);
		}
	}
	
	
	/**** HERE IS THE PEER CLIENT CLASS *****/
	public PeerClient(String id, int port, PeerInformation trackerPeerInformation, Context appContext) {
		peerNode = new PeerNode(id, port, trackerPeerInformation, appContext);
		
		//Add handlers
		HandlerInterface peerName = new PeerName(peerNode.getPeerId());
		peerNode.addHandler(PeerNode.GET_PEER_NAME, peerName);
		
		HandlerInterface peerList = new PeerList(peerNode);
		peerNode.addHandler(PeerNode.GET_PEER_LIST, peerList);
		
		HandlerInterface addPeer = new AddPeer(peerNode);
		peerNode.addHandler(PeerNode.ADD_PEER, addPeer);
		
		HandlerInterface startGame = new StartGame();
		peerNode.addHandler(PeerNode.START_GAME, startGame);
		
		HandlerInterface endGame = new EndGame();
		peerNode.addHandler(PeerNode.END_GAME, endGame);
		
		HandlerInterface moveRight = new MoveRight();
		peerNode.addHandler(PeerNode.MOVE_RIGHT, moveRight);
		
		HandlerInterface moveLeft = new MoveLeft();
		peerNode.addHandler(PeerNode.MOVE_LEFT, moveLeft);
		
		HandlerInterface moveUp = new MoveUp();
		peerNode.addHandler(PeerNode.MOVE_UP, moveUp);
		
		HandlerInterface moveDown = new MoveDown();
		peerNode.addHandler(PeerNode.MOVE_DOWN, moveDown);		
	}
	
	public boolean validateCommand(String[] command) {
		int numArguments;
		String commandName = null;
		
		numArguments = command.length;
		if (numArguments < 1) {
			return false;
		}
		
		commandName = command[0];
		HandlerInterface handler = peerNode.getHandler(commandName);
		if (handler == null) {
			return false;
		}
		
		return true;
	}
	
	public void printMyPeers() {
		Debug.print("... Printing peers", debug);
		for (String currentPeerId : peerNode.getPeerKeys()) {
			PeerInformation currentPeerInformation= peerNode.getPeer(currentPeerId);
			System.out.println("<" + currentPeerInformation.getPeerId() + 
					", " + currentPeerInformation.getHost() + ", " + currentPeerInformation.getPort());
		}
	}
	
	public void printHelp() {
		Debug.print("... Printing help", debug);
		System.out.println("MY_LIST: Print the list of available peers");
		System.out.println("MY_NAME: Print info of this peer");
		System.out.println("BYE: Quit");
	}
	
	public void moveRight() {
		Debug.print("Moving to the Right...", debug);
		peerNode.broadcastMessage(PeerNode.MOVE_RIGHT, "");
	}

	public void moveUp() {
		Debug.print("Moving Up...", debug);
		peerNode.broadcastMessage(PeerNode.MOVE_UP, "");
	}

	public void moveDown() {
		Debug.print("Moving Down...", debug);
		peerNode.broadcastMessage(PeerNode.MOVE_DOWN, "");
	}

	public void moveLeft() {
		Debug.print("Moving to the Left...", debug);
		peerNode.broadcastMessage(PeerNode.MOVE_LEFT, "");
	}
	public void endGame() {
		Debug.print("Finishing game...", debug);
		peerNode.broadcastMessage(PeerNode.END_GAME, "");
	}

	public void startGame() {
		Debug.print("Starting new game...", debug);
		peerNode.broadcastMessage(PeerNode.START_GAME, "");
	}


	public void setUpClient(Context context, SnakeView mSnakeView) {
		peerNode.setMyContext(context);	
		snakeView = mSnakeView;
	}

	public void startHandler() {
        new Thread("ConnectionHandler") {

			@Override
			public void run() {
				peerNode.connectionHandler();
			}
			
		}.start();
	}
	
	/*
	public static void main(String[] args) {
		int port = -1;
		String myId = null;
		
		String trackerIp = null;
		int trackerPort = -1;
		PeerInformation tracker = null;
		
		
		Debug.print("*** STARTING PEER ****", debug);
		
		if (args.length != 2 && args.length != 4) {
			Debug.print("ERROR: invalid number of parameters: Args Count: " + args.length, debug);
			return;
		}
	
		myId = args[0];
		port = Integer.parseInt(args[1]);
		Debug.print("I am: "+ myId, debug);
		
		
		if (args.length == 4) {
			trackerIp = args[2];
			trackerPort = Integer.parseInt(args[3]);
			
			Debug.print("Tracker: (" + trackerIp + ", " + trackerPort + ")", debug);
			tracker = new PeerInformation(null, trackerIp, trackerPort);			
		}
		else {
			Debug.print("...Creating first Node", debug);			
		}
		
		final PeerClient myClient = new PeerClient(myId, port, tracker);
		
		new Thread("ConnectionHandler") {

			@Override
			public void run() {
				myClient.peerNode.connectionHandler();
			}
			
		}.start();
		
		myClient.console();
	}
	*/
	
}
