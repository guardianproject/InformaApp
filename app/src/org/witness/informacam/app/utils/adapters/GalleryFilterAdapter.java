package org.witness.informacam.app.utils.adapters;

import org.witness.informacam.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GalleryFilterAdapter extends BaseAdapter {
	private Context mContext;
	private CharSequence[] mObjects;

	public GalleryFilterAdapter(Context context, CharSequence[] objects) {
		super();
		mContext = context;
		mObjects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null)
			view = LayoutInflater.from(mContext).inflate(
					R.layout.actionbar_filter_dropdown, parent, false);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null)
			view = LayoutInflater.from(mContext).inflate(
					android.R.layout.simple_spinner_dropdown_item, parent, false);

		((TextView) view).setText((CharSequence) getItem(position));
		return view;
	}

	@Override
	public int getCount() {
		return mObjects.length;
	}

	@Override
	public Object getItem(int position) {
		return mObjects[position];
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}
}
