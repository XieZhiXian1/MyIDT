package com.ids.idtma.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


//管理SharedPreferences的类
public class SharedPreferencesUtil {

	public static void setStringPreferences(Context context, String key,
			String value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getStringPreference(Context context, String key,
			String defaultValue) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		return sharedPreferences.getString(key, defaultValue);
	}

	public static void setBooleanPreferences(Context context, String key,
			Boolean value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static Boolean getBooleanPreference(Context context, String key,
			Boolean defaultValue) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		return sharedPreferences.getBoolean(key, defaultValue);
	}

	public static void setLongPreferences(Context context, String key,
			long value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sharedPreferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public static long getLongPreference(Context context, String key,
			long defaultValue) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		return sharedPreferences.getLong(key, defaultValue);
	}

	public static void setIntPreferences(Context context, String key, int value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static int getIntPreference(Context context, String key,
			int defaultValue) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		return sharedPreferences.getInt(key, defaultValue);
	}

	public static void removePreference(Context context, String key) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sharedPreferences.edit();
		editor.remove(key);
		editor.commit();
	}

}
