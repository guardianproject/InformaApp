package org.witness.iwitness.app.screens.popups;

import org.witness.iwitness.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TextareaPopup extends Popup implements OnClickListener
{
	protected EditText prompt;
	Button commit;
	View cancelsave;
	Button cancel;
	Button save;

	public TextareaPopup(Activity a, final Object context)
	{
		super(a, R.layout.popup_textarea);
		init();
		this.context = context;
		setShowCancelSave(false);
		Show();
	}

	public TextareaPopup(Activity a, final Object context, boolean showCancelSave)
	{
		super(a, R.layout.popup_textarea);
		init();
		this.context = context;
		setShowCancelSave(showCancelSave);
		Show();
	}

	private void setShowCancelSave(boolean showCancelSave)
	{
		if (showCancelSave)
		{
			commit.setVisibility(View.GONE);
			cancelsave.setVisibility(View.VISIBLE);
		}
		else
		{
			commit.setVisibility(View.VISIBLE);
			cancelsave.setVisibility(View.GONE);
		}
	}

	private void init()
	{
		prompt = (EditText) layout.findViewById(R.id.textarea_prompt);
		commit = (Button) layout.findViewById(R.id.textarea_commit);
		commit.setOnClickListener(this);
		cancelsave = layout.findViewById(R.id.textarea_cancel_save);
		cancel = (Button) layout.findViewById(R.id.textarea_cancel);
		cancel.setOnClickListener(this);
		save = (Button) layout.findViewById(R.id.textarea_save);
		save.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v == commit && prompt.getText().length() > 0)
		{
			cancel();
		}
		else if (v == cancel)
		{
			cancel();
		}
		else if (v == save)
		{
			onSave();
			cancel();
		}
	}

	protected void onSave()
	{

	}
}
