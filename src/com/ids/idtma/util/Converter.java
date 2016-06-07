/*
 * Copyright © 2013 Shenzhen Lenwotion Technology Development Co., Ltd. All rights reserved.
 *
 * Written by Jingzhong Chen
 * 
 * http://www.lenwotion.com/
 */

package com.ids.idtma.util;

public final class Converter {
	public static String byteToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			hex=hex+" ";
			sb.append(hex);
		}
		return sb.toString();
	}
	
    public static String byteToHexWithoutSpace(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
