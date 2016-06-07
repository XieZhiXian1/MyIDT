/*
 * Copyright (C) 2013 Shenzhen Lenwotion Technology Development Co., Ltd. All rights reserved.
 *
 * Written by Jingzhong Chen
 * 
 * http://www.lenwotion.com/
 */

package com.ids.idtma.util;

import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppVersion {
	private PackageInfo pinfo;
	
	public AppVersion(ContextWrapper contextWrapper){
		PackageManager pm = contextWrapper.getPackageManager();
		try {
			pinfo = pm.getPackageInfo(contextWrapper.getPackageName(), PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getVersionName(){
		return pinfo.versionName;
	}
	
	public int getVersionCode(){
			return pinfo.versionCode;
	}

}
