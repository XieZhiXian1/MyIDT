/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ids.idtma.util;

import java.io.File;

import com.ids.idtma.R;import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 下载显示大图
 * 
 */
public class ShowBigImage extends Activity implements OnClickListener{

	private PhotoView image;
	private int default_res = R.drawable.default_user_online_image;
	private ImageButton return_button,map;
	private TextView page_title_name;
	private Bitmap bitmap;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_show_big_image);
		super.onCreate(savedInstanceState);
		return_button = (ImageButton) findViewById(R.id.return_button);
		page_title_name = (TextView) findViewById(R.id.page_title_name);
		page_title_name.setText("");
		map = (ImageButton) findViewById(R.id.map);
		return_button.setVisibility(View.VISIBLE);
		map.setVisibility(View.GONE);
		image = (PhotoView) findViewById(R.id.image);
		default_res = getIntent().getIntExtra("default_image", R.drawable.default_user_online_image);
		Uri uri = getIntent().getParcelableExtra("uri");
		String url=getIntent().getStringExtra("url");
		//本地存在，直接显示本地的图片
		if (uri != null && new File(uri.getPath()).exists()) {
			//内存溢出
//			bitmap=ImageUtils.getInstance().getBitmapFromUri(uri, ShowBigImage.this);
			//内存溢出
//			bitmap=ImageUtils.getInstance().getBitmapFromUrl(url, ShowBigImage.this);
			bitmap=ImageUtils.getInstance().getSmallBitmap(url,20);
			image.setImageBitmap(bitmap);
		}else {
			image.setImageResource(default_res);
		}

		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.return_button){
			ShowBigImage.this.finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
