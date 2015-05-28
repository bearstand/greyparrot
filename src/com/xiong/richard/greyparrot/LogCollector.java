package com.xiong.richard.greyparrot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class LogCollector {
	private static String TAG = "LogCollector";
	private static String storagePath = null;
	private static String LINE_SEPARATOR="\n";

	public static String getLogStoragePath(Context ctxt) {
		if (storagePath != null)
			return storagePath;
		File root = Environment.getExternalStorageDirectory();
		//File root = ctxt.getFilesDir();
		if (!root.canWrite()) {
			root = ctxt.getCacheDir();
		}

		File dir = new File(root.getAbsolutePath() + File.separator
				+ "log");
		if (!dir.exists()) {
			dir.mkdirs();
			Log.i(TAG, "created diretory" + dir.getAbsolutePath());

		}
		storagePath = dir.getAbsolutePath();
		return storagePath;
	}

	public static void cleanLogsFiles(Context ctxt) {
		File logsFolder = new File(getLogStoragePath(ctxt));
		if (logsFolder != null) {
			File[] files = logsFolder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						file.delete();
					}
				}
			}
		}
	}

	public static File getLogsFile(Context ctxt) {
		Date time = new Date();
		String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
				Locale.US).format(time);
		return (new File(getLogStoragePath(ctxt) + File.separator + "GPLog"
				+ timeStamp + ".txt"));
	}
	
    private static class LogResult {
        public StringBuilder head;
        public File file;

        public LogResult(StringBuilder aHead, File aFile) {
                head = aHead;
                file = aFile;
        }
    };


	public static LogResult collectLog(Context ctxt) {

		ArrayList<String> commandLine = new ArrayList<String>();
		final StringBuilder log = new StringBuilder();

		commandLine.add("logcat");

		//File outFile = getLogsFile(ctxt);
		//if (outFile != null) {
		//	commandLine.add("-f");
		//	commandLine.add(outFile.getAbsolutePath());

		//}
		commandLine.add("-d");
		commandLine.add("D");

		try {
			Process process = Runtime.getRuntime().exec(
					commandLine.toArray(new String[0]));
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			

            String line ;
            while ((line = bufferedReader.readLine()) != null){
                log.append(line);
                log.append("\n");
            }

            
		} catch (IOException e) {
			Log.e(TAG, "Collect logs failed : ", e);
			log.append("Unable to get logs : " + e.toString());
		}
		return(new LogResult(log, null));

	}
	
	public final static StringBuilder getDeviceInfo() {
		final StringBuilder log = new StringBuilder();
		
		// TODO: add more information as this :
		// http://androidblogger.blogspot.in/2009/12/how-to-improve-your-application-crash.html
		log.append( "Here are important informations about Device : ");
        log.append(LINE_SEPARATOR); 
        log.append("android.os.Build.BOARD : " + android.os.Build.BOARD );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.BRAND : " + android.os.Build.BRAND );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.DEVICE : " + android.os.Build.DEVICE );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.ID : " + android.os.Build.ID );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.MODEL : " + android.os.Build.MODEL );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.PRODUCT : " + android.os.Build.PRODUCT );
        log.append(LINE_SEPARATOR);
		log.append("android.os.Build.TAGS : " + android.os.Build.TAGS );
        log.append(LINE_SEPARATOR);
		log.append("android.os.Build.VERSION.INCREMENTAL : " + android.os.Build.VERSION.INCREMENTAL );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.VERSION.RELEASE : " + android.os.Build.VERSION.RELEASE );
        log.append(LINE_SEPARATOR); 
		log.append("android.os.Build.VERSION.SDK_INT : " + android.os.Build.VERSION.SDK_INT );
        log.append(LINE_SEPARATOR); 
		
		return log;
	}
	
	public static Intent getLogReportIntent(String infom, Context ctx) {
		LogResult logs = collectLog(ctx);
		
		
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "GreyParrot Error-Log report");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "richard.xiong.li@gmail.com", "bearstand@163.net"});
        
        StringBuilder log = new StringBuilder();

        log.append(infom);
        log.append(LINE_SEPARATOR);
        log.append(LINE_SEPARATOR);

        log.append(getDeviceInfo());
        log.append(LINE_SEPARATOR);
        log.append(logs.head);
        

        if(logs.file != null) {
        	sendIntent.putExtra( Intent.EXTRA_STREAM, Uri.fromFile(logs.file) );
        }
        

        log.append(LINE_SEPARATOR);
        log.append(LINE_SEPARATOR);
        
        sendIntent.setType("message/rfc822");
        
        sendIntent.putExtra(Intent.EXTRA_TEXT, log.toString());
        
        return sendIntent;
	}
	
	public static void alertUser(final Context ctxt){
		Log.i(TAG, "building alertDialog");
    	AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);
    	builder.setMessage(R.string.report_exception)
        .setTitle(R.string.sorry)
    	.setPositiveButton(R.string.send_log, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
		           ctxt.startActivity(LogCollector.getLogReportIntent("some unexpected error happend",ctxt));
            }
        })
        .setNegativeButton(R.string.not_send_log, null);
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
}
