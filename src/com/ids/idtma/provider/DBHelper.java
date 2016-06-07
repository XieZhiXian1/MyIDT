package com.ids.idtma.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ids.idtma.R;
import com.ids.idtma.util.LwtLog;

/**
 * DBHelper 是一个辅助类 Implementing an SQLiteOpenHelper
 * 
 * @author dengyong
 * 
 */
public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = "wulin";

	private static final String DATABASE_NAME = "idtma_provider.db";

	private static final int DATABASE_VERSION = 1;

	private static DBHelper dbHelper;
	public static final String TABLE_SMS = "sms";
	public static final String TABLE_SMS_CONVERSATION = "sms_conversation";
	public static final String TABLE_CONTACTS = "contacts";
	public static final String TABLE_CALL_LOG = "call_log";

	private Context context;

	private DBHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = ctx;
	}

	//单例创建数据库
	public static DBHelper getInstance(Context ctx) {
		if (dbHelper == null) {
			dbHelper = new DBHelper(ctx);
		}
		return dbHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LwtLog.d(TAG, ">>创建数据库 onCreate.");
		try {
			db.beginTransaction();
			executeSQLs(db, R.raw.create_database);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LwtLog.e(TAG, e.toString(), e);
			throw new RuntimeException(
					"Database create error! Please contact the support or developer.",
					e);
		} finally {
			db.endTransaction();
		}
	}
    
	//执行生成数据库sql语句
	private void executeSQLs(SQLiteDatabase db, int sqlResourceId)
			throws IOException {
		InputStream tmpIS = context.getResources().openRawResource(
				sqlResourceId);
		InputStreamReader tmpReader = new InputStreamReader(tmpIS);
		BufferedReader tmpBuf = new BufferedReader(tmpReader);

		StringBuffer sql = new StringBuffer();
		String tmpStr = null;
		while ((tmpStr = tmpBuf.readLine()) != null) {
			if (tmpStr.startsWith("//")) {
				continue;
			}
			if (tmpStr.startsWith("--")) {
				continue;
			}
			if (tmpStr.startsWith("#")) {
				continue;
			}
			if (tmpStr.endsWith("/")) {
				db.execSQL(sql.toString());
				LwtLog.d(TAG, "执行SQL脚本 >>>>>>>>>" + sql.toString());
				sql = new StringBuffer();
			} else {
				sql.append(tmpStr);
				sql.append('\n');
			}
		}
		tmpBuf.close();
		tmpReader.close();
		tmpIS.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			LwtLog.d(TAG, "数据库升级 >> onUpgrade. Upgrading database from version "
					+ oldVersion + " to " + newVersion);
			// To-do
		} catch (Exception e) {
			LwtLog.e(TAG, "数据库升级失败>>>>>>>>>onUpgrade", e);
			throw new RuntimeException(
					"Database upgrade error! Please contact the support or developer.",
					e);
		} finally {
			db.endTransaction();// 由事务的标志决定是提交事务，还是回滚事务
		}
	}

	public Cursor query(String sql, String[] selectionArgs) {
		return dbHelper.getWritableDatabase().rawQuery(sql, selectionArgs);
	}
}
