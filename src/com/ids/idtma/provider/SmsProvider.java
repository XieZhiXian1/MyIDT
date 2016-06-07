package com.ids.idtma.provider;

import java.util.HashMap;

import com.ids.idtma.util.LwtLog;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class SmsProvider extends ContentProvider { 
	private static String TAG = SmsProvider.class.getName();
	private DBHelper dbHelper;

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ SmsProvider.class.getName() + "/" + DBHelper.TABLE_SMS);

	private static final int ALLROWS = 1;
	private static final int SINGLE_ROW = 2;
	public static final String DEFAULT_SORT_ORDER = " _id ";

	public static final String KEY_ID = "_id";
	public static final String KEY_COLUMN_1_PHONE_NUMBER = "phone_number";
	public static final String KEY_COLUMN_2_SMS_CONTENT = "sms_content";
	public static final String KEY_COLUMN_3_SMS_TYPE = "sms_type";
	public static final String KEY_COLUMN_4_READ = "read";
	public static final String KEY_COLUMN_5_CREATE_TIME = "create_time";
	public static final String KEY_COLUMN_6_SMS_RESOURCE_URL = "sms_resource_url";
	public static final String KEY_COLUMN_7_SMS_RESOURCE_TYPE = "sms_resource_type";
	public static final String KEY_COLUMN_8_SMS_RESOURCE_NAME = "sms_resource_name";
	public static final String KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH = "sms_resource_time_length";
	public static final String KEY_COLUMN_10_SMS_RESOURCE_RS_OK = "sms_resource_rs_ok";
	//目标
	public static final String KEY_COLUMN_11_TARGET_PHONE_NUMBER="target_phone_number";
	public static final String KEY_COLUMN_12_OWNER_PHONE_NUMBER="owner_phone_number";
	public static final String KEY_COLUMN_13_IS_GROUP_MESSAGE="is_group_message";
	public static final String KEY_COLUMN_14_UI_CAUSE="uiCause";
	
	public static final int SMS_TYPE_inBox = 1;
	public static final int SMS_TYPE_WORK_ORDER = 7;

	public static final String[] ALL_PROJECTION = new String[] { KEY_ID, // 0
			KEY_COLUMN_1_PHONE_NUMBER, // 1
			KEY_COLUMN_2_SMS_CONTENT, // 2
			KEY_COLUMN_3_SMS_TYPE, // 3 --- 0:all 1:inBox 2:sent 3:draft
									// 4:outBox 5:failed
			// 6:queued
			KEY_COLUMN_4_READ, // 4
			KEY_COLUMN_5_CREATE_TIME, // 5; 0:not read 1:read; default is 0
			KEY_COLUMN_6_SMS_RESOURCE_URL,
			KEY_COLUMN_7_SMS_RESOURCE_TYPE,  //0:not  1:text 2:gps position 3:image 4:voice or weixin 4:vedio
			KEY_COLUMN_8_SMS_RESOURCE_NAME,
			KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH,
			KEY_COLUMN_10_SMS_RESOURCE_RS_OK,  //0:还没有发送成功，或者接受成功   1:已经发送成功或者接受成功
			KEY_COLUMN_11_TARGET_PHONE_NUMBER,
			KEY_COLUMN_12_OWNER_PHONE_NUMBER,
			KEY_COLUMN_13_IS_GROUP_MESSAGE,  //1:是组消息   0：不是组消息
			KEY_COLUMN_14_UI_CAUSE
	};

	private static final UriMatcher uriMatcher;

	private static HashMap<String, String> projectionMap;
	static {

		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);// 不匹配根路径

		uriMatcher.addURI(SmsProvider.class.getName(), DBHelper.TABLE_SMS,
				ALLROWS);
		uriMatcher.addURI(SmsProvider.class.getName(), DBHelper.TABLE_SMS
				+ "/#", SINGLE_ROW);

		// 实例化查询列集合
		projectionMap = new HashMap<String, String>();
		// 添加查询列
		projectionMap.put(KEY_ID, KEY_ID);
		projectionMap.put(KEY_COLUMN_1_PHONE_NUMBER, KEY_COLUMN_1_PHONE_NUMBER);
		projectionMap.put(KEY_COLUMN_2_SMS_CONTENT, KEY_COLUMN_2_SMS_CONTENT);
		projectionMap.put(KEY_COLUMN_3_SMS_TYPE, KEY_COLUMN_3_SMS_TYPE);
		projectionMap.put(KEY_COLUMN_4_READ, KEY_COLUMN_4_READ);
		projectionMap.put(KEY_COLUMN_5_CREATE_TIME, KEY_COLUMN_5_CREATE_TIME);
		projectionMap.put(KEY_COLUMN_6_SMS_RESOURCE_URL, KEY_COLUMN_6_SMS_RESOURCE_URL);
		projectionMap.put(KEY_COLUMN_7_SMS_RESOURCE_TYPE, KEY_COLUMN_7_SMS_RESOURCE_TYPE);
		projectionMap.put(KEY_COLUMN_8_SMS_RESOURCE_NAME, KEY_COLUMN_8_SMS_RESOURCE_NAME);
		projectionMap.put(KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH);
		projectionMap.put(KEY_COLUMN_10_SMS_RESOURCE_RS_OK, KEY_COLUMN_10_SMS_RESOURCE_RS_OK);
		projectionMap.put(KEY_COLUMN_11_TARGET_PHONE_NUMBER, KEY_COLUMN_11_TARGET_PHONE_NUMBER);
		projectionMap.put(KEY_COLUMN_12_OWNER_PHONE_NUMBER, KEY_COLUMN_12_OWNER_PHONE_NUMBER);
		projectionMap.put(KEY_COLUMN_13_IS_GROUP_MESSAGE, KEY_COLUMN_13_IS_GROUP_MESSAGE);
		projectionMap.put(KEY_COLUMN_14_UI_CAUSE, KEY_COLUMN_14_UI_CAUSE);
	}

	@Override
	public boolean onCreate() {
		dbHelper = DBHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {
		// 查询所有
		case ALLROWS:
			queryBuilder.setTables(DBHelper.TABLE_SMS);
			queryBuilder.setProjectionMap(projectionMap);
			break;
		case SINGLE_ROW:
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(KEY_ID + "=" + rowID);
			break;
		default:
			break;
		}

		if (TextUtils.isEmpty(sortOrder)) {
			sortOrder = DEFAULT_SORT_ORDER;
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// Perform the deletion.
		int deleteCount = db.delete(DBHelper.TABLE_SMS, selection,
				selectionArgs);

		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);
		LwtLog.d(TAG, "删除了短信记录条数：>>>>>>>>>>> " + deleteCount);

		// Return the number of deleted items.
		return deleteCount;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// 获得数据库实例
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// 插入数据，返回行ID
		long rowId = db.insert(DBHelper.TABLE_SMS, null, values);
		// 如果插入成功返回uri
		if (rowId > 0) {
			Uri empUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(empUri, null);
			return empUri;
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// 获得数据库实例
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// Perform the update.
		int updateCount = db.update(DBHelper.TABLE_SMS, values, selection,
				selectionArgs);

		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);

		return updateCount;
	}

	@Override
	public String getType(Uri uri) {
		// switch (uriMatcher.match(uri)) {
		// case ALLROWS:
		// return "vnd.android.cursor.dir/vnd.com.ids.idtma.providers.sms";
		// case SINGLE_ROW:
		// return "vnd.android.cursor.item/vnd.com.ids.idtma.providers.sms";
		// default:
		// throw new IllegalArgumentException("Unsupported URI: " + uri);
		// }
		return null;
	}

}
