package com.ids.idtma.voicerecord;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.provider.SmsProvider;
import com.ids.idtma.util.FileUtil;
import android.R.string;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.widget.Toast;

public class RecordManger {
	/** 录音后文件 */
	private File file;
	/** android媒体录音类 */
	private MediaRecorder mr;
	/** 声波振幅监听器 */
	private SoundAmplitudeListen soundAmplitudeListen;

	private String old_file_name = "";
	/** 启动计时器监听振幅波动 */
	private final Handler mHandler = new Handler();

	public interface Listener {
		public void onCreatedVoiceFile(int time_length);
	}

	private Listener mListener;

	public void setListener(Listener listener) {
		this.mListener = listener;
	}

	private Runnable mUpdateMicStatusTimer = new Runnable() {
		/**
		 * 分贝的计算公式K=20lg(Vo/Vi) Vo当前振幅值 Vi基准值为600
		 */
		private int BASE = 600;
		private int RATIO = 5;
		private int postDelayed = 200;

		public void run() {
			// int vuSize = 10 * mMediaRecorder.getMaxAmplitude() / 32768;
			int ratio = mr.getMaxAmplitude() / BASE;
			int db = (int) (20 * Math.log10(Math.abs(ratio)));
			int value = db / RATIO;
			if (value < 0)
				value = 0;
			if (soundAmplitudeListen != null)
				soundAmplitudeListen.amplitude(ratio, db, value);
			mHandler.postDelayed(mUpdateMicStatusTimer, postDelayed);

		}
	};
	// 秒
	private int SMS_RESOURCE_TIME_LENGTH = 0;
	// 毫秒
	private long SMS_RESOURCE_START_TIME = 0;
	private long SMS_RESOURCE_STOP_TIME = 0;

	/** 启动录音并生成文件 */
	public void startRecordCreateFile(String file_name) throws IOException {
		if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 存在sd卡
			return;
		}
		SMS_RESOURCE_TIME_LENGTH = 0;
		SMS_RESOURCE_START_TIME = 0;
		SMS_RESOURCE_STOP_TIME = 0;
		SMS_RESOURCE_START_TIME = System.currentTimeMillis();
		file = new File(file_name);
		mr = new MediaRecorder(); // 创建录音对象
		mr.setAudioSource(MediaRecorder.AudioSource.DEFAULT);// 从麦克风源进行录音
		mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 设置输出格式
		mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);// 设置编码格式
		mr.setOutputFile(file.getAbsolutePath());// 设置输出文件
		file.getParentFile().mkdirs();
		// 创建文件
		file.createNewFile();
		// 准备录制
		mr.prepare();

		// 开始录制
		mr.start();
		// 启动振幅监听计时器
		mHandler.post(mUpdateMicStatusTimer);

	}

	/** 停止录音并返回录音文件 */
	public File stopRecord() {
		try {
			if (mr != null) {
				mr.stop();
				mr.release();
				mr = null;
				SMS_RESOURCE_STOP_TIME = System.currentTimeMillis();
				SMS_RESOURCE_TIME_LENGTH = (int) Math.ceil(((double)(SMS_RESOURCE_STOP_TIME - SMS_RESOURCE_START_TIME)) / 1000);
				mListener.onCreatedVoiceFile(SMS_RESOURCE_TIME_LENGTH);
				mHandler.removeCallbacks(mUpdateMicStatusTimer);
				return file;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public MediaRecorder getMr() {
		return mr;
	}

	public void setMr(MediaRecorder mr) {
		this.mr = mr;
	}

	public SoundAmplitudeListen getSoundAmplitudeListen() {
		return soundAmplitudeListen;
	}

	public void setSoundAmplitudeListen(SoundAmplitudeListen soundAmplitudeListen) {
		this.soundAmplitudeListen = soundAmplitudeListen;
	}

	public interface SoundAmplitudeListen {
		public void amplitude(int amplitude, int db, int value);
	}

}
