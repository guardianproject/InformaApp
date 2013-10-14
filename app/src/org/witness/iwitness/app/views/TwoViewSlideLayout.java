package org.witness.iwitness.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class TwoViewSlideLayout extends ViewGroup
{
	private float mSeparatorPosition = 0.5f;
	
	public TwoViewSlideLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public TwoViewSlideLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public TwoViewSlideLayout(Context context)
	{
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

	      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	      
    		int separatorPosition = (int) (getMeasuredHeight() * mSeparatorPosition);

	      int wspec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
	      for(int i=0; i < getChildCount(); i++)
	      {
	          View v = getChildAt(i);
	          if ( i == 0)
	        	  v.measure(wspec, MeasureSpec.makeMeasureSpec(
		                  separatorPosition, MeasureSpec.EXACTLY));
	          else
	        	  v.measure(wspec, MeasureSpec.makeMeasureSpec(
		                  getMeasuredHeight() - separatorPosition, MeasureSpec.EXACTLY));
	      }
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int separatorPosition = (int) (t + ((b - t) * mSeparatorPosition));
		
		for (int i = 0; i < this.getChildCount(); i++)
		{
			View child = this.getChildAt(i);
			
			if (i == 0)
			{
				child.layout(0, 0, r - l, separatorPosition);
			}
			else
			{
				child.layout(0, separatorPosition, r - l, b - t);
			}
		}
	}
	
	public void collapse()
	{
		mSeparatorPosition = 0;
		this.requestLayout();
	}
	
	public void expand()
	{
		mSeparatorPosition = 0.5f;
		this.requestLayout();
	}
	
}
