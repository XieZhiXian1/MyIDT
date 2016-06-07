package com.ids.idtma.frame;

import java.util.List;

import com.ids.idtma.IdtLogin;
import com.ids.idtma.R;
import com.ids.idtma.database.LoginIP;
import com.ids.idtma.database.LoginUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

@SuppressLint("InflateParams")
public class IdtLoginPopupWindow {
	private Listener mListener;

	public interface Listener {
		public void popupWindowDismiss(String selectItem, int current_mode);
		public void deleteARecord(int index);
		public void deleteAUserRecord(int index);
	}

	public void setListener(Listener listener) {
		this.mListener = listener;
	}
	
	public IdtLoginPopupWindow() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PopupWindow initPopuWindow(View parent, Context context, List<LoginIP> loginIPs, List<LoginUser> loginUsers,
			int current_mode) {
		final Context local_context = context;
		PopupWindow adt_popuWindow = null;
		View contentView1;
		if (adt_popuWindow == null) {
			LayoutInflater mLayoutInflater = LayoutInflater.from(local_context);
			contentView1 = mLayoutInflater.inflate(R.layout.idt_popuwindow, null);
			adt_popuWindow = new PopupWindow(contentView1, ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		ColorDrawable cd = new ColorDrawable(0x000000);
		adt_popuWindow.setBackgroundDrawable(cd);
		// 产生背景变暗效果
		WindowManager.LayoutParams lp = ((Activity) local_context).getWindow().getAttributes();
		lp.alpha = 0.4f;
		((Activity) local_context).getWindow().setAttributes(lp);

		adt_popuWindow.setOutsideTouchable(true);
		adt_popuWindow.setFocusable(true);
		adt_popuWindow.showAsDropDown(parent);
		adt_popuWindow.update();
		addPopupWindowView(adt_popuWindow, loginIPs, loginUsers, local_context, current_mode);
		adt_popuWindow.setOnDismissListener(new OnDismissListener() {
			// 在dismiss中恢复透明度
			public void onDismiss() {
				WindowManager.LayoutParams lp = ((Activity) local_context).getWindow().getAttributes();
				lp.alpha = 1f;
				((Activity) local_context).getWindow().setAttributes(lp);
			}
		});

		return adt_popuWindow;
	}

	public void addPopupWindowView(final PopupWindow popupWindow, final List<LoginIP> loginIPs,
			final List<LoginUser> loginUsers, Context context, final int current_mode) {
		final Context local_context = context;
		LinearLayout adt_popupwidow_layout = (LinearLayout) popupWindow.getContentView()
				.findViewById(R.id.adt_popupwidow_layout);
		// ip输入框
		if (current_mode == IdtLogin.INIT_POPUPWINDOW_FROM_IP_EDITVIEW) {
			if(loginIPs!=null){
				for (int i = 0; i < loginIPs.size(); i++) {
					final int index = i;
					View content_view = LayoutInflater.from(local_context).inflate(R.layout.idt_login_popupwidow_item,
							null);
					content_view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							popupWindow.dismiss();
							mListener.popupWindowDismiss(loginIPs.get(index).getIp_address(), current_mode);
						}
					});
					((android.widget.TextView) (content_view).findViewById(R.id.adt_login_popupwidown_item_textview))
							.setText(loginIPs.get(index).ip_custom_name);
					((content_view).findViewById(R.id.adt_login_popupwidown_item_delete_button)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							popupWindow.dismiss();
							mListener.deleteARecord(index);
						}
					});
					adt_popupwidow_layout.addView(content_view);
					if (index != (loginIPs.size() - 1)) {
						View line_view = LayoutInflater.from(local_context).inflate(R.layout.idt_line_view, null);
						adt_popupwidow_layout.addView(line_view);
					}
				}
			}
		} else if (current_mode == IdtLogin.INIT_POPUPWINDOW_FROM_USERNAME_EDITVIEW) {
			// 用户名输入框
			if(loginUsers != null){
				for (int i = 0; i < loginUsers.size(); i++) {
					final int index = i;
					View content_view = LayoutInflater.from(local_context).inflate(R.layout.idt_login_popupwidow_item,
							null);
					content_view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							popupWindow.dismiss();
							mListener.popupWindowDismiss(loginUsers.get(index).getUserphone(), current_mode);
						}
					});
					((android.widget.TextView) (content_view).findViewById(R.id.adt_login_popupwidown_item_textview))
							.setText(loginUsers.get(index).getUserphone());
					((content_view).findViewById(R.id.adt_login_popupwidown_item_delete_button)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							popupWindow.dismiss();
							mListener.deleteAUserRecord(index);
						}
					});
					adt_popupwidow_layout.addView(content_view);
					if (index != (loginUsers.size() - 1)) {
						View line_view = LayoutInflater.from(local_context).inflate(R.layout.idt_line_view, null);
						adt_popupwidow_layout.addView(line_view);
					}
				}
			}
		}
	}
}
