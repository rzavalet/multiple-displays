package com.distributedsystems.snake;

import java.io.IOException;

public interface SocketInterface {

	//public int read();
	//public String read();
	public int read(byte[] data) throws IOException;
	//public void write(String data);
	public byte[] read() throws IOException;
	
	public void write(byte[] data) throws IOException;
	public void close();
	
}