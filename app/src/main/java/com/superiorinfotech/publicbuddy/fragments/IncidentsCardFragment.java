package com.superiorinfotech.publicbuddy.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.MainActivity;
import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.adapters.SavedIncidentsCardAdapter;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;
import com.superiorinfotech.publicbuddy.events.IncidentSaveEvent;
import com.superiorinfotech.publicbuddy.events.IncidentSendEvent;
import com.superiorinfotech.publicbuddy.events.IncidentUpdatedEvent;
import com.superiorinfotech.publicbuddy.events.MapTouchEvent;
import com.superiorinfotech.publicbuddy.events.SearchInputEvent;
import com.superiorinfotech.publicbuddy.tasks.LoadServerIncidentsTask;
import com.google.gson.JsonObject;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by alex on 03.02.15.
 */
public class IncidentsCardFragment extends Fragment  {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private IncidentsFragmentType fragmentType;

    private View rootView;

    private LoadServerIncidentsTask mLoadIncidentsTask;

    public static IncidentsCardFragment newInstance(IncidentsFragmentType type){

        IncidentsCardFragment fragment= new IncidentsCardFragment();
        fragment.setFragmentType(type);
        return fragment;

    }
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        EventBus eBus = new EventBus();
        eBus.getDefault().register(this);
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_incidents_cards, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SavedIncidentsCardAdapter(getActivity(), MainActivity.getCurrentUser(), fragmentType);
        mRecyclerView.setAdapter(mAdapter);


        if(fragmentType==IncidentsFragmentType.Remote){
            //Query network DB for Incidents
            IncidentReporter.getApiService().getReportedIncidents(MainActivity.getCurrentUser().getUsername(), new Callback<JsonObject>() {
                @Override
                public void success(JsonObject jsonObject, Response response) {
                    mLoadIncidentsTask = new LoadServerIncidentsTask(getActivity(), jsonObject, MainActivity.getCurrentUser(), (SavedIncidentsCardAdapter)mAdapter);
                    mLoadIncidentsTask.execute((Void) null);

                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        DataSource ds = new DataSource(getActivity());
        getAdapter().changeCursor(ds.getData(MainActivity.getCurrentUser(), fragmentType));
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public SavedIncidentsCardAdapter getAdapter(){
        return (SavedIncidentsCardAdapter)mAdapter;
    }

    public void setFragmentType(IncidentsFragmentType fragmentType) {
        this.fragmentType = fragmentType;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//Save the fragment's state here


    }
    public void onEvent(IncidentUpdatedEvent event) {
//        DataSource ds = new DataSource(getActivity());
//        getAdapter().changeCursor(ds.getData(MainActivity.getCurrentUser(), fragmentType));
        mAdapter = new SavedIncidentsCardAdapter(getActivity(), MainActivity.getCurrentUser(), fragmentType);
        mRecyclerView.setAdapter(mAdapter);
    }
    public void onEvent(IncidentSaveEvent event) {
         updateSaveCardStatus();
        ((MainActivity)getActivity()).goToTab(1);
    }

    private void updateSaveCardStatus(){
        mAdapter = new SavedIncidentsCardAdapter(getActivity(), MainActivity.getCurrentUser(), fragmentType);
        mRecyclerView.setAdapter(mAdapter);
    };
    public void onEvent(IncidentSendEvent event) {



        switch(event.succeed) {
            case 1:
                ((MainActivity)getActivity()).goToTab(1);
                Toast.makeText(getActivity(), "Upload Failed ! Try again (Rejected Reason:Data format)", Toast.LENGTH_SHORT).show();
                updateSaveCardStatus();
            break;
            case 2:
                ((MainActivity)getActivity()).goToTab(1);
                 Toast.makeText(getActivity(), "Upload Failed! Try again (Rejected Reason:Network Status)", Toast.LENGTH_SHORT).show();
                 updateSaveCardStatus();
            break;
            case 0:
                ((MainActivity)getActivity()).goToTab(0);
                Toast.makeText(getActivity(), "Upload Succeeded!", Toast.LENGTH_SHORT).show();
                loadServer();
                mAdapter = new SavedIncidentsCardAdapter(getActivity(), MainActivity.getCurrentUser(), fragmentType);
                mRecyclerView.setAdapter(mAdapter);
                break;
            default:
                break;

        }


    }

    public void onEvent(SearchInputEvent event) {

        String query = event.searchText;
        getAdapter().setQuery(query);
        mAdapter.notifyDataSetChanged();

    }
    private void loadServer(){
        if(fragmentType==IncidentsFragmentType.Remote){
            //Query network DB for Incidents
            IncidentReporter.getApiService().getReportedIncidents(MainActivity.getCurrentUser().getUsername(), new Callback<JsonObject>() {
                @Override
                public void success(JsonObject jsonObject, Response response) {
                    mLoadIncidentsTask = new LoadServerIncidentsTask(getActivity(), jsonObject, MainActivity.getCurrentUser(), (SavedIncidentsCardAdapter)mAdapter);
                    mLoadIncidentsTask.execute((Void) null);

                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }
}
