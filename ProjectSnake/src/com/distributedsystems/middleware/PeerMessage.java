package com.distributedsystems.middleware;

public class PeerMessage {
	private byte[] type;
	private byte[] data;
	
	
	public PeerMessage(byte[] type, byte[] data) {
		this.type = type;
		this.data = data;
	}

	public PeerMessage(String type, String data) {
		this.type = type.getBytes();
		this.data = data.getBytes();
	}
	
	/*
	public static int byteArrayToInt(byte[] byteArray) {
		
	}
	
	public static byte[] intToByteArray(int integer) {
		
	}
	*/
	
	public String getMessageType() {
		String message = new String(this.type);
		return message.trim();
	}
	
	public byte[] getMessageTypeBytes() {
		return this.type;
	}
	
	public String getMessageData() {
		String data = new String(this.data);
		return data.trim();
	}
	
	public byte[] getMessageDataBytes() {
		return this.data;
	}
	
	public byte[] toBytes() {
		byte[] c = new byte[type.length + data.length];
		System.arraycopy(type, 0, c, 0, type.length);
		System.arraycopy(data, 0, c, type.length, data.length);
		
		return c;
	}
	
}
