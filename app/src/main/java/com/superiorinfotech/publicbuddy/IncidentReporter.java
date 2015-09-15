package com.superiorinfotech.publicbuddy;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;

import com.crashlytics.android.Crashlytics;
import com.superiorinfotech.publicbuddy.api.ApiService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.fabric.sdk.android.Fabric;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;


//import io.vov.vitamio.MediaPlayer;
import retrofit.RestAdapter;

/**
 * Created by alex on 13.01.15.
 */

@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        formUri = "http://acra.asoftstudio.com/report/report.php",
        httpMethod = org.acra.sender.HttpSender.Method.POST,
        reportType = org.acra.sender.HttpSender.Type.JSON
)
public class IncidentReporter extends Application {
    public static final String SHARED_PREFERENCES = "PublicBuddyIncidentReporter";

    private static final String API_URL = "http://ec2-52-27-24-214.us-west-2.compute.amazonaws.com/pbws/PBService.svc/";
    private static final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_URL).build();
    private static final ApiService apiService = restAdapter.create(ApiService.class);

    private static Context context;
    private static MediaPlayer mMediaPlayer ;
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        this.context = this;
        // The following line triggers the initialization of ACRA
   //     ACRA.init(this);

    }

    public static RestAdapter getRestAdapter() {
        return restAdapter;
    }

    public static ApiService getApiService() {
        if (BuildConfig.DEBUG) {
            getRestAdapter().setLogLevel(RestAdapter.LogLevel.FULL);
        }
        return apiService;
    }
    public static MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }
    public static void setMediaPlayer(MediaPlayer player) {
         mMediaPlayer = player;
    }
}
