package com.ids.idtma.voicerecord;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import com.ids.idtma.voicerecord.RecordManger;
import com.ids.idtma.voicerecord.RecordManger.Listener;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.DateFormat;

public class TalkNetManager implements RecordManger.Listener {
	/** 录音管理类 */
	private RecordManger recordManger;
	private Context context;
	/** 下载文件服务器地址 */
	private String downloadFileServerUrl;
	/** 下载文件服务器地址 */
	private String uploadFileServerUrl;

	private OnStateListener uploadFileStateListener;
	private OnStateListener downloadFileFileStateListener;

	public interface Listener {
		public void onCreatedVoiceFile(int time_length);
	}

	private Listener mListener;

	public void setListener(Listener listener) {
		this.mListener = listener;
	}
	
	

	public TalkNetManager() {
		super();
		recordManger = new RecordManger();
	    recordManger.setListener(TalkNetManager.this);
	}



	/** 启动录音不进行网络上传 */
	public void startRecord(String file_name) {
		try {
			recordManger.startRecordCreateFile(file_name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 停止录音 */
	public File stopRecord() {
		return recordManger.stopRecord();// 停止录音
	}

	// /** 停止录音后上传 */
	// public File stopRecordAndUpload() {
	// File file = recordManger.stopRecord();// 停止录音
	// if (file == null || !file.exists() || file.length() == 0) {
	// if (uploadFileStateListener != null)
	// uploadFileStateListener.onState(-1, "文件不存在或已经损坏");
	// return null;
	// }
	//
	// new UpLoadecordFile().execute();// 调用异步任务
	// return recordManger.getFile();
	// }

	/** 异步任务-录音上传 */
	public class UpLoadecordFile extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... parameters) {
			// TODO Auto-generated method stub11
			return UploadUtil.uploadFile(recordManger.getFile(), uploadFileServerUrl);

		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				if (uploadFileStateListener != null)
					uploadFileStateListener.onState(-2, "上传文件失败");
				return;
			}

			if (result != null)
				if (uploadFileStateListener != null)
					uploadFileStateListener.onState(0, "上传文件成功");
		}

	}

	/** 下载后播放 */
	public void downloadFileAndPlay(String uploadFilename) {
		new DownloadRecordFile().execute(uploadFilename);
	}

	/** 异步任务-下载后播放 */
	public class DownloadRecordFile extends AsyncTask<String, Integer, File> {

		@Override
		protected File doInBackground(String... parameters) {
			// TODO Auto-generated method stub11
			try {
				String filename = new DateFormat().format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA))
						+ ".amr";
				return FileHelper.DownloadFromUrlToData(downloadFileServerUrl + parameters[0], filename, context);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected void onPostExecute(File result) {

			if (result == null || !result.exists() || result.length() == 0) {
				if (downloadFileFileStateListener != null) {
					downloadFileFileStateListener.onState(-1, "下载文件失败");
					return;
				}
			}
			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					// TODO Auto-generated method stub
					mediaPlayer.release();
				}
			});
			try {
				mediaPlayer.setDataSource(result.getPath());
				mediaPlayer.prepare();
				mediaPlayer.start();
				if (downloadFileFileStateListener != null) {
					downloadFileFileStateListener.onState(0, "成功");
					return;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public RecordManger getRecordManger() {
		return recordManger;
	}

	public void setRecordManger(RecordManger recordManger) {
		this.recordManger = recordManger;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setDownloadFileServerUrl(String downloadFileServerUrl) {
		this.downloadFileServerUrl = downloadFileServerUrl;
	}

	public String getUploadFileServerUrl() {
		return uploadFileServerUrl;
	}

	public void setUploadFileServerUrl(String uploadFileServerUrl) {
		this.uploadFileServerUrl = uploadFileServerUrl;
	}

	public OnStateListener getUploadFileStateListener() {
		return uploadFileStateListener;
	}

	public void setUploadFileStateListener(OnStateListener uploadFileStateListener) {
		this.uploadFileStateListener = uploadFileStateListener;
	}

	public OnStateListener getDownloadFileFileStateListener() {
		return downloadFileFileStateListener;
	}

	public void setDownloadFileFileStateListener(OnStateListener downloadFileFileStateListener) {
		this.downloadFileFileStateListener = downloadFileFileStateListener;
	}

	public String getDownloadFileServerUrl() {
		return downloadFileServerUrl;
	}

	@Override
	public void onCreatedVoiceFile(int time_length) {
		// TODO Auto-generated method stub
		mListener.onCreatedVoiceFile(time_length);
	}
}
