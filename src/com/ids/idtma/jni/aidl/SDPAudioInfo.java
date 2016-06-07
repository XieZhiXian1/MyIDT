package com.ids.idtma.jni.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class SDPAudioInfo implements Parcelable{
	public SDPAudioInfo() {
		ucSend = 0;
		ucRecv = 0;
		ucCodec = 0xff;
		iBitRate = -1;
		ucTime = 0;
	}

	int ucSend; // 是否发送语音
	int ucRecv; // 是否接收语音
	int ucCodec; // 编码器
	int iBitRate; // 码率
	int ucTime; // 打包时长
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
	public static final Parcelable.Creator<SDPAudioInfo> CREATOR = new Parcelable.Creator<SDPAudioInfo>()
	{

		@Override
		public SDPAudioInfo createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SDPAudioInfo[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new SDPAudioInfo[arg0];
		}

	};

}
