package com.ids.proxy;

import com.ids.proxy.idt.IDTApiProxy;

/**
 * 业务API的管理类
 * 
 * @author
 *
 */
public class IDSApiProxyMgr {
	// 用一个接口接收一个实现接口的类
	private static IDSApiProxy g_curProxy = new IDTApiProxy();
	public static IDSApiProxy getCurProxy() {
		return g_curProxy;
	}

	private IDSApiProxyMgr() {

	}

}
