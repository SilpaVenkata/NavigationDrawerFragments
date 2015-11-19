package com.hci.geotagger.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class defines the compass and the canvas that it is drawn on.
 * 
 * Code modified from:
 * http://sunil-android.blogspot.com/2013/02/create-our-android-compass.html
 */
public class Compass extends View 
{
	private float direction;

	/*
	 * Constructor
	 */
	public Compass(Context context) 
	{
		super(context);
	}
	
	/*
	 * Constructor
	 */
	public Compass(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
  
	/*
	 * Constructor
	 */
	public Compass(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
  
	/**
	 * Sets the size of the compass
	 * @param widthMeasureSpec desired width of compass
	 * @param heightMeasureSpec desired height of compass
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
				MeasureSpec.getSize(heightMeasureSpec));
	}
  
	/**
	 * Draws the compass
	 * @param canvas Canvas to draw compass on
	 */
	@Override
	protected void onDraw(Canvas canvas) 
	{
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		int r = 0;
		if(w > h)
		{
			r = h/2;
		}
		else
		{
			r = w/2;
		}
    
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		paint.setColor(Color.WHITE);
    
		canvas.drawCircle(w/2, h/2, r, paint);
    
		paint.setColor(Color.RED);
		canvas.drawLine(w/2, h/2, (float)(w/2 + r * Math.sin(-direction)),
				(float)(h/2 - r * Math.cos(-direction)), paint); 
	}
   
	/**
	 * Updates the direction of the compass
	 * @param dir The current direction the compass should be pointing.
	 */
	public void update(float dir)
	{
		direction = dir;
		invalidate();
	}  
}
