package com.ids.idtma.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.LinkedList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.baidu.location.b.f;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.util.LwtLog;

import android.util.Log;

public class FtpUtil {
	/**
	 * 服务器名.
	 */
	private String hostName;

	/**
	 * 端口号
	 */
	private int serverPort;

	/**
	 * 用户名.
	 */
	private String userName;

	/**
	 * 密码.
	 */
	private String password;

	/**
	 * FTP连接.
	 */
	private FTPClient ftpClient;

	public FtpUtil(String hostName, int serverPort, String userName, String password) {
		// this.hostName = "192.168.1.105";
		//// this.hostName = "124.160.11.21";
		//// this.hostName = "192.168.2.11";
		// this.serverPort = 21;
		// this.userName = "test";
		// this.password = "test";
		this.hostName = hostName;
		this.serverPort = serverPort;
		this.userName = userName;
		this.password = password;
		this.ftpClient = new FTPClient();
	}

	// -------------------------------------------------------文件上传方法------------------------------------------------

	/**
	 * 上传单个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @param remotePath
	 *            FTP目录
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void uploadSingleFile(File singleFile, String remotePath, String remote_file_name,UploadProgressListener listener)
			throws IOException {

		// 上传之前初始化
		this.uploadBeforeOperate(remotePath, listener);

		boolean flag;
		flag = uploadingSingle(singleFile, remote_file_name,listener);
		if (flag) {
			listener.onUploadProgress(FtpBuinessLayer.FTP_UPLOAD_SUCCESS, 0, singleFile);
		} else {
			listener.onUploadProgress(FtpBuinessLayer.FTP_UPLOAD_FAIL, 0, singleFile);
		}

		// 上传完成之后关闭连接
		this.uploadAfterOperate(listener);
	}
	
	/**
	 * 上传单个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @param remotePath
	 *            FTP目录
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void uploadSingleFile(File singleFile, String remotePath,UploadProgressListener listener)
			throws IOException {

		// 上传之前初始化
		this.uploadBeforeOperate(remotePath, listener);

		boolean flag;
		flag = uploadingSingle(singleFile, listener);
		if (flag) {
			listener.onUploadProgress(FtpBuinessLayer.FTP_UPLOAD_SUCCESS, 0, singleFile);
		} else {
			listener.onUploadProgress(FtpBuinessLayer.FTP_UPLOAD_FAIL, 0, singleFile);
		}

		// 上传完成之后关闭连接
		this.uploadAfterOperate(listener);
	}

	/**
	 * 上传多个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @param remotePath
	 *            FTP目录
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void uploadMultiFile(LinkedList<File> fileList, String remotePath, UploadProgressListener listener)
			throws IOException {

		// 上传之前初始化
		this.uploadBeforeOperate(remotePath, listener);

		boolean flag;

		for (File singleFile : fileList) {
			flag = uploadingSingle(singleFile, listener);
			if (flag) {
				listener.onUploadProgress(FtpBuinessLayer.FTP_UPLOAD_SUCCESS, 0, singleFile);
			} else {
				listener.onUploadProgress(FtpBuinessLayer.FTP_UPLOAD_FAIL, 0, singleFile);
			}
		}

		// 上传完成之后关闭连接
		this.uploadAfterOperate(listener);
	}

	/**
	 * 上传单个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @return true上传成功, false上传失败
	 * @throws IOException
	 */
	private boolean uploadingSingle(File localFile, String remote_file_name,UploadProgressListener listener) throws IOException {
		
		boolean flag = true;
		// 不带进度的方式
		// // 创建输入流
		// InputStream inputStream = new FileInputStream(localFile);
		// // 上传单个文件
		// flag = ftpClient.storeFile(localFile.getName(), inputStream);
		// // 关闭文件流
		// inputStream.close();

		// 带有进度的方式
		BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(localFile));
		ProgressInputStream progressInput = new ProgressInputStream(buffIn, listener, localFile);
		flag = ftpClient.storeFile(remote_file_name, progressInput);
		buffIn.close();

