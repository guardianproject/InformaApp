package org.witness.iwitness.app.screens.wizard;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.WizardActivityListener;
import org.witness.iwitness.utils.UIHelpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class WizardCreateDB extends Fragment implements OnClickListener
{
	View rootView;
	Activity a;
	private Button commit;
	private EditText alias, email, password, passwordAgain;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);
		rootView = li.inflate(R.layout.fragment_wizard_create_db, null);
		
		alias = (EditText) rootView.findViewById(R.id.user_name);
		alias.addTextChangedListener(readAlias);
		
		email = (EditText) rootView.findViewById(R.id.user_email);

		password = (EditText) rootView.findViewById(R.id.user_password);
		password.addTextChangedListener(readPassword);

		passwordAgain = (EditText) rootView.findViewById(R.id.user_password_again);
		passwordAgain.addTextChangedListener(readPassword);
		
		commit = (Button) rootView.findViewById(R.id.wizard_commit);
		commit.setEnabled(false);
		commit.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onClick(View v)
	{
		if (v == commit)
		{
			if (isEverythingOk())
			{
				UIHelpers.hideSoftKeyboard(a);
				if (a instanceof WizardActivityListener)
				{
					((WizardActivityListener) a).onUsernameCreated(alias.getText().toString(), email.getText().toString(), password.getText().toString());
				}
			}
		}
	}
	
	private boolean checkAlias() 
	{
		if(alias.getText().length() >= 2) {
			return true;
		}
		return false;
	}
	
	private boolean checkPasswordFormat(String password)
	{
		if(password.length() >= 10) 
			return true;
		return false;
	}
	
	private boolean checkPasswordsMatch(String p1, String p2)
	{
		return checkPasswordFormat(p1) && String.valueOf(p1).equals(p2);
	}
	
	private void updateCommitButtonText()
	{
		if (!checkPasswordFormat(password.getText().toString()))
			commit.setText(R.string.wizard_password_wrong_format);
		else if (!checkPasswordsMatch(password.getText().toString(), passwordAgain.getText().toString()))
			commit.setText(R.string.wizard_password_dont_match);
		else
			commit.setText(R.string.wizard_ok_next);
	}

	private boolean isEverythingOk()
	{
		return checkAlias() && checkPasswordFormat(password.getText().toString()) && checkPasswordsMatch(password.getText().toString(), passwordAgain.getText().toString());
	}
	
	
	private void enableDisableCommit()
	{
		commit.setEnabled(isEverythingOk());
	}
	
	TextWatcher readAlias = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			enableDisableCommit();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	};

	TextWatcher readPassword = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			enableDisableCommit();
			updateCommitButtonText();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

	};
}
