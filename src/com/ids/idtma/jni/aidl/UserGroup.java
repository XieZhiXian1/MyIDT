package com.ids.idtma.jni.aidl;

import com.ids.idtma.IdtApplication;
import com.ids.idtma.util.LwtLog;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户所在组信息
 * 
 * @author Administrator
 * 
 */
public class UserGroup implements Parcelable {
	private static String TAG = UserGroup.class.getName();
	public static final int USER_MAX_GROUP = 16;
	public int iMemberNum;
	GroupMember member[]; // 成员16

	void Init() {
		LwtLog.d(IdtApplication.WULIN_TAG, "user group Init >>>>> ");
		iMemberNum = 16;
		member = new GroupMember[16];
		int i;
		for (i = 0; i < 16; i++) {
			member[i] = new GroupMember();
		}
	}

	void SetMemberNum(int iNum) {
		LwtLog.d(IdtApplication.WULIN_TAG, "用户组user group SetMemberNum() >>>>>>> " + iNum);
		iMemberNum = iNum;
	}

	int SetVal(int index, int iPrio, int iType, String sNum, String sName, int iStatus) {
		LwtLog.d(IdtApplication.WULIN_TAG, "用户组user group SetVal() >>>>>>>>>>>>>>" +"编号:"+ index +"   优先级"+ iPrio +"   是用户还是组:"+ iType +"号码:"+sNum
				+ " 名字:"+sName+"结束");

		if (index < 0 || index >= iMemberNum)
			return -1;
        //ucPrio; // 优先级
        //ucType; // 是用户还是组,1用户,2组 ,3 调度台
        //ucNum; // 号码长度32字节
        //ucName; // 名字长度64字节
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

	public GroupMember[] getMember() {
		return member;
	}

	public void setMember(GroupMember[] member) {
		this.member = member;
	}

	public static final Parcelable.Creator<UserGroup> CREATOR = new Parcelable.Creator<UserGroup>() {
		public UserGroup createFromParcel(Parcel in) {
			return null;
		}

		public UserGroup[] newArray(int size) {
			return new UserGroup[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}
}
