package org.witness.iwitness.app.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Models;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.Preferences;
import org.witness.iwitness.utils.adapters.GalleryGridAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class GalleryFragment extends SherlockFragment implements
		OnItemClickListener, OnItemLongClickListener, ListAdapterListener,
		OnNavigationListener {
	View rootView;
	GridView mediaDisplayGrid;
	GalleryGridAdapter galleryGridAdapter;

	RelativeLayout noMedia;

	Activity a = null;

	boolean isInMultiSelectMode;
	List<IMedia> batch = null;
	List<IMedia> listMedia = null;
	Handler h = new Handler();

	private static final String LOG = Home.LOG;
	private final InformaCam informaCam = InformaCam.getInstance();
	private ActionMode mActionMode;
	private int mCurrentSorting;
	private MenuItem mMenuItemBatchOperations;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_gallery, null);
		mediaDisplayGrid = (GridView) rootView
				.findViewById(R.id.media_display_grid);
		noMedia = (RelativeLayout) rootView
				.findViewById(R.id.media_display_no_media);

		return rootView;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initLayout(savedInstanceState);	
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateAdapters();
	}

	private void initData() {

		mCurrentSorting = Models.IMediaManifest.Sort.DATE_DESC;
		updateData();
	}
	
	private void updateData()
	{
		listMedia = informaCam.mediaManifest.sortBy(mCurrentSorting);
		if (listMedia != null)
			listMedia = new ArrayList<IMedia>(listMedia);

		galleryGridAdapter = new GalleryGridAdapter(a, listMedia);
		if (mediaDisplayGrid != null) {
			mediaDisplayGrid.setAdapter(galleryGridAdapter);
			mediaDisplayGrid.setOnItemLongClickListener(this);
			mediaDisplayGrid.setOnItemClickListener(this);
		}

		if((listMedia != null && listMedia.size() > 0) || this.mNumLoading > 0) {
			if (noMedia != null)
				noMedia.setVisibility(View.GONE);
		} else {

			if (noMedia != null)
				noMedia.setVisibility(View.VISIBLE);
		}

		updateAdapters();			
	}

	public void toggleMultiSelectMode(boolean mode) {
		isInMultiSelectMode = mode;
		galleryGridAdapter.setInSelectionMode(isInMultiSelectMode);
		toggleMultiSelectMode();
	}

	public void toggleMultiSelectMode() {
		// multiSelect.setImageDrawable(a.getResources().getDrawable(isInMultiSelectMode
		// ? R.drawable.ic_action_selected : R.drawable.ic_action_select));
		if (isInMultiSelectMode) {
			batch = new Vector<IMedia>();
			// batchEditHolder.setVisibility(View.VISIBLE);
		} else {
			batch = null;
			// batchEditHolder.setVisibility(View.GONE);

		}

		galleryGridAdapter.update(listMedia);
		// galleryListAdapter.update(listMedia);

	}

	private void initLayout(Bundle savedInstanceState) {
		mediaDisplayGrid.removeAllViewsInLayout();
		initData();
		toggleMultiSelectMode(false);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		
		if (position < mNumLoading)
			return false; // Ignore clicks on incomplete items
		position -= mNumLoading;
		
		((HomeActivityListener) a)
				.getContextualMenuFor(listMedia.get(position), view);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position,
			long id) {
		
		if (position < mNumLoading)
			return; // Ignore clicks on incomplete items
		position -= mNumLoading;
		
		if (!isInMultiSelectMode) {
			((HomeActivityListener) a).launchEditor(listMedia.get(position));
		} else {
			try {
				IMedia m = listMedia.get(position);

				if (!m.has(Models.IMedia.TempKeys.IS_SELECTED)) {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, false);
				}

				if (m.getBoolean(Models.IMedia.TempKeys.IS_SELECTED)) {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, false);
					batch.remove(m);
				} else {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, true);
					batch.add(m);
				}

				CheckBox chkSelected = (CheckBox) view
						.findViewById(R.id.chkSelect);
				chkSelected.setChecked(m
						.getBoolean(Models.IMedia.TempKeys.IS_SELECTED));

				if (mActionMode != null)
					mActionMode.invalidate();
				// LinearLayout ll = (LinearLayout)
				// view.findViewById(R.id.gallery_thumb_holder);
				// ll.setBackgroundDrawable(getResources().getDrawable(selectedColor));
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}

	}

	private void updateAdapters() {

		if (listMedia != null)
			this.galleryGridAdapter.update(listMedia);

		if ((listMedia != null && listMedia.size() > 0) || this.mNumLoading > 0)
		{
			if (noMedia != null)
				noMedia.setVisibility(View.GONE);
			if (mMenuItemBatchOperations != null)
				mMenuItemBatchOperations.setVisible(true);
		}
		else
		{
			if (noMedia != null)
				noMedia.setVisibility(View.VISIBLE);
			if (mMenuItemBatchOperations != null)
				mMenuItemBatchOperations.setVisible(false);
		}
	}

	@Override
	public void updateAdapter(int which) {
		Log.d(LOG, "UPDATING OUR ADAPTERS");
		if (a != null) {

			listMedia = informaCam.mediaManifest.sortBy(mCurrentSorting);
			if (listMedia != null)
				listMedia = new ArrayList<IMedia>(listMedia);
			updateAdapters();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_home_gallery, menu);

		mMenuItemBatchOperations = menu.findItem(R.id.menu_select);
		
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.home_gallery_title);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setLogo(this.getResources().getDrawable(
				R.drawable.ic_action_up));
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				actionBar.getThemedContext(),
 R.array.sort_options,
				R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(list, this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			((HomeActivityListener) a).launchMain();
			return true;
		case R.id.menu_select: {
			mActionMode = getSherlockActivity().startActionMode(
					mActionModeSelect);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private final ActionMode.Callback mActionModeSelect = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			// MenuInflater inflater = mode.getMenuInflater();
			// inflater.inflate(R.menu.context_menu, menu);
			menu.add(Menu.NONE, R.string.menu_share, 0, R.string.menu_share)
					.setIcon(R.drawable.ic_gallery_share);
			menu.add(Menu.NONE, R.string.home_gallery_delete, 0,
					R.string.home_gallery_delete).setIcon(
					R.drawable.ic_gallery_trash);

			toggleMultiSelectMode(true);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			int nSelected = (batch == null) ? 0 : batch.size();
			mode.setTitle(GalleryFragment.this.getString(
					R.string.home_gallery_selected, nSelected));
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.string.menu_done:
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.string.menu_share:
				return true;
			case R.string.home_gallery_delete:
				((HomeActivityListener) a).waiter(true);
				new Thread(new Runnable() {
					private ActionMode mMode;

					@Override
					public void run() {
						for (IMedia m : batch) {
							m.delete();
						}
						h.post(new Runnable() {
							private ActionMode mMode;

							@Override
							public void run() {
								mMode.finish();
								((HomeActivityListener) a).waiter(false);
								updateData();
							}

							public Runnable init(ActionMode mode) {
								mMode = mode;
								return this;
							}
						}.init(mMode));
					}

					public Runnable init(ActionMode mode) {
						mMode = mode;
						return this;
					}
				}.init(mode)).start();
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			toggleMultiSelectMode(false);
		}
	};
	private int mNumLoading;

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		switch (itemPosition) {
		case 0:
			mCurrentSorting = Models.IMediaManifest.Sort.DATE_DESC;
			break;
		case 1:
			mCurrentSorting = Models.IMediaManifest.Sort.TYPE_PHOTO;
			break;
		case 2:
			mCurrentSorting = Models.IMediaManifest.Sort.TYPE_VIDEO;
			break;
		}
		updateAdapter(0);
		return true;
	}

	@Override
	public void setPending(final int numPending, final int numCompleted) {		
		
		if(a == null) {
			return;
		}
		
		mNumLoading = numPending - numCompleted;
		
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				if (galleryGridAdapter != null)
				{
					galleryGridAdapter.setNumLoading(numPending - numCompleted);
				}	
				
				if (numPending > 0 && numCompleted == 0)
				{
					SharedPreferences sp = a.getSharedPreferences(a.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
					int nTimesShown = sp.getInt(Preferences.Keys.HINT_PROCESSING_IMAGES_SHOWN, 0);
					if (nTimesShown < 3)
					{
						nTimesShown++;
						sp.edit().putInt(Preferences.Keys.HINT_PROCESSING_IMAGES_SHOWN, nTimesShown).commit();
						Toast.makeText(a, getString(R.string.safely_storing_your_picture), Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}

}
