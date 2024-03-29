/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.distributedsystems.snake;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import com.distributedsystems.middleware.PeerClient;
import com.distributedsystems.snake.R;
import com.distributedsystems.utils.Debug;

/**
 * SnakeView: implementation of a simple game of Snake
 */
public class SnakeView extends TileView {

    private static final String TAG = "SnakeView";
    
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;
    
    /**
     * Labels for the drawables that will be loaded into the TileView class
     */
    private static final int RED_STAR = 1;
    private static final int YELLOW_STAR = 2;
    private static final int GREEN_STAR = 3;
    
    /**
     * Everyone needs a little randomness in their life
     */
    private static final Random RNG = new Random();
    
    /**
     * Current mode of application: READY to run, RUNNING, or you have already lost.
     */
    private int mMode = READY;

    /**
     * mLastMove: Tracks the absolute time when the snake last moved, and is used to determine if a
     * move should be made based on mMoveDelay.
     */
    private long mLastMove;

    /**
     * mStatusText: Text shows to the user in some run states
     */
    private TextView mStatusText;

    /**
     * mArrowsView: View which shows 4 arrows to signify 4 directions in which the snake can move
     */
    private View mArrowsView;

    /**
     * mBackgroundView: Background View which shows 4 different colored triangles pressing which
     * moves the snake
     */
    private View mBackgroundView;

    /**
     * Create a simple handler that we can use to cause animation to happen. We set ourselves as a
     * target and we can use the sleep() function to cause an update/invalidate to occur at a later
     * date.
     */
    private RefreshHandler mRedrawHandler = new RefreshHandler();
    

    /**
     * Current direction the snake is headed.
     */
    private int mDirection = NORTH;
    
    private int mNextDirection = NORTH;
    
    /**
     * mScore: Used to track the number of apples captured mMoveDelay: number of milliseconds
     * between snake movements. This will decrease as apples are captured.
     */
    private long mScore = 0;
    private long mMoveDelay = 1000;

    /**
     * mSnakeTrail: A list of Coordinates that make up the snake's body mAppleList: The secret
     * location of the juicy apples the snake craves.
     */
    private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();

    /**
     * myClient: This represents the application as a P2P client
     */
    private PeerClient myClient = null;
    private boolean started = false;
    private int typeOfGame = 0;
    
	public boolean needResync = false;
    
    private static final boolean debug = true;
    private boolean typeOfNode = false;
    
