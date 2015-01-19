package com.xiong.richard.greyparrot;

import java.io.File;
import android.view.KeyEvent;
import java.util.Collections;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileListActivity extends ListActivity {
	private final static String storagePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+ "/recorded";
	private final String DELETE_NAME="--deleted--";
	private String newFilename=null;
    private ListView mListView=null;
    private long currentPosition=0;
    private String [] fileNameArray=null;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fileNameArray=new File(storagePath).list();
		java.util.Arrays.sort(fileNameArray,Collections.reverseOrder());
		setListAdapter( new ArrayAdapter< String> ( this, R.layout.list_item, fileNameArray));
		
		mListView = getListView();
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

	      @Override
	      public boolean onItemLongClick(AdapterView<?> parent, View view,
	        int position, long id) {
	    	view.setSelected(true);
	    	currentPosition=position;
	    	showDialog((String)FileListActivity.this.getListAdapter().getItem(position));
	        return true;
	      }
	    });
	    
	  }
	  private void updateItemAtCurrentPosition(String filename){
		  int visiblePosition = mListView.getFirstVisiblePosition();
		  View view = mListView.getChildAt((int) ( currentPosition - visiblePosition));
		  ((TextView)view.findViewById(R.id.listFileName)).setText(filename);
		  fileNameArray[(int)currentPosition]=filename;
	  }

	  @Override
	  protected void onListItemClick(ListView l, View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);
	    v.setSelected(true);
	    String filename=this.getListAdapter().getItem(position).toString();
	    if ( filename.equals(DELETE_NAME)) return;
	    String fullPathName = storagePath+"/"+ filename;

		Intent intent = new Intent();  
		intent.setAction(android.content.Intent.ACTION_VIEW);  
		File file = new File(fullPathName);  
		intent.setDataAndType(Uri.fromFile(file), "audio/*");  
		startActivity(intent);
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
    	  private String filename=null;
    	  private EditText editText=null;
    	  private Button deleteButton=null;
    	  private Button renameButton=null;
    	  private Button closeButton=null;
    	  private boolean filenameChanged=false;
    	  
    	  public void onCreate(Bundle savedInstanceState) {
    	      super.onCreate(savedInstanceState);
    	      filename=getArguments().getString("filename");

    	  }
    	  
    	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
    	            Bundle savedInstanceState) {
    		View v = inflater.inflate(R.layout.edit_delete_file, container, false);
      	    editText = (EditText) v.findViewById(R.id.filenameText);
      		editText.setText(filename);
      		deleteButton= (Button)v.findViewById(R.id.delete);
      		renameButton= (Button)v.findViewById(R.id.rename);
      		closeButton= (Button)v.findViewById(R.id.close);
      		  
      		deleteButton.setOnClickListener(new View.OnClickListener(){
      			public void onClick(View v) {
      				String fullPath=storagePath+'/'+filename;
      				File f=new File(fullPath);
      				if( f.delete() ){
	
      				  updateItemAtCurrentPosition(DELETE_NAME);
      				  closeSelf();
      				}else{
      					//TODO: alert user
      				}
      			}
      		});
      		  
      		renameButton.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View v) {
				   if ( filenameChanged==false ) {
					   editText.requestFocus();
				   }
				   else {
					   File origFile=new File(storagePath+'/'+filename);
					   File targetFile=new File(storagePath+'/'+editText.getText());
					   if ( origFile.renameTo(targetFile) ){
						   
						   updateItemAtCurrentPosition(editText.getText().toString());
						   closeSelf();
					   }else{
						   //TODO : alert user the failure
					   }
					 
				   }
			   }
			});
      		editText.setOnKeyListener(new View.OnKeyListener(){
      			public boolean onKey(View v, int keyCode, KeyEvent event){
      				filenameChanged=true;
      				renameButton.setText(R.string.save);
      				return false;
      			}
      		});
      		closeButton.setOnClickListener(new View.OnClickListener() {
 			   @Override
 			   public void onClick(View v) {
 				   closeSelf();
 			   }});
 
    		return v;
       	  }
   		  private void closeSelf(){
   			getActivity().getFragmentManager().popBackStack();
  			return;
  		   }
      }
}
