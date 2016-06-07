package com.ids.idtma;

import java.util.List;
import java.util.Map;

import com.baidu.mapapi.map.Text;
import com.ids.idtma.adapter.IdtExpandableListViewAdapter;
import com.ids.idtma.chat.ActivityGroupCall;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.map.IdtMap;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

public class IdtGroup extends ActivityBase implements OnLongClickListener {

	private ExpandableListView expandablelistview;
	private IdtExpandableListViewAdapter expandableListAdapter;
	public static final int TO_PERSION = 0;
	public static final int TO_GROUP = 1;
	public static final int USER_STATUS_CHANGE = 2;
	public static int ACTION_RECEIVE_GROUP_CALL = 4;
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	private TextView intercom_state_textview;
	private ImageButton intercom_image_button;
	private String my_phone_number = "";
//	public static String callto_group_num = "";
	private TextView my_num_textview;
	private IdtApplication idtApplication;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == USER_STATUS_CHANGE) {
				localUserStatusChange();
			} else if (msg.what == ACTION_RECEIVE_GROUP_CALL) {
				// 通知从这个地方走
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
				ringtone.play();
				playVibrate();
			}
		}
	};

	// 点击组呼语音电话图标
	public void groupAudioStart() {
		// 是不是正在拨打电话
		if (IdtApplication.resumeCurrentCall()) {
			((NotificationManager) IdtGroup.this.getSystemService(Context.NOTIFICATION_SERVICE))
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

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idt_group_activity);
		IdtApplication.getInstance().addActivity(this);
		initView();
		expandablelistview = (ExpandableListView) findViewById(R.id.buddy_expandablelistview);
		expandableListAdapter = new IdtExpandableListViewAdapter(this);
		expandablelistview.setAdapter(expandableListAdapter);
		// 分组展开
		expandablelistview.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(final int groupPosition) {
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM = ((GroupMember) expandableListAdapter.getGroup(groupPosition)).getUcNum();
				((GroupMember) expandableListAdapter.getGroup(groupPosition)).setFocused(true);
				Toast.makeText(IdtGroup.this, "您打开了" + CurrentGroupCall.CURRENT_GROUP_CALL_NUM + "对讲组", Toast.LENGTH_SHORT).show();
				for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
					if (groupPosition != i) {
						((GroupMember) expandableListAdapter.getGroup(i)).setFocused(false);
						expandablelistview.collapseGroup(i);
					}
				}
				expandableListAdapter.notifyDataSetChanged();
			}
		});
		// 分组关闭
		expandablelistview.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(final int groupPosition) {
				((GroupMember) expandableListAdapter.getGroup(groupPosition)).setFocused(false);
				expandableListAdapter.notifyDataSetChanged();
			}
		});
		expandablelistview.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(IdtGroup.this, IdtChatActivity.class);
				intent.putExtra("to_where", IdtGroup.TO_PERSION);
				intent.putExtra("callto_persion_num",
						idtApplication.getMapUserGroup()
								.get(idtApplication.getLstGroups().get(groupPosition).getUcNum()).get(childPosition)
								.getUcNum());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				IdtGroup.this.startActivity(intent);
				return false;
			}
		});
	}

	private void initView() {
		intercom_state_textview = (TextView) findViewById(R.id.intercom_state_textview);
		intercom_image_button = (ImageButton) findViewById(R.id.intercom_button);
		intercom_image_button.setOnLongClickListener(this);
		intercom_image_button.setOnTouchListener(new MyClickListener());
		my_phone_number = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
		my_num_textview = (TextView) findViewById(R.id.my_num);
		my_num_textview.setText(my_phone_number);
		idtApplication = (IdtApplication) this.getApplication();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtGroup.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtGroup.this.getApplication();
			if (idtApplication.getLstGroups().size() != 0) {
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
	}

	@Override
	public void getGroupAndUserData() {
		// TODO Auto-generated method stub
		super.getGroupAndUserData();
		updateStatus();
		expandableListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtGroup.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtGroup.this.getApplication();
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

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.map:
			// 点击地图图标
			Intent intent = new Intent(IdtGroup.this, IdtMap.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

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
					Toast.makeText(IdtGroup.this, "我在"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+"对讲组内发起对讲", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(IdtGroup.this, "请先选择一个对讲组", Toast.LENGTH_SHORT).show();
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
		LwtLog.d("wulin", "讲话方提示 ppppppppppppppppppppppppppppppppppppp onCallTalkingTips()+group num:"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM);
		if (phone == null || "".equals(phone)) {
			CurrentGroupCall.CURRENT_GROUP_CALL_STATE = "\n主讲人员：空闲";
			intercom_state_textview.setText(
					"对讲组名：" + CurrentGroupCall.CURRENT_GROUP_CALL_NUM + CurrentGroupCall.CURRENT_GROUP_CALL_STATE);
		} else {
			LwtLog.d("wulin", "讲话方提示 ppppppppppppppppppppppppppppppppppppp onCallTalkingTips()+group num:"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM);
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

	private long mExitTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(IdtGroup.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
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

	public void outTheApp() {
		// 退出
		IDSApiProxyMgr.getCurProxy().Exit();
		LwtLog.d("wulin", ">>>> 已退出并注销。");
		// 发一个通知给父类，让父类去终结所有的Activity,包括自己
		Intent intent = new Intent();
		intent.setAction(ActivityBase.ACTION_QUIT_APPLICATION);
		sendBroadcast(intent);
	}

	private void localUserStatusChange() {
		updateStatus();
		expandableListAdapter.notifyDataSetChanged();
	}

	private void updateStatus() {
		// 讲userStatus里面的信息对应到mapUserGroup
		try {
			// 先更新一次
			IdtApplication application = (IdtApplication) getApplication();
			List<GroupMember> lGroupMembers = application.getLstGroups();
			Map<String, List<GroupMember>> maplist = application.getMapUserGroup();
			List<Map<String, String>> userStatusListMaps = application.getUserStatus();
			for (int index = 0; index < userStatusListMaps.size(); index++) {
				Map<String, String> singerUserStatus = userStatusListMaps.get(index);
				String ucNum = singerUserStatus.get("ucNum");
				int iStatus = Integer.parseInt(singerUserStatus.get("iStatus"));
				for (GroupMember groupMember : lGroupMembers) {
					List<GroupMember> the_list_group_member = maplist.get(groupMember.getUcNum());
					for (int i = 0; i < the_list_group_member.size(); i++) {
						if (the_list_group_member.get(i).getUcNum().equals(ucNum)) {
							if (iStatus == 0) {
								the_list_group_member.get(i).isOnline = false;
							} else if (iStatus == 1) {
								the_list_group_member.get(i).isOnline = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			// 此处报异常的原因是该回调先于获取组信息回调
		}
	}

	@Override
	public void userStatusChange() {
		// TODO Auto-generated method stub
		super.userStatusChange();
		handler.sendEmptyMessage(USER_STATUS_CHANGE);

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
		handler.sendMessage(msg);
	}
}
