package com.ids.idtma.jni.aidl;

import com.ids.idtma.IdtApplication;
import com.ids.idtma.util.LwtLog;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable{
	private static String TAG = UserData.class.getName();
	public String ucNum; // 号码[32]
	public String ucName; // 名字[64]
	public String ucPwd; // 密码[64]
	public int ucType; // 类型
	public int ucAttr; // 属性
	public int ucStatus; // 状态
	public int ucPriority; // 优先级
	public int iConCurrent; // 并发用户数
	public String ucIP; // IP地址,字符串形式[32]
	public int iPort; // 端口号
	public String ucAddr; // 用户地址[128]
	public String ucContact; // 联系方式[128]
	public String ucDesc; // 描述[128]
	public long CTime; // 创建时间
	public long VTime; // 有效时间
	
	void Init()
	{
		LwtLog.d(IdtApplication.WULIN_TAG, "user data Init >>>>> ");
	}
	
	int SetVal(String Num, String Name, String Pwd,
			int Type, int Attr, int Status, int Priority, int ConCurrent,
			String IP,
			int Port,
			String Addr, String Contact, String Desc, long CTime1, long VTime1)
	{
		LwtLog.d(IdtApplication.WULIN_TAG, "user data SetVal() >>>>>>>>>>>>>>"+Num);
		ucNum		= Num;
		ucName		= Name;
		ucPwd		= Pwd;
		ucType		= Type;
		ucAttr		= Attr;
		ucStatus	= Status;
		ucPriority	= Priority;
		iConCurrent	= ConCurrent;
		ucIP		= IP;
		iPort		= Port;
		ucAddr		= Addr;
		ucContact	= Contact;
		ucDesc		= Desc;
		CTime		= CTime1;
		VTime		= VTime1;
		return 0;
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
	
	public static final Parcelable.Creator<UserData> CREATOR = new Parcelable.Creator<UserData>()
	{

		@Override
		public UserData createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UserData[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new UserData[arg0];
		}

	};
}
