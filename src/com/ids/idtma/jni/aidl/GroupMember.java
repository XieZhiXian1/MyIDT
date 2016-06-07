package com.ids.idtma.jni.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 成员信息
 * 
 * @author Administrator
 * 
 */
public class GroupMember implements Parcelable {
	/**
	 * 一个组中最多多少个用户
	 */
	public static final int GROUP_MAX_MEMBER = 128;
	
	public static final int UCTYPE_GROUP = 2;
	public static final int UCTYPE_USER = 1;
	public static final int UCTYPE_USER_DIS = 3;
	
	int ucPrio; // 优先级
	int ucType; // 是用户还是组,1用户,2组 ,3 调度台
	String ucNum; // 号码长度32字节
	String ucName; // 名字长度64字节
	private boolean locked;
	private boolean checked;
	public boolean isOnline = false;
	private boolean focused = false;
	
	public String toString()
	{
		return ucName;
	}

	public GroupMember() {

	}

	public int getUcPrio() {
		return ucPrio;
	}

	public void setUcPrio(int ucPrio) {
		this.ucPrio = ucPrio;
	}

	public int getUcType() {
		return ucType;
	}

	public void setUcType(int ucType) {
		this.ucType = ucType;
	}

	public String getUcNum() {
		return ucNum;
	}

	public void setUcNum(String ucNum) {
		this.ucNum = ucNum;
	}

	public String getUcName() {
		return ucName;
	}

	public void setUcName(String ucName) {
		this.ucName = ucName;
	}

	public boolean getLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean getChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean getFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public static final Parcelable.Creator<GroupMember> CREATOR = new Parcelable.Creator<GroupMember>() {
		public GroupMember createFromParcel(Parcel in) {
			return new GroupMember(in);
		}

		public GroupMember[] newArray(int size) {
			return new GroupMember[size];
		}
	};

	private GroupMember(Parcel in) {
		ucNum = in.readString();
		ucName = in.readString();
		locked = in.readByte() != 0;
		checked = in.readByte() != 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(ucNum);
		dest.writeString(ucName);
		dest.writeByte((byte) (locked ? 1 : 0));
		dest.writeByte((byte) (checked ? 1 : 0));
	}

}
