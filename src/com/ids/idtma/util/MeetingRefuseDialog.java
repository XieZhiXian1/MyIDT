package com.ids.idtma.util;

import com.ids.idtma.R;
import com.ids.idtma.ftp.FtpBuinessLayer.Listener;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Create custom Dialog windows for your application Custom dialogs rely on
 * custom layouts wich allow you to create and use your own look & feel.
 * 
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey
 * 
 */
public class MeetingRefuseDialog extends Dialog {

	public MeetingRefuseDialog(Context context, int theme) {
		super(context, theme);
	}

	public MeetingRefuseDialog(Context context) {
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private String title;
		private String message;
		// private String aSelect;
		// private String bSelect;
		private String positiveButtonText;
		private String negativeButtonText;
		private boolean AUTO_SET_TEXTVIEW_HEIGHT = false;
		private View contentView;
		private RadioGroup radioGroup;
		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener;
		private Listener mListener;
		
		public Builder(Context context) {
			this.context = context;
		}
		

		public interface Listener {
			public void getContentText(String content);

		}

		public void setListener(Listener listener) {
			this.mListener = listener;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		// public Builder setASelect(String message){
		// this.aSelect = message;
		// return this;
		// }
		//
		// public Builder setBSelect(String message){
		// this.bSelect = message;
		// return this;
		// }

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setTextViewHeight() {
			AUTO_SET_TEXTVIEW_HEIGHT = true;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			// 获取当前的选项

			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		@SuppressWarnings("deprecation")
		public MeetingRefuseDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final MeetingRefuseDialog dialog = new MeetingRefuseDialog(context, R.style.BaseDialog);
			View layout = inflater.inflate(R.layout.new_ui_alert_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			final EditText editText=(EditText) layout.findViewById(R.id.message);
			if (positiveButtonClickListener != null) {
				((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if(editText.getText().toString().trim().equals("")){
							Toast.makeText(context, "您的拒绝理由不能为空", Toast.LENGTH_SHORT).show();
						}else{
							mListener.getContentText(editText.getText().toString());
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					}
				});
			}

			if (negativeButtonClickListener != null) {
				((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
					}
				});
			}
			dialog.setContentView(layout);
			Display d = dialog.getWindow().getWindowManager().getDefaultDisplay();
			Window window = dialog.getWindow();
			WindowManager.LayoutParams wl = window.getAttributes();
			// wl.x = 0;
			// wl.y = 0;
			wl.height = (int) (d.getHeight() * 0.61);
			wl.width = (int) (d.getWidth() * 19 / 20);
			wl.alpha = 1.0f;
			window.setAttributes(wl);
			window.setGravity(Gravity.BOTTOM);
			window.setWindowAnimations(R.style.DialogAnimation);
			return dialog;
		}

	}

}