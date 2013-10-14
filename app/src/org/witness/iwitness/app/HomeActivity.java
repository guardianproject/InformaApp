package org.witness.iwitness.app;

import info.guardianproject.odkparser.widgets.ODKSeekBar.OnMediaRecorderStopListener;
import info.guardianproject.onionkit.ui.OrbotHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.j3m.IDCIMDescriptor.IDCIMSerializable;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.CameraFragment;
import org.witness.iwitness.app.screens.GalleryFragment;
import org.witness.iwitness.app.screens.HomeFragment;
import org.witness.iwitness.app.screens.UserManagementFragment;
import org.witness.iwitness.app.screens.menus.MediaActionMenu;
import org.witness.iwitness.app.screens.popups.PopupClickListener;
import org.witness.iwitness.app.screens.popups.SharePopup;
import org.witness.iwitness.app.screens.popups.TextareaPopup;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.Codes.Routes;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.Preferences;
import org.witness.iwitness.utils.actions.ContextMenuAction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

@SuppressLint("HandlerLeak")
public class HomeActivity extends SherlockFragmentActivity implements HomeActivityListener, InformaCamStatusListener, InformaCamEventListener,
		ListAdapterListener, OnMediaRecorderStopListener
{
	Intent init, route;

	private final static String LOG = Constants.App.Home.LOG;

	private static final int INDEX_MAIN = 0;
	private static final int INDEX_GALLERY = 2;

	private String lastLocale = null;

	List<Fragment> fragments = new Vector<Fragment>();
	HomeFragment mainFragment;
	Fragment userManagementFragment, galleryFragment, cameraFragment;

	boolean initGallery = false;

	int visibility = View.VISIBLE;

	ViewPager viewPager;
	TabPager pager;

	InformaCam informaCam;

	MediaActionMenu mam;
	// WaitPopup waiter;

	Intent toEditor, toCamera;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		informaCam = (InformaCam)getApplication();		
		
		setContentView(R.layout.activity_home);

		try
		{
			Iterator<String> i = savedInstanceState.keySet().iterator();
			while (i.hasNext())
			{
				String outState = i.next();
				if (outState.equals(Home.TAG) && savedInstanceState.getBoolean(Home.TAG))
				{
					initGallery = true;
				}
			}
		}
		catch (NullPointerException e)
		{
		}

		toEditor = new Intent(this, EditorActivity.class);
		toCamera = new Intent(this, CameraActivity.class);
		route = null;

		mainFragment = (HomeFragment) Fragment.instantiate(this, HomeFragment.class.getName());
		userManagementFragment = Fragment.instantiate(this, UserManagementFragment.class.getName());
		galleryFragment = Fragment.instantiate(this, GalleryFragment.class.getName());
		cameraFragment = Fragment.instantiate(this, CameraFragment.class.getName());

		fragments.add(mainFragment);
		fragments.add(userManagementFragment);
		fragments.add(galleryFragment);
		fragments.add(cameraFragment);

		init = getIntent();

		initLayout();
		checkForUpdates();
		launchMain();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		informaCam.setStatusListener(this);
		informaCam.setEventListener(this);
		informaCam.setListAdapterListener(this);
		
		if(getIntent().hasExtra(Constants.Codes.Extras.CHANGE_LOCALE)) {
			getIntent().removeExtra(Constants.Codes.Extras.CHANGE_LOCALE);
		}
		if (getIntent().hasExtra(Constants.Codes.Extras.GENERATING_KEY)) {
			mainFragment.setIsGeneratingKey(getIntent().getBooleanExtra(Constants.Codes.Extras.GENERATING_KEY, false));
			getIntent().removeExtra(Constants.Codes.Extras.GENERATING_KEY);
		} else {
			mainFragment.setIsGeneratingKey(false);
		}

		String currentLocale = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.Keys.LANGUAGE, "0");

		if (lastLocale != null && !lastLocale.equals(currentLocale))
		{
			setNewLocale(currentLocale);
			return;
		}

		checkForCrashes();

		informaCam = (InformaCam) getApplication();

		if (init.getData() != null)
		{
			final Uri ictdURI = init.getData();

			mHandlerUI.post(new Runnable() {
				@Override
				public void run() {
					IOrganization organization = informaCam.installICTD(ictdURI, mHandlerUI, HomeActivity.this);
					if(organization != null) {
						viewPager.setCurrentItem(0);
					}
					else
					{
						Toast.makeText(HomeActivity.this, getString(org.witness.informacam.R.string.could_not_import_ictd), Toast.LENGTH_LONG).show();
					}
				}
			});

		}
	}

	private void setNewLocale(String locale_code)
	{
		Configuration configuration = new Configuration();
		configuration.locale = new Locale(informaCam.languageMap.getCode(Integer.parseInt(locale_code)));

		getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

		getIntent().putExtra(Constants.Codes.Extras.CHANGE_LOCALE, true);
		setResult(Activity.RESULT_OK, new Intent().putExtra(Constants.Codes.Extras.CHANGE_LOCALE, true));
		finish();
	}

	private void initLayout()
	{
		pager = new TabPager(getSupportFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.view_pager_root);
		viewPager.setAdapter(pager);
		viewPager.setOnPageChangeListener(pager);

		launchMain();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(Home.TAG, true);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int[] getDimensions()
	{
		Display display = getWindowManager().getDefaultDisplay();
		return new int[] { display.getWidth(), display.getHeight() };
	}

	@Override
	public void launchCamera() {
		resetActionBar();
		toCamera.removeExtra(org.witness.informacam.utils.Constants.Codes.Extras.CAMERA_TYPE);
		//toCamera.putExtra(
		//		 org.witness.informacam.utils.Constants.Codes.Extras.CAMERA_TYPE,
		//		 org.witness.informacam.utils.Constants.App.Camera.Type.CAMERA);
		route = toCamera;
		routeUs();
	}

	@Override
	public void launchEditor(IMedia media)
	{
		toEditor.putExtra(Codes.Extras.EDIT_MEDIA, media._id);

		route = toEditor;
		routeUs();
	}

	@Override
	public void getContextualMenuFor(final INotification notification)
	{
		List<ContextMenuAction> actions = new Vector<ContextMenuAction>();

		ContextMenuAction action = new ContextMenuAction();
		action.label = getResources().getString(R.string.delete);
		action.ocl = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mam.cancel();
				informaCam.notificationsManifest.getById(notification._id).delete();

				((ListAdapterListener) userManagementFragment).updateAdapter(Codes.Adapters.NOTIFICATIONS);
			}
		};
		actions.add(action);

		if (notification.canRetry)
		{
			action = new ContextMenuAction();
			action.label = getResources().getString(R.string.retry);
			action.ocl = new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mam.cancel();
					notification.retry();
				}
			};
			actions.add(action);
		}

		mam = new MediaActionMenu(this, actions);
		mam.Show();
	}

	@Override
	public void getContextualMenuFor(final IOrganization organization)
	{
		List<ContextMenuAction> actions = new Vector<ContextMenuAction>();

		ContextMenuAction action = new ContextMenuAction();
		action.label = getResources().getString(R.string.send_message);
		action.ocl = new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mam.cancel();
				new TextareaPopup(HomeActivity.this, organization)
				{
					@Override
					public void cancel()
					{
						// TODO: send a message...

						super.cancel();
					}
				};
			}
		};
		actions.add(action);

		action = new ContextMenuAction();
		action.label = getResources().getString(R.string.resend_credentials);
		action.ocl = new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mam.cancel();
				informaCam.resendCredentials(organization);
				Toast.makeText(HomeActivity.this, getResources().getString(R.string.you_have_resent_your_credentials_to_x, organization.organizationName),
						Toast.LENGTH_LONG).show();
			}
		};
		actions.add(action);

		mam = new MediaActionMenu(this, actions);
		mam.Show();
	}

	@Override
	public void getContextualMenuFor(final IMedia media, View anchorView)
	{
		try
		{
			if (anchorView == null)
				return; // Need an anchor view
			
			LayoutInflater inflater = LayoutInflater.from(this);

			ViewGroup anchorRoot = null;
			anchorRoot = (ViewGroup) anchorView.getRootView();
			
			View content = inflater.inflate(R.layout.popup_media_context_menu, anchorRoot, false);
			content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			PopupWindow mMenuPopup = new PopupWindow(content, content.getMeasuredWidth(), content.getMeasuredHeight(), true);

			// Delete
			//
			View btnDelete = content.findViewById(R.id.btnDeleteMedia);
			btnDelete.setOnClickListener(new PopupClickListener(mMenuPopup)
			{
				@Override
				protected void onSelected()
				{
					if ((informaCam.mediaManifest.getById(media._id)).delete())
					{
						((GalleryFragment) galleryFragment).updateAdapter(0);
					}
				}
			});

			// Share
			//
			View btnShare = content.findViewById(R.id.btnShareMedia);
			btnShare.setOnClickListener(new PopupClickListener(mMenuPopup)
			{
				@Override
				protected void onSelected()
				{
					new SharePopup(HomeActivity.this, informaCam.mediaManifest.getById(media._id), true);
				}
			});	
			
			mMenuPopup.setOutsideTouchable(true);
			mMenuPopup.setBackgroundDrawable(new BitmapDrawable());
			mMenuPopup.showAsDropDown(anchorView, anchorView.getWidth(), -anchorView.getHeight());
				
			mMenuPopup.getContentView().setOnClickListener(new PopupClickListener(mMenuPopup));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed()
	{
		if (viewPager.getCurrentItem() != INDEX_MAIN)
		{
			viewPager.setCurrentItem(INDEX_MAIN);
		}
		else
		{
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}

	@Override
	public void logoutUser()
	{
		getIntent().putExtra(Codes.Extras.LOGOUT_USER, true);
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data) {
		informaCam.setStatusListener(this);
		informaCam.setEventListener(this);
		informaCam.setListAdapterListener(this);
		
		if(responseCode == Activity.RESULT_OK) {
			switch(requestCode) {

			case Codes.Routes.CAMERA:
				viewPager.setCurrentItem(INDEX_GALLERY);

				informaCam.mediaManifest.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
				/*
				 * XXX: Other developers, take note:
				 * 
				 * the returned media can be JSONified. It represents the media
				 * has been captured, but that may or may not have been
				 * processed.
				 * 
				 * InformaCam will send a message whenever a new media item has
				 * been processed. The message contains the
				 * Codes.Extras.Messages.DCIM.ADD code, which is handled by
				 * "onUpdate()"
				 */
				IDCIMSerializable returnedMedia = (IDCIMSerializable) data.getSerializableExtra(Codes.Extras.RETURNED_MEDIA);
				Logger.d(LOG, "new dcim:\n" + returnedMedia.asJson().toString());
				
				if(!returnedMedia.dcimList.isEmpty()) {
					setPending(returnedMedia.dcimList.size(), 0);
				}

				informaCam.stopInforma();
				route = null;
				break;
			case Codes.Routes.LOGOUT:
				logoutUser();
				break;
			case Codes.Routes.EDITOR:
				informaCam.stopInforma();
				route = null;
				break;
			case Codes.Routes.WIPE:
				logoutUser();
				break;
			}
		}
	}

	class TabPager extends FragmentStatePagerAdapter implements OnPageChangeListener
	{

		public TabPager(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public void onPageScrollStateChanged(int state)
		{
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		@Override
		public void onPageSelected(int page)
		{
			// tabHost.setCurrentTab(page);
			if (page == 3)
			{
				launchCamera();
			} else {
				//updateAdapter(0);
			}
			supportInvalidateOptionsMenu();
		}

		@Override
		public Fragment getItem(int which)
		{
			return fragments.get(which);
		}

		@Override
		public int getCount()
		{
			return fragments.size();
		}

	}

	@Override
	public void onInformaCamStart(Intent intent)
	{
	}

	@Override
	public void onInformaCamStop(Intent intent)
	{
	}

	@Override
	public void onInformaStop(Intent intent)
	{
		route = null;
	}

	@Override
	public void onInformaStart(Intent intent)
	{
		// waiter.cancel();
		doRouteUs();
	}

	private void routeUs()
	{
		if (informaCam.informaService != null)
			doRouteUs();
		else
			informaCam.startInforma();
	}
	
	private void doRouteUs()
	{
		if (route != null)
		{
			if (route.equals(toEditor))
			{
				startActivityForResult(toEditor, Routes.EDITOR);
			}
			else if (route.equals(toCamera))
			{
				startActivityForResult(toCamera, Routes.CAMERA);
			}
		}
	}

	@Override
	public void waiter(boolean show)
	{
		/*
		 * if(show) { waiter = new WaitPopup(this); } else { if(waiter != null)
		 * { waiter.cancel(); } }
		 */
	}

	@Override
	public void updateData(INotification notification, Message message)
	{
	}

	@Override
	public void updateData(IOrganization organization, Message message)
	{
	}

	private final static String HOCKEY_APP_ID = "819d2172183272c9d84cd3a4dbd9296b";

	private void checkForCrashes()
	{
		CrashManager.register(this, HOCKEY_APP_ID);
	}

	private void checkForUpdates()
	{
		// XXX: Remove this for store builds!
		UpdateManager.register(this, HOCKEY_APP_ID);
	}

	@Override
	public void updateAdapter(int which)
	{
		if (informaCam.getCredentialManagerStatus() == org.witness.informacam.utils.Constants.Codes.Status.UNLOCKED)
		{
			Fragment f = fragments.get(viewPager.getCurrentItem());

			if (f instanceof ListAdapterListener)
			{
				((ListAdapterListener) f).updateAdapter(which);
			}
		}
	}

	@Override
	public void setPending(int numPending, int numCompleted)
	{
		if (informaCam.getCredentialManagerStatus() == org.witness.informacam.utils.Constants.Codes.Status.UNLOCKED)
		{
			Fragment f = fragments.get(viewPager.getCurrentItem());

			if (f instanceof ListAdapterListener)
			{
				((ListAdapterListener) f).setPending(numPending, numCompleted);
			}
		}

	}

	@Override
	public void setLocale(String newLocale)
	{
		lastLocale = newLocale;
	}

	@Override
	public String getLocale()
	{
		return lastLocale;
	}

	@Override
	public void onUpdate(final Message message)
	{
		int code = message.getData().getInt(Codes.Extras.MESSAGE_CODE);

		switch (code)
		{
		case org.witness.informacam.utils.Constants.Codes.Messages.DCIM.ADD:
			final Bundle data = message.getData();

			
			mHandlerUI.sendEmptyMessage(0);
			mHandlerUI.post(new Runnable()
			{
				@Override
				public void run()
				{
					setPending(data.getInt(Codes.Extras.NUM_PROCESSING), data.getInt(Codes.Extras.NUM_COMPLETED));
				}
			});

			break;
		case org.witness.informacam.utils.Constants.Codes.Messages.Transport.GENERAL_FAILURE:
			mHandlerUI.post(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(HomeActivity.this, message.getData().getString(Codes.Extras.GENERAL_FAILURE), Toast.LENGTH_LONG).show();
				}
			});
			break;

		case org.witness.informacam.utils.Constants.Codes.Messages.UI.REPLACE:
			mainFragment.setIsGeneratingKey(false);
			break;

		case org.witness.informacam.utils.Constants.Codes.Messages.Transport.ORBOT_UNINSTALLED:
			mHandlerUI.post(new Runnable() {
				@Override
				public void run() {
					OrbotHelper oh = new OrbotHelper(HomeActivity.this);
					oh.promptToInstall(HomeActivity.this);
				}
			});
			break;
		case org.witness.informacam.utils.Constants.Codes.Messages.Transport.ORBOT_NOT_RUNNING:
			mHandlerUI.post(new Runnable() {
				@Override
				public void run() {
					OrbotHelper oh = new OrbotHelper(HomeActivity.this);
					oh.requestOrbotStart(HomeActivity.this);
				}
			});
			break;

		}
	}

	private final Handler mHandlerUI = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{

			super.handleMessage(msg);

			updateAdapter(msg.what);
		}

	};

	@Override
	public void launchGallery()
	{
		informaCam.stopInforma();
		viewPager.setCurrentItem(INDEX_GALLERY);
	}

	@Override
	public void launchVideo()
	{
		resetActionBar();
		toCamera.putExtra(
				org.witness.informacam.utils.Constants.Codes.Extras.CAMERA_TYPE,
				org.witness.informacam.utils.Constants.App.Camera.Type.CAMCORDER);
		route = toCamera;
		routeUs();
	}

	@Override
	public void launchMain()
	{
		viewPager.setCurrentItem(INDEX_MAIN);
		resetActionBar();
		routeUs();
	}

	private void resetActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.app_name);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setLogo(this.getResources().getDrawable(R.drawable.ic_action_up));
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public void onMediaRecorderStop()
	{
	}
}
