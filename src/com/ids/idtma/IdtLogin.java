package com.ids.idtma;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ids.idtma.database.IDTDatabaseBusinesslayer;
import com.ids.idtma.database.LoginIP;
import com.ids.idtma.database.LoginUser;
import com.ids.idtma.frame.IdtLoginPopupWindow;
import com.ids.idtma.ftp.FtpBuinessLayer;
import com.ids.idtma.util.CommonUtils;
import com.ids.idtma.util.CurrentDownload;
import com.ids.idtma.util.CustomDialog;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.WaitDialog;
import com.ids.proxy.IDSApiProxyMgr;

import android.R.attr;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

public class IdtLogin extends ActivityBase implements OnClickListener, IdtLoginPopupWindow.Listener, WaitDialog.Listener,FtpBuinessLayer.Listener {
	private EditText login_edit_account_editview, login_edit_server_ip_editview, login_edit_pwd_editview;
	private ImageButton login_edit_account_show_popupwindow_imagebutton, login_edit_server_ip_select_button,login_edit_passwd_cancel;
	private Button idt_login_summit_button;
	private CheckBox login_cb_savepwd_checkbox, auto_login_checkbox;
	private Button setting_button;
	private String SERVER_FILE_PATH="/IM/APK/ANDROID";
	private String LOCAL_PATH=Environment.getExternalStorageDirectory()+"/IDT-MA/IM/";
	private String[] store_user_name = { "2003", "2050", "2051", "2052", "2053" };

	// private String[] store_IP = { "192.168.2.11","124.160.11.21",
	// "124.160.11.22", "124.160.11.23" };
	// private String[] store_IP = new String[50];
	public static final int INIT_POPUPWINDOW_FROM_USERNAME_EDITVIEW = 0;
	public static final int INIT_POPUPWINDOW_FROM_IP_EDITVIEW = 1;
	public static final int DELETE_A_IP_RECORD = 2;
	public static final int DELETE_A_USER_RECORD = 3;
	private int INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = 4;
	private int APK_INSTALL=5;
	public static int NOT_EXIST_NEW_EDITION = 6;
	public static int EXIST_NEW_EDITION = 7;
	public static int CURRENT_DOWNLOAD_PROCESS = 8;
	
