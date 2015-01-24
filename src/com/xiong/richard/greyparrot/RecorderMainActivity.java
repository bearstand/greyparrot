package com.xiong.richard.greyparrot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RecorderMainActivity extends Activity {

	private static final String LOG_TAG = "RecorderMainActivity";
	private Button playButton = null;
	private Button recordButton = null;
	private Button openButton = null;
	private TextView infoText = null;
	private String filename = null;
	private boolean isRecording = false;
	private static final String FILENAME_KEY = "filename_key";

	private static final int INIT_STATE = 0;
	private static final int RECORDING_STATE = 1;
	private static final int RECORDING_STOPED = 2;


	// private Mp3Recorder mp3Record=new Mp3Recorder(this);

	private ServiceManager service;

	public static final String PREFS_NAME = "FILE_NAME";

	private View.OnClickListener playListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (service.isRunning()) {
				service.stop();
				setUIByState(RECORDING_STOPED);
			}

			if (filename != null) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				File file = new File(filename);
				intent.setDataAndType(Uri.fromFile(file), "audio/*");
				startActivity(intent);
			}

		}
	};

	private View.OnClickListener recordListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (service.isRunning()) {
				service.stop();
				isRecording = false;
				setUIByState(RECORDING_STOPED);
			} else {
				filename = getNewFilePath();
				service.start("filename", filename);
				setUIByState(RECORDING_STATE);
			}
		}
	};

	private View.OnClickListener openListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(RecorderMainActivity.this,
					FileListActivity.class);
			startActivity(intent);
			// intent.setAction(android.content.Intent.ACTION_VIEW);
			// Uri uri = Uri.parse(mp3Record.getStorageDir());
			// intent.setDataAndType(uri, "resource/folder");
			// startActivity(intent);
			// intent.setDataAndType(uri, "text/csv");
			// startActivity(Intent.createChooser(intent, "Open folder"));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorder);
		playButton = (Button) findViewById(R.id.play);
		recordButton = (Button) findViewById(R.id.record);
		openButton = (Button) findViewById(R.id.open);
		infoText = (TextView) findViewById(R.id.statusInfo);
		playButton.setOnClickListener(playListener);
		recordButton.setOnClickListener(recordListener);
		openButton.setOnClickListener(openListener);

		this.service = new ServiceManager(this, Mp3Recorder.class, null);

		// restoreMe(savedInstanceState);

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(FILENAME_KEY, filename);

		Log.i(LOG_TAG, "onSaveInstanceState filename:" + filename);

	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		restoreMe(savedInstanceState);
		Log.i(LOG_TAG, "onRestoreInstanceState filename:" + filename);
	}

	private void restoreMe(Bundle state) {
		if (state != null) {
			filename = state.getString(FILENAME_KEY);
			if (service.isRunning()) {
				isRecording = true;
			}
			Log.i(LOG_TAG, "restored filename:" + filename);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(FILENAME_KEY, filename);

		editor.commit();
	}

	public void onResume() {

		super.onResume();
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		filename = settings.getString(FILENAME_KEY, null);
		isRecording = service.isRunning();
		
		Log.i(LOG_TAG, "onResume:" + filename + "isRecording:" + isRecording);
		setUIByState(getState());
	}

	public void showError() {

	}

	private String getNewFilePath() {
		Date time = new Date();
		String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss")
				.format(time);
		return (getStorageDir() + "/" + "GP" + timeStamp + ".mp3");
	}

	public String getStorageDir() {

		String storagePath = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
				+ "/recorded";

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File storageDir = null;
			storageDir = new File(storagePath);

			if (!storageDir.exists()) {
				storageDir.mkdirs();
			}

		}

		return storagePath;
	}

	private int getState() {
		if (filename == null) {
			if (isRecording) {
				Log.i(LOG_TAG,
						"Wrong status: filename is null while is recording");
				return (RECORDING_STATE);
			} else {
				return (INIT_STATE);
			}
		} else {
			if (isRecording) {
				return (RECORDING_STATE);
			} else {
				return (RECORDING_STOPED);
			}
		}

	}

	private void setUIByState(int state) {
		switch (state) {
		case INIT_STATE:
			recordButton.setText(R.string.record);
			infoText.setText(R.string.init_reminder);
			break;
		case RECORDING_STOPED:
			recordButton.setText(R.string.record);
			infoText.setText(getString(R.string.recordedFile) + getBasename(filename));
			break;
		case RECORDING_STATE:
			recordButton.setText(R.string.stop);
			infoText.setText(getString(R.string.recording_to) + getBasename(filename));
			break;

		}
		return;

	}
	private String getBasename( String fullPath){
		int lastSlash;
		lastSlash=fullPath.lastIndexOf('/');
		return fullPath.substring(lastSlash+1);
	}
}
