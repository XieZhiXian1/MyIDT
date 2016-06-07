package com.ids.idtma.meeting;

import com.alibaba.fastjson.JSON;
import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.IdtGroup;
import com.ids.idtma.R;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.MeetingMsgData;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.provider.SmsProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.JsonOperation;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.MeetingRefuseDialog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;

public class MeetingNotice extends ActivityBase implements MeetingRefuseDialog.Builder.Listener, OnClickListener,OnLongClickListener {
	private MeetingMsgData receiveMeetingMsgData;
	private TextView meeting_name_textview, promoter_name, notice_time, issue;
	private Button meeting_accept_button, meeting_refuse_button;
	private String local_my_phone_number = "", local_callto_group_num = "";
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	private TextView intercom_state_textview;
	private ImageButton intercom_image_button;
	private String my_phone_number = "";
//	private TextView my_num_textview;
//	public static String callto_group_num = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_ui_meeting_notice);
		IdtApplication.getInstance().addActivity(this);
		initView();
		initData();
	}

	private void initView() {
		meeting_name_textview = (TextView) findViewById(R.id.page_title_name);
		promoter_name = (TextView) findViewById(R.id.editText1);
		notice_time = (TextView) findViewById(R.id.editText2);
		issue = (TextView) findViewById(R.id.editText4);
		meeting_accept_button = (Button) findViewById(R.id.meeting_accept_button);
		meeting_refuse_button = (Button) findViewById(R.id.meeting_refuse_button);
		meeting_accept_button.setOnClickListener(this);
		meeting_refuse_button.setOnClickListener(this);
		intercom_state_textview = (TextView) findViewById(R.id.intercom_state_textview);
		intercom_image_button = (ImageButton) findViewById(R.id.intercom_button);
		intercom_image_button.setOnLongClickListener(this);
		intercom_image_button.setOnTouchListener(new MyClickListener());
		my_phone_number = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
		String lock_group_num = SharedPreferencesUtil.getStringPreference(MeetingNotice.this, "lock_group_num", "#");
		if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		}else if (!lock_group_num.equals("#")) {
//			IdtGroup.callto_group_num = lock_group_num;
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) MeetingNotice.this.getApplication();
			if (idtApplication.getLstGroups().size() != 0) {
//				IdtGroup.callto_group_num = idtApplication.getLstGroups().get(0).getUcNum();
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM = idtApplication.getLstGroups().get(0).getUcNum();
			}
		}
		if (IdtApplication.getCurrentCall() != null
				&& IdtApplication.getCurrentCall().getType() == CallType.GROUP_CALL && CurrentGroupCall.CALL_OK == true) {
			intercom_state_textview.setText(
					"对讲组名：" + CurrentGroupCall.CURRENT_GROUP_CALL_NUM + CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
		} else {
			intercom_state_textview.setText("对讲结束");
		}
//		my_num_textview = (TextView) findViewById(R.id.my_num);
//		my_num_textview.setText(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
	}
	
	// 点击组呼语音电话图标
		public void groupAudioStart() {
			// 是不是正在拨打电话
			if (IdtApplication.resumeCurrentCall()) {
				((NotificationManager) MeetingNotice.this.getSystemService(Context.NOTIFICATION_SERVICE))
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
			// 存储
			ContentValues contentValues = new ContentValues();
			contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER,
					SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
			contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, CurrentGroupCall.CURRENT_GROUP_CALL_NUM);
			contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER,
					SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE, 1);
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			Uri uri = getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}


	private void showDialog() {
		MeetingRefuseDialog.Builder customBuilder = new MeetingRefuseDialog.Builder(this);
		customBuilder.setPositiveButton("确认", getContentListener);
		customBuilder.setNegativeButton("取消", getContentListener);
		customBuilder.setListener(this);
		Dialog dialog = customBuilder.create();
		dialog.show();
	}

	DialogInterface.OnClickListener getContentListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				dialog.dismiss();
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				dialog.dismiss();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(MeetingNotice.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) MeetingNotice.this.getApplication();
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

	private void initData() {
		receiveMeetingMsgData = (MeetingMsgData) getIntent().getSerializableExtra("meeting_msg_data");
		local_callto_group_num=getIntent().getStringExtra("callto_group_num");
		// 设置会议名字
		meeting_name_textview.setText("临时会议");
		// 设置发起人
		promoter_name.setText(receiveMeetingMsgData.getNumber());
		// 设置时间
		notice_time.setText(receiveMeetingMsgData.getTime());
		// 设置议题
		issue.setText(receiveMeetingMsgData.getDesc());
		local_my_phone_number = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
	}

	@Override
	public void getContentText(String content) {
		// TODO Auto-generated method stub
		MeetingMsgData meetingMsgDatassss = new MeetingMsgData(JsonOperation.METTING_REPLY,
				receiveMeetingMsgData.getNumber(), receiveMeetingMsgData.getMeetId(), receiveMeetingMsgData.getTitle(),
				receiveMeetingMsgData.getDesc(), receiveMeetingMsgData.getTime(), false, "不参会原因:"+content);
		String jsonString = JSON.toJSONString(meetingMsgDatassss);
		// 会议通知点击回馈
		// 保存发送记录
		ContentValues contentValues = new ContentValues();
		contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, local_my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, jsonString);
		contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
		contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
		contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, "");
		contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 17);
		contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, "");
		contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
		contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
		contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, local_callto_group_num);
		contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, local_my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
		contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
		Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
		try {
			IdtApplication.getCurrentCall().setUri(uri);
		} catch (Exception e) {
			// TODO: handle exception
		}
		IDSApiProxyMgr.getCurProxy().iMSend(0, 11, receiveMeetingMsgData.getNumber(), jsonString, "", "");
		Toast.makeText(MeetingNotice.this, "您已经拒绝会议", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.meeting_accept_button:
			MeetingMsgData meetingMsgDatassss = new MeetingMsgData(JsonOperation.METTING_REPLY,
					receiveMeetingMsgData.getNumber(), receiveMeetingMsgData.getMeetId(),
					receiveMeetingMsgData.getTitle(), receiveMeetingMsgData.getDesc(), receiveMeetingMsgData.getTime(),
					true, "已接受");
			String jsonString = JSON.toJSONString(meetingMsgDatassss);
			// 保存发送记录
			ContentValues contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, local_my_phone_number);
			contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, jsonString);
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, "");
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 17);
			contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, "");
			contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, local_callto_group_num);
			contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, local_my_phone_number);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
			// 发送消息
			IDSApiProxyMgr.getCurProxy().iMSend(0, 17, receiveMeetingMsgData.getNumber(), jsonString, "", "");
			Toast.makeText(MeetingNotice.this, "您已经接收会议", Toast.LENGTH_SHORT).show();
			break;
		case R.id.meeting_refuse_button:
			showDialog();
			break;
		case R.id.return_button: {
			// 点击屏幕上方返回键
			IdtApplication.getInstance().deleteActivity(this);
			}
		default:
			break;
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
				if (IdtApplication.getCurrentCall() == null) {
					// 当前没有呼叫
					// 不处于组呼中
					if (!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")) {
						groupAudioStart();
						status = STATUS_TALK;
						setImageButtonBackground();
						Toast.makeText(MeetingNotice.this, "我在"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+"对讲组内发起对讲", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MeetingNotice.this, "请先选择一个对讲组", Toast.LENGTH_SHORT).show();
					}
				} else {
					// 处于组呼中
					status = STATUS_TALK;
					setImageButtonBackground();
				}
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
				CurrentGroupCall.CURRENT_GROUP_CALL_STATE = "\n主讲人员：空闲";
				intercom_state_textview.setText(
						"对讲组名：" + CurrentGroupCall.CURRENT_GROUP_CALL_NUM + CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
			} else {
				CurrentGroupCall.CURRENT_GROUP_CALL_STATE = "\n主讲人员：" + phone;
				intercom_state_textview.setText(
						"对讲组名：" + CurrentGroupCall.CURRENT_GROUP_CALL_NUM + CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
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
}
