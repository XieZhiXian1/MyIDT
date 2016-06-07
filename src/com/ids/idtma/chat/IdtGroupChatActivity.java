//
//package com.ids.idtma.chat;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.List;
//import org.w3c.dom.Text;
//import com.ids.idtma.AppConstants;
//import com.ids.idtma.IdtApplication;
//import com.ids.idtma.IdtSetting;
//import com.ids.idtma.R;
//import com.ids.idtma.adapter.ExpressionPagerAdapter;
//import com.ids.idtma.entity.CallEntity;
//import com.ids.idtma.entity.CallEntity.CallType;
//import com.ids.idtma.jni.aidl.GroupMember;
//import com.ids.idtma.jni.aidl.MediaAttribute;
//import com.ids.idtma.map.IdtMap;
//import com.ids.idtma.util.LwtLog;
//import com.ids.idtma.util.PasteEditText;
//import com.ids.proxy.IDSApiProxyMgr;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.media.ThumbnailUtils;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.PowerManager;
//import android.provider.MediaStore;
//import android.support.v4.view.ViewPager;
//import android.text.ClipboardManager;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.Window;
//import android.view.View.OnClickListener;
//import android.view.View.OnFocusChangeListener;
//import android.view.View.OnTouchListener;
//import android.view.WindowManager;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
///**
// * 聊天页面
// * 
// */
//@SuppressWarnings("deprecation")
//public class IdtGroupChatActivity extends Activity implements OnClickListener {
//	private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
//	public static final int REQUEST_CODE_CONTEXT_MENU = 3;
//	private static final int REQUEST_CODE_MAP = 4;
//	public static final int REQUEST_CODE_TEXT = 5;
//	public static final int REQUEST_CODE_VOICE = 6;
//	public static final int REQUEST_CODE_PICTURE = 7;
//	public static final int REQUEST_CODE_LOCATION = 8;
//	public static final int REQUEST_CODE_NET_DISK = 9;
//	public static final int REQUEST_CODE_FILE = 10;
//	public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
//	public static final int REQUEST_CODE_PICK_VIDEO = 12;
//	public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
//	public static final int REQUEST_CODE_VIDEO = 14;
//	public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
//	public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
//	public static final int REQUEST_CODE_SEND_USER_CARD = 17;
//	public static final int REQUEST_CODE_CAMERA = 18;
//	public static final int REQUEST_CODE_LOCAL = 19;
//	public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
//	public static final int REQUEST_CODE_GROUP_DETAIL = 21;
//	public static final int REQUEST_CODE_SELECT_VIDEO = 23;
//	public static final int REQUEST_CODE_SELECT_FILE = 24;
//	public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;
//
//	public static final int RESULT_CODE_COPY = 1;
//	public static final int RESULT_CODE_DELETE = 2;
//	public static final int RESULT_CODE_FORWARD = 3;
//	public static final int RESULT_CODE_OPEN = 4;
//	public static final int RESULT_CODE_DWONLOAD = 5;
//	public static final int RESULT_CODE_TO_CLOUD = 6;
//	public static final int RESULT_CODE_EXIT_GROUP = 7;
//
//	public static final int CHATTYPE_SINGLE = 1;
//	public static final int CHATTYPE_GROUP = 2;
//
//	public static final String COPY_IMAGE = "EASEMOBIMG";
//	private View recordingContainer;
//	private ImageView micImage;
//	private TextView recordingHint;
//	private ListView listView;
//	private View buttonSetModeKeyboard;
//	private View buttonSetModeVoice;
//	private View buttonSend;
//	private View buttonPressToSpeak;
//	// private ViewPager expressionViewpager;
//	private LinearLayout emojiIconContainer;
//	private LinearLayout btnContainer;
//	private PasteEditText mEditTextContent;
//	private View more;
//	private ClipboardManager clipboard;
//	private ViewPager expressionViewpager;
//	private InputMethodManager manager;
//	private List<String> reslist;
//	private Drawable[] micImages;
//	private int chatType;
//	private NewMessageBroadcastReceiver receiver;
//	public static IdtGroupChatActivity activityInstance = null;
//	// 给谁发送消息
//	private String toChatUsername;
//	private File cameraFile;
//	public static int resendPos;
////	private ImageView iv_emoticons_normal;
////	private ImageView iv_emoticons_checked;
//	private RelativeLayout edittext_layout;
//	private ProgressBar loadmorePB;
//	private boolean isloading;
//	private final int pagesize = 20;
//	private boolean haveMoreData = true;
//	private Button btnMore;
//	public String playMsgId;
//
//	String myUserNick = "";
//	String myUserAvatar = "";
//	String toUserNick = "";
//	String toUserAvatar = "";
//	// 分享的照片
//	String iamge_path = null;
//	// 设置按钮
//	private ImageView iv_setting;
//	private ImageView iv_setting_group;
//	//拨打过去用户的信息
//	private GroupMember callto_persion_info_member,callto_group_info;
//	private TextView title_textview;
//	@SuppressLint("HandlerLeak")
//	private Handler micImageHandler = new Handler() {
//		@Override
//		public void handleMessage(android.os.Message msg) {
//			// 切换msg切换图片
//			micImage.setImageDrawable(micImages[msg.what]);
//		}
//	};
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.idt_activity_chat_group);
//		IdtApplication.getInstance().addActivity(this);
//		initView();
//		setUpView();
//		initData();
//	}
//	
//	
//	private void initData() {
//		callto_group_info = getIntent().getExtras().getParcelable("callto_group_info");
//		title_textview.setText("与" + callto_group_info.getUcNum() + "的群消息富文本");
//	}
//
//	/**
//	 * initView
//	 */
//	protected void initView() {
//		title_textview=(TextView) findViewById(R.id.title);
//		recordingContainer = findViewById(R.id.recording_container);
//		micImage = (ImageView) findViewById(R.id.mic_image);
//		recordingHint = (TextView) findViewById(R.id.recording_hint);
//		listView = (ListView) findViewById(R.id.list);
//		mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
//		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
//		edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
//		buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
//		buttonSend = findViewById(R.id.btn_send);
//		buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
//		expressionViewpager = (ViewPager) findViewById(R.id.vPager);
//		emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
//		btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
////		iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
////		iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
//		loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
//		btnMore = (Button) findViewById(R.id.btn_more);
////		iv_emoticons_normal.setVisibility(View.VISIBLE);
////		iv_emoticons_checked.setVisibility(View.INVISIBLE);
//		more = findViewById(R.id.more);
//		edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
//
//		// 动画资源文件,用于录制语音时
//		micImages = new Drawable[] { getResources().getDrawable(R.drawable.record_animate_01),
//				getResources().getDrawable(R.drawable.record_animate_02),
//				getResources().getDrawable(R.drawable.record_animate_03),
//				getResources().getDrawable(R.drawable.record_animate_04),
//				getResources().getDrawable(R.drawable.record_animate_05),
//				getResources().getDrawable(R.drawable.record_animate_06),
//				getResources().getDrawable(R.drawable.record_animate_07),
//				getResources().getDrawable(R.drawable.record_animate_08),
//				getResources().getDrawable(R.drawable.record_animate_09),
//				getResources().getDrawable(R.drawable.record_animate_10),
//				getResources().getDrawable(R.drawable.record_animate_11),
//				getResources().getDrawable(R.drawable.record_animate_12),
//				getResources().getDrawable(R.drawable.record_animate_13),
//				getResources().getDrawable(R.drawable.record_animate_14), };
//
//		// 表情list
//		reslist = getExpressionRes(35);
//		// 初始化表情viewpager
//		List<View> views = new ArrayList<View>();
//		View gv1 = getGridChildView(1);
//		View gv2 = getGridChildView(2);
//		views.add(gv1);
//		views.add(gv2);
//		expressionViewpager.setAdapter(new ExpressionPagerAdapter(views));
//		edittext_layout.requestFocus();
//		buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
//		mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (hasFocus) {
//					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
//				} else {
//					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
//				}
//
//			}
//		});
//		mEditTextContent.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
//				more.setVisibility(View.GONE);
////				iv_emoticons_normal.setVisibility(View.VISIBLE);
////				iv_emoticons_checked.setVisibility(View.INVISIBLE);
//				emojiIconContainer.setVisibility(View.GONE);
//				btnContainer.setVisibility(View.GONE);
//			}
//		});
//		// 监听文字框
//		mEditTextContent.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				if (!TextUtils.isEmpty(s)) {
//					btnMore.setVisibility(View.GONE);
//					buttonSend.setVisibility(View.VISIBLE);
//				} else {
//					btnMore.setVisibility(View.VISIBLE);
//					buttonSend.setVisibility(View.GONE);
//				}
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//
//			}
//		});
//
//	}
//	
//	public void setUpView(){
//		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//	}
//
//	/**
//	 * 消息图标点击事件
//	 * 
//	 * @param view
//	 */
//	@Override
//	public void onClick(View view) {
//
//		int id = view.getId();
//		if (id == R.id.btn_send) {
//			// 点击发送按钮(发文字和表情)
//			String s = mEditTextContent.getText().toString();
//			sendText(s);
//		} else if (id == R.id.btn_take_picture) {
//			//点击拍摄
//			
//		} else if (id == R.id.btn_picture) {
//			// 点击图片图标
//			selectPicFromLocal(); 
//		}else if (id == R.id.btn_file) { 
//			// 点击文件图标
//			selectFileFromLocal();
//		} else if (id == R.id.btn_voice_call_group) { 
//			// 点击组呼的时候
//			groupAudioStart();
//		}else if(id==R.id.title_arrow_back){
//			//点击屏幕上方返回键
//			IdtApplication.getInstance().deleteActivity(this);
//		}else if(id == R.id.map){
//			// 点击地图图标
//			Intent intent = new Intent(IdtGroupChatActivity.this, IdtMap.class);
//			startActivity(intent);
//		}
//	}
//	
//	public void groupAudioStart(){
//		//是不是正在拨打电话
//		if(IdtApplication.resumeCurrentCall())
//		{
//			((NotificationManager) IdtGroupChatActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(AppConstants.CALL_NOTIFICATION_ID);
//			this.startActivity(IdtApplication.getCurrentCall().getIntent());
//			return;
//		}
//		MediaAttribute pAttr = new MediaAttribute();
//		pAttr.ucAudioRecv = 0;
//		pAttr.ucAudioSend = 1;
//		pAttr.ucVideoRecv = 0;
//		pAttr.ucVideoSend = 0;
//		//没有锁定的群组就直接拨打
//		String callNum = AppConstants.getLockedGroupNum().equals("") ? callto_group_info
//				.getUcNum() : AppConstants.getLockedGroupNum();
//				/**
//				 * 启动呼出 输入:
//				 * 
//				 * @param cPeerNum
//				 *            : 对方号码
//				 * @param SrvType
//				 *            : 业务类型
//				 * @param pAttr
//				 *            : 媒体属性
//				 * @param pUsrCtx
//				 *            : 用户上下文
//				 * @return 返回 -1: 失败 else: 呼叫标识 注意: 如果是组呼: 1.pcPeerNum为组号码
//				 *         2.pAttr中,ucAudioSend为1,其余为0
//				 */
//		int id = IDSApiProxyMgr.getCurProxy().CallMakeOut(callNum,
//				AppConstants.CALL_TYPE_GROUP_CALL, pAttr, 0);
//		//设置当前的拨打
//		IdtApplication.setCurrentCall(new CallEntity(id,CallType.GROUP_CALL));
//		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>开始组呼，组号码: " + callNum + ", id : " + id);
//		Intent intent = new Intent(this, ActivityGroupCall.class);
//		intent.putExtra(AppConstants.EXTRA_KEY_CALLID, id);
//		intent.putExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM, callNum);
//		this.startActivity(intent);
//	}
//	
//	/**
//	 * 照相获取图片
//	 */
//	public void selectPicFromCamera() {
//		
//	}
//
//	/**
//	 * 选择文件
//	 */
//	private void selectFileFromLocal() {
//		Intent intent = null;
//		if (Build.VERSION.SDK_INT < 19) {
//			intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.setType("*/*");
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//		} else {
//			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		}
//		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
//	}
//
//	/**
//	 * 从图库获取图片
//	 */
//	public void selectPicFromLocal() {
//		Intent intent;
//		if (Build.VERSION.SDK_INT < 19) {
//			intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.setType("image/*");
//
//		} else {
//			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		}
//		startActivityForResult(intent, REQUEST_CODE_LOCAL);
//	}
//
//	/**
//	 * 发送文本消息
//	 * 
//	 * @param content
//	 *            message content
//	 * @param isResend
//	 *            boolean resend
//	 */
//	private void sendText(String content) {
//
//		if (content.length() > 0) {
//
//		}
//	}
//
//	/**
//	 * 发送语音
//	 * 
//	 * @param filePath
//	 * @param fileName
//	 * @param length
//	 * @param isResend
//	 */
//	private void sendVoice(String filePath, String fileName, String length, boolean isResend) {
//	}
//
//	/**
//	 * 发送图片
//	 * 
//	 * @param filePath
//	 */
//	private void sendPicture(final String filePath, boolean is_share) {
//	}
//
//	/**
//	 * 发送视频消息
//	 */
//	private void sendVideo(final String filePath, final String thumbPath, final int length) {
//
//	}
//
//	/**
//	 * 根据图库图片uri发送图片
//	 * 
//	 * @param selectedImage
//	 */
//	private void sendPicByUri(Uri selectedImage) {
//		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
//		Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
//		if (cursor != null) {
//			cursor.moveToFirst();
//			int columnIndex = cursor.getColumnIndex("_data");
//			String picturePath = cursor.getString(columnIndex);
//			cursor.close();
//			cursor = null;
//
//			if (picturePath == null || picturePath.equals("null")) {
//				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.show();
//				return;
//			}
//			sendPicture(picturePath, false);
//		} else {
//			File file = new File(selectedImage.getPath());
//			if (!file.exists()) {
//				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.show();
//				return;
//
//			}
//			sendPicture(file.getAbsolutePath(), false);
//		}
//
//	}
//
//	/**
//	 * 发送位置信息
//	 * 
//	 * @param latitude
//	 * @param longitude
//	 * @param imagePath
//	 * @param locationAddress
//	 */
//	private void sendLocationMsg(double latitude, double longitude, String imagePath, String locationAddress) {
//
//	}
//
//	/**
//	 * 发送文件
//	 * 
//	 * @param uri
//	 */
//	private void sendFile(Uri uri) {
//	}
//
//	/**
//	 * 重发消息
//	 */
//	private void resendMessage() {
//	}
//
//	/**
//	 * 显示语音图标按钮
//	 * 
//	 * @param view
//	 */
//	public void setModeVoice(View view) {
//		hideKeyboard();
//		edittext_layout.setVisibility(View.GONE);
//		more.setVisibility(View.GONE);
//		view.setVisibility(View.GONE);
//		buttonSetModeKeyboard.setVisibility(View.VISIBLE);
//		buttonSend.setVisibility(View.GONE);
//		btnMore.setVisibility(View.VISIBLE);
//		buttonPressToSpeak.setVisibility(View.VISIBLE);
////		iv_emoticons_normal.setVisibility(View.VISIBLE);
////		iv_emoticons_checked.setVisibility(View.INVISIBLE);
//		btnContainer.setVisibility(View.VISIBLE);
//		emojiIconContainer.setVisibility(View.GONE);
//
//	}
//
//	/**
//	 * 显示键盘图标
//	 * 
//	 * @param view
//	 */
//	public void setModeKeyboard(View view) {
//		edittext_layout.setVisibility(View.VISIBLE);
//		more.setVisibility(View.GONE);
//		view.setVisibility(View.GONE);
//		buttonSetModeVoice.setVisibility(View.VISIBLE);
//		// mEditTextContent.setVisibility(View.VISIBLE);
//		mEditTextContent.requestFocus();
//		// buttonSend.setVisibility(View.VISIBLE);
//		buttonPressToSpeak.setVisibility(View.GONE);
//		if (TextUtils.isEmpty(mEditTextContent.getText())) {
//			btnMore.setVisibility(View.VISIBLE);
//			buttonSend.setVisibility(View.GONE);
//		} else {
//			btnMore.setVisibility(View.GONE);
//			buttonSend.setVisibility(View.VISIBLE);
//		}
//
//	}
//
//	/**
//	 * 显示或隐藏图标按钮页
//	 * 
//	 * @param view
//	 */
//	public void more(View view) {
//		if (more.getVisibility() == View.GONE) {
//			System.out.println("more gone");
//			hideKeyboard();
//			more.setVisibility(View.VISIBLE);
//			btnContainer.setVisibility(View.VISIBLE);
//			emojiIconContainer.setVisibility(View.GONE);
//		} else {
//			if (emojiIconContainer.getVisibility() == View.VISIBLE) {
//				emojiIconContainer.setVisibility(View.GONE);
//				btnContainer.setVisibility(View.VISIBLE);
////				iv_emoticons_normal.setVisibility(View.VISIBLE);
////				iv_emoticons_checked.setVisibility(View.INVISIBLE);
//			} else {
//				more.setVisibility(View.GONE);
//			}
//
//		}
//
//	}
//
//	/**
//	 * 点击文字输入框
//	 * 
//	 * @param v
//	 */
//	public void editClick(View v) {
//		listView.setSelection(listView.getCount() - 1);
//		if (more.getVisibility() == View.VISIBLE) {
//			more.setVisibility(View.GONE);
////			iv_emoticons_normal.setVisibility(View.VISIBLE);
////			iv_emoticons_checked.setVisibility(View.INVISIBLE);
//		}
//
//	}
//
//	/**
//	 * 消息广播接收者
//	 * 
//	 */
//	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			
//		}
//	}
//
//	/**
//	 * 消息回执BroadcastReceiver
//	 */
//	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//
//		}
//	};
//
//	/**
//	 * 消息送达BroadcastReceiver
//	 */
//	private BroadcastReceiver deliveryAckMessageReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//		}
//	};
//	private PowerManager.WakeLock wakeLock;
//
//	/**
//	 * 按住说话listener
//	 * 
//	 */
//	class PressToSpeakListen implements View.OnTouchListener {
//		@SuppressLint({ "ClickableViewAccessibility", "Wakelock" })
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			return true;
//		}
//	}
//
//	/**
//	 * 获取表情的gridview的子view
//	 * 
//	 * @param i
//	 * @return
//	 */
//	private View getGridChildView(int i) {
//		return null;
//	}
//
//	public List<String> getExpressionRes(int getSum) {
//		List<String> reslist = new ArrayList<String>();
//		for (int x = 1; x <= getSum; x++) {
//			String filename = "ee_" + x;
//
//			reslist.add(filename);
//
//		}
//		return reslist;
//
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//
//	/**
//	 * 隐藏软键盘
//	 */
//	private void hideKeyboard() {
//		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
//			if (getCurrentFocus() != null)
//				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//		}
//	}
//
//	/**
//	 * 加入到黑名单
//	 * 
//	 * @param username
//	 */
//	private void addUserToBlacklist(String username) {
//	}
//
//	/**
//	 * 返回
//	 * 
//	 * @param view
//	 */
//	public void back(View view) {
//		finish();
//	}
//
//	/**
//	 * 覆盖手机返回键
//	 */
//	@Override
//	public void onBackPressed() {
//		if (more.getVisibility() == View.VISIBLE) {
//			more.setVisibility(View.GONE);
////			iv_emoticons_normal.setVisibility(View.VISIBLE);
////			iv_emoticons_checked.setVisibility(View.INVISIBLE);
//		} else {
//			finish();
//			super.onBackPressed();
//		}
//	}
//
//	@Override
//	protected void onNewIntent(Intent intent) {
//		// 点击notification bar进入聊天页面，保证只有一个聊天页面
//		String username = intent.getStringExtra("userId");
//		if (toChatUsername.equals(username))
//			super.onNewIntent(intent);
//		else {
//			finish();
//			startActivity(intent);
//		}
//
//	}
//
//	/**
//	 * 转发消息
//	 * 
//	 * @param forward_msg_id
//	 */
//	protected void forwardMessage(String forward_msg_id) {
//		
//	}
//
//
//	public String getToChatUsername() {
//		return toChatUsername;
//	}
//
//}
