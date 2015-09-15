package com.superiorinfotech.publicbuddy.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import static de.keyboardsurfer.android.widget.crouton.Style.*;

/**
 * Created by alex on 18.01.15.
 */
public class MapDialog extends DialogFragment implements OnMapReadyCallback {
    private GoogleMap map;
    private SupportMapFragment fragment;
    private LatLng position;

    private TextView latitude;
    private TextView longitude;

    public MapDialog(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_map, null);

        fragment = new SupportMapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, fragment).commit();

        getDialog().setTitle("Choose Incident Location");
        //todo added


        latitude = (TextView) layout.findViewById(R.id.latitude);
        longitude= (TextView) layout.findViewById(R.id.longitude);

        final EditText address = (EditText) layout.findViewById(R.id.address);
        layout.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> addressesList =geocoder.getFromLocationName(address.getText().toString(), 1);
                    if(addressesList.size()>0 && map!=null){
                        position = new LatLng(addressesList.get(0).getLatitude(), addressesList.get(0).getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
                        //Perform actual camera fly
                        map.animateCamera(cameraUpdate);

                        map.clear();
                        map.addMarker(new MarkerOptions()
                                .position(position)
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        latitude.setText(String.valueOf(position.latitude));
                        longitude.setText(String.valueOf(position.longitude));

                    }

                } catch (IOException e) {
                    Crouton.makeText(getActivity(), "Unable to get location from address", ALERT).show();
                }
            }
        });

        layout.findViewById(R.id.saveLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = getActivity().getIntent();
                returnIntent.putExtra("position", position);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, returnIntent);

                dismiss();
            }
        });

        layout.findViewById(R.id.cancelLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return layout;
    }
    void getCurrentLocation() {
        map.setMyLocationEnabled(true);
        Location myLocation  = map.getMyLocation();
        if(myLocation!=null)
        {
            double dLatitude = myLocation.getLatitude();
            double dLongitude = myLocation.getLongitude();

            map.addMarker(new MarkerOptions().position(
                    new LatLng(dLatitude, dLongitude)).title("My Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 8));

        }
        else
        {
            Toast.makeText(getActivity(), "Unable to fetch the current location", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        //Since map needs time to be loaded, request it asynchronously and use it in  onMapReady callback
        fragment.getMapAsync(MapDialog.this);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //If this dialog was provided a current user location,
        //animate camera to it.
        Bundle args = getArguments();
        if(args!=null){
            Location location = args.getParcelable("position");
            if(location!=null && map!=null) {
                position = new LatLng(location.getLatitude(), location.getLongitude());

                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));

                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Incident location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
                //Perform actual camera fly
                map.animateCamera(cameraUpdate);
            }
        }

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Incident location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                position = latLng;

                latitude.setText(String.valueOf(position.latitude));
                longitude.setText(String.valueOf(position.longitude));
            }
        });

        map.setMyLocationEnabled(true);

        if(position!=null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
            //Perform actual camera fly
            map.animateCamera(cameraUpdate);
        }
    }
}
