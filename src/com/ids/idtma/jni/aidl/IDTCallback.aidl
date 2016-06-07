package com.ids.idtma.jni.aidl;
import com.ids.idtma.jni.aidl.GroupData;
import com.ids.idtma.jni.aidl.UserData;
import com.ids.idtma.jni.aidl.UserGroup;
import com.ids.idtma.jni.aidl.GpsData;

interface IDTCallback
{
    //状态指示
	void IDT_StatusInd(int status, int cause);
	//组信息指示
	void IDT_GInfoInd(in UserGroup userInfo);
	//呼出应答
	int IDT_CallPeerAnswer(long pUsrCtx, int type, String num, String name);
	//收到呼入
	int IDT_CallIn(int ID, String pcMyNum, String pcPeerNum, int SrvType,
	    int iAudioRecv, int iAudioSend, int iVideoRecv, int iVideoSend, long pExtInfo);
	//对方或IDT内部释放呼叫
	int IDT_CallRelInd(int ID, long pUsrCtx, int uiCause);
	//话权指示
	int IDT_CallMicInd(long pUsrCtx, int uiInd);
	//讲话方提示
	void IDT_CallTalkingIDInd(long pUsrCtx, String num, String name);
	//通话状态下收到对方发送的号码
	int IDT_CallRecvNum(long pUsrCtx, char cNum);
	//收到视频数据
	int IDT_CallRecvVideoData(long pUsrCtx, int ucCodec,in byte[] ucBuf, int IFrame, int uiTs);
	
	int IDT_RecvPassThrouth(String myNum, String peerNum, String pBuf, int iLen);
	
	//收到信息
	int IDT_RecvIM(String pcFrom, String pcTo, String pBuf, int iLen);
	
	void IDT_UOptRsp(int dwOptCode, int dwSn, int wRes, in UserData userData);
	
	void IDT_GOptRsp(int dwOptCode, int dwSn, int wRes, in GroupData groupData);     
    //获取IM文件名
	int IDT_IMGetFileNameRsp(int dwSn,String pcFileName);
    //收到IM消息
	int IDT_IMRecv(String pucSn, int dwType, String pcFrom, String pcTo,String pcOriTo,String pcTxt, String pcFileName, String pcSourceFileName);
	// IM状态指示
	int IDT_IMStatusInd(int dwSn, String pucSn, int dwType, int ucStatus);
	//收到用户的gps信息
	void IDT_GpsRecInd(String ucNum, int ucStatus, float longitude, float latitude, float speed, float direction, int year,
			int month, int day, int hour, int minute, int second);
	//获取用户状态
	void IDT_GUStatusInd(int iType, String ucNum, int iStatus, int iSrvType, int iCallStatue);
	//会议的时候，视频和语音是否
	int IDT_CallSetVideoCodec(int ucRecv,int ucSend);
	// 码流统计
	void IDT_CallMediaStats(long pUsrCtx, int ucType, int uiRxBytes, int uiRxUsrBytes, int uiRxCount, int uiRxUserCount,
				int uiTxBytes, int uiTxUsrBytes, int uiTxCount ,int uiTxUserCount);
}