package org.witness.informacam.app;

import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.Constants.Preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
	ListPreference lockScreenMode, language, originalImage, panicAction;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.preferences);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setLogo(this.getResources().getDrawable(R.drawable.ic_action_up));
		actionBar.setDisplayUseLogoEnabled(true);
		
		addPreferencesFromResource(R.xml.preferences);
		
//		lockScreenMode = (ListPreference) findPreference(Preferences.Keys.LOCK_SCREEN_MODE);
//		updateSummaryWithChoice(lockScreenMode, lockScreenMode.getValue(), getResources().getStringArray(R.array.lockScreenOptions_));

		language = (ListPreference) findPreference(Preferences.Keys.LANGUAGE);
		updateSummaryWithChoice(language, language.getValue(), getResources().getStringArray(R.array.languages_));
		
		originalImage = (ListPreference) findPreference(Preferences.Keys.ORIGINAL_IMAGE_HANDLING);
		updateSummaryWithChoice(originalImage, originalImage.getValue(), getResources().getStringArray(R.array.originalImageOptions_));

		panicAction = (ListPreference) findPreference(Preferences.Keys.PANIC_ACTION);
		updateSummaryWithChoice(panicAction, panicAction.getValue(), getResources().getStringArray(R.array.panicActionOptions_));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//		if (key.equals(Preferences.Keys.LOCK_SCREEN_MODE)) {
//			updateSummaryWithChoice(lockScreenMode, sharedPreferences.getString(key, "1"), getResources().getStringArray(R.array.lockScreenOptions_));
//		} else
		if(key.equals(Preferences.Keys.LANGUAGE)) {
			updateSummaryWithChoice(language, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.languages_));
		} else if(key.equals(Preferences.Keys.ORIGINAL_IMAGE_HANDLING)) {
			updateSummaryWithChoice(originalImage, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.originalImageOptions_));
		} else if(key.equals(Preferences.Keys.PANIC_ACTION)) {
			updateSummaryWithChoice(panicAction, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.panicActionOptions_));
		}
	}
	
	private void updateSummaryWithChoice(Preference pref, String choiceValue, String[] choices) {
		pref.setSummary(choices[Integer.parseInt(choiceValue)]);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
		{
			finish();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}
}
