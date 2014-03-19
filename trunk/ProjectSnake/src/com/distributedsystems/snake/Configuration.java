package com.distributedsystems.snake;

import com.distributedsystems.middleware.PeerInformation;
import com.distributedsystems.snake.R;
import com.distributedsystems.utils.Debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Configuration extends Activity {
	
	private static final int PORT = 8080;
	private EditText textPeerID;
	private EditText textIP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		textPeerID = (EditText)findViewById(R.id.edit_settings_user);
		textIP = (EditText)findViewById(R.id.edit_settings_IP);
	}

	public void setPreferences(View view) {
		
		if (textPeerID.getText().toString().equals("")) {
			Debug.print("Please set the requiered parameters", true);
			//TODO: Show alert dialog
			return;
		}
		
		Debug.print("PeerID: " + textPeerID.getText().toString() 
				+ " - TRACKER IP: " + textIP.getText().toString(), true);

		final SnakeApplication context = (SnakeApplication)getApplication();
		
		// Set the tracker information if the IP is provided
		PeerInformation tracker = null;
		
		if (!textIP.getText().toString().equals("")) {
			tracker = new PeerInformation(null, textIP.getText().toString(), PORT);
		}
		
		context.setMyId(textPeerID.getText().toString());
		context.setMyPort(PORT);
		context.setTracker(tracker);
		
	    Intent intent = new Intent(this, Snake.class);
	    startActivity(intent);
	}	
}
