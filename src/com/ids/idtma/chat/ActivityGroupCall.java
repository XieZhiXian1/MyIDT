package com.ids.idtma.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.entity.CalllogEntity;
import com.ids.idtma.jni.IDTNativeApi;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.provider.CalllogProvider;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;

public class ActivityGroupCall extends ActivityBase implements OnClickListener, OnLongClickListener {
	private static String TAG = ActivityGroupCall.class.getName();
	int audioMode = AudioManager.MODE_IN_CALL;

	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	String groupCallNum;// 组号码
	TextView tv_group_number;
	TextView tv_talking_user;
	ImageButton btn_speak;
	ImageView iv_talk_listen_flag;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	public final static int FLAG_INCOMING = 0;// 来电
	public final static int FLAG_CALLING = 1;// 正在呼叫
	public final static int FLAG_ANSWER = 2;// 接听
	
	AudioManager audioManager;
	Chronometer chronometer;
	NotificationManager mNotificationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_call);
		IdtApplication.getInstance().addActivity(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		findViewById(R.id.btn_hangup).setOnClickListener(this);
//		findViewById(R.id.iv_micphone).setOnClickListener(this);
//		findViewById(R.id.iv_mute).setOnClickListener(this);
		iv_talk_listen_flag = (ImageView) findViewById(R.id.iv_call_flag);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// 获取用Intent传过来的数据
		// 组呼id
		callID = getIntent().getIntExtra(AppConstants.EXTRA_KEY_CALLID, -1);
		// 最开始为空
		caller = getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLER);
		// call_to_persion num对方号码
		callee = getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLEE);
		status = getIntent().getIntExtra(AppConstants.EXTRA_KEY_CALL_STATUS, -1);
		// 组呼电话号码
		groupCallNum = getIntent().getStringExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM);
		tv_group_number = (TextView) findViewById(R.id.tv_group_number);
		tv_group_number.setText(groupCallNum);
		tv_talking_user = (TextView) findViewById(R.id.tv_talking_user);

		btn_speak = (ImageButton) findViewById(R.id.btn_speak);
		btn_speak.setOnLongClickListener(this);
		btn_speak.setOnTouchListener(new MyClickListener());

		chronometer = (Chronometer) findViewById(R.id.chronometer_call_time);

		if (status == ActivityGroupCall.FLAG_INCOMING) {
			// 通知从这个地方走
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
			ringtone.play();
			playVibrate();
			startChronometer();
			tv_group_number.setText(caller);
			MediaAttribute pAttr = new MediaAttribute();
			pAttr.ucAudioRecv = 1;
			pAttr.ucAudioSend = 0;
			pAttr.ucVideoRecv = 0;
			pAttr.ucVideoSend = 0;
			IDSApiProxyMgr.getCurProxy().CallAnswer(callID, pAttr, 0);

			// ContentValues contentValues = new ContentValues();
			// contentValues.put(CalllogProvider.KEY_COLUMN_1_PHONE_NUMBER,
			// caller);
			// contentValues.put(CalllogProvider.KEY_COLUMN_2_CALL_TYPE,
			// CalllogEntity.CalllogType.IN.value());
			// contentValues.put(CalllogProvider.KEY_COLUMN_3_CREATE_TIME,
			// System.currentTimeMillis());
			// contentValues.put(CalllogProvider.KEY_COLUMN_4_NAME, caller);
			// getContentResolver().insert(CalllogProvider.CONTENT_URI,
			// contentValues);
			ContentValues contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, caller);
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
			contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callee);
			contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "") );
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else if(status== ActivityGroupCall.FLAG_CALLING){
			// 最开始进入这个界面从这里走
			iv_talk_listen_flag.setBackgroundResource(R.drawable.group_call_talk);
			btn_speak.setBackgroundResource(R.drawable.btn_speak_pressed);
			// 将该记录存储到contentprovider中
			// ContentValues contentValues = new ContentValues();
			// contentValues.put(CalllogProvider.KEY_COLUMN_1_PHONE_NUMBER,
			// groupCallNum);
			// contentValues.put(CalllogProvider.KEY_COLUMN_2_CALL_TYPE,
			// CalllogEntity.CalllogType.OUT.value());
			// contentValues.put(CalllogProvider.KEY_COLUMN_3_CREATE_TIME,
			// System.currentTimeMillis());
			// contentValues.put(CalllogProvider.KEY_COLUMN_4_NAME,
			// groupCallNum);
			// getContentResolver().insert(CalllogProvider.CONTENT_URI,
			// contentValues);
			ContentValues contentValues = new ContentValues();
			contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, caller);
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
			contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callee);
			contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "") );
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		// 通知调用该界面的时候，从这里走
		if (getIntent().getIntExtra(AppConstants.EXTRA_KEY_NOTIFICATION_INTENT, -1) != -1) {
			chronometer.setVisibility(View.VISIBLE);
			chronometer.setFormat("通话时长：%s");
			chronometer.setBase(IdtApplication.getCurrentCall().getChronometer().getBase());
			chronometer.start();
		}

	}

	@Override
	public void onBackPressed() {
		minimizationActivity();
		super.onBackPressed();
	}

	@Override
	protected void onUserLeaveHint() {
		minimizationActivity();
		moveTaskToBack(true);
		super.onUserLeaveHint();
	}

	//当点击返回键的时候进行最小化
	private void minimizationActivity() {
		// 消息通知栏
		CharSequence tickerText = "组呼通话中";
		CharSequence contentTitle = "正在通话...";

		// 定义通知栏展现的内容信息
		int icon = R.drawable.notification_call;

		long when = System.currentTimeMillis();// 通知产生的时间，会在通知信息里显示
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// 定义下拉通知栏时要展现的内容信息
		CharSequence contentText = callee;
		Intent notificationIntent = new Intent(getApplicationContext(), ActivityGroupCall.class);
		// 群呼id
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLID, IdtApplication.getCurrentCall().getCallid());
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLER, caller);
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM, groupCallNum);
		notificationIntent.putExtra(AppConstants.EXTRA_KEY_NOTIFICATION_INTENT, 0);

		IdtApplication.getCurrentCall().setIntent(notificationIntent);
		IdtApplication.getCurrentCall().setChronometer(chronometer);

		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
				AppConstants.CALL_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);// 更新Notification

		// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
		mNotificationManager.notify(AppConstants.CALL_NOTIFICATION_ID, notification);
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
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_hangup:
			// 挂断
			mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
			// PTYT被叫不能被挂机
			if (0 == IDSApiProxyMgr.getCurProxy().CallRel(IdtApplication.getCurrentCall().getCallid(), 0, 0)) {
				LwtLog.d(TAG, "IDTNativeApi.IDT_CallRel 已挂机 >>>>>>>>>>>> callId : " + callID);
				setResult(RESULT_OK);
				finish();
			}

			IdtApplication.setCurrentCall(null);
			break;
