package com.ids.idtma.service;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.IdtGroup;
import com.ids.idtma.R;
import com.ids.idtma.chat.ActivityAudioCall;
import com.ids.idtma.chat.ActivityGroupCall;
import com.ids.idtma.chat.ActivityVideoCall;
import com.ids.idtma.chat.DeliverData;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.chat.ServiceDeliverData;
import com.ids.idtma.config.ProjectConfig;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.ftp.FtpBuinessLayer;
import com.ids.idtma.jni.IDTApi;
import com.ids.idtma.jni.aidl.GpsData;
import com.ids.idtma.jni.aidl.GroupData;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.jni.aidl.IDTCallback;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.jni.aidl.UserData;
import com.ids.idtma.jni.aidl.UserGroup;
import com.ids.idtma.provider.ProviderBuinessLayer;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;

import android.R.string;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;

public class IDTNativeService extends Service implements Handler.Callback {
	NotificationManager notificationManager;
	private Handler mCallbackHandler;
	private HandlerThread callbackHandlerThread;
	public static final int RECEIVE_MESSAGE = 1;
	public static final int RECEIVE_USER_STATUS_MESSAGE = 2;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		LwtLog.d(IdtApplication.WULIN_TAG, "绑定服务，返回IDTCallback.Stub对象");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		LwtLog.d(IdtApplication.WULIN_TAG, "当前在jni service中，当前所处线程为:" + Thread.currentThread().getName());
		LwtLog.d(IdtApplication.WULIN_TAG, "启动 IDT Native 服务并注册 >>>>>>>>>>>>>>>>>>> onStartCommand");
		// 获取储存在本地sharedPreferences的相关配置
		String ip = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "server_ip", "");
		String port = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "server_port", "10000");
		String phone = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
		String password = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_password", "");
		String ini_file = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "ini_file", "");
		String max_call_num = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "max_call_num", "1");
		boolean register_flag = SharedPreferencesUtil.getBooleanPreference(getApplicationContext(), "register_flag",
				true);
		String signal_port = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "signal_port", "0");
		String rtp_port = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "rtp_port", "0");
		String tcp_port = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "tcp_port", "0");
		LwtLog.d(IdtApplication.WULIN_TAG,
				"从本地获取的启动参数 >>>>>>>>>>>>>>>>>>> IP:" + ip + "; 端口:" + port + ",phone:" + phone + ",password:" + password
						+ ",ini_file:" + ini_file + ",max_call_num:" + max_call_num + ",register_flag:" + register_flag
						+ ",signal_port:" + signal_port + ",rtp_port:" + rtp_port + ",tcp_port:" + tcp_port);
		// 传给so库的有ini文件的位置，最大并发呼叫数,服务器的IP地址,服务器的端口，电话号码；登录密码；mBinder;是否要注册,0不需要,1需要注册；信号端口；RTP媒体端口；TCP媒体监听端口
		// 注意：此处将binder传给so库
		IDTApi.IDT_Start(ini_file, Integer.valueOf(max_call_num), ip, Integer.valueOf(port), phone, password,
				register_flag ? 1 : 0, mBinder, Integer.valueOf(signal_port), Integer.valueOf(rtp_port),
				Integer.valueOf(tcp_port));
		LwtLog.d("wulin", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTservice进程的id为:"+android.os.Process.myPid());
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>> Native 回调对象 :" + mBinder);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		LwtLog.d(IdtApplication.WULIN_TAG, "创建 IDT Native 服务  >>>>>>>>>>>>>>>> onCreate");
		// 创建一个handler，让它和service不在同一个线程里面执行
		callbackHandlerThread = new HandlerThread(IdtApplication.WULIN_TAG + ".CallbackHandler");
		callbackHandlerThread.start();
		mCallbackHandler = new Handler(callbackHandlerThread.getLooper(), this);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// IDTNativeApi.IDT_Exit();
		SharedPreferencesUtil.setBooleanPreferences(getApplicationContext(), AppConstants.MY_PREFERENCE_NETWORK_STATUS,
				false);
		LwtLog.d(IdtApplication.WULIN_TAG, "注销 IDT Native 服务>>>>>>>>>>>>>>> onDestroy");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		LwtLog.d(IdtApplication.WULIN_TAG, "解除绑定 IDT Native 服务>>>>>>>>>>>>>>> onUnbind");
		return super.onUnbind(intent);
	}

	private final IDTCallback.Stub mBinder = new IDTCallback.Stub() {

		// 用户状态指示
		// 输入:
		// status: 当前用户状态
		// 返回:
		// 无
		// 注意:
		// 由IDT.dll调用,告诉用户状态发生变化
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public void IDT_StatusInd(int status, int cause) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMM登录成功！！！回调登录信息IDT_StatusInd >>>>>> status: " + status);
			// 发送一个广播
			Intent intent = new Intent();
			intent.setAction(ActivityBase.IDT_LOGIN_SUCCESS);
			sendBroadcast(intent);
		}

		// 呼出应答 当用手机呼叫模拟器，当模拟器点击接听以后，模拟器会告诉so系统，so系统再调用该方法，回调手机端
		// 输入:
		// pUsrCtx: 用户上下文
		// pcPeerNum: 对方应答的号码,有可能与被叫号码不同
		// pcPeerName: 对方应答的用户名
		// SrvType: 业务类型,实际的业务类型,可能与MakeOut不同
		// 返回:
		// 0: 成功
		// -1: 失败
		// 注意:
		// 由IDT.dll调用,告诉用户对方应答
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_CallPeerAnswer(long pUsrCtx, int type, String Num, String Name) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM回调对端应答 IDT_CallPeerAnswer >>>>>> type: "
					+ type + ":" + Num + ":" + Name);
			// 发送一个广播
			Intent intent = new Intent();
			intent.setAction(AppConstants.ACTION_CALL_PEER_ANSWER);
			sendBroadcast(intent);
			return 0;
		}

		// 当收到对方的呼入信息
		// 输入:
		// ID: IDT的呼叫ID
		// pcMyNum: 自己号码
		// pcPeerNum: 对方号码 当为群呼的时候eg。可以为1000
		// SrvType: 业务类型
		// pAttr: 媒体属性
		// pExtInfo: 附加信息
		// 返回:
		// 0: 成功
		// -1: 失败
		// 注意:
		// 由IDT.dll调用,告诉用户有呼叫进入
		// 03-22 08:57:15.436: D/wulin(1239): 回调有来电进入IDT_CallIn >>>>>> ID:
		// 0,pcMyNum : 2052,pcPeerNum :2053,SrvType:
		// 16,iVideoSend:0,iVideoRecv:0,pExtInfo:-1789606792
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_CallIn(int ID, String pcMyNum, String pcPeerNum, int SrvType, int iAudioRecv, int iAudioSend,
				int iVideoRecv, int iVideoSend, long pExtInfo) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMM回调有来电进入IDT_CallIn >>>>>> ID: " + ID + ",pcMyNum : " + pcMyNum
							+ ",pcPeerNum :" + pcPeerNum + ",SrvType: " + SrvType + ",iVideoSend:" + iVideoSend
							+ ",iVideoRecv:" + iVideoRecv + ",pExtInfo:" + pExtInfo);
			// 显示亮度进行设定
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = powerManager
					.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Gank");
			wakeLock.acquire();

			// 当为组呼的时候
			if (SrvType == AppConstants.CALL_TYPE_GROUP_CALL) {
				String lock_group_num=SharedPreferencesUtil.getStringPreference(getApplicationContext(), "lock_group_num", "#");
				LwtLog.d(IdtApplication.WULIN_TAG,"锁定的组号:"+lock_group_num);
				if (lock_group_num.equals("#") || lock_group_num.equals(pcPeerNum)) {

					IdtApplication.setCurrentCall(new CallEntity(ID, CallType.GROUP_CALL));
					CurrentGroupCall.CURRENT_GROUP_CALL_NUM = pcPeerNum;
					CurrentGroupCall.CALL_OK = true;
                    //发送广播，各个界面进行捕捉
					Intent intent = new Intent();
					intent.setAction(ActivityBase.ACTION_RECEIVE_GROUP_CALL);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLID, ID);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, pcMyNum);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLER, pcPeerNum);
					intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityGroupCall.FLAG_INCOMING);
					LwtLog.d(IdtApplication.WULIN_TAG, "TTTTTTTTTTTTTT发送组呼广播消息");
					sendBroadcast(intent);
					// 回应
					MediaAttribute pAttr = new MediaAttribute();
					pAttr.ucAudioRecv = 1;
					pAttr.ucAudioSend = 0;
					pAttr.ucVideoRecv = 0;
					pAttr.ucVideoSend = 0;
					IDSApiProxyMgr.getCurProxy().CallAnswer(ID, pAttr, 0);
					// 存储本地
					ContentValues contentValues = new ContentValues();
					contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, -10000);
					contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
					contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
					contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 9);
					contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
					contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, pcPeerNum);
					contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER,
							SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
					contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE, 1);
					contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
					Uri uri = getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
					LwtLog.d(IdtApplication.WULIN_TAG,"getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues)>>>>>>>>>>>>>>");
					try {
						IdtApplication.getCurrentCall().setUri(uri);
					} catch (Exception e) {
						// TODO: handle exception
					}
