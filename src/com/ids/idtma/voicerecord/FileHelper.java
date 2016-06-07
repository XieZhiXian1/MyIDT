package com.ids.idtma.voicerecord;
import android.content.*;
import android.os.*;
import android.util.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import com.ids.idtma.util.LwtLog;

import org.apache.http.util.EncodingUtils;

/**
 * 文件下载工具类
 * */
public class FileHelper {

	public static File DownloadFromUrlToData(String serverUrl, String fileName,
			Context context) {
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
			urlCon.setConnectTimeout(10000);
			urlCon.setReadTimeout(10000);
			
			
			long startTime = System.currentTimeMillis();
			LwtLog.d("FileDownloader", "download begining");
			LwtLog.d("FileDownloader", "download url:" + url);
			LwtLog.d("FileDownloader", "downloaded file name:" + fileName);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			/*
			 * * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(1024);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			// /writeFileData("r"+fileName,baf.toByteArray(),context);
			createSDDir("juyouim");
			File file = new File(Environment.getExternalStorageDirectory()
					.getCanonicalFile() + "/juyouim/" + fileName);
			FileOutputStream fout = new FileOutputStream(file);
			fout.write(baf.toByteArray());
			fout.close();
			// DowloadList.downloadList.put(fileName.trim(), "1");
			return file;
		} catch (IOException e) {

			LwtLog.d("ImageManager", "Error: " + e);
			return null;
		}
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param dirName
	 * @return
	 */
	public static File createSDDir(String dirName) {
		try {
			File dir = new File(Environment.getExternalStorageDirectory()
					.getCanonicalFile() + "/" + dirName);
			if (!dir.exists()) {
				dir.mkdir();
			}
			return dir;
		} catch (Exception ex) {
			return null;
		}
	}

	public File createSDFile(String fileName) throws IOException {
		File file = new File(Environment.getExternalStorageDirectory()
				.getCanonicalFile() + "/" + fileName);
		file.createNewFile();
		return file;
	}

	public static String getFileName() {

		GregorianCalendar currentDay = new GregorianCalendar();
		int year = currentDay.get(Calendar.YEAR);
		int month = currentDay.get(Calendar.MONTH);
		int today = currentDay.get(Calendar.DAY_OF_MONTH);
		int hour = currentDay.get(Calendar.HOUR_OF_DAY);
		int min = currentDay.get(Calendar.MINUTE);
		int second = currentDay.get(Calendar.SECOND);
		int ms = currentDay.get(Calendar.MILLISECOND);
		String s = "";
		s = s + new Integer(year).toString() + new Integer(month).toString()
				+ new Integer(today).toString();
		s = s + new Integer(hour).toString() + new Integer(min).toString()
				+ new Integer(second).toString();
		s = s + new Integer(ms).toString();
		return s;
	}

	public static void writeFileData(String fileName, byte[] bytes,
			Context context) {
		try {
			FileOutputStream fout = context.openFileOutput(fileName,
					context.MODE_PRIVATE);
			fout.write(bytes);
			fout.close();
			// PrintWriter pw= new PrintWriter(fout);
			// pw.write(new String(bytes,0,bytes.length,"utf-8"));
			// pw.close();
			// fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
