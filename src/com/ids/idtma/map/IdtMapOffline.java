package com.ids.idtma.map;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.util.SharedPreferencesUtil;

public class IdtMapOffline extends Activity implements MKOfflineMapListener, OnItemClickListener {

	private MKOfflineMap mOffline = null;
	private TextView cidView;
	private TextView stateView;
	private TextView page_title_name;
	private EditText cityNameView;
	private ArrayList<MKOLSearchRecord> records1;
	private ArrayList<MKOLSearchRecord> records2;
//	private TextView my_num_textview;
	/**
	 * 已下载的离线地图信息列表
	 */
	private ArrayList<MKOLUpdateElement> localMapList = null;
	private LocalMapAdapter lAdapter = null;

	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline);
		IdtApplication.getInstance().addActivity(this);
		mOffline = new MKOfflineMap();
		mOffline.init(this);
		initView();
	}

	private void initView() {

		cidView = (TextView) findViewById(R.id.cityid);
		cityNameView = (EditText) findViewById(R.id.city);
		stateView = (TextView) findViewById(R.id.state);
		page_title_name = (TextView) findViewById(R.id.page_title_name);
		page_title_name.setText("离线地图");
		ListView hotCityList = (ListView) findViewById(R.id.hotcitylist);
		ArrayList<String> hotCities = new ArrayList<String>();
		// 获取热闹城市列表
		records1 = mOffline.getHotCityList();
		if (records1 != null) {
			for (MKOLSearchRecord r : records1) {
				hotCities.add(r.cityName + "(" + r.cityID + ")" + "   --" + this.formatDataSize(r.size));
			}
		}
		ListAdapter hAdapter = (ListAdapter) new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				hotCities);
		hotCityList.setAdapter(hAdapter);
		hotCityList.setOnItemClickListener(this);

		ListView allCityList = (ListView) findViewById(R.id.allcitylist);
		// 获取所有支持离线地图的城市
		ArrayList<String> allCities = new ArrayList<String>();
		records2 = mOffline.getOfflineCityList();
		if (records2 != null) {
			for (MKOLSearchRecord r : records2) {
				allCities.add(r.cityName + "(" + r.cityID + ")" + "   --" + this.formatDataSize(r.size));
			}
		}
		ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				allCities);
		allCityList.setAdapter(aAdapter);
		allCityList.setOnItemClickListener(this);
		LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
		LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
		lm.setVisibility(View.GONE);
		cl.setVisibility(View.VISIBLE);

		// 获取已下过的离线地图信息
		localMapList = mOffline.getAllUpdateInfo();
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
		}

		ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
		lAdapter = new LocalMapAdapter();
		localMapListView.setAdapter(lAdapter);
