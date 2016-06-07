package com.ids.idtma.entity;

import android.content.Intent;
import android.net.Uri;
import android.widget.Chronometer;

//呼叫类
public class CallEntity {
    //当前呼叫唯一编码，由so库端产生
	int callid;
	String caller;// 主叫号码
	String callee;// 被叫号码
	private CallType type;
	private CallStatus status;
	Intent intent;
	Uri uri;
	//计时器控件
	Chronometer chronometer;
	
	public CallEntity(int callid, CallType type) {
		this.callid = callid;
		this.type = type;
	}

	/**
	 * AUDIO_CALL-语音, VEDIO_CALL-视频, GROUP_CALL-组呼
	 * 枚举类
	 */
	public static enum CallType {
		AUDIO_CALL, VEDIO_CALL, GROUP_CALL
	}
	
	/**
	 * 
	 * INCOMING-来电, CALLING-拨号, ANSWER-接听通话中
	 *
	 */
	public static enum CallStatus {
		INCOMING, CALLING, ANSWER
	}

	public int getCallid() {
		return callid;
	}

	public void setCallid(int callid) {
		this.callid = callid;
	}

	public CallType getType() {
		return type;
	}

	public void setType(CallType type) {
		this.type = type;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getCallee() {
		return callee;
	}

	public void setCallee(String callee) {
		this.callee = callee;
	}

	public CallStatus getStatus() {
		return status;
	}

	public void setStatus(CallStatus status) {
		this.status = status;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public Chronometer getChronometer() {
		return chronometer;
	}
    
	public void setChronometer(Chronometer chronometer) {
		this.chronometer = chronometer;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}
}
