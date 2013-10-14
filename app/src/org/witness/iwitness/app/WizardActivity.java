package org.witness.iwitness.app;

import java.util.Locale;

import org.json.JSONException;
import org.witness.informacam.InformaCam;
import org.witness.informacam.crypto.KeyUtility;
import org.witness.informacam.ui.SurfaceGrabberActivity;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Models.IUser;
import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.wizard.WizardCreateDB;
import org.witness.iwitness.app.screens.wizard.WizardSelectLanguage;
import org.witness.iwitness.app.screens.wizard.WizardTakePhoto;
import org.witness.iwitness.utils.Constants.WizardActivityListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class WizardActivity extends SherlockFragmentActivity implements WizardActivityListener
{
	private InformaCam informaCam;
	
	public WizardActivity()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		informaCam =  InformaCam.getInstance();
		
		setContentView(R.layout.activity_wizard);

		Fragment step1 = Fragment.instantiate(this, WizardSelectLanguage.class.getName());

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.wizard_holder, step1);
		//ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onBackPressed()
	{
		if (getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
			this.setResult(RESULT_CANCELED);
			finish();
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	public void onLanguageSelected(String languageCode)
	{
		SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();
		sp.putString(Codes.Extras.LOCALE_PREF_KEY, languageCode).commit();

		Configuration configuration = new Configuration();
		configuration.locale = new Locale(languageCode);

		getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

		setResult(Activity.RESULT_FIRST_USER, new Intent().putExtra(Codes.Extras.CHANGE_LOCALE, true));
		finish();
	}

	@Override
	public void onLanguageConfirmed()
	{
		Fragment step2 = Fragment.instantiate(this, WizardCreateDB.class.getName());

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		ft.replace(R.id.wizard_holder, step2);
		ft.addToBackStack(null);
		ft.setTransitionStyle(2);
		ft.commit();
	}

	@Override
	public void onUsernameCreated(String username, String email, String password)
	{
		try {
			informaCam.user.put(IUser.ALIAS, username);
			informaCam.user.put(IUser.EMAIL, email);
			informaCam.user.put(IUser.PASSWORD, password);
			
			Fragment step3 = Fragment.instantiate(this, WizardTakePhoto.class.getName());

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			ft.replace(R.id.wizard_holder, step3);
			ft.addToBackStack(null);
			ft.commit();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTakePhotoClicked()
	{
		Intent surfaceGrabberIntent = new Intent(this, SurfaceGrabberActivity.class);
		startActivityForResult(surfaceGrabberIntent, Codes.Routes.IMAGE_CAPTURE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Codes.Routes.IMAGE_CAPTURE:
				// init the key...
//				Toast.makeText(this, getString(R.string.wizard_key_is_being_made), Toast.LENGTH_LONG).show();
//
////				toImageCapture.setClickable(false);
////				((WizardActivity) a).autoAdvance();
////				
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						if(KeyUtility.initDevice()) {
//							Bundle data = new Bundle();
//							data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.UI.REPLACE);
//							
//							Message message = new Message();
//							message.setData(data);
//							
//							Toast.makeText(WizardActivity.this, getString(R.string.done), Toast.LENGTH_LONG).show();
//							//((InformaCamEventListener) this).onUpdate(message);
//						}
//					}
//				}).start();
				setResult(RESULT_OK);
				finish();
				break;
			}
		}
	}
}
