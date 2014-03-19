package com.distributedsystems.utils;

import android.util.Log;

public class Debug {

	public static void print(String message, boolean debug) {
		if (debug == true){
			Log.i("DEBUG: ", message);
		}
	}
}
