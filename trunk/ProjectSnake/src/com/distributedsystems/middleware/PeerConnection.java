package com.distributedsystems.middleware;

import java.io.IOException;
import java.net.Socket;

import com.distributedsystems.snake.SocketInterface;
import com.distributedsystems.utils.Debug;

public class PeerConnection {

	private PeerInformation peerInformation;
	private SocketInterface socket;
	private final boolean debug = false;
	
	public PeerConnection(PeerInformation peerInformation) {
		//Set the peer information
		this.peerInformation = peerInformation;
		
		//Set the socket
		PeerSocketFactory socketFactory = PeerSocketFactory.getSocketFactory();
		this.socket = socketFactory.makeSocket(this.peerInformation.getHost(), this.peerInformation.getPort());
	}

	public PeerConnection(PeerInformation peerInformation, Socket socket) {
		this.peerInformation = peerInformation;
		
		//Set the socket
		this.socket = PeerSocketFactory.getSocketFactory().makeSocket(socket);
	}

	public PeerInformation getPeerInformation() {
		return peerInformation;
	}
	
	public PeerMessage receiveData() throws IOException {
		byte[] marshalledMessage = null;
		PeerMessage message = null;
		
		Debug.print("Receiving data.... ", debug);
		//Read the marshalled message
		marshalledMessage = socket.read();
		
		if (marshalledMessage == null || marshalledMessage.length < 4) {
			return null;
		}
		
		//Unmarshall
		byte[] type = new byte[4];
		byte[] data = new byte[252];
		System.arraycopy(marshalledMessage, 0, type, 0, 4);
		System.arraycopy(marshalledMessage, 4, data, 0, Math.min(252, marshalledMessage.length));
		
		//byte[] type = Arrays.copyOfRange(marshalledMessage, 0, 4);
		//byte[] data = Arrays.copyOfRange(marshalledMessage, 4, marshalledMessage.length);
		
		//Convert to object
		message = new PeerMessage(type, data);
		Debug.print("... Received: <" + message.getMessageType() +
				" - " + message.getMessageData() + ">", debug);

		
		return message;
	}
	
	/*
	public PeerMessage receiveData() {
		String marshalledMessage = null;
		PeerMessage message = null;
		int numBytesRead;
		
		Debug.print("Receiving data.... ", debug);
		marshalledMessage = socket.read();
		
		if (marshalledMessage == null) {
			return null;
		}
		
		Debug.print("... Received: <" + marshalledMessage + ">", debug);
		numBytesRead = marshalledMessage.length();
		
		if (numBytesRead >= 4){ 
			String type = marshalledMessage.substring(0, 4);
			String data = marshalledMessage.substring(5);
			
			//Convert to object
			message = new PeerMessage(type, data);

		}
		
		return message;
	}
	*/
	
	public void sendData(PeerMessage message) throws IOException {
		byte[] marshalledMessage = new byte[256];
		byte[] originalMessage;
		
		Debug.print("Sending data: <" + message.getMessageType() + " - " + message.getMessageData() + ">", debug);
		//Marshall the message
		originalMessage = message.toBytes();
		System.arraycopy(originalMessage, 0, marshalledMessage, 0, originalMessage.length);
		//Send the message
		socket.write(marshalledMessage);
	}

	/*
	public void sendData(PeerMessage message) {
		String marshalledMessage = null;
		
		Debug.print("Sending data: <" + message.getMessageType() + " - " + message.getMessageData() + ">", debug);
		//Marshall the message
		marshalledMessage = message.getMessageType() + " " + message.getMessageData();
		
		//Send the message
		socket.write(marshalledMessage);
	}
	*/
	
	public void close() {
		Debug.print("... Closing socket ", debug);
		socket.close();
	}
	
	@Override
	public String toString() {
		return this.peerInformation.toString();
	}

}
