package com.ids.idtma.ftp;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.ids.idtma.ActivityBase;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.chat.PathConstant;
import com.ids.idtma.ftp.FtpUtil.DownLoadProgressListener;
import com.ids.idtma.ftp.FtpUtil.NOExistNewEdition;
import com.ids.idtma.ftp.FtpUtil.UploadProgressListener;
import com.ids.idtma.util.AppVersion;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.StringsUtils;
import com.ids.idtma.voicerecord.TalkNetManager.Listener;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class FtpBuinessLayer {
	public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
	public static final String FTP_CONNECT_FAIL = "ftp连接失败";
	public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
	public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";

	public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
	public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
	public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";

	public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
	public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
	public static final String FTP_DOWN_FAIL = "ftp文件下载失败";

	public static final String FTP_DELETEFILE_SUCCESS = "ftp文件删除成功";
	public static final String FTP_DELETEFILE_FAIL = "ftp文件删除失败";
	private Context mContext;
	private FtpUtil ftpUtil;

	public FtpBuinessLayer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public interface Listener {
		public void fileUploadSuccess(int style, Uri uri, String callto_persion_num,String filename);

		public void fileDownloadSuccess();

		public void apkDownloadCase(String case_status, String install_apk_path,long process);
		
		public void noExitNewEdition();
		
		public void exitNewEdition();

	}

	private Listener mListener;

	public void setListener(Listener listener) {
		this.mListener = listener;
	}

	public FtpBuinessLayer(Context context) {
		super();
		this.mContext = context;
		ftpInit();
	}

	public void ftpInit() {
		String hostName = SharedPreferencesUtil.getStringPreference(mContext, "ftp_server_host", "");
		int serverPort = Integer.parseInt(SharedPreferencesUtil.getStringPreference(mContext, "ftp_server_port", "21"));
		String userName = SharedPreferencesUtil.getStringPreference(mContext, "ftp_account", "");
		String password = SharedPreferencesUtil.getStringPreference(mContext, "ftp_password", "");
		LwtLog.d("wulin", "ftp hostname:" + hostName + ",serverPort:" + serverPort + ",username:" + userName
				+ ",password:" + password);
		ftpUtil = new FtpUtil(hostName, serverPort, userName, password);
	}

//	// 生成本地和远程路径和文件名
//	private void createFileNameAndPath(String directory_num, int file_type) {
//		String data_time = DateUtil.dateToString(new Date(), DateUtil.TIME_PATTERN_6);
//		String random_string = StringsUtils.getRandomString(8);
//		PathConstant.REMOTE_FTP_PATH = "/IM/" + directory_num;
//		if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
//			// 存在sd卡
//			return;
//		}
//		if (file_type == 4) {
//			PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".amr";
//		} else if (file_type == 3) {
//			PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".png";
//		} else if (file_type == 5) {
//			PathConstant.REMOTE_FTP_FILE = data_time + "-" + random_string + ".mp4";
//		}
//		PathConstant.LOCAL_FILE_STRING = Environment.getExternalStorageDirectory().getPath() + "/IDT-MA"
//				+ PathConstant.REMOTE_FTP_PATH + "/" + PathConstant.REMOTE_FTP_FILE;
//		SharedPreferencesUtil.setStringPreferences(mContext, "REMOTE_FTP_PATH", PathConstant.REMOTE_FTP_PATH);
//		SharedPreferencesUtil.setStringPreferences(mContext, "REMOTE_FTP_FILE", PathConstant.REMOTE_FTP_FILE);
//		SharedPreferencesUtil.setStringPreferences(mContext, "LOCAL_FILE_STRING", PathConstant.LOCAL_FILE_STRING);
//	}
//
//	private void startUploadFile(int style, Uri uri, String callto_persion_num) {
//		PathConstant.REMOTE_FTP_PATH = SharedPreferencesUtil.getStringPreference(mContext, "REMOTE_FTP_PATH", "");
//		PathConstant.REMOTE_FTP_FILE = SharedPreferencesUtil.getStringPreference(mContext, "REMOTE_FTP_FILE", "");
//		PathConstant.LOCAL_FILE_STRING = SharedPreferencesUtil.getStringPreference(mContext, "LOCAL_FILE_STRING", "");
//		uploadFile(PathConstant.LOCAL_FILE_STRING, PathConstant.REMOTE_FTP_PATH, style, uri, callto_persion_num);
//	}
	
	public void uploadFile(final String file_path, final String remote_path, final int style, final Uri uri,
			final String callto_persion_num,final String local_filename) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 上传
				File file = new File(file_path);
				try {
					// 单文件上传
					ftpUtil.uploadSingleFile(file, remote_path,new UploadProgressListener() {

						@Override
						public void onUploadProgress(String currentStep, long uploadSize, File file) {
							// TODO Auto-generated method stub
							// LwtLog.d(IdtApplication.WULIN_TAG, currentStep);
							if (currentStep.equals(FTP_UPLOAD_SUCCESS)) {
								LwtLog.d(IdtApplication.WULIN_TAG, "上传成功");
								mListener.fileUploadSuccess(style, uri, callto_persion_num,local_filename);
							} else if (currentStep.equals(FTP_UPLOAD_LOADING)) {
								// long fize = file.length();
								// float num = (float) uploadSize / (float)
								// fize;
								// int result = (int) (num * 100);
								// LwtLog.d(IdtApplication.WULIN_TAG, "文件大小:" +
								// file.length() + ",上传部分大小:" + uploadSize
								// + "-----上传百分比---" + result + "%");
							}
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LwtLog.d(IdtApplication.WULIN_TAG, "上传过程发生io exception故障");
					e.printStackTrace();
				}

			}
		}).start();
	}
	
	//地图界面上传
	public void uploadFile(final String file_path, final String remote_path, final String remote_file_name) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 上传
				File file = new File(file_path);
				try {
					// 单文件上传
					ftpUtil.uploadSingleFile(file, remote_path, remote_file_name,new UploadProgressListener() {

						@Override
						public void onUploadProgress(String currentStep, long uploadSize, File file) {
							// TODO Auto-generated method stub
							// LwtLog.d(IdtApplication.WULIN_TAG, currentStep);
							if (currentStep.equals(FTP_UPLOAD_SUCCESS)) {
								LwtLog.d(IdtApplication.WULIN_TAG, "上传成功");
								mListener.fileUploadSuccess(-1, null, "","");
							} else if (currentStep.equals(FTP_UPLOAD_LOADING)) {
								// long fize = file.length();
								// float num = (float) uploadSize / (float)
								// fize;
								// int result = (int) (num * 100);
								// LwtLog.d(IdtApplication.WULIN_TAG, "文件大小:" +
								// file.length() + ",上传部分大小:" + uploadSize
								// + "-----上传百分比---" + result + "%");
							}
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LwtLog.d(IdtApplication.WULIN_TAG, "上传过程发生io exception故障");
					e.printStackTrace();
				}

			}
		}).start();
	}

	//此处带了一个远程文件名，因为当上传文件的时候，本地名称和上传名称不一致
	public void uploadFile(final String file_path, final String remote_path, final int style, final Uri uri,
			final String callto_persion_num,final String local_filename,final String remote_file_name) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 上传
				File file = new File(file_path);
				try {
					// 单文件上传
					ftpUtil.uploadSingleFile(file, remote_path, remote_file_name,new UploadProgressListener() {

						@Override
						public void onUploadProgress(String currentStep, long uploadSize, File file) {
							// TODO Auto-generated method stub
							// LwtLog.d(IdtApplication.WULIN_TAG, currentStep);
							if (currentStep.equals(FTP_UPLOAD_SUCCESS)) {
								LwtLog.d(IdtApplication.WULIN_TAG, "上传成功");
								mListener.fileUploadSuccess(style, uri, callto_persion_num,local_filename);
							} else if (currentStep.equals(FTP_UPLOAD_LOADING)) {
								// long fize = file.length();
								// float num = (float) uploadSize / (float)
								// fize;
								// int result = (int) (num * 100);
								// LwtLog.d(IdtApplication.WULIN_TAG, "文件大小:" +
								// file.length() + ",上传部分大小:" + uploadSize
								// + "-----上传百分比---" + result + "%");
							}
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LwtLog.d(IdtApplication.WULIN_TAG, "上传过程发生io exception故障");
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void downLoadFile(final String server_file_String_name, final String server_file_path,
			final String local_path, final String local_store_name) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 下载
				try {
					// ftpUtil.downloadSingleFile("/fff/ftpTest.docx","/mnt/sdcard/download/","ftpTest.docx",new
					// DownLoadProgressListener(){
					// 单文件下载
					ftpUtil.downloadSingleFile(server_file_String_name, server_file_path, local_path, local_store_name,
							new DownLoadProgressListener() {

								@Override
								public void onDownLoadProgress(String currentStep, long downProcess, File file,
										String install_apk_path) {
									// LwtLog.d(IdtApplication.WULIN_TAG,
									// currentStep);
									if (currentStep.equals(FTP_DOWN_SUCCESS)) {
										LwtLog.d(IdtApplication.WULIN_TAG, "下载成功");
										mListener.fileDownloadSuccess();
									} else if (currentStep.equals(FTP_DOWN_LOADING)) {

									}
								}

							});

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}
	
	
	//service会进行调用
	public void downLoadFile(final String server_file_String_name, final String server_file_path,
			final String local_path, final String local_store_name,final int dwtype,final Uri uri) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 下载
				try {
					// 单文件下载
					ftpUtil.downloadSingleFile(server_file_String_name, server_file_path, local_path, local_store_name,
							new DownLoadProgressListener() {

								@Override
								public void onDownLoadProgress(String currentStep, long downProcess, File file,
										String install_apk_path) {
									// LwtLog.d(IdtApplication.WULIN_TAG,
									// currentStep);
									if (currentStep.equals(FTP_DOWN_SUCCESS)) {
										LwtLog.d(IdtApplication.WULIN_TAG, "下载成功");
										Intent intent = new Intent();
										intent.setAction(ActivityBase.IM_DOWNLOAD_SUCCESS);
										intent.putExtra("uri", uri);
										mContext.sendBroadcast(intent);
									} else if (currentStep.equals(FTP_DOWN_LOADING)) {

									}
								}

							});

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void searchFromFtpAndDownload(final String server_file_path, final String local_path) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int version_code = new AppVersion((ContextWrapper) mContext).getVersionCode();
				ftpUtil.searchFtpDownload(server_file_path, version_code, local_path, new DownLoadProgressListener() {

					@Override
					public void onDownLoadProgress(String currentStep, long downProcess, File file,
							String local_file_path) {
						// TODO Auto-generated method stub
						if (currentStep.equals(FTP_DOWN_SUCCESS)) {
							LwtLog.d(IdtApplication.WULIN_TAG, "下载成功");
							mListener.apkDownloadCase(FTP_DOWN_SUCCESS, local_file_path,-1);
						} else if (currentStep.equals(FTP_DOWN_LOADING)) {
							mListener.apkDownloadCase(FTP_DOWN_LOADING, "" ,downProcess);
						} else if (currentStep.equals(FTP_CONNECT_FAIL)) {
							mListener.apkDownloadCase(FTP_CONNECT_FAIL, "" ,-1);
						} else if (currentStep.equals(FTP_FILE_NOTEXISTS)) {
							mListener.apkDownloadCase(FTP_FILE_NOTEXISTS, "" , -1);
						}
					}
				},new NOExistNewEdition() {
					
					@Override
					public void onNotExistNewEdition() {
						// TODO Auto-generated method stub
						mListener.noExitNewEdition();
					}

					@Override
					public void existNewEdition() {
						// TODO Auto-generated method stub
						mListener.exitNewEdition();
					}
				});
			}
		}).start();
	}

}
