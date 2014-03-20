package com.distributedsystems.middleware;

import java.io.IOException;

import com.distributedsystems.snake.BackgroundView;
import com.distributedsystems.snake.Coordinate;
import com.distributedsystems.snake.HandlerInterface;
import com.distributedsystems.snake.Snake;
import com.distributedsystems.snake.SnakeView;
import com.distributedsystems.utils.Debug;

import android.content.Context;

public class PeerClient {

	private PeerNode peerNode;
	private SnakeView snakeView = null;
	private BackgroundView background = null;
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
	
	private class NewLeader implements HandlerInterface {
		private PeerNode peer;
		
		public NewLeader(PeerNode peer) {
			this.peer = peer;
		}
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("... Setting new leader: " + message.getMessageData(), debug);
			peer.setLeaderId(message.getMessageData());
		}
		
	}
	
	private class GetLeader implements HandlerInterface {
		private PeerNode peer;
		
		public GetLeader(PeerNode peer) {
			this.peer = peer;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			PeerMessage messageName = null;
			Debug.print("... Replying with leader name: " + peer.getLeaderId(), debug);
			
			messageName = new PeerMessage(PeerNode.REPLY, peer.getLeaderId());
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
	
	private class SendSnake implements HandlerInterface {
		private SnakeView snake;
		
		public SendSnake(SnakeView snake) {
			this.snake = snake;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			PeerMessage messageList = null;
			Debug.print("Sending snake", debug);
			
			messageList = new PeerMessage(PeerNode.REPLY, String.valueOf(snake.getmSnakeTrail().size()));
			
			try {
				connection.sendData(messageList);
				for (Coordinate currentCoordinate : snake.getmSnakeTrail()) {
					messageList = new PeerMessage(PeerNode.REPLY, 
							currentCoordinate.x + " " + currentCoordinate.y);
					connection.sendData(messageList);
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private class SendApples implements HandlerInterface {
		private SnakeView snake;
		
		public SendApples(SnakeView snake) {
			this.snake = snake;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			PeerMessage messageList = null;
			Debug.print("Sending apples", debug);
			
			messageList = new PeerMessage(PeerNode.REPLY, String.valueOf(snake.getmAppleList().size()));
			
			try {
				connection.sendData(messageList);
				for (Coordinate currentCoordinate : snake.getmAppleList()) {
					messageList = new PeerMessage(PeerNode.REPLY, 
							currentCoordinate.x + " " + currentCoordinate.y);
					connection.sendData(messageList);
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private class SendConfig implements HandlerInterface {
		private SnakeView snake;
		private BackgroundView backgroundView;
		public SendConfig(SnakeView snake, BackgroundView mBackgroundView) {
			this.snake = snake;
			this.backgroundView = mBackgroundView;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			PeerMessage messageList = null;
			Debug.print("Sending configuration", debug);
			
			final String[] size = message.getMessageData().split(" ");

			
			backgroundView.post(new Runnable(){
			    public void run(){
			    	backgroundView.resetView(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
			    	backgroundView.invalidate();
			    }
			});
			
			snake.post(new Runnable(){
			    public void run(){
			    	snake.resetView(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
			    }
			});
			
			messageList = new PeerMessage(PeerNode.REPLY, snake.getmMoveDelay() + " " + snake.getmNextDirection() + " " + snake.getmScore()
					+ " " + snake.getMyWidth() + " " + snake.getMyHeight());
			
			try {
				connection.sendData(messageList);		
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
	
	private class BlockGame implements HandlerInterface {
		SnakeView snakeView;
		
		public BlockGame(SnakeView snakeView) {
			this.snakeView = snakeView;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** BLOCKING GAME", debug);
			if (snakeView.isStarted() == true) {
				snakeView.post(new Runnable(){
				    public void run(){
				    	snakeView.blockSnake();
				    }
				});
			}
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Peer Blocked");
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class UnblockGame implements HandlerInterface {
		SnakeView snakeView;
		
		public UnblockGame(SnakeView snakeView) {
			this.snakeView = snakeView;
		}
		
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** UNBLOCKING GAME",debug);
			if (snakeView.isStarted() == true) {
				snakeView.post(new Runnable(){
				    public void run(){
				    	snakeView.unblockSnake();
				    }
				});
			}
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Peer Unblocked");
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
			Debug.print("*** STARTING GAME", debug);
		}
	}
	
	private class EndGame implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** END GAME", debug);
		}
	}
	
	private class MoveRight implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** MOVING RIGHT", debug);
			if (snakeView.isStarted() == true) {
				snakeView.moveSnake(Snake.MOVE_RIGHT, true);
			}
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Moved right");
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class MoveLeft implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** MOVING LEFT", debug);
			if (snakeView.isStarted() == true) {
				snakeView.moveSnake(Snake.MOVE_LEFT, true);
			}
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Moved left");
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class MoveUp implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** MOVING UP", debug);
	    	if (snakeView.isStarted() == true) {
	    		snakeView.moveSnake(Snake.MOVE_UP, true);;
	    	}			
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Moved up");
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class MoveDown implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** MOVING DOWN", debug);
			if (snakeView.isStarted() == true) {
				snakeView.moveSnake(Snake.MOVE_DOWN, true);
			}
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Moved down");
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class Advance implements HandlerInterface {
		@Override
		public void handleMessage(PeerConnection connection, PeerMessage message) {
			Debug.print("*** ADVANCING", debug);
			if (snakeView.isStarted() == true) {
				if (snakeView.isStarted() == true) {
					snakeView.post(new Runnable(){
					    public void run(){
					    	snakeView.advance();
					    }
					});
				}
				
			}
			PeerMessage replyMessage = new PeerMessage(PeerNode.REPLY, "Advanced");
			try {
				connection.sendData(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**** HERE IS THE PEER CLIENT CLASS *****/
	public PeerClient(String id, int port, PeerInformation trackerPeerInformation, SnakeView mSnakeView, BackgroundView mBackgroundView, Context appContext) {

		peerNode = new PeerNode(id, port, trackerPeerInformation, appContext);
		snakeView = mSnakeView;
		background = mBackgroundView;
		
		shutdown = false;
		
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
		
		HandlerInterface getLeader = new GetLeader(peerNode);
		peerNode.addHandler(PeerNode.GET_LEADER, getLeader);		
		
		HandlerInterface sendSnake = new SendSnake(snakeView);
		peerNode.addHandler(PeerNode.GET_SNAKE, sendSnake);	
		
		HandlerInterface sendApples = new SendApples(snakeView);
		peerNode.addHandler(PeerNode.GET_APPLES, sendApples);	
		
		HandlerInterface sendConfig = new SendConfig(snakeView, background);
		peerNode.addHandler(PeerNode.GET_CONFIG, sendConfig);	
		
		HandlerInterface blockGame = new BlockGame(snakeView);
		peerNode.addHandler(PeerNode.BLOCK, blockGame);	
		
		HandlerInterface unblockGame = new UnblockGame(snakeView);
		peerNode.addHandler(PeerNode.UNBLOCK, unblockGame);	
		
		HandlerInterface newLeader = new NewLeader(peerNode);
		peerNode.addHandler(PeerNode.NEW_LEADER, newLeader);	
		
		HandlerInterface advance = new Advance();
		peerNode.addHandler(PeerNode.ADVANCE, advance);	
	}
	
	public boolean isShutdown() {
		return shutdown;
	}

	public boolean amILeader() {
		return peerNode.getLeaderId().equals(peerNode.getPeerId());
	}
	
	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
		this.peerNode.setShutdown(shutdown);
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
	
	public void advance() {
		if (amILeader() == false) {
			return;
		}
		
		Debug.print("Advancing...", debug);
		//peerNode.broadcastMessage(PeerNode.BLOCK, "",true);
		peerNode.broadcastMessage(PeerNode.ADVANCE, "",true);
		//peerNode.broadcastMessage(PeerNode.UNBLOCK, "",true);
	}
	
	public void moveRight() {
		if (peerNode.getLeaderId().equals(peerNode.getPeerId()) == false) {
			return;
		}
		
		Debug.print("Moving to the Right...", debug);
		//peerNode.broadcastMessage(PeerNode.BLOCK, "", true);
		peerNode.broadcastMessage(PeerNode.MOVE_RIGHT, "", true);
		//peerNode.broadcastMessage(PeerNode.UNBLOCK, "", true);
	}

	public void moveUp() {
		if (peerNode.getLeaderId().equals(peerNode.getPeerId()) == false) {
			return;
		}
		
		Debug.print("Moving Up...", debug);
		//peerNode.broadcastMessage(PeerNode.BLOCK, "", true);
		peerNode.broadcastMessage(PeerNode.MOVE_UP, "", true);
		//peerNode.broadcastMessage(PeerNode.UNBLOCK, "", true);
	}

	public void moveDown() {
		if (peerNode.getLeaderId().equals(peerNode.getPeerId()) == false) {
			return;
		}
		
		Debug.print("Moving Down...", debug);
		//peerNode.broadcastMessage(PeerNode.BLOCK, "", true);
		peerNode.broadcastMessage(PeerNode.MOVE_DOWN, "", true);
		//peerNode.broadcastMessage(PeerNode.UNBLOCK, "", true);
	}

	public void moveLeft() {
		if (peerNode.getLeaderId().equals(peerNode.getPeerId()) == false) {
			return;
		}
		
		Debug.print("Moving to the Left...", debug);
		//peerNode.broadcastMessage(PeerNode.BLOCK, "",true);
		peerNode.broadcastMessage(PeerNode.MOVE_LEFT, "",true);
		//peerNode.broadcastMessage(PeerNode.UNBLOCK, "",true);
	}
	public void endGame() {
		Debug.print("Finishing game...", debug);
		peerNode.broadcastMessage(PeerNode.END_GAME, "", true);
	}

	public void startGame() {
		Debug.print("Starting new game...", debug);
		peerNode.broadcastMessage(PeerNode.START_GAME, "", true);
	}
	
	public void setLeader() {
		if (peerNode.getLeaderId().equals(peerNode.getPeerId()) == true) {
			return;
		}
		
		Debug.print("Sending new leader...", debug);
		peerNode.broadcastMessage(PeerNode.NEW_LEADER, peerNode.getPeerId(), true);
	}
	
	public PeerNode getPeerNode() {
		return peerNode;
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
