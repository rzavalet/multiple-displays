package com.distributedsystems.middleware;

public class PeerInformation {
	private String peerId;
	private String host;
	private int	port;
	
	
	public PeerInformation(String peerId, String host, int port) {
		super();
		this.peerId = peerId;
		this.host = host;
		this.port = port;
	}

	public String getPeerId() {
		return peerId;
	}
	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "I am " + this.peerId + ": (" + this.host + ", " + this.port + ")";
	}
}
