package com.distributedsystems.snake;

import java.util.ArrayList;
import java.util.List;

public class Layout {

	public List<Coordinate> snake;
	public List<Coordinate> apples;
	
    public long mScore;
    public long mMoveDelay;
    public int mNextDirection;
    
    public Layout() {
    	snake = new ArrayList<Coordinate>();
    	apples = new ArrayList<Coordinate>();
    	mScore = -1;
    	mMoveDelay = -1;
    	mNextDirection = -1;
    }
}
