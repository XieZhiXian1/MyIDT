package com.ids.idtma.meeting;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration.Status;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ids.idtma.ActivityBase;
import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.util.Converter;
import com.ids.idtma.util.CurrentGroupCall;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.proxy.IDSApiProxyMgr;

public class ActivityMeetingCall extends ActivityBase implements OnClickListener,SurfaceHolder.Callback,Camera.PreviewCallback,Runnable {
	private static String TAG = ActivityMeetingCall.class.getName();
	int audioMode = AudioManager.MODE_IN_CALL;

	int callID; // 呼叫ID
	String caller;// 主叫号码
	String callee;// 被叫号码
	String groupCallNum;// 组号码
	TextView tv_group_number;
	TextView tv_talking_user;
	ImageButton btn_speak;
//	ImageView iv_talk_listen_flag;
	private final static int STATUS_TALK = 1;
	private final static int STATUS_LISTEN = 2;
	
	public final static int FLAG_INCOMING = 0;// 来电
	public final static int FLAG_CALLING = 1;// 正在呼叫
	public final static int FLAG_ANSWER = 2;// 接听
	public static final int SHOW_CURRENT_TIME = 3;
	private int status;
	AudioManager audioManager;
	Chronometer chronometer;
	NotificationManager mNotificationManager;
	
	private Camera camera;
	// 是前置摄像头还是后置摄像头
	private int currentCamera = CameraInfo.CAMERA_FACING_BACK;
	private MediaCodec mMediaEncoder;
	private MediaCodec mMediaDecoder;
	private byte[] mMediaHead = null;
	private int Frame_Rate = 15;
	private int iBitrate = 200 * 1024;
	private int FRAME_INTERVAL = 5;
	public static int SEND_PREVIEW_WIDTH = 480;
	public static int SEND_PREVIEW_HEIGHT = 320;
	//本地的
	private SurfaceView surfaceView_local;
	private SurfaceHolder surfaceHolder;
	//远程的只需要surface，解码的时候，是将.264格式的直接写入到surface里面的
    private SurfaceView surfaceView_remote;
	private Surface mRemoteSurface;
	// 每一个像素需要1.5个字节进行存储
	private byte[] mYuvBuffer = null;
	private Bitmap previewBitmap;
	// 编码器释放出来的byte数组，包含空的要干掉
	private byte[] mEncoderH264Buf = new byte[10240];
	private UdpSendTask netSendTask;
	private boolean Thread_RUN=false;
	private List<Size> mSupportedPreviewSizes;
	private ByteBuffer byteBuffer = null;
	private int SEND_STATUS = 0;
	private int RECEIVE_STATUS = 1;
    private TextView my_num_textview;
    private TextView streamTextview,current_time_textview;
    private boolean TIME_THREAD_RUN = true;
    private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == SHOW_CURRENT_TIME) {
				String current_time = (String) msg.obj;
				current_time_textview.setText(current_time);
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_ui_video_meeting_call);
		IdtApplication.getInstance().addActivity(this);
		my_num_textview = (TextView) findViewById(R.id.my_num);
		my_num_textview.setText(SharedPreferencesUtil.getStringPreference(getApplicationContext(), "phone_number", ""));
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		findViewById(R.id.btn_hangup).setOnClickListener(this);
//		findViewById(R.id.iv_micphone).setOnClickListener(this);
//		findViewById(R.id.iv_mute).setOnClickListener(this);
//		iv_talk_listen_flag = (ImageView) findViewById(R.id.iv_call_flag);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// 获取用Intent传过来的数据
//		// 组呼id
//		callID = getIntent().getIntExtra(AppConstants.EXTRA_KEY_CALLID, -1);
		// 最开始为空
		caller = getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLER);
		// call_to_persion num对方号码
		callee = getIntent().getStringExtra(AppConstants.EXTRA_KEY_CALLEE);
		status = getIntent().getIntExtra(AppConstants.EXTRA_KEY_CALL_STATUS, -1);
		// 组呼电话号码
		groupCallNum = getIntent().getStringExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM);
		tv_group_number = (TextView) findViewById(R.id.tv_group_number);
		tv_group_number.setText(groupCallNum);
		tv_talking_user = (TextView) findViewById(R.id.tv_talking_user);

		btn_speak = (ImageButton) findViewById(R.id.btn_speak);
//		btn_speak.setOnLongClickListener(this);
		btn_speak.setOnClickListener(this);
		
		
