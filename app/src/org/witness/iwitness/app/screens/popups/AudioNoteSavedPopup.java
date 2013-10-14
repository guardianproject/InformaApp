package org.witness.iwitness.app.screens.popups;

import org.witness.iwitness.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AudioNoteSavedPopup extends Popup
{
	Button commit;

	public AudioNoteSavedPopup(Activity a, final Object context)
	{
		super(a, R.layout.popup_audio_saved);
		this.context = context;

		commit = (Button) layout.findViewById(R.id.btnOk);
		commit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				cancel();
			}
		});

		Show();
	}
}
