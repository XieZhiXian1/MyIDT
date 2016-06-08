
package com.ids.idtma.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.ids.idtma.AboutOurActivity;
import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.IdtGroup;
import com.ids.idtma.IdtLogin;
import com.ids.idtma.IdtMessage;
import com.ids.idtma.R;
import com.ids.idtma.adapter.SmsDetailAdapter;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.entity.MeetingMsgData;
import com.ids.idtma.entity.SmsEntity;
import com.ids.idtma.frame.PullToRefreshView;
import com.ids.idtma.frame.PullToRefreshView.OnHeaderRefreshListener;
import com.ids.idtma.ftp.FtpBuinessLayer;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.map.IdtMap;
import com.ids.idtma.provider.SmsConversationProvider;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.service.LocationService;
import com.ids.idtma.util.CommonUtils;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.CustomDialog;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.ImageGetDialog;
import com.ids.idtma.util.JsonOperation;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.PasteEditText;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.StringsUtils;
import com.ids.idtma.util.WaitDialog;
import com.ids.idtma.voicerecord.OnStateListener;
import com.ids.idtma.voicerecord.RecordManger.SoundAmplitudeListen;
import com.ids.idtma.voicerecord.TalkNetManager;
import com.ids.proxy.IDSApiProxyMgr;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 聊天页面
 * 
 */
