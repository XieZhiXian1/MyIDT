package com.ids.idtma.jni;

import android.annotation.SuppressLint;

import java.nio.ByteBuffer;

import com.ids.idtma.util.LwtLog;

import android.view.Surface;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      编解码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public class Codec
{
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      语音编码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    MediaCodec		        m_EncodeA;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      语音解码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    MediaCodec		        m_DecodeA;
    
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      视频编码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    MediaCodec		        m_EncodeV;
    int						m_iEncodeVWidth;
    int						m_iEncodeVHeight;
    int                     m_iEncodeVTs;
    byte[] 					m_EnocdeYUV420 = null;
    byte[] 					m_EncodeVInfo = null;
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      视频解码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    MediaCodec		        m_DecodeV;
    int						m_iDecodeVWidth;
    int						m_iDecodeVHeight;
    int                     m_iDecodeVTs;

    @SuppressLint("NewApi")
    public Codec()
    {
    }
    
    @SuppressLint("NewApi")
    public void Init()
    {
        m_EncodeA = null;
        m_DecodeA = null;
    	m_EncodeV = null;
        m_DecodeV = null;
    }

//--------------------------------------------------------------------------------
//  YV12转I420
//输入:
//  yv12bytes:		YV12缓冲区
//	i420bytes:		I420缓冲区
//	width:			宽度
//  height:			高度
//返回:
//  无
//--------------------------------------------------------------------------------
    public void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) 
    {      
    	System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
    	System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
    	System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);  
    }    
//--------------------------------------------------------------------------------
//      实际的编解码调用函数
//  输入:
//  	iType:          调用类型    0:语音编码 1:语音解码 2:视频编码 3:视频解码    
//      codec:          编解码器
//      InputBuf:       输入缓冲区
//      OutputBuf:      输出缓冲区
//      pucSrcBuf:      源缓冲区
//      iSrcLen:        源缓冲区长度
//      pucDstBuf:      目的缓冲区
//      iDstSize:       目的缓冲区大小
//  返回:
//      -1:             失败
//      else:           编解码之后,目的缓冲区有效长度
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public int CodecFunc1(int iType, MediaCodec codec, ByteBuffer[] InputBuf, ByteBuffer[] OutputBuf,
        byte[] pucSrcBuf, int iSrcLen, byte[] pucDstBuf, int iDstSize,
        int iTs)
    {
    	int iPos = -1;
        try
        {
        	// 1.放入输入数据
        	int iIndexInput = codec.dequeueInputBuffer(0);
        	if (iIndexInput < 0)
        		return -1;
        	InputBuf[iIndexInput].clear();
        	InputBuf[iIndexInput].put(pucSrcBuf);
        	codec.queueInputBuffer(iIndexInput, 0, iSrcLen, iTs, 0);
        	
        	// 2.获取输出数据
        	MediaCodec.BufferInfo BufferInfo = new MediaCodec.BufferInfo();
        	//用循环取,确保取完
        	int iOutputIndex = codec.dequeueOutputBuffer(BufferInfo, 2);
        	for (iPos = 0; iOutputIndex >= 0;)
        	{
        		//把数据拷贝出来
	            byte[] tData = new byte[BufferInfo.size];
	            OutputBuf[iOutputIndex].get(tData);
	            
	            if (2 == iType)
	            {
		            if (m_EncodeVInfo != null)//如果已经有头信息了,PPS/SPS
		            {            	
		            	System.arraycopy(tData, 0,  pucDstBuf, iPos, tData.length);
		            	iPos += tData.length;
		            }
		            else//没有头信息,要保存
		            {
		            	 ByteBuffer spsPpsBuffer = ByteBuffer.wrap(tData);  
		                 if (spsPpsBuffer.getInt() == 0x00000001)
		                 {  
		                	 m_EncodeVInfo = new byte[tData.length];
		                	 System.arraycopy(tData, 0, m_EncodeVInfo, 0, tData.length);
		                 } 
		                 else 
		                 {
		                        return -1;
		                 }
		            }
	            }
	            else
	            {
	            	System.arraycopy(tData, 0,  pucDstBuf, iPos, tData.length);
	            	iPos += tData.length;
	            }
	            
	            //释放内存
	            codec.releaseOutputBuffer(iOutputIndex, false);
	            
	            //看是否完成
        		iOutputIndex = codec.dequeueOutputBuffer(BufferInfo, 0);
        	}

	        if (2 == iType && 0x65 == pucDstBuf[4])//如果是视频编码I帧,添加SPS/PPS
	        {
	        	System.arraycopy(pucDstBuf, 0,  m_EnocdeYUV420, 0, iPos);
	        	System.arraycopy(m_EncodeVInfo, 0, pucDstBuf, 0, m_EncodeVInfo.length);
	        	System.arraycopy(m_EnocdeYUV420, 0, pucDstBuf, m_EncodeVInfo.length, iPos);
	        	iPos += m_EncodeVInfo.length;
	        }        	
        }
        catch (Throwable t)
        {
        	LwtLog.d("wulin", "CodecFunc1------------------此处有bug");
            t.printStackTrace();
        }
        return iPos;
    }
    
