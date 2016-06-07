package com.ids.idtma;

import com.baidu.mapapi.SDKInitializer;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.provider.ProviderBuinessLayer;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.SpeakerPhone;
import com.ids.proxy.IDSApiProxyMgr;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;

public class ActivityBase extends Activity {
	/**
	 * 登录成功
	 */
	public final static String IDT_LOGIN_SUCCESS = "com.ids.idtma.intent.action.login";

	/**
	 * 收到富文本消息
	 */
	public final static String IDT_RECEIVE_RICHMESSAGE = "com.ids.idtma.intent.action.receiverichmessage";

	/**
	 * 获取到文件名
	 */
	public final static String IDT_GET_FILE_NAME = "com.ids.idtma.intent.action.gitfilename";
	/**
	 * 获取到组和组成员消息
	 */
	public final static String ACTION_RECEIVE_GROUP_DATA = "com.ids.idtma.intent.action.getGroupanduserdata";
	/**
	 * 获取到gps信息
	 */
	public final static String RECEIVE_GPS_MESSAGE = "com.ids.idtma.intent.action.receivegpsmessage";

	public final static String IM_DOWNLOAD_SUCCESS = "com.ids.idtma.intent.action.imdownloadsuccess";

	public final static String USER_STATUS_CHANGE = "com.ids.idtma.intent.action.userstatuschange";
	// 退出应用
	public final static String ACTION_QUIT_APPLICATION = "intent.filter.quit.application";
	// 收到组呼请求
	public final static String ACTION_RECEIVE_GROUP_CALL = "com.ids.idtma.intent.action.receivegroupcall";
	// 码流
	public final static String GET_STREAM = "com.ids.idtma.intent.action.getstream";
	//控制视频的收发
	public final static String IDT_VIDEO_CODEC_STATUS = "com.ids.idtma.intent.action.videocodecstatus";
	//获得话权
	public final static String ACTION_CALL_GET_MIC = "com.ids.idtma.intent.action.callgetmic";
	

	private BroadcastReceiver activityBaseBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// 设为不会屏幕变暗
			PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = powerManager
					.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Gank");
			wakeLock.acquire();

