package com.distributedsystems.snake;

import com.distributedsystems.middleware.PeerInformation;
import com.distributedsystems.snake.R;
import com.distributedsystems.utils.Debug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class Configuration extends Activity {
	
	private static final int PORT = 8080;
	private EditText textPeerID;
	private EditText textIP;
	private RadioButton duplicate;
	private RadioButton extend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		textPeerID = (EditText)findViewById(R.id.edit_settings_user);
		textIP = (EditText)findViewById(R.id.edit_settings_IP);
		duplicate = (RadioButton)findViewById(R.id.duplicate);
		extend = (RadioButton)findViewById(R.id.extend);
	}

	public void setPreferences(View view) {
		
		if (textPeerID.getText().toString().equals("")) {
			Debug.print("Please set the requiered parameters", true);
			AlertDialog alertDialog = new AlertDialog.Builder(
                    Configuration.this).create();

		    // Setting Dialog Title
		    alertDialog.setTitle("Error");
		
		    // Setting Dialog Message
		    alertDialog.setMessage("Please introduce the User");
		
		    // Setting OK Button
		    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            	
		            }
		    });
		
		    // Showing Alert Message
		    alertDialog.show();
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
		
		if (tracker != null) {
			context.setTypeOfGame(1);
		}
		if (duplicate.isChecked()) {
			context.setTypeOfGame(1);
		}
		else if (extend.isChecked()) {
			context.setTypeOfGame(2);
		}
		
	    Intent intent = new Intent(this, Snake.class);
	    startActivity(intent);
	}	
}
