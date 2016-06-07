package com.ids.idtma;

import com.ids.proxy.IDSApiProxyMgr;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.Toast;

/**
 * 主页
 * 
 * @author yangwulin
 *
 */

@SuppressWarnings("deprecation")
public class IdtMainActivity extends TabActivity implements OnCheckedChangeListener {
	private TabHost tabHost;
	private RadioGroup radioderGroup;
	private int defaultOpenId = R.id.adt_mainTabs_radio_group;
	private RadioButton radioButton1, radioButton2, radioButton3,radioButton4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idt_activity_main);
		// 将activity加到集合中
		IdtApplication.getInstance().addActivity(this);
		tabHost = this.getTabHost();
		tabHost.addTab(tabHost.newTabSpec("1").setIndicator("1").setContent(new Intent(this, IdtGroup.class)));
		tabHost.addTab(tabHost.newTabSpec("2").setIndicator("2").setContent(new Intent(this, IdtMessage.class)));
		tabHost.addTab(tabHost.newTabSpec("3").setIndicator("3").setContent(new Intent(this, IdtWork.class)));
		tabHost.addTab(tabHost.newTabSpec("4").setIndicator("4").setContent(new Intent(this, IdtSetting.class)));
		radioderGroup = (RadioGroup) findViewById(R.id.adt_main_radio);
		radioderGroup.setOnCheckedChangeListener(this);
		// 设置底部导航当前默认的状态（默认为辅具商城）
		radioderGroup.check(defaultOpenId);
		radioButton1 = (RadioButton) findViewById(R.id.adt_mainTabs_radio_group);
		radioButton2 = (RadioButton) findViewById(R.id.adt_mainTabs_radio_message);
		radioButton3 = (RadioButton) findViewById(R.id.adt_mainTabs_radio_work);
		radioButton4 = (RadioButton) findViewById(R.id.adt_mainTabs_radio_setting);
		setDrawableTopSize();
	}

	private void setDrawableTopSize() {
		//群组tab
		Drawable drawable = getResources().getDrawable(R.drawable.new_ui_group);
		// 第一0是距左边距离，第二0是距上边距离，后面分别为长和宽
		drawable.setBounds(0, 0, 55, 45);
		radioButton1.setCompoundDrawables(null, drawable, null, null);
		
		//消息tab
		drawable = getResources().getDrawable(R.drawable.new_ui_message);
		// 第一0是距左边距离，第二0是距上边距离，后面分别为长和宽
		drawable.setBounds(0, 0, 55, 45);
		radioButton2.setCompoundDrawables(null, drawable, null, null);
		
		//工作tab
		drawable = getResources().getDrawable(R.drawable.new_ui_work);
		// 第一0是距左边距离，第二0是距上边距离，后面分别为长和宽
		drawable.setBounds(0, 0, 55, 45);
		radioButton3.setCompoundDrawables(null, drawable, null, null);
		
		//设置tab
		drawable = getResources().getDrawable(R.drawable.new_ui_main_setting);
		// 第一0是距左边距离，第二0是距上边距离，后面分别为长和宽
		drawable.setBounds(0, 0, 55, 45);
		radioButton4.setCompoundDrawables(null, drawable, null, null);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.adt_mainTabs_radio_group:
			tabHost.setCurrentTabByTag("1");
			break;
		case R.id.adt_mainTabs_radio_message:
			tabHost.setCurrentTabByTag("2");
			break;
		case R.id.adt_mainTabs_radio_work:
			tabHost.setCurrentTabByTag("3");
			break;
		case R.id.adt_mainTabs_radio_setting:
			tabHost.setCurrentTabByTag("4");
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		IdtApplication.getInstance().deleteActivity(this);// 将activity从集合移除
	}
}