			LwtLog.d(IdtApplication.WULIN_TAG, "收到广播消息 >>>>>>>>>>>>>" + intent.getAction());
			if (IDT_LOGIN_SUCCESS.equals(intent.getAction())) {
				// 登录成功
				loginSuccess();
			} else if (ACTION_RECEIVE_GROUP_DATA.equals(intent.getAction())) {
				// 收到一个组的所有成员消息
				getGroupAndUserData();
			} else if (AppConstants.ACTION_CALL_PEER_ANSWER.equals(intent.getAction())) {
				// 对方接听了电话，我端要进行相应操作
				onCallPeerAnswer();
			} else if (AppConstants.ACTION_CALL_REL_IND.equals(intent.getAction())) {
//				int uiCause = intent.getIntExtra("uiCause", -1);
				LwtLog.d(IdtApplication.WULIN_TAG, "IDT_CallRelInd 222222");
				// 对端应答
				onCallRelInd(intent.getIntExtra("ID", -1), intent.getIntExtra("uiCause", -1),intent.getParcelableExtra("uri"));
			} else if (AppConstants.ACTION_RECEIVE_VIDEO_DATA.equals(intent.getAction())) {
				// 收到对方视频数据
				onReceiveVideoData(intent.getByteArrayExtra(AppConstants.EXTRA_KEY_RECEIVE_VIDEO_DATA));
			} else if (AppConstants.ACTION_CALL_TALKING_TIPS.equals(intent.getAction())) {
				// 当存在话权方指示的时候
				onCallTalkingTips(intent.getStringExtra(AppConstants.EXTRA_KEY_TALKING_USERNAME),
						intent.getStringExtra(AppConstants.EXTRA_KEY_TALKING_PHONE));
			} else if (IDT_RECEIVE_RICHMESSAGE.equals(intent.getAction())) {
				// 收到富文本消息
				receiveRichMessage(intent.getStringExtra("SMS_RESOURCE_URL"), intent.getIntExtra("dwType", -1),
						intent.getStringExtra("pcFileName"));
			} else if (IDT_GET_FILE_NAME.equals(intent.getAction())) {
				// 向服务器请求的文件名得到了响应
				getFileNameFromServer(intent.getStringExtra("file_name"));
			} else if (IM_DOWNLOAD_SUCCESS.equals(intent.getAction())) {
				// 向服务器请求的文件名得到了响应
				receiveIMDownloadSuccess((Uri) intent.getParcelableExtra("uri"));
			} else if (RECEIVE_GPS_MESSAGE.equals(intent.getAction())) {
				// 获取到gps信息
				receiveGpsMessage();
			} else if (USER_STATUS_CHANGE.equals(intent.getAction())) {
				// 用户状态改变
				userStatusChange();
			} else if (ACTION_QUIT_APPLICATION.equals(intent.getAction())) {
				// 卸载so库
				LwtLog.d("wulin", ">>>> 卸载so库。");
				IDSApiProxyMgr.getCurProxy().unloadLibrary(context);
				// 结束所有的Activity
				IdtApplication.getInstance().clearAllActivity();
				final Context appContext = context.getApplicationContext();
				// 关闭扬声器
				SpeakerPhone speakerPhone = new SpeakerPhone(appContext);
				speakerPhone.CloseSpeaker();
				// 杀死进程、退出程序
				new Thread() {
					public void run() {
						try {
							Thread.sleep(100);
						} catch (Throwable e) {

						}
						ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
						String packageName = appContext.getPackageName();
						// 删除该包名相关的所有后台进程
						am.killBackgroundProcesses(packageName);
						// 删除本进程
						android.os.Process.killProcess(android.os.Process.myPid());
						System.exit(0);
					}
				}.start();
			} else if (ACTION_RECEIVE_GROUP_CALL.equals(intent.getAction())) {
				receiveGroupRequest(intent.getIntExtra(AppConstants.EXTRA_KEY_CALLID, -1),
						intent.getStringExtra(AppConstants.EXTRA_KEY_CALLEE),
						intent.getStringExtra(AppConstants.EXTRA_KEY_CALLER),
						intent.getIntExtra(AppConstants.EXTRA_KEY_CALL_STATUS, -1));
			} else if (GET_STREAM.equals(intent.getAction())) {
				// 收到码流提示
				receiveStream(intent.getIntExtra("uiRxUsrBytes", -1), intent.getIntExtra("uiTxUsrBytes", -1));
			}else if (IDT_VIDEO_CODEC_STATUS.equals(intent.getAction())) {
				//控制视频的收发
				changeVideoCodec(intent.getIntExtra("ucRecv", -1), intent.getIntExtra("ucSend", -1));
			}else if (ACTION_CALL_GET_MIC.equals(intent.getAction())) {
				//控制视频的收发
				getMic(intent.getIntExtra("uiInd", -1));
			}
		}
	};

	public void playVibrate() {

		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// 震动一次
		vibrator.vibrate(500);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		IntentFilter filter = new IntentFilter();
		// 该广播可以捕捉到
		// 退出应用广播
		// 只有在此处进行注册了，才可以
		filter.addAction(IDT_LOGIN_SUCCESS);
		filter.addAction(ACTION_RECEIVE_GROUP_DATA);
		filter.addAction(AppConstants.ACTION_CALL_PEER_ANSWER);
		filter.addAction(AppConstants.ACTION_CALL_REL_IND);
		filter.addAction(AppConstants.ACTION_RECEIVE_VIDEO_DATA);
		filter.addAction(AppConstants.ACTION_CALL_TALKING_TIPS);
		filter.addAction(IDT_RECEIVE_RICHMESSAGE);
		filter.addAction(IDT_GET_FILE_NAME);
		filter.addAction(IM_DOWNLOAD_SUCCESS);
		filter.addAction(RECEIVE_GPS_MESSAGE);
		filter.addAction(USER_STATUS_CHANGE);
		filter.addAction(ACTION_QUIT_APPLICATION);
		filter.addAction(ACTION_RECEIVE_GROUP_CALL);
		filter.addAction(GET_STREAM);
		filter.addAction(IDT_VIDEO_CODEC_STATUS);
		filter.addAction(ACTION_CALL_GET_MIC);
		registerReceiver(activityBaseBroadcastReceiver, filter);
	}

//	private void setCurrentCallNull(int uiCause) {
//		// 更新数据库uiCause字段值
//		try {
//			ProviderBuinessLayer providerBuinessLayer = new ProviderBuinessLayer(this);
//			Uri uri = IdtApplication.getCurrentCall().getUri();
//			providerBuinessLayer.update_ui_cause(uri, uiCause);
//			IdtApplication.setCurrentCall(null);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// 所有继承了该类的子类结束时候，自然会调用该方法
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LwtLog.d(IdtApplication.WULIN_TAG, "onDestroy() >>>> 删除广播注册");
		// 取消注册广播注册
		unregisterReceiver(activityBaseBroadcastReceiver);
	}

	public void loginSuccess() {

	}

	public void getGroupAndUserData() {

	}

	public void onCallPeerAnswer() {

	}

	public void onCallRelInd(int ID, int uiCause,Parcelable parcelable) {

	}

	public void onReceiveVideoData(byte[] data) {

	}

	public void onCallTalkingTips(String name, String phone) {

	}

	public void receiveRichMessage(String SMS_RESOURCE_URL, int dwType, String pcFileName) {

	}

	public void getFileNameFromServer(String file_name) {

	}

	public void receiveIMDownloadSuccess(Uri uri) {

	}

	public void receiveGpsMessage() {

	}

	public void userStatusChange() {

	}

	public void receiveGroupRequest(int ID, String pcMyNum, String pcPeerNum, int status) {

	}

	public void receiveStream(int uiRxUsrBytes, int uiTxUsrBytes) {

	}
	
	public void changeVideoCodec(int ucRecv,int ucSend){
		
	}
	
	public void getMic(int uiInd){
		
	}
}
