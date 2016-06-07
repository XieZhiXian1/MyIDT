package com.ids.idtma.util;

import com.ids.idtma.config.ProjectConfig;

import android.util.Log;

public final class LwtLog {

	@SuppressWarnings("unused")
	public static void d(String tag, String msg) {
		if (ProjectConfig.DEBUG == true) {
			Log.d(tag, msg);
		}
	}

	@SuppressWarnings("unused")
	public static void e(String tag, String msg) {
		if (ProjectConfig.DEBUG == true) {
			Log.d(tag, msg);
		}
	}

	@SuppressWarnings("unused")
	public static void i(String tag, String msg) {
		if (ProjectConfig.DEBUG == true) {
			Log.i(tag, msg);
		}
	}

	@SuppressWarnings("unused")
	public static void e(String tag, String msg, Exception exception) {
		if (ProjectConfig.DEBUG == true) {
			Log.d(tag, msg);
		}
	}
	
	@SuppressWarnings("unused")
	public static void v(String tag, String msg) {
		if (ProjectConfig.DEBUG == true) {
			Log.v(tag, msg);
		}
	}
}
