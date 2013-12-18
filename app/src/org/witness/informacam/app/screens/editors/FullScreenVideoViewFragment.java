package org.witness.informacam.app.screens.editors;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.witness.informacam.InformaCam;
import org.witness.informacam.app.R;
import org.witness.informacam.app.screens.FullScreenViewFragment;
import org.witness.informacam.app.utils.Constants;
import org.witness.informacam.app.utils.Constants.EditorActivityListener;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.models.media.IRegionBounds;
import org.witness.informacam.models.media.IVideo;
import org.witness.informacam.models.media.IVideoRegion;
import org.witness.informacam.storage.IOService;
import org.witness.informacam.ui.editors.IRegionDisplay;
import org.witness.informacam.utils.Constants.App.Storage.Type;

import android.app.Activity;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.efor18.rangeseekbar.RangeSeekBar;
import com.efor18.rangeseekbar.RangeSeekBar.OnRangeSeekBarChangeListener;

public class FullScreenVideoViewFragment extends FullScreenViewFragment implements OnCompletionListener,OnClickListener, 
OnErrorListener, OnInfoListener, OnBufferingUpdateListener, OnPreparedListener, OnSeekCompleteListener,
OnVideoSizeChangedListener, SurfaceHolder.Callback, OnTouchListener, MediaController.MediaPlayerControl, 
OnRangeSeekBarChangeListener<Integer> {
	IVideo media_;

	//MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	VideoView videoView;
	SurfaceHolder surfaceHolder;
	View mediaHolder_;
	
	MediaPlayer mediaPlayer;
	MediaController mediaController;

	LinearLayout videoControlsHolder, endpointHolder;
	VideoSeekBar videoSeekBar;
	ImageButton playPauseToggle;

	Uri videoUri;

	long duration = 0L;
	int currentCue = 1;
	
	Thread mServerThread;

	ServerSocket mVideoServerSocket;
	private int mLocalHostPort = 9999;
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		media_ = new IVideo(((EditorActivityListener) a).media());
		
		
	}
	

	boolean keepRunning = true;
	
	private void initMediaServer ()
	{


		
		
		closeMediaServer();
		
		
		mServerThread = new Thread(new Runnable() {
			
			public void run() {

				keepRunning = true;
				
				while (keepRunning)
				{
					try
					{

						if (mVideoServerSocket == null)
						{
							try {
								mVideoServerSocket = new ServerSocket (mLocalHostPort);
							} catch (IOException e) {
								Log.e(Constants.App.TAG,"unable to open server socket",e);
								return;
							}
						}
						
						Socket socket = mVideoServerSocket.accept();
						
						BufferedOutputStream os = new BufferedOutputStream(socket.getOutputStream());
						String mType = media_.dcimEntry.mediaType;

						IOService ioService = InformaCam.getInstance().ioService;
			
						IOUtils.write("HTTP/1.1 200\r\n",os);
						IOUtils.write("Content-Type: " + mType + "\r\n",os);
						
						long contentLength = ioService.getLength(media_.dcimEntry.fileName, Type.IOCIPHER);
						
						IOUtils.write("Content-Length: " + contentLength + "\r\n\r\n",os);
						
						InputStream is = ioService.getStream(media_.dcimEntry.fileName, Type.IOCIPHER);
						
						
						byte[] buffer = new byte[2048*4];
						int n = -1;
						while ((n = is.read(buffer))!=-1)
						{
							os.write(buffer,0,n);
						}
						
						os.close();
					}
					catch (IOException ioe)
					{

					}
				
				}
					
			}
		});
		
		mServerThread.start();
		
	}
	
	private void initVideo() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {


		if (mediaPlayer == null)
			mediaPlayer = new MediaPlayer();
		
		mediaPlayer.setDisplay(surfaceHolder);		
		
		
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnVideoSizeChangedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);

		mediaPlayer.setLooping(false);


		if (mServerThread == null)
		{
			initMediaServer();
		}
		
		videoUri = Uri.parse("http://localhost:" + mLocalHostPort + "/video");
		mediaPlayer.setDataSource(videoUri.toString());			

		mediaPlayer.prepare();

			
	}
	
	private void initVideoPost ()
	{

		mediaPlayer.setScreenOnWhilePlaying(true);
		mediaPlayer.start();
		mediaPlayer.setVolume(1f, 1f);
		mediaPlayer.seekTo(currentCue);
		mediaPlayer.pause();
		
		RangeSeekBar<Integer> rsb = videoSeekBar.init(mediaPlayer);
		rsb.setOnRangeSeekBarChangeListener(FullScreenVideoViewFragment.this);
		endpointHolder.addView(rsb);
		videoSeekBar.hideEndpoints();
		initRegions();
		
		playPauseToggle.setVisibility(View.VISIBLE);
		
		playPauseToggle.setClickable(true);
		
		updateRegionView(mediaPlayer.getCurrentPosition());	
	}
	
	private void updateRegionView(final long timestamp) {
		
		if (media_ == null || media_.associatedRegions == null || mediaHolder == null)
			return;
		
		for(IRegion r : media_.associatedRegions) {
			if(r.getRegionDisplay() != null && r.bounds.displayWidth != 0 && r.bounds.displayHeight != 0) {
				
				IRegionDisplay rd = (IRegionDisplay) mediaHolder.getChildAt(r.getRegionDisplay().indexOnScreen);
				
				if(timestamp >= r.bounds.startTime && timestamp <= r.bounds.endTime) {
					rd.setVisibility(View.VISIBLE);
					
					// TODO: update region display with new bounds from trail
					IRegionBounds rb = ((IVideoRegion) r).getBoundsAtTime(mediaPlayer.getCurrentPosition());
					
				} else {
					rd.setVisibility(View.GONE);
				}
				
			}
		}
			
	}
	
	@Override
	public void onDetach() {
		super.onDetach();

		closeMediaServer();
	}
	
	@Override
	public void onPause() {

		super.onPause();
		
		closeMediaServer();
	}

	private synchronized void closeMediaServer()
	{
		if (mVideoServerSocket != null && mVideoServerSocket.isBound())
		{
			try {
				keepRunning = false;
				mServerThread.interrupt();
				
				mVideoServerSocket.close();
				mVideoServerSocket = null;
				mServerThread = null;
			} catch (IOException e) {
				Log.e(Constants.App.TAG,"error shutting down video server",e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void initLayout() {
		super.initLayout();
	
		mediaHolder_ = LayoutInflater.from(getActivity()).inflate(R.layout.editors_video, null);

		videoView = (VideoView) mediaHolder_.findViewById(R.id.video_view);

		LayoutParams vv_lp = videoView.getLayoutParams();
		vv_lp.width = dims[0];
		vv_lp.height = (int) (((float) media_.dcimEntry.exif.height) / ((float) media_.dcimEntry.exif.width) * dims[0]);

		videoView.setLayoutParams(vv_lp);
		videoView.setOnTouchListener(this);

		mediaHolder.addView(mediaHolder_);
		
		surfaceHolder = videoView.getHolder();
		
		//Log.d(LOG, "surface holder dims: " + surfaceHolder.getSurfaceFrame().width() + " x " + surfaceHolder.getSurfaceFrame().height());
		surfaceHolder.addCallback(this);
		
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		//Log.d(LOG, "video view dims: " + videoView.getWidth() + " x " + videoView.getHeight());
		
		videoControlsHolder = (LinearLayout) mediaHolder_.findViewById(R.id.video_controls_holder);		

		videoSeekBar = (VideoSeekBar) mediaHolder_.findViewById(R.id.video_seek_bar);
		endpointHolder = (LinearLayout) mediaHolder_.findViewById(R.id.video_seek_bar_endpoint_holder);
		
		playPauseToggle = (ImageButton) mediaHolder_.findViewById(R.id.video_play_pause_toggle);
		playPauseToggle.setOnClickListener(this);
		playPauseToggle.setClickable(false);
		playPauseToggle.setVisibility(View.GONE);
		

		
		
	}
	
	 @Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		Toast.makeText(getActivity(), "Loading video. Please wait..." , Toast.LENGTH_LONG).show();
		
	}

	
	@Override
	public void onSelected(IRegionDisplay regionDisplay) {		
		
		((IVideoRegion) regionDisplay.parent).setTimestampInQuestion(mediaPlayer.getCurrentPosition());
		
		setCurrentRegion(regionDisplay.parent);
		videoSeekBar.showEndpoints((IVideoRegion) regionDisplay.parent);
		
		mediaPlayer.seekTo((int) regionDisplay.bounds.startTime);
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		surfaceHolder = holder;
		
		new VideoLoader().execute("");
		
	}

	private class VideoLoader extends AsyncTask<String, Void, Boolean> {
           @Override
           protected Boolean doInBackground(String... params) {
                
        	   try
        	   {
        		   initVideo();
        	   
        		   return true;
        	   }
        	   catch (Exception e)
        	   {
        		   Log.e(Constants.App.TAG,"error initVideo()",e);
        		   return false;
        	   }
           }

           @Override
           protected void onPostExecute(Boolean result) {

           }

           @Override
           protected void onPreExecute() {}

          @Override
           protected void onProgressUpdate(Void... values) {}
    };


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	
		if (mediaPlayer != null)
		{
			mediaPlayer.stop();
			videoSeekBar.disable();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		//Log.v(LOG, "onVideoSizeChanged called, new width: " + width + ", new height: " + height);
		
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		videoSeekBar.update();
		mIsSeeking = false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		
		 initVideoPost ();

		
		
	}

	int mBufferPercent = 0;
	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		mBufferPercent = percent;
		
		//Log.d(LOG,"buffering " + percent + "%");
	}

	@Override
	public boolean onError(MediaPlayer mp, int whatInfo, int extra) {
		
	//	Log.v(LOG, "onError called " + whatInfo + " (extra: " + extra + ")");
		
		if (whatInfo == -38 || whatInfo == 1)
		{
			playPauseToggle.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_videol_play));
			
			currentCue = mediaPlayer.getCurrentPosition();
			mediaPlayer.reset();
			try {
				mediaPlayer.setDataSource(videoUri.toString());
				mediaPlayer.prepare();
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			

		}
	
		/*
		if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
			Log.v(LOG, "Media Info, Media Info Bad Interleaving " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
			Log.v(LOG, "Media Info, Media Info Not Seekable " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
			Log.v(LOG, "Media Info, Media Info Unknown " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
			Log.v(LOG, "MediaInfo, Media Info Video Track Lagging " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) { 
			Log.v(LOG, "MediaInfo, Media Info Metadata Update " + extra); 
		} else if (whatInfo == MediaPlayer.MEDIA_ERROR_IO) {
			Log.v(LOG, "Media Info, Media Info IO error " + extra);
		} else if (whatInfo == -38) {
			Log.v(LOG, "i have no clue what error -38 is");
		}*/
		
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		//Log.d(LOG, "onCompletion called");

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
	//	Log.d(LOG, "onInfo called: what=" + what + "; extra=" + extra);
		return false;
	}

	@Override
	public void onClick(View v) {
	
		if(v == playPauseToggle) {
			if(mediaPlayer.isPlaying()) {
				pause();
			} else {
				start();
			}
		}
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return (mBufferPercent == 100);
	}

	@Override
	public int getBufferPercentage() {
		return mBufferPercent;
	}

	@Override
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return mediaPlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	@Override
	public void pause() {
		playPauseToggle.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_videol_play));
		videoSeekBar.pause();
		mediaPlayer.pause();
	}

	private boolean mIsSeeking = false;
	
	@Override
	public void seekTo(int pos) {
		
		if (!mIsSeeking)
		{
			if (pos <= (duration * (mBufferPercent/100)))
			{
				mIsSeeking = true;
				
				mediaPlayer.seekTo(pos);
			}
		}
		
	}

	@Override
	public void start() {
		playPauseToggle.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_videol_pause));

		duration = mediaPlayer.getDuration();
		mediaPlayer.start();
		videoSeekBar.play();
	}

	@Override
	public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
		//Log.d(LOG, "new range: " + minValue + " - " + maxValue);
		if(currentRegion != null) {
			currentRegion.bounds.startTime = minValue;
			currentRegion.bounds.endTime = maxValue;
		}
	}

	@Override
	public void onStartTrackingTouch(RangeSeekBar<?> bar) {}

	@Override
	public void onStopTrackingTouch(RangeSeekBar<?> bar) {}
	
	@Override
	public int[] getSpecs() {
		//Log.d(LOG, "RECALCULATING FOR VIDEO");
		List<Integer> specs = new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(super.getSpecs())));
		
		int[] locationInWindow = new int[2];
		videoView.getLocationInWindow(locationInWindow);
		for(int i : locationInWindow) {
			specs.add(i);
		}
		
		// these might not be needed
		specs.add(videoView.getWidth());
		specs.add(videoView.getHeight());
		
		//Log.d(LOG, "position on screen : " + locationInWindow[0] + ", " + locationInWindow[1]);
		
		return ArrayUtils.toPrimitive(specs.toArray(new Integer[specs.size()]));
	}
	
	@Override
	public RectF getImageBounds()
	{
		return new RectF(0,0,videoView.getWidth(),videoView.getHeight());
	}

	@Override
	public int getAudioSessionId() {
		return 1;
	}
}
