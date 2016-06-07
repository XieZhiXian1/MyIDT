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

public class CalllogProvider extends ContentProvider {
	private static String TAG = CalllogProvider.class.getName();
	private DBHelper dbHelper;

	private static final UriMatcher uriMatcher;

	private static final int ALLROWS = 1;
	private static final int SINGLE_ROW = 2;
	//对ContentProvider的介绍
	//http://www.2cto.com/kf/201404/296974.html
    //content://CalllogProvider/call_log
	//自定义的数据库表格
	//以下为本ContentProvider的唯一URI
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ CalllogProvider.class.getName() + "/" + DBHelper.TABLE_CALL_LOG);

	public static final String DEFAULT_SORT_ORDER = " _id DESC ";
	public static final String KEY_ID = "_id";
	public static final String KEY_COLUMN_1_PHONE_NUMBER = "phone_number";
	public static final String KEY_COLUMN_2_CALL_TYPE = "call_type";
	public static final String KEY_COLUMN_3_CREATE_TIME = "create_time";
	public static final String KEY_COLUMN_4_NAME = "name";
	public static final String KEY_COLUMN_5_CONTACTS_ID = "contacts_id";

	public final static String[] ALL_PROJECTION = new String[] { KEY_ID,
			KEY_COLUMN_1_PHONE_NUMBER, KEY_COLUMN_2_CALL_TYPE,
			KEY_COLUMN_3_CREATE_TIME, KEY_COLUMN_4_NAME,
			KEY_COLUMN_5_CONTACTS_ID };

	private static HashMap<String, String> projectionMap;
	static {
		//常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		//如果match()方法匹配content://CalllogProvider/call_log路径，返回匹配码为ALLROWS
		uriMatcher.addURI(CalllogProvider.class.getName(),
				DBHelper.TABLE_CALL_LOG, ALLROWS);
		//如果match()方法匹配content://CalllogProvider/call_log/112路径，返回匹配码为SINGLE_ROW
		//#为通配符
		uriMatcher.addURI(CalllogProvider.class.getName(),
				DBHelper.TABLE_CALL_LOG + "/#", SINGLE_ROW);
		// 实例化查询列集合
		projectionMap = new HashMap<String, String>();
		// 添加查询列
		projectionMap.put(KEY_ID, KEY_ID);
		projectionMap.put(KEY_COLUMN_1_PHONE_NUMBER, KEY_COLUMN_1_PHONE_NUMBER);
		projectionMap.put(KEY_COLUMN_2_CALL_TYPE, KEY_COLUMN_2_CALL_TYPE);
		projectionMap.put(KEY_COLUMN_3_CREATE_TIME, KEY_COLUMN_3_CREATE_TIME);
		projectionMap.put(KEY_COLUMN_4_NAME, KEY_COLUMN_4_NAME);
		projectionMap.put(KEY_COLUMN_5_CONTACTS_ID, KEY_COLUMN_5_CONTACTS_ID);
	}

	public boolean onCreate() {
		//在AndroidManifest.xml定义了该contentprovider，所以最开始，就会跑这个地方
		//最开始就生成了一个数据库，数据库的名字为idtma.db
		dbHelper = DBHelper.getInstance(getContext());
		return true;
	}

	public Uri insert(Uri uri, ContentValues values) {
		// 插入数据，返回行ID
		long rowId = dbHelper.getReadableDatabase().insert(
				DBHelper.TABLE_CALL_LOG, null, values);
		// 如果插入成功返回uri
		if (rowId > 0) {
			Uri empUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(empUri, null);
			return empUri;
		}
		return null;
	}

	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		//此处就可以提现上面static里面对uriMatch定义的价值了
		switch (uriMatcher.match(uri)) {
		// 查询所有
		case ALLROWS:
			qb.setTables(DBHelper.TABLE_CALL_LOG);
			qb.setProjectionMap(projectionMap);
			break;
		// 根据ID查询
		case SINGLE_ROW:
			qb.setTables(DBHelper.TABLE_CALL_LOG);
			qb.setProjectionMap(projectionMap);
			qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Uri error！ " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = qb.query(db, projection, selection, selectionArgs,
				null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		int deleteCount = db.delete(DBHelper.TABLE_CALL_LOG, selection,
				selectionArgs);

		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);
		LwtLog.d(TAG, "删除记录条数：>>>>>>>>>>> " + deleteCount);
		// Return the number of deleted items.
		return deleteCount;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case ALLROWS:
			count = db.update(DBHelper.TABLE_CALL_LOG, values, selection,
					selectionArgs);
			break;
		case SINGLE_ROW:
			long personid = ContentUris.parseId(uri);
			// 防止他输入时String selection, String[] selectionArgs参数为空，这样就会修改表的所有数据了
			String where = TextUtils.isEmpty(selection) ? KEY_ID + "=?"
					: selection + " and " + KEY_ID + "=?";
			String[] params = new String[] { String.valueOf(personid) };
			if (!TextUtils.isEmpty(selection) && selectionArgs != null) {
				params = new String[selectionArgs.length + 1];
				for (int i = 0; i < selectionArgs.length; i++) {
					params[i] = selectionArgs[i];
				}
				params[selectionArgs.length] = String.valueOf(personid);
			}
			count = db.update(DBHelper.TABLE_CALL_LOG, values, where, params);
			break;
		default:
			throw new IllegalArgumentException("Unknow Uri:" + uri);

		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
}
