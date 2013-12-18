package org.witness.informacam.app.screens.editors;

import org.witness.informacam.app.utils.Constants.App;
import org.witness.informacam.models.media.IVideoRegion;
import org.witness.informacam.app.R;

import com.efor18.rangeseekbar.RangeSeekBar;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;;

public class VideoSeekBar extends SeekBar implements OnSeekBarChangeListener {
	MediaPlayer mp;
	int thumbInactive = R.drawable.ic_videol_mark_un;
	int thumbActive = R.drawable.ic_videol_mark_selected;
	
	public boolean isEditing = false;
	
	RangeSeekBar<Integer> endpointBar;
	Context context;
	
	private final static String LOG = App.TAG;
	private boolean keepRunning = true;
	
	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			
			try
			{
				if (keepRunning && mp != null)
				{
					setProgress(mp.getCurrentPosition());
					postDelayed(progressRunnable, 1000L);
				}
			}
			catch (IllegalStateException ise)
			{
				Log.d(LOG,"player not in proper state",ise);
			}
		}
	};
	
	public VideoSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	public RangeSeekBar<Integer> init(MediaPlayer mp) {
		this.mp = mp;
		
		setMax(mp.getDuration());
		post(progressRunnable);
		setOnSeekBarChangeListener(this);
		
		endpointBar = new RangeSeekBar<Integer>(0, mp.getDuration(), context);		
		return endpointBar;
		
	}
	
	public void update() {
		setProgress(mp.getCurrentPosition());
	}
	
	public void play() {
		hideEndpoints();
	}
	
	public void disable ()
	{
		keepRunning = false;
		
	}
	public void pause() {
	}
	
	public void showEndpoints(IVideoRegion region) {
		
		endpointBar.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
		
		endpointBar.setSelectedMinValue((int) region.bounds.startTime);
		endpointBar.setSelectedMaxValue((int) region.bounds.endTime);
	}
	
	public void hideEndpoints() {
		setVisibility(View.VISIBLE);
		endpointBar.setVisibility(View.GONE);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			mp.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
}
