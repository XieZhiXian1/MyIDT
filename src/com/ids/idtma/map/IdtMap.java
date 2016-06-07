package com.ids.idtma.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.IdtGroup;
import com.ids.idtma.IdtMessage;
import com.ids.idtma.IdtSetting;
import com.ids.idtma.R;
import com.ids.idtma.adapter.IdtMapExpandableListViewAdapter;
import com.ids.idtma.chat.ActivityAudioCall;
import com.ids.idtma.chat.ActivityVideoCall;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.chat.PathConstant;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.ftp.FtpBuinessLayer;
import com.ids.idtma.jni.aidl.GpsData;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.service.LocationService;
import com.ids.idtma.util.CommonUtils;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.StringsUtils;
import com.ids.proxy.IDSApiProxyMgr;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class IdtMap extends ActivityBase implements IdtMapExpandableListViewAdapter.Listener,OnClickListener,OnLongClickListener,FtpBuinessLayer.Listener{
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private Button scanVedioButton, messageButton, singerCallButton;
	// private AutoCompleteTextView searchAutoCompleteTextView;
	private ExpandableListView expandablelistview;
	private IdtMapExpandableListViewAdapter idtMapExpandableListViewAdapter;
	private Button pop_zoom_button;
	private List<Map<String, String>> showMapPointListmaps;
	private String FROM_WHERE = "";
	private String LOCATION = "";
	private String USER_PHONE_NUM = "";
	private IdtApplication idtApplication;
	private LocationService locationService;
	private BDLocation local_get_location = null;
	public static int GPS_REPORT = 1;
	public static int RECEIVE_GPS_MESSAGE = 2;
	public static int SHOW_CURRENT_MEMBER_NUM = 3;
	public static int ACTION_RECEIVE_GROUP_CALL = 4;
	public static final int EXPAND = 5;
	public static final int NO_EXPAND = 6;
	public static final int REQUEST_CODE_VIDEO = 7;
	private int SUCCESS_TAKE_VIDEO = 8;
	public static final int FILE_UPLOAD_SUCCESS = 9;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	private IdtApplication application;
	private List<Map<String, Marker>> listMapMarkers;
	private String my_phone_number = "";
	public static String select_phone_num;
	private static double select_latitude;
	private static double select_longitute;
	private int CURRENT_EXPAND_STATE = EXPAND;
	private TextView current_memeber_textview;
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	private TextView intercom_state_textview;
	private ImageButton intercom_image_button;
	// 秒
	private int SMS_RESOURCE_TIME_LENGTH = 0;
	// 毫秒
	private long SMS_RESOURCE_START_TIME = 0;
	private long SMS_RESOURCE_STOP_TIME = 0;
	private FtpBuinessLayer ftpBuinessLayer;
	private Button zoomInBtn,zoomOutBtn;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == GPS_REPORT) {
				float longitute = (float) local_get_location.getLongitude();
				float latitude = (float) local_get_location.getLatitude();
				float speed = local_get_location.getSpeed();
				float direction = local_get_location.getDirection();
				Date date = new Date();
				int year = date.getYear();
				int month = date.getMonth();
				int day = date.getDay();
				int hour = date.getHours();
				int minute = date.getMinutes();
				int second = date.getSeconds();
				LwtLog.d("mymap", "IDSApiProxyMgr.getCurProxy().GpsReport===============" + longitute + "======"
						+ latitude + "==========" + speed + "========");
				IDSApiProxyMgr.getCurProxy().GpsReport(longitute, latitude, speed, direction, year, month, day, hour,
						minute, second);
			} else if (msg.what == RECEIVE_GPS_MESSAGE) {
				// 服务器有返回用户的gps情况
				// 需要刷新listview
				refresh();
			}else if (msg.what == SHOW_CURRENT_MEMBER_NUM) {
				current_memeber_textview.setVisibility(View.VISIBLE);
				current_memeber_textview.setText(select_phone_num);
			}else if (msg.what == ACTION_RECEIVE_GROUP_CALL) {
//				Bundle bundle=(Bundle) msg.getData();
//				callID = bundle.getInt(AppConstants.EXTRA_KEY_CALLID);
//				callee = bundle.getString(AppConstants.EXTRA_KEY_CALLEE);
//				caller = bundle.getString(AppConstants.EXTRA_KEY_CALLER);
//				status = bundle.getInt(AppConstants.EXTRA_KEY_CALL_STATUS);
				// 通知从这个地方走
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
				ringtone.play();
				playVibrate();
////			tv_group_number.setText(caller);
//				MediaAttribute pAttr = new MediaAttribute();
//				pAttr.ucAudioRecv = 1;
//				pAttr.ucAudioSend = 0;
//				pAttr.ucVideoRecv = 0;
//				pAttr.ucVideoSend = 0;
//				IDSApiProxyMgr.getCurProxy().CallAnswer(callID, pAttr, 0);
//                //存储本地
//				ContentValues contentValues = new ContentValues();
//				contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, caller);
//				contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
//				contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
//				contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
//				contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
//				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callee);
//				contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "") );
//				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
//				contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
//				Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
//				try {
//					IdtApplication.getCurrentCall().setUri(uri);
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
			} else if (msg.what == SUCCESS_TAKE_VIDEO) {
				SMS_RESOURCE_STOP_TIME = System.currentTimeMillis();
				sendVideo();
			} else if(msg.what == FILE_UPLOAD_SUCCESS){
				Toast.makeText(IdtMap.this, "视频上传成功", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	// 检测到没有按屏幕以后，证明释放话权
	class MyClickListener implements OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				status = STATUS_LISTEN;
				setImageButtonBackground();
				break;
			default:
				break;
			}
			return false;
		}
	}

	// 按下和放松的时候
		public void setImageButtonBackground() {
			switch (status) {
			case STATUS_TALK:
				intercom_image_button.setBackgroundResource(R.drawable.new_ui_ppt02);
				findViewById(R.id.intercom_other_image).setBackgroundResource(R.drawable.new_ui_say_button);
				IDSApiProxyMgr.getCurProxy().CallMicCtrl(callID, true);
				LwtLog.d("wulin", ">>>>>>>>>>>>> 获取话权");
				break;
			case STATUS_LISTEN:
				intercom_image_button.setBackgroundResource(R.drawable.new_ui_ppt01);
				findViewById(R.id.intercom_other_image).setBackgroundResource(R.drawable.new_ui_no_say_button);
				IDSApiProxyMgr.getCurProxy().CallMicCtrl(callID, false);
				LwtLog.d("wulin", ">>>>>>>>>>>>> 释放话权");
				break;
			default:
				break;
			}
		}

	// 长按获取话权
			@Override
			public boolean onLongClick(View view) {
				switch (view.getId()) {
				case R.id.intercom_button:
					if (IdtApplication.getCurrentCall()==null) {
						//不处于组呼中
						if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
							groupAudioStart();
							status = STATUS_TALK;
							setImageButtonBackground();
							Toast.makeText(IdtMap.this, "我在"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+"对讲组内发起对讲", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(IdtMap.this, "请先选择一个对讲组", Toast.LENGTH_SHORT).show();
						}
					}else{
						//处于组呼中
						status = STATUS_TALK;
						setImageButtonBackground();
					}
					break;
				default:
					break;
				}
				return true;
			}
			
			// 点击组呼语音电话图标
			public void groupAudioStart() {
				// 是不是正在拨打电话
				if (IdtApplication.resumeCurrentCall()) {
					((NotificationManager) IdtMap.this.getSystemService(Context.NOTIFICATION_SERVICE))
							.cancel(AppConstants.CALL_NOTIFICATION_ID);
					this.startActivity(IdtApplication.getCurrentCall().getIntent());
					return;
				}
				MediaAttribute pAttr = new MediaAttribute();
				pAttr.ucAudioRecv = 0;
				pAttr.ucAudioSend = 1;
				pAttr.ucVideoRecv = 0;
				pAttr.ucVideoSend = 0;
				// 没有锁定的群组就直接拨打
				String callNum = AppConstants.getLockedGroupNum().equals("") ? CurrentGroupCall.CURRENT_GROUP_CALL_NUM
						: AppConstants.getLockedGroupNum();
				/**
				 * 启动呼出 输入:
				 * 
				 * @param cPeerNum
				 *            : 对方号码
				 * @param SrvType
				 *            : 业务类型
				 * @param pAttr
				 *            : 媒体属性
				 * @param pUsrCtx
				 *            : 用户上下文
				 * @return 返回 -1: 失败 else: 呼叫标识 注意: 如果是组呼: 1.pcPeerNum为组号码
				 *         2.pAttr中,ucAudioSend为1,其余为0
				 */
				int id = IDSApiProxyMgr.getCurProxy().CallMakeOut(callNum, AppConstants.CALL_TYPE_GROUP_CALL, pAttr, 0);
				// 设置当前的拨打
				IdtApplication.setCurrentCall(new CallEntity(id, CallType.GROUP_CALL));
				//存储
				ContentValues contentValues = new ContentValues();
				contentValues = new ContentValues();
				contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
				contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
				contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
				contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
				contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, CurrentGroupCall.CURRENT_GROUP_CALL_NUM);
				contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
				contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
				Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
				try {
					IdtApplication.getCurrentCall().setUri(uri);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

	// A方发起群呼，当群呼建立起来以后，给A方一个回应
	@Override
	public void onCallPeerAnswer() {
		// TODO Auto-generated method stub
		super.onCallPeerAnswer();
		LwtLog.d("wulin", "对端应答 >>>>>>>>>>>> onCallPeerAnswer()");
		// 只要一打通，就置为听筒模式
		status = STATUS_LISTEN;
		CurrentGroupCall.CALL_OK = true;
		if (IdtApplication.getCurrentCall() != null
				&& IdtApplication.getCurrentCall().getType() == CallType.GROUP_CALL) {
			intercom_state_textview.setText(
					"对讲组名：" + CurrentGroupCall.CURRENT_GROUP_CALL_NUM + CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
		} else {
			intercom_state_textview.setText("对讲结束");
		}
	}

	// 根据现在存在话权方与否进行配置
		@Override
		public void onCallTalkingTips(String name, String phone) {
			LwtLog.d("wulin", "讲话方提示 >>>>>>>>>>>> onCallTalkingTips()");
			if (phone == null || "".equals(phone)) {
				CurrentGroupCall.CURRENT_GROUP_CALL_STATE="\n主讲人员：空闲";
				intercom_state_textview.setText("对讲组名："+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
			} else {
				CurrentGroupCall.CURRENT_GROUP_CALL_STATE="\n主讲人员：" + phone;
				intercom_state_textview.setText("对讲组名："+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
			}
		}

	// 远端释放的时候
	@Override
	public void onCallRelInd(int ID,int uiCause,Parcelable parcelable) {
		// TODO Auto-generated method stub
		LwtLog.d("wulin", "远端释放 >>>>>>>>>>>> onCallRelInd()");
		intercom_state_textview.setText("对讲结束");
	}


	public void playVibrate() {

		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// 震动一次
		vibrator.vibrate(500);
		// 第一个参数，指代一个震动的频率数组。每两个为一组，每组的第一个为等待时间，第二个为震动时间。
		// 比如 [2000,500,100,400],会先等待2000毫秒，震动500，再等待100，震动400
		// 第二个参数，repest指代从 第几个索引（第一个数组参数） 的位置开始循环震动。
		// 会一直保持循环，我们需要用 vibrator.cancel()主动终止
		// vibrator.vibrate(new long[]{300,500},0);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idt_activity_map);
		IdtApplication.getInstance().addActivity(this);
		idtApplication = (IdtApplication) this.getApplication();
		listMapMarkers = new ArrayList<Map<String, Marker>>();
		my_phone_number = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
		initMapView();
		initSidebar();
		startGetLocation();
		// 订阅gps
		spsSubs();
	}

	// 订阅用户
	private void spsSubs() {
		Set<String> set = getAllMemberNum();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String pcNum = it.next();
			IDSApiProxyMgr.getCurProxy().GpsSubs(pcNum, 1);
			LwtLog.d("mymap", "IDSApiProxyMgr.getCurProxy().GpsSubs(pcNum, 1)===============" + pcNum);
		}
	}

	/*****
	 * @see copy funtion to you project
	 *      定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
	 *
	 */
	private int index = 0;
	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				StringBuffer sb = new StringBuffer(256);
				local_get_location = location;
				if (index % 10 == 0) {
					handler.sendEmptyMessage(GPS_REPORT);
				} else {
					index++;
				}
				// sb.append("time : ");
				// /**
				// * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
				// * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
				// */
				// sb.append(location.getTime());
				// sb.append("\nerror code : ");
				// sb.append(location.getLocType());
				// sb.append("\nlatitude : ");
				// sb.append(location.getLatitude());
				// sb.append("\nlontitude : ");
				// sb.append(location.getLongitude());
				// sb.append("\nradius : ");
				// sb.append(location.getRadius());
				// sb.append("\nCountryCode : ");
				// sb.append(location.getCountryCode());
				// sb.append("\nCountry : ");
				// sb.append(location.getCountry());
				// sb.append("\ncitycode : ");
				// sb.append(location.getCityCode());
				// sb.append("\ncity : ");
				// sb.append(location.getCity());
				// sb.append("\nDistrict : ");
				// sb.append(location.getDistrict());
				// sb.append("\nStreet : ");
				// sb.append(location.getStreet());
				// sb.append("\naddr : ");
				// sb.append(location.getAddrStr());
				// sb.append("\nDescribe: ");
				// sb.append(location.getLocationDescribe());
				// sb.append("\nDirection(not all devices have value): ");
				// sb.append(location.getDirection());
				// sb.append("\nPoi: ");
				// if (location.getPoiList() != null &&
				// !location.getPoiList().isEmpty()) {
				// for (int i = 0; i < location.getPoiList().size(); i++) {
				// Poi poi = (Poi) location.getPoiList().get(i);
				// sb.append(poi.getName() + ";");
				// }
				// }
				// if (location.getLocType() == BDLocation.TypeGpsLocation) {
				// //GPS定位结果
				// sb.append("\nspeed : ");
				// sb.append(location.getSpeed());// 单位：km/h
				// sb.append("\nsatellite : ");
				// sb.append(location.getSatelliteNumber());
				// sb.append("\nheight : ");
				// sb.append(location.getAltitude());// 单位：米
				// sb.append("\ndescribe : ");
				// sb.append("gps定位成功");
				// } else if (location.getLocType() ==
				// BDLocation.TypeNetWorkLocation) {// 网络定位结果
				// // 运营商信息
				// sb.append("\noperationers : ");
				// sb.append(location.getOperators());
				// sb.append("\ndescribe : ");
				// sb.append("网络定位成功");
				// } else if (location.getLocType() ==
				// BDLocation.TypeOffLineLocation) {// 离线定位结果
				// sb.append("\ndescribe : ");
				// sb.append("离线定位成功，离线定位结果也是有效的");
				// } else if (location.getLocType() ==
				// BDLocation.TypeServerError) {
				// sb.append("\ndescribe : ");
				// sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
				// } else if (location.getLocType() ==
				// BDLocation.TypeNetWorkException) {
				// sb.append("\ndescribe : ");
				// sb.append("网络不同导致定位失败，请检查网络是否通畅");
				// } else if (location.getLocType() ==
				// BDLocation.TypeCriteriaException) {
				// sb.append("\ndescribe : ");
				// sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				// }
				// LwtLog.d("mymap", sb.toString());
			}
		}

	};

	// 打开定位服务
	private void startGetLocation() {
		locationService = ((IdtApplication) getApplication()).locationService;
		// 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		locationService.registerListener(mListener);
		// 注册监听
		int type = getIntent().getIntExtra("from", 0);
		locationService.setLocationOption(locationService.getDefaultLocationClientOption());
		// 定位SDK
		// start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
		locationService.start();
	}

	private Set<String> getAllMemberNum() {
		Set<String> set = new HashSet<String>();
		List<GroupMember> lGroupMembers = idtApplication.getLstGroups();
		Map<String, List<GroupMember>> maplist = idtApplication.getMapUserGroup();
		for (int i = 0; i < lGroupMembers.size(); i++) {
			String group_phone_num = lGroupMembers.get(i).getUcNum();
			for (int j = 0; j < maplist.get(group_phone_num).size(); j++) {
				GroupMember groupMember = maplist.get(group_phone_num).get(j);
				set.add(groupMember.getUcNum());
			}
		}
		return set;
	}

	private void initSidebar() {
		pop_zoom_button = (Button) findViewById(R.id.pop_zoom);
		// searchAutoCompleteTextView = (AutoCompleteTextView)
		// findViewById(R.id.idt_search);
		expandablelistview = (ExpandableListView) findViewById(R.id.buddy_expandablelistview);
		idtMapExpandableListViewAdapter = new IdtMapExpandableListViewAdapter(this);
		idtMapExpandableListViewAdapter.setListener(this);
		expandablelistview.setAdapter(idtMapExpandableListViewAdapter);
		// 分组展开
		expandablelistview.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				for (int i = 0; i < idtMapExpandableListViewAdapter.getGroupCount(); i++) {
					if (groupPosition != i) {
						expandablelistview.collapseGroup(i);
					}
				}
			}
		});
		// 分组关闭
		expandablelistview.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(final int groupPosition) {
			}
		});
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.pop_zoom:
			// if (pop_zoom_button.getText().toString().trim().equals("点击会收缩"))
			// {
			// pop_zoom_button.setText("点击会展开");
			// sssss
			//// searchAutoCompleteTextView.setVisibility(View.GONE);
			// expandablelistview.setVisibility(View.GONE);
			// } else {
			// pop_zoom_button.setText("点击会收缩");
			//// searchAutoCompleteTextView.setVisibility(View.VISIBLE);
			// expandablelistview.setVisibility(View.VISIBLE);
			// }
			if (CURRENT_EXPAND_STATE == NO_EXPAND) {
				CURRENT_EXPAND_STATE = EXPAND;
				expandablelistview.setVisibility(View.VISIBLE);
				view.setBackgroundResource(R.drawable.new_ui_map_open);
			} else {
				CURRENT_EXPAND_STATE = NO_EXPAND;
				expandablelistview.setVisibility(View.GONE);
				view.setBackgroundResource(R.drawable.new_ui_map_hide);
			}
			break;
		case R.id.scan_vedio:
			if(select_phone_num!=null && !select_phone_num.equals("")){
				if(!select_phone_num.equals(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""))){
					singerVedioCall();
				}else{
					//拨打自己电话
					Toast.makeText(IdtMap.this, "您不需要和自己通信", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.message:
			if(select_phone_num!=null && !select_phone_num.equals("")){
				if(!select_phone_num.equals(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""))){
					toRichMessagePage();
				}else{
					//拨打自己电话
					Toast.makeText(IdtMap.this, "您不需要和自己通信", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.singer_call:
			if(select_phone_num!=null && !select_phone_num.equals("")){
				if(!select_phone_num.equals(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""))){
					singerAudioStart();
				}else{
					//拨打自己电话
					Toast.makeText(IdtMap.this, "您不需要和自己通信", Toast.LENGTH_SHORT).show();
				}
			}
			break;
			
		case R.id.return_button:
			IdtApplication.getInstance().deleteActivity(this);
			break;
			
		case R.id.video_upload:
			selectVideoFromCamera();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 获取录像
	 */
	public void selectVideoFromCamera() {
		SMS_RESOURCE_TIME_LENGTH = 0;
		SMS_RESOURCE_START_TIME = 0;
		SMS_RESOURCE_STOP_TIME = 0;
		if (!CommonUtils.isExitsSdcard()) {
			Toast.makeText(getApplicationContext(), "SD卡不存在，不能拍照", Toast.LENGTH_SHORT).show();
			return;
		}
		LwtLog.d("take_video", "--------------selectVideoFromCamera");
		createFileNameAndPath(my_phone_number);
		LwtLog.d("take_video", "--------------LOCAL_FILE_STRING:" + PathConstant.LOCAL_FILE_STRING);
		File videoFile = new File(PathConstant.LOCAL_FILE_STRING);
		LwtLog.d("take_video", videoFile == null ? "videoFile is null" : "videoFile is not null");
		videoFile.getParentFile().mkdirs();
		LwtLog.d("take_video", "--------------startActivityForResult");
		SMS_RESOURCE_START_TIME = System.currentTimeMillis();
		startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
				.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile)).putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
				.putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, 10), REQUEST_CODE_VIDEO);
	}
	
	// 生成本地和远程路径和文件名
	private void createFileNameAndPath(String directory_num) {
		String data_time = DateUtil.dateToString(new Date(), DateUtil.TIME_PATTERN_6);
		String random_string = StringsUtils.getRandomString(8);
		//ftp文件所在目录
		PathConstant.REMOTE_FTP_PATH = "/MY_STORE/" + directory_num;
		if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 存在sd卡
			return;
		}	
		//ftp文件名字
		PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".mp4";
		//本地文件名(包含目录和名称)
		PathConstant.LOCAL_FILE_STRING = Environment.getExternalStorageDirectory().getPath() + "/IDT-MA"
				+ PathConstant.REMOTE_FTP_PATH + "/" + PathConstant.REMOTE_FTP_FILE;
		SharedPreferencesUtil.setStringPreferences(IdtMap.this, "REMOTE_FTP_PATH",
				PathConstant.REMOTE_FTP_PATH);
		SharedPreferencesUtil.setStringPreferences(IdtMap.this, "REMOTE_FTP_FILE",
				PathConstant.REMOTE_FTP_FILE);
		SharedPreferencesUtil.setStringPreferences(IdtMap.this, "LOCAL_FILE_STRING",
				PathConstant.LOCAL_FILE_STRING);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LwtLog.d("take_pic", "--------------onActivityResult");
		if (resultCode == RESULT_OK) {
			LwtLog.d("take_pic", "--------------RESULT_OK");
			if (requestCode == REQUEST_CODE_VIDEO) {
				LwtLog.d("wulin", "ppppppppppppp---------------------------------------onActivityResult");
				handler.sendEmptyMessage(SUCCESS_TAKE_VIDEO);
			} 
		}
	}
	
	/**
	 * 发送视频消息
	 */
	private void sendVideo() {
		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(IdtMap.this,
				"REMOTE_FTP_PATH", "");
		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(IdtMap.this,
				"REMOTE_FTP_FILE", "");
		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(IdtMap.this,
				"LOCAL_FILE_STRING", "");
		LwtLog.d("take_pic", ">>>>>>>>>>sendPicture....file_path:" + PathConstant.LOCAL_FILE_STRING + ",file_name:"
				+ PathConstant.REMOTE_FTP_FILE);
		startUploadFile(PathConstant.REMOTE_FTP_FILE);
		Toast.makeText(IdtMap.this, "开始上传视频", Toast.LENGTH_LONG).show();
	}
	
	private void startUploadFile(String filename) {
		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(IdtMap.this,
				"REMOTE_FTP_PATH", "");
		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(IdtMap.this,
				"REMOTE_FTP_FILE", "");
		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(IdtMap.this,
				"LOCAL_FILE_STRING", "");
		ftpBuinessLayer.uploadFile(PathConstant.LOCAL_FILE_STRING, PathConstant.REMOTE_FTP_PATH,PathConstant.REMOTE_FTP_FILE);
	}

	private void toRichMessagePage() {
		Intent intent = new Intent(this, IdtChatActivity.class);
		intent.putExtra("to_where", IdtGroup.TO_PERSION);
		intent.putExtra("callto_persion_num", select_phone_num);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	// 单独视频对话
	public void singerVedioCall() {
		if (IdtApplication.resumeCurrentCall()) {
			((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(AppConstants.CALL_NOTIFICATION_ID);
			this.startActivity(IdtApplication.getCurrentCall().getIntent());
			return;
		}
		MediaAttribute mediaAttribute = new MediaAttribute();
		mediaAttribute.ucAudioRecv = 1;
		mediaAttribute.ucAudioSend = 1;
		mediaAttribute.ucVideoRecv = 1;
		mediaAttribute.ucVideoSend = 1;
		int callId = IDSApiProxyMgr.getCurProxy().CallMakeOut(select_phone_num, AppConstants.CALL_TYPE_SINGLE_CALL,
				mediaAttribute, 0);
		IdtApplication.setCurrentCall(new CallEntity(callId, CallType.VEDIO_CALL));
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>开始视频呼叫" + select_phone_num + ", id : " + callId);
		Intent intent = new Intent(this, ActivityVideoCall.class);
		// 视频呼叫id
		intent.putExtra(AppConstants.EXTRA_KEY_CALLID, callId);
		// 对方号码
		intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, select_phone_num);
		// 自己号码
		intent.putExtra(AppConstants.EXTRA_KEY_CALLER, my_phone_number);
		// 媒体配置
		intent.putExtra(AppConstants.EXTRA_KEY_MediaAttr, mediaAttribute);
		// 当前状态
		intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityAudioCall.FLAG_CALLING);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	// 单呼别人
	public void singerAudioStart() {
		if (IdtApplication.resumeCurrentCall()) {
			((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(AppConstants.CALL_NOTIFICATION_ID);
			this.startActivity(IdtApplication.getCurrentCall().getIntent());
			return;
		}
		MediaAttribute pAttr = new MediaAttribute();
		pAttr.ucAudioRecv = 1;
		pAttr.ucAudioSend = 1;
		pAttr.ucVideoRecv = 0;
		pAttr.ucVideoSend = 0;
		// 传递一个序列化的对象、对方电话号码 到so/c端
		int id = IDSApiProxyMgr.getCurProxy().CallMakeOut(select_phone_num, AppConstants.CALL_TYPE_SINGLE_CALL, pAttr,
				0);
		// 记忆当前呼叫
		IdtApplication.setCurrentCall(new CallEntity(id, CallType.AUDIO_CALL));
		LwtLog.d("mymap", ">>>>>开始呼叫" + select_phone_num + ", id : " + id);
		Intent intent = new Intent(IdtMap.this, ActivityAudioCall.class);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLID, id);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLER, my_phone_number);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, select_phone_num);
		intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityVideoCall.FLAG_CALLING);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		IdtMap.this.startActivity(intent);
	}

	@Override
	public void getGroupAndUserData() {
		// TODO Auto-generated method stub
		super.getGroupAndUserData();
		idtMapExpandableListViewAdapter.notifyDataSetChanged();
	}
	
	private void hideZoomAndLogo() {
		// 隐藏百度的LOGO
		View child = mMapView.getChildAt(1);
		if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
			child.setVisibility(View.INVISIBLE);
		}
		//隐藏缩放控件
		mMapView.showZoomControls(false);
	}

	private void initMapView() {
		intercom_state_textview = (TextView) findViewById(R.id.intercom_state_textview);
		intercom_image_button = (ImageButton) findViewById(R.id.intercom_button);
		intercom_image_button.setOnLongClickListener(this);
		intercom_image_button.setOnTouchListener(new MyClickListener());
		// 获取地图控件引用
		current_memeber_textview=(TextView) findViewById(R.id.current_member);
		scanVedioButton = (Button) findViewById(R.id.scan_vedio);
		messageButton = (Button) findViewById(R.id.message);
		singerCallButton = (Button) findViewById(R.id.singer_call);
		scanVedioButton.setOnClickListener(this);
		messageButton.setOnClickListener(this);
		singerCallButton.setOnClickListener(this);
		hideFunctionView();
		mMapView = (MapView) findViewById(R.id.map);
		mBaiduMap = mMapView.getMap();
		// 隐藏百度logo图标和比例尺缩放图标
		hideZoomAndLogo();
		showMapPointListmaps = new ArrayList<Map<String, String>>();
		try {
			Intent intent = getIntent();
			FROM_WHERE = intent.getStringExtra("from");
			LOCATION = intent.getStringExtra("location");
			USER_PHONE_NUM = intent.getStringExtra("user_phone_num");
			if (LOCATION != null && !LOCATION.equals("") && USER_PHONE_NUM != null && !USER_PHONE_NUM.equals("")) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("user_phone_num", USER_PHONE_NUM);
				map.put("location", LOCATION);
				showMapPointListmaps.add(map);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 测试数据
		// for (Map<String, String> map : showMapPointListmaps) {
		// String location = map.get("location");
		// String[] la_lo = location.split(",");
		// LatLng latLng = new LatLng(Double.parseDouble(la_lo[0]),
		// Double.parseDouble(la_lo[1]));
		// toMarkAPoint(latLng, map.get("user_phone_num"));
		// }
		
		// 最开始需要刷新一次
		refresh();
		//显示中心点
		Double longitude = getIntent().getDoubleExtra("longitude", -1);
		Double latitude = getIntent().getDoubleExtra("latitude", -1);
		if (longitude != -1 && latitude != -1) {
			setCenterPoint(latitude, longitude);
		} else {
			setCenterPoint(22.5944330000, 113.9806750000);
		}
		//假如是从别人发的位置链接处跳过来的
		if(getIntent().getStringExtra("from")!=null && getIntent().getStringExtra("from").equals("IdtChatActivity")){
			LatLng latLng=new LatLng(latitude, longitude);
			Log.d("wulin", "toMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPointtoMarkAPoint");
			toMarkAPoint(latLng, my_phone_number);
		}
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtMap.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtMap.this.getApplication();
			if (idtApplication.getLstGroups().size() != 0) {
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM = idtApplication.getLstGroups().get(0).getUcNum();
			}
		}
		if(IdtApplication.getCurrentCall()!=null && IdtApplication.getCurrentCall().getType()==CallType.GROUP_CALL && CurrentGroupCall.CALL_OK == true){
			intercom_state_textview.setText("对讲组名："+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
		}else{
			intercom_state_textview.setText("对讲结束");
		}
		ftpBuinessLayer = new FtpBuinessLayer(IdtMap.this);
		ftpBuinessLayer.setListener(IdtMap.this);
		//自定义放大缩小功能
		zoomInBtn = (Button) findViewById(R.id.scan_big);
		zoomOutBtn = (Button) findViewById(R.id.scan_small);
		zoomInBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float zoomLevel = mBaiduMap.getMapStatus().zoom;
				if(zoomLevel<=18){
//					MapStatusUpdateFactory.zoomIn();
					mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomIn());
					zoomOutBtn.setEnabled(true);
				}else{
					Toast.makeText(IdtMap.this, "已经放至最大", Toast.LENGTH_SHORT).show();
					zoomInBtn.setEnabled(false);
				}
			}
		});
		zoomOutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float zoomLevel = mBaiduMap.getMapStatus().zoom;
				if(zoomLevel>4){
					mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomOut());
					zoomInBtn.setEnabled(true);
				}else{
					zoomOutBtn.setEnabled(false);
					Toast.makeText(IdtMap.this, "已经缩至最小", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void setCenterPoint(double latitude, double longitude) {
		// LatLng latLng_center = new LatLng(22.5944330000, 113.9806750000);
		LatLng latLng_center = new LatLng(latitude, longitude);
		// 设置地图状态 中心点
		MyLocationData locData = new MyLocationData.Builder()
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(latLng_center.latitude).longitude(latLng_center.longitude).build();
		mBaiduMap.setMyLocationData(locData);
		MapStatus.Builder builder = new MapStatus.Builder();
		builder.target(latLng_center).zoom(15.0f);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	}

	private void toMarkAPoint(LatLng latLng, String phone_num) {
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.new_ui_map_user_head_portrait);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
		// 在地图上添加Marker，并显示
		Marker marker = (Marker) mBaiduMap.addOverlay(option);
		Bundle bundle = new Bundle();
		bundle.putString("phone_num", phone_num);
		marker.setExtraInfo(bundle);
		setMakerWindow(marker, bitmap);
	}

	private void setMakerWindow(Marker marker, final BitmapDescriptor bitmapDescriptor) {
		// 对Marker的点击
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(final Marker marker) {
				// 获得marker中的数据
				final String phone_num = marker.getExtraInfo().getString("phone_num");
				InfoWindow mInfoWindow;
				OnInfoWindowClickListener listener = null;
				showFunctionView();
				// 生成一个TextView用户在地图中显示InfoWindow
				TextView textView = new TextView(getApplicationContext());
				textView.setText(phone_num);
				// 将marker所在的经纬度的信息转化成屏幕上的坐标
				LatLng latLng = marker.getPosition();
				listener = new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick() {
						// TODO Auto-generated method stub
						// Toast.makeText(IdtMap.this, "弹出" + phone_num +
						// "的功能视图", Toast.LENGTH_SHORT).show();
						// showFunctionView();
					}
				};
				select_phone_num = marker.getExtraInfo().getString("phone_num");
				handler.sendEmptyMessage(SHOW_CURRENT_MEMBER_NUM);
				select_latitude = marker.getExtraInfo().getDouble("latitude");
				select_longitute = marker.getExtraInfo().getDouble("longitute");
				mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(textView), latLng, -5, listener);
				// 显示InfoWindow
				mBaiduMap.showInfoWindow(mInfoWindow);
				return true;
			}
		});
	}

	private void refresh() {
		clearAllMarker();
		List<GpsData> lGpsDatas = idtApplication.getLgpsDatas();
		Set<String> set = getAllCheckNum();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String pcNum = it.next();
			for (int i = 0; i < lGpsDatas.size(); i++) {
				if (lGpsDatas.get(i).getUcNum().equals(pcNum)) {
					// 删除已经存在的记录
					existMarkerToDelete(pcNum);
					float longitute = lGpsDatas.get(i).getLongitude();
					float latitude = lGpsDatas.get(i).getLatitude();
					LatLng latLng = new LatLng(latitude, longitute);
					Map<String, Marker> map = new HashMap<String, Marker>();
					// 构建Marker图标
					BitmapDescriptor bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.new_ui_map_user_head_portrait);
					// 构建MarkerOption，用于在地图上添加Marker
					OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
					// 在地图上添加Marker，并显示
					Marker marker = (Marker) mBaiduMap.addOverlay(option);
					setMakerWindow(marker, bitmap);
					Bundle bundle = new Bundle();
					bundle.putString("phone_num", pcNum);
					bundle.putDouble("latitude", latitude);
					bundle.putDouble("longitute", longitute);
					marker.setExtraInfo(bundle);
					map.put("persion_marker", marker);
					// 将一个点记录加入到
					listMapMarkers.add(map);
					LwtLog.d("mymap", "refresh------refresh--------refresh-----------refresh------" + pcNum
							+ ",listMapMarkers的长度:" + listMapMarkers.size() + ",lGpsDatas的长度:" + lGpsDatas.size());
				}
			}
		}
	}

	private void clearAllMarker() {
		for (int i = 0; i < listMapMarkers.size(); i++) {
			listMapMarkers.get(i).get("persion_marker").remove();
		}
		mBaiduMap.hideInfoWindow();
	}

	private void existMarkerToDelete(String num) {
		int exist_index = -1;
		for (int i = 0; i < listMapMarkers.size(); i++) {
			if (listMapMarkers.get(i).get("persion_marker").getExtraInfo().getString("phone_num").equals(num)) {
				exist_index = i;
			}
		}
		if (exist_index != -1) {
			listMapMarkers.remove(exist_index);
		}
	}

	private Set<String> getAllCheckNum() {
		Set<String> set = new HashSet<String>();
		List<GroupMember> lGroupMembers = idtApplication.getLstGroups();
		Map<String, List<GroupMember>> maplist = idtApplication.getMapUserGroup();
		for (int i = 0; i < lGroupMembers.size(); i++) {
			String group_phone_num = lGroupMembers.get(i).getUcNum();
			for (int j = 0; j < maplist.get(group_phone_num).size(); j++) {
				GroupMember groupMember = maplist.get(group_phone_num).get(j);
				if (groupMember.getChecked()) {
					set.add(groupMember.getUcNum());
				}
			}
		}
		return set;
	}

	private void hideFunctionView() {
		scanVedioButton.setVisibility(View.GONE);
		messageButton.setVisibility(View.GONE);
		singerCallButton.setVisibility(View.GONE);
		current_memeber_textview.setVisibility(View.GONE);
	}

	private void showFunctionView() {
		scanVedioButton.setVisibility(View.VISIBLE);
		messageButton.setVisibility(View.VISIBLE);
		singerCallButton.setVisibility(View.VISIBLE);
		current_memeber_textview.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		locationService.unregisterListener(mListener); // 注销掉监听
		locationService.stop(); // 停止定位服务
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtMap.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtMap.this.getApplication();
			if (idtApplication.getLstGroups().size() != 0) {
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM = idtApplication.getLstGroups().get(0).getUcNum();
			}
		}
		if(IdtApplication.getCurrentCall()!=null && IdtApplication.getCurrentCall().getType()==CallType.GROUP_CALL && CurrentGroupCall.CALL_OK == true){
			intercom_state_textview.setText("对讲组名："+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
		}else{
			intercom_state_textview.setText("对讲结束");
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	public void receiveGpsMessage() {
		// TODO Auto-generated method stub
		super.receiveGpsMessage();
		handler.sendEmptyMessage(RECEIVE_GPS_MESSAGE);
	}

	@Override
	public void someOperation(String ucNum) {
		// TODO Auto-generated method stub
		refresh();
		hideFunctionView();
		if (!ucNum.equals("#")) {
			LatLng latLng = getLatLng(ucNum);
			if (latLng != null) {
				setCenterPoint(latLng.latitude, latLng.longitude);
			}
		}
	}

	private LatLng getLatLng(String phone_num) {
		LatLng latLng = null;
		for (int i = 0; i < listMapMarkers.size(); i++) {
			Marker marker = listMapMarkers.get(i).get("persion_marker");
			if (marker.getExtraInfo().getString("phone_num").equals(phone_num)) {
				latLng = new LatLng(marker.getExtraInfo().getDouble("latitude"),
						marker.getExtraInfo().getDouble("longitute"));
				break;
			}
		}
		return latLng;
	}
	
	//收到组呼请求
	@Override
	public void receiveGroupRequest(int ID, String pcMyNum, String pcPeerNum, int status) {
		// TODO Auto-generated method stub
		super.receiveGroupRequest(ID, pcMyNum, pcPeerNum, status);
		Log.d("mymap", "---------------------------"+ID+"---"+pcMyNum+"--------"+pcPeerNum+"-------"+status);
		Message msg=new Message();
		msg.what=ACTION_RECEIVE_GROUP_CALL;
//		Bundle bundle=new Bundle();
//		bundle.putInt(AppConstants.EXTRA_KEY_CALLID, ID);
//		bundle.putString(AppConstants.EXTRA_KEY_CALLEE, pcMyNum);
//		bundle.putString(AppConstants.EXTRA_KEY_CALLER, pcPeerNum);
//		bundle.putInt(AppConstants.EXTRA_KEY_CALL_STATUS, status);
//		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	@Override
	public void fileUploadSuccess(int style, Uri uri, String callto_persion_num, String filename) {
		// TODO Auto-generated method stub
		handler.sendEmptyMessage(FILE_UPLOAD_SUCCESS);
	}

	@Override
	public void fileDownloadSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noExitNewEdition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNewEdition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void apkDownloadCase(String case_status, String install_apk_path, long process) {
		// TODO Auto-generated method stub
		
	}
}
