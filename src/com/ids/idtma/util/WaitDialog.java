package com.ids.idtma.util;

import com.ids.idtma.R;
import com.ids.idtma.ftp.FtpBuinessLayer.Listener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * 缓冲布局dialog(加载时的dialog)
 * @author ywl
 *
 */
public class WaitDialog {

    private Context context;
    private Dialog dialog;
    private TextView messageText;
    private Handler handler;
    public interface Listener {
		public void dialogBackKey();

	}
    
	private Listener mListener;

	public void setListener(Listener listener) {
		this.mListener = listener;
	}
//    private boolean flag;

    public WaitDialog(Context context){
        if (null == context){
            throw new IllegalArgumentException("Creat WaitDialog: params null(context)");
        }
        this.context = context;
//        this.flag = flag;
        handler = new Handler();
    }

    /**
     * 初始化dialog
     */
    private void init(){
        if (null != context){
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.idt_dialog_progress, null);
            messageText = (TextView) view.findViewById(R.id.message);
            dialog.setContentView(view);
            dialog.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if (keyCode == KeyEvent.KEYCODE_BACK  
	                        && event.getRepeatCount() == 0) {  
	                   mListener.dialogBackKey();
	                }  
					return false;
				}
			});
        }
    }
   /**
    * 设置是否需要设置外部点击取消dialog
    * @param flag
    */
//   public void setIsCancelOnTouchOutSide(boolean flag){
//	   if(flag){
//		   dialog.setCanceledOnTouchOutside(true);
//	   }else{
//		   dialog.setCanceledOnTouchOutside(false);
//	   }
//   }
    /**
    *	显示dialog
    */
   public void show(String str){
       if (null == dialog){
           init();
       }
       if (((Activity) context).isFinishing()){
           return;
       }
       if (null != dialog){
           if (!dialog.isShowing()){
               dialog.show();
           }
       }
       setDialogText(str);
   }
    private void setDialogText(String str){
        if (null != messageText){
        	messageText.setText(str);
        }
    }

    /**
     * 取消dialog
     */
    public void cancel(){
        if (null != dialog){
            dialog.cancel();
        }
    }

    /**
     * 延时取消dialog
     * @param time
     */
    public void delayCancel(int time){
        if (null != handler){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancel();
                }
            }, time);
        }
    }

    /**
     * 延时取消dialog,并回调响应处理
     * @param time
     * @param listener
     */
    public void delayCancel(int time, final DelayCancelListener listener){
        if (null != handler){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancel();
                    if (null != listener){
                        listener.onDeal();
                    }
                }
            }, time);
        }
    }

    public boolean isShowing() {
        if(null != dialog) {
            return dialog.isShowing();
        }
        return false;
    }

    /**
     * 设置dialog是否可以取消
     * @param cancelable
     */
    public void setCancelable(boolean cancelable){
        if (null != dialog){
            dialog.setCancelable(cancelable);
        }
    }

    /**
     * 延时处理回到接口
     */
    public interface DelayCancelListener{
        public void onDeal();
    }
}
