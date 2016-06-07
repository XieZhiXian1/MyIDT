package com.ids.idtma.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ids.idtma.ActivityBase;
import com.ids.idtma.IdtApplication;
import com.ids.idtma.R;
import com.ids.idtma.entity.SmsEntity;
import com.ids.idtma.jni.aidl.GroupMember;
import com.ids.idtma.provider.SmsConversationProvider;
import com.ids.idtma.util.DateUtil;
import com.ids.idtma.util.LwtLog;
import com.ids.idtma.util.StringsUtils;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IdtMessageListviewAdapter extends BaseAdapter {

	private Context context;

	/**
	 * 测试数据 private List<Map<String, String>> testListMaps = new
	 * ArrayList<Map<String, String>>(); // private void testData() { //
	 * Map<String, String> map1 = new HashMap<String, String>(); //
	 * map1.put("id", "2050"); // map1.put("time", "2016年3月17日 15:12"); //
	 * map1.put("message", "今天晚上一起打篮球吗？"); // testListMaps.add(map1); // //
	 * Map<String, String> map2 = new HashMap<String, String>(); //
	 * map2.put("id", "2051"); // map2.put("time", "2016年3月17日 15:22"); //
	 * map2.put("message", "今天晚上一起打羽毛球吗？"); // testListMaps.add(map2); // // }
	 *
	 *
	 */
	private List<SmsEntity> lSmsEntities = new ArrayList<SmsEntity>();

	public void assignment(List<SmsEntity> list) {
		this.lSmsEntities = list;
	}

	public IdtMessageListviewAdapter(Context context) {
		super();
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lSmsEntities.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lSmsEntities.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public boolean isGroupMember(String num){
		boolean isGroupMember=false;
		 List<GroupMember> lGroupMembers = ((IdtApplication)context.getApplicationContext()).getLstGroups();
		 for(int i=0;i<lGroupMembers.size();i++){
			if(num.equals(lGroupMembers.get(i).getUcNum())){
				isGroupMember=true;
				break;
			}
		}
		return isGroupMember;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		IdtMessageListviewHolder idtMessageListviewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.new_ui_idt_message_listview_item, parent,
					false);
			idtMessageListviewHolder = new IdtMessageListviewHolder();
			idtMessageListviewHolder.imageView= (ImageView) convertView
					.findViewById(R.id.head);
			idtMessageListviewHolder.phone_num = (TextView) convertView
					.findViewById(R.id.idt_message_listview_item_phone_num);
			idtMessageListviewHolder.time = (TextView) convertView.findViewById(R.id.idt_message_listview_item_time);
			idtMessageListviewHolder.message = (TextView) convertView
					.findViewById(R.id.idt_message_listview_item_message);
			idtMessageListviewHolder.idt_exist_new_message_image = (ImageView) convertView
					.findViewById(R.id.idt_exist_new_message_image);
		} else {
			idtMessageListviewHolder = (IdtMessageListviewHolder) convertView.getTag();
		}
		if (lSmsEntities.get(position).getSms_type() == 1) {
			// in
			if(isGroupMember(lSmsEntities.get(position).getTarget_phone_number())){
				//组
				idtMessageListviewHolder.imageView.setImageResource(R.drawable.new_ui_message_group_image);
				idtMessageListviewHolder.phone_num.setText(lSmsEntities.get(position).getTarget_phone_number());
			}else{
				idtMessageListviewHolder.imageView.setImageResource(R.drawable.new_ui_message_person_image);
				idtMessageListviewHolder.phone_num.setText(lSmsEntities.get(position).getPhone_number());
			}
		} else {
			// out or other
			idtMessageListviewHolder.phone_num.setText(lSmsEntities.get(position).getTarget_phone_number());
			if(isGroupMember(lSmsEntities.get(position).getTarget_phone_number())){
				//组
				idtMessageListviewHolder.imageView.setImageResource(R.drawable.new_ui_message_group_image);
			}else{
				idtMessageListviewHolder.imageView.setImageResource(R.drawable.new_ui_message_person_image);
			}
		}
		idtMessageListviewHolder.time.setText(lSmsEntities.get(position).getCreate_time());
		if (lSmsEntities.get(position).getSms_resource_type() == 1) {
			idtMessageListviewHolder.message.setText((StringsUtils.replaceBlank(lSmsEntities.get(position).getSms_content())));
		} else if (lSmsEntities.get(position).getSms_resource_type() == 2) {
			idtMessageListviewHolder.message.setText("位置信息");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 3) {
			idtMessageListviewHolder.message.setText("图片信息");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 4) {
			idtMessageListviewHolder.message.setText("语音文件信息");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 5) {
			idtMessageListviewHolder.message.setText("视频录像信息");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 6) {
			idtMessageListviewHolder.message.setText("文件信息");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 7) {
			idtMessageListviewHolder.message.setText("单呼语音");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 8) {
			idtMessageListviewHolder.message.setText("单呼视频");
		} else if (lSmsEntities.get(position).getSms_resource_type() == 9) {
			idtMessageListviewHolder.message.setText("群呼语音");
		}

		if (lSmsEntities.get(position).getRead() == 0) {
			// 未读
			idtMessageListviewHolder.idt_exist_new_message_image.setVisibility(View.VISIBLE);
		} else if (lSmsEntities.get(position).getRead() == 1) {
			// 已经读取
			idtMessageListviewHolder.idt_exist_new_message_image.setVisibility(View.GONE);
		}
		convertView.setTag(idtMessageListviewHolder);
		return convertView;
	}

	public final class IdtMessageListviewHolder {
		public ImageView imageView;
		public TextView phone_num;
		public TextView time;
		public TextView message;
		public ImageView idt_exist_new_message_image;
	}

}
