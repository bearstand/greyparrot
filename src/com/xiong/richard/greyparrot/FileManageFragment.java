package com.xiong.richard.greyparrot;

import java.io.File;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class FileManageFragment extends DialogFragment {
	private String filename = null;
	private static String gStoragePath=null;
	private String storagePath=null;
	private String fileDeleted=null;
	private EditText editText = null;
	private Button deleteButton = null;
	private Button renameButton = null;
	private Button closeButton = null;
	private Button shareButton = null;
	private boolean filenameChanged = false;
	private static final String TAG="FileManageFragment";
	
	private static boolean noExternalStorage=false;

	public static FileManageFragment newInstance( String filename, String storagePath){
		FileManageFragment fragment=new FileManageFragment();
		
		Bundle args = new Bundle();
		args.putString("filename", filename);
		args.putString("storagePath", storagePath);
		fragment.setArguments(args);
		
		return fragment;
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		filename = getArguments().getString("filename");
		storagePath=getArguments().getString(Consts.STORAGE_PATH_KEY);
		fileDeleted = getString(R.string.deletedFileName);
		setStyle(STYLE_NO_TITLE,0);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.file_manage, container,false);
		editText = (EditText) v.findViewById(R.id.filenameText);
		editText.setText(filename);
		deleteButton = (Button) v.findViewById(R.id.delete);
		renameButton = (Button) v.findViewById(R.id.rename);
		closeButton = (Button) v.findViewById(R.id.close);
		shareButton = (Button) v.findViewById(R.id.share);
		Log.i(TAG, "onCreateView "+ "filename="+filename);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String fullPath = storagePath + '/' + filename;
				File f = new File(fullPath);
				if (f.delete()) {

					//updateItemAtCurrentPosition(fileDeleted);
					((FileListActivity) getActivity()).updateItemAtCurrentPosition(fileDeleted);
					closeSelf();
				} else {
					// TODO: alert user
				}
			}
		});

		renameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (filenameChanged == false) {
					editText.requestFocus();
				} else {
					File origFile = new File(storagePath + '/' + filename);
					File targetFile = new File(storagePath + '/'
							+ editText.getText());
					if (origFile.renameTo(targetFile)) {

						//updateItemAtCurrentPosition(editText.getText().toString());
						((FileListActivity) getActivity()).updateItemAtCurrentPosition(editText.getText().toString());
						closeSelf();
					} else {
						// TODO : alert user the failure
					}

				}
			}
		});
		editText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				filenameChanged = true;
				renameButton.setText(R.string.save);
				return false;
			}
		});
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeSelf();
			}
		});

		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			    try {
			    	shareFile(getActivity(), storagePath+File.separator+filename);

			    }catch (IllegalArgumentException e) {
			        Log.e(TAG,"The selected file can't be shared: " + storagePath+File.separator+filename);
				}finally{
			        closeSelf();
				}
			}
		});
		return v;
	}


	private void closeSelf() {
		getActivity().getFragmentManager().popBackStack();
		return;
	}
	
	public void shareFile(Activity activity, String filename){
    	File file=new File( filename);
    	
    	if ( !usePrivateStorage(activity)){
    		Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			shareIntent.setType("*/*");
			startActivity(Intent.createChooser(shareIntent, "send to"));
    	}else{
	    	Uri fileUri;
	        fileUri = FileProvider.getUriForFile(activity,Consts.FILE_PROVIDER, file);
	        
	        final Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
	                .setType("*/*")
	                .setStream(fileUri)
	                .createChooserIntent()
	                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	
	        startActivity(shareIntent);
    	}

	}
	
	public static void playFile(Context ctx, String filename){
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = new File(filename);
		

		if ( !usePrivateStorage(ctx)){
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "audio/*");
		}else{
		    try {
			    // Use the FileProvider to get a content URI
				Uri fileUri;
		        fileUri = FileProvider.getUriForFile(ctx,Consts.FILE_PROVIDER, file);
				intent.setDataAndType(fileUri, "audio/*");
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		    } catch (IllegalArgumentException e) {
		    	//TODO: notify user about the error
		        Log.e(TAG, "The selected file can't be shared: " + filename);
		        return;
		    }


		}
		ctx.startActivity(intent);
	}
	
	public static String getStorageDir(Context ctxt) {

		if ( gStoragePath == null ){

			if (usePrivateStorage(ctxt)){
				gStoragePath=ctxt.getFilesDir()+File.separator+Consts.SUB_PATH;
			}else{
				gStoragePath=ctxt.getExternalFilesDir(null)+File.separator+Consts.SUB_PATH;
			}
			File storageDir = new File(gStoragePath);
			if (!storageDir.exists()) {
				storageDir.mkdirs();
			}
		}
		return gStoragePath;
	}
	
	private static boolean usePrivateStorage(Context ctxt){
		if ( noExternalStorage) return(true);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctxt);
		return sharedPref.getBoolean("pref_private_storage", false);
	}
	public static void reset(){
		noExternalStorage=false;
		gStoragePath=null;
	}
	public static boolean isInPublicStorage( String filename){
	
		if ( filename.indexOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath())>= 0){
			Log.i(TAG, "isInPublicStorage, filename:"+ filename+"  public path:"+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
			return(true);
		}
		return(false);
	}
}


