package com.ids.idtma;

import java.io.File;

import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.ftp.FtpBuinessLayer;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.map.IdtMap;
import com.ids.idtma.map.IdtMapOffline;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.CommonUtils;
import com.ids.idtma.util.CurrentDownload;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.CustomDialog;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.WaitDialog;
import com.ids.proxy.IDSApiProxyMgr;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class IdtSetting extends ActivityBase
		implements OnItemClickListener, FtpBuinessLayer.Listener, OnLongClickListener {
	private ListView listview;
	private ArrayAdapter<String> arrayAdapter;
//	private WaitDialog waitDialog;
	private FtpBuinessLayer ftpBuinessLayer;
	private String SERVER_FILE_PATH = "/IM/APK/ANDROID";
	private String LOCAL_PATH = Environment.getExternalStorageDirectory() + "/IDT-MA/IM/";
	private String APK_INSTALL_STRING_PATH = "";
	private TextView page_title_name;
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	private TextView intercom_state_textview;
	private ImageButton intercom_image_button;
	public static int ACTION_RECEIVE_GROUP_CALL = 4;
	public static int NOT_EXIST_NEW_EDITION = 5;
	public static int EXIST_NEW_EDITION = 6;
	private int APK_INSTALL = 7;
	public static int CURRENT_DOWNLOAD_PROCESS = 8;
	private TextView my_num_textview;
	private long local_process = 0;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == APK_INSTALL) {
				Toast.makeText(IdtSetting.this, "下载成功", Toast.LENGTH_SHORT).show();
				File apkfile = new File(APK_INSTALL_STRING_PATH);
				if (!apkfile.exists()) {
					return;
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
				IdtSetting.this.startActivity(i);
			} else if (msg.what == ACTION_RECEIVE_GROUP_CALL) {
//				Bundle bundle = (Bundle) msg.getData();
//				callID = bundle.getInt(AppConstants.EXTRA_KEY_CALLID);
//				callee = bundle.getString(AppConstants.EXTRA_KEY_CALLEE);
//				caller = bundle.getString(AppConstants.EXTRA_KEY_CALLER);
//				status = bundle.getInt(AppConstants.EXTRA_KEY_CALL_STATUS);
				// 通知从这个地方走
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
				ringtone.play();
				playVibrate();
//				// tv_group_number.setText(caller);
//				MediaAttribute pAttr = new MediaAttribute();
//				pAttr.ucAudioRecv = 1;
//				pAttr.ucAudioSend = 0;
//				pAttr.ucVideoRecv = 0;
//				pAttr.ucVideoSend = 0;
//				IDSApiProxyMgr.getCurProxy().CallAnswer(callID, pAttr, 0);
//				// 存储本地
//				ContentValues contentValues = new ContentValues();
//				contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, caller);
//				contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
//				contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
//				contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
//				contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
//				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callee);
//				contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER,
//						SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
//				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE, 1);
//				contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
//				Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
//				try {
//					IdtApplication.getCurrentCall().setUri(uri);
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
			} else if (msg.what == NOT_EXIST_NEW_EDITION) {
				Toast.makeText(IdtSetting.this, "没有更新的版本", Toast.LENGTH_SHORT).show();
			} else if (msg.what == EXIST_NEW_EDITION) {
				CurrentDownload.APP_DOWNLOAD = true;
				Toast.makeText(IdtSetting.this, "存在更新的版本，正在下载,请在通知栏查看下载进度", Toast.LENGTH_SHORT).show();
				createNotifacation();
			} else if(msg.what == CURRENT_DOWNLOAD_PROCESS){
				downloadProcess();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_ui_idt_setting_activity);
		IdtApplication.getInstance().addActivity(this);
		initView();
		initData();
	}
	
	private NotificationManager notificationManager;
	private Notification updateNotification;
	// 创建通知栏
	private void createNotifacation() {
		// 初始化通知管理器
		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		this.updateNotification = new Notification();
		updateNotification.icon = R.drawable.new_ui_logo;
		updateNotification.tickerText="移动呼叫开始下载";
		updateNotification.contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
		updateNotification.contentView.setProgressBar(R.id.content_view_progress, 100, 0, false);
		updateNotification.contentView.setTextViewText(R.id.content_view_text1, "0%");
		// 发出通知
		notificationManager.notify(1, updateNotification);
	}
	
	// 通知栏进度动态显示
	private void downloadProcess() {
		updateNotification.contentView.setProgressBar(R.id.content_view_progress, 100, (int) local_process, false);
		updateNotification.contentView.setTextViewText(R.id.content_view_text1, (int) local_process + "%");
		notificationManager.notify(1, updateNotification);
	}

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
							Toast.makeText(IdtSetting.this, "我在"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+"对讲组内发起对讲", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(IdtSetting.this, "请先选择一个对讲组", Toast.LENGTH_SHORT).show();
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
					((NotificationManager) IdtSetting.this.getSystemService(Context.NOTIFICATION_SERVICE))
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

	public void initView() {
		listview = (ListView) findViewById(R.id.idt_setting_page_listview);
		page_title_name = (TextView) findViewById(R.id.page_title_name);
		ftpBuinessLayer = new FtpBuinessLayer(IdtSetting.this);
		ftpBuinessLayer.setListener(this);
		page_title_name.setText("设置");
		intercom_state_textview = (TextView) findViewById(R.id.intercom_state_textview);
		intercom_image_button = (ImageButton) findViewById(R.id.intercom_button);
		intercom_image_button.setOnLongClickListener(this);
		intercom_image_button.setOnTouchListener(new MyClickListener());
		my_num_textview = (TextView) findViewById(R.id.my_num);
		my_num_textview.setText(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
	}

	public void initData() {
		arrayAdapter = new ArrayAdapter<String>(IdtSetting.this, R.layout.idt_setting_listview_item);
		arrayAdapter.add("离线地图");
		arrayAdapter.add("软件更新");
		arrayAdapter.add("关于我们");
		arrayAdapter.add("注销登录");
		listview.setAdapter(arrayAdapter);
		listview.setOnItemClickListener(this);
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtSetting.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtSetting.this.getApplication();
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

	private void softwareUpdate() {
		CommonUtils netStatus = new CommonUtils();
		if (netStatus.isConnectingToInternet(IdtSetting.this)) {
			// 启动与JNI相关的监护和被监护程序
			if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				// 存在sd卡
				Toast.makeText(IdtSetting.this, "您的手机没有SD卡", Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(IdtSetting.this, "正在检查当前软件版本", Toast.LENGTH_SHORT).show();
			ftpBuinessLayer.searchFromFtpAndDownload(SERVER_FILE_PATH, LOCAL_PATH);
		} else {
			Toast.makeText(IdtSetting.this, "没有可以使用的网络，请打开您的网络", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtSetting.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtSetting.this.getApplication();
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

	private void logOff() {
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_number", "");
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_password", "");
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "server_ip", "");
		SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "remember_passwd_checkbox_open",
				false);
		SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "auto_login_checkbox_open", false);
		SharedPreferencesUtil.setBooleanPreferences(this, "update_switch_open", false);
//		IDSApiProxyMgr.getCurProxy().Exit();
//		IDSApiProxyMgr.getCurProxy().unloadLibrary(IdtSetting.this);
//		IdtApplication.getInstance().clearAllActivity();
//		Intent intent = new Intent(IdtSetting.this, IdtLogin.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
		outTheApp();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (arrayAdapter.getItem(position).equals("关于我们")) {
			Intent intent = new Intent(IdtSetting.this, AboutOurActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (arrayAdapter.getItem(position).equals("软件更新")) {
			if (CurrentDownload.APP_DOWNLOAD == true) {
				Toast.makeText(IdtSetting.this, "APK已经在下载了，请在通知栏中查看下载进度", Toast.LENGTH_SHORT).show();
			} else {
				new CustomDialog.Builder(this).setTitle("温馨提示").setMessage("您确定进行软件更新吗？")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case AlertDialog.BUTTON_POSITIVE:
									softwareUpdate();
									if (dialog != null) {
										dialog.dismiss();
									}
									break;
								case AlertDialog.BUTTON_NEGATIVE:
									if (dialog != null) {
										dialog.dismiss();
									}
									break;
								default:
									break;
								}
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case AlertDialog.BUTTON_POSITIVE:
									softwareUpdate();
									if (dialog != null) {
										dialog.dismiss();
									}
									break;
								case AlertDialog.BUTTON_NEGATIVE:
									if (dialog != null) {
										dialog.dismiss();
									}
									break;
								default:
									break;
								}
							}
						}).create().show();
			}
		} else if (arrayAdapter.getItem(position).equals("注销登录")) {

			new CustomDialog.Builder(this).setTitle("温馨提示").setMessage("您确定注销登录当前账号吗？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							switch (which) {
							case AlertDialog.BUTTON_POSITIVE:
								logOff();
								if (dialog != null) {
									dialog.dismiss();
								}
								break;
							case AlertDialog.BUTTON_NEGATIVE:
								if (dialog != null) {
									dialog.dismiss();
								}
								break;
							default:
								break;
							}
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if (dialog != null) {
								dialog.dismiss();
							}
						}
					}).create().show();
		}
		if (arrayAdapter.getItem(position).equals("离线地图")) {
			Intent intent = new Intent(IdtSetting.this, IdtMapOffline.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		// else if (arrayAdapter.getItem(position).equals("文件上传")) {
		// LwtLog.d("wulin", "文件上传栏被点中");
		// // FtpBuinessLayer ftpBuinessLayer=new FtpBuinessLayer(this);
		// // ftpBuinessLayer.uploadFile();
		// }
	}

	// public void checkUpgrade() {
	// AppUpdateManager appUpdateManager = new AppUpdateManager(this);
	// appUpdateManager.checkUpdate();
	// }

	// public void searchFtp() {
	// FtpManager appUpdateManager = new FtpManager(this);
	// appUpdateManager.searchFtpServerFiles();
	// }

	private long mExitTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(IdtSetting.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				outTheApp();
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.map:
			// 点击地图图标
			Intent intent = new Intent(IdtSetting.this, IdtMap.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	public void outTheApp() {
		// 退出
		IDSApiProxyMgr.getCurProxy().Exit();
		LwtLog.d("wulin", ">>>> 已退出并注销。");
		// 发一个通知给父类，让父类去终结所有的Activity
		Intent intent = new Intent();
		intent.setAction(ActivityBase.ACTION_QUIT_APPLICATION);
		sendBroadcast(intent);
	}

	@Override
	public void fileUploadSuccess(int style, Uri uri, String callto_persion_num, String filename) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileDownloadSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void apkDownloadCase(String case_status, String install_apk_path,long process) {
		// TODO Auto-generated method stub
		if (case_status.equals(FtpBuinessLayer.FTP_DOWN_SUCCESS)) {
//			waitDialog.cancel();
			APK_INSTALL_STRING_PATH = install_apk_path;
			CurrentDownload.APP_DOWNLOAD = false;
			handler.sendEmptyMessage(APK_INSTALL);
			return;
		}else if(case_status.equals(FtpBuinessLayer.FTP_DOWN_LOADING)){
			local_process = process;
			handler.sendEmptyMessage(CURRENT_DOWNLOAD_PROCESS);
			return;
		}
	}

	// 收到组呼请求
	@Override
	public void receiveGroupRequest(int ID, String pcMyNum, String pcPeerNum, int status) {
		// TODO Auto-generated method stub
		super.receiveGroupRequest(ID, pcMyNum, pcPeerNum, status);
		Log.d("mymap",
				"---------------------------" + ID + "---" + pcMyNum + "--------" + pcPeerNum + "-------" + status);
		Message msg = new Message();
		msg.what = ACTION_RECEIVE_GROUP_CALL;
//		Bundle bundle = new Bundle();
//		bundle.putInt(AppConstants.EXTRA_KEY_CALLID, ID);
//		bundle.putString(AppConstants.EXTRA_KEY_CALLEE, pcMyNum);
//		bundle.putString(AppConstants.EXTRA_KEY_CALLER, pcPeerNum);
//		bundle.putInt(AppConstants.EXTRA_KEY_CALL_STATUS, status);
//		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	@Override
	public void noExitNewEdition() {
		// TODO Auto-generated method stub
		handler.sendEmptyMessage(NOT_EXIST_NEW_EDITION);
	}

	@Override
	public void exitNewEdition() {
		// TODO Auto-generated method stub
		handler.sendEmptyMessage(EXIST_NEW_EDITION);
	}
}
