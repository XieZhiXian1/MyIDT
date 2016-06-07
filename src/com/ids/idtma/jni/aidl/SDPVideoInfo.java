package com.ids.idtma.jni.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class SDPVideoInfo implements Parcelable{
	int ucSend; // 是否发送视频
	int ucRecv; // 是否接收视频
	int ucCodec; // 编码器ID
	int iWidth; // 宽
	int iHeight; // 高
	int iFrameRate; // 帧率
	int iBitRate; // 码率
	int iGop; // I帧间隔

	public SDPVideoInfo() {
		ucSend = 0;
		ucRecv = 0;
		ucCodec = 0xff;
		iWidth = 0;
		iHeight = 0;
		iFrameRate = 0;
		iBitRate = -1;
		iGop = 0;
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
	
	public static final Parcelable.Creator<SDPVideoInfo> CREATOR = new Parcelable.Creator<SDPVideoInfo>()
	{

		@Override
		public SDPVideoInfo createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SDPVideoInfo[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new SDPVideoInfo[arg0];
		}

	};
}