//		case R.id.iv_micphone:
//
//			break;
//		case R.id.iv_mute:
//
//			break;
		default:
			break;
		}
	}

	private void setSpeakerphoneOn(boolean on) {
		if (on) {
			audioManager.setSpeakerphoneOn(true);
		} else {
			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
			audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			// 把声音设定成Earpiece（听筒）出来，设定为正在通话中
			audioManager.setMode(AudioManager.MODE_IN_CALL);
		}
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
			btn_speak.setBackgroundResource(R.drawable.btn_speak_pressed);
			iv_talk_listen_flag.setBackgroundResource(R.drawable.group_call_talk);
			IDSApiProxyMgr.getCurProxy().CallMicCtrl(callID, true);
			LwtLog.d(TAG, ">>>>>>>>>>>>> 获取话权");
			break;
		case STATUS_LISTEN:
			btn_speak.setBackgroundResource(R.drawable.btn_speak_normal);
			iv_talk_listen_flag.setBackgroundResource(R.drawable.group_call_listen);
			IDSApiProxyMgr.getCurProxy().CallMicCtrl(callID, false);
			LwtLog.d(TAG, ">>>>>>>>>>>>> 释放话权");
			break;
		default:
			break;
		}
	}

	// 长按获取话权
	@Override
	public boolean onLongClick(View view) {
		switch (view.getId()) {
		case R.id.btn_speak:
			status = STATUS_TALK;
			setImageButtonBackground();
			break;
		default:
			break;
		}
		return true;
	}

	// A方发起群呼，当群呼建立起来以后，给A方一个回应
	@Override
	public void onCallPeerAnswer() {
		// TODO Auto-generated method stub
		super.onCallPeerAnswer();
		LwtLog.d(TAG, "对端应答 >>>>>>>>>>>> onCallPeerAnswer()");
		// 只要一打通，就置为听筒模式
		status = STATUS_LISTEN;
		// 将计时器清零并启动
		startChronometer();
	}

	// 根据现在存在话权方与否进行配置
	@Override
	public void onCallTalkingTips(String name, String phone) {
		LwtLog.d(TAG, "讲话方提示 >>>>>>>>>>>> onCallTalkingTips()");
		tv_talking_user.setVisibility(View.VISIBLE);
		if (phone == null || "".equals(phone)) {
			tv_talking_user.setText("主讲人员：空闲...等待话权");
		} else {
			tv_talking_user.setText("主讲人员：" + phone);
		}
	}

	// 远端释放的时候
	@Override
	public void onCallRelInd(int ID,int uiCause,Parcelable parcelable) {
		// TODO Auto-generated method stub
		LwtLog.d(TAG, "远端释放 >>>>>>>>>>>> onCallRelInd()");
		mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
		setResult(RESULT_OK);
		finish();
	}

	private void startChronometer() {
		// 将计时器清零
		chronometer.setVisibility(View.VISIBLE);
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.setFormat("通话时长：%s");
		// 开始计时
		chronometer.start();
	}
}