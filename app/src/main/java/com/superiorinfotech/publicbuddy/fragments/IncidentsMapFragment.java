package com.superiorinfotech.publicbuddy.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.IncidentCardDetailActivity;
import com.superiorinfotech.publicbuddy.MainActivity;
import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;
import com.superiorinfotech.publicbuddy.utils.DateConvert;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class IncidentsMapFragment extends Fragment  implements GoogleMap.OnMarkerClickListener {
    private GoogleMap map;
    private View mapView;
    Marker myMarker;

    public IncidentsMapFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mapView == null) {
            mapView = inflater.inflate(R.layout.fragment_incidents_map, container, false);
        }

        return mapView;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        try {
            SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
            map = mapFragment.getMap();
            map.setOnMarkerClickListener(this);
        } catch (Exception e) {
            Toast.makeText(getActivity(),"Google Play Service not Exit",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(map!=null) {
            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    DataSource ds = new DataSource(getActivity());
                    Cursor cursor = ds.getData(MainActivity.getCurrentUser(), null);
                    Boolean hasMarkers = false;
                    if (cursor != null) {




                        cursor.moveToFirst();

                        LatLng position;
                        while (cursor.isAfterLast() == false) {
                            String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                            String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));


                            if (latitude != null && longitude != null) {
                                position = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                                //set marker's info title
                                String entityId = cursor.getString(cursor.getColumnIndex("entity_id"));
                                String categoryId = cursor.getString(cursor.getColumnIndex("category_id"));
                                int status;
                                status = cursor.getInt(cursor.getColumnIndex("Status"));
                                int idColumnIndex = 0;
                                if(status == Incident.UPLOADED) {
                                    idColumnIndex = cursor.getColumnIndex("IncidentID");

                                }else {
                                    idColumnIndex = cursor.getColumnIndex("_id");

                                }
                                final String id = cursor.getString(idColumnIndex);
                                String reportId =    id + "-" +entityId +"-" + categoryId;
                                String dateTime =  cursor.getString(cursor.getColumnIndex("datetime"));
                               // change incident id to date time when saved
                                if(status != Incident.UPLOADED){
                                    reportId = DateConvert.convert5(dateTime);
                                }
                                MarkerOptions options = new MarkerOptions()
                                        .position(position)
                                        .title(reportId);

                                if (cursor.getInt(cursor.getColumnIndex("Status")) == Incident.UPLOADED) {
                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }else{
                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                }

                                 myMarker = map.addMarker(options);


                                builder.include(new LatLng(position.latitude, position.longitude));
                                hasMarkers = true;
                            }
//                            if(myMarker!=null){
//
//                             //   myMarker.showInfoWindow();
//
//                            }

                            cursor.moveToNext();
                        }

                        //If there is no points no need to move camera
                        if (hasMarkers) {

                            LatLngBounds bounds = builder.build();
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
                        }
                    }
                }
            });
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
//        if (marker.equals(myMarker))
//        {
        if(marker.isInfoWindowShown()){
            marker.hideInfoWindow();
        }else{
            marker.showInfoWindow();
        }

//            Intent intent = new Intent(getActivity(), IncidentCardDetailActivity.class);
//            intent.putExtra("incidentId",myMarker.get)
        //handle click here

        return true;
    }

}
