package com.distributedsystems.middleware;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.distributedsystems.middleware.PeerConnection;
import com.distributedsystems.middleware.PeerInformation;
import com.distributedsystems.middleware.PeerMessage;
import com.distributedsystems.snake.Coordinate;
import com.distributedsystems.snake.HandlerInterface;
import com.distributedsystems.snake.Layout;
import com.distributedsystems.utils.Debug;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class PeerNode {

	public static final String GET_PEER_NAME = "NAME";
	public static final String REPLY = "REPL";
	public static final String ADD_PEER = "ADDP";
	public static final String GET_PEER_LIST = "GETL";
	public static final String END_GAME = "ENDG";
	public static final String START_GAME = "STAG";
	public static final String MOVE_RIGHT = "MOVR";
	public static final String MOVE_LEFT = "MOVL";
	public static final String MOVE_UP = "MOVU";
	public static final String MOVE_DOWN = "MOVD";
	public static final String HELLO = "HELL";
	public static final String GET_LEADER = "LEAD";
	public static final String GET_SNAKE = "SNAK";
	public static final String GET_APPLES = "APPL";
	public static final String GET_CONFIG = "CONF";
	public static final String BLOCK = "BLOC";
	public static final String UNBLOCK = "UNBL";
	public static final String NEW_LEADER = "NEWL";
	
	private PeerInformation myPeerInformation;
	private HashMap<String, PeerInformation> fingerTable;
	private HashMap<String, HandlerInterface> handlers;
	
	private String leaderId = null;
	private boolean shutdown = false;
	private static final boolean debug = true;
	
	private Context myContext = null;
	
	public PeerNode() {
		Debug.print("Creating Peer Node", debug);
				
		//Initialize handlers
		this.handlers = new HashMap<String, HandlerInterface>();
		
		//Get peers		
		Debug.print("...Creating Finger Table", debug);
		this.fingerTable = new HashMap<String, PeerInformation>();
		//buildFingerTable(trackerPeerInformation, 5);
	}
	
	public PeerNode(String id, int port, PeerInformation trackerPeerInformation, Context appContext) {
		
		final String myId = id;
		final int myPort = port;
		
		Debug.print("Creating Peer Node", debug);
		
		myContext = appContext;
		
		String hostIP = wifiIpAddress();
		Debug.print("I am: " + myId + "( " + hostIP + ", " + myPort + " )", debug);
	    myPeerInformation = new PeerInformation(myId, hostIP, myPort);
		
		//Initialize handlers
		this.handlers = new HashMap<String, HandlerInterface>();
		
		//Get peers		
		Debug.print("...Creating Finger Table", debug);
		this.fingerTable = new HashMap<String, PeerInformation>();		
		buildFingerTable(trackerPeerInformation, 5);
		
		//At the beginning we assume our tracker is the leader
		this.leaderId = trackerPeerInformation.getPeerId();
	}
	
	private void buildFingerTable(PeerInformation trackerPeerInformation, int ttl){
		List<PeerMessage> messages = null;
		PeerMessage message = null;
		
		if (trackerPeerInformation == null){
			return;
		}
		
		//First obtain the peer's name
		//Debug.print("...Obtaining tracker information", debug);
		messages = null;
		messages = connectAndSend(trackerPeerInformation, GET_PEER_NAME, "", true);
		if (messages == null) {
			return;
		}
		
		if (messages.size() <= 0) {
			return;
		}
		
		message = messages.get(0);
		if (!message.getMessageType().equals(REPLY)) {
			return;
		}
		
		Debug.print("...Adding peer in remote peer", debug);
		
		String remotePeerId = message.getMessageData();
		if (fingerTable.containsKey(remotePeerId) == true) {
			Debug.print("...Peer already in table " + remotePeerId, debug);
			return;
		}
		else {
			Debug.print("...Peer not in table " + remotePeerId, debug);
		}
		
		//Now ask the peer to add me
		messages = null;
		messages = connectAndSend(trackerPeerInformation, ADD_PEER, 
				getPeerId() + " " + getHost() + " " + getPort() + " ", true);
		if (messages == null) {
			return;
		}
		
		if (messages.size() <= 0) {
			return;
		}
		
		message = messages.get(0);
		if (!message.getMessageType().equals(REPLY)) {
			return;
		}
		
		Debug.print("...Adding remote peer in local peer", debug);
		Debug.print("...... PeerID: " + remotePeerId, debug);
		//Add the remote peer to my finger table
		trackerPeerInformation.setPeerId(remotePeerId);
		fingerTable.put(trackerPeerInformation.getPeerId(), trackerPeerInformation);
		
		//Ask the peer for other peers
		Debug.print("...Getting remote peer list", debug);
		messages = null;
		messages = connectAndSend(trackerPeerInformation, GET_PEER_LIST, "", true);
		if (messages == null) {
			return;
		}
		
		if (messages.size() <= 1) {
			return;
		}
		
		message = messages.remove(0);
		
		for (PeerMessage currentMessage : messages) {
			String[] fields = currentMessage.getMessageData().split(" ");
			PeerInformation currentRemoteHost = new PeerInformation(fields[0], 
					fields[1], Integer.parseInt(fields[2]));
			if (getPeerId().equals(currentRemoteHost.getPeerId()) == false) {
				buildFingerTable(currentRemoteHost, ttl-1);
			}
		}
		
		Debug.print("*** FINISHED BUILDING TABLE ***", debug);
	}
	

	public Layout askForLayout(String leaderPeerId) {
		Layout layout = new Layout();
		
		List<PeerMessage> messages = null;
		PeerMessage message = null;
		PeerInformation leaderPeer = getPeer(leaderPeerId);
		
		if (leaderPeer == null){
			return null;
		}
		
		messages = null;
		//Block the leader
		messages = connectAndSend(leaderPeer, BLOCK, "", false);
		
		//Obtain the snake coordinates
		messages = connectAndSend(leaderPeer, GET_SNAKE, "", true);
		if (messages == null) {
			return null;
		}
		
		if (messages.size() <= 1) {
			return null;
		}
		
		message = messages.remove(0);
		
		for (PeerMessage currentMessage : messages) {
			String[] fields = currentMessage.getMessageData().split(" ");
			Coordinate coordinate = new Coordinate(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
			
			layout.snake.add(coordinate);
		}
		
		//Now obtain the apples coordinates
		messages = null;
		messages = connectAndSend(leaderPeer, GET_APPLES, "", true);
		if (messages == null) {
			return null;
		}
		
		if (messages.size() <= 1) {
			return null;
		}
		
		message = messages.remove(0);
		
		for (PeerMessage currentMessage : messages) {
			String[] fields = currentMessage.getMessageData().split(" ");
			Coordinate coordinate = new Coordinate(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
			
			layout.apples.add(coordinate);
		}
		
		//Now obtain the apples coordinates
		messages = null;
		messages = connectAndSend(leaderPeer, GET_CONFIG, "", true);
		if (messages == null) {
			return null;
		}
		
		if (messages.size() <= 0) {
			return null;
		}
		
		message = messages.get(0);
		if (!message.getMessageType().equals(REPLY)) {
			return null;
		}
		
		
		String[] fields = message.getMessageData().split(" ");
		if (fields.length < 3) {
			return null;
		}
		
		layout.mMoveDelay = Integer.parseInt(fields[0]);
		layout.mNextDirection = Integer.parseInt(fields[1]);
		layout.mScore = Integer.parseInt(fields[2]);
		
		messages = connectAndSend(leaderPeer, UNBLOCK, "", false);
		
		Debug.print("*** FINISHED GETTING LAYOUT ***", debug);
		return layout;
	}
	
	public String askForLeader() {
		List<PeerMessage> messages = null;
		PeerMessage message = null;
		PeerInformation randomPeer = null;
		
		if (getNumberOfPeers() == 0){
			return null;
		}
		
		//First obtain the peer's name
		//Debug.print("...Obtaining tracker information", debug);
		messages = null;

		//Get a random peer
		int size = getPeerKeys().size();
		int item = new Random().nextInt(size);
		int i = 0;
		for(String peerId: getPeerKeys())
		{
		    if (i == item){
		    	randomPeer = getPeer(peerId);
		    	break;
		    }
		    i = i + 1;
		}
		
		messages = connectAndSend(randomPeer, GET_LEADER, "", true);
		if (messages == null) {
			return null;
		}
		
		if (messages.size() <= 0) {
			return null;
		}
		
		message = messages.get(0);
		if (!message.getMessageType().equals(REPLY)) {
			return null;
		}
		
		Debug.print("...Adding peer in remote peer", debug);
		
		String leaderPeerId = message.getMessageData();
		if (fingerTable.containsKey(leaderPeerId) == true) {
			Debug.print("...Leader is " + leaderPeerId, debug);
			return leaderPeerId;
		}
		else {
			Debug.print("I do not know this peer: " + leaderPeerId, debug);
			return null;
		}
	}
	
	public Context getMyContext() {
		return myContext;
	}

	public void setMyContext(Context myContext) {
		this.myContext = myContext;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}
	
	public String getPeerId() {
		return this.myPeerInformation.getPeerId();
	}
	
	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getHost() {
		return this.myPeerInformation.getHost();
	}
	
	public int getPort() {
		return this.myPeerInformation.getPort();
	}
	
	public HandlerInterface getHandler(String command) {
		return this.handlers.get(command);
	}
	
	public void addHandler(String msgType, HandlerInterface handler) {
		this.handlers.put(msgType, handler);
	}
	

	public void broadcastMessage(String messageType,  String messageData) {
		for(String remotePeerId : getPeerKeys()) {
			connectAndSend(getPeer(remotePeerId), messageType, messageData, false);
		}
	}
	
	public List<PeerMessage> connectAndSend(PeerInformation remoteHost, 
			String messageType,  String messageData, boolean waitReply) {
		
		PeerMessage reply = null;
		List<PeerMessage> messages = new ArrayList<PeerMessage>();
		
		Debug.print("...Sending message to: " + remoteHost.getHost() + ":" + remoteHost.getPort(), debug);
		
		PeerMessage message = new PeerMessage(messageType, messageData);
		try {
			PeerConnection connection = new PeerConnection(remoteHost);
			connection.sendData(message);
			if (waitReply == true) {
				Debug.print("...Receiving message from: " + remoteHost.getHost() + ":" + remoteHost.getPort(), debug);
				reply = connection.receiveData();
				while (reply != null) {
					messages.add(reply);
					reply = connection.receiveData();
				}

			}			
			connection.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return messages;
	}
	
	public PeerMessage sendToPeer(String remotePeerId, 
			String messageType,  String messageData, boolean waitReply) {
		
		List<PeerMessage> receivedMessage = null;
		PeerInformation remoteHost = null;
		
		remoteHost = fingerTable.get(remotePeerId);
		if (remoteHost == null) {
			return null;
		}
		
		receivedMessage = connectAndSend(remoteHost, messageType, messageData, waitReply);
		if (receivedMessage == null) {
			return null;
		}
		
		if (receivedMessage.isEmpty()) {
			return null;
		}
		
		return receivedMessage.get(0);
	}
	
	public PeerInformation getPeer(String peerId) {
		return fingerTable.get(peerId);
	}
	
	public Set<String> getPeerKeys() {
		return fingerTable.keySet();
	}
	
	public int getNumberOfPeers() {
		return fingerTable.size();
	}
	
	public PeerInformation getMyPeerInformation() {
		return myPeerInformation;
	}

	public void insertPeer(PeerInformation peer) {
		fingerTable.put(peer.getPeerId(), peer);
	}
	
	private String wifiIpAddress() {
	    WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
	    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

	    // Convert little-endian to big-endianif needed
	    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
	        ipAddress = Integer.reverseBytes(ipAddress);
	    }

	    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

	    String ipAddressString;
	    try {
	        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
	    } catch (UnknownHostException ex) {
	        Log.e("WIFIIP", "Unable to get host address.");
	        ipAddressString = null;
	    }

	    return ipAddressString;
	}
	
	public void keepAlive() {
		while (shutdown == false) {
			List<String> deleteList = new ArrayList<String>();
			for(String remotePeerId : getPeerKeys()) {
				PeerInformation remotePeer = getPeer(remotePeerId);
				PeerMessage message = new PeerMessage(HELLO, "");

				try{
					PeerConnection connection = new PeerConnection(remotePeer);
					connection.sendData(message);
					connection.close();
				}
				catch (IOException e){
					deleteList.add(remotePeerId);
				}
				
			}
			
			for (String peerId : deleteList) {
				if (fingerTable.containsKey(peerId)) {
					fingerTable.remove(peerId);
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void connectionHandler() {
		Debug.print("...Starting connection Handler", debug);
		//Set the socket
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(getPort());
				serverSocket.setSoTimeout(2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (serverSocket == null) {
				System.out.println("ERROR: could not create server socket...");
				return;
			}
			
			while (shutdown == false) {
				final Socket clientSocket;
				try {
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(0);
					//Start a new thread to handle this request
					new Thread() {

						@Override
						public void run() {
							PeerMessage message;
							try {
								PeerConnection peerConnection = new PeerConnection(myPeerInformation, clientSocket);
								message = peerConnection.receiveData();
								Debug.print("Processing: " + message.getMessageType(), debug);
								HandlerInterface handler = handlers.get(message.getMessageType());
								if (handler != null){
									handler.handleMessage(peerConnection, message);
								}
								peerConnection.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}.start();
				} catch (IOException e) {
					// TODO What can I do here?
					//e.printStackTrace();
					//Debug.print("...Waiting connection", debug);
				}
				
			}

	}



	
	
}
