package org.witness.iwitness.utils;

import info.guardianproject.odkparser.Constants.RecorderState;
import info.guardianproject.odkparser.utils.QD;
import info.guardianproject.odkparser.widgets.ODKSeekBar;
import info.guardianproject.odkparser.widgets.ODKSeekBar.OnMediaRecorderStopListener;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;

public class AudioNoteHelper implements OnCompletionListener, OnMediaRecorderStopListener
{
	public ODKSeekBar progress;

	int state = RecorderState.IS_IDLE;

	Handler h = new Handler();

	public IForm form;
	InformaCam informaCam = InformaCam.getInstance();

	private long mRecordingStartTime;

	public AudioNoteHelper(Activity a, IForm form)
	{
		this.form = form;

		progress = new ODKSeekBar(a);
		initData();
	}

	private void updateClock(int time)
	{
		Log.d(App.Home.LOG, "counter: " + time);
		onUpdateClock(time);
	}

	private void initData()
	{
		progress.init(new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp"), this);
		QD qd = form.getQuestionDefByTitleId(Forms.FreeAudio.PROMPT);
		if (qd.hasInitialValue)
		{
			progress.setRawAudioData(qd.initialValue.getBytes());
			updateClock(progress.mp.getDuration());
		}
		this.form.associate(progress, Forms.FreeAudio.PROMPT);
	}

	public void toggle()
	{
		switch (state)
		{
		case RecorderState.IS_IDLE:
			if (progress.rawAudioData == null)
			{
				// record
				progress.record();
				setState(RecorderState.IS_RECORDING);
			}
			else
			{
				if (state != RecorderState.IS_IDLE)
				{
					if (state == RecorderState.IS_RECORDING)
					{
						progress.stop();
					}
					else if (state == RecorderState.IS_PLAYING)
					{
						progress.pause();
					}
					setState(RecorderState.IS_IDLE);
				}

				progress.play();
				setState(RecorderState.IS_PLAYING);
			}

			break;
		case RecorderState.IS_RECORDING:
			progress.stop();
			setState(RecorderState.IS_IDLE);

			form.answer(Forms.FreeAudio.PROMPT);
			break;
		case RecorderState.IS_PLAYING:

			progress.pause();
			setState(RecorderState.IS_IDLE);
			break;
		}
	}

	public void done()
	{
		if (state == RecorderState.IS_PLAYING)
		{
			progress.pause();
		}
		else if (state == RecorderState.IS_RECORDING)
		{
			progress.stop();
		}
		setState(RecorderState.IS_IDLE);
		form.answer(Forms.FreeAudio.PROMPT);
	}

	public void redo()
	{
		if (state == RecorderState.IS_PLAYING)
		{
			progress.pause();
		}
		else if (state == RecorderState.IS_RECORDING)
		{
			progress.stop();
		}

		form.getQuestionDefByTitleId(Forms.FreeAudio.PROMPT).clear();
		progress.reInit(new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp"), this);
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		Log.d(App.Home.LOG, "FINISHED PLAYING MEDIA FILE");

		progress.pause();
		mp.seekTo(0);

		setState(RecorderState.IS_IDLE);
	}

	@Override
	public void onMediaRecorderStop()
	{
		Log.d(App.Home.LOG, "HERE I CALL ON MEDIA RECORDER STOP");

	}

	private void setState(int newState)
	{
		state = newState;
		onStateChanged();
		if (state == RecorderState.IS_RECORDING)
		{
			mRecordingStartTime = System.currentTimeMillis();
			h.post(mUpdateRecordTimeRunnable);
		}
		else
		{
			h.removeCallbacks(mUpdateRecordTimeRunnable);
		}
	}

	Runnable mUpdateRecordTimeRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			onUpdateClock((int) (System.currentTimeMillis() - mRecordingStartTime));
			if (getState() == RecorderState.IS_RECORDING)
				h.postDelayed(mUpdateRecordTimeRunnable, 1000);
		}
	};

	public int getState()
	{
		return state;
	}

	protected void onUpdateClock(int time)
	{

	}

	/**
	 * Override this to handle state changes
	 */
	protected void onStateChanged()
	{

	}
	
	public void setCurrentPosition(int msec)
	{
		progress.setProgress(msec / 1000);
		progress.onProgressChanged(progress, progress.getProgress(), true); // Make sure callback is called as if from user!
	}
}
