package com.superiorinfotech.publicbuddy.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.db.model.MediaAttachment;

import java.net.URL;
import java.util.List;
import java.util.zip.Inflater;



/**
 * Created by admin on 5/12/2015.
 */
public class AudioPlayerDialog implements View.OnClickListener,View.OnTouchListener ,MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    private Button btn_play, btn_pause, btn_stop;
    private Button btn_next,btn_prev;

    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private long lengthOfAudio;
    private  String url ;
    private static final int MINUTES_IN_AN_HOUR = 60;
    private static final int SECONDS_IN_A_MINUTE = 60;
    private final Handler handler = new Handler();
    private boolean is_loading = true;
    private boolean is_stopped = false;

    private TextView txtTime;
    private ProgressBar musicProgress;
    private ImageView artistImg;

    private int currentAudio = 0;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
           updateSeekProgress();
        }
    };
    public AudioPlayerDialog(Context mContext, final List<MediaAttachment> mediaAttachments, final Boolean isServer) {

        Rect displayRectangle = new Rect();
        Window window = ((Activity) mContext).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View builderView = inflater.inflate(R.layout.dialog_audio, null);
//        builderView.setMinimumWidth((int) (displayRectangle.width() * 0.5f));
//        builderView.setMinimumHeight((int) (displayRectangle.height() * 0.5f));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setView(builderView);
        AlertDialog alert = builder.create();
        alert.show();
        alert.setCanceledOnTouchOutside(true);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                stopAudio();
            }
        });


        btn_play = (Button) builderView.findViewById(R.id.btn_play);
        btn_play.setOnClickListener(this);
        btn_pause = (Button) builderView.findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(this);
        btn_pause.setEnabled(false);
        btn_stop = (Button) builderView.findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(this);
        btn_stop.setEnabled(false);
        btn_next = (Button) builderView.findViewById(R.id.btn_forward_item_audio);
        btn_prev = (Button) builderView.findViewById(R.id.btn_prev_item_audio);



        musicProgress = (ProgressBar) builderView.findViewById(R.id.progress);
        // artistImg = (ImageView)  view.findViewById(R.id.artistImg);

        seekBar = (SeekBar) builderView.findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(this);

        txtTime = (TextView) builderView.findViewById(R.id.time);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        //hide next ,prev button
        if (mediaAttachments.size() == 1) {
            btn_next.setVisibility(View.GONE);
            btn_prev.setVisibility(View.GONE);


        }else{
            btn_prev.setVisibility(View.INVISIBLE);
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudio();
                    currentAudio += 1;
                    if(currentAudio == mediaAttachments.size()-1){
                        btn_next.setVisibility(View.INVISIBLE);
                    }else{
                        btn_next.setVisibility(View.VISIBLE);
                    }
                    btn_prev.setVisibility(View.VISIBLE);
                    setPath(mediaAttachments, isServer, currentAudio);
                    startAudio();
                }
            });
            btn_prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudio();
                    currentAudio += -1;
                    if(currentAudio == 0){
                        btn_prev.setVisibility(View.INVISIBLE);


                    }else{
                        btn_prev.setVisibility(View.VISIBLE);
                    }
                    btn_next.setVisibility(View.VISIBLE);
                    setPath(mediaAttachments, isServer, currentAudio);
                    startAudio();
                }
            });
        }

        setPath(mediaAttachments,isServer,currentAudio);
        startAudio();
        btn_pause.setVisibility(View.VISIBLE);
        btn_play.setVisibility(View.INVISIBLE);

    }
    private void setPath(List<MediaAttachment> mediaAttachments,Boolean isServer,int current){
        if(isServer) {
            url = mediaAttachments.get(current).getData();
        }else{
            url ="file:///" + mediaAttachments.get(current).getData();
        }
    }

    @Override
    public void onClick(View view) {

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            lengthOfAudio = mediaPlayer.getDuration();

        } catch (Exception e) {
            // Log.e("Error", e.getMessage());
        }

        switch (view.getId()) {
            case R.id.btn_play:
                if (is_stopped) {
                    is_stopped = false;
                    mediaPlayer.seekTo(0);
                }
                playAudio();
                btn_play.setVisibility(View.INVISIBLE);
                btn_pause.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_pause:
                pauseAudio();
                btn_play.setVisibility(View.VISIBLE);
                btn_pause.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_stop:
                btn_play.setVisibility(View.VISIBLE);
                btn_pause.setVisibility(View.INVISIBLE);
                stopAudio();
                break;
            default:
                break;
        }

        updateSeekProgress();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mediaPlayer.isPlaying()) {
            SeekBar tmpSeekBar = (SeekBar) v;
            mediaPlayer
                    .seekTo((int)(lengthOfAudio / 100) * tmpSeekBar.getProgress());
        }
        return false;

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play.setEnabled(true);
        btn_pause.setEnabled(false);
        btn_stop.setEnabled(false);
        btn_play.setVisibility(View.VISIBLE);
        btn_pause.setVisibility(View.INVISIBLE);
        seekBar.setProgress(seekBar.getMax());
        txtTime.setText("" + timeConversion((int)0));
    }
    private void updateSeekProgress() {
        if (mediaPlayer.isPlaying()) {
            if (is_loading) {
                is_loading = false;
                musicProgress.setVisibility(View.GONE);

            }
            int progress = (int) (((float) mediaPlayer.getCurrentPosition() / lengthOfAudio) * 100);

            long remainSec = (lengthOfAudio - mediaPlayer.getCurrentPosition()) / 1000;

            seekBar.setProgress(progress);
            txtTime.setText("" + timeConversion((int)remainSec));

            handler.postDelayed(runnable, 1000);
        }
    }
    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            is_stopped = true;
        }
        seekBar.setProgress(0);
        seekBar.setSecondaryProgress(0);
        txtTime.setText("" + timeConversion((int)lengthOfAudio / 1000));
        btn_play.setEnabled(true);
        btn_pause.setEnabled(false);
        btn_stop.setEnabled(false);
    }

    private void pauseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        btn_play.setEnabled(true);
        btn_pause.setEnabled(false);
    }

    private void playAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        btn_play.setEnabled(false);
        btn_pause.setEnabled(true);
        btn_stop.setEnabled(true);
    }

    private static String timeConversion(int totalSeconds) {
        int hours = totalSeconds / MINUTES_IN_AN_HOUR / SECONDS_IN_A_MINUTE;
        int minutes = (totalSeconds - (hoursToSeconds(hours)))
                / SECONDS_IN_A_MINUTE;
        int seconds = totalSeconds
                - ((hoursToSeconds(hours)) + (minutesToSeconds(minutes)));

        return hours + ":" + minutes + ":" + seconds;
    }

    private static int hoursToSeconds(int hours) {
        return hours * MINUTES_IN_AN_HOUR * SECONDS_IN_A_MINUTE;
    }

    private static int minutesToSeconds(int minutes) {
        return minutes * SECONDS_IN_A_MINUTE;
    }

    class PlayTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                lengthOfAudio = mediaPlayer.getDuration();

            } catch (Exception e) {
                // Log.e("Error", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            playAudio();
            updateSeekProgress();
        }
    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        pauseAudio();
//        finish();
//    }
    public void startAudio(){

        new PlayTask().execute();
    }

}