//--------------------------------------------------------------------------------
//      编解码接口函数
//  输入:
//      iType:          调用类型    0:语音编码 1:语音解码 2:视频编码 3:视频解码
//      pucSrcBuf:      源缓冲区
//      iSrcLen:        源缓冲区长度
//      pucDstBuf:      目的缓冲区
//      iDstSize:       目的缓冲区大小
//  返回:
//      -1:             失败
//      else:           编解码之后,目的缓冲区有效长度
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public synchronized int CodecFunc(int iType, byte[] pucSrcBuf, int iSrcLen, byte[] pucDstBuf, int iDstSize)
    {
        ByteBuffer[]  InputBuffer;
        ByteBuffer[]  OutputBuffer;
    
    	switch (iType)
    	{
    	case 0:
    		break;
    	case 1://DecodeA
    		return DecodeADecode(pucSrcBuf, iSrcLen, pucDstBuf, iDstSize);
    	case 2://EncodeV
            InputBuffer = m_EncodeV.getInputBuffers();
            OutputBuffer = m_EncodeV.getOutputBuffers();
            m_iEncodeVTs++;
            //swapYV12toI420(pucSrcBuf, m_EnocdeYUV420, m_iEncodeVWidth, m_iEncodeVHeight);
    		//return CodecFunc1(iType, m_EncodeV, InputBuffer, OutputBuffer, m_EnocdeYUV420, iSrcLen, pucDstBuf, iDstSize, m_iEncodeVTs);
            return CodecFunc1(iType, m_EncodeV, InputBuffer, OutputBuffer, pucSrcBuf, iSrcLen, pucDstBuf, iDstSize, m_iEncodeVTs);
    	case 3://DecodeV
            InputBuffer = m_DecodeV.getInputBuffers();
            OutputBuffer = m_DecodeV.getOutputBuffers();    		
            m_iDecodeVTs++;
    		return CodecFunc1(iType, m_DecodeV, InputBuffer, OutputBuffer, pucSrcBuf, iSrcLen, pucDstBuf, iDstSize, m_iDecodeVTs);
    	default:
    		break;
    	}
        return 0;
    }    
