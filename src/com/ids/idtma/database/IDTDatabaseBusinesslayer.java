package com.ids.idtma.database;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IDTDatabaseBusinesslayer {
	private IDTDatabaseHelper helper;
	private SQLiteDatabase db;
	private static IDTDatabaseBusinesslayer idtDatabaseBusinesslayer = null;
	public static final int INSERT_DATA_FAIL = 0;
	public static final int DATA_HAS_EXIST = 1;
	public static final int DATA_HAS_NOT_EXIST = 2;
	public static final int INSERT_DATA_SUCCESS = 3;
	public static final int DELETE_DATA_SUCCESS = 4;
	public static final int DELETE_DATA_FAIL = 5;

	public IDTDatabaseBusinesslayer(Context context) {
		helper = new IDTDatabaseHelper(context, IDTDatabaseHelper.DATABASE_NAME, null,
				IDTDatabaseHelper.DATABASE_VERSION);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
	}

	// 单例获取
	public static IDTDatabaseBusinesslayer getInstance(Context context) {
		if (idtDatabaseBusinesslayer == null) {
			idtDatabaseBusinesslayer = new IDTDatabaseBusinesslayer(context);
		}
		return idtDatabaseBusinesslayer;
	}
    //-----------------------------ip表进行处理----------------------------------------------
	// 向ip表中插入数据
	public int insertIntoIPTable(LoginIP loginIP) {
		int flag = INSERT_DATA_FAIL;
		if (findFromIPTable(loginIP.ip_custom_name, loginIP.ip_address, loginIP.ip_port) == DATA_HAS_EXIST) {
			return DATA_HAS_EXIST;
		}
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL("insert into " + IDTDatabaseHelper.IP_TABLE + " values(?,?, ?, ?)",
					new Object[] { null, loginIP.ip_custom_name, loginIP.ip_address, loginIP.ip_port });
			db.setTransactionSuccessful(); // 设置事务成功完成
			flag = INSERT_DATA_SUCCESS;
		} catch (Exception e) {
			// TODO: handle exception
			flag = INSERT_DATA_FAIL;
		} finally {
			db.endTransaction(); // 结束事务
		}
		return flag;
	}

	// 查询IP表中有没有相似记录
	private int findFromIPTable(String ip_custom_name, String ip_address, int ip_port) {
		String sql = "select * from " + IDTDatabaseHelper.IP_TABLE + " where ip_custom_name= '" + ip_custom_name
				+ "' and ip_address= '" + ip_address + "' and ip_port=" + ip_port;
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			return DATA_HAS_EXIST;
		}
		return DATA_HAS_NOT_EXIST;
	}

	// 查询IP表中的所有数据
	@SuppressWarnings("unused")
	public List<LoginIP> getAllDataFromIPTable() {
		String sql = "select * from " + IDTDatabaseHelper.IP_TABLE;
		List<LoginIP> lIps = new ArrayList<LoginIP>();
		Cursor cursor = db.rawQuery(sql, null);
		int count=cursor.getCount();
		Log.d("login", "数量为:"+count);
		if (cursor.getCount() > 0) {
			// 当cursor中存在数据才往下执行
			while (cursor.moveToNext()) {
				LoginIP loginIP = new LoginIP();
				loginIP.set_id(cursor.getInt(0));
				loginIP.setIp_custom_name(cursor.getString(1));
				loginIP.setIp_address(cursor.getString(2));
				loginIP.setIp_port(cursor.getInt(3));
				lIps.add(loginIP);
			}
		}
		if (lIps.size() > 0) {
			return lIps;
		}
		return null;
	}
	
	public int deleteAIPRecord(int ip_id) {
		int flag = DELETE_DATA_FAIL;
		db.beginTransaction(); // 开始事务
		try {
			String sql = "delete from " + IDTDatabaseHelper.IP_TABLE + " where _id=" + ip_id;
			db.execSQL(sql);
			db.setTransactionSuccessful(); // 设置事务成功完成
			flag = DELETE_DATA_SUCCESS;
		} catch (Exception e) {
			// TODO: handle exception
			flag = DELETE_DATA_FAIL;
		} finally {
			db.endTransaction(); // 结束事务
		}
		return flag;
	}

	// -----------------------------use表进行处理----------------------------------------------
	// 向user表中插入数据
	public int insertIntoUserTable(LoginUser loginUser) {
		int flag = INSERT_DATA_FAIL;
		int _id = findFromUserTable(loginUser.getUserphone());
		if ( _id >= 0) {
			deleteAUserRecord(_id);
		}
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL("insert into " + IDTDatabaseHelper.USER_TABLE + " values(?, ?, ?)",
					new Object[] { null, loginUser.userphone, loginUser.password });
			db.setTransactionSuccessful(); // 设置事务成功完成
			flag = INSERT_DATA_SUCCESS;
		} catch (Exception e) {
			// TODO: handle exception
			flag = INSERT_DATA_FAIL;
		} finally {
			db.endTransaction(); // 结束事务
		}
		return flag;
	}

	// 查询IP表中有没有相似记录
	private int findFromUserTable(String userphone) {
		String sql = "select * from " + IDTDatabaseHelper.USER_TABLE + " where userphone= '" + userphone + "'";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			if (cursor.moveToNext()) {
				return cursor.getInt(0);
			}
		}
		return -1;
	}

	// 查询IP表中的所有数据
	@SuppressWarnings("unused")
	public List<LoginUser> getAllDataFromUserTable() {
		String sql = "select * from " + IDTDatabaseHelper.USER_TABLE;
		List<LoginUser> lIps = new ArrayList<LoginUser>();
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.getCount() > 0) {
			// 当cursor中存在数据才往下执行
			while (cursor.moveToNext()) {
				LoginUser loginUser = new LoginUser();
				loginUser.set_id(cursor.getInt(0));
				loginUser.setPassword(cursor.getString(2));
				loginUser.setUserphone(cursor.getString(1));
				lIps.add(loginUser);
			}
		}
		if (lIps.size() > 0) {
			return lIps;
		}
		return null;
	}
	
	public int deleteAUserRecord(int user_id) {
		int flag = DELETE_DATA_FAIL;
		db.beginTransaction(); // 开始事务
		try {
			String sql = "delete from " + IDTDatabaseHelper.USER_TABLE + " where _id=" + user_id;
			db.execSQL(sql);
			db.setTransactionSuccessful(); // 设置事务成功完成
			flag = DELETE_DATA_SUCCESS;
		} catch (Exception e) {
			// TODO: handle exception
			flag = DELETE_DATA_FAIL;
		} finally {
			db.endTransaction(); // 结束事务
		}
		return flag;
	}
}
