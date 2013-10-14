package org.witness.iwitness.app.views;

import org.witness.iwitness.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class DropdownSpinner extends RelativeLayout implements OnItemClickListener
{
	public interface OnSelectionChangedListener
	{
		void onSelectionChanged(int position);
	}

	private ListAdapter mAdapter;
	private View mCurrentView;
	private PopupWindow mPopup;
	private Drawable mDropDownBackground;
	private Drawable mDivider;
	private int mCurrentSelection;
	private OnSelectionChangedListener mOnSelectionChangedListener;

	public DropdownSpinner(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs);
	}

	public DropdownSpinner(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs);
	}

	public DropdownSpinner(Context context)
	{
		super(context);
		init(null);
	}

	private void init(AttributeSet attrs)
	{
		if (attrs != null)
		{
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DropdownSpinner);
			mDropDownBackground = a.getDrawable(R.styleable.DropdownSpinner_dropdown_background);
			mDivider = a.getDrawable(R.styleable.DropdownSpinner_android_divider);
			a.recycle();
		}
		if (mDropDownBackground == null)
			mDropDownBackground = new ColorDrawable(Color.TRANSPARENT);

		this.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showPopup();
			}
		});
	}

	public void setOnSelectionChangedListener(OnSelectionChangedListener listener)
	{
		this.mOnSelectionChangedListener = listener;
	}

	public void setAdapter(ListAdapter adapter)
	{
		mAdapter = adapter;
		setCurrentSelection(0, false);
	}

	private void showPopup()
	{
		if (mAdapter != null && mAdapter.getCount() > 0)
		{
			try
			{
				ListView lv = new ListView(getContext());
				lv.setAdapter(mAdapter);
				lv.setOnItemClickListener(this);
				lv.setDivider(mDivider);
				
				Rect rectGlobal = new Rect();
				this.getGlobalVisibleRect(rectGlobal);

				Rect rectGlobalParent = new Rect();
				((View) this.getParent()).getGlobalVisibleRect(rectGlobalParent);

				int maxHeight = rectGlobalParent.bottom - rectGlobal.top;
				lv.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));

				mPopup = new PopupWindow(lv, getWidth(), lv.getMeasuredHeight(), true);
				mPopup.setOutsideTouchable(true);
				mPopup.setBackgroundDrawable(mDropDownBackground);
				mPopup.showAtLocation(this, Gravity.TOP | Gravity.LEFT, rectGlobal.left, rectGlobal.top);
				mPopup.getContentView().setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						mPopup.dismiss();
						mPopup = null;
					}
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public int getCurrentSelection()
	{
		return mCurrentSelection;
	}

	public void setCurrentSelection(int position, boolean sendNotification)
	{
		mCurrentSelection = position;
		if (mCurrentView != null)
			this.removeView(mCurrentView);
		if (mAdapter != null && position >= 0 && position < mAdapter.getCount())
			mCurrentView = mAdapter.getView(position, null, this);
		else
			mCurrentView = null;
		if (mCurrentView != null)
			this.addView(mCurrentView);
		if (sendNotification && mOnSelectionChangedListener != null)
			mOnSelectionChangedListener.onSelectionChanged(position);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
	{
		if (mPopup != null)
		{
			mPopup.dismiss();
			mPopup = null;
		}
		setCurrentSelection(position, true);
	}
}
