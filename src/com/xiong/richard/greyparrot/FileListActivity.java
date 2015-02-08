package com.xiong.richard.greyparrot;

import android.support.v7.app.ActionBarActivity;

import java.io.File;

import android.util.Log;

import java.util.Collections;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileListActivity extends ActionBarActivity  {
	private final static String TAG = "ListActivity";
	private String storagePath = null;
	private String fileDeleted = null;
	private ListView mListView = null;
	private long currentPosition = -1;
	private View selectedView = null;
	private String[] fileNameArray = null;

	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"oncreate "+"current postion:"+currentPosition);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_list_layout);

		Intent intent = getIntent(); 
		storagePath=intent.getStringExtra("storagePath");
		
		fileDeleted = getString(R.string.deletedFileName);
		fileNameArray = new File(storagePath).list();
		java.util.Arrays.sort(fileNameArray, Collections.reverseOrder());

		mListView = (ListView) findViewById(R.id.list);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				fileNameArray));

		
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(true);
				currentPosition = position;
				selectedView = view;
				showDialog((String) mListView.getAdapter().getItem(position));
				return true;
			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				v.setSelected(true);
				String filename = mListView.getAdapter().getItem(position).toString();
				if (filename.equals(fileDeleted))
					return;
				String fullPathName = storagePath + "/" + filename;

				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				File file = new File(fullPathName);
				intent.setDataAndType(Uri.fromFile(file), "audio/*");
				startActivity(intent);
			}
		});

		/* TODO: find a method to open default file browser
		Button openDirBtn=(Button)findViewById(R.id.openDir);
		openDirBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//File file = new File(storagePath);
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
				Uri data = Uri.parse(storagePath);

				intent.setDataAndType(data, "file/*");
				startActivity(Intent.createChooser(intent, "Open folder"));
			}
		});
		*/

	}

	public void updateItemAtCurrentPosition(String filename) {
		if (selectedView != null) {
			((TextView) selectedView.findViewById(R.id.listFileName))
					.setText(filename);
			if ( currentPosition >=0 ) fileNameArray[(int) currentPosition] = filename;
		} else {
			Log.i(TAG, "selectedView is null");
		}
		
	}


	void showDialog(String filename) {

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		FileManageFragment newFragment = FileManageFragment.newInstance(filename, storagePath);
		newFragment.show(ft, "dialog");
	}

}
