package com.ids.idtma;

import java.io.File;
import com.ids.idtma.database.IDTDatabaseBusinesslayer;
import com.ids.idtma.database.LoginIP;
import com.ids.idtma.util.CustomDialog;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LoginSettingActivity extends Activity implements OnClickListener {
	private EditText edittext1, edittext2, edittext3;
	private EditText ftp_editText1, ftp_editText2, ftp_editText3, ftp_editText4;
	private IDTDatabaseBusinesslayer idtDatabaseBusinesslayer;
	private ImageButton image_sure_button, image_return_button, ftp_sure_button;
	// private Button button;
	private String SERVER_FILE_PATH = "/IM/APK/ANDROID";
	private String LOCAL_PATH = Environment.getExternalStorageDirectory() + "/IDT-MA/IM/";
	// private FtpBuinessLayer ftpBuinessLayer;
	// private WaitDialog waitDialog;
	private String APK_INSTALL_STRING_PATH = "";
	private int APK_INSTALL = 0;
	public static int NOT_EXIST_NEW_EDITION = 5;
	private ImageButton self_update_imageButton;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == APK_INSTALL) {
				Toast.makeText(LoginSettingActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
				File apkfile = new File(APK_INSTALL_STRING_PATH);
				if (!apkfile.exists()) {
					return;
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
				LoginSettingActivity.this.startActivity(i);
			}
			// else if (msg.what == NOT_EXIST_NEW_EDITION) {
			// waitDialog.cancel();
			// Toast.makeText(LoginSettingActivity.this, "没有更新的版本",
			// Toast.LENGTH_SHORT).show();
			// }
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_login_setting_activity);
		IdtApplication.getInstance().addActivity(this);
		initView();
		initData();
	}

	@SuppressLint("CutPasteId")
	private void initView() {
		edittext1 = (EditText) findViewById(R.id.editText1);
		edittext2 = (EditText) findViewById(R.id.editText2);
		edittext3 = (EditText) findViewById(R.id.editText3);
		ftp_editText1 = (EditText) findViewById(R.id.ftp_editText1);
		ftp_editText2 = (EditText) findViewById(R.id.ftp_editText2);
		ftp_editText3 = (EditText) findViewById(R.id.ftp_editText3);
		ftp_editText4 = (EditText) findViewById(R.id.ftp_editText4);
		image_sure_button = (ImageButton) findViewById(R.id.sure_button);
		ftp_sure_button = (ImageButton) findViewById(R.id.ftp_sure_button);
		image_sure_button.setOnClickListener(this);
		ftp_sure_button.setOnClickListener(this);
		image_return_button = (ImageButton) findViewById(R.id.return_button);
		image_return_button.setOnClickListener(this);
		// button=(Button) findViewById(R.id.app_update_button);
		// button.setOnClickListener(this);
		self_update_imageButton = (ImageButton) findViewById(R.id.self_update);
		self_update_imageButton.setOnClickListener(this);
		Boolean update_switch_open = SharedPreferencesUtil.getBooleanPreference(LoginSettingActivity.this,
				"update_switch_open", true);
		if (update_switch_open == true) {
			self_update_imageButton.setBackgroundResource(R.drawable.new_ui_update_start);
		} else {
			self_update_imageButton.setBackgroundResource(R.drawable.new_ui_update_close);
		}
		// ftp最开始存在一个填充
		ftp_editText1
				.setText(SharedPreferencesUtil.getStringPreference(LoginSettingActivity.this, "ftp_server_host", ""));
		ftp_editText2
				.setText(SharedPreferencesUtil.getStringPreference(LoginSettingActivity.this, "ftp_server_port", ""));
		ftp_editText3.setText(SharedPreferencesUtil.getStringPreference(LoginSettingActivity.this, "ftp_account", ""));
		ftp_editText4.setText(SharedPreferencesUtil.getStringPreference(LoginSettingActivity.this, "ftp_password", ""));
	}

	private void initData() {
		idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(LoginSettingActivity.this);
		// ftpBuinessLayer=new FtpBuinessLayer(LoginSettingActivity.this);
		// ftpBuinessLayer.setListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sure_button:
			new CustomDialog.Builder(this).setTitle("温馨提示").setMessage("您确定增加该IP配置吗？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							switch (which) {
							case AlertDialog.BUTTON_POSITIVE:
								addIPSetting();
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
								addIPSetting();
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
			break;
		case R.id.return_button:
			LoginSettingActivity.this.finish();
			break;
		// case R.id.app_update_button:
		// CommonUtils netStatus = new CommonUtils();
		// if (netStatus.isConnectingToInternet(LoginSettingActivity.this)) {
		// // 启动与JNI相关的监护和被监护程序
		// if
		// (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		// {
		// // 存在sd卡
		// Toast.makeText(LoginSettingActivity.this, "您的手机没有SD卡",
		// Toast.LENGTH_SHORT).show();
		// return;
		// }
		// waitDialog = new WaitDialog(this);
		// waitDialog.show("正在查询并下载，请稍后");
		// ftpBuinessLayer.searchFromFtpAndDownload(SERVER_FILE_PATH,
		// LOCAL_PATH);
		// } else {
		// Toast.makeText(LoginSettingActivity.this, "没有可以使用的网络，请打开您的网络",
		// Toast.LENGTH_SHORT).show();
		// }
		// break;
		case R.id.self_update:
			Boolean update_switch_open = SharedPreferencesUtil.getBooleanPreference(LoginSettingActivity.this,
					"update_switch_open", true);
			if (update_switch_open == true) {
				// 已经处于自动更新状态，现在需要关闭自动更新
				SharedPreferencesUtil.setBooleanPreferences(LoginSettingActivity.this, "update_switch_open", false);
				self_update_imageButton.setBackgroundResource(R.drawable.new_ui_update_close);
			} else {
				// 处于非自动更新状态，现在需要开启
				SharedPreferencesUtil.setBooleanPreferences(LoginSettingActivity.this, "update_switch_open", true);
				self_update_imageButton.setBackgroundResource(R.drawable.new_ui_update_start);
			}
			break;
		case R.id.ftp_sure_button:
			new CustomDialog.Builder(this).setTitle("温馨提示").setMessage("您确定修该您的ftp配置吗？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							switch (which) {
							case AlertDialog.BUTTON_POSITIVE:
								storeFtpSetting();
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
								storeFtpSetting();
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
			break;
		default:
			break;
		}
	}

	private void storeFtpSetting() {
		String ftp_ip = ftp_editText1.getText().toString().trim();
		String ftp_port = ftp_editText2.getText().toString().trim();
		String ftp_account = ftp_editText3.getText().toString().trim();
		String ftp_password = ftp_editText4.getText().toString().trim();
		SharedPreferencesUtil.setStringPreferences(LoginSettingActivity.this, "ftp_server_host", ftp_ip);
		SharedPreferencesUtil.setStringPreferences(LoginSettingActivity.this, "ftp_server_port", ftp_port);
		SharedPreferencesUtil.setStringPreferences(LoginSettingActivity.this, "ftp_account", ftp_account);
		SharedPreferencesUtil.setStringPreferences(LoginSettingActivity.this, "ftp_password", ftp_password);
	}

	private void addIPSetting() {
		String name = edittext1.getText().toString().trim();
		String ip_name = edittext2.getText().toString().trim();
		String ip_port = edittext3.getText().toString().trim();
		if (name.equals("")) {
			Toast.makeText(LoginSettingActivity.this, "IP昵称不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (ip_name.equals("")) {
			Toast.makeText(LoginSettingActivity.this, "IP不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (ip_port.equals("")) {
			Toast.makeText(LoginSettingActivity.this, "IP端口不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		int flag = idtDatabaseBusinesslayer.insertIntoIPTable(new LoginIP(edittext1.getText().toString().trim(),
				edittext2.getText().toString().trim(), Integer.parseInt(edittext3.getText().toString().trim())));
		LwtLog.d("login", "返回的信号:" + flag);
		if (flag == IDTDatabaseBusinesslayer.INSERT_DATA_SUCCESS) {
			Toast.makeText(LoginSettingActivity.this, "您设置的IP信息保存成功", Toast.LENGTH_SHORT).show();
		} else if (flag == IDTDatabaseBusinesslayer.DATA_HAS_EXIST) {
			Toast.makeText(LoginSettingActivity.this, "该IP设置已经存在", Toast.LENGTH_SHORT).show();
		} else if (flag == IDTDatabaseBusinesslayer.INSERT_DATA_FAIL) {
			Toast.makeText(LoginSettingActivity.this, "您没有成功的是指IP信息", Toast.LENGTH_SHORT).show();
		}
	}

	// @Override
	// public void fileUploadSuccess(int style,Uri uri,String
	// callto_persion_num,String filename) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void fileDownloadSuccess() {
	// // TODO Auto-generated method stub
	//
	// }

	// @Override
	// public void apkDownloadCase(String case_status,String install_apk_path) {
	// // TODO Auto-generated method stub
	// if(case_status.equals(FtpBuinessLayer.FTP_DOWN_SUCCESS)){
	// waitDialog.cancel();
	// APK_INSTALL_STRING_PATH = install_apk_path;
	// handler.sendEmptyMessage(APK_INSTALL);
	// return;
	// }
	// waitDialog.cancel();
	// Toast.makeText(LoginSettingActivity.this, "下载失败",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// @Override
	// public void noExitNewEdition() {
	// // TODO Auto-generated method stub
	// handler.sendEmptyMessage(NOT_EXIST_NEW_EDITION);
	// }
}
