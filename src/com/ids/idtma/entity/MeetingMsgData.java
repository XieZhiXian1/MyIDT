package com.ids.idtma.entity;

import java.io.Serializable;

public class MeetingMsgData implements Serializable{
	
	private static final long serialVersionUID = 1631799804599407095L;
	public int type; // 0 会议预约信息通知 1 成员会议回复结果消息 2 会议开始通知
	public String number; // 会议组号码信息
	public String meetId; // 会场号码

	// 会议预约信息通知 填写
	public String title; // 会议标题
	public String desc; // 会议描述
	public String time; // 会议时间

	// 成员会议回复结果消息 填写
	public boolean accept; // 是否接受
	public String reason; // 拒绝原因

	public MeetingMsgData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MeetingMsgData(int type, String number, String meetId, String title, String desc, String time,
			boolean accept, String reason) {
		super();
		this.type = type;
		this.number = number;
		this.meetId = meetId;
		this.title = title;
		this.desc = desc;
		this.time = time;
		this.accept = accept;
		this.reason = reason;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMeetId() {
		return meetId;
	}

	public void setMeetId(String meetId) {
		this.meetId = meetId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}