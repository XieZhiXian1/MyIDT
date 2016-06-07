package com.ids.idtma.jni;

import com.ids.idtma.jni.aidl.GpsData;
import com.ids.idtma.jni.aidl.IDTCallback;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.jni.aidl.SDPAudioInfo;
import com.ids.idtma.jni.aidl.SDPVideoInfo;
import com.ids.idtma.jni.aidl.UserData;

public class IDTNativeApi {
	/**
	 * 初始化
	 */
	static native void init();

	// 释放
	static native void dispose();

	// --------------------------------------------------------------------------------
	// 启动
	// 输入:
	// pcIniFile: INI文件名
	// iMaxCallNum: 最大并发呼叫数
	// cIp: 服务器IP地址
	// iPort: 服务器端口号
	// cNumber: 号码
	// cPwd: 密码
	// iRegFlag: 是否要注册,0不需要,1需要注册
	// CallBack: 回调函数
	// iSigPort: 信令端口 = 10001
	// iMedRtpPort: RTP媒体端口 = 11000
	// iMedTcpPort: TCP媒体监听端口 = 0
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_Start(String pcIniFile, int iMaxCallNum, String pcIp, int iPort, String pcIpGps, int iPortGps,
			String pcNum, String pcPwd, int iRegFlag, IDTCallback callBack, int iSigPort, int iMedRtpPort,
			int iMedTcpPort);

	// --------------------------------------------------------------------------------
	// 退出
	// 输入:
	// 无
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_Exit();

	/**
	 * 用户状态查询
	 * 
	 * @return
	 */
	static native int IDT_Status();

	/**
	 * 修改用户属性
	 * 
	 * @param cIp
	 * @param iPort
	 * @param cNumber
	 * @param cPwd
	 * @return
	 */
	static native int IDT_ModifyProperty(String cIp, int iPort, String cNumber, String cPwd);

	/**
	 * 启动呼出 输入:
	 * 
	 * @param cPeerNum
	 *            : 对方号码
	 * @param SrvType
	 *            : 业务类型
	 * @param pAttr
	 *            : 媒体属性
	 * @param pUsrCtx
	 *            : 用户上下文
	 * @return 返回 -1: 失败 else: 呼叫标识 注意: 如果是组呼: 1.pcPeerNum为组号码
	 *         2.pAttr中,ucAudioSend为1,其余为0
	 */
	static native int IDT_CallMakeOut(String pcPeerNum, int SrvType, MediaAttribute pAttr, long pUsrCtx);// ,
																											// void
																											// *pUsrCtx)

	/**
	 * 呼入应答 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param pAttr
	 *            : 媒体属性
	 * @param pUsrCtx
	 *            : 用户上下文
	 * @return 返回: 0: 成功 -1: 失败
	 */
	static native int IDT_CallAnswer(int ID, MediaAttribute pAttr, long pUsrCtx);

	/**
	 * 呼叫释放 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param pUsrCtx
	 *            : 用户上下文,通常不用这个,但启动主叫后,没有收到主叫应答
	 * @param uiCause
	 *            : 释放原因值
	 * @return 返回: 0: 成功 -1: 失败
	 */
	static native int IDT_CallRel(int ID, long pUsrCtx, int uiCause);

	/**
	 * 通话状态下发送号码 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param cNum
	 *            : 发送的号码,ASC字符串形式,有效值为'0'~'9','*','#','A'~'D',16(FLASH)
	 * @return返回: 0: 成功 -1: 失败
	 */
	static native int IDT_CallSendNum(int ID, String cNum);

	/**
	 * 话权控制 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param bWant
	 *            : 是否期望话权，按下就是true，申请话权; 放开就是false，释放话权
	 * @return: 0: 成功 -1: 失败
	 */
	static native int IDT_CallMicCtrl(int ID, boolean bWant);

	/**
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param pAInfo
	 *            : 语音信息
	 * @param pVInfo
	 *            : 视频信息
	 * @return返回: 0: 成功 -1: 失败
	 */
	static native int IDT_CallModify(int ID, SDPAudioInfo pAInfo, SDPVideoInfo pVInfo);

