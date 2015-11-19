package com.hci.geotagger.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class GravityBookFontTextView extends TextView {

	public GravityBookFontTextView(Context context) {
		super(context);
		setFont();
	}
	public GravityBookFontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont();
	}
	public GravityBookFontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFont();
	}

	private void setFont() {
		if (this.isInEditMode())
			return;
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"Gravity-Book.ttf");
		setTypeface(font);
	}
}