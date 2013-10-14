package org.witness.iwitness.app.screens.popups;

import android.view.View;
import android.widget.PopupWindow;

public class PopupClickListener implements View.OnClickListener
{
	private final PopupWindow mParent;

	public PopupClickListener(PopupWindow parent)
	{
		mParent = parent;
	}

	@Override
	public void onClick(View v)
	{
		onSelected();
		mParent.dismiss();
	}

	/**
	 * Override this to handle item selection. After this call the popup
	 * window will automatically be closed.
	 */
	protected void onSelected()
	{
	}
}