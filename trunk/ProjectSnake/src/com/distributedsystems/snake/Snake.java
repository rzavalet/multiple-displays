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

import com.distributedsystems.middleware.PeerClient;
import com.distributedsystems.snake.R;
import com.distributedsystems.utils.Debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a serpent roaming
 * around the garden looking for apples. Be careful, though, because when you catch one, not only
 * will you become longer, but you'll move faster. Running into yourself or the walls will end the
 * game.
 * 
 */
public class Snake extends Activity {

    /**
     * Constants for desired direction of moving the snake
     */
    public static int MOVE_LEFT = 0;
    public static int MOVE_UP = 1;
    public static int MOVE_DOWN = 2;
    public static int MOVE_RIGHT = 3;

    private static String ICICLE_KEY = "snake-view";
    private static final boolean debug = true;

    
    private SnakeView mSnakeView;
    private BackgroundView backgroundView;
    
    /**
     * Called when Activity is first created. Turns off the title bar, sets up the content views,
     * and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Debug.print("--- onCreate", debug);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.snake_layout);

        mSnakeView = (SnakeView) findViewById(R.id.snake);
        backgroundView = (BackgroundView) findViewById(R.id.background);
        
        mSnakeView.setDependentViews((TextView) findViewById(R.id.text),
                findViewById(R.id.arrowContainer), findViewById(R.id.background));

        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }
        mSnakeView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSnakeView.getGameState() == SnakeView.RUNNING) {
                    // Normalize x,y between 0 and 1
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();

                    // Direction will be [0,1,2,3] depending on quadrant
                    int direction = 0;
                    direction = (x > y) ? 1 : 0;
                    direction |= (x > 1 - y) ? 2 : 0;

                    // Direction is same as the quadrant which was clicked
                    mSnakeView.moveSnake(direction, false);

                } else {
                    // If the game is not running then on touching any part of the screen
                    // we start the game by sending MOVE_UP signal to SnakeView
                    mSnakeView.moveSnake(MOVE_UP, false);
                }
                return false;
            }
        });
        
        final SnakeApplication context = (SnakeApplication)this.getApplication();
		PeerClient myClient = new PeerClient(context.getMyId(), context.getMyPort(), context.getTracker(), mSnakeView, backgroundView, this);
		myClient.startHandler();
		
		/*This should be corrected*/
        mSnakeView.setMyClient(myClient);
        mSnakeView.setTypeOfGame(context.getTypeOfGame());
        if (context.getTracker() == null) {
        	mSnakeView.setTypeOfNode(false);
        }
        else {
        	mSnakeView.setTypeOfNode(true);
        }
        TextView myInfo = (TextView) findViewById(R.id.txt_info);
        myInfo.setText(myClient.getPeerNode().getMyPeerInformation().toString());
    }

    @Override
    protected void onPause() {
    	Debug.print("--- onPause", debug);
        
        // Pause the game along with the activity
        mSnakeView.setMode(SnakeView.PAUSE);
        
        /*We will restart the game*/
        if (mSnakeView.getMyClient() != null) {
        	mSnakeView.getMyClient().setShutdown(true);
        	mSnakeView.setMyClient(null);
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	Debug.print("--- onSaveInstanceState", debug);
        // Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

    /**
     * Handles key events in the game. Update the direction our snake is traveling based on the
     * DPAD.
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mSnakeView.moveSnake(MOVE_UP, false);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mSnakeView.moveSnake(MOVE_RIGHT, false);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSnakeView.moveSnake(MOVE_DOWN, false);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mSnakeView.moveSnake(MOVE_LEFT, false);
                break;
        }

        return super.onKeyDown(keyCode, msg);
    }

	public SnakeView getmSnakeView() {
		return mSnakeView;
	}

}