//		btn_speak.setOnTouchListener(new MyClickListener());
		chronometer = (Chronometer) findViewById(R.id.chronometer_call_time);

		
		
		
		
		MediaAttribute pAttr1 = new MediaAttribute();
		pAttr1.ucAudioRecv = 1;
		pAttr1.ucAudioSend = 0;
		pAttr1.ucVideoRecv = 1;
		pAttr1.ucVideoSend = 0;
		 //链接是对方发过来的，那么target_phone_number就为群组号
		callID = IDSApiProxyMgr.getCurProxy().CallMakeOut(callee, AppConstants.CALL_TYPE_GROUP_CALL, pAttr1, 0);
		// 设置当前的拨打
		IdtApplication.setCurrentCall(new CallEntity(callID, CallType.GROUP_CALL));
		LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>开始拨打会议链接: " + callee + ", id : " + callID);
		
		
		
		
		
		// 别人的头像
		surfaceView_remote = (SurfaceView) findViewById(R.id.surfaceView_remote);
		// 访问SurfaceView的底层图形是通过SurfaceHolder接口来实现的，通过 getHolder()方法可以得到这个
		// SurfaceHolder对象。你应该实现surfaceCreated(SurfaceHolder)和
		// surfaceDestroyed(SurfaceHolder)方法来知道在这个Surface在窗口的显示和隐藏过程中是什么时候创建和销毁的。
		// SurfaceView可以在多线程中被访问。
		// 注 意：一个SurfaceView只在SurfaceHolder.Callback.surfaceCreated() 和
		// SurfaceHolder.Callback.surfaceDestroyed()调用之间是可用的，其他时间是得不到它的Canvas对象的
		// （null）。
		mRemoteSurface = surfaceView_remote.getHolder().getSurface();
