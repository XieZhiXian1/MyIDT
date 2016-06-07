package com.ids.idtma.chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.entity.CalllogEntity;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.provider.CalllogProvider;
import com.ids.idtma.provider.ProviderBuinessLayer;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

public class ActivityAudioCall extends ActivityBase implements OnClickListener, Runnable {
	private static String TAG = ActivityAudioCall.class.getName();

	AudioManager audioManager;
	boolean isSpeakerphoneOn = false;
	boolean isMute = false;
	Chronometer chronometer;
	// 主叫挂机
	ImageButton btn_hangup;
	// 被叫
	ImageButton ll_answer, ll_hangup;
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码

	TextView tv_call_number;
	TextView tv_calling_hint, current_time_textview;

	private int status;
	private int notificationFlag;
	public final static int FLAG_INCOMING = 0;// 来电
	public final static int FLAG_CALLING = 1;// 正在呼叫
	public final static int FLAG_ANSWER = 2;// 接听
	public static final int SHOW_CURRENT_TIME = 3;
	private Uri uri;
	// 定义NotificationManager
	NotificationManager mNotificationManager;
	private boolean THREAD_RUN = true;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == SHOW_CURRENT_TIME) {
				String current_time = (String) msg.obj;
				current_time_textview.setText(current_time);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_ui_activity_audio_call);
		LwtLog.d(TAG, "onCreate");
		IdtApplication.getInstance().addActivity(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// 主叫挂机
		btn_hangup = (ImageButton) findViewById(R.id.btn_hangup);
		btn_hangup.setOnClickListener(this);
		ll_answer = (ImageButton) findViewById(R.id.ll_answer);
		ll_answer.setOnClickListener(this);
		ll_hangup = (ImageButton) findViewById(R.id.ll_hangup);
		ll_hangup.setOnClickListener(this);

		chronometer = (Chronometer) this.findViewById(R.id.chronometer_call_time);
		current_time_textview = (TextView) findViewById(R.id.current_time);
		// 实时显示时间
		new Thread(this).start();
		
		// 获取参数
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		callID = getIntent().getIntExtra(AppConstants.EXTRA_KEY_CALLID, -1);
		// 主叫号码
		caller = getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLER);
		// call_to_persion num对方号码
		callee = getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLEE);
		status = getIntent().getIntExtra(AppConstants.EXTRA_KEY_CALL_STATUS, -1);
		notificationFlag = getIntent().getIntExtra(AppConstants.EXTRA_KEY_NOTIFICATION_INTENT, -1);
		tv_calling_hint = (TextView) findViewById(R.id.tv_calling_hint);
		tv_call_number = (TextView) findViewById(R.id.tv_call_number);
		ContentValues contentValues;
		switch (status) {
		case FLAG_INCOMING:
			// 来电进入
			// 显示对方号码
			tv_call_number.setText(caller);
			tv_calling_hint.setText("来电");
			// 操作视图
			setCalleeMode();
			// 当不是从notification中产生的activity，那么不存储呼叫信息，要不然进行存储
			if (notificationFlag == -1) {
				// 播放震铃
				IdtApplication.playRingtone();
				// contentValues = new ContentValues();
				// //储存主叫电话 比如：2052
				// contentValues.put(CalllogProvider.KEY_COLUMN_1_PHONE_NUMBER,
				// caller);
				// //还未接
				// contentValues.put(CalllogProvider.KEY_COLUMN_2_CALL_TYPE,
				// CalllogEntity.CalllogType.MISSED.value());
				// //创建时间
				// contentValues.put(CalllogProvider.KEY_COLUMN_3_CREATE_TIME,
				// System.currentTimeMillis());
				// //储存主叫姓名 比如：2052
				// contentValues.put(CalllogProvider.KEY_COLUMN_4_NAME, caller);
				// uri =
				// getContentResolver().insert(CalllogProvider.CONTENT_URI,
				// contentValues);

				contentValues = new ContentValues();
				contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, caller);
				contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
				contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
				contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 7);
				contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callee);
				contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER,
						SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
				contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
				Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
				try {
					IdtApplication.getCurrentCall().setUri(uri);
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {
				// 通知进行的启动activity，首先获取notification里面的数据
				uri = Uri.parse(getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLLOG_URI));
			}
			break;
		case FLAG_CALLING:
			// 呼叫出去
			// 对界面视图进行操作
			setCallerMode();
			tv_calling_hint.setVisibility(View.VISIBLE);
			tv_call_number.setText(callee);
			if (notificationFlag == -1) {
				// // 将通话记录储存起来
				// contentValues = new ContentValues();
				// // 电话号码
				// contentValues.put(CalllogProvider.KEY_COLUMN_1_PHONE_NUMBER,
				// callee);
				// // 呼叫类型 打出去
				// contentValues.put(CalllogProvider.KEY_COLUMN_2_CALL_TYPE,
				// CalllogEntity.CalllogType.OUT.value());
				// // 创建时间
				// contentValues.put(CalllogProvider.KEY_COLUMN_3_CREATE_TIME,
				// System.currentTimeMillis());
				// // 呼出给谁
				// contentValues.put(CalllogProvider.KEY_COLUMN_4_NAME, callee);
				// // 向ContentProvider中写入数据
				// getContentResolver().insert(CalllogProvider.CONTENT_URI,
				// contentValues);
				// // 将通话记录储存起来
				contentValues = new ContentValues();
				contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, caller);
				contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
				contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
				contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 7);
				contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callee);
				contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER,
						SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
				contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
				Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
				try {
					IdtApplication.getCurrentCall().setUri(uri);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			break;
		// 只有在有notification的时候才会调用这个地方？
		case FLAG_ANSWER:
			setCallerMode();
			tv_calling_hint.setVisibility(View.INVISIBLE);
			if (null == caller || "".equals(caller))
				tv_call_number.setText(callee);
			else
				tv_call_number.setText(caller);
			if (notificationFlag != -1) {
				// startChronometer(SystemClock.elapsedRealtime());
				chronometer.setVisibility(View.VISIBLE);
				chronometer.setFormat("通话时长：%s");
				chronometer.setBase(IdtApplication.getCurrentCall().getChronometer().getBase());
				chronometer.start();
			}
			break;
		}
	}

	// 点击返回键
	@Override
	public void onBackPressed() {
		minimizationActivity();
		super.onBackPressed();
	}

	@Override
	protected void onUserLeaveHint() {
		minimizationActivity();
		moveTaskToBack(true);// true对任何Activity都适用
		super.onUserLeaveHint();
	}

	// 当在通话的时候，点击返回键，那么当前通话以通知的形式显示
	private void minimizationActivity() {
		// 消息通知栏
		CharSequence tickerText = "";
		CharSequence contentTitle = "";

		switch (status) {
		case FLAG_CALLING:
			tickerText = "呼叫";// 状态栏(Status Bar)显示的通知文本提示
			contentTitle = "正在呼叫...";
			break;
		case FLAG_INCOMING:
			tickerText = "来电";
			contentTitle = "新的来电";
			break;
		case FLAG_ANSWER:
			tickerText = "通话中";
			contentTitle = "正在通话...";
			break;
		default:
			break;
		}
		// 定义通知栏展现的内容信息
		int icon = R.drawable.notification_call;
		long when = System.currentTimeMillis();// 通知产生的时间，会在通知信息里显示
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// 定义下拉通知栏时要展现的内容信息
		CharSequence contentText = callee;
		// 点击跳转到本类
		Intent notificationIntent = new Intent(getApplicationContext(), ActivityAudioCall.class);
		// 获取到存放在当前通话对象里面的数据
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLID, IdtApplication.getCurrentCall().getCallid());
		// 主叫号码
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLER, caller);
		// 被叫号码
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLEE, callee);
		// 当时点击了通知栏跳转到该界面的话，会存在这个变量
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_NOTIFICATION_INTENT, 0);
		// 呼叫状态和最开始一样
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, status);
		// 当为呼入的时候并且uri不为空
		// 这个是在为呼入状态下点击通知特有的一个状态
		if (null != uri && status == FLAG_INCOMING)
			notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLLOG_URI, uri.toString());
		IdtApplication.getCurrentCall().setIntent(notificationIntent);
		// 给当前呼叫类给一个计时器控件
		IdtApplication.getCurrentCall().setChronometer(chronometer);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
				AppConstants.CALL_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);// 更新Notification
		// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
		mNotificationManager.notify(AppConstants.CALL_NOTIFICATION_ID, notification);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 点击接听后 当前状态转换为接听状态
		case R.id.ll_answer:
			status = FLAG_ANSWER;
			// 停止铃声和震动
			IdtApplication.stopRingtone();
			// 界面显示设置
			setCallerMode();
			// 将计时器清零并启动
			startChronometer(0);

			MediaAttribute pAttr = new MediaAttribute();
			pAttr.ucAudioRecv = 1;
			pAttr.ucAudioSend = 1;
			pAttr.ucVideoRecv = 0;
			pAttr.ucVideoSend = 0;
			// 点击确定接听以后，需要向so库发送一个接听信号指令
			// callID主叫电话 2053
			IDSApiProxyMgr.getCurProxy().CallAnswer(callID, pAttr, 0);

			if (null != uri) {
				// 更新刚刚插入的数据，最开始还是未接状态
				ContentValues contentValues = new ContentValues();
				contentValues.put(CalllogProvider.KEY_COLUMN_2_CALL_TYPE, CalllogEntity.CalllogType.OUT.value());
				LwtLog.d(TAG, ">>>>>> update calllog :" + uri.toString());
				getContentResolver().update(uri, contentValues, null, null);
			}
			break;
		case R.id.ll_hangup:// 被叫挂机 接听方挂掉电话
			// 停止震动
			IdtApplication.stopRingtone();
			String between_time_string = chronometer.getText().toString();
			Log.d("between_time_string", "通话时长："+between_time_string);
			if(!between_time_string.equals("0:00") && !between_time_string.equals("00:00")){
				ProviderBuinessLayer providerBuinessLayer = new ProviderBuinessLayer(this);
				int between_time = (int) ((SystemClock.elapsedRealtime()-chronometer.getBase())/1000);
				providerBuinessLayer.update_sms_resource_time_length((Uri)uri, between_time);
			}
			// 停止计时
			chronometer.stop();
			mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
			IDSApiProxyMgr.getCurProxy().CallRel(IdtApplication.getCurrentCall().getCallid(), 0, 0);
			IdtApplication.setCurrentCall(null);
			LwtLog.d(TAG, "IDTNativeApi.IDT_CallRel 已挂机 >>>>>>>>>>>> callId : " + callID);
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.btn_hangup:// 主叫挂机：拨打方挂掉电话
			IdtApplication.stopRingtone();
			String between_time_string1 = chronometer.getText().toString();
			Log.d("between_time_string", "通话时长："+between_time_string1);
			if(!between_time_string1.equals("0:00") && !between_time_string1.equals("00:00")){
				ProviderBuinessLayer providerBuinessLayer1 = new ProviderBuinessLayer(this);
				int between_time1 = (int) ((SystemClock.elapsedRealtime()-chronometer.getBase())/1000);
				providerBuinessLayer1.update_sms_resource_time_length(IdtApplication.getCurrentCall().getUri(), between_time1);
			}
			// 停止计时
			chronometer.stop();
			// 取消该activity对应的通知栏
			mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
			// 告诉so库，你要取消当前通话 callID是通话id
			IDSApiProxyMgr.getCurProxy().CallRel(IdtApplication.getCurrentCall().getCallid(), 0, 0);
			// 将当前呼叫置为空
			IdtApplication.setCurrentCall(null);
			LwtLog.d(IdtApplication.WULIN_TAG, "IDTNativeApi.IDT_CallRel 已挂机 >>>>>>>>>>>> callId : " + callID);
			setResult(RESULT_OK);
			// 结束当前Activity
			finish();
			break;
		default:
			break;
		}
	}

	private void startChronometer(long elapsedRealtime) {
		// 将计时器清零
		chronometer.setVisibility(View.VISIBLE);
		if (elapsedRealtime == 0)
			elapsedRealtime = SystemClock.elapsedRealtime();
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.setFormat("通话时长：%s");
		// 开始计时
		chronometer.start();
	}

	/**
	 * 设置界面是主叫模式
	 */
	private void setCallerMode() {
		tv_calling_hint.setVisibility(View.INVISIBLE);
		findViewById(R.id.ll_answer).setVisibility(View.GONE);
		findViewById(R.id.new_ui_video_call_divide0).setVisibility(View.GONE);
		findViewById(R.id.ll_hangup).setVisibility(View.GONE);
		findViewById(R.id.new_ui_video_call_divide1).setVisibility(View.GONE);
		findViewById(R.id.btn_hangup).setVisibility(View.VISIBLE);
		tv_calling_hint.setText("正在语音呼叫 ... ");
	}

	/**
	 * 设置界面是被叫模式
	 */
	private void setCalleeMode() {
		tv_calling_hint.setVisibility(View.VISIBLE);
		findViewById(R.id.ll_answer).setVisibility(View.VISIBLE);
		findViewById(R.id.new_ui_video_call_divide0).setVisibility(View.VISIBLE);
		findViewById(R.id.ll_hangup).setVisibility(View.VISIBLE);
		findViewById(R.id.new_ui_video_call_divide1).setVisibility(View.GONE);
		findViewById(R.id.btn_hangup).setVisibility(View.GONE);
		tv_calling_hint.setText("邀请您语音通话 ... ");

	}
	// 对端应答so库调用本地service里面方法，发送广播，然后调用这个方法
	@Override
	public void onCallPeerAnswer() {
		// TODO Auto-generated method stub
		super.onCallPeerAnswer();
		LwtLog.d(IdtApplication.WULIN_TAG, "ActivityAudioCall对端应答 >>>>>>>>>>>> onCallPeerAnswer()");
		setCallerMode();
		status = FLAG_ANSWER;
		// 将计时器清零并启动
		startChronometer(0);
	}

	// 当主叫方挂掉电话，那么在本页面会回调onCallRelInd这个函数
	@Override
	public void onCallRelInd(int ID,int uiCause,Parcelable uri) {
		// TODO Auto-generated method stub
		LwtLog.d(IdtApplication.WULIN_TAG, "ActivityAudioCall远端释放 >>>>>>>>>>>> onCallRelInd()");
		String between_time_string = chronometer.getText().toString();
		Log.d("between_time_string", "通话时长："+between_time_string);
		if(!between_time_string.equals("0:00") && !between_time_string.equals("00:00")){
			ProviderBuinessLayer providerBuinessLayer = new ProviderBuinessLayer(this);
			int between_time = (int) ((SystemClock.elapsedRealtime()-chronometer.getBase())/1000);
			providerBuinessLayer.update_sms_resource_time_length((Uri)uri, between_time);
		}
		//停止计时器
		chronometer.stop();
		IdtApplication.stopRingtone();
		mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		THREAD_RUN = false;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (THREAD_RUN) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
			String str = sdf.format(new Date());
			handler.sendMessage(handler.obtainMessage(SHOW_CURRENT_TIME, str));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
