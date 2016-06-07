package com.ids.idtma;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.baidu.mapapi.SDKInitializer;
import com.ids.idtma.config.ProjectConfig;
import com.ids.idtma.database.IDTDatabaseBusinesslayer;
import com.ids.idtma.database.IDTDatabaseHelper;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.jni.aidl.GpsData;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.service.LocationService;
import com.ids.idtma.util.AppUncaughtExceptionHandler;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SpeakerPhone;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

//使用单例的方式，实现存储每个activity，并实现关闭所有Activity的操作
public class IdtApplication extends Application {
	public static final String WULIN_TAG = "wulin";
	private List<Activity> mActicityList = new LinkedList<Activity>();
	private static IdtApplication mMyApplication;
	private IDTDatabaseBusinesslayer idtDatabaseBusinesslayer;
	// 组信息
	private List<GroupMember> lstGroups = new ArrayList<GroupMember>();
	// 组成员，key-组号码
	private Map<String, List<GroupMember>> mapUserGroup = new HashMap<String, List<GroupMember>>();
	
	private List<GpsData> lgpsDatas=new ArrayList<GpsData>();
	
	private List<Map<String,String>> userStatus=new ArrayList<Map<String,String>>();
	// 呼叫类
	private static CallEntity currentCall;
	private static MediaPlayer mMediaPlayer;
	private static IdtApplication context;
	// 震动
	private static Vibrator vibrator;
	public SoundPool soundPool;
	public LocationService locationService;
	public Vibrator mVibrator;

	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		// 加载默认设置 直接将xml文件对应到sharedPreferences里面
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference_base, true);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference_server, true);
		/***
		 * 初始化定位sdk，建议在Application中创建
		 */
		locationService = new LocationService(getApplicationContext());
		mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		SDKInitializer.initialize(getApplicationContext());
		// 保存服务器配置文件idt.ini到该apk文件、数据库存储目录下面
		saveIniFile();
		// 创建登录数据库
		initDatabase();
		context = this;
		// 加载音频到soundpool里面
		init();
		applicationOpenSpeaker();
		if(ProjectConfig.APP_UNCAUGHT_EXCEPTION_HANDLER_OPEN==true){
			// 处理系统没有处理的bug
			AppUncaughtExceptionHandler appUncaughtExceptionHandler = AppUncaughtExceptionHandler.getInstance();
			appUncaughtExceptionHandler.init(getApplicationContext());
		}
	}

	private void initDatabase() {
		// idtDatabaseBusinesslayer=new
		// IDTDatabaseBusinesslayer(getApplicationContext());
		idtDatabaseBusinesslayer = IDTDatabaseBusinesslayer.getInstance(getApplicationContext());
	}

	// 将raw资源库里面的idt.ini文件复制到该apk文件、数据库存储目录下面
	private void saveIniFile() {
		FileOutputStream outStream;
		try {
			outStream = this.openFileOutput("IDT.ini", Context.MODE_WORLD_READABLE);

			InputStream inStream = getResources().openRawResource(R.raw.idt);

			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			outStream.close();
			inStream.close();
			LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>>>> IDT.ini文件保存成功！");
		} catch (FileNotFoundException e) {
			LwtLog.e(IdtApplication.WULIN_TAG, "文件未找到", e);
		} catch (IOException e) {
			LwtLog.e(IdtApplication.WULIN_TAG, "文件操作异常", e);
		}
	}

	public IdtApplication() {
	}

	// 单例模式中获取唯一的application实例
	public static IdtApplication getInstance() {
		if (mMyApplication == null) {
			mMyApplication = new IdtApplication();
		}
		return mMyApplication;
	}

	public List<GroupMember> getLstGroups() {
		return lstGroups;
	}

	public void setLstGroups(List<GroupMember> lstGroups) {
		this.lstGroups = lstGroups;
	}

	public Map<String, List<GroupMember>> getMapUserGroup() {
		return mapUserGroup;
	}

	public void setMapUserGroup(Map<String, List<GroupMember>> mapUserGroup) {
		this.mapUserGroup = mapUserGroup;
	}

	public List<GpsData> getLgpsDatas() {
		return lgpsDatas;
	}

	public void setLgpsDatas(List<GpsData> lgpsDatas) {
		this.lgpsDatas = lgpsDatas;
	}
	
	public List<Map<String, String>> getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(List<Map<String, String>> userStatus) {
		this.userStatus = userStatus;
	}

	// 添加Activity到集合
	public void addActivity(Activity activity) {
		mActicityList.add(activity);
	}

	// 遍历所有Activity并finish
	public void clearAllActivity() {
		for (Activity activity : mActicityList) {
			activity.finish();
		}
	}

	// 当activity销毁时remove掉在集合中的activity
	public void deleteActivity(Activity activity) {
		mActicityList.remove(activity);
		activity.finish();
	}

	public static CallEntity getCurrentCall() {
		return currentCall;
	}

	public static void setCurrentCall(CallEntity current) {
		IdtApplication.currentCall = current;
	}

	public static boolean resumeCurrentCall() {
		if (null == getCurrentCall() || null == getCurrentCall().getIntent())
			return false;
		else {
			return true;
		}
	}

	public static void playRingtone() {
		// 开始播放手机铃声及震动
		try {
			// 注意：如果想要改变播放铃声类型，如：想要闹铃：则替换RingtoneManager.TYPE_RINGTONE为RingtoneManager.TYPE_ALARM,
			// 而如果要提示音，则替换为TYPE_NOTIFICATION。然后将mMediaPlayer.setAudioStreamType(AudioManager.STREAM_XXXXX);即可。
			// 注意：MediaPlayer设置StreamType需要在prepare之前。
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(context, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
			mMediaPlayer.prepare();
			mMediaPlayer.start();

		} catch (Exception e) {
			LwtLog.e(IdtApplication.WULIN_TAG, ">>>>>>> 播放来电铃音", e);
		}

		try {
			// 注：Vibrator.vibrate()有两个函数实现，一个是设定让其震动多长时间自动关闭，另一个实现（文中所示），是要震动以pattern节奏进行。
			vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			// 例子，在程序起动后等待2秒后，振动1秒，再等待2秒后，振动1秒，再等待2秒后，振动1秒
			long[] pattern = { 2000, 1000, 2000, 1000, 2000, 1000 }; // OFF/ON/OFF/ON
																		// //
																		// OFF/ON/OFF/ON...
			vibrator.vibrate(pattern, 0);// 第二个参数为 -1表示只震动一次，为0则震动会一直持续
		} catch (Exception e) {
			LwtLog.e(IdtApplication.WULIN_TAG, ">>>>>>> 震动", e);
		}
	}

	private void init() {
		// 系统音频流
		soundPool = new SoundPool(12, AudioManager.STREAM_SYSTEM, 5);
		soundPool.load(this, R.raw.digit0, 0);
		soundPool.load(this, R.raw.digit1, 0);
		soundPool.load(this, R.raw.digit2, 0);
		soundPool.load(this, R.raw.digit3, 0);
		soundPool.load(this, R.raw.digit4, 0);
		soundPool.load(this, R.raw.digit5, 0);
		soundPool.load(this, R.raw.digit6, 0);
		soundPool.load(this, R.raw.digit7, 0);
		soundPool.load(this, R.raw.digit8, 0);
		soundPool.load(this, R.raw.digit9, 0);
		soundPool.load(this, R.raw.digit11, 0);
		soundPool.load(this, R.raw.digit12, 0);
	}

	// 停止震动
	public static void stopRingtone() {
		// 关闭
		try {
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
			}
		} catch (Exception e) {
			LwtLog.e(IdtApplication.WULIN_TAG, ">>>>>>> 关闭播放来电铃音", e);
		}

		try {
			if (null != vibrator) {
				vibrator.cancel();
				vibrator = null;
			}
		} catch (Exception e) {
			LwtLog.e(IdtApplication.WULIN_TAG, ">>>>>>> 关闭震动", e);
		}
	}
	
	//打开扬声器
	private void applicationOpenSpeaker(){
		SpeakerPhone speakerPhone=new SpeakerPhone(this);
		speakerPhone.OpenSpeaker();
	}
}
