package com.ids.idtma.adapter;

import java.util.List;

import com.baidu.lbsapi.auth.g;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.chat.IdtChatActivity;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.SharedPreferencesUtil;
import com.ids.idtma.voicerecord.TalkNetManager.Listener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IdtMapExpandableListViewAdapter extends BaseExpandableListAdapter {
	private final Activity context;
	private LayoutInflater inflater;
	private IdtApplication idtApplication;
	public interface Listener {
		public void someOperation(String ucNum);
	}

	private Listener mListener;

	public void setListener(Listener listener) {
		this.mListener = listener;
	}
	public IdtMapExpandableListViewAdapter(Activity context) {
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

	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean arg2, View convertView,
			final ViewGroup arg4) {
		final MemberHolder memberHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.idt_map_listview_child_item, null);
			memberHolder = new MemberHolder();
			memberHolder.avatar=(ImageView) convertView.findViewById(R.id.buddy_listview_child_avatar);
			memberHolder.memberPhone = (TextView) convertView.findViewById(R.id.buddy_listview_child_nick);
			memberHolder.checkBox = (CheckBox) convertView.findViewById(R.id.idt_map_checkbox);
			convertView.setTag(memberHolder);
		} else {
			memberHolder = (MemberHolder) convertView.getTag();
		}
		if(idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
				.get(childPosition).getChecked()==true){
			memberHolder.checkBox.setChecked(true);
		}else{
			memberHolder.checkBox.setChecked(false);
		}
		if(idtApplication.getMapUserGroup()
				.get(idtApplication.getLstGroups().get(groupPosition).getUcNum()).get(childPosition).isOnline==true){
			memberHolder.avatar.setImageResource(R.drawable.new_ui_map_singer_member_p);
		}else{
			memberHolder.avatar.setImageResource(R.drawable.new_ui_map_singer_member_p_off);
		}
//		memberHolder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// TODO Auto-generated method stub
//				if (isChecked == true) {
//					idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
//							.get(childPosition).setChecked(true);
//					notifyDataSetChanged();
//				} else {
//					idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
//							.get(childPosition).setChecked(false);
//					notifyDataSetChanged();
//				}
//			}
//		});
		memberHolder.checkBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(memberHolder.checkBox.isChecked()==true){
					LwtLog.d("wulin_map", "-------------------------------------");
					idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
							.get(childPosition).setChecked(true);
					mListener.someOperation(idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
							.get(childPosition).getUcNum());
				}else{
					idtApplication.getMapUserGroup().get(idtApplication.getLstGroups().get(groupPosition).getUcNum())
					.get(childPosition).setChecked(false);
				}
				mListener.someOperation("#");
			}
		});
		memberHolder.memberPhone.setText(idtApplication.getMapUserGroup()
				.get(idtApplication.getLstGroups().get(groupPosition).getUcNum()).get(childPosition).getUcNum());
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
		final GroupHolder groupHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.idt_map_listview_group_item, null);
			groupHolder = new GroupHolder();
			groupHolder.groupPhone = (TextView) convertView.findViewById(R.id.idt_listview_group_name);
			groupHolder.checkBox = (CheckBox) convertView.findViewById(R.id.idt_checkbox);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupHolder) convertView.getTag();
		}

		groupHolder.groupPhone.setText(idtApplication.getLstGroups().get(groupPosition).getUcNum());
		if(idtApplication.getLstGroups().get(groupPosition).getChecked()==true){
			groupHolder.checkBox.setChecked(true);
		}else{
			groupHolder.checkBox.setChecked(false);
		}
//		groupHolder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// TODO Auto-generated method stub
//				if (isChecked == true) {
//					idtApplication.getLstGroups().get(groupPosition).setChecked(true);
//					List<GroupMember> lGroupMembers = idtApplication.getMapUserGroup()
//							.get(idtApplication.getLstGroups().get(groupPosition).getUcNum());
//					for (int i = 0; i < lGroupMembers.size(); i++) {
//						lGroupMembers.get(i).setChecked(true);
//					}
//					notifyDataSetChanged();
//				} else {
//					idtApplication.getLstGroups().get(groupPosition).setChecked(false);
//					List<GroupMember> lGroupMembers = idtApplication.getMapUserGroup()
//							.get(idtApplication.getLstGroups().get(groupPosition).getUcNum());
//					for (int i = 0; i < lGroupMembers.size(); i++) {
//						lGroupMembers.get(i).setChecked(false);
//					}
//					notifyDataSetChanged();
//				}
//				mListener.someOperation("#");
//			}
//		});
		
		groupHolder.checkBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(groupHolder.checkBox.isChecked()==true){
					//点击选择了
					idtApplication.getLstGroups().get(groupPosition).setChecked(true);
					List<GroupMember> lGroupMembers = idtApplication.getMapUserGroup()
							.get(idtApplication.getLstGroups().get(groupPosition).getUcNum());
					for (int i = 0; i < lGroupMembers.size(); i++) {
						lGroupMembers.get(i).setChecked(true);
					}
					notifyDataSetChanged();
				}else{
					//最开始群组没有被选择
					idtApplication.getLstGroups().get(groupPosition).setChecked(false);
					List<GroupMember> lGroupMembers = idtApplication.getMapUserGroup()
							.get(idtApplication.getLstGroups().get(groupPosition).getUcNum());
					for (int i = 0; i < lGroupMembers.size(); i++) {
						lGroupMembers.get(i).setChecked(false);
					}
					notifyDataSetChanged();
				}
				mListener.someOperation("#");
			}
		});
		return convertView;
	}

	class GroupHolder {
		// 群组图片
		public ImageView groupImage;
		// 群组名
		public TextView groupName;
		// 群组电话
		public TextView groupPhone;
		public CheckBox checkBox;
	}

	class MemberHolder {
		//单用户头像
		public ImageView avatar;
		// 单用户名字
		public TextView memberName;
		// 单用户号码
		public TextView memberPhone;
		public CheckBox checkBox;
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