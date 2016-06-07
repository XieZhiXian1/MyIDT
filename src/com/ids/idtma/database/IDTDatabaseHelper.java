package com.ids.idtma.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class IDTDatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "idtma_custom.db";
	public static final int DATABASE_VERSION = 1;
	public static final String USER_TABLE = "user_table";
	public static final String IP_TABLE = "ip_table";

	public IDTDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// 创建用户表：唯一码、用户名、密码
		String sql = "create table "+USER_TABLE+" (_id integer primary key autoincrement,userphone varchar(20) unique,password varchar(20))";
		db.execSQL(sql);
		// 创建ip表：唯一码、ip自定义名称、IP地址、ip端口
		sql = "create table "+IP_TABLE+" (_id integer primary key autoincrement,ip_custom_name varchar(20),ip_address varchar(20),ip_port integer)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