//		// 发送线程
//		netSendTask = new UdpSendTask();
//		netSendTask.init();
//		netSendTask.start();
		Thread_RUN=true;
		// 自己的头像
		surfaceView_local = (SurfaceView)findViewById(R.id.surfaceView_local);
		// 将自己的视频图像至于最上层
		surfaceView_local.bringToFront();
		// 获取自己图像的SurfaceHolder，方便对其进行操作
		surfaceHolder = surfaceView_local.getHolder();
		// 给当前图像一个回调方法
		surfaceHolder.addCallback(this);
		//previewBitmap = Bitmap.createBitmap(RECEIVE_PREVIEW_WIDTH, RECEIVE_PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
		
		
		
		
		
		
		if (status == ActivityMeetingCall.FLAG_INCOMING) {
			// 通知从这个地方走
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
			ringtone.play();
			playVibrate();
			startChronometer();
			tv_group_number.setText(caller);
			MediaAttribute pAttr = new MediaAttribute();
			pAttr.ucAudioRecv = 1;
			pAttr.ucAudioSend = 0;
			pAttr.ucVideoRecv = 0;
			pAttr.ucVideoSend = 0;
			IDSApiProxyMgr.getCurProxy().CallAnswer(callID, pAttr, 0);
		} else if(status== ActivityMeetingCall.FLAG_CALLING){
			// 最开始进入这个界面从这里走
//			iv_talk_listen_flag.setBackgroundResource(R.drawable.group_call_talk);
			btn_speak.setBackgroundResource(R.drawable.new_ui_ppt02);
		}
		// 通知调用该界面的时候，从这里走
		if (getIntent().getIntExtra(AppConstants.EXTRA_KEY_NOTIFICATION_INTENT, -1) != -1) {
			chronometer.setVisibility(View.VISIBLE);
			chronometer.setFormat("通话时长：%s");
			chronometer.setBase(IdtApplication.getCurrentCall().getChronometer().getBase());
			chronometer.start();
		}
		streamTextview =(TextView) findViewById(R.id.stream);
		//时间显示
		current_time_textview = (TextView) findViewById(R.id.current_time);
		// 实时显示时间
		new Thread(this).start();
	}
	
	// 界面surface改变的时候调用
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	// 界面surface创建时调用
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// startPreview(currentCamera);
		IDSApiProxyMgr.getCurProxy().SetSurface(mRemoteSurface);
	}

	// 界面surface销毁时调用
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (null != camera) {
			camera.stopFaceDetection();
			camera.stopPreview();
		}
	}
	
	// 当一个帧被显示了，就会调用
		// 当方法会被不停地被回调，我们可以在此处将数据传送到服务器
		@Override
		public void onPreviewFrame(byte[] rawData, Camera camera) {
			//可以发送
			if(SEND_STATUS == 1){
				
				IDSApiProxyMgr.getCurProxy().CallSendVideoData(0, 98, null, 0, rawData, rawData.length, 1,
						0, 0, 0);
				
				
////			writeToFileTest(rawData, "raw.yuv");
//			
//			
//			// int coded_flag = 0;
//			// pucDstBuf=null;
//			// i420bytes=null;
//			// pucDstBuf = new byte[SEND_PREVIEW_WIDTH * SEND_PREVIEW_HEIGHT * 2];
//			// i420bytes = new byte[SEND_PREVIEW_WIDTH * SEND_PREVIEW_HEIGHT * 2];
//			// //LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>>>> 发送视频数据: " + new
//			// String(data));
//			// // codec.CodecFunc1(2, codec, ByteBuffer.wrap(data), outputBuf,
//			// // pucSrcBuf, iSrcLen, pucDstBuf, iDstSize, iTs);
//			// //
//			// --------------------------------------------------------------------------------
//			// // 编解码接口函数
//			// // 输入:
//			// // iType: 调用类型 0:语音编码 1:语音解码 2:视频编码 3:视频解码
//			// // pucSrcBuf: 源缓冲区
//			// // iSrcLen: 源缓冲区长度
//			// // pucDstBuf: 目的缓冲区
//			// // iDstSize: 目的缓冲区大小
//			// // 返回:
//			// // -1: 失败
//			// // else: 编解码之后,目的缓冲区有效长度
//			// codec.swapYV12toI420(data, i420bytes, SEND_PREVIEW_WIDTH,
//			// SEND_PREVIEW_HEIGHT);
//			// //writeToFileTest(i420bytes,"1.yuv");
//			// coded_flag = codec.CodecFunc(2, i420bytes, (int) (SEND_PREVIEW_WIDTH
//			// * SEND_PREVIEW_HEIGHT * 1.5), pucDstBuf, pucDstBuf.length);
//			// if (coded_flag != 0) {
//			// //LwtLog.d(IdtApplication.WULIN_TAG, ">>>>>>>>>>>>> 加密的视频数据: " + new
//			// String(pucDstBuf));
//			// //writeToFileTest(pucDstBuf, "1.264");
//			// IDSApiProxyMgr.getCurProxy().CallSendVideoData(callId, 0, null, 0,
//			// pucDstBuf, coded_flag, 0, 0, 0, 0);
//			// }
//			// camera.addCallbackBuffer(data);
//			int w = camera.getParameters().getPreviewSize().width;
//			int h = camera.getParameters().getPreviewSize().height;
//			int format = camera.getParameters().getPreviewFormat();
//			// Log.d("wulin", "编码设置的一些参数preview frame format:" + format + " size:" +
//			// rawData.length + " w:" + w + " h:" + h);
//			if (mMediaEncoder == null) {
//				setupEncoder("video/avc", w, h);
//			}
//			// convert yv12 to i420
//			swapYV12toI420(rawData, mYuvBuffer, w, h);
//			System.arraycopy(mYuvBuffer, 0, rawData, 0, rawData.length);
//			// set h264 buffer to zero.
//			for (int i = 0; i < mEncoderH264Buf.length; i++)
//				mEncoderH264Buf[i] = 0;
//			int encoderRet = offerEncoder(rawData, mEncoderH264Buf);
//			if (encoderRet > 0) {
//				Log.d("wulin",
//						"encoder output h264 buffer len-------------------------------------------------------------------------------------------:"
//								+ encoderRet);
//				// 将其推送到一个线程里面，依次进行提交到服务器
//				netSendTask.pushBuf(mEncoderH264Buf, encoderRet);
//				
//				
//				
//				
////	            //-----------------------------------------------------------------------------------------------------
////				// 在此处进行解码测试,实际不在这个地方
////				if (mMediaDecoder == null) {
////					try {
////						setupDecoder(mRemoteSurface, "video/avc", w, h);
////					} catch (Exception e) {
////						// TODO: handle exception
////						LwtLog.d(IdtApplication.WULIN_TAG, "setupDecoder处有bug");
////					}
////				}
////				/**
////	        	 * push data to decoder
////	        	 */
////	        	offerDecoder(mEncoderH264Buf,encoderRet);
//			}
		}
			// reset buff to camera.
			camera.addCallbackBuffer(rawData);
	}
	
	

		class UdpSendTask extends Thread {
			private ArrayList<byte[]> mList;

			public void init() {
				mList = new ArrayList<byte[]>();
			}

			public void pushBuf(byte[] buf, int len) {
				// ByteBuffer buffer = ByteBuffer.allocate(len);
				// buffer.put(buf, 0, len);
				byte[] effectiveByte = subBytes(buf, 0, len);
				mList.add(effectiveByte);
				// 写入到文件中
//				writeToFileTest(effectiveByte, "encode.264");
			}

			// 截取byte数组的部分
			public byte[] subBytes(byte[] src, int begin, int count) {
				byte[] bs = new byte[count];
				for (int i = begin; i < begin + count; i++)
					bs[i - begin] = src[i];
				return bs;
			}

			@Override
			public void run() {
//				Log.d(TAG, "fall in udp send thread");
				while (Thread_RUN) {
					if (mList.size() <= 0) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					while (mList.size() > 0) {
						byte[] sendBuf = mList.get(0);
						// byte[] bytes = new byte[sendBuf.remaining()];
						IDSApiProxyMgr.getCurProxy().CallSendVideoData(0, 98, null, 0, sendBuf, sendBuf.length, 1,
								0, 0, 0);
						Log.d("wulin",
								"发送数据了IDSApiProxyMgr.getCurProxy().CallSendVideoData(0, 98, null, 0, bytes, bytes.length, Frame_Rate, 5, 0, 0)");
						mList.remove(0);
					}
				}
			}
		}

		// 编码
		@SuppressLint("NewApi")
		private int offerEncoder(byte[] input, byte[] output) {

			int pos = 0;
			try {
				ByteBuffer[] inputBuffers = mMediaEncoder.getInputBuffers();
				ByteBuffer[] outputBuffers = mMediaEncoder.getOutputBuffers();
				int inputBufferIndex = mMediaEncoder.dequeueInputBuffer(-1);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
					// Log.d(TAG, "offerEncoder InputBufSize: " +
					// inputBuffer.capacity() + " inputSize: " + input.length
					// + " bytes");
					inputBuffer.clear();
					inputBuffer.put(input);
					mMediaEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);

				}

				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				int outputBufferIndex = mMediaEncoder.dequeueOutputBuffer(bufferInfo, 0);
				while (outputBufferIndex >= 0) {
					ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

					byte[] data = new byte[bufferInfo.size];
					outputBuffer.get(data);

					// Log.d(TAG, "offerEncoder InputBufSize:" +
					// outputBuffer.capacity() + " outputSize:" + data.length
					// + " bytes written");

					if (mMediaHead != null) {
						System.arraycopy(data, 0, output, pos, data.length);
						pos += data.length;
					} else {
						// Log.d(TAG, "offer Encoder save sps head,len:" +
						// data.length);
						ByteBuffer spsPpsBuffer = ByteBuffer.wrap(data);
						if (spsPpsBuffer.getInt() == 0x00000001) {
							mMediaHead = new byte[data.length];
							System.arraycopy(data, 0, mMediaHead, 0, data.length);
						} else {
							// Log.e(TAG, "not found media head.");
							return -1;
						}
					}

					mMediaEncoder.releaseOutputBuffer(outputBufferIndex, false);
					outputBufferIndex = mMediaEncoder.dequeueOutputBuffer(bufferInfo, 0);
				}

				if (output[4] == 0x65) {
					System.arraycopy(output, 0, input, 0, pos);
					System.arraycopy(mMediaHead, 0, output, 0, mMediaHead.length);
					System.arraycopy(input, 0, output, mMediaHead.length, pos);
					pos += mMediaHead.length;
				}

			} catch (Throwable t) {
				t.printStackTrace();
			}
			return pos;
		}

		// yuv先转成i420
		private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
			System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
			System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
			System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
		}

		// 初始化编码器
		@SuppressLint("NewApi")
		private boolean setupEncoder(String mime, int width, int height) {
			int colorFormat = selectColorFormat(selectCodec(mime), mime);
			// Log.d(TAG, "setupEncoder " + mime + " colorFormat:" + colorFormat + "
			// w:" + width + " h:" + height);

			mMediaEncoder = MediaCodec.createEncoderByType(mime);
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
			mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, iBitrate);
			mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Frame_Rate);
			// mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
			// MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
			mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
			mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);
			mMediaEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			mMediaEncoder.start();

			return true;
		}

		@SuppressLint("NewApi")
		private boolean setupDecoder(Surface surface, String mime, int width, int height) {
			// Log.d(TAG, "setupDecoder surface:" + surface + " mime:" + mime + "
			// w:" + width + " h:" + height);
			int colorFormat = selectColorFormat(selectCodec(mime), mime);
			mMediaDecoder = MediaCodec.createDecoderByType(mime);
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
			mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
	        if (mMediaDecoder == null) {
				// Log.e("DecodeActivity", "createDecoderByType fail!");
				return false;
			}
			mMediaDecoder.configure(mediaFormat, surface, null, 0);
			mMediaDecoder.start();
			return true;
		}

		/**
		 * Returns the first codec capable of encoding the specified MIME type, or
		 * null if no match was found.
		 */
		@SuppressLint("NewApi")
		private static MediaCodecInfo selectCodec(String mimeType) {
			int numCodecs = MediaCodecList.getCodecCount();
			for (int i = 0; i < numCodecs; i++) {
				MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

				if (!codecInfo.isEncoder()) {
					continue;
				}

				String[] types = codecInfo.getSupportedTypes();
				for (int j = 0; j < types.length; j++) {
					if (types[j].equalsIgnoreCase(mimeType)) {
						return codecInfo;
					}
				}
			}
			return null;
		}

		/**
		 * Returns a color format that is supported by the codec and by this test
		 * code. If no match is found, this throws a test failure -- the set of
		 * formats known to the test should be expanded for new platforms.
		 */
		@SuppressLint("NewApi")
		private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
			MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
			for (int i = 0; i < capabilities.colorFormats.length; i++) {
				int colorFormat = capabilities.colorFormats[i];
				if (isRecognizedFormat(colorFormat)) {
					return colorFormat;
				}
			}
			// Log.e(TAG, "couldn't find a good color format for " +
			// codecInfo.getName() + " / " + mimeType);
			return 0; // not reached
		}

		/**
		 * Returns true if this is a color format that this test code understands
		 * (i.e. we know how to read and generate frames in this format).
		 */
		private static boolean isRecognizedFormat(int colorFormat) {
			switch (colorFormat) {
			// these are the formats we know how to handle for this test
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
				return true;
			default:
				return false;
			}
		}

		public void writeToFileTest(byte[] testByte, String name) {
			try {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/IDT-MA/activity_video/");
					if (!dir.exists()) {
						dir.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(
							Environment.getExternalStorageDirectory().getPath() + "/IDT-MA/activity_video/" + name, true);
					// fos.write(name.getBytes());
					fos.write(testByte);
					fos.flush();
					fos.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		// 获取最优的摄像头拍照尺寸
		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
			double ASPECT_TOLERANCE = 0.1;
			double targetRatio = (double) w / h;
			if (sizes == null)
				return null;

			Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;
			int targetHeight = h;

			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
					continue;
				}
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}

			if (optimalSize == null) {
				minDiff = Double.MAX_VALUE;
				for (Size size : sizes) {
					if (Math.abs(size.height - targetHeight) < minDiff) {
						optimalSize = size;
						minDiff = Math.abs(size.height - targetHeight);
					}
				}
			}
			return optimalSize;
		}

		// 对摄像机进行初始化
		private synchronized void initCamera(int cameraId) {
			// 关闭摄像头和与之相关的服务
			closeCamera();
			// 打开前置摄像头
			camera = Camera.open(cameraId);
			//获取最优的相机拍照尺寸
			mSupportedPreviewSizes = camera.getParameters()
					.getSupportedPictureSizes();
			for(int i=0;i<mSupportedPreviewSizes.size();i++){
				LwtLog.d("wulin", mSupportedPreviewSizes.get(i).width+"*"+mSupportedPreviewSizes.get(i).height);
			}
			int local_surfaceview_width = surfaceView_local.getWidth();
			int local_surfaceview_height = surfaceView_local.getHeight();
			Size goodSize=getOptimalPreviewSize(mSupportedPreviewSizes, local_surfaceview_width, local_surfaceview_height);
			SEND_PREVIEW_WIDTH = goodSize.width;
			SEND_PREVIEW_HEIGHT = goodSize.height;
			byteBuffer = ByteBuffer.wrap(new byte[SEND_PREVIEW_WIDTH * SEND_PREVIEW_HEIGHT * 2]);
			mYuvBuffer = new byte[SEND_PREVIEW_WIDTH * SEND_PREVIEW_HEIGHT * 3 / 2];
			// 设置照相机的参数配置
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(SEND_PREVIEW_WIDTH, SEND_PREVIEW_HEIGHT);
			parameters.setPictureSize(SEND_PREVIEW_WIDTH, SEND_PREVIEW_HEIGHT);
			// 获取预览合适
			List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
			if (supportedPreviewFormats.contains(ImageFormat.YV12))
				parameters.setPreviewFormat(ImageFormat.YV12);
			// if (supportedPreviewFormats.contains(ImageFormat.RGB_565)) {
			// parameters.setPreviewFormat(ImageFormat.RGB_565);
			// }
			// if (supportedPreviewFormats.contains(ImageFormat.NV16)) {
			// parameters.setPreviewFormat(ImageFormat.NV16);
			// }
			// 设置帧率为15，帧率越高越不准
			// 当设为15的时候，每秒钟onPreviewFrame执行15次
			parameters.setPreviewFrameRate(Frame_Rate);
			Frame_Rate = parameters.getPreviewFrameRate();
			LwtLog.d("video", "帧率为:" + parameters.getPreviewFrameRate());
			List<String> suportFocusModes = parameters.getSupportedFocusModes();
			// 假如支持自动对焦功能，那么让摄像头处于自动对焦状态（人脸识别）
			if (suportFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			// 告诉摄像头你要去录像而不是照相，更加高效
			parameters.setRecordingHint(true);
			// if (parameters.isVideoStabilizationSupported())
			// parameters.setVideoStabilization(true);
			camera.setParameters(parameters);
			// // 初始化codec对象
			// codec.Init();
			// // iType: 调用类型 0:语音编码 1:语音解码 2:视频编码 3:视频解码
			// // iCtrl: 控制 0:启动 1:停止
			// // iWidth: 宽度
			// // iHeight: 高度
			// // iFrameRate: 帧率
			// // iBitrate: 码率
			// codec.CtrlFunc(2, 0, SEND_PREVIEW_WIDTH, SEND_PREVIEW_HEIGHT,
			// Frame_Rate, iBitrate);
		}


		// 开始预览
		private void startPreview(int cameraId) {
			// previewSize = camera.getParameters().getPreviewSize();
			try {
				// 设置预览图片显示的位置
				camera.setPreviewDisplay(surfaceHolder);
				// camera.addCallbackBuffer(new byte[previewSize.width
				// * previewSize.height * 3 / 2]);
				camera.addCallbackBuffer(new byte[(int) (SEND_PREVIEW_WIDTH * SEND_PREVIEW_HEIGHT * 1.5)]);
				// 设置一个回调
				camera.setPreviewCallbackWithBuffer(this);
				// 设置摄像头的显示方向
				setCameraDisplayOrientation(cameraId);
				// 支持人脸检测的话，最大的检测人脸面数
				if (0 < camera.getParameters().getMaxNumDetectedFaces())
					// 开始人脸检测
					camera.startFaceDetection();
				// 开始捕获并绘制预览帧到屏幕
				camera.startPreview();
			} catch (IOException e) {
				LwtLog.e(TAG, e.getMessage(), e);
			}

			// LwtLog.d(TAG, ">>>>>>>>>>>>> initCamera previewSize: " +
			// previewSize.width
			// + " * " + previewSize.height);
		}

		// 释放摄像有关的资源
		private void closeCamera() {
			if (camera != null) {
				surfaceHolder.setKeepScreenOn(false);
				surfaceHolder.removeCallback(this);
				camera.setPreviewCallback(null);
				camera.addCallbackBuffer(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}

		
		// 设置显示方向
		public void setCameraDisplayOrientation(int cameraId) {
			CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, cameraInfo);
			// 获取屏幕的旋转
			//逆时针计算
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			int degrees = 0;
			switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			}
			int result;
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				result = (cameraInfo.orientation + degrees) % 360;
				result = (360 - result) % 360;
			} else {
				result = (cameraInfo.orientation - degrees + 360) % 360;
			}
	        //设置顺时针旋转的预览显示在度。这会影响预览帧和快照后显示的图片。这种方法是有用的肖像模式应用。请注意，预览显示的前置摄像头是水平翻转前的旋转，
	        //即，图像是反映沿中心垂直轴的摄像头传感器。所以用户可以看到自己像镜子一样。
			camera.setDisplayOrientation(result);
		}

		@Override
		public void onReceiveVideoData(byte[] data) {
			// byteBuffer = ByteBuffer.wrap(data);
			if(RECEIVE_STATUS == 1){
				//可以接收
//				writeVideoData(data);
			}
		}

		public int writeVideoData(byte[] pucBuf) {
			try {
				int w = camera.getParameters().getPreviewSize().width;
				int h = camera.getParameters().getPreviewSize().height;
				LwtLog.d("ACodec",
						"*************************************************************************************************"
								+ "************************************************************************************************"
								+ "***************************************************************************** ");
				LwtLog.d("ACodec", "收到视频数据长度：" + pucBuf.length);
				if (mRemoteSurface == null || !mRemoteSurface.isValid())
					return 0;
				// 解码
				if (mMediaDecoder == null) {
					try {
						setupDecoder(mRemoteSurface, "video/avc", w, h);
					} catch (Exception e) {
						// TODO: handle exception
						LwtLog.d(IdtApplication.WULIN_TAG, "setupDecoder处有bug");
					}
				}
				if (pucBuf != null && pucBuf.length != 0) {
					LwtLog.d("ACodec", Converter.byteToHex(pucBuf));
					/**
					 * push data to decoder
					 */
					offerDecoder(pucBuf, pucBuf.length);
				}
				// if (pucBuf.length > 0) {
				// byteBuffer = ByteBuffer.wrap(pucBuf);
				// byteBuffer.rewind();
				// previewBitmap.copyPixelsFromBuffer(byteBuffer);
				// // FileOutputStream fos = null;
				// // try {
				// // fos = new FileOutputStream("/mnt/sdcard/com.ids.idtma.photo/"
				// // + System.currentTimeMillis() + ".jpg");
				// // } catch (FileNotFoundException e) {
				// // LwtLog.e(TAG, e.getMessage(), e);
				// // }
				// // if (fos != null) {
				// // previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				// // try {
				// // fos.close();
				// // } catch (IOException e) {
				// // LwtLog.e(TAG, e.getMessage(), e);
				// // }
				// // }
				//
				// doDraw();
				// }
			} catch (Exception e) {
				// TODO: handle exception
			}
			return 0;
		}

		private int mFrameIndex = 0;

		@SuppressLint("NewApi")
		private void offerDecoder(byte[] input, int length) {
			try {
				ByteBuffer[] inputBuffers = mMediaDecoder.getInputBuffers();
				int inputBufferIndex = mMediaDecoder.dequeueInputBuffer(-1);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
					long timestamp = mFrameIndex++ * 1000000 / Frame_Rate;
					// Log.d(TAG, "offerDecoder timestamp: " + timestamp + "
					// inputSize: " + length + " bytes");
					inputBuffer.clear();
					inputBuffer.put(input, 0, length);
					mMediaDecoder.queueInputBuffer(inputBufferIndex, 0, length, timestamp, 0);
				}

				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				int outputBufferIndex = mMediaDecoder.dequeueOutputBuffer(bufferInfo, 0);
				while (outputBufferIndex >= 0) {
					// Log.d(TAG, "offerDecoder OutputBufSize:" + bufferInfo.size +
					// " bytes written");

					// If a valid surface was specified when configuring the codec,
					// passing true renders this output buffer to the surface.
					mMediaDecoder.releaseOutputBuffer(outputBufferIndex, true);
					outputBufferIndex = mMediaDecoder.dequeueOutputBuffer(bufferInfo, 0);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

	@Override
	public void onBackPressed() {
		minimizationActivity();
		super.onBackPressed();
	}

	@Override
	protected void onUserLeaveHint() {
		minimizationActivity();
		moveTaskToBack(true);
		super.onUserLeaveHint();
	}

	//当点击返回键的时候进行最小化
	private void minimizationActivity() {
		
		try {
			// 消息通知栏
			CharSequence tickerText = "会议进行中";
			CharSequence contentTitle = "正在通话...";

			// 定义通知栏展现的内容信息
			int icon = R.drawable.notification_call;

			long when = System.currentTimeMillis();// 通知产生的时间，会在通知信息里显示
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			// 定义下拉通知栏时要展现的内容信息
			CharSequence contentText = callee;
			Intent notificationIntent = new Intent(getApplicationContext(), ActivityMeetingCall.class);
			// 群呼id
			notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLID, IdtApplication.getCurrentCall().getCallid());
			notificationIntent.putExtra(AppConstants.EXTRA_KEY_CALLER, caller);
			notificationIntent.putExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM, groupCallNum);
			notificationIntent.putExtra(AppConstants.EXTRA_KEY_NOTIFICATION_INTENT, 0);

			IdtApplication.getCurrentCall().setIntent(notificationIntent);
			IdtApplication.getCurrentCall().setChronometer(chronometer);

			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
					AppConstants.CALL_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);// 更新Notification

			// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
			mNotificationManager.notify(AppConstants.CALL_NOTIFICATION_ID, notification);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	public void playVibrate() {

		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// 震动一次
		vibrator.vibrate(500);
		// 第一个参数，指代一个震动的频率数组。每两个为一组，每组的第一个为等待时间，第二个为震动时间。
		// 比如 [2000,500,100,400],会先等待2000毫秒，震动500，再等待100，震动400
		// 第二个参数，repest指代从 第几个索引（第一个数组参数） 的位置开始循环震动。
		// 会一直保持循环，我们需要用 vibrator.cancel()主动终止
		// vibrator.vibrate(new long[]{300,500},0);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_hangup:
			// 挂断
			mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
			// PTYT被叫不能被挂机
			if (0 == IDSApiProxyMgr.getCurProxy().CallRel(IdtApplication.getCurrentCall().getCallid(), 0, 0)) {
				LwtLog.d(TAG, "IDTNativeApi.IDT_CallRel 已挂机 >>>>>>>>>>>> callId : " + callID);
				callRel();
				setResult(RESULT_OK);
				CurrentGroupCall.CURRENT_GROUP_CALL_NUM="";
				CurrentGroupCall.CURRENT_GROUP_CALL_STATE="";
				CurrentGroupCall.CALL_OK = false;
				finish();
			}

			IdtApplication.setCurrentCall(null);
			break;
//		case R.id.iv_micphone:
//
//			break;
//		case R.id.iv_mute:
//
//			break;
		case R.id.btn_speak:
			if(status == STATUS_LISTEN){
				status = STATUS_TALK;
				setImageButtonBackground();
			}else if(status == STATUS_TALK){
				status = STATUS_LISTEN;
				setImageButtonBackground();
			}
			break;
		default:
			break;
		}
	}
	
	void callRel() {
		//设置当前呼叫为空
		IdtApplication.setCurrentCall(null);
		//取消本页面的notification
		mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
		closeCamera();
	}

	private void setSpeakerphoneOn(boolean on) {
		if (on) {
			audioManager.setSpeakerphoneOn(true);
		} else {
			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
			audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			// 把声音设定成Earpiece（听筒）出来，设定为正在通话中
			audioManager.setMode(AudioManager.MODE_IN_CALL);
		}
	}

