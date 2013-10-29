package org.witness.informacam.app.utils;

import java.util.Date;

import org.witness.informacam.app.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
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
	
	@SuppressLint("NewApi")
	public static void translateY(final View view, float fromY, float toY, long duration)
	{
		if (Build.VERSION.SDK_INT >= 12)
		{
			if (duration == 0)
				view.setTranslationY(toY);
			else
				view.animate().translationY(toY).setDuration(duration).start();
		}
		else
		{
			TranslateAnimation translate = new TranslateAnimation(0, 0, fromY, toY);
			translate.setDuration(duration);
			translate.setFillEnabled(true);
			translate.setFillBefore(true);
			translate.setFillAfter(true);
			addAnimation(view, translate);
		}
	}

	@SuppressLint("NewApi")
	public static void setTranslationY(final View view, int value)
	{
		if (Build.VERSION.SDK_INT >= 11)
		{
			view.setTranslationY(value);
		}
		else
		{
			ViewGroup.MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
			if (params != null)
			{
				ViewGroup.MarginLayoutParams originalParams = (MarginLayoutParams) view.getTag(R.id.compatibility_margin_tag_key);
				if (originalParams == null)
				{
					originalParams = new ViewGroup.MarginLayoutParams(params);
					view.setTag(R.id.compatibility_margin_tag_key, originalParams);
				}
				params.topMargin = originalParams.topMargin + value;
				params.leftMargin = originalParams.leftMargin;
				params.bottomMargin = originalParams.bottomMargin - value;
				params.rightMargin = originalParams.rightMargin;
				view.setLayoutParams(params);
			}
		}
	}
	
	@SuppressLint("NewApi")
	public static void scale(final View view, float fromScale, float toScale, long duration, final Runnable whenDone)
	{
		if (Build.VERSION.SDK_INT >= 12)
		{
			if (duration == 0)
			{
				view.setScaleX(toScale);
				view.setScaleY(toScale);
				if (whenDone != null)
					whenDone.run();
			}
			else
			{
				ViewPropertyAnimator animation = view.animate().scaleX(toScale).scaleY(toScale).setDuration(duration);
				if (whenDone != null)
				{
					animation.setListener(new AnimatorListener()
					{
						@Override
						public void onAnimationCancel(Animator animation)
						{
							whenDone.run();
						}

						@Override
						public void onAnimationEnd(Animator animation)
						{
							whenDone.run();
						}

						@Override
						public void onAnimationRepeat(Animator animation)
						{
						}

						@Override
						public void onAnimationStart(Animator animation)
						{
						}

					});
				}
				animation.start();
			}
		}
		else
		{
			ScaleAnimation scale = new ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f);
			scale.setDuration(duration);
			scale.setFillEnabled(true);
			scale.setFillBefore(true);
			scale.setFillAfter(true);

			if (whenDone != null)
			{
				scale.setAnimationListener(new AnimationListener()
				{
					@Override
					public void onAnimationEnd(Animation animation)
					{
						whenDone.run();
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
			}
			addAnimation(view, scale);
		}
	}

	public static void addAnimation(View view, Animation animation)
	{
		addAnimation(view, animation, false);
	}

	public static void addAnimation(View view, Animation animation, boolean first)
	{
		Animation previousAnimation = view.getAnimation();
		if (previousAnimation == null || previousAnimation.getClass() == animation.getClass())
		{
			view.startAnimation(animation);
			return;
		}

		if (!(previousAnimation instanceof AnimationSet))
		{
			AnimationSet newSet = new AnimationSet(false);
			newSet.addAnimation(previousAnimation);
			previousAnimation = newSet;
		}

		// Remove old of same type
		//
		AnimationSet set = (AnimationSet) previousAnimation;
		for (int i = 0; i < set.getAnimations().size(); i++)
		{
			Animation anim = set.getAnimations().get(i);
			if (anim.getClass() == animation.getClass())
			{
				set.getAnimations().remove(i);
				break;
			}
		}

		// Add this (first if it is a scale animation ,else at end)
		if (animation instanceof ScaleAnimation || first)
		{
			set.getAnimations().add(0, animation);
		}
		else
		{
			set.getAnimations().add(animation);
		}

		animation.startNow();
		view.setAnimation(set);
	}
}
