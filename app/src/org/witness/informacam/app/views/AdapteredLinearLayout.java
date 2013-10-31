package org.witness.informacam.app.views;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public class AdapteredLinearLayout extends LinearLayout
{
	private ListAdapter mAdapter;

	public AdapteredLinearLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public AdapteredLinearLayout(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		setOrientation(LinearLayout.VERTICAL);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	private final DataSetObserver mChangeObserver = new DataSetObserver()
	{
		@Override
		public void onChanged()
		{
			rebuildViews();
		}

		@Override
		public void onInvalidated()
		{
			AdapteredLinearLayout.this.removeAllViews();
		}

	};

	public ListAdapter getAdapter()
	{
		return mAdapter;
	}
	
	public void setAdapter(ListAdapter adapter)
	{
		if (mAdapter != null)
			mAdapter.unregisterDataSetObserver(mChangeObserver);

		mAdapter = adapter;
		if (mAdapter != null)
			mAdapter.registerDataSetObserver(mChangeObserver);

		rebuildViews();
	}

	private void rebuildViews()
	{
		removeAllViews();
		if (mAdapter != null)
		{
			for (int i = 0; i < mAdapter.getCount(); i++)
			{
				View child = mAdapter.getView(i, null, this);
				addView(child);
			}
		}
	}
}
