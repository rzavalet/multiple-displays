package com.distributedsystems.utils;

public class Debug {

	public static void print(String message, boolean debug) {
		if (debug == true){
			System.out.println("DEBUG: " + message);
		}
	}
}
