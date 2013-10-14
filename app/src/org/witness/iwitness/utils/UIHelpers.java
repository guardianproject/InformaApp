package org.witness.iwitness.utils;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;

public class UIHelpers
{
	public static int dpToPx(int dp, Context ctx)
	{
		Resources r = ctx.getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}
	
	public static void showSoftKeyboard(Activity activity, View view)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	public static void hideSoftKeyboard(Activity activity)
	{
		UIHelpers.hideSoftKeyboard(activity, null);
	}

	public static void hideSoftKeyboard(Activity activity, View view)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (view == null)
			view = activity.getCurrentFocus();
		if (view != null)
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	public static int getRelativeLeft(View myView)
	{
		if (myView.getParent() == myView.getRootView())
			return myView.getLeft();
		else
			return myView.getLeft() + UIHelpers.getRelativeLeft((View) myView.getParent());
	}

	public static int getRelativeTop(View myView)
	{
		if (myView.getParent() == myView.getRootView())
			return myView.getTop();
		else
			return myView.getTop() + UIHelpers.getRelativeTop((View) myView.getParent());
	}

	/**
	 * Get the coordinates of a view relative to another anchor view. The anchor
	 * view is assumed to be in the same view tree as this view.
	 * 
	 * @param anchorView
	 *            View relative to which we are getting the coordinates
	 * @param view
	 *            The view to get the coordinates for
	 * @return A Rect containing the view bounds
	 */
	public static Rect getRectRelativeToView(View anchorView, View view)
	{
		Rect ret = new Rect(getRelativeLeft(view) - getRelativeLeft(anchorView), getRelativeTop(view) - getRelativeTop(anchorView), 0, 0);
		ret.right = ret.left + view.getWidth();
		ret.bottom = ret.top + view.getHeight();
		return ret;
	}
	
	
	/**
	 * Fade the alpha of the view to 0.
	 * 
	 * @param view
	 *            The view to fade
	 * @param duration
	 *            Number of ms for the animation
	 */
	public static void fadeOut(final View view, int duration)
	{
		AlphaAnimation alpha = new AlphaAnimation((duration == 0) ? 0 : 1.0f, 0);
		alpha.setDuration(duration);
		alpha.setFillAfter(true);
		alpha.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation animation)
			{
				view.setVisibility(View.GONE);
				view.clearAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationStart(Animation animation)
			{
			}
		});
		view.clearAnimation();
		view.startAnimation(alpha);
	}

	public static void fadeIn(final View view, final int duration)
	{
		AlphaAnimation alpha = new AlphaAnimation((duration == 0) ? 1 : 0, 1);
		alpha.setDuration(duration);
		alpha.setFillAfter(true);
		alpha.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationStart(Animation animation)
			{
				view.setVisibility(View.VISIBLE);
			}
		});
		view.clearAnimation();
		view.startAnimation(alpha);
	}
	
	public static String dateDiffDisplayString(Date date, Context context, int idStringNever, int idStringRecently, int idStringMinutes, int idStringMinute,
			int idStringHours, int idStringHour, int idStringDays, int idStringDay)
	{
		if (date == null)
			return "";

		Date todayDate = new Date();
		double ti = todayDate.getTime() - date.getTime();
		if (ti < 0)
			ti = -ti;
		ti = ti / 1000; // Convert to seconds
		if (ti < 1)
		{
			return context.getString(idStringNever);
		}
		else if (ti < 60)
		{
			return context.getString(idStringRecently);
		}
		else if (ti < 3600 && (int) Math.round(ti / 60) < 60)
		{
			int diff = (int) Math.round(ti / 60);
			if (diff == 1)
				return context.getString(idStringMinute, diff);
			return context.getString(idStringMinutes, diff);
		}
		else if (ti < 86400 && (int) Math.round(ti / 60 / 60) < 24)
		{
			int diff = (int) Math.round(ti / 60 / 60);
			if (diff == 1)
				return context.getString(idStringHour, diff);
			return context.getString(idStringHours, diff);
		}
		else
		{
			int diff = (int) Math.round(ti / 60 / 60 / 24);
			if (diff == 1)
				return context.getString(idStringDay, diff);
			return context.getString(idStringDays, diff);
		}
	}
}
