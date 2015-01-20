package com.xiong.richard.greyparrot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class RecorderMainActivity extends Activity {

	private static final String LOG_TAG = "RecorderMainActivity";
	private Button playButton = null;
	private Button recordButton = null;
	private Button openButton = null;
	private String filename;
	
	private static final int INIT_STATE=0;
	private static final int RECORDING_STATE=1;
	private static final int RECORDING_STOPED=2;
	
	// private Mp3Recorder mp3Record=new Mp3Recorder(this);

	private ServiceManager service;

	private View.OnClickListener playListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (service.isRunning()) {
				service.stop();
				recordButton.setText(R.string.record);
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
				recordButton.setText(R.string.record);
			} else {
				filename = getNewFilePath();
				service.start("filename", filename);
				recordButton.setText(R.string.stop);
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
		playButton.setOnClickListener(playListener);
		recordButton.setOnClickListener(recordListener);
		openButton.setOnClickListener(openListener);
		restoreMe(savedInstanceState);
		this.service = new ServiceManager(this, Mp3Recorder.class, null);

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("filename", filename);

	}
	public void onRestoreInstanceState(Bundle savedInstanceState) {  
		  super.onRestoreInstanceState(savedInstanceState);  
		  filename=savedInstanceState.getString("filename");
	}

	private void restoreMe(Bundle state) {
		if (state != null) {
			filename = state.getString("filename");
		}
	}

	@Override
	public void onPause() {
		super.onPause();

	}
	public void onResume(){
		super.onResume();
		setUIByState(guessState());
	}

	public void stopRecording() {
		recordButton.setText(R.string.record);
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
	private int guessState(){
		if ( filename == null ){
			if ( service.isRunning()){
				Log.i(LOG_TAG, "Wrong status: filename is null, service is runing");
				return(INIT_STATE);
			}else {
				return(INIT_STATE);
			}
		}else {
			if ( service.isRunning()){
				
				return(RECORDING_STATE);
			}else {
				return(RECORDING_STOPED);
			}
		}
		
	}
	private void setUIByState( int state){
		switch ( state ){
			case INIT_STATE:
			case RECORDING_STOPED:
				recordButton.setText(R.string.record);
				break;
			case RECORDING_STATE:
				recordButton.setText(R.string.stop);
				break;
			
		}
		return;
		
	}
}
