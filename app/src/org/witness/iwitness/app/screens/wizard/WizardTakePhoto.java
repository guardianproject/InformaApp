package org.witness.iwitness.app.screens.wizard;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.WizardActivityListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class WizardTakePhoto extends Fragment implements OnClickListener
{
	View rootView;
	Activity a;
	private Button commit;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);
		rootView = li.inflate(R.layout.fragment_wizard_take_photo, null);
		
		commit = (Button) rootView.findViewById(R.id.wizard_commit);
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
			if (a instanceof WizardActivityListener)
			{
				((WizardActivityListener) a).onTakePhotoClicked();
			}
		}
	}
}
