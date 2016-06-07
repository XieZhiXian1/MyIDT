package com.ids.idtma;

import java.io.File;

import com.ids.idtma.IdtSetting.MyClickListener;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.map.IdtMap;
import com.ids.idtma.provider.ProviderBuinessLayer;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.AppVersion;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AboutOurActivity extends ActivityBase implements OnLongClickListener {
	private TextView verSionTextView;
	private TextView commany_indroduce, about;
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	private TextView intercom_state_textview;
	private ImageButton intercom_image_button;
	public static int ACTION_RECEIVE_GROUP_CALL = 4;
//	private TextView my_num_textview;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == ACTION_RECEIVE_GROUP_CALL) {
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
//				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
//				contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
//				Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
//				try {
//					IdtApplication.getCurrentCall().setUri(uri);
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_our);
		IdtApplication.getInstance().addActivity(this);
		initLayout();
	}

	public void initLayout() {
		verSionTextView = (TextView) findViewById(R.id.version);
		verSionTextView.setText(getCurrentVersionName());
		commany_indroduce = (TextView) findViewById(R.id.commany_indroduce);
		commany_indroduce.setMovementMethod(ScrollingMovementMethod.getInstance());
		about = (TextView) findViewById(R.id.page_title_name);
		about.setText("关于");
		intercom_state_textview = (TextView) findViewById(R.id.intercom_state_textview);
		intercom_image_button = (ImageButton) findViewById(R.id.intercom_button);
		intercom_image_button.setOnLongClickListener(this);
		intercom_image_button.setOnTouchListener(new MyClickListener());
		String lock_group_num = SharedPreferencesUtil.getStringPreference(AboutOurActivity.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) AboutOurActivity.this.getApplication();
			if (idtApplication.getLstGroups().size() != 0) {
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM = idtApplication.getLstGroups().get(0).getUcNum();
			}
		}
		if(IdtApplication.getCurrentCall()!=null && IdtApplication.getCurrentCall().getType()==CallType.GROUP_CALL && CurrentGroupCall.CALL_OK == true){
			intercom_state_textview.setText("对讲组名："+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
		}else{
			intercom_state_textview.setText("对讲结束");
		}
//		my_num_textview = (TextView) findViewById(R.id.my_num);
//		my_num_textview.setText(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
	}

	public String getCurrentVersionName() {
		String versionName = "";
		versionName = new AppVersion(this).getVersionName();
		return versionName;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.map:
			// 点击地图图标
			Intent intent = new Intent(AboutOurActivity.this, IdtMap.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		case R.id.return_button: {
			// 点击屏幕上方返回键
			IdtApplication.getInstance().deleteActivity(this);
			}
		default:
			break;
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(AboutOurActivity.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) AboutOurActivity.this.getApplication();
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
							Toast.makeText(AboutOurActivity.this, "我在"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+"对讲组内发起对讲", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(AboutOurActivity.this, "请先选择一个对讲组", Toast.LENGTH_SHORT).show();
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
					((NotificationManager) AboutOurActivity.this.getSystemService(Context.NOTIFICATION_SERVICE))
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
}