//					Intent intent = new Intent(getApplicationContext(), ActivityGroupCall.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					intent.putExtra(AppConstants.EXTRA_KEY_CALLID, ID);
//					intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, pcMyNum);
//					intent.putExtra(AppConstants.EXTRA_KEY_CALLER, pcPeerNum);
//					intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityGroupCall.FLAG_INCOMING);
//					startActivity(intent);
				} else {
					IDTApi.IDT_CallRel(ID, 0, 0);
					LwtLog.d(IdtApplication.WULIN_TAG,
							"呼叫被阻止，不是锁定的组号码。 IDTNativeApi.IDT_CallRel 已挂机 >>>>>>>>>>>> callId : " + ID);
				}
			} else {
				// 当为单呼并且有视频的时候
				LwtLog.d(IdtApplication.WULIN_TAG, "单呼视频");
				if (iVideoRecv == 1 || iVideoSend == 1) {

					IdtApplication.setCurrentCall(new CallEntity(ID, CallType.VEDIO_CALL));

					Intent intent = new Intent(getApplicationContext(), ActivityVideoCall.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLID, ID);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, pcMyNum);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLER, pcPeerNum);
					intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityAudioCall.FLAG_INCOMING);
					startActivity(intent);
				} else {
					// 当为单呼没有视频的时候
					// 将当前呼叫进行存储
					LwtLog.d(IdtApplication.WULIN_TAG, "单呼没有视频");
					IdtApplication.setCurrentCall(new CallEntity(ID, CallType.AUDIO_CALL));

					Intent intent = new Intent(getApplicationContext(), ActivityAudioCall.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLID, ID);
					intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityAudioCall.FLAG_INCOMING);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, pcMyNum);
					intent.putExtra(AppConstants.EXTRA_KEY_CALLER, pcPeerNum);
					startActivity(intent);
				}
			}
			return 0;
		}

		// 对方或IDT内部释放呼叫 当拨打方挂掉电话，那么在接听方，将调用此方法
		// 输入:
		// ID: IDT的呼叫ID,通常不使用这个,但可能启动被叫后,用户还没有应答,就释放了
		// pUsrCtx: 用户上下文
		// uiCause: 释放原因值
		// 返回:
		// 0: 成功
		// -1: 失败
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_CallRelInd(int ID, long pUsrCtx, int uiCause) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM回调远端释放 IDT_CallRelInd >>>>>> ID: " + ID+",uiCause:"+uiCause);
			Intent intent = new Intent();
			intent.setAction(AppConstants.ACTION_CALL_REL_IND);
			intent.putExtra("ID", ID);
			intent.putExtra("uiCause", uiCause);
			intent.putExtra("uri", IdtApplication.getCurrentCall().getUri());
			sendBroadcast(intent);
			setCurrentCallNull(uiCause);
			notificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
			IdtApplication.stopRingtone();
			LwtLog.d(IdtApplication.WULIN_TAG, "IDT_CallRelInd 1111111");
			return 0;
		}

		// 话权指示 讲话方获得这条信息 假如是获取话权：从第二次开始会先释放一次，然后再获取话权；第一次不用进行释放，直接获取话权
		// 输入:
		// pUsrCtx: 用户上下文
		// uiInd: 指示值:0话权被释放,1获得话权,与媒体属性相同
		// 返回:
		// 0: 成功
		// -1: 失败
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_CallMicInd(long pUsrCtx, int uiInd) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMM话权指示 >>>>>>>>>>>>>> IDT_CallMicInd >>>>>> pUsrCtx: " + pUsrCtx
							+ ", uiInd(0-释放,1-获得):" + uiInd);
			// 获得话权，震动,播放铃声
			if (uiInd > 0) {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
				ringtone.play();
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(200);
				//获取到话权
				Intent intent = new Intent();
				intent.putExtra("uiInd", 1);
				intent.setAction(ActivityBase.ACTION_CALL_GET_MIC);
				sendBroadcast(intent);
			}else{
				//获取到话权
				Intent intent = new Intent();
				intent.putExtra("uiInd", 0);
				intent.setAction(ActivityBase.ACTION_CALL_GET_MIC);
				sendBroadcast(intent);
			}
			return 0;
		}

		// 讲话方提示 所有的群成员都会受到这条信息
		// 输入:
		// pUsrCtx: 用户上下文
		// pcNum: 讲话方号码
		// pcName: 讲话方名字
		// 返回:
		// 0: 成功
		// -1: 失败
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public void IDT_CallTalkingIDInd(long pUsrCtx, String num, String name) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMM讲话方提示 >>>>>>>>>>>>>> IDT_CallTalkingIDInd >>>>>> pUsrCtx: " + pUsrCtx
							+ ", phone :" + num + ", name:" + name);
			Intent intent = new Intent();
			intent.setAction(AppConstants.ACTION_CALL_TALKING_TIPS);
			intent.putExtra(AppConstants.EXTRA_KEY_TALKING_USERNAME, name);
			// 讲话方电话
			intent.putExtra(AppConstants.EXTRA_KEY_TALKING_PHONE, num);
			sendBroadcast(intent);
		}

		// 通话状态下收到对方发送的号码
		// 输入:
		// pUsrCtx: 用户上下文
		// cNum: 收到的号码,ASC字符形式,有效值为'0'~'9','*','#','A'~'D',16(FLASH)
		// 返回:
		// 0: 成功
		// -1: 失败
		@Override
		public int IDT_CallRecvNum(long pUsrCtx, char cNum) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMIDT_CallRecvNum >>>>>> pUsrCtx: " + pUsrCtx);
			return 0;
		}

		// 收到视频数据
		// 输入:
		// pUsrCtx: 用户上下文
		// ucCodec: CODEC
		// ucBuf: 数据
		// iLen: 数据长度
		// IFrame: I帧标识
		// uiTs: 时戳
		// 返回:
		// 0: 成功
		// -1: 失败
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_CallRecvVideoData(long pUsrCtx, int ucCodec, byte[] ucBuf, int IFrame, int uiTs)
				throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMIDT_CallRecvVideoData收到视频数据");
			Intent intent = new Intent();
			intent.setAction(AppConstants.ACTION_RECEIVE_VIDEO_DATA);
			intent.putExtra(AppConstants.EXTRA_KEY_RECEIVE_VIDEO_DATA, ucBuf);
			sendBroadcast(intent);
			return 0;
		}

		@Override
		public int IDT_RecvPassThrouth(String myNum, String peerNum, String pBuf, int iLen) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMIDT_RecvPassThrouth >>>>>> myNum: " + myNum);
			return 0;
		}

		// 组信息指示 当登录成功以后 so库会回调该方法
		// 输入:
		// pGInfo: 组信息
		// 返回:
		// 无
		// 注意:
		// 由IDT.dll调用,告诉用户状态发生变化
		// so库调用这边的
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public void IDT_GInfoInd(UserGroup userInfo) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMM获取组信息 IDT_GInfoInd >>>>>> UserGroup.iMemberNum: "
							+ userInfo.iMemberNum);
			IdtApplication application = (IdtApplication) getApplication();
			if (application.getLstGroups() == null) {
				application.setLstGroups(new ArrayList<GroupMember>());
			} else {
				application.getLstGroups().clear();
			}
			for (int i = 0; i < userInfo.iMemberNum; i++) {
				application.getLstGroups().add(userInfo.getMember()[i]);
				LwtLog.d(IdtApplication.WULIN_TAG, "IDT_GInfoInd 获取组：" + userInfo.getMember()[i].getUcName() + ",Num: "
						+ userInfo.getMember()[i].getUcNum());
				IDTApi.IDT_GQueryU(i, userInfo.getMember()[i].getUcNum());
				LwtLog.d(IdtApplication.WULIN_TAG,
						"IDT_GInfoInd 下发命令获取组成员 ,Num: " + userInfo.getMember()[i].getUcNum());
			}
		}

		// 收到IM消息
		// 输入:
		// pcFrom: 源号码
		// pcTo: 目的号码
		// pBuf: 消息内容
		// iLen: 消息长度
		// 返回:
		// 0: 成功
		// -1: 失败
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_RecvIM(String pcFrom, String pcTo, String pBuf, int iLen) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_RecvIM >>>>>> From: " + pcFrom
					+ ", To:" + pcTo + ", Content: " + pBuf);
			/**
			 * 
			 * 屏蔽掉消息通知
			 * 
			 * 
			 * // 消息通知栏 // 定义NotificationManager NotificationManager
			 * mNotificationManager = (NotificationManager)
			 * getSystemService(Context.NOTIFICATION_SERVICE); // 定义通知栏展现的内容信息
			 * int icon = R.drawable.notification_sms; CharSequence tickerText =
			 * "您有新的信息";// 状态栏(Status Bar)显示的通知文本提示 long when =
			 * System.currentTimeMillis();// 通知产生的时间，会在通知信息里显示 Context context =
			 * getApplicationContext(); // API level16 // Notification
			 * notification = new Notification.Builder( //
			 * getApplicationContext()) // .setContentTitle("来自IDT-MA的信息") //
			 * .setContentText(pBuf) // .setSmallIcon(icon) // .setLargeIcon( //
			 * BitmapFactory.decodeResource( //
			 * getApplicationContext().getResources(), //
			 * R.drawable.ic_launcher)).build(); // API level14
			 * 
			 * 
			 * Notification notification = new Notification(icon, tickerText,
			 * when); // 定义下拉通知栏时要展现的内容信息 CharSequence contentTitle =
			 * "来自IDT-MA的信息"; CharSequence contentText = pBuf; Intent
			 * notificationIntent = new Intent(context, IdtChatActivity.class);
			 * // notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * notificationIntent.putExtra(IdtChatActivity.EXTRA_KEY_TAB_INDEX,
			 * IdtChatActivity.TAB_INDEX_2_SMS); PendingIntent contentIntent =
			 * PendingIntent.getActivity(context, 0, notificationIntent, 0);
			 * notification.setLatestEventInfo(context, contentTitle,
			 * contentText, contentIntent); notification.contentIntent =
			 * contentIntent;
			 * 
			 * // 设置默认的声音和振动、LED灯闪烁 notification.defaults =
			 * Notification.DEFAULT_ALL; notification.flags |=
			 * Notification.FLAG_AUTO_CANCEL;
			 * 
			 * // 用mNotificationManager的notify方法通知用户生成标题栏消息通知
			 * mNotificationManager.notify(AppConstants.SMS_NOTIFICATION_ID,
			 * notification);
			 *
			 *
			 *
			 */
			// 有消息来得时候，震动
			Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(200);
			// 保存记录
			ContentValues contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, pcFrom);
			contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, pBuf);
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, "");
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 4);
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			LwtLog.d(IdtApplication.WULIN_TAG, "IDTNativeService当前时间:" + DateUtil.formatDate(null, null));
			Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
			// 发一个收到富文本消息的广播
			Intent intent = new Intent();
			intent.setAction(ActivityBase.IDT_RECEIVE_RICHMESSAGE);
			sendBroadcast(intent);
			return 0;
		}

		// 用户操作响应
		// 输入:
		// dwOptCode: 操作码
		// dwSn: 操作序号
		// wRes: 结果
		// pUser: 用户信息
		// 返回:
		// 无
		// 注意:
		// 由IDT.dll调用,告诉用户操作结果
		@Override
		public void IDT_UOptRsp(int dwOptCode, int dwSn, int wRes, UserData userData) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG,
					"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMIDT_UOptRsp >>>>>> dwOptCode: " + dwOptCode);
		}

		// 组查询成员操作响应 查询一个组里面的所有成员
		// 输入:
		// dwOptCode: 操作码
		// dwSn: 操作序号
		// wRes: 结果0是成功，其他是失败码
		// groupData: 组信息
		// 返回:
		// 无
		// 注意:
		// 由IDT.dll调用,告诉用户操作结果
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public void IDT_GOptRsp(int dwOptCode, int dwSn, int wRes, GroupData groupData) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d(IdtApplication.WULIN_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM获取组成员信息 IDT_GOptRsp >>>>>> dwOptCode: "
					+ dwOptCode + ", Num: " + groupData.getUcNum());
			IdtApplication application = (IdtApplication) getApplication();
			if (application.getMapUserGroup() == null) {
				// 初始化一个用户组对象
				application.setMapUserGroup(new HashMap<String, List<GroupMember>>());
			}
			List<GroupMember> lstGroupMember = new ArrayList<GroupMember>();
			for (int i = 0; i < groupData.getDwNum(); i++) {
				GroupMember o = new GroupMember();
				// 组成员的名字
				o.setUcName(groupData.getMember()[i].getUcName());
				// 组成员的编号
				o.setUcNum(groupData.getMember()[i].getUcNum());
				//已经存在的话，那么checkout赋值过去
				if(application.getMapUserGroup().get(groupData.getUcNum())!=null){
					List<GroupMember> listGroupmember = application.getMapUserGroup().get(groupData.getUcNum());
//					o.setChecked(application.getMapUserGroup().get(groupData.getMember()[i].getUcName()));
					for(int index=0;index<listGroupmember.size();index++){
						if(listGroupmember.get(index).getUcNum().equals(groupData.getMember()[i].getUcNum())){
							if(listGroupmember.get(index).getChecked()==true){
								o.setChecked(true);
							}else{
								o.setChecked(false);
							}
						}
					}
					
				}
				lstGroupMember.add(o);
				LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>>>>组成员：name=" + groupData.getMember()[i].getUcName()
						+ ",num=" + groupData.getMember()[i].getUcNum());
			}
			// 将一个组加到一个组对象中
			application.getMapUserGroup().put(groupData.getUcNum(), lstGroupMember);
			// 发一个收到组信息的广播
			Intent intent = new Intent();
			intent.setAction(ActivityBase.ACTION_RECEIVE_GROUP_DATA);
			sendBroadcast(intent);
		}

		// 获取IM文件名
		// 输入:
		// dwSn: 消息事务号
		// pcTo: 目的号码
		// dwType: 及时消息类型,IM_TYPE_IMAGE等
		// #define IM_TYPE_TXT 0x01 //只有文本 不需要文件 from + to + text
		// #define IM_TYPE_GPS 0x02 //GPS位置信息 不需要文件 from + to + text(字符串:经度,纬度)
		// #define IM_TYPE_IMAGE 0x03 //图像 需要文件 from + to + text + filename
		// #define IM_TYPE_AUDIO 0x04 //语音文件,微信 需要文件 from + to + text + filename
		// #define IM_TYPE_VIDEO 0x05 //视频录像文件 需要文件 from + to + text + filename
		// 返回:
		// 0: 成功
		// -1: 失败
		// -----------------------------------------------------------------------------------------------------------------------------------
		@Override
		public int IDT_IMGetFileNameRsp(int dwSn, String pcFileName) throws RemoteException {
			LwtLog.d(IdtApplication.WULIN_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM获取到文件名IDT_IMGetFileNameRsp >>>>>> dwsn: "
					+ dwSn + ", pcFileName: " + pcFileName);
			// 发一个收到组信息的广播
			Intent intent = new Intent();
			intent.setAction(ActivityBase.IDT_GET_FILE_NAME);
			intent.putExtra("file_name", pcFileName);
			sendBroadcast(intent);
			return 0;
		}

		// 收到IM消息
		// 输入:
		// dwSn: 消息事务号
		// dwType: 及时消息类型,IM_TYPE_IMAGE等
		// pcFrom: 源号码
		// pcTo: 目的号码
		// pcTxt: 文本内容
		// pcFileName: 文件名
		// 返回:
		// 0: 成功
		// -1: 失败
		@Override
		public int IDT_IMRecv(String pucSn, int dwType, String pcFrom, String pcTo, String pcOriTo,String pcTxt, String pcFileName,
				String pcSourceFileName) throws RemoteException {
			try {
				LwtLog.d(IdtApplication.WULIN_TAG, "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_IMRecv >>>>>>: " + "消息事物号:"
						+ pucSn + ",源号码:" + pcFrom + ",目的号码:" + pcTo + ",文本内容:" + pcTxt + "文件名:" + pcFileName);
			} catch (Exception e) {
				// TODO: handle exception
			}
			// 有消息来得时候，震动
			Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(200);
			// 保存记录
			ContentValues contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, pcFrom);
			contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, pcTxt);
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 1);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			String SMS_RESOURCE_URL = (pcFileName == null ? ""
					: (Environment.getExternalStorageDirectory().getPath() + "/IDT-MA" + "/IM/" + pcFrom + "/"
							+ pcFileName));
			contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, SMS_RESOURCE_URL);
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, dwType);
			contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, pcFileName);
			if (dwType == 4 || dwType == 5) {
				contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, Integer.parseInt(pcTxt));
			}
			contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
			if(pcTo.startsWith("#")){
				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, pcTo.replace("#", ""));
				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE, 1);
			}else{
				contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, pcTo);
				contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE, 0);
			}
			contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "") );
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			LwtLog.d(IdtApplication.WULIN_TAG, "IDTNativeService当前时间:" + DateUtil.formatDate(null, null));
			Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (dwType == 3 || dwType == 4 || dwType == 5 || dwType == 6) {
				Message msg = new Message();
				msg.what = RECEIVE_MESSAGE;
				msg.obj = new ServiceDeliverData(dwType, uri, pcFrom, SMS_RESOURCE_URL, pcFileName);
				mCallbackHandler.sendMessage(msg);
			}
			// 发一个收到富文本消息的广播
			Intent intent = new Intent();
			intent.setAction(ActivityBase.IDT_RECEIVE_RICHMESSAGE);
			intent.putExtra("SMS_RESOURCE_URL", SMS_RESOURCE_URL);
			intent.putExtra("pcFileName", pcFileName);
			intent.putExtra("dwType", dwType);
			sendBroadcast(intent);
			return 0;
		}

		// IM状态指示
		// 输入:
		// dwSn: 消息事务号
		// pucSn: 系统的事务号
		// dwType: 及时消息类型,IM_TYPE_IMAGE等
		// ucStatus: 状态,PTE_CODE_TXCFM等
		// #define PTE_CODE_REQ 1 //发送请求
		// #define PTE_CODE_TXCFM 2 //传输确认
		// #define PTE_CODE_USRREAD 3 //用户阅读
		// #define PTE_CODE_USRREADCFM 4 //用户阅读消息的确认
		// #define PTE_CODE_FILENAMEREQ 0x80 //文件名请求
		// #define PTE_CODE_FILENAMERSP 0x81 //文件名响应
		// #define PTE_CODE_NSSUBS 0x82 //存储订阅 起始号码~结束号码;起始号码~结束号码;起始号码~结束号码;
		// #define PTE_CODE_NSQUERYREQ 0x84 //存储查询
		// #define PTE_CODE_NSQUERYRSP 0x85 //存储查询响应
		// 返回:
		// 0: 成功
		// -1: 失败
		@Override
		public int IDT_IMStatusInd(int dwSn, String pucSn, int dwType, int ucStatus) throws RemoteException {
			LwtLog.d("wulin", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_IMStatusInd");
			return 0;
		}

		@Override
		public void IDT_GpsRecInd(String ucNum, int ucStatus, float longitude, float latitude, float speed,
				float direction, int year, int month, int day, int hour, int minute, int second)
				throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d("mymap", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_GpsRecInd================" + ucNum + "======"
					+ longitude + "==========" + latitude);
			IdtApplication application = (IdtApplication) getApplication();
			List<GpsData> lGpsDatas = application.getLgpsDatas();
			// 删除掉存在的数据
			int index = -1;
			for (int i = 0; i < lGpsDatas.size(); i++) {
				if (lGpsDatas.get(i).getUcNum().equals(ucNum)) {
					index = i;
				}
			}
			if (index != -1) {
				lGpsDatas.remove(index);
			}
			GpsData gpsData = new GpsData();
			gpsData.setUcNum(ucNum);
			gpsData.setUcStatus(ucStatus);
			gpsData.setLongitude(longitude);
			gpsData.setLatitude(latitude);
			gpsData.setSpeed(speed);
			gpsData.setDirection(direction);
			gpsData.setYear(year);
			gpsData.setMonth(month);
			gpsData.setDay(day);
			gpsData.setHour(hour);
			gpsData.setMinute(minute);
			gpsData.setSecond(second);
			lGpsDatas.add(gpsData);
			application.setLgpsDatas(lGpsDatas);
			// 发一个收到富文本消息的广播
			Intent intent = new Intent();
			intent.setAction(ActivityBase.RECEIVE_GPS_MESSAGE);
			sendBroadcast(intent);
		}

		// 获取用户状态
		// iType user:1 group:2
		// iStatus 离线：0 在线：1
		// iSrvType 业务类型
		// iCallStatue 呼叫状态
		@Override
		public void IDT_GUStatusInd(int iType, String ucNum, int iStatus, int iSrvType, int iCallStatue)
				throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d("mystatus", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_GUStatusInd================" + iType + "======"
					+ ucNum + "==========" + iStatus);
			if (iType == 1) {
				// 只需要捕获个人用户状态
				Message msg = new Message();
				msg.what = RECEIVE_USER_STATUS_MESSAGE;
				Bundle bundle = new Bundle();
				bundle.putString("ucNum", ucNum);
				bundle.putInt("iStatus", iStatus);
				msg.setData(bundle);
				mCallbackHandler.sendMessage(msg);
			}
		}

		//会议过程中对视频的收发进行控制
		//ucRecv=0，不接收，ucRecv=1，接收
		//ucSend=0，不接收，ucSend=1，接收
		@Override
		public int IDT_CallSetVideoCodec(int ucRecv, int ucSend) throws RemoteException {
			// TODO Auto-generated method stub
			LwtLog.d("mystatus", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_CallSetVideoCodec================ucRecv:"+ucRecv+",ucSend:"+ucSend);
			Intent intent = new Intent();
			intent.setAction(ActivityBase.IDT_VIDEO_CODEC_STATUS);
			intent.putExtra("ucRecv", ucRecv);
			intent.putExtra("ucSend", ucSend);
			sendBroadcast(intent);
			return 0;
		}
		
		// 媒体流统计数据
		// 输入:
		// pUsrCtx: 用户上下文
		// ucType: 语音还是视频,1语音,2视频,SDP_MEDIA_AUDIO
		// uiRxBytes: 当前统计段接收的所有字节数
		// uiRxUsrBytes: 当前统计段用户接收的字节数
		// uiRxCount: 当前统计段收到的报文个数
		// uiRxBytes: 当前统计段发送的所有字节数
		// uiRxUsrBytes: 当前统计段用户发送的字节数
		// uiRxCount: 当前统计段发送的报文个数
		// 返回:
		// 0: 成功
		// -1: 失败
		@Override
		public void IDT_CallMediaStats(long pUsrCtx, int ucType, int uiRxBytes, int uiRxUsrBytes, int uiRxCount, int uiRxUserCount,
				int uiTxBytes, int uiTxUsrBytes, int uiTxCount ,int uiTxUserCount) throws RemoteException {
			// TODO Auto-generated method stub
			// 发一个收到富文本消息的广播
			LwtLog.d("wulin", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMM收到信息 IDT_CallMediaStats================"+uiRxUsrBytes+",,,,,"+uiTxUsrBytes);
			Intent intent = new Intent();
			intent.setAction(ActivityBase.GET_STREAM);
			intent.putExtra("uiRxUsrBytes", uiRxUsrBytes);
			intent.putExtra("uiTxUsrBytes", uiTxUsrBytes);
			sendBroadcast(intent);
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case RECEIVE_MESSAGE:
			ServiceDeliverData deliverData = (ServiceDeliverData) msg.obj;
			int dwType = deliverData.getStyle();
			Uri uri = deliverData.getUri();
			String pcFrom = deliverData.getCallto_persion_num();
			String SMS_RESOURCE_URL = deliverData.getSms_resource_url();
			String pcFileName = deliverData.getPcFileName();
			FtpBuinessLayer ftpBuinessLayer = new FtpBuinessLayer(this);
			LwtLog.d(IdtApplication.WULIN_TAG, "mmm我到了这里");
			ftpBuinessLayer.downLoadFile("/IM/" + pcFrom + "/" + pcFileName, "/IM/" + pcFrom,
					SMS_RESOURCE_URL.replaceAll(pcFileName, ""), pcFileName, dwType, uri);
			break;

		case RECEIVE_USER_STATUS_MESSAGE:
			Bundle bundle = msg.getData();
			String ucNum = bundle.getString("ucNum");
			int iStatus = bundle.getInt("iStatus");
			try {
				//先更新一次
				IdtApplication application = (IdtApplication) getApplication();
				List<GroupMember> lGroupMembers = application.getLstGroups();
				Map<String, List<GroupMember>> maplist = application.getMapUserGroup();
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

			} catch (Exception e) {
				// TODO: handle exception
				// 此处报异常的原因是该回调先于获取组信息回调
			}
			// 获取IDTApplication中的存储，然后将当前的放进去
			IdtApplication application = (IdtApplication) getApplication();
			List<Map<String, String>> lMaps = application.getUserStatus();
			int index = -1;
			// 删除同样Ucnum
			for (int i = 0; i < lMaps.size(); i++) {
				if (lMaps.get(i).get("ucNum").equals(ucNum)) {
					// 存在相同ucNum的数据
					index = i;
					break;
				}
			}
			// 存在就删除
			if (index != -1) {
				lMaps.remove(index);
			}
			// 无论以前存在，还是没有，都会进行添加
			Map<String, String> map = new HashMap<String, String>();
			map.put("ucNum", ucNum);
			map.put("iStatus", iStatus + "");
			lMaps.add(map);
			application.setUserStatus(lMaps);

			// 发送一个通知
			Intent intent = new Intent();
			intent.setAction(ActivityBase.USER_STATUS_CHANGE);
			sendBroadcast(intent);
			break;

		default:
			break;
		}
		return true;
	}
	
	private void setCurrentCallNull(int uiCause) {
		// 更新数据库uiCause字段值
		try {
			ProviderBuinessLayer providerBuinessLayer = new ProviderBuinessLayer(this);
			Uri uri = IdtApplication.getCurrentCall().getUri();
			providerBuinessLayer.update_ui_cause(uri, uiCause);
			IdtApplication.setCurrentCall(null);
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM="";
			CurrentGroupCall.CURRENT_GROUP_CALL_STATE="";
			CurrentGroupCall.CALL_OK = false;
			String lock_group_num = SharedPreferencesUtil.getStringPreference(this, "lock_group_num", "#");
			if (!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")) {

			} else if (!lock_group_num.equals("#")) {
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
			} else {
				IdtApplication idtApplication = (IdtApplication) this.getApplication();
				if (idtApplication.getLstGroups().size() != 0) {
					CurrentGroupCall.CURRENT_GROUP_CALL_NUM = idtApplication.getLstGroups().get(0).getUcNum();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}