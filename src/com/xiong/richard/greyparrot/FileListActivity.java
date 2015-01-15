package com.xiong.richard.greyparrot;

import java.io.File;
import java.util.Collections;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FileListActivity extends ListActivity {
	private final static String storagePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+ "/recorded";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String [] fileNameArray=new File(storagePath).list();
		java.util.Arrays.sort(fileNameArray,Collections.reverseOrder());
		setListAdapter( new ArrayAdapter< String> ( this, R.layout.list_item, fileNameArray));
		
		ListView list = getListView();
	    list.setOnItemLongClickListener(new OnItemLongClickListener() {

	      @Override
	      public boolean onItemLongClick(AdapterView<?> parent, View view,
	          int position, long id) {
	    	Intent intent=new Intent(FileListActivity.this, ChangeDeleteFileActivity.class);
	    	startActivity(intent);
        
	        return true;
	      }
	    });
	    
	  }

	  @Override
	  protected void onListItemClick(ListView l, View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);

	    String filename = storagePath+"/"+ this.getListAdapter().getItem(position);

	    
		Intent intent = new Intent();  
		intent.setAction(android.content.Intent.ACTION_VIEW);  
		File file = new File(filename);  
		intent.setDataAndType(Uri.fromFile(file), "audio/*");  
		startActivity(intent);

	  }


}
