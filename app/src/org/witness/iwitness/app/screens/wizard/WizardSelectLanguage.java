package org.witness.iwitness.app.screens.wizard;

import java.util.Locale;

import org.witness.informacam.utils.Constants.Codes;
import org.witness.iwitness.R;
import org.witness.iwitness.app.views.DropdownSpinner;
import org.witness.iwitness.app.views.DropdownSpinner.OnSelectionChangedListener;
import org.witness.iwitness.utils.Constants.WizardActivityListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class WizardSelectLanguage extends Fragment implements OnClickListener, OnSelectionChangedListener
{
	View rootView;
	Activity a;

	Spinner languageChoices;
	Button commit;

	int choice = 0;
	String langKey;

	Handler handler = new Handler();

	private String[] mCodes;
	private DropdownSpinner mDropdownLanguage;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);
		rootView = li.inflate(R.layout.fragment_wizard_select_language, null);

		mDropdownLanguage = (DropdownSpinner) rootView.findViewById(R.id.languagePopup);
		mCodes = getResources().getStringArray(R.array.locales);

		ListAdapter adapter = new LanguageListAdapter(rootView.getContext(), rootView.getContext().getResources().getStringArray(R.array.languages_));
		mDropdownLanguage.setAdapter(adapter);
		mDropdownLanguage.setOnSelectionChangedListener(this);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());

		
		// What is currently set?
		//
		Locale locale = a.getBaseContext().getResources().getConfiguration().locale;
		String currentLanguage = locale.getLanguage();

		// What is prefered language?
		//
		String preferedLanguage = sp.getString(Codes.Extras.LOCALE_PREF_KEY, null);
		if (preferedLanguage == null)
		{
			preferedLanguage = currentLanguage;
		}

		boolean different = !currentLanguage.equals(preferedLanguage);
		if (!selectLanguageByLanguageCode(currentLanguage, different))
			selectLanguageByLanguageCode("en", true);

		commit = (Button) rootView.findViewById(R.id.wizard_commit);
		commit.setOnClickListener(this);
		return rootView;
	}

	private boolean selectLanguageByLanguageCode(String code, boolean sendNotification)
	{
		for (int i = 0; i < mCodes.length; i++)
		{
			if (mCodes[i].equals(code))
			{
				mDropdownLanguage.setCurrentSelection(i, sendNotification);
				return true;
			}
		}		
		return false;
	}
	
	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		this.a = a;
	}

	private class LanguageListAdapter extends ArrayAdapter<String>
	{
		public LanguageListAdapter(Context context, String[] languages)
		{
			super(context, R.layout.wizard_language_item, R.id.tvLanguageName, languages);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// User super class to create the View
			View v = super.getView(position, convertView, parent);
			TextView tv = (TextView) v.findViewById(R.id.tvLanguageName);
			tv.setText(getItem(position));
			return v;
		}
	}

	@Override
	public void onClick(View v)
	{
		if (v == commit)
		{
			if (a instanceof WizardActivityListener)
			{
				((WizardActivityListener) a).onLanguageConfirmed();
			}
		}
	}

	@Override
	public void onSelectionChanged(int position)
	{
		if (a instanceof WizardActivityListener)
		{
			((WizardActivityListener) a).onLanguageSelected(mCodes[position]);
		}
	};
}