@SuppressWarnings("deprecation")
public class IdtChatActivity extends ActivityBase
		implements OnClickListener, TalkNetManager.Listener, FtpBuinessLayer.Listener, OnHeaderRefreshListener,OnLongClickListener,SmsDetailAdapter.Listener {
	// 和notifation相关
	public static final String EXTRA_KEY_TAB_INDEX = "EXTRA_KEY_TAB_INDEX";
	public static final int TAB_INDEX_0_COMMUNICATIONS = 0;
	public static final int TAB_INDEX_1_MEDIA = 1;
	public static final int TAB_INDEX_2_SMS = 2;
	public static final int TAB_INDEX_3_SETTINGS = 3;

	private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
	public static final int REQUEST_CODE_CONTEXT_MENU = 3;
	private static final int REQUEST_CODE_MAP = 4;
	public static final int REQUEST_CODE_TEXT = 5;
	public static final int REQUEST_CODE_VOICE = 6;
	public static final int REQUEST_CODE_PICTURE = 7;
	public static final int REQUEST_CODE_LOCATION = 8;
	public static final int REQUEST_CODE_NET_DISK = 9;
	public static final int REQUEST_CODE_FILE = 10;
	public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
	public static final int REQUEST_CODE_PICK_VIDEO = 12;
	public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
	public static final int REQUEST_CODE_VIDEO = 14;
	public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
	public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
	public static final int REQUEST_CODE_SEND_USER_CARD = 17;
	public static final int REQUEST_CODE_CAMERA = 18;
	public static final int REQUEST_CODE_LOCAL = 19;
	public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
	public static final int REQUEST_CODE_GROUP_DETAIL = 21;
	public static final int REQUEST_CODE_SELECT_VIDEO = 23;
	public static final int REQUEST_CODE_SELECT_FILE = 24;
	public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

	public static final int RESULT_CODE_COPY = 1;
	public static final int RESULT_CODE_DELETE = 2;
	public static final int RESULT_CODE_FORWARD = 3;
	public static final int RESULT_CODE_OPEN = 4;
	public static final int RESULT_CODE_DWONLOAD = 5;
	public static final int RESULT_CODE_TO_CLOUD = 6;
	public static final int RESULT_CODE_EXIT_GROUP = 7;

	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;

	public static final String COPY_IMAGE = "EASEMOBIMG";
	private LocationService locationService;
	private View recordingContainer;
	private ImageView micImage;
	private TextView recordingHint;
	private ListView listView;
	private SmsDetailAdapter adapter;
	private View buttonSetModeKeyboard;
	private View buttonSetModeVoice;
	private View buttonSend;
	private View buttonPressToSpeak;
	// private ViewPager expressionViewpager;
	private LinearLayout emojiIconContainer;
	private LinearLayout btnContainer,groupBtnContainer;
	private PasteEditText mEditTextContent;
	private View more;
	private ClipboardManager clipboard;
	private InputMethodManager manager;
	private List<String> reslist;
	private Drawable[] micImages;
	private int chatType;
	private NewMessageBroadcastReceiver receiver;
	public static IdtChatActivity activityInstance = null;
	// 给谁发送消息
	private String toChatUsername;
	public static int resendPos;
	// private ImageView iv_emoticons_normal;
	// private ImageView iv_emoticons_checked;
	private RelativeLayout edittext_layout;
	private ProgressBar loadmorePB;
	private boolean isloading;
	private boolean haveMoreData = true;
	private Button btnMore;
	public String playMsgId;

	String myUserNick = "";
	String myUserAvatar = "";
	String toUserNick = "";
	String toUserAvatar = "";
	// 分享的照片
	String iamge_path = null;
	// 设置按钮
	private ImageView iv_setting;
	private ImageView iv_setting_group;
	private NotificationManager notificationManager;
	// 拨打过去用户的信息
	// private GroupMember callto_persion_info_member, callto_group_info;
	private String callto_persion_num = "";
	private String callto_group_num = "";
	private String my_phone_number = "";
	private TextView title_textview;
	private TalkNetManager talk;
	// public static String SERVER_GET_FILE_NAME="";
	// public static String TEMP_VOICE_FILE_NAME="temp_voice_file_name";
	public static List<SmsEntity> list = new ArrayList<SmsEntity>();
	public static List<SmsEntity> show_list = new ArrayList<SmsEntity>();
	private FtpBuinessLayer ftpBuinessLayer;
	private int FILE_UPLOAD_SUCCESS = 0;
	private int FILE_Download_SUCCESS = 1;
	private int GET_PICTURE_FROM_CAMERA = 2;
	private int SUCCESS_TAKE_PICTURE = 3;
	private int GET_VIDEO_FROM_CAMERA = 4;
	private int SUCCESS_TAKE_VIDEO = 5;
	private int GET_GPS_FROM_LOCAL = 6;
	public static int RESULT_LOAD_FILE = 7;
	public static int REFRESH_OK = 8;
	public static int GET_FILE_FROM_BROWSER = 9;
	public static int SINGR_AUDIO_CALL_REQUEST = 10;
	public static int SINGR_AUDIO_VIDEO_CALL_REQUEST = 11;
	public static int SINGR_GROUP_AUDIO_CALL_REQUEST = 12;
	// private String create_voice_file_path="";
	// private String create_voice_file_name="";
	// private String remote_path_voice="";
	// private int CURRENT_UPLOAD_FILE_STYLE = -1;
	// 录音或者视频的时间间隔（秒）
	public static int TIME_LENGTH = 0;
	// 秒
	private int SMS_RESOURCE_TIME_LENGTH = 0;
	// 毫秒
	private long SMS_RESOURCE_START_TIME = 0;
	private long SMS_RESOURCE_STOP_TIME = 0;

	private BDLocation local_get_location = null;

	private PullToRefreshView mPullToRefreshView;
	private int pageNum = 1;// 用于记录页数
	private int totalPageNum;// 总页数
	private int FROM_WHERE = -1;
	
	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	private int status;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	private TextView intercom_state_textview;
	private ImageButton intercom_image_button;
	public static int ACTION_RECEIVE_GROUP_CALL = 20;
	public static boolean FILE_IS_IMAGE = false;
//	private TextView my_num_textview;
	@SuppressLint("HandlerLeak")
	private Handler micImageHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// 切换msg切换图片
			micImage.setImageDrawable(micImages[msg.what]);
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == FILE_UPLOAD_SUCCESS) {
				DeliverData deliverData = (DeliverData) msg.obj;
				Uri uri = (Uri) deliverData.getUri();
				int style = deliverData.getStyle();
				String file_name = deliverData.getFilename();
				String callto_persion_num = deliverData.getCallto_persion_num();
				if (style == 3) {
					// 图片
					// sendPicture();
					Toast.makeText(IdtChatActivity.this, "信息发送成功", Toast.LENGTH_SHORT).show();
					update_SMS_RESOURSE_RS_OK(callto_persion_num, uri);
					loadRichFileData();
					// 发送IM消息
					// 输入:
					// dwSn: 消息事务号
					// dwType: 及时消息类型,IM_TYPE_IMAGE等
					// pcTo: 目的号码
					// pcTxt: 文本内容
					// pcFileName: 文件名
					// 返回:
					// 0: 成功
					// -1: 失败
					if (FROM_WHERE == IdtGroup.TO_GROUP) {
						IDSApiProxyMgr.getCurProxy().iMSend(0, 3, callto_group_num, "", file_name,"");
					} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
						IDSApiProxyMgr.getCurProxy().iMSend(0, 3, callto_persion_num, "", file_name,"");
					}
				} else if (style == 4) {
					// 语音
					// sendVoice(PathConstant.LOCAL_FILE_STRING,
					// PathConstant.REMOTE_FTP_FILE);
					Toast.makeText(IdtChatActivity.this, "信息发送成功", Toast.LENGTH_SHORT).show();
					update_SMS_RESOURSE_RS_OK(callto_persion_num, uri);
					loadRichFileData();
					// 发送IM消息
					// 输入:
					// dwSn: 消息事务号
					// dwType: 及时消息类型,IM_TYPE_IMAGE等
					// pcTo: 目的号码
					// pcTxt: 文本内容
					// pcFileName: 文件名
					// 返回:
					// 0: 成功
					// -1: 失败
					if (FROM_WHERE == IdtGroup.TO_GROUP) {
						IDSApiProxyMgr.getCurProxy().iMSend(0, 4, callto_group_num, TIME_LENGTH + "", file_name,"");
					} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
						IDSApiProxyMgr.getCurProxy().iMSend(0, 4, callto_persion_num, TIME_LENGTH + "", file_name,"");
					}

				} else if (style == 5) {
					// 录像
					// sendVideo();
					Toast.makeText(IdtChatActivity.this, "信息发送成功", Toast.LENGTH_SHORT).show();
					update_SMS_RESOURSE_RS_OK(callto_persion_num, uri);
					loadRichFileData();
					// 发送IM消息
					// 输入:
					// dwSn: 消息事务号
					// dwType: 及时消息类型,IM_TYPE_IMAGE等
					// pcTo: 目的号码
					// pcTxt: 文本内容
					// pcFileName: 文件名
					// 返回:
					// 0: 成功
					// -1: 失败
					if (FROM_WHERE == IdtGroup.TO_GROUP) {
						IDSApiProxyMgr.getCurProxy().iMSend(0, 5, callto_group_num, SMS_RESOURCE_TIME_LENGTH + "",
								file_name,"");
					} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
						IDSApiProxyMgr.getCurProxy().iMSend(0, 5, callto_persion_num, SMS_RESOURCE_TIME_LENGTH + "",
								file_name,"");
					}

				} else if (style == 6) {
					// 任意文件
					// sendFile();
					Toast.makeText(IdtChatActivity.this, "信息发送成功", Toast.LENGTH_SHORT).show();
					update_SMS_RESOURSE_RS_OK(callto_persion_num, uri);
					loadRichFileData();
					// 发送IM消息
					// 输入:
					// dwSn: 消息事务号
					// dwType: 及时消息类型,IM_TYPE_IMAGE等
					// pcTo: 目的号码
					// pcTxt: 文本内容
					// pcFileName: 文件名
					// 返回:
					// 0: 成功
					// -1: 失败
					if (FROM_WHERE == IdtGroup.TO_GROUP) {
						if(FILE_IS_IMAGE==true){
							//相册
							IDSApiProxyMgr.getCurProxy().iMSend(0, 3, callto_group_num, "", file_name,"");
						}else{
							IDSApiProxyMgr.getCurProxy().iMSend(0, 6, callto_group_num, "", file_name,"");
						}
						
					} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
						if(FILE_IS_IMAGE==true){
							//相册
							IDSApiProxyMgr.getCurProxy().iMSend(0, 3, callto_persion_num, "", file_name,"");
						}else{
							IDSApiProxyMgr.getCurProxy().iMSend(0, 6, callto_persion_num, "", file_name,"");
						}
					}
				}

			} else if (msg.what == FILE_Download_SUCCESS) {
				Uri uri = (Uri) msg.obj;
				update_SMS_RESOURSE_RS_OK("", uri);
				loadRichFileData();
				Toast.makeText(IdtChatActivity.this, "信息接收成功", Toast.LENGTH_SHORT).show();
			} else if (msg.what == GET_PICTURE_FROM_CAMERA) {
				selectPicFromCamera();
			} else if (msg.what == SUCCESS_TAKE_PICTURE) {
				LwtLog.d("take_pic", "--------------SUCCESS_TAKE_PICTURE");
				// 先存储起来
				sendPicture();
			} else if (msg.what == GET_VIDEO_FROM_CAMERA) {
				selectVideoFromCamera();
			} else if (msg.what == SUCCESS_TAKE_VIDEO) {
				SMS_RESOURCE_STOP_TIME = System.currentTimeMillis();
				sendVideo();
			} else if (msg.what == GET_GPS_FROM_LOCAL) {
				getGPSFromLocalAndSend();
			} else if (msg.what == REFRESH_OK) {
				mPullToRefreshView.setLastUpdated("更新于:" + new Date().toLocaleString());
				mPullToRefreshView.onHeaderRefreshComplete();
			} else if (msg.what == GET_FILE_FROM_BROWSER) {
				sendFile();
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
////				tv_group_number.setText(caller);
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
								Toast.makeText(IdtChatActivity.this, "我在"+CurrentGroupCall.CURRENT_GROUP_CALL_NUM+"对讲组内发起对讲", Toast.LENGTH_SHORT).show();
								groupAudioStart_other();
								status = STATUS_TALK;
								setImageButtonBackground();
							}else{
								Toast.makeText(IdtChatActivity.this, "请先选择一个对讲组", Toast.LENGTH_SHORT).show();
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
				public void groupAudioStart_other() {
					// 是不是正在拨打电话
					if (IdtApplication.resumeCurrentCall()) {
						((NotificationManager) IdtChatActivity.this.getSystemService(Context.NOTIFICATION_SERVICE))
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
			loadRichFileData();
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
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_ui_idt_activity_chat);
		IdtApplication.getInstance().addActivity(this);
		FROM_WHERE = getIntent().getIntExtra("to_where", -1);
		initView();
		initData();
		setUpView();
		startGetLocation();
	}

	private void initData() {
		// callto_persion_info_member =
		// getIntent().getExtras().getParcelable("callto_persion_info");
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			callto_group_num = getIntent().getStringExtra("callto_group_num");
			title_textview.setText(callto_group_num + "");
			btnContainer.setVisibility(View.GONE); 
			groupBtnContainer.setVisibility(View.VISIBLE);  
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			callto_persion_num = getIntent().getExtras().getString("callto_persion_num");
			title_textview.setText(callto_persion_num + "");
			btnContainer.setVisibility(View.VISIBLE); 
			groupBtnContainer.setVisibility(View.GONE);  
		}

		my_phone_number = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
		talk = new TalkNetManager(); // 初始化一个网络对话管理类
		talk.setListener(IdtChatActivity.this);
		// talk.setUploadFileServerUrl(uploadServerUrl);// 设置文件上传网址
		// talk.setDownloadFileServerUrl(downloadServerUrl); // 设置文件下载网址
		talk.getRecordManger().setSoundAmplitudeListen(onSoundAmplitudeListen);// 设置振幅监听器
		talk.setDownloadFileFileStateListener(onDownloadFileFileStateListener);// 设置下载播放状态监听器
		talk.setUploadFileStateListener(onUploadFileStateListener);// 设置文件上传状态监听器
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtChatActivity.this.getApplication();
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

	/** 下载播放状态监听器 */
	private OnStateListener onDownloadFileFileStateListener = new OnStateListener() {

		@Override
		public void onState(int error, String msg) {
			// TODO Auto-generated method stub

		}
	};
	/** 文件上传状态监听器 */
	private OnStateListener onUploadFileStateListener = new OnStateListener() {

		@Override
		public void onState(int error, String msg) {
			// TODO Auto-generated method stub

		}
	};

	/** 回调振幅，根据振幅设置图片 */
	private SoundAmplitudeListen onSoundAmplitudeListen = new SoundAmplitudeListen() {

		@Override
		public void amplitude(int amplitude, int db, int value) {
			if (value >= 14) {
				value = 14;
			}
			// micImage.setBackgroundDrawable(micImages[value]);// 显示震幅图片
			micImage.setImageDrawable(micImages[value]);

		}
	};

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		mPullToRefreshView.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (list.size() % 10 == 0) {
					totalPageNum = list.size() / 10;
				} else {
					totalPageNum = (list.size() / 10) + 1;
				}
				LwtLog.d("onHeaderRefresh", "list.size:" + list.size() + ",总页数：" + totalPageNum + ",当前页数:" + pageNum);
				if (pageNum < totalPageNum) {
					pageNum++;
					getListData(pageNum);
					adapter.assignment(show_list);
					LwtLog.d(IdtApplication.WULIN_TAG, "通知adapter");
					adapter.notifyDataSetChanged();
					listView.setSelection(list.size() - (pageNum - 1) * 10);
					handler.sendEmptyMessage(REFRESH_OK);
				} else {
					Toast.makeText(IdtChatActivity.this, "数据已经加载完毕", 0).show();
					mPullToRefreshView.setLastUpdated("更新于:" + new Date().toLocaleString());
					mPullToRefreshView.onHeaderRefreshComplete();
				}
			}
		}, 1000);
	}

	private void getListData(int pageNum) {
		show_list.clear();
		if (list.size() <= pageNum * 10) {
			for (int i = 0; i < list.size(); i++) {
				show_list.add(list.get(i));
			}
		} else {
			for (int i = 0; i < (pageNum) * 10; i++) {
				show_list.add(list.get(list.size() - (pageNum) * 10 + i));
			}
		}
	}

	/**
	 * initView
	 */
	protected void initView() {
		intercom_state_textview = (TextView) findViewById(R.id.intercom_state_textview);
		intercom_image_button = (ImageButton) findViewById(R.id.intercom_button);
		intercom_image_button.setOnLongClickListener(this);
		intercom_image_button.setOnTouchListener(new MyClickListener());
		title_textview = (TextView) findViewById(R.id.page_title_name);
		recordingContainer = findViewById(R.id.recording_container);
		micImage = (ImageView) findViewById(R.id.mic_image);
		recordingHint = (TextView) findViewById(R.id.recording_hint);
		listView = (ListView) findViewById(R.id.list);
		mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
		edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
		buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
		buttonSend = findViewById(R.id.btn_send);
		buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
		emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
		btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
		groupBtnContainer = (LinearLayout) findViewById(R.id.ll_btn_container_some);
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pullToRefresh);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		// iv_emoticons_normal = (ImageView)
		// findViewById(R.id.iv_emoticons_normal);
		// iv_emoticons_checked = (ImageView)
		// findViewById(R.id.iv_emoticons_checked);
		loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
		btnMore = (Button) findViewById(R.id.btn_more);
		// iv_emoticons_normal.setVisibility(View.VISIBLE);
		// iv_emoticons_checked.setVisibility(View.INVISIBLE);
		more = findViewById(R.id.more);
		edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);

		// 动画资源文件,用于录制语音时
		micImages = new Drawable[] { getResources().getDrawable(R.drawable.record_animate_01),
				getResources().getDrawable(R.drawable.record_animate_02),
				getResources().getDrawable(R.drawable.record_animate_03),
				getResources().getDrawable(R.drawable.record_animate_04),
				getResources().getDrawable(R.drawable.record_animate_05),
				getResources().getDrawable(R.drawable.record_animate_06),
				getResources().getDrawable(R.drawable.record_animate_07),
				getResources().getDrawable(R.drawable.record_animate_08),
				getResources().getDrawable(R.drawable.record_animate_09),
				getResources().getDrawable(R.drawable.record_animate_10),
				getResources().getDrawable(R.drawable.record_animate_11),
				getResources().getDrawable(R.drawable.record_animate_12),
				getResources().getDrawable(R.drawable.record_animate_13),
				getResources().getDrawable(R.drawable.record_animate_14), };

		// 表情list
		reslist = getExpressionRes(35);
		// 初始化表情viewpager
		List<View> views = new ArrayList<View>();
		View gv1 = getGridChildView(1);
		View gv2 = getGridChildView(2);
		views.add(gv1);
		views.add(gv2);
		edittext_layout.requestFocus();
		buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
		mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
				} else {
					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
				}

			}
		});
		mEditTextContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
				more.setVisibility(View.GONE);
				// iv_emoticons_normal.setVisibility(View.VISIBLE);
				// iv_emoticons_checked.setVisibility(View.INVISIBLE);
				emojiIconContainer.setVisibility(View.GONE);
				if (FROM_WHERE == IdtGroup.TO_GROUP) {
					groupBtnContainer.setVisibility(View.GONE);  
				} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
					btnContainer.setVisibility(View.GONE); 
				}
			}
		});
		// 监听文字框
		mEditTextContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(s)) {
					btnMore.setVisibility(View.GONE);
					buttonSend.setVisibility(View.VISIBLE);
				} else {
					btnMore.setVisibility(View.VISIBLE);
					buttonSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			findViewById(R.id.container_location).setVisibility(View.GONE);
			findViewById(R.id.container_voice_call).setVisibility(View.GONE);
			findViewById(R.id.container_video).setVisibility(View.GONE);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
//			findViewById(R.id.container_voice_call_group).setVisibility(View.GONE);
		}
