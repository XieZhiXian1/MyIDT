package com.ids.proxy;

import com.ids.idtma.jni.aidl.MediaAttribute;
import android.app.Activity;
import android.content.Context;
import android.view.SurfaceView;

/**
 * 中继代理，使用这个接口，对两套API进行适配
 * 
 * @author
 *
 */
public interface IDSApiProxy {
	void loadLibrary(Activity context);

	void init(Activity context);

	// 拨打
	int CallMakeOut(String pcPeerNum, int SrvType, MediaAttribute pAttr, long pUsrCtx);

	// 接听以后给so应答
	int CallAnswer(int ID, MediaAttribute pAttr, long pUsrCtx);

	// 告诉so库，你要取消当前通话
	int CallRel(int ID, long pUsrCtx, int uiCause);

	int CallMicCtrl(int ID, boolean bWant);

	int SendIM(String from, String pcPeerNum, String pBuf, int iLen);

	int CallSendVideoData(int ID, int ucCodec, byte[] pucHdr, int iHdrLen, byte[] pucBuf, int iLen, int IFrame,
			int uiTs, int uiDatalLen, int uiFlg);

	int iMGetFileName(int dwSn, String pcTo, int dwType);

	int iMSend(int dwSn, int dwType, String pcTo, String pcTxt, String pcFileName, String pcSourceFileName);

	int iMRead(int dwSn, String pucSn, int dwType, String pcTo);
	
	int GpsReport(float longitude, float latitude, float speed, float direction, int year,
			int month, int day, int hour, int minute, int second);
	
	int GpsSubs(String pcNum, int ucSubs);
	
	int SetSurface(Object object);
	
	Class getVideoCallActivity();

	int Exit();

	void unloadLibrary(Context context);

	void lockGroup(String tel);
}
