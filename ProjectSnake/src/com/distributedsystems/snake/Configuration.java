package com.distributedsystems.snake;

import com.distributedsystems.middleware.PeerClient;
import com.distributedsystems.middleware.PeerInformation;
import com.distributedsystems.snake.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Configuration extends Activity {
	
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
			Log.e("RICARDO_DEBUG", "Please set the requiered parameters");
			//TODO: Show alert dialog
			return;
		}
		
		Log.i("RICARDO_DEBUG", "PeerID: " + textPeerID.getText().toString() 
				+ " - TRACKER IP: " + textIP.getText().toString());

		final SnakeApplication context = (SnakeApplication)getApplication();
		
		// Set the tracker information if the IP is provided
		PeerInformation tracker = null;
		
		if (!textIP.getText().toString().equals("")) {
			tracker = new PeerInformation(null, textIP.getText().toString(), 8080);
		}
		
		
		PeerClient myClient = new PeerClient(textPeerID.getText().toString(), 8080, tracker, this);
		myClient.startHandler();
		context.setPeerClient(myClient);
		
	    Intent intent = new Intent(this, Snake.class);
	    startActivity(intent);
	}
	
}
