package com.superiorinfotech.publicbuddy.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;


import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.uraroji.garage.android.mp3recvoice.RecMicToMp3;

import java.io.IOException;

/**
 * Created by alex on 17.01.15.
 */
public class RecordAudio {
    private Context context;


    private RecMicToMp3 mRecMicToMp3  = null;
    public int getRecordStatus() {
        return recordStatus;
    }

    private int recordStatus=0;
    public RecordAudio(Context context){
       mRecMicToMp3 = new RecMicToMp3(
               IncidentReporter.getContext().getExternalFilesDir(null).getPath() + "/user_audio_tmp.mp3", 8000);
        this.context = context;
    }

    public void startRecording() {


           mRecMicToMp3.start();
            recordStatus = 1;

    }

    public void stopRecording() {

            mRecMicToMp3.stop();
            recordStatus = 0;

    }

}
