package com.ids.idtma.chat;

import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class MyVideoView extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_video_view);
		IdtApplication.getInstance().addActivity(this);
		String url = getIntent().getStringExtra("url");
		Uri uri = Uri.parse(url);  
	    VideoView videoView = (VideoView)this.findViewById(R.id.video_view);  
	    videoView.setMediaController(new MediaController(this));  
	    videoView.setVideoURI(uri);  
	    videoView.start();  
	    videoView.requestFocus(); 
	}
}
