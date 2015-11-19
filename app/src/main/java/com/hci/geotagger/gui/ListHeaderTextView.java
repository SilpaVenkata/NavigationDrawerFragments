package com.hci.geotagger.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class ListHeaderTextView extends TextView {

	public ListHeaderTextView(Context context) {
		super(context);
		setFont();
	}
	public ListHeaderTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont();
	}
	public ListHeaderTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFont();
	}

	private void setFont() {
		if (this.isInEditMode())
			return;
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"steelfish rg.ttf");
		setTypeface(font, Typeface.NORMAL);
	}
}