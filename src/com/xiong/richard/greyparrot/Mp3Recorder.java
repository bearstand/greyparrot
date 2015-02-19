package com.xiong.richard.greyparrot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.app.Notification;
import android.app.PendingIntent;

public class Mp3Recorder extends AbstractService {

	public static final int MSG_REC_STARTED = 0;
	public static final int MSG_REC_STOPPED = 1;
	public static final int MSG_ERROR_GET_MIN_BUFFERSIZE = 2;
	public static final int MSG_ERROR_CREATE_FILE = 3;
	public static final int MSG_ERROR_REC_START = 4;
	public static final int MSG_ERROR_AUDIO_RECORD = 5;
	public static final int MSG_ERROR_AUDIO_ENCODE = 6;
	public static final int MSG_ERROR_WRITE_FILE = 7;
	public static final int MSG_ERROR_CLOSE_FILE = 8;

	private int mSampleRate = 22050; // 44100? 22050,8000
	private String mFilePath = null;
	private boolean mIsRecording = false;
	private FileOutputStream output = null;
	private static final String TAG = "MP3Recorder";

	private static final int NOTIFICATION_ID = 99;

	public void onStartService() {

	}

	public void onStopService() {
		// stop recording thread
		stop();
		// close file
	}

	public void onReceiveMessage(Message msg) {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// set file name
		if ( intent == null){
			Log.i(TAG, "recieved null intent");
			return(START_NOT_STICKY);
		}
		mFilePath = intent.getStringExtra("filename");
		// create file
		try {
			output = new FileOutputStream(new File(mFilePath));
			mIsRecording = true;
			// start recording thread
			recordThread = new RecordThread();
			recordThread.start();
			runAsForeground();
			
			//TEST code for capture exception
			//Log.e("TEST code", "throw a RuntimeException");
			//throw( new RuntimeException());
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "create file failed", e);
		}
		// TODO: If the service is stopped by system,it is better to continue recording, but it needs to :
		// 1. get a new filename
		// 2. record in the new file and let the user know the new file
		// refert to : http://stackoverflow.com/questions/17430690/android-ics-service-restart-hang-on-intent-getextras
		return START_NOT_STICKY; 
							
	}

	private void runAsForeground() {
		Intent notificationIntent = new Intent(this, RecorderMainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentText(getString(R.string.isRecording))
				.setContentIntent(pendingIntent).build();

		startForeground(NOTIFICATION_ID, notification);

	}

	private Thread recordThread = null;

	static {
		System.loadLibrary("mp3lame");
	}

	public Mp3Recorder() {

	}

	public void stop() {
		mIsRecording = false;
		try {
			recordThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, "join thread failed", e);

		}
		stopForeground(true);
	}

	private void sendResultMessage(int flag) {

	}

	//TODO: separate it to a new class if it become complicate
	//refer to : http://en.wikipedia.org/wiki/ID3
	// add the MP3 tag in the file
	private void addID3() throws IOException{
		byte[] tag="TAG".getBytes();
		byte[] title=mFilePath.substring(mFilePath.lastIndexOf('/')+3,mFilePath.lastIndexOf('.')).getBytes();
		byte[] artist="GreyParrot APP".getBytes();
		byte[] album="richard.xiong.li@gmail.com".getBytes();
		byte[] year="2015".getBytes();
		byte[] buf=new byte[128];
		
		fillBuf(buf, tag, 0);
		fillBuf(buf, title, 3);
		fillBuf(buf, artist, 33);
		fillBuf(buf, album, 63);
		fillBuf(buf, year, 93);
		
		output.write(buf);
	}
	
	private void fillBuf(byte[] dest, byte[] src, int offset){
		for ( int i=0; i< src.length;i++){
			dest[offset+i]=src[i];
		}
	}
	
	private class RecordThread extends Thread {
		@Override
		public void run() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			final int minBufferSize = AudioRecord
					.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);

			if (minBufferSize < 0) {
				sendResultMessage(MSG_ERROR_GET_MIN_BUFFERSIZE);
				return;
			}

			AudioRecord audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC, mSampleRate,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);

			// PCM buffer size (5sec)
			short[] buffer = new short[mSampleRate * (16 / 8) * 1 * 5]; // SampleRate[Hz]
																		// *
																		// 16bit
																		// *
																		// Mono
																		// *
																		// 5sec
			byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

			// Lame init
			SimpleLame.init(mSampleRate, 1, mSampleRate, 32);

			mIsRecording = true;
			try {
				audioRecord.startRecording();

				sendResultMessage(MSG_REC_STARTED);

				int readSize = 0;

				while (mIsRecording) {
					readSize = audioRecord.read(buffer, 0, minBufferSize);
					if (readSize < 0) {
						sendResultMessage(MSG_ERROR_AUDIO_RECORD);
						break;
					} else if (readSize > 0) {
						int encResult = SimpleLame.encode(buffer, buffer,
								readSize, mp3buffer);
						if (encResult < 0) {
							sendResultMessage(MSG_ERROR_AUDIO_ENCODE);
							break;
						}
						if (encResult != 0) {
							output.write(mp3buffer, 0, encResult);
						}
					}
				}

				int flushResult = SimpleLame.flush(mp3buffer);
				if (flushResult < 0) {
					sendResultMessage(MSG_ERROR_AUDIO_ENCODE);
				} else if (flushResult > 0) {
					output.write(mp3buffer, 0, flushResult);
				}
				addID3();
				output.close();

			} catch (IOException e) {
				sendResultMessage(MSG_ERROR_WRITE_FILE);
				Log.e(TAG, "write file failed", e);
				
			} catch (IllegalStateException e) {
				sendResultMessage(MSG_ERROR_REC_START);
				Log.e(TAG, "record voice error", e);
			} finally {
				audioRecord.stop();
				audioRecord.release();
				SimpleLame.close();
				mIsRecording = false;
			}

			sendResultMessage(MSG_REC_STOPPED);

		}
	}

}
