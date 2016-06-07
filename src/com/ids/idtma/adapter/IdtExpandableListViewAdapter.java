package com.ids.idtma.adapter;

import java.util.List;

import com.ids.idtma.IdtApplication;
import com.ids.idtma.IdtGroup;
import com.ids.idtma.R;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.util.CustomDialog;
import com.ids.idtma.util.SharedPreferencesUtil;

import android.R.style;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IdtExpandableListViewAdapter extends BaseExpandableListAdapter {
	private final Activity context;
	private LayoutInflater inflater;
	private IdtApplication idtApplication;
	public static final int TO_LOCK=1;
	public static final int TO_UNLOCK=2;
	public static String CURRENT_OPERATION_GROUP_NUM="";

	public IdtExpandableListViewAdapter(Activity context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		idtApplication = (IdtApplication) context.getApplication();
	}

	@Override
	public Object getChild(final int groupPosition, final int childPosition) {
		return idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
				.get(childPosition);
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return childPosition;
	}

	private void showNoticeDialog(int type) {
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle("温馨提醒");
		if(type==TO_LOCK){
			customBuilder.setMessage("当您锁定该组以后，您将不能接收到其它组的组呼，并且会取消对其它组的锁定，您确定锁定吗？");
			customBuilder.setPositiveButton("确认", lockListener);
			customBuilder.setNegativeButton("取消", lockListener);
		}else if(type==TO_UNLOCK){
			customBuilder.setMessage("当您解除锁定以后，您将可以接收您所有组的组呼，您确定解除锁定吗？");
			customBuilder.setPositiveButton("确认", unlockListener);
			customBuilder.setNegativeButton("取消", unlockListener);
		}
		Dialog dialog = customBuilder.create();
		dialog.show();
	}

	DialogInterface.OnClickListener lockListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				okToLock();
				dialog.dismiss();
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				dialog.dismiss();
				break;
			default:
				break;
			}
		}
	};
	
	DialogInterface.OnClickListener unlockListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				okToUnLock();
				dialog.dismiss();
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				dialog.dismiss();
				break;
			default:
				break;
			}
		}
	};

	private void okToLock(){
		SharedPreferencesUtil.setStringPreferences(context, "lock_group_num",CURRENT_OPERATION_GROUP_NUM);
		Toast.makeText(context, "您完成了对"+CURRENT_OPERATION_GROUP_NUM+"的锁定", Toast.LENGTH_SHORT).show();
		notifyDataSetChanged();
	}
	
	private void okToUnLock(){
		SharedPreferencesUtil.setStringPreferences(context, "lock_group_num","#" );
		Toast.makeText(context, "您解除了对"+CURRENT_OPERATION_GROUP_NUM+"的锁定", Toast.LENGTH_SHORT).show();
		notifyDataSetChanged();
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean arg2, View convertView,
			final ViewGroup arg4) {
		MemberHolder memberHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.new_ui_idt_group_listview_child_item, null);
			memberHolder = new MemberHolder();
			memberHolder.avatar = (ImageView) convertView.findViewById(R.id.buddy_listview_child_avatar);
			memberHolder.memberPhone = (TextView) convertView.findViewById(R.id.buddy_listview_child_nick);
			memberHolder.online = (TextView) convertView.findViewById(R.id.idt_listview_user_online_or_not);
			memberHolder.toRichText = (ImageView) convertView.findViewById(R.id.idt_to_user_message_image);
			convertView.setTag(memberHolder);
		} else {
			memberHolder = (MemberHolder) convertView.getTag();
		}
		// if
		// (idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
		// .get(childPosition).getUcNum().equals(SharedPreferencesUtil
		// .getStringPreference(context.getApplicationContext(), "phone_number",
		// ""))) {
		// convertView.setBackgroundResource(R.color.gray_70);
		// }
		if (idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
				.get(childPosition).isOnline == true) {
			memberHolder.avatar.setImageResource(R.drawable.new_ui_default_user_not_normal_image);
		} else {
			memberHolder.avatar.setImageResource(R.drawable.new_ui_off_line);
		}
		memberHolder.memberPhone.setText(idtApplication.getMapUserGroup()
				.get(idtApplication.getLstGroups().get(groupPosition).getUcNum()).get(childPosition).getUcNum());
		memberHolder.toRichText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
						.get(childPosition).getUcNum().equals(SharedPreferencesUtil
								.getStringPreference(context.getApplicationContext(), "phone_number", ""))) {
					Toast.makeText(context, "您不需要和自己进行通信", Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(context, IdtChatActivity.class);
					intent.putExtra("to_where", IdtGroup.TO_PERSION);
					intent.putExtra("callto_persion_num",
							idtApplication.getMapUserGroup()
									.get(idtApplication.getLstGroups().get(groupPosition).getUcNum()).get(childPosition)
									.getUcNum());
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		try {
			return idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
					.size();
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}

	@Override
	public Object getGroup(final int groupPosition) {
		if (idtApplication.getLstGroups().isEmpty() || idtApplication.getLstGroups().size() < groupPosition)
			return null;
		return idtApplication.getLstGroups().get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return idtApplication.getLstGroups().size();
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView,
			final ViewGroup arg3) {
		GroupHolder groupHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.new_ui_idt_group_listview_group_item, null);
			groupHolder = new GroupHolder();
			groupHolder.idt_group_fold_arrow_image = (ImageView) convertView.findViewById(R.id.idt_group_fold_arrow_image);
			groupHolder.groupPhone = (TextView) convertView.findViewById(R.id.idt_listview_group_name);
			groupHolder.iv_lock = (ImageView) convertView.findViewById(R.id.idt_lock_image);
			groupHolder.online_persion_num = (TextView) convertView.findViewById(R.id.idt_listview_group_num);
			groupHolder.toRichText = (ImageView) convertView.findViewById(R.id.idt_to_group_message_image);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupHolder) convertView.getTag();
		}
        final String lock_group_num=SharedPreferencesUtil.getStringPreference(context, "lock_group_num", "#");
        if(lock_group_num.equals(idtApplication.getLstGroups().get(groupPosition).getUcNum())){
        	groupHolder.iv_lock.setImageResource(R.drawable.new_ui_idt_unlock);
        }else{
        	groupHolder.iv_lock.setImageResource(R.drawable.new_ui_idt_lock);
        }
		groupHolder.groupPhone.setText(idtApplication.getLstGroups().get(groupPosition).getUcNum());
		groupHolder.toRichText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, IdtChatActivity.class);
				intent.putExtra("to_where",IdtGroup.TO_GROUP);
				//intent.putExtra("callto_persion_info", idtApplication.getLstGroups().get(groupPosition));
				intent.putExtra("callto_group_num", idtApplication.getLstGroups().get(groupPosition).getUcNum());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			}
		});
		groupHolder.iv_lock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CURRENT_OPERATION_GROUP_NUM = idtApplication.getLstGroups().get(groupPosition).getUcNum();
				if(lock_group_num.equals(idtApplication.getLstGroups().get(groupPosition).getUcNum())){
					if(IdtApplication.getCurrentCall()!=null){
						Toast.makeText(context, "通信结束以后才能对锁进行操作", Toast.LENGTH_SHORT).show();
						return;
					}
					//本群之前已经被锁定了
					showNoticeDialog(TO_UNLOCK);
				}else{
					if(IdtApplication.getCurrentCall()!=null){
						Toast.makeText(context, "通信结束以后才能对锁进行操作", Toast.LENGTH_SHORT).show();
						return;
					}
					//本群之前没有被锁定
					showNoticeDialog(TO_LOCK);
				}
			}
		});
		if(idtApplication.getLstGroups().get(groupPosition).getFocused()==true){
			groupHolder.idt_group_fold_arrow_image.setImageResource(R.drawable.new_ui_group_page_arrow_open);
		}else{
			groupHolder.idt_group_fold_arrow_image.setImageResource(R.drawable.new_ui_group_page_arrow_close);
		}
		//在线人数进行设定
		String ucNum = idtApplication.getLstGroups().get(groupPosition).getUcNum();
		int onLineNum=0;
		int allNum= 0;
		onLineNum = onLineGroupMemberNum(ucNum);
		allNum = getGroupAllNum(ucNum);
		groupHolder.online_persion_num.setText("("+onLineNum+"/"+allNum+")");
		return convertView;
	}
	
	private int onLineGroupMemberNum(String ucNum){
		int onLineNum=0;
		try {
			List<GroupMember> lGroupMembers = idtApplication.getMapUserGroup().get(ucNum);
			for(int i=0;i<lGroupMembers.size();i++){
				if(lGroupMembers.get(i).isOnline){
					onLineNum++;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return onLineNum;
	}
	
	private int getGroupAllNum(String ucNum){
		List<GroupMember> lGroupMembers=null;
		try {
			lGroupMembers= idtApplication.getMapUserGroup().get(ucNum);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(lGroupMembers!=null){
			return lGroupMembers.size();
		}else{
			return 0;
		}
	}

	class GroupHolder {
		// 群组图片
		public ImageView groupImage;
		// 箭头
		public ImageView idt_group_fold_arrow_image;
		// 群组名
		public TextView groupName;
		// 群组电话
		public TextView groupPhone;
		// 是否锁定图标
		public ImageView iv_lock;
		// 在线的人数
		public TextView online_persion_num;
		// 跳转到富文本图标
		public ImageView toRichText;
	}

	class MemberHolder {
		// 单用户头像
		public ImageView avatar;
		// 单用户名字
		public TextView memberName;
		// 单用户号码
		public TextView memberPhone;
		// 在线与否
		public TextView online;
		// 跳转到富文本图标
		public ImageView toRichText;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	// 子选项是否可以选择
	@Override
	public boolean isChildSelectable(final int arg0, final int arg1) {
		// TODO Auto-generated method stub
		return true;
	}
}