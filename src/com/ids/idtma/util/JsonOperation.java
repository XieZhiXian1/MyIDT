package com.ids.idtma.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.ids.idtma.entity.MeetingMsgData;

public class JsonOperation {
	public static final int METTING_NOTICE = 0;
	public static final int METTING_REPLY = 1;
	public static final int METTING_LINK = 2;

	public JsonOperation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MeetingMsgData meetingJsonStringParse(String meeting_json_string) {
		try {
			int type = -1;
			String number = "";
			String meetId = "";
			String title = "";
			String desc = "";
			String time = "";
			boolean accept = false;
			String reason = "";
			JSONObject jsonObject = new JSONObject(meeting_json_string);
			type = jsonObject.getInt("type");
			number = jsonObject.getString("number");
			if (type == METTING_NOTICE) {
				title = jsonObject.getString("title");
				desc = jsonObject.getString("desc");
				time = jsonObject.getString("time");
			} else if (type == METTING_REPLY) {
				title = jsonObject.getString("title");
				desc = jsonObject.getString("desc");
				time = jsonObject.getString("time");
				reason= jsonObject.getString("reason");
			} else if (type == METTING_LINK) {
				meetId = jsonObject.getString("meetId");
			}
			return new MeetingMsgData(type, number, meetId, title, desc, time, accept, reason);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
