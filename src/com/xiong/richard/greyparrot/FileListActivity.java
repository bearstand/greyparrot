package com.xiong.richard.greyparrot;

import android.support.v7.app.ActionBarActivity;

import java.io.File;

import android.util.Log;
import android.view.KeyEvent;

import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class FileListActivity extends ActionBarActivity {
	private final static String TAG = "ListActivity";
	private String storagePath = null;
	private String fileDeleted = null;
	private ListView mListView = null;
	private long currentPosition = 0;
	private View selectedView = null;
	private String[] fileNameArray = null;

	public void onCreate(Bundle savedInstanceState) {
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

		/*
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

	private void updateItemAtCurrentPosition(String filename) {
		if (selectedView != null) {
			((TextView) selectedView.findViewById(R.id.listFileName))
					.setText(filename);
		} else {
			Log.i(TAG, "selectedView is null");
		}
		fileNameArray[(int) currentPosition] = filename;
	}


	void showDialog(String filename) {

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = new FileManageFragement();
		Bundle args = new Bundle();
		args.putString("filename", filename);
		newFragment.setArguments(args);

		newFragment.show(ft, "dialog");
	}

	private class FileManageFragement extends DialogFragment {
		private String filename = null;
		private EditText editText = null;
		private Button deleteButton = null;
		private Button renameButton = null;
		private Button closeButton = null;
		private Button shareButton = null;
		private boolean filenameChanged = false;

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			filename = getArguments().getString("filename");
			setStyle(STYLE_NO_TITLE,0);

		}

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.file_manage, container,
					false);
			editText = (EditText) v.findViewById(R.id.filenameText);
			editText.setText(filename);
			deleteButton = (Button) v.findViewById(R.id.delete);
			renameButton = (Button) v.findViewById(R.id.rename);
			closeButton = (Button) v.findViewById(R.id.close);
			shareButton = (Button) v.findViewById(R.id.share);

			deleteButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String fullPath = storagePath + '/' + filename;
					File f = new File(fullPath);
					if (f.delete()) {

						updateItemAtCurrentPosition(fileDeleted);
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

							updateItemAtCurrentPosition(editText.getText()
									.toString());
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
					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND);
					shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(storagePath + '/' + filename)));
					shareIntent.setType("audio/mpeg");
					startActivity(Intent.createChooser(shareIntent, "send to"));
					closeSelf();
				}
			});
			return v;
		}


		private void closeSelf() {
			getActivity().getFragmentManager().popBackStack();
			return;
		}
	}

}
