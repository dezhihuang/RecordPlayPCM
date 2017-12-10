package com.example.hdz.recordplaypcm;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

/**
 * Created by hdz on 2017/12/10.
 */

public class PCMPlay {
    public final static int  MSG_ID = 0x0010;
    public final static int PLAY_STATE_COMPLETE = 0;
    private Handler m_handler;

    private PCMParam m_pcmParam = null;
    private AudioTrack m_audioTrack = null;
    private boolean m_bThreadExit = false;						// 线程退出标志
    private int     m_iPrimePlaySize = 0;						// 较优播放块大小
    private int 	m_iPlayOffset = 0;							// 当前播放位置
    private byte[] 	m_pcmData;								    // PCM音频数据
    private PlayPCMThread m_pcmPlayThread;
    private String m_sPCMFilePath = "";

    public PCMPlay(Handler handler) {
        m_handler = handler;
    }

    public void initPlay() {
        if (m_pcmParam != null) {
            createAudioTrack();
        }
    }


    public void clean() {
        stop();
        releaseAudioTrack();
    }

    public void setPlayParam(PCMParam pcmParam) {
        m_pcmParam = pcmParam;
    }

    public void play()
    {
        m_iPlayOffset = 0;
        startThread();
    }

    public void stop() {
        stopThread();
    }

    public void setPCMFilePath(String filePath) {
        m_sPCMFilePath = filePath;
    }

    private void createAudioTrack()
    {
        // 获得构建对象的最小缓冲区大小
        // 根据音频数据的特性来确定所要分配的缓冲区的最小size
        int minBufSize = AudioTrack.getMinBufferSize(m_pcmParam.m_iFrequency, m_pcmParam.m_iChannel, m_pcmParam.m_iSampBit);

        m_iPrimePlaySize = minBufSize * 2;

//		         STREAM_ALARM：警告声
//		         STREAM_MUSCI：音乐声，例如music等
//		         STREAM_RING：铃声
//		         STREAM_SYSTEM：系统声音
//		         STREAM_VOCIE_CALL：电话声音
        m_audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, m_pcmParam.m_iFrequency, m_pcmParam.m_iChannel, m_pcmParam.m_iSampBit, minBufSize, AudioTrack.MODE_STREAM);
//				AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
//      		STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
//				这个和我们在socket中发送数据一样，应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
//				这种方式的坏处就是总是在JAVA层和Native层交互，效率损失较大。
//				而STATIC的意思是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
//				后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
//				这种方法对于铃声等内存占用较小，延时要求较高的声音来说很适用。
    }

    private void releaseAudioTrack(){
        if (m_audioTrack != null){
            m_audioTrack.stop();
            m_audioTrack.release();
            m_audioTrack = null;
        }

    }

    private void startThread()
    {
        if (m_pcmPlayThread == null) {
            m_bThreadExit = false;
            m_pcmPlayThread = new PlayPCMThread();
            m_pcmPlayThread.start();
        }
    }

    private void stopThread()
    {
        if (m_pcmPlayThread != null) {
            m_bThreadExit = true;
            m_pcmPlayThread = null;
        }
    }

    class PlayPCMThread extends Thread {
        public void run() {
            m_pcmData = getPCMData();
            if (m_pcmData == null) {
                Log.e("PCMPlay", "m_pcmData == null");
                return;
            }
            m_audioTrack.play();
            while(true) {
                if (m_bThreadExit) {
                    break;
                }
                try {
                    m_audioTrack.write(m_pcmData, m_iPlayOffset, m_iPrimePlaySize);
                    m_iPlayOffset += m_iPrimePlaySize;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                if (m_iPlayOffset >= m_pcmData.length) {
                    break;
                }
            }
            m_audioTrack.stop();

            //播放完成通知UI
            if (m_handler != null) {
                Message msg = m_handler.obtainMessage(MSG_ID);
                msg.what =  PLAY_STATE_COMPLETE;
                msg.sendToTarget();
            }
        }
    }

    //获得PCM音频数据
    private byte[] getPCMData()
    {
        if (m_sPCMFilePath.equals("")) {
            return null;
        }

        File file = new File(m_sPCMFilePath);
        if (!file.exists()) {
            return null;
        }

        FileInputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        long size = file.length();

        byte[] data_pack = new byte[(int) size];
        try {
            inStream.read(data_pack);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return data_pack;
    }

}
