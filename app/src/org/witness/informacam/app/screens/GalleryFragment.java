package org.witness.informacam.app.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.witness.informacam.InformaCam;
import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.Constants.App.Home;
import org.witness.informacam.app.utils.Constants.HomeActivityListener;
import org.witness.informacam.app.utils.Constants.Preferences;
import org.witness.informacam.app.utils.adapters.GalleryFilterAdapter;
import org.witness.informacam.app.utils.adapters.GalleryGridAdapter;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Models;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class GalleryFragment extends Fragment implements
		OnItemClickListener, OnItemLongClickListener, ListAdapterListener,
		OnItemSelectedListener {
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
	private int mCurrentFiltering;
	private MenuItem mMenuItemBatchOperations;
	private View mEncodingMedia;
	
	@SuppressWarnings("unused")
	private boolean isDataInitialized;
	
	private ProgressBar progressWait;
	private int mNumLoading;
	
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
		noMedia.setVisibility(View.GONE);
		
		progressWait = (ProgressBar) rootView.findViewById(R.id.progressWait);
		progressWait.setVisibility(View.VISIBLE);
		
		mEncodingMedia = rootView.findViewById(R.id.media_encoding);
		mEncodingMedia.setVisibility(View.GONE);
		mEncodingMedia.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mEncodingMedia.setVisibility(View.GONE);
			}
		});
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

	public void initData() {
		mCurrentFiltering = 0; //All items
		updateData();
	}
	
	private void getMediaList()
	{
		int sorting = Models.IMediaManifest.Sort.DATE_DESC;
		if (mCurrentFiltering == 1) // Photos
			sorting = Models.IMediaManifest.Sort.TYPE_PHOTO;
		else if (mCurrentFiltering == 2)
			sorting = Models.IMediaManifest.Sort.TYPE_VIDEO;
		
		listMedia = informaCam.mediaManifest.sortBy(sorting);
		if (listMedia != null)
			listMedia = new ArrayList<IMedia>(listMedia);
		
		if (mCurrentFiltering == 3) // Tagged items
		{
			for (int i = listMedia.size() - 1; i >= 0; i--)
			{
				IMedia m = listMedia.get(i);
				boolean hasTags = (m.getInnerLevelRegions().size() > 0);
				if (!hasTags)
					listMedia.remove(i);
			}
		}
		else if (mCurrentFiltering == 4) // Shared items
		{
			List<INotification> listNotifications = InformaCam.getInstance().notificationsManifest.sortBy(Models.INotificationManifest.Sort.DATE_DESC);

			for (int i = listMedia.size() - 1; i >= 0; i--)
			{
				boolean hasBeenShared = false;
				
				IMedia m = listMedia.get(i);
				for (INotification n : listNotifications)
				{
					if (m._id.equals(n.mediaId))
					{
						if (n.type == Models.INotification.Type.SHARED_MEDIA &&
							n.taskComplete)
						{
							hasBeenShared = true;
							break;
						}
					}
				}
				if (!hasBeenShared)
					listMedia.remove(i);
			}			
		}
	}
	
	private void onMediaListAvailable()
	{
		progressWait.setVisibility(View.GONE);
		galleryGridAdapter = new GalleryGridAdapter(a, listMedia);
		galleryGridAdapter.setNumLoading(mNumLoading);
		galleryGridAdapter.setInSelectionMode(isInMultiSelectMode);
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
	
	private void updateData()
	{
		Thread getMediaListThread = new Thread(new Runnable()
		{
			@Override
			public void run() {
				getMediaList();
				a.runOnUiThread(new Runnable()
				{
					@Override
					public void run() {
						onMediaListAvailable();
					}
				});
			}
		});
		getMediaListThread.start();
	}

	public void toggleMultiSelectMode(boolean mode) {
		isInMultiSelectMode = mode;
		if (galleryGridAdapter != null)
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
		
		if (galleryGridAdapter != null)
			galleryGridAdapter.update(listMedia);
		// galleryListAdapter.update(listMedia);

	}

	private void initLayout(Bundle savedInstanceState) {
		mediaDisplayGrid.removeAllViewsInLayout();
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

		if (listMedia != null && galleryGridAdapter != null)
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
			updateData();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_home_gallery, menu);

		MenuItem menuFilter = menu.findItem(R.id.menu_filter);
		Spinner spinner = (Spinner) menuFilter.getActionView();
		GalleryFilterAdapter spinnerAdapter = new GalleryFilterAdapter(spinner.getContext(), this.getResources().getTextArray(R.array.filter_options));
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(this);
		
		mMenuItemBatchOperations = menu.findItem(R.id.menu_select);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.home_gallery_title);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setLogo(this.getResources().getDrawable(
				R.drawable.ic_action_up));
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			((HomeActivityListener) a).launchMain();
			return true;
		case R.id.menu_select: {
			mActionMode = getActivity().startActionMode(
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
			//menu.add(Menu.NONE, R.string.menu_share, 0, R.string.menu_share)
				//	.setIcon(R.drawable.ic_gallery_share);
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
						mEncodingMedia.setVisibility(View.VISIBLE);
					}
				}
				else if (mNumLoading == 0)
				{
					mEncodingMedia.setVisibility(View.GONE);
				}
				
			}
		});
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition,
			long itemId) {
		mCurrentFiltering = itemPosition;
		updateAdapter(0);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	
}
