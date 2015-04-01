package com.xiong.richard.greyparrot;

import android.support.v7.app.ActionBarActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RecorderMainActivity extends ActionBarActivity {

	private static final String TAG = "RecorderMainActivity";
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
				FileManageFragment.playFile(RecorderMainActivity.this, filename);

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
			//TODO: replace the key with a public constant
			intent.putExtra("storagePath", getStorageDir()); 
			startActivity(intent);

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
		
		
		//Thanks to this discussion : 
		// http://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			        public void uncaughtException(Thread t, Throwable e) {
			           Log.wtf(TAG, t + " throws exception: " + e.getMessage());
		        	   Log.wtf(TAG, "stackTrace"+Log.getStackTraceString(e));

			           //why this can't work?
			           //LogCollector.alertUser(RecorderMainActivity.this);
		        	   
		        	   // I tried to add a new activity to show message to user and send log.
		        	   // But it doesn't work
			           startActivity(LogCollector.getLogReportIntent(getString(R.string.report_exception), RecorderMainActivity.this));
			        }
			     });
		
		this.service = new ServiceManager(this, Mp3Recorder.class, null);


	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(FILENAME_KEY, filename);

		Log.i(TAG, "onSaveInstanceState filename:" + filename);

	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		restoreMe(savedInstanceState);
		Log.i(TAG, "onRestoreInstanceState filename:" + filename);
	}

	private void restoreMe(Bundle state) {
		if (state != null) {
			filename = state.getString(FILENAME_KEY);
			if (service.isRunning()) {
				isRecording = true;
			}
			Log.i(TAG, "restored filename:" + filename);
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
		
		Log.i(TAG, "onResume:" + filename + "isRecording:" + isRecording);
		setUIByState(getState());
	}

	public void showError() {

	}

	private String getNewFilePath() {
		Date time = new Date();
		String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.US)
				.format(time);
		return (getStorageDir() + File.separator + "GP" + timeStamp + ".mp3");
	}

	public String getStorageDir() {
		return FileManageFragment.getStorageDir(this);
	}

	private int getState() {
		if (filename == null) {
			if (isRecording) {
				Log.i(TAG,
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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        	case R.id.help:
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage(getString(R.string.fileDir)+"\n"+FileManageFragment.getStorageDir(this))
	            .setTitle(R.string.help)
	        	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    // User clicked OK button
	                }
	            });
	        	AlertDialog dialog = builder.create();
	        	dialog.show();
	        	break;
        	case R.id.send_log:
        		Log.i(TAG,"info log is turned on");
        		Log.e(TAG, "error log is turned on");
        		Log.d(TAG, "debug log is turned on");
        		Log.w(TAG, "warning log is turned on");
        		startActivity( LogCollector.getLogReportIntent("--Please add any other infomation here:",this));
        		break;
        		
        	case R.id.settings:
    			startActivity(new Intent(RecorderMainActivity.this,	SettingsActivity.class ));
        		break;
        }
        return super.onOptionsItemSelected(item);
    }
}
