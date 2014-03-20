package com.distributedsystems.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import com.distributedsystems.snake.SocketInterface;
import com.distributedsystems.utils.Debug;

public class PeerSocket implements SocketInterface{

	private Socket socket;
	private boolean debug = false;
	
	public PeerSocket(Socket mySocket) {

		this.socket = mySocket;
		Thread peerSocketThread = new Thread () {

			@Override
			public void run() {
				try {
					socket.setTcpNoDelay(true);
					socket.setPerformancePreferences(1, 0, 0);
					socket.setReceiveBufferSize(256);
					socket.setSendBufferSize(256);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};
		peerSocketThread.start();
		try {
			peerSocketThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public PeerSocket(String host, int port) {
		
		final String hostInThread = host;
		final int portInThread = port;
		
		Thread peerSocketThread = new Thread() {
			@Override
			public void run() {
				InetAddress address;
				try {
					address = InetAddress.getByName(hostInThread);

					socket = new Socket(address, portInThread);
					socket.setTcpNoDelay(true);
					socket.setPerformancePreferences(1, 0, 0);
					socket.setReceiveBufferSize(256);
					socket.setSendBufferSize(256);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		};
		
		peerSocketThread.start();
		try {
			peerSocketThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	@Override
	public int read() {
		InputStream in = null;
		int size = -1;
		
		try {
			in = socket.getInputStream();
			size = in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return size;
	}

	class ReadThread extends Thread {
		public int size = -1;
		public byte[] data = new byte[256];

		@Override
		public void run() {
			InputStream in = null;
			
			try {
				in = socket.getInputStream();
				size = in.read(data);
				Debug.print("...Received bytes: " + new String(data), debug);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	 	
 	@Override
	public int read(byte[] data) {

		
		ReadThread readThread = new ReadThread();
		readThread.start();
		try {
			readThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data = readThread.data;
        return readThread.size;
	}
	
	 
	public String read() {
	
		try {
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String data = inFromServer.readLine();
			Debug.print("...Received bytes: " + data, debug);
			return data;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return null;
	}

	public void write(String data) {

		try {
			PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			Debug.print("...Sending bytes: " + new String(data), debug);
			outToServer.print(data + "\n");
			outToServer.flush();
			//out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	
	@Override
	public int read(byte[] data) {

		final byte[] dataInThread = new byte[256];
		
		Thread readThread = new Thread() {
			@Override
			public void run() {
				InputStream in = null;
				try {
					in = socket.getInputStream();
					int size = in.read(dataInThread);
					Debug.print("...Received bytes: " + size + " -- "  + new String(dataInThread), debug);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}	
		};
				
		readThread.start();
		try {
			readThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data = dataInThread;

        return data.length;
	}
	
	@Override
	public void write(byte[] data) {

		final byte[] dataInThread = data;
		
		Thread writeThread = new Thread() {
			@Override
			public void run() {
				OutputStream out = null;
				
				try {
					out = socket.getOutputStream();
					//Debug.print("...Sending bytes: " + new String(data), debug);
					out.write(dataInThread);
					out.flush();
					//out.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e){
					e.printStackTrace();
				}
			}
		};
		
		writeThread.start();
		try {
			writeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
	}

	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public byte[] read() {
		final byte[] dataInThread = new byte[256];
		
		Thread readThread = new Thread() {
			@Override
			public void run() {
				InputStream in = null;
				int size = -1;
				try {
					in = socket.getInputStream();
					size = in.read(dataInThread);
					Debug.print("...Received bytes ( " + size + "): " + new String(dataInThread), debug);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}	
		};
				
		readThread.start();
		try {
			readThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (dataInThread[0] == 0) {
			return null;
		}
		else {
			return dataInThread;
		}
	}

}
