package org.witness.iwitness.app.screens.popups;

import info.guardianproject.odkparser.Constants.RecorderState;
import info.guardianproject.odkparser.utils.QD;
import info.guardianproject.odkparser.widgets.ODKSeekBar;
import info.guardianproject.odkparser.widgets.ODKSeekBar.OnMediaRecorderStopListener;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AudioNotePopup extends Popup implements OnClickListener, OnCompletionListener, OnMediaRecorderStopListener {
	RelativeLayout actionToggle;
	LinearLayout actionHolder;
	ImageView actionToggleIcon, actionToggleIcon2;
	TextView actionToggleLabel, actionClock;
	Button actionRedo, actionDone;
	public ODKSeekBar progress;
	
	int state = RecorderState.IS_IDLE;
	
	int recordRes, pauseRes, playRes;
	int recordLabel, pauseLabel, resumeLabel;
	int res, label;

	Handler h = new Handler();

	IForm form;
	InformaCam informaCam = InformaCam.getInstance();

	public AudioNotePopup(Activity a, IForm form) {
		super(a, R.layout.popup_audio_note);
		this.form = form;
		
		actionToggle = (RelativeLayout) layout.findViewById(R.id.audio_action_toggle);
		actionToggle.setOnClickListener(this);
		
		actionHolder = (LinearLayout) layout.findViewById(R.id.audio_action_holder);
		
		actionToggleIcon = (ImageView) layout.findViewById(R.id.audio_action_toggle_icon);
		actionToggleIcon2 = (ImageView) layout.findViewById(R.id.audio_action_toggle_icon_2);
		
		actionToggleLabel = (TextView) layout.findViewById(R.id.audio_action_toggle_label);
		actionClock = (TextView) layout.findViewById(R.id.audio_action_clock);

		recordRes = R.drawable.ic_audio_btn_pause;
		pauseRes = R.drawable.ic_audio_btn_pause;
		playRes = R.drawable.ic_audio_play;

		progress = (ODKSeekBar) layout.findViewById(R.id.audio_seekbar);

		actionRedo = (Button) layout.findViewById(R.id.audio_action_redo);
		actionRedo.setOnClickListener(this);
				
		actionDone = (Button) layout.findViewById(R.id.audio_action_done);
		actionDone.setOnClickListener(this);
		
		initData();
		Show();
	}
	
	private void updateClock(int time) {
		Log.d(LOG, "counter: " + time);
		actionClock.setText(TimeUtility.millisecondsToStopwatchTime(time));
	}
	
	private void updateLayout() {
		if(progress.mp.getDuration() == 0) {
			updateLayout(false);
		} else {
			updateLayout(true);
		}
	}
	
	private void updateLayout(boolean showHolder) {
		res = recordRes;
		label = R.string.record;
		
		if(showHolder) {
			actionClock.setVisibility(View.VISIBLE);
			actionHolder.setVisibility(View.VISIBLE);
			
			res = pauseRes;
			label = R.string.play;
			
			updateClock(progress.mp.getDuration());
		} else {
			actionClock.setVisibility(View.INVISIBLE);
			actionHolder.setVisibility(View.INVISIBLE);
		}
		
		if(state == RecorderState.IS_RECORDING) {
			actionToggleIcon2.setVisibility(View.VISIBLE);
			actionToggleLabel.setVisibility(View.GONE);
			res = pauseRes;
		} else {
			actionToggleIcon2.setVisibility(View.GONE);
			actionToggleLabel.setVisibility(View.VISIBLE);
			actionToggleLabel.setText(a.getString(label));
		}
		
		actionToggleIcon.setImageDrawable(a.getResources().getDrawable(res));
		
	}
	
	private void initData() {
		progress.init(new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp"), this);
		QD qd = form.getQuestionDefByTitleId(Forms.FreeAudio.PROMPT);
		if(qd.hasInitialValue) {
			progress.setRawAudioData(qd.initialValue.getBytes());			
			updateClock(progress.mp.getDuration());
		}
		
		updateLayout(qd.hasInitialValue);
		this.form.associate(progress, Forms.FreeAudio.PROMPT);
	}
	

	@Override
	public void onClick(View v) {
		if(v == actionToggle) {
			switch(state) {
			case RecorderState.IS_IDLE:
				if(progress.rawAudioData == null) {
					// record
					progress.record();
					setState(RecorderState.IS_RECORDING);
				} else {
					if(state != RecorderState.IS_IDLE) {
						if(state == RecorderState.IS_RECORDING) {
							progress.stop();
						} else if(state == RecorderState.IS_PLAYING) {
							progress.pause();
						}
						setState(RecorderState.IS_IDLE);
					}
					
					res = pauseRes;
					
					progress.play();
					setState(RecorderState.IS_PLAYING);
				}
				
				break;
			case RecorderState.IS_RECORDING:
				progress.stop();
				setState(RecorderState.IS_IDLE);
				
				res = recordRes;
				
				form.answer(Forms.FreeAudio.PROMPT);
				break;
			case RecorderState.IS_PLAYING:
				
				res = recordRes;
				
				progress.pause();
				setState(RecorderState.IS_IDLE);
				v.performClick();
				break;
			}
		} else if(v == actionDone) {
			if(state == RecorderState.IS_PLAYING) {
				progress.pause();
			} else if(state == RecorderState.IS_RECORDING) {
				progress.stop();
			}
			setState(RecorderState.IS_IDLE);
			
			form.answer(Forms.FreeAudio.PROMPT);
			cancel();
			return;
		} else if(v == actionRedo) {
			if(state == RecorderState.IS_PLAYING) {
				progress.pause();
			} else if(state == RecorderState.IS_RECORDING) {
				progress.stop();
			}
			
			form.getQuestionDefByTitleId(Forms.FreeAudio.PROMPT).clear();
			progress.reInit(new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp"), this);
			updateLayout(false);
			return;
		}
		
		updateLayout();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(LOG, "FINISHED PLAYING MEDIA FILE");
		
		progress.pause();
		mp.seekTo(0);
		
		setState(RecorderState.IS_IDLE);
	}

	@Override
	public void onMediaRecorderStop() {
		Log.d(LOG, "HERE I CALL ON MEDIA RECORDER STOP");
		
	}

	private void setState(int newState)
	{
		state = newState;
		onStateChanged();
	}
	
	/**
	 * Override this to handle state changes
	 */
	protected void onStateChanged()
	{
		
	}
}
