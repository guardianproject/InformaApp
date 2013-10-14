package org.witness.iwitness.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {

	public SquareFrameLayout(Context context) {
		super(context);
	}

	public SquareFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
	 
		if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)
		{
			int inhibitedHeightSpec = MeasureSpec.makeMeasureSpec((int) (MeasureSpec.getSize(widthMeasureSpec)),
					MeasureSpec.getMode(widthMeasureSpec));
			super.onMeasure(widthMeasureSpec, inhibitedHeightSpec);
		}
		else
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			int width = this.getMeasuredWidth();
				int height = (int) (width);
				super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		}
	}
}