	private String APK_INSTALL_STRING_PATH="";
	private WaitDialog waitDialog = null;
	private List<LoginIP> loginIPs = null;
	private List<LoginUser> listLoginUser = null;
	private FtpBuinessLayer ftpBuinessLayer;
	private int to_delete_ip_index=-1;
	private int to_delete_user_index=-1;
	private long local_process = 0;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == APK_INSTALL) {
				Toast.makeText(IdtLogin.this, "下载成功", Toast.LENGTH_SHORT).show();
				File apkfile = new File(APK_INSTALL_STRING_PATH);
		        if (!apkfile.exists())  
		        {
		            return;
		        }
		        Intent i = new Intent(Intent.ACTION_VIEW);
		        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		        IdtLogin.this.startActivity(i);
			}
			else if (msg.what == NOT_EXIST_NEW_EDITION) {
				Toast.makeText(IdtLogin.this, "没有更新的版本", Toast.LENGTH_SHORT).show();
			} else if(msg.what == EXIST_NEW_EDITION){
				Toast.makeText(IdtLogin.this, "存在更新的版本，正在下载,请在通知栏查看下载进度", Toast.LENGTH_SHORT).show();
				CurrentDownload.APP_DOWNLOAD = true;
				createNotifacation();
			} else if(msg.what == DELETE_A_IP_RECORD){
				deleteARecordFromIpTable(to_delete_ip_index);
			} else if(msg.what == DELETE_A_USER_RECORD){
				deleteARecordFromUserTable(to_delete_user_index);
			} else if(msg.what == CURRENT_DOWNLOAD_PROCESS){
				downloadProcess();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		LwtLog.d("wulin", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTactivity进程的id为:"+android.os.Process.myPid());
		super.onCreate(savedInstanceState);
		LwtLog.d(IdtApplication.WULIN_TAG, "当前在IdLwtLogin界面，当前所处线程为:" + Thread.currentThread().getName());
		setContentView(R.layout.new_idt_login_activity);
		IdtApplication.getInstance().addActivity(this);
		initView();
		selfUpdata();
		if (SharedPreferencesUtil.getBooleanPreference(this.getApplicationContext(), "auto_login_checkbox_open",
				false)) {
			toLwtLogin();
		}
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
		notificationManager.notify(0, updateNotification);
	}
	
	// 通知栏进度动态显示
	private void downloadProcess() {
		updateNotification.contentView.setProgressBar(R.id.content_view_progress, 100, (int) local_process, false);
		updateNotification.contentView.setTextViewText(R.id.content_view_text1, (int) local_process + "%");
		notificationManager.notify(0, updateNotification);
	}

	public void startJNIOperationService() {
		// 存储当前网络的状态，设为：关
		SharedPreferencesUtil.setBooleanPreferences(getApplicationContext(), AppConstants.MY_PREFERENCE_NETWORK_STATUS,
				false);
		LwtLog.d(IdtApplication.WULIN_TAG, "加载动态库 svcapi");
		// 获取一个代理类，并调用其中的方法loadLibrary,加载svcapi这个so库
		IDSApiProxyMgr.getCurProxy().loadLibrary(this);
		LwtLog.d(IdtApplication.WULIN_TAG, "动态库 svcapi 加载成功!");
		// 初始化 将基本信息和mbinder发送给so库,mbinder里面的方法工so库不定时的调用
		IDSApiProxyMgr.getCurProxy().init(this);
	}
	
	private void selfUpdata(){
		Boolean update_switch_open = SharedPreferencesUtil.getBooleanPreference(IdtLogin.this, "update_switch_open", true);
		if(update_switch_open==true){
			CommonUtils netStatus = new CommonUtils();
			if (netStatus.isConnectingToInternet(IdtLogin.this)) {
				// 启动与JNI相关的监护和被监护程序
				if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
					// 存在sd卡
					Toast.makeText(IdtLogin.this, "您的手机没有SD卡", Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(IdtLogin.this, "正在检查当前软件版本", Toast.LENGTH_SHORT).show();
				ftpBuinessLayer.searchFromFtpAndDownload(SERVER_FILE_PATH, LOCAL_PATH);
			} else {
				Toast.makeText(IdtLogin.this, "没有可以使用的网络，请打开您的网络", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void toLwtLogin() {
		// 对输入内容的格式进行限定
		if (!checkInputStatus()) {
			return;
		}
		CommonUtils netStatus = new CommonUtils();
		if (netStatus.isConnectingToInternet(IdtLogin.this)) {
			// 启动与JNI相关的监护和被监护程序
			waitDialog = new WaitDialog(this);
			waitDialog.show("正在登录，请稍后");
			waitDialog.setListener(this);
			startJNIOperationService();
		} else {
			Toast.makeText(IdtLogin.this, "没有可以使用的网络，请打开您的网络", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean checkInputStatus() {
		String login_name = login_edit_account_editview.getText().toString().trim();
		String login_password = login_edit_pwd_editview.getText().toString().trim();
		String login_ip = login_edit_server_ip_editview.getText().toString().trim();
		if (login_name.equals("")) {
			Toast.makeText(IdtLogin.this, "账号名不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (login_password.equals("")) {
			Toast.makeText(IdtLogin.this, "密码不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (login_ip.equals("")) {
			Toast.makeText(IdtLogin.this, "服务器名IP地址不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}

		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_number", login_name);
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_password", login_password);
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "server_ip", login_ip);
		return true;
	}

	private void initView() {
		login_edit_account_editview = (EditText) findViewById(R.id.login_edit_account);
		login_edit_server_ip_editview = (EditText) findViewById(R.id.login_edit_server_ip);
		login_edit_server_ip_select_button = (ImageButton) findViewById(R.id.login_edit_server_ip_select_button);
		login_edit_passwd_cancel = (ImageButton) findViewById(R.id.login_edit_passwd_cancel);
		idt_login_summit_button = (Button) findViewById(R.id.idt_login_summit_button);
		setting_button = (Button) findViewById(R.id.setting_button);
		setting_button.setOnClickListener(this);
		login_edit_account_show_popupwindow_imagebutton = (ImageButton) findViewById(
				R.id.login_edit_account_show_popupwindow);
		login_edit_pwd_editview = (EditText) findViewById(R.id.login_edit_pwd);
		login_cb_savepwd_checkbox = (CheckBox) findViewById(R.id.login_cb_savepwd);
		auto_login_checkbox = (CheckBox) findViewById(R.id.auto_login);
		auto_login_checkbox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!SharedPreferencesUtil.getBooleanPreference(IdtLogin.this, "auto_login_checkbox_open",
						false)){
					//原来为没选中的时候
					login_cb_savepwd_checkbox.setChecked(true);
				}
			}
		});
		if (SharedPreferencesUtil.getBooleanPreference(this.getApplicationContext(), "remember_passwd_checkbox_open",
				false)) {
			login_cb_savepwd_checkbox.setChecked(true);
		} else {
			login_cb_savepwd_checkbox.setChecked(false);
		}

		if (SharedPreferencesUtil.getBooleanPreference(this.getApplicationContext(), "auto_login_checkbox_open",
				false)) {
			auto_login_checkbox.setChecked(true);
		} else {
			auto_login_checkbox.setChecked(false);
		}
		login_edit_account_show_popupwindow_imagebutton.setOnClickListener(this);
		login_edit_server_ip_select_button.setOnClickListener(this);
		idt_login_summit_button.setOnClickListener(this);
		initData();
		login_edit_passwd_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String before_string = login_edit_pwd_editview.getText().toString().trim();
				String after_string = "";
				if(before_string.length()>0){
					//after_string = before_string.substring(0, before_string.length()-1);
					//login_edit_pwd_editview.setText(after_string);
					login_edit_pwd_editview.setText("");
				}
			}
		});
	}

	private void initData() {
		String phone_number = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", "");
		String phone_password = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_password",
				"");
		String server_ip = SharedPreferencesUtil.getStringPreference(getApplicationContext(), "server_ip", "");
		if ((login_cb_savepwd_checkbox.isChecked()) || (SharedPreferencesUtil
				.getBooleanPreference(this.getApplicationContext(), "auto_login_checkbox_open", false))) {
			login_edit_account_editview.setText(phone_number);
			login_edit_pwd_editview.setText(phone_password);
			login_edit_server_ip_editview.setText(server_ip);
		}else{
			login_edit_account_editview.setText(phone_number);
			login_edit_server_ip_editview.setText(server_ip);
		}
		ftpBuinessLayer=new FtpBuinessLayer(IdtLogin.this);
		ftpBuinessLayer.setListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.login_edit_account_show_popupwindow:
			INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = INIT_POPUPWINDOW_FROM_USERNAME_EDITVIEW;
			IdtLoginPopupWindow idtLwtLoginPopupWindow_name = new IdtLoginPopupWindow();
			getUserDataFromDB();
			if(listLoginUser!=null && listLoginUser.size()>0){
				idtLwtLoginPopupWindow_name.initPopuWindow(login_edit_account_editview, IdtLogin.this, loginIPs,
						listLoginUser, INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW);
				idtLwtLoginPopupWindow_name.setListener(IdtLogin.this);
			}
			break;

		case R.id.login_edit_server_ip_select_button:
			INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = INIT_POPUPWINDOW_FROM_IP_EDITVIEW;
			IdtLoginPopupWindow idtLwtLoginPopupWindow_ip = new IdtLoginPopupWindow();
			getIpDataFromDB();
			if(loginIPs!=null && loginIPs.size()>0){
				idtLwtLoginPopupWindow_ip.initPopuWindow(login_edit_server_ip_editview, IdtLogin.this, loginIPs,
						listLoginUser, INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW);
				idtLwtLoginPopupWindow_ip.setListener(IdtLogin.this);
			}
			break;

		case R.id.idt_login_summit_button:
			toLwtLogin();
			break;

		case R.id.setting_button:
			Intent intent = new Intent(IdtLogin.this, LoginSettingActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			outTheApp();
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
		// 发一个通知给父类，让父类去终结所有的Activity
		Intent intent = new Intent();
		intent.setAction(ActivityBase.ACTION_QUIT_APPLICATION);
		sendBroadcast(intent);
	}

	private void getIpDataFromDB() {
		loginIPs = new ArrayList<LoginIP>();
		IDTDatabaseBusinesslayer idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(IdtLogin.this);
		loginIPs = idtDatabaseBusinesslayer.getAllDataFromIPTable();
	}
	
	private void deleteARecordFromIpTable(int index){
		int ip_id = loginIPs.get(index)._id;
		IDTDatabaseBusinesslayer idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(IdtLogin.this);
		int flag = idtDatabaseBusinesslayer.deleteAIPRecord(ip_id);
//		if(flag==IDTDatabaseBusinesslayer.DELETE_DATA_FAIL){
//			//删除数据失败
//			
//		}else if(flag==IDTDatabaseBusinesslayer.DELETE_DATA_SUCCESS){
//			//删除数据成功
//			loginIPs = null;
//			INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = INIT_POPUPWINDOW_FROM_IP_EDITVIEW;
//			IdtLoginPopupWindow idtLwtLoginPopupWindow_ip = new IdtLoginPopupWindow();
//			getIpDataFromDB();
//			if(loginIPs!=null && loginIPs.size()>0){
//				idtLwtLoginPopupWindow_ip.initPopuWindow(login_edit_server_ip_editview, IdtLogin.this, loginIPs,
//						listLoginUser, INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW);
//				idtLwtLoginPopupWindow_ip.setListener(IdtLogin.this);
//			}
//		}
		
		if(flag==IDTDatabaseBusinesslayer.DELETE_DATA_SUCCESS){
			//删除数据成功
			Toast.makeText(IdtLogin.this, "您删除了一个IP配置信息", Toast.LENGTH_SHORT).show();
		}
		loginIPs = null;
		INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = INIT_POPUPWINDOW_FROM_IP_EDITVIEW;
		IdtLoginPopupWindow idtLwtLoginPopupWindow_ip = new IdtLoginPopupWindow();
		getIpDataFromDB();
		if(loginIPs!=null && loginIPs.size()>0){
			idtLwtLoginPopupWindow_ip.initPopuWindow(login_edit_server_ip_editview, IdtLogin.this, loginIPs,
					listLoginUser, INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW);
			idtLwtLoginPopupWindow_ip.setListener(IdtLogin.this);
		}
	}

	private void getUserDataFromDB() {
		listLoginUser = new ArrayList<LoginUser>();
		IDTDatabaseBusinesslayer idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(IdtLogin.this);
		listLoginUser = idtDatabaseBusinesslayer.getAllDataFromUserTable();
	}
	
	private void deleteARecordFromUserTable(int index){
		int user_id = listLoginUser.get(index)._id;
		IDTDatabaseBusinesslayer idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(IdtLogin.this);
		int flag = idtDatabaseBusinesslayer.deleteAUserRecord(user_id);
//		if(flag==IDTDatabaseBusinesslayer.DELETE_DATA_FAIL){
//			//删除数据失败
//			
//		}else if(flag==IDTDatabaseBusinesslayer.DELETE_DATA_SUCCESS){
//			//删除数据成功
//			loginIPs = null;
//			INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = INIT_POPUPWINDOW_FROM_IP_EDITVIEW;
//			IdtLoginPopupWindow idtLwtLoginPopupWindow_ip = new IdtLoginPopupWindow();
//			getIpDataFromDB();
//			if(loginIPs!=null && loginIPs.size()>0){
//				idtLwtLoginPopupWindow_ip.initPopuWindow(login_edit_server_ip_editview, IdtLogin.this, loginIPs,
//						listLoginUser, INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW);
//				idtLwtLoginPopupWindow_ip.setListener(IdtLogin.this);
//			}
//		}
		
		if(flag==IDTDatabaseBusinesslayer.DELETE_DATA_SUCCESS){
			//删除数据成功
			Toast.makeText(IdtLogin.this, "您删除了一个用户信息", Toast.LENGTH_SHORT).show();
		}
		listLoginUser = null;
		INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW = INIT_POPUPWINDOW_FROM_USERNAME_EDITVIEW;
		IdtLoginPopupWindow idtLwtLoginPopupWindow_name = new IdtLoginPopupWindow();
		getUserDataFromDB();
		if(listLoginUser!=null && listLoginUser.size()>0){
			idtLwtLoginPopupWindow_name.initPopuWindow(login_edit_account_editview, IdtLogin.this, loginIPs,
					listLoginUser, INIT_POPUPWINDOW_FROM_CURRENT_EDITVIEW);
			idtLwtLoginPopupWindow_name.setListener(IdtLogin.this);
		}
	}

	@Override
	public void popupWindowDismiss(String selectItem, int curent_mode) {
		// TODO Auto-generated method stub
		if (curent_mode == INIT_POPUPWINDOW_FROM_USERNAME_EDITVIEW) {
			login_edit_account_editview.setText(selectItem);
		} else if (curent_mode == INIT_POPUPWINDOW_FROM_IP_EDITVIEW) {
			login_edit_server_ip_editview.setText(selectItem);
		}
	}

	@Override
	public void loginSuccess() {
		// TODO Auto-generated method stub
		super.loginSuccess();
		if (login_cb_savepwd_checkbox.isChecked()) {
			SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "remember_passwd_checkbox_open",
					true);
		} else {
			SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "remember_passwd_checkbox_open",
					false);
		}

		if (auto_login_checkbox.isChecked()) {
			SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "auto_login_checkbox_open", true);
			SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "remember_passwd_checkbox_open",
					true);
		} else {
			SharedPreferencesUtil.setBooleanPreferences(this.getApplicationContext(), "auto_login_checkbox_open",
					false);
		}
		String login_name = login_edit_account_editview.getText().toString().trim();
		String login_password = login_edit_pwd_editview.getText().toString().trim();
		String login_ip = login_edit_server_ip_editview.getText().toString().trim();
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_number", login_name);
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_password", login_password);
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "server_ip", login_ip);
		insertIntoLoginUserDB(login_name, login_password);
		waitDialog.cancel();
		Intent intent = new Intent(IdtLogin.this, IdtMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void insertIntoLoginUserDB(String userphone, String password) {
		IDTDatabaseBusinesslayer idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(IdtLogin.this);
		int flag = idtDatabaseBusinesslayer.insertIntoUserTable(new LoginUser(userphone, password));
		LwtLog.d("login", "插入login用户数据库返回的信号:" + flag);
	}

	@Override
	public void dialogBackKey() {
		// 退出
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_number", "");
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "phone_password", "");
		SharedPreferencesUtil.setStringPreferences(getApplicationContext(), "server_ip", "");
		outTheApp();
	}
	
	@Override
	public void fileUploadSuccess(int style,Uri uri,String callto_persion_num,String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileDownloadSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void apkDownloadCase(String case_status,String install_apk_path,long process) {
		// TODO Auto-generated method stub
		if(case_status.equals(FtpBuinessLayer.FTP_DOWN_SUCCESS)){
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

	@Override
	public void deleteARecord(int index) {
		// TODO Auto-generated method stub
		to_delete_ip_index=index;
		handler.sendEmptyMessage(DELETE_A_IP_RECORD);
	}

	@Override
	public void deleteAUserRecord(int index) {
		// TODO Auto-generated method stub
		to_delete_user_index=index;
		handler.sendEmptyMessage(DELETE_A_USER_RECORD);
	}
}
