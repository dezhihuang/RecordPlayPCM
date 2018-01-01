package com.example.hdz.recordplaypcm;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by hdz on 2017/12/10.
 */

public class PCMRecord {

    private PCMParam m_pcmParam = null;
    private boolean m_bRecording = false;
    private AudioRecord m_recorder = null;
    private boolean m_bSaveFile = false;
    private String m_sPCMFilePath = "";
    private boolean m_bStopRecord = false;
    private RecordThread m_recordThread;


    public void record(){
        m_recordThread = new RecordThread();
        m_recordThread.start();
    }

    public void setRecordParam(PCMParam pcmParam) {
        m_pcmParam = pcmParam;
    }

    public void stopRecord() {
        if (m_bRecording) {
            m_recorder.stop();
            m_recorder.release();
            m_recorder = null;
            m_bRecording = false;
        }
    }

    private boolean ReadAudioData(byte[] data, int offset, int size) {
        int readSize = 0;
        while (m_bRecording) {
            int thisReadSize = m_recorder.read(data, offset + readSize, size - readSize);
            if (!m_bSaveFile) {
                data[0] = '\0'; //如果不是正真的录音开始，则直接加结束符
                continue;
            }
            if (thisReadSize > 0) {
                readSize += thisReadSize;
            } else {
                break;
            }
            if (readSize == size)
                return true;
            else if (readSize > size) {
                break;
            }
        }
        return false;
    }

    private class RecordThread extends Thread{
        public void run(){
            StartPCMRecord();
            int size = AudioRecord.getMinBufferSize(m_pcmParam.m_iFrequency, m_pcmParam.m_iChannel, m_pcmParam.m_iSampBit);
            ByteBuffer dataBuffer = ByteBuffer.allocate(size);
            while (!m_bStopRecord){
                dataBuffer.clear();
                dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                ReadAudioData(dataBuffer.array(), 0, size);
                if (m_bSaveFile) {
                    SaveToFile(dataBuffer.array(), size, m_sPCMFilePath);
                }
            }
            stopRecord();
        }
    }

    public void setPCMFilePath(String filePath) {
        m_sPCMFilePath = filePath;
    }

    public void setIsSaveFile(boolean bSaveFile) {
        m_bSaveFile = bSaveFile;
    }

    private void WriteTxtFile(byte[] b, String strFilePath) {
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(b);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StartPCMRecord() {
        if (m_pcmParam != null) {
            stopRecord();
            // 获得构建对象的最小缓冲区大小
            // 根据音频数据的特性来确定所要分配的缓冲区的最小size
            int bufSize = AudioRecord.getMinBufferSize(m_pcmParam.m_iFrequency, m_pcmParam.m_iChannel, m_pcmParam.m_iSampBit);
            m_recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, m_pcmParam.m_iFrequency, m_pcmParam.m_iChannel, m_pcmParam.m_iSampBit, bufSize);
            try {
                m_recorder.startRecording();
                m_bRecording = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void SaveToFile(byte[] b, int len, String strFilePath) {
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(b, 0, len);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