//		my_num_textview = (TextView) findViewById(R.id.my_num);
//		my_num_textview.setText(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.map:
			// 点击地图图标
			Intent intent = new Intent(IdtMapOffline.this, IdtMap.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.return_button: {
			// 点击屏幕上方返回键
			IdtApplication.getInstance().deleteActivity(this);
			}
		default:
			break;
		}
	}

	/**
	 * 切换至城市列表
	 * 
	 * @param view
	 */
	public void clickCityListButton(View view) {
		LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
		LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
		lm.setVisibility(View.GONE);
		cl.setVisibility(View.VISIBLE);

	}

	/**
	 * 切换至下载管理列表
	 * 
	 * @param view
	 */
	public void clickLocalMapListButton(View view) {
		LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
		LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
		lm.setVisibility(View.VISIBLE);
		cl.setVisibility(View.GONE);
	}

	/**
	 * 搜索离线需市
	 *
	 * @param view
	 */
	public void search(View view) {
		ArrayList<MKOLSearchRecord> records = mOffline.searchCity(cityNameView.getText().toString());
		if (records == null || records.size() != 1) {
			Toast.makeText(IdtMapOffline.this, "您的输入不规范", Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(IdtMapOffline.this, "搜索到的城市为:"+records.get(0).cityName+",点击开始按钮进行下载", Toast.LENGTH_LONG).show();
		cidView.setText(String.valueOf(records.get(0).cityID));
	}

	/**
	 * 开始下载
	 * 
	 * @param view
	 */
	public void start(View view) {
		int cityid = Integer.parseInt(cidView.getText().toString());
		mOffline.start(cityid);
		clickLocalMapListButton(null);
		Toast.makeText(this, "开始下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
		updateView();
	}

	/**
	 * 暂停下载
	 * 
	 * @param view
	 */
	public void stop(View view) {
		int cityid = Integer.parseInt(cidView.getText().toString());
		mOffline.pause(cityid);
		Toast.makeText(this, "暂停下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
		updateView();
	}

	/**
	 * 删除离线地图
	 * 
	 * @param view
	 */
	public void remove(View view) {
		int cityid = Integer.parseInt(cidView.getText().toString());
		mOffline.remove(cityid);
		Toast.makeText(this, "删除离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
		updateView();
	}

	/**
	 * 更新状态显示
	 */
	public void updateView() {
		localMapList = mOffline.getAllUpdateInfo();
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
		}
		lAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		int cityid = Integer.parseInt(cidView.getText().toString());
		MKOLUpdateElement temp = mOffline.getUpdateInfo(cityid);
		if (temp != null && temp.status == MKOLUpdateElement.DOWNLOADING) {
			mOffline.pause(cityid);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}

	@Override
	protected void onDestroy() {
		/**
		 * 退出时，销毁离线地图模块
		 */
		mOffline.destroy();
		super.onDestroy();
	}

	@Override
	public void onGetOfflineMapState(int type, int state) {
		switch (type) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
			MKOLUpdateElement update = mOffline.getUpdateInfo(state);
			// 处理下载进度更新提示
			if (update != null) {
				stateView.setText(String.format("%s : %d%%", update.cityName, update.ratio));
				updateView();
			}
		}
			break;
		case MKOfflineMap.TYPE_NEW_OFFLINE:
			// 有新离线地图安装
			Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
			break;
		case MKOfflineMap.TYPE_VER_UPDATE:
			// 版本更新提示
			// MKOLUpdateElement e = mOffline.getUpdateInfo(state);

			break;
		default:
			break;
		}

	}

	/**
	 * 离线地图管理列表适配器
	 */
	public class LocalMapAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return localMapList.size();
		}

		@Override
		public Object getItem(int index) {
			return localMapList.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View view, ViewGroup arg2) {
			MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
			view = View.inflate(IdtMapOffline.this, R.layout.offline_localmap_list, null);
			initViewItem(view, e);
			return view;
		}

		void initViewItem(View view, final MKOLUpdateElement e) {
			Button display = (Button) view.findViewById(R.id.display);
			Button remove = (Button) view.findViewById(R.id.remove);
			TextView title = (TextView) view.findViewById(R.id.title);
			TextView update = (TextView) view.findViewById(R.id.update);
			TextView ratio = (TextView) view.findViewById(R.id.ratio);
			ratio.setText(e.ratio + "%");
			title.setText(e.cityName);
			if (e.update) {
				update.setText("可更新");
			} else {
				update.setText("最新");
			}
			if (e.ratio != 100) {
				display.setEnabled(false);
			} else {
				display.setEnabled(true);
			}
			remove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mOffline.remove(e.cityID);
					updateView();
				}
			});
			display.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("longitude", e.geoPt.longitude);
					intent.putExtra("latitude", e.geoPt.latitude);
					intent.setClass(IdtMapOffline.this, IdtMap.class);
					startActivity(intent);
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.hotcitylist:
			cidView.setText(records1.get(position).cityID+"");
			break;
			
		case R.id.allcitylist:
			cidView.setText(records2.get(position).cityID+"");
			break;

		default:
			break;
		}
	}

}