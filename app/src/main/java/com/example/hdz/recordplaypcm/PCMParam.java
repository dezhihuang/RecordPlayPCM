package com.example.hdz.recordplaypcm;

import android.media.AudioFormat;

/**
 * Created by hdz on 2017/12/10.
 */

public class PCMParam {
    public int m_iFrequency; //采样率
    public int m_iChannel;   //声道数
    public int m_iSampBit;   //采样精度

    //DecodeType
    public static final int AUDIO_AAC_ELD = 1;  //44100 16bit 2channel, use AAC_ELD compress
    public static final int AUDIO_HIGH_PCM = 2; //44100 16bit 2 channel
    public static final int AUDIO_LOW_PCM = 3; //8000 16bit 1 channel
    public static final int AUDIO_HIGHER_PCM = 4; //48000 16bit 2 channel
    public static final int AUDIO_16K_PCM = 5; //16000 16bit 1 channel
    public static final int AUDIO_24K_PCM = 6; //24000 16bit 2 channel

    public PCMParam(int frequency, int channel, int sampBit) {
        m_iFrequency = frequency;
        m_iChannel = channel;
        m_iSampBit = sampBit;
    }

    public static PCMParam GetPCMParamByDecodeType(int decode_type, boolean isInput) {
        int bits = 0, channel = 0, rate = 0;
        switch (decode_type) {
            case AUDIO_AAC_ELD:
            case AUDIO_HIGH_PCM:
                bits = 16;
                channel = 2;
                rate = 44100;
                break;
            case AUDIO_LOW_PCM:
                bits = 16;
                channel = 1; //CHANNEL_IN_MONO
                rate = 8000;
                break;
            case AUDIO_16K_PCM:
                bits = 16;
                channel = 1; //CHANNEL_IN_MONO
                rate = 16000;
                break;
            case AUDIO_24K_PCM:
                bits = 16;
                channel = 1; //CHANNEL_IN_MONO
                rate = 24000;
                break;
            case AUDIO_HIGHER_PCM:
                bits = 16;
                channel = 2;
                rate = 48000;
                break;
        }

        int iBits = (bits == 16) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        if (isInput) {
            int iChannel = (channel == 1) ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
            return new PCMParam(rate, iChannel, iBits);
        }
        else {
            int iChannel = (channel == 1) ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
            return new PCMParam(rate, iChannel, iBits);
        }
    }
}
