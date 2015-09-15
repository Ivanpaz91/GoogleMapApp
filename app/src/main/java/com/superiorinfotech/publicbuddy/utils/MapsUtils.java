package com.superiorinfotech.publicbuddy.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;

import com.superiorinfotech.publicbuddy.R;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by alex on 31.01.15.
 */
public class MapsUtils {

//    private void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null)
//        {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.basicMap)).getMap();
//
//            if(isGoogleMapsInstalled())
//            {
//                if (mMap != null)
//                {
//                    setUpMap();
//                }
//            }
//            else
//            {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("Install Google Maps");
//                builder.setCancelable(false);
//                builder.setPositiveButton("Install", getGoogleMapsListener());
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        }
//    }
//
//    public boolean isGoogleMapsInstalled()
//    {
//        try
//        {
//            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
//            return true;
//        }
//        catch(PackageManager.NameNotFoundException e)
//        {
//            return false;
//        }
//    }
//
//    public View.OnClickListener getGoogleMapsListener()
//    {
//        return new View.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
//                startActivity(intent);
//
//                //Finish the activity so they can't circumvent the check
//                finish();
//            }
//        };
//    }
}
