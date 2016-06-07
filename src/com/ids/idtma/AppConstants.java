package com.ids.idtma;
import android.os.Environment;

//app的常亮
public class AppConstants {
	public static String CACHE_PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/mediacenter/imgcache/";

	/**
	 * 业务类型：组呼、会议
	 */
	public final static int CALL_TYPE_GROUP_CALL = 17;//0x11
	/**
	 * 业务类型：单呼
	 */
	public final static int CALL_TYPE_SINGLE_CALL = 16;//0x10
	/**
	 * 应用上下文设置
	 */
	public final static String MY_PREFERENCE = "com.ids.idtma.preference";
	/**
	 * 相片
	 */
	public final static String MY_PREFERENCE_PHOTO = "com.ids.idtma.photo";
	/**
	 * 视频
	 */
	public final static String MY_PREFERENCE_VIDEO = "com.ids.idtma.video";
	/**
	 * 下载保存路径
	 */
	public final static String MY_PREFERENCE_DOWNLOAD_DIRECTORY = Environment
			.getExternalStorageDirectory().getPath() + "/IDT-MA/ftp/";
	/**
	 * 异常文件保存路径
	 */
	public final static String MY_PREFERENCE_EXCEPTION_DIRECTORY = Environment
			.getExternalStorageDirectory().getPath() + "/IDT-MA/exception/";
	
	/**
	 * 异常文件保存路径
	 */
	public final static String MY_BAIDU_MAP_DATA_DIRECTORY = Environment
			.getExternalStorageDirectory().getPath() + "/IDT-MA/baidu/";

	/**
	 * 网络状态 : true - online ; false - offline
	 */
	public final static String MY_PREFERENCE_NETWORK_STATUS = "network_sataus";
	/**
	 * 系统版本
	 */
	public final static String APP_VERSION = "App_Version";

	/**
	 * FTP升级说明文件名
	 */
	public final static String FTP_UPGRADE_FILENAME = "upgrade.xml";
	/**
	 * FTP服务器升级目录
	 */
	public final static String FTP_UPGRADE_DIRECTORY = "/upgrade";

	/**
	 * perference key : 帐号
	 */
	public final static String KEY_PHONE_NUMBER = "phone_number";

	/**
	 * GPS经度-Y
	 */
	public final static String GPS_LOCATION_LONGITUDE = "gps_location_longitude";
	/**
	 * GPS纬度-X
	 */
	public final static String GPS_LOCATION_LATITUDE = "gps_location_latitude";
	/**
	 * 最后一次记录的GPS信息时间
	 */
	public final static String LAST_KNOWN_LOCATION_TIME = "last_known_location_time";

	/**
	 * Intent action 呼叫进来
	 */
	public final static String ACTION_CALL_IN = "IDT.intent.action.CALL_IN";
	/**
	 * Intent action 对端应答
	 */
	public final static String ACTION_CALL_PEER_ANSWER = "IDT.intent.action.CALL_PEER_ANSWER";
	/**
	 * Intent action 讲话方提示
	 */
	public final static String ACTION_CALL_TALKING_TIPS = "IDT.intent.action.CALL_TALKING_TIPS";
	/**
	 * Intent action 远端释放
	 */
	public final static String ACTION_CALL_REL_IND = "IDT.intent.action.CALL_REL_IND";
	/**
	 * Intent action 注册成功
	 */
	public final static String ACTION_NETWORK_ON_LINE = "IDT.intent.action.NETWORK_ON_LINE";
	/**
	 * Intent action 收到视频数据
	 */
	public final static String ACTION_RECEIVE_VIDEO_DATA = "IDT.intent.action.RECEIVE_VIDEO_DATA";
	/**
	 * Intent action 收到组成员信息
	 */
	public final static String ACTION_RECEIVE_GROUP_DATA = "IDT.intent.action.RECEIVE_GROUP_DATA";

