package com.superiorinfotech.publicbuddy;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;
import com.superiorinfotech.publicbuddy.events.IncidentUpdatedEvent;
import com.superiorinfotech.publicbuddy.events.SearchInputEvent;
import com.superiorinfotech.publicbuddy.fragments.IncidentCardDetailFragment;
import com.superiorinfotech.publicbuddy.fragments.IncidentsCardFragment;
import com.superiorinfotech.publicbuddy.fragments.IncidentsMapFragment;
import com.superiorinfotech.publicbuddy.tasks.LoadServerIncidentsTask;
import com.google.gson.JsonObject;

import java.util.Locale;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

//import io.vov.vitamio.MediaPlayer;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private static User user;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        try {
//            if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) {
//                return;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //      IncidentReporter.setMediaPlayer(new MediaPlayer(this));

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Hide ActionBar Title
        actionBar.setDisplayShowTitleEnabled(false);

        //Set current user
        Intent intent = getIntent();
        Long userId = intent.getLongExtra(User.TYPE, -1L);
        this.user = new User(MainActivity.this, userId);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    public void onResume(){
        super.onResume();

    }

    public void onDestroy(){
        super.onDestroy();
        Crouton.cancelAllCroutons();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        menu.findItem(R.id.action_search).collapseActionView();
        if (null != searchView) {
//            searchView.setSearchableInfo(searchManager
//                    .getSearchableInfo(getComponentName()));
         //   searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                EventBus.getDefault().post(new SearchInputEvent(newText));

                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                //Here u can get the value "query" which is entered in the search box.
                EventBus.getDefault().post(new SearchInputEvent(query));

                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == R.id.action_add_incident){
            Intent intent = new Intent(MainActivity.this, CreateIncident.class);
            startActivity(intent);
        }

        if (id == R.id.action_logout){
//            SharedPreferences.Editor editor = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE).edit();
//            editor.remove("login");
//            editor.remove("password");
//            editor.commit();
            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Do you want to sign out?")
                    .setPositiveButton(R.string.yas, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            //Open Login Screen
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNeutralButton(android.R.string.cancel, null).create().show();




            //Close current activity


            return true;
        }
//        if(id == R.id.action_search){
//            SearchManager searchManager = (SearchManager) getSystemService(this.SEARCH_SERVICE);
//            SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)-
//            .getActionView();
//            if (null != searchView) {
//                searchView.setSearchableInfo(searchManager
//                        .getSearchableInfo(getComponentName()));
//                searchView.setIconifiedByDefault(false);
//            }
//
//            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
//                public boolean onQueryTextChange(String newText) {
//                    // this is your adapter that will be filtered
//                    return true;
//                }
//
//                public boolean onQueryTextSubmit(String query) {
//                    //Here u can get the value "query" which is entered in the search box.
//
//                }
//            };
//            searchView.setOnQueryTextListener(queryTextListener);
//
//            return super.onCreateOptionsMenu(menu);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onBackPressed() {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
    }

//-----GETTERS-------------------------------------------------------------------------------------
    public static User getCurrentUser(){
        return user;
    }
//-------------------------------------------------------------------------------------------------

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


//            Create fragment depending on requested tab
            if(position==0){
                return IncidentsCardFragment.newInstance(IncidentsFragmentType.Remote);
            }else if(position==1){
                return IncidentsCardFragment.newInstance(IncidentsFragmentType.Local);
            }else if (position==2) {
                return new IncidentsMapFragment();
            }else{
                return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    public void goToTab(int i){
        mViewPager.setCurrentItem(i);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

    }



}
