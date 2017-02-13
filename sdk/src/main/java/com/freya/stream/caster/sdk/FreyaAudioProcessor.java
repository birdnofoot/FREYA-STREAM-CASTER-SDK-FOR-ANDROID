package com.freya.stream.caster.sdk;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class FreyaAudioProcessor {

    private FreyaStreamSDK mStreamPusher = null;

    private boolean isAudioRecording = false;
    private int mAudioSampleRate = 44100;
    private int mAudioChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord = null;
    private int mMaxAudioReadbytes = 0;

    public void setAudioOption(int sampleRate, int channels, int maxAudioReadbytes) {
        if (sampleRate <= 0) {
            mAudioSampleRate = 44100;
        } else {
            mAudioSampleRate = sampleRate;
        }
        if(channels == 2)
            mAudioChannels = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        else
            mAudioChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO;

        mMaxAudioReadbytes = maxAudioReadbytes;
    }

    public void setAudioDataCallBack(FreyaStreamSDK Obj) {
        mStreamPusher = Obj;
    }

    public void OpenAudio(FreyaStreamSDK mediaPusher, int maxAudioReadbytes) {
        if (isAudioRecording){
            return;
        }

        int recAudioBufSize = AudioRecord.getMinBufferSize(mAudioSampleRate, mAudioChannels, mAudioEncoding);

        if (mAudioRecord!=null)
        {
            mAudioRecord.release();
            mAudioRecord =null;
        }

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mAudioSampleRate, mAudioChannels,
                mAudioEncoding, recAudioBufSize);
        isAudioRecording = true;

        new RecordPlayThread().start();
    }

    public void closeAudio() {
        isAudioRecording = false;
    }

    class RecordPlayThread extends Thread {
        public void run() {
            try {

                byte[] pcmBuffer = new byte[mMaxAudioReadbytes];

                try{
                    mAudioRecord.startRecording();
                }catch (IllegalStateException e){
                    e.printStackTrace();
                }

                while (isAudioRecording) {

                    int bufferReadResult = mAudioRecord.read(pcmBuffer, 0, mMaxAudioReadbytes);

                    if (bufferReadResult > 0 && mStreamPusher != null) {
                        mStreamPusher.PushAudioData(pcmBuffer, bufferReadResult);
                    }
                    Thread.sleep(1);
                }

                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord =null;

            } catch (Throwable t) {
            }
        }
    };
}