	/**
	 * 参数 -- 呼叫ID
	 */
	public final static String EXTRA_KEY_CALLID = "callID";
	/**
	 * 参数 -- 主叫
	 */
	public final static String EXTRA_KEY_CALLER = "caller";
	/**
	 * 参数 -- 被叫
	 */
	public final static String EXTRA_KEY_CALLEE = "callee";
	/**
	 * 参数 -- 呼叫状态
	 */
	public final static String EXTRA_KEY_CALL_STATUS = "callStatus";
	/**
	 * 参数 -- calllog uri
	 */
	public final static String EXTRA_KEY_CALLLOG_URI = "calllogUri";
	/**
	 * 参数 -- notification intent
	 */
	public final static String EXTRA_KEY_NOTIFICATION_INTENT = "notificationIntent";
	/**
	 * 参数 --媒体属性
	 */
	public final static String EXTRA_KEY_MediaAttr = "MediaAttr";
	/**
	 * 参数 -- 组呼号码
	 */
	public final static String EXTRA_KEY_GROUP_CALL_NUM = "group_call_num";
	/**
	 * 参数 -- 讲话方名称
	 */
	public final static String EXTRA_KEY_TALKING_USERNAME = "talking_user_name";
	/**
	 * 参数 -- 讲话方号码
	 */
	public final static String EXTRA_KEY_TALKING_PHONE = "talking_user_phone";
	/**
	 * 参数 -- 收到视频数据
	 */
	public final static String EXTRA_KEY_RECEIVE_VIDEO_DATA = "receive_video_data";
	/**
	 * lock group number
	 */
	private static String LOCKED_GROUP_NUM = "";
	/**
	 * sms notification id
	 */
	public final static int SMS_NOTIFICATION_ID = 131;
	/**
	 * call notification id
	 */
	public final static int CALL_NOTIFICATION_ID = 132;

	public static String getLockedGroupNum() {
		return LOCKED_GROUP_NUM;
	}


	public final static String ACTION_Server_Config_changed = "IDT.intent.action.server_config_changed";
	public final static String ACTION_OrganizeDataChanged= "IDT.intent.action.organizeDataChanged";
	
	//收到工单
	public final static String ACTION_WORK_ORDER_RECV = "IDT.intent.action.recv_work_order";
	public final static String EXTRA_WORK_ORDER_MSG="WORK_ORDER_MSG";//
	public final static String EXTRA_WORK_ORDER_DESC="WORK_ORDER_DESC";
	
	public final static String EXTRA_WORK_ORDER_TYPE = "WORK_ORDER_TYPE";
	
	public final static String EXTRA_WORK_ORDER_GPS_X = "WORK_ORDER_GPS_X";
	public final static String EXTRA_WORK_ORDER_GPS_Y = "WORK_ORDER_GPS_Y";
	
	public final static String EXTRA_WORK_ORDER_DRIVER_NAME = "WORK_ORDER_DRIVER_NAME";
	public final static String EXTRA_WORK_ORDER_DRIVER_TEL	= "WORK_ORDER_DRIVER_TEL";	
	public final static String EXTRA_WORK_ORDER_DRIVER_MOBILE	= "WORK_ORDER_DRIVER_MOBILE";	
	public final static String EXTRA_WORK_ORDER_DRIVER_ADDRESS	= "WORK_ORDER_DRIVER_ADDRESS";	
	public final static String EXTRA_WORK_ORDER_DRIVER_TIME	= "WORK_ORDER_DRIVER_TIME";	
	
	public final static String EXTRA_WORK_ORDER_PASSENGER_NUMBER = "WORK_ORDER_PASSENGER_NUMBER";
	public final static String EXTRA_WORK_ORDER_PASSENGER_NAME = "WORK_ORDER_PASSENGER_NAME";
	public final static String EXTRA_WORK_ORDER_PASSENGER_TEL = "WORK_ORDER_PASSENGER_TEL";
	public final static String EXTRA_WORK_ORDER_PASSENGER_MOBILE = "WORK_ORDER_PASSENGER_MOBILE";
	public final static String EXTRA_WORK_ORDER_PASSENGER_ADDRESS = "WORK_ORDER_PASSENGER_ADDRESS";
	public final static String EXTRA_WORK_ORDER_PASSENGER_TIME = "WORK_ORDER_PASSENGER_TIME";
}
