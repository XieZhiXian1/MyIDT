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
public class ImageGetDialog extends Dialog {
	public static final int BUTTON_TAKE_PICTURE = 1;
	public static final int BUTTON_SELECT_PICTURE = 2;
	public static final int BUTTON_CANCEL = 3;

	public ImageGetDialog(Context context, int theme) {
		super(context, theme);
	}

	public ImageGetDialog(Context context) {
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private DialogInterface.OnClickListener takePictureButtonOnclickListener, selectPictureButtonOnclickListener,
				cancelButtonOnclickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setTakePictureButton(DialogInterface.OnClickListener listener) {
			this.takePictureButtonOnclickListener = listener;
			return this;
		}

		public Builder setSelectPictureButton(DialogInterface.OnClickListener listener) {
			this.selectPictureButtonOnclickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setCalcelButton(DialogInterface.OnClickListener listener) {
			this.cancelButtonOnclickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		@SuppressWarnings("deprecation")
		public ImageGetDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final ImageGetDialog dialog = new ImageGetDialog(context, R.style.BaseDialog);
			View layout = inflater.inflate(R.layout.new_ui_image_alert_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			if (takePictureButtonOnclickListener != null) {
				((Button) layout.findViewById(R.id.btn_take_photo)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						takePictureButtonOnclickListener.onClick(dialog, ImageGetDialog.BUTTON_TAKE_PICTURE);
					}
				});
			}
			if (selectPictureButtonOnclickListener != null) {
				((Button) layout.findViewById(R.id.btn_pick_photo)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						selectPictureButtonOnclickListener.onClick(dialog, ImageGetDialog.BUTTON_SELECT_PICTURE);
					}
				});

			}
			if (cancelButtonOnclickListener != null) {
				((Button) layout.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						cancelButtonOnclickListener.onClick(dialog, ImageGetDialog.BUTTON_CANCEL);
					}
				});

			}
			dialog.setContentView(layout);
			Display d = dialog.getWindow().getWindowManager().getDefaultDisplay();
			Window window = dialog.getWindow();
			WindowManager.LayoutParams wl = window.getAttributes();
			wl.height = (int) (d.getHeight() * 0.4);
			wl.width = (int) (d.getWidth() * 19 / 20);
			wl.alpha = 1.0f;
			window.setAttributes(wl);
			window.setGravity(Gravity.BOTTOM);
			window.setWindowAnimations(R.style.DialogAnimation);
			return dialog;
		}

	}

}