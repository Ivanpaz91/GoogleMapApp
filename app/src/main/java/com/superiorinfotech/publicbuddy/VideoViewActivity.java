package com.superiorinfotech.publicbuddy;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.superiorinfotech.publicbuddy.db.model.MediaAttachment;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;
import com.superiorinfotech.publicbuddy.enums.MediaType;
import com.superiorinfotech.publicbuddy.utils.MyMediaController;

import java.util.List;


public class VideoViewActivity extends Activity {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_view);

//
        mContext  = this;
        Intent intent = getIntent();
        final int videoCount = intent.getIntExtra("count", 1);
        final int videoId = intent.getIntExtra("_id",1);
        final Boolean isLocal = intent.getBooleanExtra("type", true);

        setContentView(R.layout.dialog_attachment_layout);


        final ProgressDialog pDialog = new ProgressDialog(this, R.style.MyTheme);
        ImageView closeBtn = (ImageView)findViewById(R.id.imageView_close);
        final RelativeLayout layout = (RelativeLayout)findViewById(R.id.aie_scroll_full_type11);
        final TextView pageNumber = (TextView)findViewById(R.id.textView_number_page);
        pageNumber.setText("1/" + videoCount);

        closeBtn.setVisibility(View.VISIBLE);
        closeBtn.bringToFront();

        final VideoView videoView = (VideoView) findViewById(R.id.videoView_item);

//                videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
        //   videoView.getHolder().setFormat(PixelFormat.RGBX_8888);

        videoView.setVisibility(View.VISIBLE);
    ;

        final Button prevBtn = (Button) findViewById(R.id.btn_prev_item);
        final Button forwardBtn = (Button) findViewById(R.id.btn_forward_item);

        final List<MediaAttachment> attachments = MediaAttachment.getIncidentAttachmentsByType((long)videoId, MediaType.VIDEO);
        if(videoCount == 1){
            prevBtn.setVisibility(View.INVISIBLE);
            forwardBtn.setVisibility(View.INVISIBLE);
        }else{
            prevBtn.setVisibility(View.INVISIBLE);
            forwardBtn.setVisibility(View.VISIBLE);
        }

        // Set progressbar message

        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(true);
        // Show progressbar
        pDialog.show();
        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
               finish();
            }
        });


        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 finish();
            }
        });
        final int[] current_video={0};
        try {

            if(isLocal) {
                videoView.setVideoPath("file:///" + attachments.get(current_video[0]).getData());
//                        String  newVideoPath = "file:///" + attachments.get(current_video[0]).getData();
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newVideoPath));
//                        intent.setDataAndType(Uri.parse(newVideoPath), "video/mp4");
//                        mContext.startActivity(intent);

            }else{
                videoView.setVideoPath(attachments.get(current_video[0]).getData());
//                        String  newVideoPath = attachments.get(current_video[0]).getData()
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newVideoPath));
//                        intent.setDataAndType(Uri.parse(newVideoPath), "video/mp4");
//                        mContext.startActivity(intent);

            }


            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                            videoView.start();
                    pDialog.dismiss();

                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                          /*
                                           *  add media controller
                                           */
                            MyMediaController mc = new MyMediaController(mContext, (FrameLayout)findViewById(R.id.controllerAnchor));

                            videoView.setMediaController(mc);
                                          /*
                                           * and set its position on screen
                                           */
                            mc.setAnchorView(videoView);
                            videoView.start();
                        }
                    });
                }
                    //  mediaPlayer.setPlaybackSpeed(1.0f);

            });

            MyMediaController mediaController = new MyMediaController(this, (FrameLayout)findViewById(R.id.controllerAnchor));
            ((FrameLayout) findViewById(R.id.controllerAnchor)).bringToFront();
            mediaController.setMediaPlayer(videoView);

            mediaController.requestFocus();
            mediaController.setEnabled(true);
            videoView.setMediaController(mediaController);
            videoView.start();


        } catch (Exception e) {
            e.printStackTrace();
        }


        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                current_video[0]--;
                if (current_video[0] == 0) {
                    prevBtn.setVisibility(View.INVISIBLE);
                }else{
                    prevBtn.setVisibility(View.VISIBLE);
                }
                forwardBtn.setVisibility(View.VISIBLE);
                if(isLocal) {
                    videoView.setVideoURI(Uri.parse("file:///" + Uri.parse(attachments.get(current_video[0]).getData())));

                }else{
                    videoView.setVideoURI(Uri.parse(attachments.get(current_video[0]).getData()));
                }
                pageNumber.setText(String.valueOf(current_video[0] + 1) + "/" + videoCount);
                pageNumber.bringToFront();
                videoView.start();
            }
        });
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                current_video[0]++;
                if (current_video[0] == videoCount - 1) {
                    forwardBtn.setVisibility(View.INVISIBLE);
                }else{
                    forwardBtn.setVisibility(View.VISIBLE);
                }
                prevBtn.setVisibility(View.VISIBLE);
                if(isLocal) {
                    videoView.setVideoURI(Uri.parse("file:///" + Uri.parse(attachments.get(current_video[0]).getData())));

                }else{
                    videoView.setVideoURI(Uri.parse(attachments.get(current_video[0]).getData()));
                }
                pageNumber.setText(String.valueOf(current_video[0] + 1) + "/" + videoCount);
                pageNumber.bringToFront();
                videoView.start();

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
