package com.xiong.richard.greyparrot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

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

	private int mSampleRate = 22050; // 22050,8000
	private String mFilePath = null;
	private boolean mIsRecording = false;
	private FileOutputStream output = null;
	private static String TAG = "MP3Recorder";

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
		mFilePath = intent.getStringExtra("filename");
		// create file
		try {
			output = new FileOutputStream(new File(mFilePath));
			mIsRecording = true;
			// start recording thread
			recordThread = new RecordThread();
			recordThread.start();
		} catch (FileNotFoundException e) {

		}

		return START_STICKY; // run until explicitly stopped.
	}

	private Thread recordThread = null;

	static {
		System.loadLibrary("mp3lame");
	}

	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REC_STARTED:
				break;
			case MSG_REC_STOPPED:
				break;
			case MSG_ERROR_GET_MIN_BUFFERSIZE:
				break;
			case MSG_ERROR_CREATE_FILE:
				break;
			case MSG_ERROR_REC_START:
				break;
			case MSG_ERROR_AUDIO_RECORD:
				break;
			case MSG_ERROR_AUDIO_ENCODE:
				break;
			case MSG_ERROR_WRITE_FILE:
				break;
			case MSG_ERROR_CLOSE_FILE:
				break;
			default:
				break;
			}
		}
	};

	public Mp3Recorder() {

	}

	public void start() {
		if (mIsRecording) {
			return;
		}
		recordThread = new RecordThread();
		recordThread.start();
	}

	public void stop() {
		mIsRecording = false;
		try {
			recordThread.join();
		} catch (InterruptedException e) {

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
				if (mHandler != null) {
					mHandler.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFERSIZE);
				}
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

			// PowerManager pm = (PowerManager)
			// mainUI.getApplicationContext().getSystemService(Context.POWER_SERVICE);
			// PowerManager.WakeLock wl =
			// pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Mp3Recorder");

			mIsRecording = true;
			try {
				try {
					audioRecord.startRecording();
				} catch (IllegalStateException e) {

					if (mHandler != null) {
						mHandler.sendEmptyMessage(MSG_ERROR_REC_START);
					}
					return;
				}

				try {
					if (mHandler != null) {
						mHandler.sendEmptyMessage(MSG_REC_STARTED);
					}

					int readSize = 0;

					// wl.acquire();
					while (mIsRecording) {
						readSize = audioRecord.read(buffer, 0, minBufferSize);
						if (readSize < 0) {
							if (mHandler != null) {
								mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
							}
							break;
						}

						else if (readSize == 0) {
							;
						} else {
							int encResult = SimpleLame.encode(buffer, buffer,
									readSize, mp3buffer);
							if (encResult < 0) {
								if (mHandler != null) {
									mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
								}
								break;
							}
							if (encResult != 0) {
								try {
									output.write(mp3buffer, 0, encResult);
								} catch (IOException e) {

									if (mHandler != null) {
										mHandler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
									}
									break;
								}
							}
						}
					}

					int flushResult = SimpleLame.flush(mp3buffer);
					if (flushResult < 0) {
						if (mHandler != null) {
							mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
						}
					}
					if (flushResult != 0) {
						try {
							output.write(mp3buffer, 0, flushResult);
						} catch (IOException e) {
							if (mHandler != null) {
								mHandler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
							}
						}
					}

					try {
						output.close();
					} catch (IOException e) {
						if (mHandler != null) {
							mHandler.sendEmptyMessage(MSG_ERROR_CLOSE_FILE);
						}
					}
				} finally {
					audioRecord.stop();
					audioRecord.release();
					// wl.release();
				}
			} finally {
				SimpleLame.close();
				mIsRecording = false;
			}

			if (mHandler != null) {
				mHandler.sendEmptyMessage(MSG_REC_STOPPED);
			}
		}
	}

}
