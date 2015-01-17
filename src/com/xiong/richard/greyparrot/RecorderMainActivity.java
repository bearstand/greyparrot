package com.xiong.richard.greyparrot;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class RecorderMainActivity extends Activity {

    private static final String LOG_TAG = "RecorderMainActivity";
    private Button playButton=null;
    private Button recordButton=null;
    private Button openButton=null;
    private Boolean isPlaying=false;

    private Mp3Recorder mp3Record=new Mp3Recorder(this);
    
    private View.OnClickListener playListener=new View.OnClickListener() {
 		
		@Override
		public void onClick(View v) {
			if (mp3Record==null ) return;
			if ( mp3Record.isRecording() ) {
				mp3Record.stop();					
			}
			
			if ( mp3Record.getFilePath()!=null){
				Intent intent = new Intent();  
				intent.setAction(android.content.Intent.ACTION_VIEW);  
				File file = new File(mp3Record.getFilePath());  
				intent.setDataAndType(Uri.fromFile(file), "audio/*");  
				startActivity(intent);
			}
				
		}
	};

    private View.OnClickListener recordListener=new View.OnClickListener() {
 		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if ( mp3Record.isRecording() ) {
				mp3Record.stop();
				
				
			}else {
				mp3Record.init();
				mp3Record.start();
				recordButton.setText(R.string.stop);
			}
		}
	};
	
	  private View.OnClickListener openListener=new View.OnClickListener() {
	 		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecorderMainActivity.this, FileListActivity.class);  
				startActivity(intent);
				//intent.setAction(android.content.Intent.ACTION_VIEW); 
				//Uri uri = Uri.parse(mp3Record.getStorageDir());
				//intent.setDataAndType(uri, "resource/folder");
				//startActivity(intent);
				//intent.setDataAndType(uri, "text/csv");
				//startActivity(Intent.createChooser(intent, "Open folder"));
			}
		};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        playButton=(Button)findViewById(R.id.play);
        recordButton=(Button)findViewById(R.id.record);
        openButton=(Button)findViewById(R.id.open);
        playButton.setOnClickListener(playListener);
        recordButton.setOnClickListener(recordListener);
        openButton.setOnClickListener(openListener);
        
    }
    
    

    @Override
    public void onPause() {
        super.onPause();

    }
    public void stopRecording(){
    	recordButton.setText(R.string.record);    	
    }
    public void showError(){
    	
    }
}
