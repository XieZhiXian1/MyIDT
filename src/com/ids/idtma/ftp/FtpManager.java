package com.ids.idtma.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.entity.UpgradeEntity;
import com.ids.idtma.util.AppVersion;
import com.ids.idtma.util.DOMParseXmlUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * APP更新
 */

public class FtpManager {
	private static String TAG = FtpManager.class.getSimpleName();

	public FTPClient ftpClient = new FTPClient();

	/**
	 * 下载中
	 */
	private static final int WHAT_DOWNLOAD = 1;
	/**
	 * 下载结束
	 */
	private static final int WHAT_DOWNLOAD_FINISH = 2;
	/**
	 * 有更新
	 */
	private static final int WHAT_UPDATES = 3;
	/**
	 * 无更新
	 */
	private static final int WHAT_NO_UPDATES = 4;
	/**
	 * 异常
	 */
	private static final int WHAT_EXCEPTION = 5;
	/**
	 * 文件不存在
	 */
	private static final int WHAT_FILE_NOTFOUNT = 6;

	// 升级信息
	UpgradeEntity upgradeEntity;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 正在下载
			case WHAT_DOWNLOAD:
				// 设置进度条位置
				mProgress.setProgress(progress);
				mDownloadDialog.setTitle(mContext.getString(R.string.upgrade_hint_updating) + " " + progress + "%");
				break;
			case WHAT_DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			case WHAT_UPDATES:
				// 显示提示对话框
				showNoticeDialog();
				break;
			case WHAT_NO_UPDATES:
				Toast.makeText(mContext, R.string.upgrade_hint_no_update, Toast.LENGTH_SHORT).show();
				break;
			case WHAT_FILE_NOTFOUNT:
				Toast.makeText(mContext, "服务器apk升级文件不存在", Toast.LENGTH_SHORT).show();
				break;
			case WHAT_EXCEPTION:
				Toast.makeText(mContext, "FTP升级服务连接失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	public FtpManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 检测软件更新
	 */
	public void checkUpdate() {
		new CheckUpgradeThread().start();
	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog() {
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.upgrade_hint_title);
		builder.setMessage(R.string.upgrade_hint_content);
		// 更新
		builder.setPositiveButton(R.string.btn_upgrade, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 显示下载对话框
				showDownloadDialog();
			}
		});
		// 稍后更新
		builder.setNegativeButton(R.string.btn_upgrade_later, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.upgrade_hint_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.view_update_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		/* 指定Progress为最多100 */
		// mProgress.setMax(100);
		/* 初始Progress为0 */
		// mProgress.setProgress(0);
		mProgress.setVisibility(View.VISIBLE);

		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.btn_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 设置取消状态
				cancelUpdate = true;
			}
		});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 下载文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		new DownloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 */
	private class DownloadApkThread extends Thread {
		@Override
		public void run() {
			// 判断SD卡是否存在，并且是否具有读写权限
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				try {
					/// IDT-MA/ftp/
					File downloadDirectory = new File(AppConstants.MY_PREFERENCE_DOWNLOAD_DIRECTORY);
					if (!downloadDirectory.exists()) {
						downloadDirectory.mkdirs();
					}
					download(upgradeEntity.getApkFile(),
							AppConstants.MY_PREFERENCE_DOWNLOAD_DIRECTORY + upgradeEntity.getApkFile());
				} catch (IOException e) {
					LwtLog.e(TAG, "DownloadApkThread >>>>>", e);
				}
			} else {
				LwtLog.d(TAG, "DownloadApkThread >>>>> SD card does not exist, or didn't read and write permissions.");
				// return false;
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 
	 * 上传文件到ftp线程
	 *
	 */
	private class UploadFileThread extends Thread {
		@Override
		public void run() {
			// 判断SD卡是否存在，并且是否具有读写权限
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//					upload(local_file);
			} else {
				LwtLog.d(TAG, "DownloadApkThread >>>>> SD card does not exist, or didn't read and write permissions.");
			}
		}
	}

	

	/**
	 * 检测升级线程
	 */
	private class CheckUpgradeThread extends Thread {
		@Override
		public void run() {
			try {
				if (!ftpClient.isConnected()) {
					connect();
				}

				// 改变当前ftp的工作目录
				ftpClient.changeWorkingDirectory(AppConstants.FTP_UPGRADE_DIRECTORY);

				// 创建输入流
				InputStream inputStream = ftpClient.retrieveFileStream(AppConstants.FTP_UPGRADE_FILENAME);

				// 获取服务端版本
				upgradeEntity = DOMParseXmlUtil.getUpgradeEntity(inputStream);

				LwtLog.d(TAG, "CheckUpgradeThread get upgrade info --->>>>" + upgradeEntity.toString());
				inputStream.close();
				ftpClient.logout();
				ftpClient.disconnect();
				// 版本判断
				if (!upgradeEntity.getVersionName()
						.equals(new AppVersion((ContextWrapper) mContext).getVersionName())) {
					// 更新
					mHandler.sendEmptyMessage(WHAT_UPDATES);
				} else {
					mHandler.sendEmptyMessage(WHAT_NO_UPDATES);
				}
			} catch (Exception e) {
				LwtLog.e(TAG, "CheckUpgradeThread", e);
				mHandler.sendEmptyMessage(WHAT_EXCEPTION);
			}
		}
	};

	// 查询ftp服务器上面的文件
	public void searchFtpServerFiles() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if (!ftpClient.isConnected()) {
						connect();
					}
					// 改变当前ftp的工作目录
					ftpClient.changeWorkingDirectory("/IM/2052");
					// 检查远程文件是否存在
					FTPFile[] files = ftpClient.listFiles();
					for (FTPFile f : files) {
						LwtLog.d(IdtApplication.WULIN_TAG, "文件名字:" + f.getName() + ",文件长度:" + f.getSize());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();
	}

	/**
	 * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
	 * 
	 * @param remote
	 *            远程文件路径
	 * @param local
	 *            本地文件路径
	 * @return 上传的状态
	 * @throws IOException
	 */
	public void download(String remote, String local) throws IOException {

		// 打开ftp连接
		connect();

		// 设置被动模式
		ftpClient.enterLocalPassiveMode();
		// 设置以二进制方式传输
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		// 改变当前ftp的工作目录
		ftpClient.changeWorkingDirectory(AppConstants.FTP_UPGRADE_DIRECTORY);
		// 检查远程文件是否存在
		FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"), "iso-8859-1"));

		if (files.length != 1) {
			mHandler.sendEmptyMessage(WHAT_FILE_NOTFOUNT);
			return;
		}

		long remoteFileSize = 0L;
		for (FTPFile f : files) {
			if (f.getName().equalsIgnoreCase(remote)) {
				remoteFileSize = f.getSize();
				break;
			}
		}

		File localFile = new File(local);
		// 检查文件是否存在
		if (!localFile.exists()) {
			localFile.createNewFile();
		}

		OutputStream out = new FileOutputStream(localFile);
		InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"), "iso-8859-1"));
		byte[] bytes = new byte[1024];
		long step = remoteFileSize / 100;
		progress = 0;
		long localSize = 0L;
		int size;
		while ((size = in.read(bytes)) != -1) {
			out.write(bytes, 0, size);
			localSize += size;
			int nowProgress = new Long(localSize / step).intValue();
			if (nowProgress > progress) {
				progress = nowProgress;
				if (progress % 10 == 0) {
					// 更新进度
					mHandler.sendEmptyMessage(WHAT_DOWNLOAD);
				}
			}
		}
		boolean isComplete = ftpClient.completePendingCommand();
		if (isComplete) {
			// 下载完成
			mHandler.sendEmptyMessage(WHAT_DOWNLOAD_FINISH);
		} else {
			// 下载异常
			mHandler.sendEmptyMessage(WHAT_EXCEPTION);
		}

		in.close();
		out.close();
		disconnect();

	}
	
	/**
	 * 
	 * 上传的文件或文件夹
	 * 
	 */
	private void upload(File local_file) {
		try {
			if (local_file.isDirectory()) {
				//上传目录
				ftpClient.makeDirectory(local_file.getName());
				ftpClient.changeWorkingDirectory(local_file.getName());
				String[] files = local_file.list();
				for (int i = 0; i < files.length; i++) {
					File file1 = new File(local_file.getPath() + "\\" + files[i]);
					if (file1.isDirectory()) {
						upload(file1);
						ftpClient.changeToParentDirectory();
					} else {
						File file = new File(local_file.getPath() + "\\" + files[i]);
						FileInputStream input = new FileInputStream(file);
						ftpClient.storeFile(file.getName(), input);
						input.close();
					}
				}
			} else {
				//上传文件
				File file = new File(local_file.getPath());
				FileInputStream input = new FileInputStream(file);
				ftpClient.storeFile(file.getName(), input);
				input.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
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
//		String ip = "192.168.1.105";
		String ip = "124.160.11.21";
		int port = 21218;
		try {
			port = 21218;
		} catch (NumberFormatException e) {
			port = 21218;
		}
//		LwtLog.d(IdtApplication.WULIN_TAG,
//				">>>>>>> 连接FTP服务：" + ip + " , 端口：" + port + ",用户名:"
//						+ SharedPreferencesUtil.getStringPreference(mContext, "ftp_account", "") + ",密码:"
//						+ SharedPreferencesUtil.getStringPreference(mContext, "ftp_password", ""));
		// 3秒钟，如果超过就判定超时了
		ftpClient.setConnectTimeout(3000);
		ftpClient.connect(ip, port);
		// 设置一个命令执行后最大等待Server反馈的时间
		ftpClient.setSoTimeout(3000);
		ftpClient.setControlEncoding("GBK");
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login("ftp-user-)P",
					"!@#$0p;/")) {
				LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>> 已登录FTP ");
				return true;
			}
		}
		disconnect();
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

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		if (null == upgradeEntity) {
			LwtLog.d(TAG, " >>> install Apk fail, Did not get update information. ");
			return;
		}
		File apkFile = new File(AppConstants.MY_PREFERENCE_DOWNLOAD_DIRECTORY, upgradeEntity.getApkFile());
		if (!apkFile.exists()) {
			LwtLog.d(TAG, "install Apk fail, file not exist -- > " + upgradeEntity.getApkFile());
			return;
		}
		// 通过Intent安装APK文件
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(intent);
	}
}
