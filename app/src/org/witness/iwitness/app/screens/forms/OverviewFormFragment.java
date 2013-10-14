package org.witness.iwitness.app.screens.forms;

import info.guardianproject.odkparser.Constants.RecorderState;
import info.guardianproject.odkparser.FormWrapper.ODKFormListener;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.popups.PopupClickListener;
import org.witness.iwitness.app.views.AudioNoteInfoView;
import org.witness.iwitness.utils.AudioNoteHelper;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;
import org.witness.iwitness.utils.Constants.EditorActivityListener;
import org.witness.iwitness.utils.UIHelpers;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class OverviewFormFragment extends Fragment implements ODKFormListener, OnClickListener, OnLongClickListener
{
	View rootView;
	Activity a;
	TextView timeCaptured, location;
	TextView notes;
	EditText notesAnswerHolder; // Dummy EditText to hold notes
	IForm form = null;
	private IForm textForm;
	LinearLayout llAudioFiles;
	private SeekBar sbAudio;
	private AudioNotePlayer mAudioPlayer;
	private View rlAudio;
	private boolean mIsEditable;
	
	private final static String LOG = App.LOG;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_forms_overview, container, false);

		timeCaptured = (TextView) rootView.findViewById(R.id.media_time_captured);
		location = (TextView) rootView.findViewById(R.id.media_details_location);

		notes = (TextView) rootView.findViewById(R.id.media_notes);
		notes.setText("");
		notesAnswerHolder = (EditText) rootView.findViewById(R.id.media_notes_edit); // new EditText(container.getContext());
		notesAnswerHolder.setVisibility(View.GONE);
		notesAnswerHolder.setText("");

		rlAudio = rootView.findViewById(R.id.rlAudio);
		llAudioFiles = (LinearLayout) rootView.findViewById(R.id.llAudioFiles);
		sbAudio = (SeekBar) rootView.findViewById(R.id.sbAudio);
		sbAudio.setVisibility(View.GONE);
		return rootView;
	}

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		initLayout();
	}

	public void setIsEditable(boolean isEditable)
	{
		mIsEditable = isEditable;
	}
	
	@Override
	public void onDetach()
	{
		Log.d(LOG, "SHOULD SAVE FORM STATE!");
		super.onDetach();
	}

	private void initLayout()
	{
		initData();
		initForms();
	}

	private void initData()
	{
		IMedia media = ((EditorActivityListener) a).media();

		// int displayNumMessages = 0;
		// if (((EditorActivityListener) a).media().messages != null &&
		// ((EditorActivityListener) a).media().messages.size() > 0)
		// {
		// displayNumMessages = ((EditorActivityListener)
		// a).media().messages.size();
		// }
		// messagesHolder.setText(a.getResources().getString(R.string.x_messages,
		// displayNumMessages, (displayNumMessages == 1 ? "" : "s")));

		String[] dateAndTime = TimeUtility.millisecondsToDatestampAndTimestamp(((EditorActivityListener) a).media().dcimEntry.timeCaptured);
		timeCaptured.setText(dateAndTime[0] + " " + dateAndTime[1]);

		// if (((EditorActivityListener) a).media().alias != null)
		// {
		// alias.setText(((EditorActivityListener) a).media().alias);
		// }

//		if (media.dcimEntry.exif.location != null && media.dcimEntry.exif.location != new float[] { 0.0f, 0.0f })
//		{
//			location.setText(a.getString(R.string.x_location, media.dcimEntry.exif.location[0], media.dcimEntry.exif.location[1]));
//		}
//		else
//		{
//			location.setText(a.getString(R.string.location_unknown));
//		}

		location.setText(a.getString(R.string.shared_never));
		List<INotification> listNotifications = InformaCam.getInstance().notificationsManifest.sortBy(Models.INotificationManifest.Sort.DATE_DESC);
		for (INotification n : listNotifications)
		{
			if (media._id.equals(n.mediaId))
			{
				if ((n.type == Models.INotification.Type.SHARED_MEDIA || n.type == Models.INotification.Type.EXPORTED_MEDIA) &&
					n.taskComplete)
				{
					Date date = new Date(n.timestamp);
					location.setText(UIHelpers.dateDiffDisplayString(date, a, 
							R.string.shared_never, R.string.shared_recently,
							R.string.shared_minutes, R.string.shared_minute,
							R.string.shared_hours, R.string.shared_hour,
							R.string.shared_days, R.string.shared_day));
					break;
				}
			}
		}
		
	}

	private void initForms()
	{
		Logger.d(LOG, ((EditorActivityListener) a).media().asJson().toString());

		IRegion overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if (overviewRegion == null)
		{
			overviewRegion = ((EditorActivityListener) a).media().addRegion(a, null);
		}

		for (IForm form : ((EditorActivityListener) a).media().getForms(a))
		{
			if (form.namespace.equals(Forms.FreeText.TAG))
			{
				textForm = form;
			}
		}
		if (textForm == null)
		{
			// No text form found, add one!
			for (IForm form : ((EditorActivity) a).availableForms)
			{
				if (form.namespace.equals(Forms.FreeText.TAG))
				{
					textForm = new IForm(form, a);
					textForm.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_t"
							+ System.currentTimeMillis()).getAbsolutePath();

					overviewRegion.addForm(textForm);
				}
			}
		}

		textForm.associate(notesAnswerHolder, Forms.FreeText.PROMPT);
		notes.setText(notesAnswerHolder.getText());
		
		updateAudioFiles();
	}

	private void updateAudioFiles()
	{
		llAudioFiles.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(this.getActivity());

		IRegion overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if (overviewRegion != null)
		{
			for (IForm form : ((EditorActivityListener) a).media().getForms(a))
			{
				if (form.namespace.equals(Forms.FreeAudio.TAG))
				{
					AudioNoteInfoView view = (AudioNoteInfoView) inflater.inflate(R.layout.audio_note_info_view, llAudioFiles, false);
					view.setOnClickListener(this);
					view.setOnLongClickListener(this);
					view.setForm(form);
					llAudioFiles.addView(view);
				}
			}
		}

		if (llAudioFiles.getChildCount() > 0)
		{
			rlAudio.setVisibility(View.VISIBLE);
		}
		else
		{
			rlAudio.setVisibility(View.GONE);
		}
	}

	public View getAudioFilesView()
	{
		return llAudioFiles;
	}
	
	@Override
	public boolean saveForm()
	{
		Log.d(LOG, "OK I AM SAVING FORM");

		try
		{
			textForm.save(new info.guardianproject.iocipher.FileOutputStream(textForm.answerPath));
		}
		catch (FileNotFoundException e)
		{
			Logger.e(LOG, e);
		}

		// try {
		// audioForm.save(new
		// info.guardianproject.iocipher.FileOutputStream(audioForm.answerPath));
		// } catch (FileNotFoundException e) {
		// Logger.e(LOG, e);
		// }

		return InformaCam.getInstance().mediaManifest.save();
	}

	public void stopEditNotes(boolean save)
	{
		notes.setVisibility(View.VISIBLE);
		notesAnswerHolder.setVisibility(View.GONE);
		if (save)
		{
			if (TextUtils.isEmpty(notesAnswerHolder.getText()))
			{
				deleteForm(textForm);
				textForm = null;
				this.initForms();
			}
			else
			{
				textForm.answer(Forms.FreeText.PROMPT);
				notes.setText(notesAnswerHolder.getText());
			}
		}
		else
		{
			notesAnswerHolder.setText(notes.getText());
		}
		UIHelpers.hideSoftKeyboard(a, rootView);
	}
	
	public void startEditNotes()
	{
		notes.setVisibility(View.GONE);
		notesAnswerHolder.setVisibility(View.VISIBLE);
		notesAnswerHolder.requestFocus();
		notesAnswerHolder.setSelection(notesAnswerHolder.getText().length());
		UIHelpers.showSoftKeyboard(a, notesAnswerHolder);
	}

	@Override
	public void onClick(View v)
	{
		if (v instanceof AudioNoteInfoView)
		{
			AudioNoteInfoView view = (AudioNoteInfoView) v;
			
			if (mAudioPlayer != null && mAudioPlayer.form == view.getForm())
			{
				mAudioPlayer.toggle();
			}
			else
			{
				if (mAudioPlayer != null)
					mAudioPlayer.done();
				mAudioPlayer = new AudioNotePlayer(a, view);
				mAudioPlayer.toggle();
			}
		}
	}

	@Override
	public boolean onLongClick(View v)
	{
		if (v instanceof AudioNoteInfoView && mIsEditable)
		{
			showAudioNoteContextMenu((AudioNoteInfoView) v);
			return true;
		}
		return false;
	}
	
	private class AudioNotePlayer extends AudioNoteHelper implements OnSeekBarChangeListener
	{
		private Handler mHandler;
		private boolean mHasBeenShown;
		private final AudioNoteInfoView mView;
		
		public AudioNotePlayer(Activity a, AudioNoteInfoView view)
		{
			super(a, view.getForm());
			mView = view;
			mHasBeenShown = false;
			sbAudio.setProgress(0);
			sbAudio.setOnSeekBarChangeListener(this);
			this.progress.setOnSeekBarChangeListener(this);
		}

		@Override
		protected void onStateChanged()
		{
			super.onStateChanged();
			if (this.getState() == RecorderState.IS_PLAYING)
			{
				if (mHandler != null)
					mHandler.removeCallbacks(mHidePlayerRunnable);
				sbAudio.setMax(this.progress.getMax());
				if (!mHasBeenShown)
				{
					UIHelpers.fadeIn(sbAudio, 500);
					mHasBeenShown = true;
				}
				mView.getIconView().setImageResource(R.drawable.ic_view_audionote_pause);
			}
			else
			{
				if (mHandler == null)
					mHandler = new Handler();
				mHandler.postDelayed(mHidePlayerRunnable, 12000);
				sbAudio.setProgress(progress.getProgress());
				mView.getIconView().setImageResource(R.drawable.ic_view_audionote_play);
			}
		}

		private final Runnable mHidePlayerRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				closePlayer();
			}
		};
		
		private void closePlayer()
		{
			UIHelpers.fadeOut(sbAudio, 500);
			sbAudio.setOnSeekBarChangeListener(null);
			mAudioPlayer = null;
			mView.getIconView().setImageResource(R.drawable.ic_view_audionote);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			sbAudio.setProgress(progress);
			mView.setTime(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			if (seekBar == sbAudio)
			{
				this.setCurrentPosition(seekBar.getProgress() * 1000);
			}
		}
	}

	private void showAudioNoteContextMenu(final AudioNoteInfoView view)
	{
		try
		{
			LayoutInflater inflater = LayoutInflater.from(getActivity());

			View content = inflater.inflate(R.layout.popup_audionote_context_menu, (ViewGroup) rootView, false);
			content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			PopupWindow mMenuPopup = new PopupWindow(content, content.getMeasuredWidth(), content.getMeasuredHeight(), true);

			// Delete
			//
			View btnDelete = content.findViewById(R.id.btnDeleteAudioNote);
			btnDelete.setOnClickListener(new PopupClickListener(mMenuPopup)
			{
				@Override
				protected void onSelected()
				{
					// Delete!
					deleteForm(view.getForm());
					updateAudioFiles();
				}
			});

			mMenuPopup.setOutsideTouchable(true);
			mMenuPopup.setBackgroundDrawable(new BitmapDrawable());
			mMenuPopup.showAsDropDown(view, view.getWidth(), -view.getHeight());

			mMenuPopup.getContentView().setOnClickListener(new PopupClickListener(mMenuPopup));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void deleteForm(IForm formToDelete)
	{
		// Delete!
		IRegion overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if (overviewRegion != null)
		{
			for (IForm form : overviewRegion.associatedForms)
			{
				if (form.answerPath.equals(formToDelete.answerPath))
				{
					overviewRegion.associatedForms.remove(form);
					break;
				}
			}
		}
	}
}
