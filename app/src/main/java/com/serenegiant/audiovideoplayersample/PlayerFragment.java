package com.serenegiant.audiovideoplayersample;
/*
 * AudioVideoPlayerSample
 * Sample project to play audio and video from MPEG4 file using MediaCodec.
 *
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: PlayerFragment.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

import com.serenegiant.media.MediaMoviePlayer;
import com.serenegiant.media.IFrameCallback;
import com.serenegiant.media.MediaVideoPlayer;
import com.serenegiant.widget.PlayerTextureView;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

@SuppressWarnings("unused")
public class PlayerFragment extends Fragment {
	private static final boolean DEBUG = true;	// TODO set false on release
	private static final String TAG = "PlayerFragment";
	
	/**
	 * for camera preview display
	 */
	private PlayerTextureView mPlayerView;	//	private PlayerGLView mPlayerView;
	/**
	 * button for start/stop recording
	 */
	private ImageButton mPlayerButton;

//	private MediaVideoPlayer mPlayer;
	private MediaMoviePlayer mPlayer;

	public PlayerFragment() {
		// need default constructor
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		mPlayerView = (PlayerTextureView)rootView.findViewById(R.id.player_view);
		mPlayerView.setAspectRatio(640 / 480.f);
		mPlayerButton = (ImageButton)rootView.findViewById(R.id.play_button);
		mPlayerButton.setOnClickListener(mOnClickListener);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mPlayerView.onResume();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		stopPlay();
		mPlayerView.onPause();
		super.onPause();
	}

	/**
	 * method when touch record button
	 */
	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.play_button:
				if (mPlayer == null)
					startPlay();
				else
					stopPlay();
				break;
			}
		}
	};

	/**
	 * start playing
	 */
	private void startPlay() {
		if (DEBUG) Log.v(TAG, "startRecording:");
		final Activity activity = getActivity();
		try {
			final File dir = activity.getFilesDir();
			dir.mkdirs();
			final File path = new File(dir, "easter_egg_nexus9_small.mp4");
			prepareSampleMovie(path);
			mPlayerButton.setColorFilter(0x7fff0000);	// turn red
//			mPlayer = new MediaVideoPlayer(mPlayerView.getSurface(), mIFrameCallback);
			mPlayer = new MediaMoviePlayer(mPlayerView.getSurface(), mIFrameCallback, true);
			mPlayer.prepare(path.toString());
		} catch (IOException e) {
			Log.e(TAG, "startPlay:", e);
		}
	}

	/**
	 * request stop playing
	 */
	private void stopPlay() {
		if (DEBUG) Log.v(TAG, "stopRecording:mPlayer=" + mPlayer);
		mPlayerButton.setColorFilter(0);	// return to default color
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			// you should not wait here
		}
	}

	/**
	 * callback methods from decoder
	 */
	private final IFrameCallback mIFrameCallback = new IFrameCallback() {
		@Override
		public void onPrepared() {
			final float aspect = mPlayer.getWidth() / (float)mPlayer.getHeight();
			final Activity activity = getActivity();
			if ((activity != null) && !activity.isFinishing())
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPlayerView.setAspectRatio(aspect);
					}
				});
			mPlayer.play();
		}

		@Override
		public void onFinished() {
			mPlayer = null;
			final Activity activity = getActivity();
			if ((activity != null) && !activity.isFinishing())
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPlayerButton.setColorFilter(0);	// return to default color
					}
				});
		}

		@Override
		public boolean onFrameAvailable(long presentationTimeUs) {
			return false;
		}
	};

	private final void prepareSampleMovie(File path) throws IOException {
		final Activity activity = getActivity();
		if (!path.exists()) {
			if (DEBUG) Log.i(TAG, "copy sample movie file from res/raw to app private storage");
			final BufferedInputStream in = new BufferedInputStream(activity.getResources().openRawResource(R.raw.easter_egg_nexus9_small));
			final BufferedOutputStream out = new BufferedOutputStream(activity.openFileOutput(path.getName(), Context.MODE_PRIVATE));
			byte[] buf = new byte[8192];
			int size = in.read(buf);
			while (size > 0) {
				out.write(buf, 0, size);
				size = in.read(buf);
			}
			in.close();
			out.flush();
			out.close();
		}
	}
}
