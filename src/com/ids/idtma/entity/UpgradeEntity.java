package com.ids.idtma.entity;

public class UpgradeEntity {
	private int versionCode;
	private String versionName;
	private String versionDescription;
	private String apkFile;

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getVersionDescription() {
		return versionDescription;
	}

	public void setVersionDescription(String versionDescription) {
		this.versionDescription = versionDescription;
	}

	public String getApkFile() {
		return apkFile;
	}

	public void setApkFile(String apkFile) {
		this.apkFile = apkFile;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("apkFile:").append(apkFile);
		sb.append("versionCode:").append(versionCode);
		sb.append("versionName:").append(versionName);
		sb.append("versionDescription:").append(versionDescription);
		return sb.toString();
	}

}