	class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
        	//Debug.print("Moving automagically", debug);
        	if (SnakeView.this.started && 
        			SnakeView.this.myClient != null &&
        			SnakeView.this.myClient.amILeader() == true &&
        			SnakeView.this.mMode == RUNNING) 
        	{
        		//Debug.print("Moving automagically", debug);
        		
        		SnakeView.this.myClient.advance();
        		
        		SnakeView.this.update();
        		if (SnakeView.this.needResync ) {
        			SnakeView.this.myClient.resync();
        			SnakeView.this.needResync = false;
        		}
        		
        		SnakeView.this.invalidate();
    
        	}
        }

        public void sleep(long delayMillis) {
        	try {
				Thread.sleep(delayMillis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };

    /**
     * Constructs a SnakeView based on inflation from XML
     * 
     * @param context
     * @param attrs
     */
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSnakeView(context);
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSnakeView(context);
    }

    public boolean isStarted() {
		return started;
	}

	public boolean isTypeOfNode() {
		return typeOfNode;
	}

	public void setTypeOfNode(boolean typeOfNode) {
		this.typeOfNode = typeOfNode;
	}

	public int getTypeOfGame() {
		return typeOfGame;
	}

	public void setTypeOfGame(int typeOfGame) {
		this.typeOfGame = typeOfGame;
	}

	public int getmMode() {
		return mMode;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public void setmNextDirection(int mNextDirection) {
		this.mNextDirection = mNextDirection;
	}
	public void setmScore(long mScore) {
		this.mScore = mScore;
	}
	public void setmMoveDelay(long mMoveDelay) {
		this.mMoveDelay = mMoveDelay;
	}

    public PeerClient getMyClient() {
 		return myClient;
 	}

 	public void setMyClient(PeerClient myClient) {
 		this.myClient = myClient;
 	}

    /**
     * Sets the Dependent views that will be used to give information (such as "Game Over" to the
     * user and also to handle touch events for making movements
     * 
     * @param newView
     */
    public void setDependentViews(TextView msgView, View arrowView, View backgroundView) {
        mStatusText = msgView;
        mArrowsView = arrowView;
        mBackgroundView = backgroundView;
    }

    /**
     * Updates the current mode of the application (RUNNING or PAUSED or the like) as well as sets
     * the visibility of textview for notification
     * 
     * @param newMode
     */
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;

        if (newMode == RUNNING && oldMode != RUNNING) {
            // hide the game instructions
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            // make the background and arrows visible as soon the snake starts moving
            mArrowsView.setVisibility(View.VISIBLE);
            mBackgroundView.setVisibility(View.VISIBLE);
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            mArrowsView.setVisibility(View.GONE);
            mBackgroundView.setVisibility(View.GONE);
            str = res.getText(R.string.mode_pause);
            myClient.stopTimer();
        }
        if (newMode == READY) {
            mArrowsView.setVisibility(View.GONE);
            mBackgroundView.setVisibility(View.GONE);

            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
            mArrowsView.setVisibility(View.GONE);
            mBackgroundView.setVisibility(View.GONE);
            str = res.getString(R.string.mode_lose, mScore);
            started = false;
            myClient.stopTimer();
            Debug.print("You loose", debug);
        }

        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    /**
     * @return the Game state as Running, Ready, Paused, Lose
     */
    public int getGameState() {
        return mMode;
    }
    
	public long getmMoveDelay() {
		return mMoveDelay;
	}

	public int getmNextDirection() {
		return mNextDirection;
	}

	public long getmScore() {
		return mScore;
	}

	public ArrayList<Coordinate> getmSnakeTrail() {
		return mSnakeTrail;
	}

	public ArrayList<Coordinate> getmAppleList() {
		return mAppleList;
	}


    /**
     * Save game state so that the user does not lose anything if the game process is killed while
     * we are in the background.
     * 
     * @return a Bundle with this view's state
     */
    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
        map.putLong("mScore", Long.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));

        return map;
    }

    /**
     * Restore game state if our process is being relaunched
     * 
     * @param icicle a Bundle containing the game state
     */
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);

        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        mScore = icicle.getLong("mScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
    }
    
    public void blockSnake() {
    	mMode = PAUSE;
    	update();
    }
    
    public void unblockSnake() {
    	mMode = RUNNING;
    	update();
    }
    
    /**
     * Handles snake movement triggers from Snake Activity and moves the snake accordingly. Ignore
     * events that would cause the snake to immediately turn back on itself.
     *
     * @param direction The desired direction of movement
     */
    public void moveSnake(int direction, boolean remote) {

    	boolean newLeader = false;
    	
    	if (mMode == RUNNING && remote == false && myClient.amILeader() == false) {
    		myClient.setLeader();
    		newLeader = true;
    	}

    	
        if (direction == Snake.MOVE_UP) {
            if (mMode == READY | mMode == LOSE) {
                /*
                 * At the beginning of the game, or the end of a previous one,
                 * we should start a new game if UP key is clicked.
                 */
            	
            	if (remote == false) {
            		initNewGame();
            	}
                setMode(RUNNING);
                update();
                started = true;
                return;
            }

            if (mMode == PAUSE) {
                /*
                 * If the game is merely paused, we should just continue where we left off.
                 */
            	
            	if (remote == false) {
            		initNewGame();
            	}
                setMode(RUNNING);
                update();
                started = true;
                return;
            }

            if (mDirection != SOUTH) {
                mNextDirection = NORTH;
            }
            
            if (remote == false) {
            	myClient.moveUp();
            	if (newLeader)
            		update();
            	myClient.resync();
            }
            return;
        }

        if (direction == Snake.MOVE_DOWN) {
            if (mDirection != NORTH) {
                mNextDirection = SOUTH;
            }
            if (remote == false) {
            	myClient.moveDown();
            	if (newLeader)
            		update();
            	myClient.resync();
            }
            return;
        }

        if (direction == Snake.MOVE_LEFT) {
            if (mDirection != EAST) {
                mNextDirection = WEST;
            }
            if (remote == false) {
            	myClient.moveLeft();
            	if (newLeader)
            		update();
            	myClient.resync();
            }
            return;
        }

        if (direction == Snake.MOVE_RIGHT) {
            if (mDirection != WEST) {
                mNextDirection = EAST;
            }
            if (remote == false) {
            	myClient.moveRight();
            	if (newLeader)
            		update();
            	myClient.resync();
            }
            return;
        }

    }


	public void advance() {
		updateNow(true);
		invalidate();
	}

    /**
     * Handles the basic update loop, checking to see if we are in the running state, determining if
     * a move should be made, updating the snake's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateSnake(true);
                updateApples();
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }

    }
    
    public void updateNow(boolean move) {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();
            clearTiles();
            updateWalls();
            updateSnake(true);
            updateApples();
            mLastMove = now;
        }
    }
    
	public void resyncWithLayout(Layout mLayout) {
        if (mMode == PAUSE) {
        	Debug.print("Resyncing game properties", debug);
            mSnakeTrail.clear();
            for (Coordinate coord : mLayout.snake){
            	mSnakeTrail.add(coord);
            }
            mLayout.snake.clear();
            
            mAppleList.clear();
            for (Coordinate coord : mLayout.apples){
            	mAppleList.add(coord);
            }
            mLayout.apples.clear();
            
            mNextDirection = mLayout.mNextDirection;
            mScore = mLayout.mScore;
            mMoveDelay = mLayout.mMoveDelay;
            width = mLayout.width;
            height = mLayout.height;
            typeOfGame = mLayout.typeOfGame;
    		//updateNow();
    		//invalidate();
       }
	}
	
	private void printGame() {
		Debug.print("Printing Snake", debug);
		for (Coordinate currentCoord : mSnakeTrail) {
			Debug.print(currentCoord.toString(), debug);
		}
		
		Debug.print("Printing Apples", debug);
		for (Coordinate currentCoord : mAppleList) {
			Debug.print(currentCoord.toString(), debug);
		}
		
		Debug.print("Direction: " + mNextDirection, debug);
		Debug.print("Delay: " + mMoveDelay, debug);
		Debug.print("Score: " + mScore, debug);
		Debug.print("Width: " + width, debug);
		Debug.print("Height: " + height, debug);
	}
	
    private void initSnakeView(Context context) {

        setFocusable(true);

        Resources r = this.getContext().getResources();

        resetTiles(4);
        loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
        loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
        loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));
    }

    private void initNewGame() {
        mSnakeTrail.clear();
        mAppleList.clear();
        
        if (myClient.getPeerNode().getNumberOfPeers() > 0) {
            /*
             * At this point we should already know some peers.
             * Steps to coordinate the game on startup is as follows:
             * 
             * 1) Ask the tracker who the leader is now
             * 2) Block the leader and ask for the current layout
             * 3) Unblock the leader
             */
        	Debug.print("Obtaining game from peer", debug);
        	String myLeader = myClient.getPeerNode().askForLeader();
        	if (myLeader != null && myLeader.equals(myClient.getPeerNode().getPeerId()) == false) {
	        	Layout myLayout = myClient.getPeerNode().askForLayout(myLeader, getMyWidth(), getMyHeight());
	        	for (Coordinate coordinate : myLayout.snake) {
	        		mSnakeTrail.add(coordinate);
	        	}
	        	for (Coordinate coordinate : myLayout.apples) {
	        		mAppleList.add(coordinate);
	        	}
	        	
	        	mNextDirection = myLayout.mNextDirection;
	        	mMoveDelay = myLayout.mMoveDelay;
	        	mScore = myLayout.mScore;
	        	typeOfGame = myLayout.typeOfGame;
	        	
	        	resetView(myLayout.width, myLayout.height, typeOfGame, typeOfNode);
        	}
        }
        else {
	        // For now we're just going to load up a short default eastbound snake
	        // that's just turned north
        	Debug.print("Creating new game", debug);
        	
	        mSnakeTrail.add(new Coordinate(7, 7));
	        mSnakeTrail.add(new Coordinate(6, 7));
	        mSnakeTrail.add(new Coordinate(5, 7));
	        mSnakeTrail.add(new Coordinate(4, 7));
	        mSnakeTrail.add(new Coordinate(3, 7));
	        mSnakeTrail.add(new Coordinate(2, 7));
	        mNextDirection = NORTH;
	
	        // Two apples to start with
	        addRandomApple();
	        addRandomApple();
	
	        mMoveDelay = 600;
	        mScore = 0;
        }
        
        printGame();
        myClient.startGame();
    }


 
    /**
     * Selects a random location within the garden that is not currently covered by the snake.
     * Currently _could_ go into an infinite loop if the snake currently fills the garden, but we'll
     * leave discovery of this prize to a truly excellent snake-player.
     */
    private void addRandomApple() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // Choose a new location for our apple
            int newX = 1 + RNG.nextInt(mXTileCount - 2);
            int newY = 1 + RNG.nextInt(mYTileCount - 2);
            newCoord = new Coordinate(newX, newY);

            // Make sure it's not already under the snake
            boolean collision = false;
            int snakelength = mSnakeTrail.size();
            for (int index = 0; index < snakelength; index++) {
                if (mSnakeTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }
            // if we're here and there's been no collision, then we have
            // a good location for an apple. Otherwise, we'll circle back
            // and try again
            found = !collision;
        }
        if (newCoord == null) {
            Log.e(TAG, "Somehow ended up with a null newCoord!");
        }
        mAppleList.add(newCoord);
    }


    /**
     * Draws some walls.
     */
    private void updateWalls() {
        for (int x = 0; x < mXTileCount; x++) {
            setTile(GREEN_STAR, x, 0);
            setTile(GREEN_STAR, x, mYTileCount - 1);
        }
        for (int y = 1; y < mYTileCount - 1; y++) {
            setTile(GREEN_STAR, 0, y);
            setTile(GREEN_STAR, mXTileCount - 1, y);
        }
    }

    /**
     * Draws some apples.
     */
    private void updateApples() {
        if (mAppleList == null || mAppleList.size() == 0)
        	return;
        
        for (Coordinate c : mAppleList) {
            setTile(YELLOW_STAR, c.x, c.y);
        }
    }

    /**
     * Figure out which way the snake is going, see if he's run into anything (the walls, himself,
     * or an apple). If he's not going to die, we then add to the front and subtract from the rear
     * in order to simulate motion. If we want to grow him, we don't subtract from the rear.
     */
    private void updateSnake(boolean move) {
        boolean growSnake = false;

        // Grab the snake by the head
        if (mSnakeTrail == null || mSnakeTrail.size() == 0)
        	return;
        
        Coordinate head = mSnakeTrail.get(0);
        
        if (true == move) {
        Coordinate newHead = new Coordinate(1, 1);

        mDirection = mNextDirection;

        switch (mDirection) {
            case EAST: {
                newHead = new Coordinate(head.x + 1, head.y);
                break;
            }
            case WEST: {
                newHead = new Coordinate(head.x - 1, head.y);
                break;
            }
            case NORTH: {
                newHead = new Coordinate(head.x, head.y - 1);
                break;
            }
            case SOUTH: {
                newHead = new Coordinate(head.x, head.y + 1);
                break;
            }
        }

        // Collision detection
        // For now we have a 1-square wall around the entire arena
        if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > mXTileCount - 2)
                || (newHead.y > mYTileCount - 2)) {
            setMode(LOSE);
            return;

        }

        // Look for collisions with itself
        int snakelength = mSnakeTrail.size();
        for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
            Coordinate c = mSnakeTrail.get(snakeindex);
            if (c.equals(newHead)) {
                setMode(LOSE);
                return;
            }
        }

        // Look for apples
        int applecount = mAppleList.size();
        for (int appleindex = 0; appleindex < applecount; appleindex++) {
            Coordinate c = mAppleList.get(appleindex);
            if (c.equals(newHead)) {
                mAppleList.remove(c);
                addRandomApple();

                mScore++;
                //mMoveDelay *= 0.9;

                growSnake = true;
                needResync = true;
            }
        }

        // push a new head onto the ArrayList and pull off the tail
        mSnakeTrail.add(0, newHead);
        // except if we want the snake to grow
        if (!growSnake) {
            mSnakeTrail.remove(mSnakeTrail.size() - 1);
        }
        }
        int index = 0;
        for (Coordinate c : mSnakeTrail) {
            if (index == 0) {
                setTile(YELLOW_STAR, c.x, c.y);
            } else {
                setTile(RED_STAR, c.x, c.y);
            }
            index++;
        }
    }
	
    /**
     * Given a ArrayList of coordinates, we need to flatten them into an array of ints before we can
     * stuff them into a map for flattening and storage.
     * 
     * @param cvec : a ArrayList of Coordinate objects
     * @return : a simple array containing the x/y values of the coordinates as
     *         [x1,y1,x2,y2,x3,y3...]
     */
    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int[] rawArray = new int[cvec.size() * 2];

        int i = 0;
        for (Coordinate c : cvec) {
            rawArray[i++] = c.x;
            rawArray[i++] = c.y;
        }

        return rawArray;
    }

    /**
     * Given a flattened array of ordinate pairs, we reconstitute them into a ArrayList of
     * Coordinate objects
     * 
     * @param rawArray : [x1,y1,x2,y2,...]
     * @return a ArrayList of Coordinates
     */
    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }


}
