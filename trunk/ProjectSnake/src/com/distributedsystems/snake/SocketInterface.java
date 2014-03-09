package com.distributedsystems.snake;

public interface SocketInterface {

	//public int read();
	//public String read();
	public int read(byte[] data);
	//public void write(String data);
	public byte[] read();
	
	public void write(byte[] data);
	public void close();
	
}