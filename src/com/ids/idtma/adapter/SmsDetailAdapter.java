package com.ids.idtma.adapter;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ids.idtma.AppConstants;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.UiCauseConstants;
import com.ids.idtma.chat.ActivityGroupCall;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.chat.MyVideoView;
import com.ids.idtma.chat.ShowBigVideo;
import com.ids.idtma.config.ProjectConfig;
import com.ids.idtma.entity.CallEntity;
import com.ids.idtma.entity.MeetingMsgData;
import com.ids.idtma.entity.SmsEntity;
import com.ids.idtma.entity.CallEntity.CallType;
import com.ids.idtma.ftp.FtpBuinessLayer.Listener;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.map.IdtMap;
import com.ids.idtma.meeting.ActivityMeetingCall;
import com.ids.idtma.meeting.MeetingNotice;
import com.ids.idtma.util.FileUtil;
import com.ids.idtma.util.ImageUtils;
import com.ids.idtma.util.JsonOperation;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.util.ShowBigImage;
import com.ids.proxy.IDSApiProxyMgr;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SmsDetailAdapter extends BaseAdapter {
	private List<SmsEntity> list = new ArrayList<SmsEntity>();
	private Context context;
	private ClipboardManager cmb;
	public interface Listener {
		public void reCall();
	}

	private Listener mListener;

	public void setListener(Listener listener) {
		this.mListener = listener;
	}
	public SmsDetailAdapter(Context context) {
		cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		this.context = context;
	}

	public void assignment(List<SmsEntity> list) {
		this.list = list;
	}

	public void add(SmsEntity bean) {
		list.add(bean);
	}

	public void remove(int position) {
		list.remove(position);
	}

	public int getCount() {
		return list.size();
	}

	public SmsEntity getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("ResourceAsColor")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final SmsEntity smsBean = list.get(position);
		// LwtLog.d(IdtApplication.WULIN_TAG, "getview:" +
		// smsBean.getCreate_time()
		// + "," + smsBean.getSms_content());
		holder = new ViewHolder();
		convertView = LayoutInflater.from(context).inflate(smsBean.getLayoutID(), parent, false);
		if (smsBean.getSms_resource_type() == 1) {
			// 文字
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
			holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			// 这里是文字内容
			holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.tv.setText(smsBean.getSms_content());
			holder.tv.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					//将文本数据复制到剪贴板
		            ClipData clip = ClipData.newPlainText("copy", smsBean.getSms_content());
		            cmb.setPrimaryClip(clip);
		            Toast.makeText(context, "文本已经复制成功", Toast.LENGTH_SHORT).show();
					return false;
				}
			});
		} else if (smsBean.getSms_resource_type() == 4) {
			// 语音的时候
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
			holder.tv_length = (TextView) convertView.findViewById(R.id.tv_length);
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
			holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
			holder.tv_length.setText(smsBean.getSms_resource_time_length() + "s");
			if (smsBean.getSms_resource_rs_ok() == 1) {
				holder.pb.setVisibility(View.GONE);
			}
			if (smsBean.getSms_type() == 1) {
				// in
				holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
			} else if (smsBean.getSms_type() == 2) {
				// out
				holder.iv.setImageResource(R.drawable.chatto_voice_playing);
			}
			// 点击语音播放按钮
			holder.iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final View the_v = v;
					// TODO Auto-generated method stub
					MediaPlayer mediaPlayer = new MediaPlayer();
					mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mediaPlayer) {
							// TODO Auto-generated method stub
							mediaPlayer.release();
							View view = the_v;
							if (smsBean.getSms_type() == 1) {
								// in
								((ImageView) view).setImageResource(R.drawable.chatfrom_voice_playing);
							} else if (smsBean.getSms_type() == 2) {
								// out
								((ImageView) view).setImageResource(R.drawable.chatto_voice_playing);
							}
						}
					});
					try {
						if (smsBean.getSms_type() == 1) {
							// in
							((ImageView) v).setImageResource(R.anim.voice_from_icon);
						} else if (smsBean.getSms_type() == 2) {
							// out
							((ImageView) v).setImageResource(R.anim.voice_to_icon);
						}
						AnimationDrawable voiceAnimation;
						voiceAnimation = (AnimationDrawable) ((ImageView) v).getDrawable();
						voiceAnimation.start();
						mediaPlayer.setDataSource(smsBean.getSms_resource_url());
						mediaPlayer.prepare();
						mediaPlayer.start();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} else if (smsBean.getSms_resource_type() == 3) {
			// 显示图像
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv = (TextView) convertView.findViewById(R.id.percentage);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv.setImageBitmap(ImageUtils.getInstance().getSmallBitmap(smsBean.getSms_resource_url()));
			if (smsBean.getSms_resource_url() != null && !smsBean.getSms_resource_url().equals("")) {
				holder.iv.setClickable(true);
				holder.iv.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, ShowBigImage.class);
						File file = new File(smsBean.getSms_resource_url());
						if (file.exists()) {
							Uri uri = Uri.fromFile(file);
							intent.putExtra("uri", uri);
							intent.putExtra("url", smsBean.getSms_resource_url());
						}
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
					}
				});
			}
			if (smsBean.getSms_resource_rs_ok() == 1) {
				holder.pb.setVisibility(View.GONE);
			}
		} else if (smsBean.getSms_resource_type() == 5) {
			// 显示录像
			holder.iv = ((ImageView) convertView.findViewById(R.id.chatting_content_iv));
			holder.tv_length = (TextView) convertView.findViewById(R.id.tv_length);
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv = (TextView) convertView.findViewById(R.id.percentage);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
			holder.size = (TextView) convertView.findViewById(R.id.chatting_size_iv);
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.playBtn = (ImageView) convertView.findViewById(R.id.chatting_status_btn);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
			holder.container_status_btn = (LinearLayout) convertView.findViewById(R.id.container_status_btn);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv.setImageBitmap(new ImageUtils().getThumbnail(smsBean.getSms_resource_url()));
			// holder.tv_length.setText(smsBean.getSms_resource_time_length() +
			// "s");
			holder.iv.setClickable(true);
			if (smsBean.getSms_resource_rs_ok() == 1) {
				holder.pb.setVisibility(View.GONE);
			}
			holder.iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String url = smsBean.getSms_resource_url();
					// // 调用系统自带的播放器
					// try {
					// Toast.makeText(context, "我要启动视频了",
					// Toast.LENGTH_SHORT).show();
					// Intent intent = new Intent(Intent.ACTION_VIEW);
					// String strend="";
					// if(url.toLowerCase().endsWith(".mp4")){
					// strend="mp4";
					// }
					// else if(url.toLowerCase().endsWith(".3gp")){
					// strend="3gp";
					// }
					// else if(url.toLowerCase().endsWith(".mov")){
					// strend="mov";
					// }
					// else if(url.toLowerCase().endsWith(".wmv")){
					// strend="wmv";
					// }
					// intent.setDataAndType(Uri.parse(url), "video/"+strend);
					// context.startActivity(intent);
					// } catch (Exception e) {
					// // TODO: handle exception
					// Intent intent = new Intent(context, ShowBigVideo.class);
					// intent.putExtra("url", url);
					// context.startActivity(intent);
					// }
					Intent intent = new Intent(context, MyVideoView.class);
					intent.putExtra("url", url);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}
			});
		} else if (smsBean.getSms_resource_type() == 2) {
			// 显示地图位置
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
			holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			try {
				String sms_content = smsBean.getSms_content();
				String[] contents = sms_content.split(",");
				holder.tv.setText(contents[2]);
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			holder.tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(context, IdtMap.class);
					intent.putExtra("location", smsBean.getSms_content());
					if (smsBean.getSms_type() == 1) {
						// in
						intent.putExtra("user_phone_num", smsBean.getPhone_number());
					} else if (smsBean.getSms_type() == 2) {
						// send
						intent.putExtra("user_phone_num", SharedPreferencesUtil
								.getStringPreference(context.getApplicationContext(), "phone_number", ""));
					}
					try {
						String sms_content = smsBean.getSms_content();
						String[] contents = sms_content.split(",");
						intent.putExtra("longitude", contents[1]);
						intent.putExtra("latitude", contents[0]);
					} catch (Exception e) {
						// TODO: handle exception
					}
					intent.putExtra("from", "IdtChatActivity");
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}
			});
			
		} else if (smsBean.getSms_resource_type() == 6) {
			// 文件传输
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.tv_file_name = (TextView) convertView.findViewById(R.id.tv_file_name);
			holder.tv_file_size = (TextView) convertView.findViewById(R.id.tv_file_size);
			holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
			holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
			holder.tv_file_download_state = (TextView) convertView.findViewById(R.id.tv_file_state);
			holder.ll_container = (LinearLayout) convertView.findViewById(R.id.ll_file_container);
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			// 这里是进度值
			holder.tv = (TextView) convertView.findViewById(R.id.percentage);
			holder.tv_file_name.setText(smsBean.getSms_resource_name());
			if (smsBean.getSms_resource_rs_ok() == 1) {
				holder.pb.setVisibility(View.GONE);
			}
			File file = new File(smsBean.getSms_resource_url());
			if (file != null && file.exists()) {
				holder.tv_file_size.setText(file.length() / 1024 + "K");
			}
			holder.ll_container.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						File file = new File(smsBean.getSms_resource_url());
						if (file != null && file.exists()) {
							// 文件存在，直接打开
							Intent intent = FileUtil.openFile(smsBean.getSms_resource_url());
							context.startActivity(intent);
						}
					} catch (Exception e) {
						// TODO: handle exception
						Toast.makeText(context, "不能打开这个文件", Toast.LENGTH_SHORT).show();
					}
				}
			});
		} else if (smsBean.getSms_resource_type() == 7) {
			// 语音单呼
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			if (smsBean.getSms_type() == 1) {
				// in
				if(ProjectConfig.UI_CAUSE_SHOW==true){
					holder.tv.setText("语音电话呼入("+new UiCauseConstants().getDataType(smsBean.getUiCause())+")"+"\n"+(smsBean.getSms_resource_time_length()==0 ? "未接通":("通话时长:"+smsBean.getSms_resource_time_length()+"秒")));
				}else{
					holder.tv.setText("语音电话呼入"+"\n"+(smsBean.getSms_resource_time_length()==0 ? "未接通":("通话时长:"+smsBean.getSms_resource_time_length()+"秒")));
				}
			} else if (smsBean.getSms_type() == 2) {
				// send
				if(ProjectConfig.UI_CAUSE_SHOW==true){
					holder.tv.setText("语音电话呼出("+new UiCauseConstants().getDataType(smsBean.getUiCause())+")"+"\n"+(smsBean.getSms_resource_time_length()==0 ? "未接通":("通话时长:"+smsBean.getSms_resource_time_length()+"秒")));
				}else{
					holder.tv.setText("语音电话呼出"+"\n"+(smsBean.getSms_resource_time_length()==0 ? "未接通":("通话时长:"+smsBean.getSms_resource_time_length()+"秒")));
				}
			}
			holder.tv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mListener.reCall();
				}
			});
		} else if (smsBean.getSms_resource_type() == 8) {
			// 语音单呼视频
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			if (smsBean.getSms_type() == 1) {
				// in
				if(ProjectConfig.UI_CAUSE_SHOW==true){
					holder.tv.setText("视频电话呼入("+new UiCauseConstants().getDataType(smsBean.getUiCause())+")");
				}else{
					holder.tv.setText("视频电话呼入");
				}
			} else if (smsBean.getSms_type() == 2) {
				// send
				holder.tv.setText("视频电话呼出");
				if(ProjectConfig.UI_CAUSE_SHOW==true){
					holder.tv.setText("视频电话呼出("+new UiCauseConstants().getDataType(smsBean.getUiCause())+")");
				}else{
					holder.tv.setText("视频电话呼出");
				}
			}
		} else if (smsBean.getSms_resource_type() == 9) {
			// 语音单呼视频
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			if (smsBean.getSms_type() == 1) {
				// in
				if(ProjectConfig.UI_CAUSE_SHOW==true){
					holder.tv.setText("语音群呼呼入("+new UiCauseConstants().getDataType(smsBean.getUiCause())+")");
				}else{
					holder.tv.setText("语音群呼呼入");
				}
			} else if (smsBean.getSms_type() == 2) {
				// send
				if(ProjectConfig.UI_CAUSE_SHOW==true){
					holder.tv.setText("语音群呼呼出("+new UiCauseConstants().getDataType(smsBean.getUiCause())+")");
				}else{
					holder.tv.setText("语音群呼呼出");
				}
			}
		} else if (smsBean.getSms_resource_type() == 17) {
			// 会议
			holder.timeLength = (TextView) convertView.findViewById(R.id.timestamp);
			holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
			holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
			holder.iv_call_icon = (ImageView) convertView.findViewById(R.id.iv_call_icon);
			String sms_content = smsBean.getSms_content();
			JsonOperation jsonOperation = new JsonOperation();
			final MeetingMsgData meetingMsgData = jsonOperation.meetingJsonStringParse(sms_content);
			if (meetingMsgData.type == JsonOperation.METTING_NOTICE) {
				holder.tv.setText("预约会议通知");
				holder.iv_call_icon.setImageResource(R.drawable.new_ui_notice);
			} else if (meetingMsgData.type == JsonOperation.METTING_LINK) {
				holder.tv.setText("会议开始链接");
				holder.iv_call_icon.setImageResource(R.drawable.new_ui_meeting_link_button);
			} else if (meetingMsgData.type == JsonOperation.METTING_REPLY) {
				holder.tv.setText(meetingMsgData.getReason());
				holder.tv.setTextColor(R.color.new_ui_meeting_reply_text_color);
			}
			// 点击会议通知，显示你接受还是拒绝相关信息界面
			holder.tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (meetingMsgData.type == JsonOperation.METTING_NOTICE) {
						// 跳转到会议的接受和拒绝界面上
						Intent intent = new Intent(context, MeetingNotice.class);
						intent.putExtra("meeting_msg_data", meetingMsgData);
						//target_phone_number是组号
						intent.putExtra("callto_group_num", smsBean.getTarget_phone_number());
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
					} else if (meetingMsgData.type == JsonOperation.METTING_LINK) {
						// 跳转到会议主界面上
						// 是不是正在拨打电话
						if (IdtApplication.resumeCurrentCall()) {
							((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
									.cancel(AppConstants.CALL_NOTIFICATION_ID);
							context.startActivity(IdtApplication.getCurrentCall().getIntent());
							return;
						}
						// 没有锁定的群组就直接拨打
						String callNum = AppConstants.getLockedGroupNum().equals("") ? smsBean.getTarget_phone_number()
								: AppConstants.getLockedGroupNum();
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
						Intent intent = new Intent(context, ActivityMeetingCall.class);
						// 对方号码
						intent.putExtra(AppConstants.EXTRA_KEY_CALLEE, callNum);
						intent.putExtra(AppConstants.EXTRA_KEY_GROUP_CALL_NUM, callNum);
						intent.putExtra(AppConstants.EXTRA_KEY_CALLER, SharedPreferencesUtil.getStringPreference(context, "phone_number", ""));
						intent.putExtra(AppConstants.EXTRA_KEY_CALL_STATUS, ActivityMeetingCall.FLAG_CALLING);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
					}
				}
			});
		}
		if(smsBean.getPhone_number().equals("-10000")){
			holder.tv_userId.setText(smsBean.getTarget_phone_number());
		}else{
			holder.tv_userId.setText(smsBean.getPhone_number());
		}
		Date new_date = null;
		Date last_date = null;
		long between_mscound = 0;
		if (position != 0) {
			try {
				new_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(smsBean.getCreate_time());
				last_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(list.get(position - 1).getCreate_time());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			between_mscound = new_date.getTime() - last_date.getTime();
			// 超过5分钟，显示
			if ((between_mscound / (1000 * 60)) > 5) {
				holder.timeLength.setText(smsBean.getCreate_time());
			} else {
				holder.timeLength.setVisibility(View.GONE);
			}
		} else {
			// 第一次，显示
			holder.timeLength.setText(smsBean.getCreate_time());
		}
		convertView.setTag(holder);
		return convertView;
	}

	public static class ViewHolder {
		ImageView iv;
		TextView tv;
		// 资源有多少秒
		TextView tv_length;
		ProgressBar pb;
		ImageView staus_iv;
		ImageView head_iv;
		// 发信息的人
		TextView tv_userId;
		// 语音点击以后，进行播放功能图标
		ImageView playBtn;
		// 发表时间
		TextView timeLength;
		TextView size;
		LinearLayout container_status_btn;
		LinearLayout ll_container;
		ImageView iv_read_status;
		// 显示已读回执状态
		TextView tv_ack;
		// 显示送达回执状态
		TextView tv_delivered;
		TextView tv_file_name;
		TextView tv_file_size;
		TextView tv_file_download_state;

		// 会议时用
		ImageView iv_call_icon;
	}
}