	/**
	 * 发送语音数据 输入:????????????????????????????????????????????????
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param ucCodec
	 *            : CODEC
	 * @param pucBuf
	 *            : 数据
	 * @param iLen
	 *            : 数据长度
	 * @param uiTs
	 *            : 时戳
	 * @return 返回: 0: 成功 -1: 失败
	 */
	static native int IDT_CallSendAuidoData(int ID, int ucCodec, byte[] pucBuf, int iLen, int uiTs);

	// --------------------------------------------------------------------------------
	// 发送视频数据
	// 输入:
	// ID: IDT的呼叫ID
	// ucCodec: CODEC
	// pucBuf: 数据
	// iLen: 数据长度
	// IFrame: I帧标识
	// uiTs: 时戳
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_CallSendVideoData(int ID, int ucCodec, byte[] pucHdr, int iHdrLen, byte[] pucBuf, int iLen,
			int IFrame, int uiTs, int uiDatalLen, int uiFlg);

	static native int IDT_ForceRel(String pcPeerNum);

	static native int IDT_SendIM(String from, String pcPeerNum, String pBuf, int iLen);

	static native int IDT_SendGpsInfo(long dwDevTimeStamp, // 设备时间戳，秒级
			long dwGPSTimeStamp, // GPS卫星时间戳，秒级
			double fLongitude, // 经度
			double fLatitude, // 纬度
			float forientation, // 方向
			float fvelocity // 速度：千米/小时
	);

	// --------------------------------------------------------------------------------
	// 添加用户
	// 输入:
	// dwSn: 操作序号
	// pUser: 用户信息
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_UAdd(long dwSn, UserData pUser);

	// --------------------------------------------------------------------------------
	// 删除用户
	// 输入:
	// dwSn: 操作序号
	// pucNum: 用户号码
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_UDel(long dwSn, String pucNum);

	// --------------------------------------------------------------------------------
	// 修改用户
	// 输入:
	// dwSn: 操作序号
	// pUser: 用户信息
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_UModify(long dwSn, UserData pUser);

	// --------------------------------------------------------------------------------
	// 查询用户
	// 输入:
	// dwSn: 操作序号
	// pucNum: 用户号码
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	static native int IDT_UQuery(long dwSn, String pucNum);

	// --------------------------------------------------------------------------------
	// 查询组内用户信息
	// 输入:
	// dwSn: 操作序号
	// pucNum: 组号码
	// 返回:
	// 0: 成功
	// -1: 失败
	static native int IDT_GQueryU(long dwSn, String pucNum);

	// 获取IM文件名
	// 输入:
	// dwSn: 消息事务号
	// pcTo: 目的号码
	// dwType: 及时消息类型,IM_TYPE_IMAGE等
	// 返回:
	// 0: 成功
	// -1: 失败
	static native int IDT_IMGetFileName(int dwSn, String pcTo, int dwType);
	// 发送IM消息
	// 输入:
	// dwSn: 消息事务号
	// dwType: 及时消息类型,IM_TYPE_IMAGE等
	// pcTo: 目的号码
	// pcTxt: 文本内容
	// pcFileName: 文件名
	// 返回:
	// 0: 成功
	// -1: 失败
	static native int IDT_IMSend(int dwSn, int dwType, String pcTo, String pcTxt, String pcFileName, String pcSourceFileName);
	
	// 阅读IM消息
	// 输入:
	// dwSn: 消息事务号
	// pucSn: 系统的事务号
	// dwType: 及时消息类型
	// 返回:
	// 0: 成功
	// -1: 失败
	static native int IDT_IMRead(int dwSn, String pucSn, int dwType, String pcTo);

	// 上报自己的GPS信息
	// 输入:
	// pGps: GPS信息
	// 返回:
	// 0: 成功
	// -1: 失败
	static native int IDT_GpsReport(float longitude, float latitude, float speed, float direction, int year,
			int month, int day, int hour, int minute, int second);
	
	// 订阅GPS
	// 输入:
	// pucNum: 用户号码或组号码,第一个字符是"##0"表示取消之前所有订阅,"0"表示所有用户
	// ucSubs: 是否订阅,0取消订阅,1订阅
	// 返回:
	// 0: 成功
	// -1: 失败
	static native int IDT_GpsSubs(String pcNum, int ucSubs);
	
	//将远程的surface发送
	static native int IDT_SetSurface(Object object);

}
