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

import com.distributedsystems.snake.R;
import com.distributedsystems.utils.Debug;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * TileView: a View-variant designed for handling arrays of "icons" or other drawables.
 * 
 */
public class TileView extends View {

    /**
     * Parameters controlling the size of the tiles and their range within view. Width/Height are in
     * pixels, and Drawables will be scaled to fit to these dimensions. X/Y Tile Counts are the
     * number of tiles that will be drawn.
     */

    protected static int mTileSize;

    protected static int mXTileCount;
    protected static int mYTileCount;
    
    protected static int width;
    protected static int height;
    
    protected static int originalWidth;
    protected static int originalHeight;
    
    private static int mXOffset;
    private static int mYOffset;

    private static int mMyPos;
    
    private final Paint mPaint = new Paint();
    private static final boolean debug = true;
    
    /**
     * A hash that maps integer handles specified by the subclasser to the drawable that will be
     * used for that reference
     */
    private Bitmap[] mTileArray;

    /**
     * A two-dimensional array of integers in which the number represents the index of the tile that
     * should be drawn at that locations
     */
    private int[][] mTileGrid;

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        mTileSize = a.getDimensionPixelSize(R.styleable.TileView_tileSize, 12);

        a.recycle();
    }

    public TileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        mTileSize = a.getDimensionPixelSize(R.styleable.TileView_tileSize, 12);

        a.recycle();

    }

    /**
     * Resets all tiles to 0 (empty)
     * 
     */
    public void clearTiles() {
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 0; y < mYTileCount; y++) {
                setTile(0, x, y);
            }
        }
    }

    /**
     * Function to set the specified Drawable as the tile for a particular integer key.
     *
     * @param key
     * @param tile
     */
    public void loadTile(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, mTileSize, mTileSize);
        tile.draw(canvas);

        mTileArray[key] = bitmap;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int x = mMyPos; x < mXTileCount; x += 1) {
            for (int y = 0; y < mYTileCount; y += 1) {
                if (mTileGrid[x][y] > 0) {
                    canvas.drawBitmap(mTileArray[mTileGrid[x][y]], mXOffset + (x-mMyPos) * mTileSize,
                            mYOffset + y * mTileSize, mPaint);
                }
            }
        }

    }

    /**
     * Rests the internal array of Bitmaps used for drawing tiles, and sets the maximum index of
     * tiles to be inserted
     *
     * @param tilecount
     */

    public void resetTiles(int tilecount) {
        mTileArray = new Bitmap[tilecount];
    }

    /**
     * Used to indicate that a particular tile (set with loadTile and referenced by an integer)
     * should be drawn at the given x/y coordinates during the next invalidate/draw cycle.
     * 
     * @param tileindex
     * @param x
     * @param y
     */
    public void setTile(int tileindex, int x, int y) {
    	//Debug.print("Tile: (" + x + ", " + y + ") Grid Size: (" + mXTileCount +  ", " + mYTileCount + ")", debug);
    	if (x<mXTileCount && y<mYTileCount) {
    		mTileGrid[x][y] = tileindex;
    	}
    }

    public int getMyWidth() {
		return width;
	}

	public int getMyHeight() {
		return height;
	}

	public void resetView(int width, int height, int typeOfGame, int myPos) {
    	
		if (1 == typeOfGame || 0 == typeOfGame){
	        mMyPos = 0;
	        
	    	if (TileView.width > width) {
	    		TileView.width = width;
	    	}
	    	if (TileView.height > height) {
	    		TileView.height = height;
	    	}
	    	Debug.print("New Width: " + TileView.width, debug);
	    	Debug.print("New Height: " + TileView.height, debug);
	    	
	        mXTileCount = (int) Math.floor(TileView.width / mTileSize);
	        mYTileCount = (int) Math.floor(TileView.height / mTileSize);

	        mXOffset = ((TileView.originalWidth - (mTileSize * mXTileCount)) / 2);
	        mYOffset = ((TileView.originalHeight - (mTileSize * mYTileCount)) / 2);


	        
	        /*TODO: What would happen if snake or apples are out of grid*/
	        mTileGrid = new int[mXTileCount][mYTileCount];
	        clearTiles();
	        
		} else {

			mMyPos = (int) Math.floor(width / mTileSize);
	    	
			
	    	if (TileView.height > height) {
	    		TileView.height = height;
	    	}
	    	
	    	Debug.print("New Width: " + TileView.width, debug);
	    	Debug.print("New Height: " + TileView.height, debug);
	    	
	        mXTileCount = (int) Math.floor(TileView.width / mTileSize);
	        mYTileCount = (int) Math.floor(TileView.height / mTileSize);

	        mXOffset = ((TileView.originalWidth - (mTileSize * mXTileCount)) / 2);
	        mYOffset = ((TileView.originalHeight - (mTileSize * mYTileCount)) / 2);

	        TileView.width += width;
	        
	        mXTileCount = (int) Math.floor(TileView.width / mTileSize);
	        mYTileCount = (int) Math.floor(TileView.height / mTileSize);
	        
	        mTileGrid = new int[mXTileCount+1][mYTileCount];
	        clearTiles();
	        
		}
    	

    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	
    	width = w;
    	height = h;
    	
    	originalWidth = w;
    	originalHeight = h;
    	
        mXTileCount = (int) Math.floor(w / mTileSize);
        mYTileCount = (int) Math.floor(h / mTileSize);

        mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
        mYOffset = ((h - (mTileSize * mYTileCount)) / 2);

        mTileGrid = new int[mXTileCount][mYTileCount];
        clearTiles();
    }

}
