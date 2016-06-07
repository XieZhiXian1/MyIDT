package com.ids.idtma.jni.aidl;

import com.ids.idtma.IdtApplication;
import com.ids.idtma.util.LwtLog;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupData implements Parcelable {
	private static String TAG = GroupData.class.getName();
	String ucNum; // 组号码[32]
	String ucName; // 组名字[64]
	int ucPriority; // 优先级
	int dwNum; // 用户个数
	GroupMember member[]; // 组成员128

	void Init() {
		LwtLog.d(IdtApplication.WULIN_TAG, "组成员group data Init >>>>> ");
		member = new GroupMember[128];// ????????
		int i;
		for (i = 0; i < 128; i++) {
			member[i] = new GroupMember();
		}
	}

	int SetData(String Num, String Name, int Prio, int MemNum) {
		ucNum = Num;
		ucName = Name;
		ucPriority = Prio;
		dwNum = MemNum;
		return 0;
	}

	int SetMember(int index, int iPrio, int iType, String sNum, String sName, int iStatus) {
		LwtLog.d(IdtApplication.WULIN_TAG, "组成员group data SetVal() >>>>>>>>>>>>>>" + "编号:"+index + "优先级:"+iPrio + "组还是成员:"+iType + "号码:"+sNum
				+ "名字:"+sName);
		if (index < 0 || index >= dwNum)
			return -1;
		member[index].ucPrio = iPrio;
		member[index].ucType = iType;
		member[index].ucNum = sNum;
		member[index].ucName = sName;
		if(iStatus==0){
			//离线
			member[index].isOnline = false;
		}else if(iStatus==1){
			//在线
			member[index].isOnline = true;
		}
		return 0;
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

	public int getUcPriority() {
		return ucPriority;
	}

	public void setUcPriority(int ucPriority) {
		this.ucPriority = ucPriority;
	}

	public int getDwNum() {
		return dwNum;
	}

	public void setDwNum(int dwNum) {
		this.dwNum = dwNum;
	}

	public GroupMember[] getMember() {
		return member;
	}

	public void setMember(GroupMember[] member) {
		this.member = member;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}

	public static final Parcelable.Creator<GroupData> CREATOR = new Parcelable.Creator<GroupData>() {

		@Override
		public GroupData createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GroupData[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new GroupData[arg0];
		}

	};
}
