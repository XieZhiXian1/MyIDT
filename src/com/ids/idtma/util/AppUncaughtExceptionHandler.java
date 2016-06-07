package com.ids.idtma.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.proxy.IDSApiProxyMgr;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * AppUncaughtExceptionHandler处理类,当程序发生Uncaught异常的时候,该类来接管程序,并向服务器发送错误报告.
 * 
 * 
 */
public class AppUncaughtExceptionHandler implements UncaughtExceptionHandler {

	public static final String TAG = AppUncaughtExceptionHandler.class.getSimpleName();

	public FTPClient ftpClient = new FTPClient();

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

	private static AppUncaughtExceptionHandler INSTANCE = new AppUncaughtExceptionHandler();

	// 程序的Context对象
	private Context mContext;
	
	public static final int OUT_THE_APP=1;

	// 用来存储设备信息和异常信息
	private Map<String, String> uncaughtExceptionMap = new HashMap<String, String>();
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == OUT_THE_APP) {
				outTheApp();
			}
		}
	};
	private AppUncaughtExceptionHandler() {
	}

	/**
	 * 单例模式
	 */
	public static AppUncaughtExceptionHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		LwtLog.d("mAppUncaughtExceptionHandler", "--------------------------uncaughtException");
		IdtApplication.setCurrentCall(null);
		handleException(ex);
		return;
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		LwtLog.d("mAppUncaughtExceptionHandler", "--------------------------handleException");
		if (ex == null) {
			return false;
		}
		// 收集设备参数信息
		collectDeviceInfo(mContext);
		// 保存日志文件,发送错误信息至服务器
		saveUncaughtExceptionInfo2Server(saveUncaughtExceptionInfo2File(ex));
		outTheApp();
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			LwtLog.d("mAppUncaughtExceptionHandler", "--------------------------collectDeviceInfo");
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				uncaughtExceptionMap.put("versionName", versionName);
				uncaughtExceptionMap.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			LwtLog.e("AppUncaughtExceptionHandler", "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				uncaughtExceptionMap.put(field.getName(), field.get(null).toString());
				LwtLog.d("AppUncaughtExceptionHandler", field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				LwtLog.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveUncaughtExceptionInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : uncaughtExceptionMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		LwtLog.e(TAG, result);
		sb.append(result);
		try {
			String fileName = SharedPreferencesUtil.getStringPreference(mContext.getApplicationContext(),
					"phone_number", "unknown") + "_" + DateUtil.getDateDir() + ".txt";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = new File(AppConstants.MY_PREFERENCE_EXCEPTION_DIRECTORY);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(AppConstants.MY_PREFERENCE_EXCEPTION_DIRECTORY + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
			LwtLog.e(TAG, "an error occured while writing file...", e);
		}
		return null;
	}

	private void saveUncaughtExceptionInfo2Server(String fileName) {
		LwtLog.d("mAppUncaughtExceptionHandler", "--------------------------saveUncaughtExceptionInfo2Server");
		if (null != fileName)
			new FeedbackThread(fileName).start();
	}

	class FeedbackThread extends Thread {
		private String fileName;

		FeedbackThread(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public void run() {
			LwtLog.d("mAppUncaughtExceptionHandler", ">>>> feedback thread run...上传文件到服务器--->>" + fileName);
			try {
				// 打开ftp连接
				connect();

				ftpClient.changeWorkingDirectory("/logs");
				// 设置上传文件需要的一些基本信息
				ftpClient.setBufferSize(1024);
				ftpClient.setControlEncoding("UTF-8");
				ftpClient.enterLocalPassiveMode();
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				// 文件上传
				FileInputStream fileInputStream = new FileInputStream(
						AppConstants.MY_PREFERENCE_EXCEPTION_DIRECTORY + fileName);
				ftpClient.storeFile(fileName, fileInputStream);

				// 关闭文件流
				fileInputStream.close();

				// 退出登陆FTP，关闭ftpCLient的连接
				ftpClient.logout();
				ftpClient.disconnect();
				LwtLog.d("mAppUncaughtExceptionHandler", ">>>> feedback thread run...结束上传文件到服务器--->>" + fileName);
//				handler.sendEmptyMessage(OUT_THE_APP);
//                outTheApp();
			} catch (IOException e) {
				LwtLog.e(TAG, "IOException", e);
			}
		}
	}
	
	public void outTheApp() {
		// 退出
		IDSApiProxyMgr.getCurProxy().Exit();
		LwtLog.d("mAppUncaughtExceptionHandler", ">>>> 已退出并注销。");
		IDSApiProxyMgr.getCurProxy().unloadLibrary(mContext);
		//结束所有的Activity
		IdtApplication.getInstance().clearAllActivity();
		final Context appContext = mContext.getApplicationContext();
		//关闭扬声器
		SpeakerPhone speakerPhone=new SpeakerPhone(appContext);
		speakerPhone.CloseSpeaker();
		//杀死进程、退出程序
		new Thread()
		{
			public void run()
			{
				try{
					Thread.sleep(2000);
				}
				catch(Throwable e)
				{
					
				}
				ActivityManager am = (ActivityManager)appContext.getSystemService(Context.ACTIVITY_SERVICE);
				String packageName = appContext.getPackageName();
				//删除该包名相关的所有后台进程
				am.killBackgroundProcesses(packageName);
				//删除本进程
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
			}
		}.start();
	}


	/**
	 * 连接到FTP服务器
	 * 
	 * @param hostname
	 *            主机名
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return 是否连接成功
	 * @throws IOException
	 */
	public boolean connect() throws IOException {
		int port = 21;
		try {
			port = Integer.parseInt(SharedPreferencesUtil.getStringPreference(mContext, "ftp_server_port", "21"));
		} catch (NumberFormatException e) {
			port = 21;
		}
		ftpClient.connect(
				SharedPreferencesUtil.getStringPreference(mContext.getApplicationContext(), "ftp_server_host", ""),
				port);
		ftpClient.setControlEncoding("GBK");
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(SharedPreferencesUtil.getStringPreference(mContext, "ftp_account", ""),
					SharedPreferencesUtil.getStringPreference(mContext, "ftp_password", ""))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 断开与远程服务器的连接
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}
}