//	// 检测到没有按屏幕以后，证明释放话权
//	class MyClickListener implements OnTouchListener {
//		public boolean onTouch(View v, MotionEvent event) {
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_UP:
//				status = STATUS_LISTEN;
//				setImageButtonBackground();
//				break;
//			default:
//				break;
//			}
//			return false;
//		}
//	}

	// 按下和放松的时候
	public void setImageButtonBackground() {
		switch (status) {
		case STATUS_TALK:
			btn_speak.setBackgroundResource(R.drawable.new_ui_ppt02);
//			iv_talk_listen_flag.setBackgroundResource(R.drawable.group_call_talk);
			IDSApiProxyMgr.getCurProxy().CallMicCtrl(callID, true);
			LwtLog.d(TAG, ">>>>>>>>>>>>> 获取话权");
			break;
		case STATUS_LISTEN:
			btn_speak.setBackgroundResource(R.drawable.new_ui_ppt01);
//			iv_talk_listen_flag.setBackgroundResource(R.drawable.group_call_listen);
			IDSApiProxyMgr.getCurProxy().CallMicCtrl(callID, false);
			LwtLog.d(TAG, ">>>>>>>>>>>>> 释放话权");
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LwtLog.d(TAG, ">>>>>>>>> onDestroy");
		closeCamera();
		mRemoteSurface = null;
		IDSApiProxyMgr.getCurProxy().SetSurface(mRemoteSurface);
		Thread_RUN=false;
		TIME_THREAD_RUN = false;
		mYuvBuffer=null;
		mEncoderH264Buf=null;
	}

//	// 长按获取话权
//	@Override
//	public boolean onLongClick(View view) {
//		switch (view.getId()) {
//		case R.id.btn_speak:
//			status = STATUS_TALK;
//			setImageButtonBackground();
//			break;
//		default:
//			break;
//		}
//		return true;
//	}

	// A方发起群呼，当会议建立起来以后，给A方一个回应
	@Override
	public void onCallPeerAnswer() {
		// TODO Auto-generated method stub
		super.onCallPeerAnswer();
		LwtLog.d(TAG, "对端应答 >>>>>>>>>>>>meeting onCallPeerAnswer()");
		// 只要一打通，就置为听筒模式
		status = STATUS_LISTEN;
		// 将计时器清零并启动
		startChronometer();
		
		
		
		
		//初始化摄像头,并开始预览
		initCamera(currentCamera);
		startPreview(currentCamera);
	}

	// 根据现在存在话权方与否进行配置
	@Override
	public void onCallTalkingTips(String name, String phone) {
		LwtLog.d(TAG, "讲话方提示 >>>>>>>>>>>> onCallTalkingTips()");
		tv_talking_user.setVisibility(View.VISIBLE);
		if (phone == null || "".equals(phone)) {
			tv_talking_user.setText("主讲人员：空闲");
		} else {
			tv_talking_user.setText("主讲人员：" + phone);
		}
	}
	
	@Override
	public void getMic(int uiInd) {
		// TODO Auto-generated method stub
		super.getMic(uiInd);
		if(uiInd>0){
			tv_talking_user.setText("主讲人员：我");
			status = STATUS_TALK;
			btn_speak.setBackgroundResource(R.drawable.new_ui_ppt02);
		}else{
			tv_talking_user.setText("主讲人员：空闲");
			status = STATUS_LISTEN;
			btn_speak.setBackgroundResource(R.drawable.new_ui_ppt01);
		}
		
	}

	// 远端释放的时候
	@Override
	public void onCallRelInd(int ID,int uiCause,Parcelable parcelable) {
		// TODO Auto-generated method stub
		LwtLog.d(TAG, "远端释放 >>>>>>>>>>>> onCallRelInd()");
		mNotificationManager.cancel(AppConstants.CALL_NOTIFICATION_ID);
		setResult(RESULT_OK);
		finish();
	}

	private void startChronometer() {
		// 将计时器清零
		chronometer.setVisibility(View.VISIBLE);
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.setFormat("通话时长：%s");
		// 开始计时
		chronometer.start();
	}
	
	@Override
	public void changeVideoCodec(int ucRecv, int ucSend) {
		// TODO Auto-generated method stub
		super.changeVideoCodec(ucRecv, ucSend);
		if(ucSend == 0){
			//不接收
			SEND_STATUS = 0;
		}else if(ucSend == 1){
			//接收
			SEND_STATUS = 1;
		}
		if(ucRecv == 0){
			//不发送
			RECEIVE_STATUS = 0;
		}else if(ucRecv == 1){
			//发送
			RECEIVE_STATUS = 1;
		}
	}
	
	@Override
	public void receiveStream(int uiRxUsrBytes, int uiTxUsrBytes) {
		// TODO Auto-generated method stub
		super.receiveStream(uiRxUsrBytes, uiTxUsrBytes);
		streamTextview.setVisibility(View.VISIBLE);
		if(uiRxUsrBytes!=-1 && uiTxUsrBytes!=-1){
			streamTextview.setText("接收:"+uiRxUsrBytes/1000/2*8+"kbps  "+" 发送:"+uiTxUsrBytes/1000/2*8+"kbps");
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (TIME_THREAD_RUN) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String str = sdf.format(new Date());
			handler.sendMessage(handler.obtainMessage(SHOW_CURRENT_TIME, str));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}