//		my_num_textview = (TextView) findViewById(R.id.my_num);
//		my_num_textview.setText(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
	}

	public void setUpView() {
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
		adapter = new SmsDetailAdapter(IdtChatActivity.this);
		adapter.setListener(this);
		// 显示消息
		listView.setAdapter(adapter);
		listView.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                more.setVisibility(View.GONE);
                return false;
            }
        });
		// 加载contentProvider中的内容
		getLoaderManager().initLoader(0, null, loaderCallback);
		clearNotifation();
		// 将该消息所在的会话状态设置为已读取
		updateSmsReadStatus();
	}

	// 将该消息所在的会话状态设置为已读取
	private int updateSmsReadStatus() {
		try {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(SmsConversationProvider.KEY_COLUMN_5_READ, 1);
			String where = "";
			if (FROM_WHERE == IdtGroup.TO_GROUP) {
				where = "(" + SmsConversationProvider.KEY_COLUMN_12_TARGET_PHONE_NUMBER + "='" + callto_group_num
						+ "' and " + SmsConversationProvider.KEY_COLUMN_13_OWNER_PHONE_NUMBER + "='" + my_phone_number
						+ "')";
			} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
				where = "((" + SmsConversationProvider.KEY_COLUMN_4_SMS_TYPE + "=1 and "
						+ SmsConversationProvider.KEY_COLUMN_12_TARGET_PHONE_NUMBER + "='" + my_phone_number + "' and "
						+ SmsConversationProvider.KEY_COLUMN_1_PHONE_NUMBER + "='" + callto_persion_num + "') or ("
						+ SmsConversationProvider.KEY_COLUMN_4_SMS_TYPE + "=2 and "
						+ SmsConversationProvider.KEY_COLUMN_12_TARGET_PHONE_NUMBER + "='" + callto_persion_num
						+ "' and " + SmsConversationProvider.KEY_COLUMN_1_PHONE_NUMBER + "='" + my_phone_number + "'))";
			}
			// Update the specified row.
			int updatedRowCount = getContentResolver().update(SmsConversationProvider.CONTENT_URI, updatedValues, where,
					null);
			return updatedRowCount;
		} catch (Throwable e) {
			LwtLog.d("wulin", "updateSmsReadStatus------------------此处有bug");
			e.printStackTrace();
			return 0;
		}
	}

	private int update_SMS_RESOURSE_RS_OK(String callto_persion_num, Uri uri) {
		try {
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 1);
			// Update the specified row.
			int updatedRowCount = getContentResolver().update(uri, updatedValues, null, null);
			return updatedRowCount;
		} catch (Throwable e) {
			LwtLog.d("wulin", "update_SMS_RESOURSE_RS_OK------------------此处有bug");
			e.printStackTrace();
			return 0;
		}
	}

	// // 更新文件路径
	// private int updateSmsResourceUrl() {
	// try {
	// ContentValues updatedValues = new ContentValues();
	// updatedValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL,
	// LOCAL_VOICE_FILE_STRING);
	// String where = SmsProvider.KEY_COLUMN_1_PHONE_NUMBER + "='" +
	// callto_persion_num + "'";
	//
	// // Update the specified row.
	// int updatedRowCount =
	// getContentResolver().update(SmsProvider.CONTENT_URI, updatedValues,
	// where,
	// null);
	//
	// return updatedRowCount;
	// } catch (Throwable e) {
	// e.printStackTrace();
	// return 0;
	// }
	// }

	// //获取到文件名
	// private String getSmsFileName(){
	// ssss
	// String where = SmsProvider.KEY_COLUMN_1_PHONE_NUMBER + "='" +
	// callto_persion_num + "'";
	// String[] projection=new
	// String[]{SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME};
	// Cursor cursor=getContentResolver().query(SmsProvider.CONTENT_URI,
	// projection, where, null, null);
	// String file_name="";
	// while(cursor.moveToNext()) {
	// cursor.getString(cursor.getColumnIndex(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME));
	// }
	// cursor.close();
	// return "";
	// }

	public void loadRichFileData() {
		// 加载contentProvider中的内容
		getLoaderManager().restartLoader(0, null, loaderCallback);
	}

	public void clearNotifation() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// notificationManager.cancel(AppConstants.SMS_NOTIFICATION_ID);
		notificationManager.cancelAll();
	}

	/**
	 * 消息图标点击事件
	 * 
	 * @param view
	 */
	@Override
	public void onClick(View view) {
		CommonUtils netStatus = new CommonUtils();
		if (netStatus.isConnectingToInternet(IdtChatActivity.this)) {
			int id = view.getId();
			if (id == R.id.btn_send) {
				// 点击发送按钮(发文字和表情)
				String s = mEditTextContent.getText().toString();
				sendText(s);
			} else if (id == R.id.btn_take_picture) {
				// 点击图片图标
			} else if (id == R.id.btn_picture || id == R.id.btn_picture_some) {
				if(IdtApplication.getCurrentCall()!=null){
					Toast.makeText(IdtChatActivity.this, "您另外的通信还没有结束", Toast.LENGTH_SHORT).show();
					return;
				}
				// 点击照相图标
				showImageDialog();
			} else if (id == R.id.btn_location) {
				// 位置
				handler.sendEmptyMessage(GET_GPS_FROM_LOCAL);
			} else if (id == R.id.btn_video) {
				if(IdtApplication.getCurrentCall()!=null){
					Toast.makeText(IdtChatActivity.this, "您另外的通信还没有结束", Toast.LENGTH_SHORT).show();
					return;
				}
				// 点击视频图标
				singerVedioCall();
			} else if (id == R.id.btn_file || id == R.id.btn_file_some) {
				if(IdtApplication.getCurrentCall()!=null){
					Toast.makeText(IdtChatActivity.this, "您另外的通信还没有结束", Toast.LENGTH_SHORT).show();
					return;
				}
				// 点击文件图标
				selectFileFromLocal();
			} else if (id == R.id.btn_voice_call) {
				if(IdtApplication.getCurrentCall()!=null){
					Toast.makeText(IdtChatActivity.this, "您另外的通信还没有结束", Toast.LENGTH_SHORT).show();
					return;
				}
				// 点击语音电话图标
				singerAudioStart();
			} 
//			else if (id == R.id.btn_voice_call_group_some) {
//				// 点击组呼语音电话图标
//				groupAudioStart();
//			} 
			else if (id == R.id.map) {
				// 点击地图图标
				Intent intent = new Intent(IdtChatActivity.this, IdtMap.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else if (id == R.id.return_button) {
				// 点击屏幕上方返回键
				IdtApplication.getInstance().deleteActivity(this);
			} else if (id == R.id.btn_video_luxiang || id == R.id.btn_video_luxiang_some) {
				if(IdtApplication.getCurrentCall()!=null){
					Toast.makeText(IdtChatActivity.this, "您另外的通信还没有结束", Toast.LENGTH_SHORT).show();
					return;
				}
				// 发送录像
				handler.sendEmptyMessage(GET_VIDEO_FROM_CAMERA);
			}
		} else {
			Toast.makeText(IdtChatActivity.this, "没有可以使用的网络，请打开您的网络", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	DialogInterface.OnClickListener imageDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case ImageGetDialog.BUTTON_TAKE_PICTURE:
				handler.sendEmptyMessage(GET_PICTURE_FROM_CAMERA);
				dialog.dismiss();
				break;
			case ImageGetDialog.BUTTON_SELECT_PICTURE:
				selectPictureFromLocal();
				dialog.dismiss();
				break;
			case ImageGetDialog.BUTTON_CANCEL:
				dialog.dismiss();
				break;
			default:
				break;
			}
		}
	};
	
	private void showImageDialog() {
		new ImageGetDialog.Builder(this).setTakePictureButton(imageDialogListener)
				.setSelectPictureButton(imageDialogListener).setCalcelButton(imageDialogListener).create().show();
	}
	


	// 单呼别人
	public void singerAudioStart() {
		if (IdtApplication.resumeCurrentCall()) {
			((NotificationManager) IdtChatActivity.this.getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(AppConstants.CALL_NOTIFICATION_ID);
			IdtChatActivity.this.startActivity(IdtApplication.getCurrentCall().getIntent());
			return;
		}
		MediaAttribute pAttr = new MediaAttribute();
		pAttr.ucAudioRecv = 1;
		pAttr.ucAudioSend = 1;
		pAttr.ucVideoRecv = 0;
		pAttr.ucVideoSend = 0;
		// 传递一个序列化的对象、对方电话号码 到so/c端
		int id = IDSApiProxyMgr.getCurProxy().CallMakeOut(callto_persion_num, AppConstants.CALL_TYPE_SINGLE_CALL, pAttr,
				0);
		// 记忆当前呼叫
		IdtApplication.setCurrentCall(new CallEntity(id, CallType.AUDIO_CALL));
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>开始呼叫" + callto_persion_num + ", id : " + id);
		Intent intent = new Intent(IdtChatActivity.this, ActivityAudioCall.class);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLID, id);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLER, my_phone_number);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, callto_persion_num);
		intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityVideoCall.FLAG_CALLING);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		IdtChatActivity.this.startActivityForResult(intent, SINGR_AUDIO_CALL_REQUEST);
	}

	// 点击组呼语音电话图标
	public void groupAudioStart() {
		// 是不是正在拨打电话
		if (IdtApplication.resumeCurrentCall()) {
			((NotificationManager) IdtChatActivity.this.getSystemService(Context.NOTIFICATION_SERVICE))
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
		String callNum = AppConstants.getLockedGroupNum().equals("") ? callto_group_num
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
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>开始组呼，组号码: " + callNum + ", id : " + id);
		Intent intent = new Intent(this, ActivityGroupCall.class);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLID, id);
		// 对方号码
		intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, callto_group_num);
		intent.putExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM, callNum);
		intent.putExtra(AppConstants.EXTRA_KEY_CALLER, my_phone_number);
		intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityGroupCall.FLAG_CALLING);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivityForResult(intent, SINGR_GROUP_AUDIO_CALL_REQUEST);
	}

	// 单独视频对话
	public void singerVedioCall() {
		if (IdtApplication.resumeCurrentCall()) {
			((NotificationManager) IdtChatActivity.this.getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(AppConstants.CALL_NOTIFICATION_ID);
			IdtChatActivity.this.startActivity(IdtApplication.getCurrentCall().getIntent());
			return;
		}
		MediaAttribute mediaAttribute = new MediaAttribute();
		mediaAttribute.ucAudioRecv = 1;
		mediaAttribute.ucAudioSend = 1;
		mediaAttribute.ucVideoRecv = 1;
		mediaAttribute.ucVideoSend = 1;
		int callId = IDSApiProxyMgr.getCurProxy().CallMakeOut(callto_persion_num, AppConstants.CALL_TYPE_SINGLE_CALL,
				mediaAttribute, 0);
		IdtApplication.setCurrentCall(new CallEntity(callId, CallType.VEDIO_CALL));
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>开始视频呼叫" + callto_persion_num + ", id : " + callId);
		Intent intent = new Intent(IdtChatActivity.this, ActivityVideoCall.class);
		// 视频呼叫id
		intent.putExtra(AppConstants.EXTRA_KEY_CALLID, callId);
		// 对方号码
		intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, callto_persion_num);
		// 自己号码
		intent.putExtra(AppConstants.EXTRA_KEY_CALLER, my_phone_number);
		// 媒体配置
		intent.putExtra(AppConstants.EXTRA_KEY_MediaAttr, mediaAttribute);
		// 当前状态
		intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityAudioCall.FLAG_CALLING);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SINGR_AUDIO_VIDEO_CALL_REQUEST);
	}

	/**
	 * 照相获取图片
	 */
	public void selectPicFromCamera() {
		if (!CommonUtils.isExitsSdcard()) {
			Toast.makeText(getApplicationContext(), "SD卡不存在，不能拍照", Toast.LENGTH_SHORT).show();
			return;
		}
		LwtLog.d("take_pic", "--------------selectPicFromCamera");
		createFileNameAndPath(my_phone_number, 3);
		LwtLog.d("take_pic", "--------------LOCAL_VOICE_FILE_STRING:" + PathConstant.LOCAL_FILE_STRING);
		File cameraFile = new File(PathConstant.LOCAL_FILE_STRING);
		LwtLog.d("take_pic", cameraFile == null ? "cameraFile is null" : "cameraFile is not null");
		cameraFile.getParentFile().mkdirs();
		LwtLog.d("take_pic", "--------------startActivityForResult");
		startActivityForResult(
				new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
				REQUEST_CODE_CAMERA);
	}

	/**
	 * 选择文件
	 */
	private void selectFileFromLocal() {
		if (!CommonUtils.isExitsSdcard()) {
			Toast.makeText(getApplicationContext(), "SD卡不存在，不能拍照", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = null;
//		if (Build.VERSION.SDK_INT < 19) {
//			intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.setType("*/*");
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//		} else {
//			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		}
		FILE_IS_IMAGE = false;
		intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
	}
	
	/**
	 * 选择图片
	 */
	private void selectPictureFromLocal() {
		Intent intent = null;
//		if (Build.VERSION.SDK_INT < 19) {
//			intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.setType("*/*");
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//		} else {
//			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		}
		FILE_IS_IMAGE = true;
		intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
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
		createFileNameAndPath(my_phone_number, 5);
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

	/*****
	 * @see copy funtion to you project
	 *      定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
	 *
	 */
	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				StringBuffer sb = new StringBuffer(256);
				local_get_location = location;
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
				// if (location.getLocType() == BDLocation.TypeGpsLocation) {//
				// GPS定位结果
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
				// LwtLog.d("location", sb.toString());
			}
		}

	};

	/**
	 * 获取gps并发送给服务器
	 */
	public void getGPSFromLocalAndSend() {
		if (local_get_location != null) {
			LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>调用接口发送信息....");
			// IDSApiProxyMgr.getCurProxy().SendIM(phone_number,
			// callto_persion_num,
			// content,
			// content.length());

			// 发送IM消息
			// 输入:
			// dwSn: 消息事务号
			// dwType: 及时消息类型,IM_TYPE_IMAGE等
			// pcTo: 目的号码
			// pcTxt: 文本内容
			// pcFileName: 文件名
			// 返回:
			// 0: 成功
			// -1: 失败
			IDSApiProxyMgr.getCurProxy().iMSend(0, 2, callto_persion_num,
					local_get_location.getLatitude() + "," + local_get_location.getLongitude()+","+local_get_location.getAddress().address, null,"");
			// 保存发送记录·
			ContentValues contentValues = new ContentValues();
			contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, my_phone_number);
			contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT,
					local_get_location.getLatitude() + "," + local_get_location.getLongitude()+","+local_get_location.getAddress().address);
			contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
			contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
			contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, "");
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 2);
			contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, "");
			contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_persion_num);
			contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, my_phone_number);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
			contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
			Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
			try {
				IdtApplication.getCurrentCall().setUri(uri);
			} catch (Exception e) {
				// TODO: handle exception
			}
			loadRichFileData();
			LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>信息保存成功");
			Toast.makeText(IdtChatActivity.this, "信息发送成功", Toast.LENGTH_SHORT).show();
			mEditTextContent.setText("");
		} else {
			Toast.makeText(IdtChatActivity.this, "获取您的位置信息失败", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		locationService.unregisterListener(mListener); // 注销掉监听
		locationService.stop(); // 停止定位服务
	}

	/**
	 * onActivityResult
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LwtLog.d("take_pic", "--------------onActivityResult");
		if (resultCode == RESULT_OK) {
			LwtLog.d("take_pic", "--------------RESULT_OK");
			if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
				handler.sendEmptyMessage(SUCCESS_TAKE_PICTURE);
			} else if (requestCode == REQUEST_CODE_VIDEO) {
				handler.sendEmptyMessage(SUCCESS_TAKE_VIDEO);
			} else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						String filePath = null;
						if ("content".equalsIgnoreCase(uri.getScheme())) {
							String[] projection = { "_data" };
							Cursor cursor = null;

							try {
								cursor = getContentResolver().query(uri, projection, null, null, null);
								int column_index = cursor.getColumnIndexOrThrow("_data");
								if (cursor.moveToFirst()) {
									filePath = cursor.getString(column_index);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if ("file".equalsIgnoreCase(uri.getScheme())) {
							filePath = uri.getPath();
						}
						File file = new File(filePath);
						if (file == null || !file.exists()) {
							Toast.makeText(getApplicationContext(), "文件不存在", Toast.LENGTH_SHORT).show();
							return;
						} else {
							createFileNameAndPath(filePath, my_phone_number, 6);
							// startUploadFile(6);
							handler.sendEmptyMessage(GET_FILE_FROM_BROWSER);
						}

					}
				}
			} else if (requestCode == SINGR_AUDIO_CALL_REQUEST) {
				// 单呼
				loadRichFileData();
			} else if (requestCode == SINGR_GROUP_AUDIO_CALL_REQUEST) {
				loadRichFileData();
			}
		}
	}

	// 生成本地和远程路径和文件名
	private void createFileNameAndPath(String file_path, String directory_num, int file_type) {
		String data_time = DateUtil.dateToString(new Date(), DateUtil.TIME_PATTERN_6);
		String random_string = StringsUtils.getRandomString(8);
		PathConstant.REMOTE_FTP_PATH = "/IM/" + directory_num;
		if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 存在sd卡
			return;
		}
		File file = new File(file_path);
		String file_name = file.getName();
		String prefix = file_name.substring(file_name.lastIndexOf(".") + 1);
		PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + "." + prefix;
		PathConstant.LOCAL_FILE_STRING = file_path;
		SharedPreferencesUtil.setStringPreferences(IdtChatActivity.this, "REMOTE_FTP_PATH",
				PathConstant.REMOTE_FTP_PATH);
		SharedPreferencesUtil.setStringPreferences(IdtChatActivity.this, "REMOTE_FTP_FILE",
				PathConstant.REMOTE_FTP_FILE);
		SharedPreferencesUtil.setStringPreferences(IdtChatActivity.this, "LOCAL_FILE_STRING",
				PathConstant.LOCAL_FILE_STRING);
	}

	/**
	 * 从图库获取图片
	 */
	public void selectPicFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, REQUEST_CODE_LOCAL);
	}

	/**
	 * 发送文本消息
	 * 
	 * @param content
	 *            message content
	 * @param isResend
	 *            boolean resend
	 */
	private void sendText(String content) {
		if ("".equals(content) || content.length() > 200) {
			Toast.makeText(getApplicationContext(), "短信内容不能为空，且在200字以内", Toast.LENGTH_SHORT).show();
			return;
		}
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>调用接口发送信息....");
		// IDSApiProxyMgr.getCurProxy().SendIM(phone_number, callto_persion_num,
		// content,
		// content.length());

		// 发送IM消息
		// 输入:
		// dwSn: 消息事务号
		// dwType: 及时消息类型,IM_TYPE_IMAGE等
		// pcTo: 目的号码
		// pcTxt: 文本内容
		// pcFileName: 文件名
		// 返回:
		// 0: 成功
		// -1: 失败
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			IDSApiProxyMgr.getCurProxy().iMSend(0, 1, callto_group_num, content, null,"");
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			IDSApiProxyMgr.getCurProxy().iMSend(0, 1, callto_persion_num, content, null,"");
		}
		// 保存发送记录·
		ContentValues contentValues = new ContentValues();
		contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, content);
		contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
		contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
		contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, "");
		contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 1);
		contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, "");
		contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, 0);
		contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_group_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_persion_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
		}
		contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
		Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
		try {
			IdtApplication.getCurrentCall().setUri(uri);
		} catch (Exception e) {
			// TODO: handle exception
		}
		loadRichFileData();
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>信息保存成功");
		mEditTextContent.setText("");
	}

	// 加载所有富文本
	LoaderManager.LoaderCallbacks<Cursor> loaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>加载所有短信");
			// 当收到新数据以后，初始化页码状态
			pageNum = 1;
			LwtLog.d("take_pic", "111111");
			if (FROM_WHERE == IdtGroup.TO_GROUP) {
				//CursorLoader异步查询
				return new CursorLoader(IdtChatActivity.this, SmsProvider.CONTENT_URI, SmsProvider.ALL_PROJECTION,
						"target_phone_number = '" + callto_group_num + "' and owner_phone_number = '"
								+ my_phone_number + "'",
						null, SmsProvider.DEFAULT_SORT_ORDER);
			} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
				return new CursorLoader(IdtChatActivity.this, SmsProvider.CONTENT_URI, SmsProvider.ALL_PROJECTION,
						"(sms_type=2 and phone_number = '" + my_phone_number + "' and target_phone_number = '"
								+ callto_persion_num + "') or " + "(sms_type=1 and phone_number = '"
								+ callto_persion_num + "' and target_phone_number = '" + my_phone_number + "')",
						null, SmsProvider.DEFAULT_SORT_ORDER);
			} else {
				return null;
			}
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			LwtLog.d(IdtApplication.WULIN_TAG, "onLoadFinished");
			handleCursor(cursor);
			getListData(pageNum);
			adapter.assignment(show_list);
			LwtLog.d(IdtApplication.WULIN_TAG, "通知adapter");
			adapter.notifyDataSetChanged();
			listView.setSelection(show_list.size());// 获取焦点
			handler.sendEmptyMessage(REFRESH_OK);
		}

		public void onLoaderReset(Loader<Cursor> loader) {
			LwtLog.d(IdtApplication.WULIN_TAG, "onLoaderReset");
			handleCursor(null);
			show_list.clear();
			adapter.assignment(show_list);
			LwtLog.d(IdtApplication.WULIN_TAG, "通知adapter");
			adapter.notifyDataSetChanged();
			listView.setSelection(show_list.size());// 获取焦点
			handler.sendEmptyMessage(REFRESH_OK);
		}
	};

	// 将从provider中获取的数据进行二次加工
	public void handleCursor(Cursor cursor) {
		list.clear();
		if (cursor == null || cursor.getCount() == 0)
			return;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			SmsEntity smsBean = new SmsEntity();
			// 信息id
			smsBean.set_id(cursor.getInt(0));
			// 对方电话号码
			smsBean.setPhone_number(cursor.getString(1));
			// 信息内容
			smsBean.setSms_content(cursor.getString(2));
			// 信息种类
			smsBean.setSms_type(cursor.getInt(3));
			smsBean.setRead(cursor.getInt(4));
			smsBean.setCreate_time(cursor.getString(5));
			smsBean.setSms_resource_url(cursor.getString(6));
			// 0:not 1:text 2:gps position 3:voice or weixin 4:vedio
			smsBean.setSms_resource_type(cursor.getInt(7));
			smsBean.setSms_resource_name(cursor.getString(8));
			smsBean.setSms_resource_time_length(cursor.getInt(9));
			smsBean.setSms_resource_rs_ok(cursor.getInt(10));
			smsBean.setTarget_phone_number(cursor.getString(11));
			smsBean.setOwner_phone_number(cursor.getString(12));
			smsBean.setIs_group_message(cursor.getInt(13));
			smsBean.setUiCause(cursor.getInt(14)); 
			if (smsBean.getSms_type() == 1) {
				if (smsBean.getSms_resource_type() == 1) {
					// 接收文字信息
					smsBean.setLayoutID(R.layout.row_received_message);
				} else if (smsBean.getSms_resource_type() == 4) {
					// 接收语音信息
					smsBean.setLayoutID(R.layout.row_received_voice);
				} else if (smsBean.getSms_resource_type() == 3) {
					// 接收图片信息
					smsBean.setLayoutID(R.layout.row_received_picture);
				} else if (smsBean.getSms_resource_type() == 5) {
					// 接收视频信息
					smsBean.setLayoutID(R.layout.row_received_video);
				} else if (smsBean.getSms_resource_type() == 2) {
					// 定位信息
					smsBean.setLayoutID(R.layout.row_received_location);
				} else if (smsBean.getSms_resource_type() == 6) {
					// 文件信息
					smsBean.setLayoutID(R.layout.row_received_file);
				} else if (smsBean.getSms_resource_type() == 7) {
					// 语音单呼
					smsBean.setLayoutID(R.layout.row_received_voice_call);
				} else if (smsBean.getSms_resource_type() == 8) {
					// 语音单呼视频
					smsBean.setLayoutID(R.layout.row_received_voice_call);
				} else if (smsBean.getSms_resource_type() == 9) {
					// 群呼无视频
					smsBean.setLayoutID(R.layout.row_received_voice_call);
				}else if(smsBean.getSms_resource_type() == 17){
					//接收会议通知
					JsonOperation jsonOperation=new JsonOperation();
					final MeetingMsgData meetingMsgData=jsonOperation.meetingJsonStringParse(smsBean.getSms_content());
					if(meetingMsgData.type==JsonOperation.METTING_NOTICE){
						smsBean.setLayoutID(R.layout.row_received_meeting_call);
					}else if(meetingMsgData.type==JsonOperation.METTING_LINK){
						smsBean.setLayoutID(R.layout.row_received_meeting_call);
					}else if(meetingMsgData.type==JsonOperation.METTING_REPLY){
						smsBean.setLayoutID(R.layout.row_received_message);
					}
				}
			} else if (smsBean.getSms_type() == 2) {
				if (smsBean.getSms_resource_type() == 1) {
					// 发送语音信息
					smsBean.setLayoutID(R.layout.row_sent_message);
				} else if (smsBean.getSms_resource_type() == 4) {
					// 发送语音信息
					smsBean.setLayoutID(R.layout.row_sent_voice);
				} else if (smsBean.getSms_resource_type() == 3) {
					// 发送图片信息
					smsBean.setLayoutID(R.layout.row_sent_picture);
				} else if (smsBean.getSms_resource_type() == 5) {
					// 发送视频信息
					smsBean.setLayoutID(R.layout.row_sent_video);
				} else if (smsBean.getSms_resource_type() == 2) {
					// 发送定位信息
					smsBean.setLayoutID(R.layout.row_sent_location);
				} else if (smsBean.getSms_resource_type() == 6) {
					// 发送文件信息
					smsBean.setLayoutID(R.layout.row_sent_file);
				} else if (smsBean.getSms_resource_type() == 7) {
					// 语音单呼
					smsBean.setLayoutID(R.layout.row_sent_voice_call);
				} else if (smsBean.getSms_resource_type() == 8) {
					// 语音单呼视频
					smsBean.setLayoutID(R.layout.row_sent_voice_call);
				} else if (smsBean.getSms_resource_type() == 9) {
					// 群呼无视频
					smsBean.setLayoutID(R.layout.row_sent_voice_call);
				}else if(smsBean.getSms_resource_type() == 17){
					//发送会议通知
					JsonOperation jsonOperation=new JsonOperation();
					final MeetingMsgData meetingMsgData=jsonOperation.meetingJsonStringParse(smsBean.getSms_content());
					if(meetingMsgData.type==JsonOperation.METTING_NOTICE){
					}else if(meetingMsgData.type==JsonOperation.METTING_LINK){
					}else if(meetingMsgData.type==JsonOperation.METTING_REPLY){
						smsBean.setLayoutID(R.layout.row_sent_message);
					}
				}
			}
			LwtLog.d(IdtApplication.WULIN_TAG,
					"handleCursor加载短信>>>>>>>>>>>>>>>" + ",time:" + cursor.getString(5) + ", type: "
							+ smsBean.getSms_type() + ",content:" + cursor.getString(2) + ",file_url:"
							+ cursor.getString(6));
			LwtLog.d("take_pic", "handleCursor加载短信>>>>>>>>>>>>>>>" + ",time:" + cursor.getString(5) + ", type: "
					+ smsBean.getSms_type() + ",content:" + cursor.getString(2) + ",file_url:" + cursor.getString(6));
			LwtLog.d("location", "handleCursor加载短信>>>>>>>>>>>>>>>" + ",time:" + cursor.getString(5) + ", type: "
					+ smsBean.getSms_type() + ",content:" + cursor.getString(2) + ",file_url:" + cursor.getString(6));
			list.add(smsBean);
		}
	}

	/**
	 * 发送语音
	 * 
	 * @param filePath
	 * @param fileName
	 * @param length
	 * @param isResend
	 */
	private void sendVoice(String filePath, String file_name) {
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>调用接口发送信息....");
		// 保存发送记录·
		ContentValues contentValues = new ContentValues();
		contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, "");
		contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
		contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
		contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, filePath);
		contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 4);
		contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, file_name);
		contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, TIME_LENGTH);
		contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_group_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_persion_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
		}
		contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
		Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
		try {
			IdtApplication.getCurrentCall().setUri(uri);
		} catch (Exception e) {
			// TODO: handle exception
		}
		loadRichFileData();
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			startUploadFile(4, uri, callto_group_num, file_name);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			startUploadFile(4, uri, callto_persion_num, file_name);
		}
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>信息保存成功");
	}

	/**
	 * 发送视频消息
	 */
	private void sendVideo() {
		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_PATH", "");
		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_FILE", "");
		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"LOCAL_FILE_STRING", "");
		LwtLog.d("take_pic", ">>>>>>>>>>sendPicture....file_path:" + PathConstant.LOCAL_FILE_STRING + ",file_name:"
				+ PathConstant.REMOTE_FTP_FILE);
		SMS_RESOURCE_TIME_LENGTH = (int) Math
				.ceil(((double) (SMS_RESOURCE_STOP_TIME - SMS_RESOURCE_START_TIME)) / 1000);
		// 保存发送记录·
		ContentValues contentValues = new ContentValues();
		contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, "");
		contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
		contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
		contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, PathConstant.LOCAL_FILE_STRING);
		contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 5);
		contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, PathConstant.REMOTE_FTP_FILE);
		contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, SMS_RESOURCE_TIME_LENGTH);
		contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_group_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_persion_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
		}
		contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
		Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
		try {
			IdtApplication.getCurrentCall().setUri(uri);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			startUploadFile(5, uri, callto_group_num, PathConstant.REMOTE_FTP_FILE);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			startUploadFile(5, uri, callto_persion_num, PathConstant.REMOTE_FTP_FILE);
		}
		loadRichFileData();
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>信息保存成功");
		// 如果输入法在窗口上已经显示，则隐藏，反之则显示
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		mEditTextContent.setText("");
	}

	public void sendPicture() {
		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_PATH", "");
		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_FILE", "");
		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"LOCAL_FILE_STRING", "");
		LwtLog.d("take_pic", ">>>>>>>>>>sendPicture....file_path:" + PathConstant.LOCAL_FILE_STRING + ",file_name:"
				+ PathConstant.REMOTE_FTP_FILE);
		// 保存发送记录·
		ContentValues contentValues = new ContentValues();
		contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, "");
		contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
		contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
		contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, PathConstant.LOCAL_FILE_STRING);
		contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 3);
		contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, PathConstant.REMOTE_FTP_FILE);
		contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, "");
		contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_group_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_persion_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
		}
		contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
		//通过ContentResolver接口将provider的数据存入数据库中
		Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
		try {
			IdtApplication.getCurrentCall().setUri(uri);
		} catch (Exception e) {
			// TODO: handle exception
		}
		LwtLog.d("take_pic", "loadRichFileData之前");
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			startUploadFile(3, uri, callto_group_num, PathConstant.REMOTE_FTP_FILE);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			startUploadFile(3, uri, callto_persion_num, PathConstant.REMOTE_FTP_FILE);
		}
		loadRichFileData();
		LwtLog.d("take_pic", "loadRichFileData之后");
		// 如果输入法在窗口上已经显示，则隐藏，反之则显示
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		mEditTextContent.setText("");
	}

	/**
	 * 发送文件
	 * 
	 * @param uri
	 */
	private void sendFile() {
		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_PATH", "");
		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_FILE", "");
		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"LOCAL_FILE_STRING", "");
		LwtLog.d("take_pic", ">>>>>>>>>>sendPicture....file_path:" + PathConstant.LOCAL_FILE_STRING + ",file_name:"
				+ PathConstant.REMOTE_FTP_FILE);
		// 保存发送记录·
		ContentValues contentValues = new ContentValues();
		contentValues.put(SmsProvider.KEY_COLUMN_1_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_2_SMS_CONTENT, "");
		contentValues.put(SmsProvider.KEY_COLUMN_3_SMS_TYPE, 2);
		contentValues.put(SmsProvider.KEY_COLUMN_5_CREATE_TIME, DateUtil.formatDate(null, null));
		contentValues.put(SmsProvider.KEY_COLUMN_6_SMS_RESOURCE_URL, PathConstant.LOCAL_FILE_STRING);
		if(FILE_IS_IMAGE==true){
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 3);
		}else{
			contentValues.put(SmsProvider.KEY_COLUMN_7_SMS_RESOURCE_TYPE, 6);
		}
		contentValues.put(SmsProvider.KEY_COLUMN_8_SMS_RESOURCE_NAME, PathConstant.REMOTE_FTP_FILE);
		contentValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, "");
		contentValues.put(SmsProvider.KEY_COLUMN_10_SMS_RESOURCE_RS_OK, 0);
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_group_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,1);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			contentValues.put(SmsProvider.KEY_COLUMN_11_TARGET_PHONE_NUMBER, callto_persion_num);
			contentValues.put(SmsProvider.KEY_COLUMN_13_IS_GROUP_MESSAGE,0);
		}
		contentValues.put(SmsProvider.KEY_COLUMN_12_OWNER_PHONE_NUMBER, my_phone_number);
		contentValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, -1);
		Uri uri=getContentResolver().insert(SmsProvider.CONTENT_URI, contentValues);
		try {
			IdtApplication.getCurrentCall().setUri(uri);
		} catch (Exception e) {
			// TODO: handle exception
		}
		LwtLog.d("take_pic", "loadRichFileData之前");
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			startUploadFile(6, uri, callto_group_num, PathConstant.REMOTE_FTP_FILE);
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			startUploadFile(6, uri, callto_persion_num, PathConstant.REMOTE_FTP_FILE);
		}
		loadRichFileData();
		LwtLog.d("take_pic", "loadRichFileData之后");
		// 如果输入法在窗口上已经显示，则隐藏，反之则显示
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		mEditTextContent.setText("");
	}

	private void startUploadFile(int style, Uri uri, String callto_persion_num, String filename) {
		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_PATH", "");
		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"REMOTE_FTP_FILE", "");
		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this,
				"LOCAL_FILE_STRING", "");
		ftpBuinessLayer = new FtpBuinessLayer(IdtChatActivity.this);
		ftpBuinessLayer.setListener(IdtChatActivity.this);
		ftpBuinessLayer.uploadFile(PathConstant.LOCAL_FILE_STRING, PathConstant.REMOTE_FTP_PATH, style, uri,
				callto_persion_num, filename, PathConstant.REMOTE_FTP_FILE);
	}

	/**
	 * 根据图库图片uri发送图片
	 * 
	 * @param selectedImage
	 */
	private void sendPicByUri(Uri selectedImage) {
		// // String[] filePathColumn = { MediaStore.Images.Media.DATA };
		// Cursor cursor = getContentResolver().query(selectedImage, null, null,
		// null, null);
		// if (cursor != null) {
		// cursor.moveToFirst();
		// int columnIndex = cursor.getColumnIndex("_data");
		// String picturePath = cursor.getString(columnIndex);
		// cursor.close();
		// cursor = null;
		//
		// if (picturePath == null || picturePath.equals("null")) {
		// Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
		// toast.setGravity(Gravity.CENTER, 0, 0);
		// toast.show();
		// return;
		// }
		// sendPicture(picturePath, false);
		// } else {
		// File file = new File(selectedImage.getPath());
		// if (!file.exists()) {
		// Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
		// toast.setGravity(Gravity.CENTER, 0, 0);
		// toast.show();
		// return;
		//
		// }
		// sendPicture(file.getAbsolutePath(), false);
		// }

	}

	/**
	 * 重发消息
	 */
	private void resendMessage() {
	}

	/**
	 * 显示语音图标按钮
	 * 
	 * @param view
	 */
	public void setModeVoice(View view) {
		hideKeyboard();
		edittext_layout.setVisibility(View.GONE);
		more.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeKeyboard.setVisibility(View.VISIBLE);
		buttonSend.setVisibility(View.GONE);
		btnMore.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.VISIBLE);
		// iv_emoticons_normal.setVisibility(View.VISIBLE);
		// iv_emoticons_checked.setVisibility(View.INVISIBLE);
		if (FROM_WHERE == IdtGroup.TO_GROUP) {
			groupBtnContainer.setVisibility(View.VISIBLE);  
		} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
			btnContainer.setVisibility(View.VISIBLE); 
		}
		emojiIconContainer.setVisibility(View.GONE);

	}

	/**
	 * 显示键盘图标
	 * 
	 * @param view
	 */
	public void setModeKeyboard(View view) {
		edittext_layout.setVisibility(View.VISIBLE);
		more.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeVoice.setVisibility(View.VISIBLE);
		// mEditTextContent.setVisibility(View.VISIBLE);
		mEditTextContent.requestFocus();
		// buttonSend.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.GONE);
		if (TextUtils.isEmpty(mEditTextContent.getText())) {
			btnMore.setVisibility(View.VISIBLE);
			buttonSend.setVisibility(View.GONE);
		} else {
			btnMore.setVisibility(View.GONE);
			buttonSend.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 显示或隐藏图标按钮页
	 * 
	 * @param view
	 */
	public void more(View view) {
		if (more.getVisibility() == View.GONE) {
			System.out.println("more gone");
			hideKeyboard();
			more.setVisibility(View.VISIBLE);
			if (FROM_WHERE == IdtGroup.TO_GROUP) {
				groupBtnContainer.setVisibility(View.VISIBLE);  
			} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
				btnContainer.setVisibility(View.VISIBLE); 
			}
			emojiIconContainer.setVisibility(View.GONE);
		} else {
			if (emojiIconContainer.getVisibility() == View.VISIBLE) {
				emojiIconContainer.setVisibility(View.GONE);
				if (FROM_WHERE == IdtGroup.TO_GROUP) {
					groupBtnContainer.setVisibility(View.VISIBLE);  
				} else if (FROM_WHERE == IdtGroup.TO_PERSION) {
					btnContainer.setVisibility(View.VISIBLE); 
				}
				// iv_emoticons_normal.setVisibility(View.VISIBLE);
				// iv_emoticons_checked.setVisibility(View.INVISIBLE);
			} else {
				more.setVisibility(View.GONE);
			}

		}

	}

	/**
	 * 点击文字输入框
	 * 
	 * @param v
	 */
	public void editClick(View v) {
		listView.setSelection(listView.getCount() - 1);
		if (more.getVisibility() == View.VISIBLE) {
			more.setVisibility(View.GONE);
			// iv_emoticons_normal.setVisibility(View.VISIBLE);
			// iv_emoticons_checked.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * 消息广播接收者
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

	/**
	 * 消息回执BroadcastReceiver
	 */
	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

		}
	};

	/**
	 * 消息送达BroadcastReceiver
	 */
	private BroadcastReceiver deliveryAckMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};
	private PowerManager.WakeLock wakeLock;

	// 生成本地和远程路径和文件名
	private void createFileNameAndPath(String directory_num, int file_type) {
		String data_time = DateUtil.dateToString(new Date(), DateUtil.TIME_PATTERN_6);
		String random_string = StringsUtils.getRandomString(8);
		PathConstant.REMOTE_FTP_PATH = "/IM/" + directory_num;
		if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 存在sd卡
			return;
		}
		if (file_type == 4) {
			PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".amr";
		} else if (file_type == 3) {
			PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".png";
		} else if (file_type == 5) {
			PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".mp4";
		}
		PathConstant.LOCAL_FILE_STRING = Environment.getExternalStorageDirectory().getPath() + "/IDT-MA"
				+ PathConstant.REMOTE_FTP_PATH + "/" + PathConstant.REMOTE_FTP_FILE;
		SharedPreferencesUtil.setStringPreferences(IdtChatActivity.this, "REMOTE_FTP_PATH",
				PathConstant.REMOTE_FTP_PATH);
		SharedPreferencesUtil.setStringPreferences(IdtChatActivity.this, "REMOTE_FTP_FILE",
				PathConstant.REMOTE_FTP_FILE);
		SharedPreferencesUtil.setStringPreferences(IdtChatActivity.this, "LOCAL_FILE_STRING",
				PathConstant.LOCAL_FILE_STRING);
	}

	/**
	 * 按住说话listener
	 * 
	 */
	class PressToSpeakListen implements View.OnTouchListener {
		@SuppressLint({ "ClickableViewAccessibility", "Wakelock" })
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!CommonUtils.isExitsSdcard()) {
					Toast.makeText(IdtChatActivity.this, "发送语音需要sdcard支持！", Toast.LENGTH_SHORT).show();
					return false;
				}
				CommonUtils netStatus = new CommonUtils();
				if (netStatus.isConnectingToInternet(IdtChatActivity.this)) {
				} else {
					Toast.makeText(IdtChatActivity.this, "没有可以使用的网络，请打开您的网络", Toast.LENGTH_SHORT).show();
					return false;
				}
				LwtLog.d(IdtApplication.WULIN_TAG,
						"PressToSpeakListen=======================before===IDSApiProxyMgr.getCurProxy().iMGetFileName");
				// IDSApiProxyMgr.getCurProxy().iMGetFileName(0,
				// callto_persion_num, 4);
				createFileNameAndPath(my_phone_number, 4);
				LwtLog.d(IdtApplication.WULIN_TAG,
						"PressToSpeakListen=======================after====IDSApiProxyMgr.getCurProxy().iMGetFileName");
				try {
					v.setPressed(true);
					wakeLock.acquire();
					recordingContainer.setVisibility(View.VISIBLE);
					recordingHint.setText(getString(R.string.move_up_to_cancel));
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
					// 发送语音
					talk.startRecord(PathConstant.LOCAL_FILE_STRING);
				} catch (Exception e) {
					e.printStackTrace();
					v.setPressed(false);
					if (wakeLock.isHeld())
						wakeLock.release();
					recordingContainer.setVisibility(View.INVISIBLE);
					Toast.makeText(IdtChatActivity.this, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
					return false;
				}

				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					recordingHint.setText(getString(R.string.release_to_cancel));
					recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
				} else {
					recordingHint.setText(getString(R.string.move_up_to_cancel));
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				// 将从服务器获取到的名字赋值给生成的文件
				talk.stopRecord();
				recordingContainer.setVisibility(View.INVISIBLE);
				if (wakeLock.isHeld())
					wakeLock.release();
				return true;
			default:
				recordingContainer.setVisibility(View.INVISIBLE);
				return false;
			}
		}
	}

	/**
	 * 获取表情的gridview的子view
	 * 
	 * @param i
	 * @return
	 */
	private View getGridChildView(int i) {
		return null;
	}

	public List<String> getExpressionRes(int getSum) {
		List<String> reslist = new ArrayList<String>();
		for (int x = 1; x <= getSum; x++) {
			String filename = "ee_" + x;

			reslist.add(filename);

		}
		return reslist;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LwtLog.d(IdtApplication.WULIN_TAG, "onResume()");
		loadRichFileData();
		String lock_group_num = SharedPreferencesUtil.getStringPreference(IdtChatActivity.this, "lock_group_num", "#");
        if(!CurrentGroupCall.CURRENT_GROUP_CALL_NUM.equals("")){
			
		} else if (!lock_group_num.equals("#")) {
			CurrentGroupCall.CURRENT_GROUP_CALL_NUM = lock_group_num;
		} else {
			IdtApplication idtApplication = (IdtApplication) IdtChatActivity.this.getApplication();
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
		super.onPause();
	}

	/**
	 * 隐藏软键盘
	 */
	private void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 加入到黑名单
	 * 
	 * @param username
	 */
	private void addUserToBlacklist(String username) {
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}

	/**
	 * 覆盖手机返回键
	 */
	@Override
	public void onBackPressed() {
		if (more.getVisibility() == View.VISIBLE) {
			more.setVisibility(View.GONE);
			// iv_emoticons_normal.setVisibility(View.VISIBLE);
			// iv_emoticons_checked.setVisibility(View.INVISIBLE);
		} else {
			finish();
			super.onBackPressed();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// 点击notification bar进入聊天页面，保证只有一个聊天页面
		String username = intent.getStringExtra("userId");
		if (toChatUsername.equals(username))
			super.onNewIntent(intent);
		else {
			finish();
			startActivity(intent);
		}

	}

	/**
	 * 转发消息
	 * 
	 * @param forward_msg_id
	 */
	protected void forwardMessage(String forward_msg_id) {

	}

	public String getToChatUsername() {
		return toChatUsername;
	}

	@Override
	public void receiveRichMessage(String SMS_RESOURCE_URL, int dwType, String pcFileName) {
		// TODO Auto-generated method stub
		super.receiveRichMessage(SMS_RESOURCE_URL, dwType, pcFileName);
		// if (dwType == 1 || dwType == 2) {
		// 为纯文字或者地址的时候，不用进行下载，可以直接进行显示
		// 无论发送什么文件都进行加载，将下载交给service来完成
		loadRichFileData();
		// } else {
		// // 进行下载
		// // 为录音文件等
		// ftpBuinessLayer = new FtpBuinessLayer(IdtChatActivity.this);
		// ftpBuinessLayer.setListener(IdtChatActivity.this);
		// ftpBuinessLayer.downLoadFile("/IM/" + callto_persion_num + "/" +
		// pcFileName, "/IM/" + callto_persion_num,
		// SMS_RESOURCE_URL.replaceAll(pcFileName, ""), pcFileName);
		// // 当下载成功，再显示
		// }
	}

	@Override
	public void receiveIMDownloadSuccess(Uri uri) {
		// TODO Auto-generated method stub
		super.receiveIMDownloadSuccess(uri);
		Message message = new Message();
		message.what = FILE_Download_SUCCESS;
		message.obj = uri;
		handler.sendMessage(message);
	}

	@Override
	public void getFileNameFromServer(String file_name) {
		// TODO Auto-generated method stub
		// 可以给音频命名了
		// 屏蔽了该接口，不从服务器获取文件名了
	}

	// 到成功创建语音文件以后
	@Override
	public void onCreatedVoiceFile(int time_length) {
		// TODO Auto-generated method stub
		LwtLog.d("wulin", "onCreatedVoiceFile");
		TIME_LENGTH = time_length;
		sendVoice(PathConstant.LOCAL_FILE_STRING, PathConstant.REMOTE_FTP_FILE); 
	}

	// 文件上传成功
	@Override
	public void fileUploadSuccess(int style, Uri uri, String callto_persion_num, String filename) {
		// TODO Auto-generated method stub
		LwtLog.d("wulin", "fileUploadSuccess");
		// CURRENT_UPLOAD_FILE_STYLE = style;
		Message message = new Message();
		message.what = FILE_UPLOAD_SUCCESS;
		message.obj = new DeliverData(style, uri, callto_persion_num, filename);
		handler.sendMessage(message);
	}

	@Override
	public void fileDownloadSuccess() {
		// TODO Auto-generated method stub
		LwtLog.d("wulin", "fileDownloadSuccess");
		handler.sendEmptyMessage(FILE_Download_SUCCESS);
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
		
	}

	@Override
	public void exitNewEdition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reCall() {
		// TODO Auto-generated method stub
		new CustomDialog.Builder(this).setTitle("温馨提示").setMessage("您确定要单呼"+callto_persion_num+"吗？")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					singerAudioStart();
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
					singerAudioStart();
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

	@Override
	public void apkDownloadCase(String case_status, String install_apk_path, long process) {
		// TODO Auto-generated method stub
		
	}
}