//--------------------------------------------------------------------------------
//      编解码控制接口函数
//  输入:
//      iType:          调用类型    0:语音编码 1:语音解码 2:视频编码 3:视频解码
//      iCtrl:          控制        0:启动 1:停止
//      iWidth:         宽度
//      iHeight:        高度
//      iFrameRate:     帧率
//      iBitrate:       码率
//  返回:
//      0:              成功
//      -1:             失败
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public synchronized int CtrlFunc(int iType, int iCtrl, int iWidth, int iHeight, int iFrameRate, int iBitrate)
    {
    	switch (iType)
    	{
    	case 0:
    		break;
    	case 1://DecodeA
    		if (0 == iCtrl)//启动
    		{
    			DecodeAStart();
    		}
    		else//退出
    		{
    			DecodeAEnd();
    		}
    		break;
    	case 2://EncodeV
    		if (0 == iCtrl)//启动
    		{
    			EncodeVStart(iWidth, iHeight, iFrameRate, iBitrate);
        		m_iEncodeVTs = 0;
    		}
    		else//退出
    		{
    			EncodeVEnd();
                m_iEncodeVTs = 0;
    		}
    		break;
    	default:
    		break;
    	}
        return 0;
    }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      语音编码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      语音解码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @SuppressLint("NewApi")
    public int DecodeAStart()
    {
    	m_DecodeA = MediaCodec.createDecoderByType("audio/3gpp");
        MediaFormat mFormat = MediaFormat.createAudioFormat("audio/3gpp", 8000, 1);
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, 7950);
        m_DecodeA.configure(mFormat, null, null, 0);         
        m_DecodeA.start();
        
        return 0;
    }

    @SuppressLint("NewApi")
    public int DecodeAEnd()
    {
        try
        {
        	if (m_DecodeA != null)
        	{
	            m_DecodeA.stop();
	            m_DecodeA.release();
	            m_DecodeA = null;
        	}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    @SuppressLint("NewApi")
    public int DecodeADecode(byte[] pucSrcBuf, int iSrcLen, byte[] pucDstBuf, int iDstSize)
    {
    	int iPos = -1;
        try
        {
            ByteBuffer[]	m_DecodeAInputBuffer;
            ByteBuffer[]	m_DecodeAOutputBuffer;
            m_DecodeAInputBuffer = m_DecodeA.getInputBuffers();
            m_DecodeAOutputBuffer = m_DecodeA.getOutputBuffers();
            
        	// 1.放入输入数据
        	int iIndexInput = m_DecodeA.dequeueInputBuffer(-1);
        	if (iIndexInput < 0)
        		return -1;
        	m_DecodeAInputBuffer[iIndexInput].clear();
        	m_DecodeAInputBuffer[iIndexInput].put(pucSrcBuf);
        	m_DecodeA.queueInputBuffer(iIndexInput, 0, iSrcLen, 0, 0);
        	
        	// 2.获取输出数据
        	MediaCodec.BufferInfo BufferInfo = new MediaCodec.BufferInfo();
        	//用循环取,确保取完
        	int iOutputIndex = m_DecodeA.dequeueOutputBuffer(BufferInfo, 0);
        	for (iPos = 0; iOutputIndex >= 0;)
        	{
        		//把数据拷贝出来
	            byte[] tData = new byte[BufferInfo.size];
	            m_DecodeAOutputBuffer[iOutputIndex].get(tData);
	            System.arraycopy(tData, 0,  pucDstBuf, iPos, pucDstBuf.length);
	            
	            //释放内存
	            m_DecodeA.releaseOutputBuffer(iOutputIndex, false);
	            
	            //看是否完成
        		iOutputIndex = m_DecodeA.dequeueOutputBuffer(BufferInfo, 0);
        	}
        }
        catch (Throwable t)
        {
        	LwtLog.d("wulin", "DecodeADecode------------------此处有bug");
            t.printStackTrace();
        }
        return iPos;
    }
    
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      视频编码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//--------------------------------------------------------------------------------
//      启动
//  输入:
//      iWidth:         宽度
//      iHeight:        高度
//      iFrameRate:     帧率
//      iBitrate:       码率
//  返回:
//      0:              成功
//      -1:             失败
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public int EncodeVStart(int iWidth, int iHeight, int iFrameRate, int iBitrate)
    {
    	m_iEncodeVWidth  = iWidth;
    	m_iEncodeVHeight = iHeight;
    	m_EnocdeYUV420 = new byte[iWidth * iHeight * 3 / 2];
	
    	m_EncodeV = MediaCodec.createEncoderByType("video/avc");
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", iWidth, iHeight);
	    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, iBitrate);
	    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, iFrameRate);
	    //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
	    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
	    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
	    
	    m_EncodeV.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    m_EncodeV.start();

        return 0;
    }
//--------------------------------------------------------------------------------
//      停止
//  输入:
//      无
//  返回:
//      0:              成功
//      -1:             失败
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public int EncodeVEnd()
    {
        try
        {
        	if (m_EncodeV != null)
        	{
	            m_EncodeV.stop();
	            m_EncodeV.release();
	            m_EnocdeYUV420 = null;
	            m_EncodeVInfo = null;
        	}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//      视频解码器
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//--------------------------------------------------------------------------------
//      启动
//  输入:
//      无
//  返回:
//      0:              成功
//      -1:             失败
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public int DecodeVStart(int iWidth, int iHeight, int iFrameRate, int iBitrate, Surface sf)
    {
    	m_iDecodeVWidth  = iWidth;
    	m_iDecodeVHeight = iHeight;
	
    	m_DecodeV = MediaCodec.createDecoderByType("video/avc");
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", iWidth, iHeight);
	    //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
	    m_DecodeV.configure(mediaFormat, sf, null, 0);
	    m_DecodeV.start();

        return 0;
    }
//--------------------------------------------------------------------------------
//      停止
//  输入:
//      无
//  返回:
//      0:              成功
//      -1:             失败
//--------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    public int DecodeVEnd()
    {
        try
        {
            m_DecodeV.stop();
            m_DecodeV.release();
            m_DecodeV = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}