		return flag;
	}
	
	
	/**
	 * 上传单个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @return true上传成功, false上传失败
	 * @throws IOException
	 */
	private boolean uploadingSingle(File localFile,UploadProgressListener listener) throws IOException {
		boolean flag = true;
		// 不带进度的方式
		// // 创建输入流
		// InputStream inputStream = new FileInputStream(localFile);
		// // 上传单个文件
		// flag = ftpClient.storeFile(localFile.getName(), inputStream);
		// // 关闭文件流
		// inputStream.close();

		// 带有进度的方式
		BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(localFile));
		ProgressInputStream progressInput = new ProgressInputStream(buffIn, listener, localFile);
		flag = ftpClient.storeFile(localFile.getName(), progressInput);
		buffIn.close();

		return flag;
	}

	/**
	 * 上传文件之前初始化相关参数
	 * 
	 * @param remotePath
	 *            FTP目录
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	private void uploadBeforeOperate(String remotePath, UploadProgressListener listener) throws IOException {

		// 打开FTP服务
		try {
			this.openConnect();
			listener.onUploadProgress(FtpBuinessLayer.FTP_CONNECT_SUCCESSS, 0, null);
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onUploadProgress(FtpBuinessLayer.FTP_CONNECT_FAIL, 0, null);
			return;
		}
		// 设置模式
		Boolean boolean1 = ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
		LwtLog.d("wulin", "setFileTransferMode---------" + boolean1);
		// FTP下创建文件夹
		LwtLog.d("wulin", "remotePath---------" + remotePath);
		String[] directorys = remotePath.split("\\/");
		String directory_one = directorys[1];
		if (!ftpClient.changeWorkingDirectory("/" + directory_one)) {
			Boolean boolean2 = ftpClient.makeDirectory("/" + directory_one);
			LwtLog.d("wulin", "一级目录创建情况:" + boolean2);
		}
		if (!ftpClient.changeWorkingDirectory(remotePath)) {
			Boolean boolean3 = ftpClient.makeDirectory(remotePath);
			LwtLog.d("wulin", "二级目录创建情况:" + boolean3);
		}
		// 改变FTP目录
		Boolean boolean4 = ftpClient.changeWorkingDirectory(remotePath);
		LwtLog.d("wulin", "changeWorkingDirectory---------" + boolean4);
		// 上传单个文件

	}

	/**
	 * 上传完成之后关闭连接
	 * 
	 * @param listener
	 * @throws IOException
	 */
	private void uploadAfterOperate(UploadProgressListener listener) throws IOException {
		this.closeConnect();
		listener.onUploadProgress(FtpBuinessLayer.FTP_DISCONNECT_SUCCESS, 0, null);
	}

	// -------------------------------------------------------文件下载方法------------------------------------------------

	/**
	 * 
	 * @param serverPath
	 *            Ftp目录及文件路径
	 * @param localPath
	 *            本地目录
	 * @param fileName
	 *            下载之后的文件名称
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void downloadSingleFile(String server_file_String_name, String server_file_path, String localPath,
			String fileName, DownLoadProgressListener listener) throws Exception {

		// 打开FTP服务
		try {
			this.openConnect();
			Boolean boolean4 = ftpClient.changeWorkingDirectory(server_file_path);
			LwtLog.d("wulin", "changeWorkingDirectory---------" + boolean4);
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_CONNECT_SUCCESSS, 0, null,"");
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_CONNECT_FAIL, 0, null,"");
			return;
		}
		// 先判断服务器文件是否存在
		FTPFile[] files = ftpClient.listFiles();
		if (files.length == 0) {
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_FILE_NOTEXISTS, 0, null,"");
			return;
		}

		// 创建本地文件夹
		File mkFile = new File(localPath);
		if (!mkFile.exists()) {
			mkFile.mkdirs();
		}

		localPath = localPath + fileName;
		for (FTPFile file : files) {
			if (file.getName().equals(fileName)) {
				// 根据绝对路径初始化文件
				File localFile = new File(localPath);
				// 输出流
				OutputStream outputStream = new FileOutputStream(localFile);
				// 下载文件
				ftpClient.retrieveFile(file.getName(), outputStream);
				outputStream.flush();
				// 关闭流
				outputStream.close();
				listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_SUCCESS, 0, null,localPath);
			}
		}
		// 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
		if (ftpClient.completePendingCommand()) {
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_SUCCESS, 0, new File(localPath),"");
		} else {
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_FAIL, 0, null,"");
		}
		// 下载完成之后关闭连接
		this.closeConnect();
		listener.onDownLoadProgress(FtpBuinessLayer.FTP_DISCONNECT_SUCCESS, 0, null,"");
		return;
	}
	
	// -------------------------------------------------------文件下载带进度条------------------------------------------------

	/**
	 * 
	 * @param serverPath
	 *            Ftp目录及文件路径
	 * @param localPath
	 *            本地目录
	 * @param fileName
	 *            下载之后的文件名称
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void downloadSingleFile(String server_file_String_name, String server_file_path, String localPath,
			String fileName, DownLoadProgressListener listener, int mode) throws Exception {

		// 打开FTP服务
		try {
			this.openConnect();
			Boolean boolean4 = ftpClient.changeWorkingDirectory(server_file_path);
			LwtLog.d("wulin", "changeWorkingDirectory---------" + boolean4);
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_CONNECT_SUCCESSS, 0, null, "");
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_CONNECT_FAIL, 0, null, "");
			return;
		}
		// 先判断服务器文件是否存在
		FTPFile[] files = ftpClient.listFiles();
		if (files.length == 0) {
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_FILE_NOTEXISTS, 0, null, "");
			return;
		}

		// 创建本地文件夹
		File mkFile = new File(localPath);
		if (!mkFile.exists()) {
			mkFile.mkdirs();
		}

		localPath = localPath + fileName;
		for (FTPFile file : files) {
			if (file.getName().equals(fileName)) {
				// 根据绝对路径初始化文件
				File localFile = new File(localPath);
				long lRemoteSize = file.getSize();
				// 输出流
				OutputStream outputStream = new FileOutputStream(localFile);
				// 下载文件
				InputStream inputStream = ftpClient.retrieveFileStream(file.getName());
				byte[] bytes = new byte[2048];
				long step = lRemoteSize / 100;
				long process = 0;
				long localSize = 0L;
				int c;
				while ((c = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, c);
					localSize += c;
					long nowProcess = localSize / step;
					if (nowProcess > process) {
						process = nowProcess;
						// 更新文件下载进度,值存放在process变量中
						if (process % 2 == 0){
						 //Log.d("download", "下载进度:"+process);
						 listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_LOADING, process ,null, "");
						}
					}
				}
				outputStream.flush();
				// 关闭流
				outputStream.close();
				inputStream.close();
				listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_SUCCESS, 0, null, localPath);
			}
		}
		// 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
		if (ftpClient.completePendingCommand()) {
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_SUCCESS, 0, new File(localPath), "");
		} else {
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_DOWN_FAIL, 0, null, "");
		}
		// 下载完成之后关闭连接
		this.closeConnect();
		listener.onDownLoadProgress(FtpBuinessLayer.FTP_DISCONNECT_SUCCESS, 0, null, "");
		return;
	}

	// -------------------------------------------------------文件删除方法------------------------------------------------

	/**
	 * 删除Ftp下的文件.
	 * 
	 * @param serverPath
	 *            Ftp目录及文件路径
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void deleteSingleFile(String serverPath, DeleteFileProgressListener listener) throws Exception {

		// 打开FTP服务
		try {
			this.openConnect();
			listener.onDeleteProgress(FtpBuinessLayer.FTP_CONNECT_SUCCESSS);
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onDeleteProgress(FtpBuinessLayer.FTP_CONNECT_FAIL);
			return;
		}

		// 先判断服务器文件是否存在
		FTPFile[] files = ftpClient.listFiles(serverPath);
		if (files.length == 0) {
			listener.onDeleteProgress(FtpBuinessLayer.FTP_FILE_NOTEXISTS);
			return;
		}

		// 进行删除操作
		boolean flag = true;
		flag = ftpClient.deleteFile(serverPath);
		if (flag) {
			listener.onDeleteProgress(FtpBuinessLayer.FTP_DELETEFILE_SUCCESS);
		} else {
			listener.onDeleteProgress(FtpBuinessLayer.FTP_DELETEFILE_FAIL);
		}

		// 删除完成之后关闭连接
		this.closeConnect();
		listener.onDeleteProgress(FtpBuinessLayer.FTP_DISCONNECT_SUCCESS);

		return;
	}

	// -------------------------------------------------------打开关闭连接------------------------------------------------

	/**
	 * 打开FTP服务.
	 * 
	 * @throws IOException
	 */
	public void openConnect() throws IOException {
		// 中文转码
		ftpClient.setControlEncoding("UTF-8");
		int reply; // 服务器响应值
		// 连接至服务器
		ftpClient.connect(hostName, serverPort);
		//设置每次读取文件流时缓存数组的大小
		ftpClient.setBufferSize(1024 * 1024);
		// 获取响应值
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			// 断开连接
			ftpClient.disconnect();
			throw new IOException("connect fail: " + reply);
		}
		// 登录到服务器
		ftpClient.login(userName, password);
		// 获取响应值
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			// 断开连接
			ftpClient.disconnect();
			throw new IOException("connect fail: " + reply);
		} else {
			// 获取登录信息
			FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
			config.setServerLanguageCode("zh");
			ftpClient.configure(config);
			// 使用被动模式设为默认
			ftpClient.enterLocalPassiveMode();
			// ftpClient.enterLocalActiveMode();
			// 二进制文件支持
			ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
		}
	}

	/**
	 * 关闭FTP服务.
	 * 
	 * @throws IOException
	 */
	public void closeConnect() throws IOException {
		if (ftpClient != null) {
			// 退出FTP
			ftpClient.logout();
			// 断开连接
			ftpClient.disconnect();
		}
	}

	// 查询是否应该下载
	public void searchFtpDownload(String server_file_path, int version_code, String local_storage_path,
			DownLoadProgressListener listener,NOExistNewEdition two_listener) {
		// 打开FTP服务
		try {
			this.openConnect();
			Boolean boolean4 = ftpClient.changeWorkingDirectory(server_file_path);
			LwtLog.d("wulin", "changeWorkingDirectory---------" + boolean4);
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_CONNECT_SUCCESSS, 0, null,"");
		} catch (IOException e1) {
			e1.printStackTrace();
			try {
				this.closeConnect();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			listener.onDownLoadProgress(FtpBuinessLayer.FTP_CONNECT_FAIL, 0, null,"");
			return;
		}
		try {
			// 先判断服务器文件是否存在
			FTPFile[] files = ftpClient.listFiles();
			try {
				this.closeConnect();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if (files.length > 0) {
				for (FTPFile file : files) {
					int server_apk_version=Integer.parseInt(file.getName().substring(0,file.getName().lastIndexOf(".")));
					if (server_apk_version > version_code) {
						try {
							two_listener.existNewEdition();
							downloadSingleFile(server_file_path + "/" + server_apk_version + ".apk", server_file_path,
									local_storage_path, server_apk_version+".apk", listener,0);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							try {
								this.closeConnect();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						two_listener.onNotExistNewEdition();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				this.closeConnect();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	// ---------------------------------------------------上传、下载、删除监听---------------------------------------------

	/*
	 * 上传进度监听
	 */
	public interface UploadProgressListener {
		public void onUploadProgress(String currentStep, long uploadSize, File file);
	}

	/*
	 * 下载进度监听
	 */
	public interface DownLoadProgressListener {
		public void onDownLoadProgress(String currentStep, long downProcess, File file,String install_apk_path);
	}

	/*
	 * 文件删除监听
	 */
	public interface DeleteFileProgressListener {
		public void onDeleteProgress(String currentStep);
	}
	
	/*
	 * 文件已经存在
	 */
	
	public interface NOExistNewEdition{
		public void onNotExistNewEdition();
		
		public void existNewEdition();
	}
}
