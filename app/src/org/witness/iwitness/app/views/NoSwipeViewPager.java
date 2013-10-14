package org.witness.iwitness.app.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSwipeViewPager extends ViewPager {

	public NoSwipeViewPager(Context context) {
		super(context);
	}

	public NoSwipeViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	 @Override
	 public boolean onInterceptTouchEvent(MotionEvent arg0) {
		 return false;
	 }
	
	 @Override
	 public boolean onTouchEvent(MotionEvent arg0) {
		 return false;
	 }

}
