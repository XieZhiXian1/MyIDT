package com.ids.idtma.provider;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class ProviderBuinessLayer {
	private Context mContext;

	public ProviderBuinessLayer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ProviderBuinessLayer(Context mContext) {
		super();
		this.mContext = mContext;
	}

	// 给呼叫挂断设置原因值
	// content://com.ids.idtma.provider.SmsProvider/sms/36
	public int update_ui_cause(Uri uri, int uiCause) {
		int updatedRowCount = 0;
		String spliteUri = "";
		try {
			spliteUri = uri.toString().substring(uri.toString().lastIndexOf('/') + 1);
			if (!spliteUri.equals("")) {
				ContentValues updatedValues = new ContentValues();
				String where = SmsProvider.KEY_ID + "=" + spliteUri;
				updatedValues.put(SmsProvider.KEY_COLUMN_14_UI_CAUSE, uiCause);
				updatedRowCount = mContext.getContentResolver().update(SmsProvider.CONTENT_URI, updatedValues, where,
						null);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return updatedRowCount;
	}
	
	public int update_sms_resource_time_length(Uri uri, int time) {
		int updatedRowCount = 0;
		String spliteUri = "";
		try {
			spliteUri = uri.toString().substring(uri.toString().lastIndexOf('/') + 1);
			if (!spliteUri.equals("")) {
				ContentValues updatedValues = new ContentValues();
				String where = SmsProvider.KEY_ID + "=" + spliteUri;
				updatedValues.put(SmsProvider.KEY_COLUMN_9_SMS_RESOURCE_TIME_LENGTH, time);
				updatedRowCount = mContext.getContentResolver().update(SmsProvider.CONTENT_URI, updatedValues, where,
						null);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return updatedRowCount;
	}
}
