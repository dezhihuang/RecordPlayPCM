package com.example.hdz.recordplaypcm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener{

    private String m_sSDPath = "";
    private PCMRecord m_pcmRecord;
    private PCMPlay m_pcmPlay;

    private EditText m_etRecordFileName;
    private EditText m_etPlayFileName;
    private Button m_btnStartRecord;
    private Button m_btnStartPlay;

    private Handler m_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();

        //判断设备是否插入了SD卡，且应用程序具有读写SD卡的权限
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                m_sSDPath = Environment.getExternalStorageDirectory().getCanonicalPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            m_sSDPath = "";
        }

        initLogic();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStartPlay:
                startPlay();
                break;
            case R.id.btnStartRecord:
                startRecord();
                break;
            case R.id.btnStopPlay:
                stopPlay();
                break;
            case R.id.btnStopRecord:
                stopRecord();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (m_pcmRecord != null) {
            m_pcmRecord.stopRecord();
            m_pcmRecord = null;
        }
        if (m_pcmPlay != null) {
            m_pcmPlay.clean();
            m_pcmPlay = null;
        }
        super.onDestroy();
    }

    private void initWidget() {
        m_btnStartRecord = (Button) findViewById(R.id.btnStartRecord);
        m_btnStartRecord.setOnClickListener(this);

        Button btnStopRecord = (Button) findViewById(R.id.btnStopRecord);
        btnStopRecord.setOnClickListener(this);

        m_btnStartPlay = (Button) findViewById(R.id.btnStartPlay);
        m_btnStartPlay.setOnClickListener(this);

        Button btnStopPlay = (Button)findViewById(R.id.btnStopPlay);
        btnStopPlay.setOnClickListener(this);

        m_etRecordFileName = (EditText) findViewById(R.id.etRecordFileName);
        m_etRecordFileName.setText("test.pcm");

        m_etPlayFileName   = (EditText) findViewById(R.id.etPlayFileName);
        m_etPlayFileName.setText("test.pcm");
    }

    private void initLogic() {
        m_handler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case PCMPlay.PLAY_STATE_COMPLETE:
                        m_pcmPlay.stop();
                        m_etPlayFileName.setEnabled(true);
                        m_btnStartPlay.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        };

        m_pcmRecord = new PCMRecord();
        m_pcmPlay   = new PCMPlay(m_handler);

        //设置录音参数
        PCMParam pcmParam_in = PCMParam.GetPCMParamByDecodeType(PCMParam.AUDIO_16K_PCM, true);
        m_pcmRecord.setRecordParam(pcmParam_in);

        PCMParam pcmParam_out = PCMParam.GetPCMParamByDecodeType(PCMParam.AUDIO_16K_PCM, false);
        m_pcmPlay.setPlayParam(pcmParam_out);

        //启动App就开始录音，但是不保存到文件，这样才能不会延迟录音
        m_pcmRecord.record();

        m_pcmPlay.initPlay();
    }

    //开始保存录音文件
    private void startRecord() {
        if (m_sSDPath.equals("")) {
            return;
        }
        String fileName = m_etRecordFileName.getText().toString();
        if (fileName.equals("")) {
            return;
        }

        //如果文件存在就先删除
        String sPath = m_sSDPath + "/" + fileName;
        File file = new File(sPath);
        if (file.exists()) {
            file.delete();
        }

        //设置录音文件路径
        m_pcmRecord.setPCMFilePath(sPath);

        //开始将录音数据保存到文件中
        m_pcmRecord.setIsSaveFile(true);

        //禁止修改文件名
        m_etRecordFileName.setEnabled(false);

        //禁止重新开始保存录音
        m_btnStartRecord.setEnabled(false);
    }

    //停止保存录音文件
    private void stopRecord() {
        m_pcmRecord.setIsSaveFile(false);
        m_etRecordFileName.setEnabled(true);
        m_btnStartRecord.setEnabled(true);
    }

    private void startPlay() {
        if (m_sSDPath.equals("")) {
            return;
        }
        String fileName = m_etPlayFileName.getText().toString();
        if (fileName.equals("")) {
            return;
        }

        String sPath = m_sSDPath + "/" + fileName;
        m_pcmPlay.setPCMFilePath(sPath);

        m_etPlayFileName.setEnabled(false);
        m_btnStartPlay.setEnabled(false);
        m_pcmPlay.play();
    }

    private void stopPlay() {
        m_pcmPlay.stop();
        m_etPlayFileName.setEnabled(true);
        m_btnStartPlay.setEnabled(true);
    }
}
