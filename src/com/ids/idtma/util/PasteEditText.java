package com.ids.idtma.util;
import com.ids.idtma.chat.IdtChatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView.BufferType;

/**
 * 自定义的textview，用来处理复制粘贴的消息
 *
 */
public class PasteEditText extends EditText{
    private Context context;

    
    public PasteEditText(Context context) {
        super(context);
        this.context = context;
    }

    public PasteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        // TODO Auto-generated constructor stub
    }

    public PasteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }
    //EditText通过onTextContextMenuItem方法捕获复制粘贴事件
    //以EASEMOBIMG打头，就将这个干掉
    //复制图片的时候
    @SuppressLint("NewApi")
	@Override
    public boolean onTextContextMenuItem(int id) {
        if(id == android.R.id.paste){
            @SuppressWarnings("deprecation")
            ClipboardManager clip = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            String text = clip.getText().toString();
            if(text.startsWith(IdtChatActivity.COPY_IMAGE)){
//                intent.setDataAndType(Uri.fromFile(new File("/sdcard/mn1.jpg")), "image/*");     
                text = text.replace(IdtChatActivity.COPY_IMAGE, "");
                Intent intent = new Intent(context,FXAlertDialog.class);
                intent.putExtra("title", "发送以下图片？");
                intent.putExtra("forwardImage", text);
                intent.putExtra("cancel", true);
                ((Activity)context).startActivityForResult(intent,IdtChatActivity.REQUEST_CODE_COPY_AND_PASTE);
//                clip.setText("");
            }
        }
        return super.onTextContextMenuItem(id);
    }
    
    
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if(!TextUtils.isEmpty(text) && text.toString().startsWith(IdtChatActivity.COPY_IMAGE)){
            setText("");
        }
//        else if(!TextUtils.isEmpty(text)){
//        	setText(SmileUtils.getSmiledText(getContext(), text),BufferType.SPANNABLE);
//        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }
    

}
