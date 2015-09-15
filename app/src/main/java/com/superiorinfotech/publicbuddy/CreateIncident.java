package com.superiorinfotech.publicbuddy;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.fragments.CreateIncidentFragment;
import com.superiorinfotech.publicbuddy.fragments.IncidentsCardFragment;

import de.keyboardsurfer.android.widget.crouton.Crouton;


public class CreateIncident extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_incident);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Hide ActionBar Title
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            Incident incident = new Incident(extras.getLong(Incident.TYPE));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, CreateIncidentFragment.newInstance(incident.getID()))
                    .commit();

            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateIncidentFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_incident, menu);
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

        if (id == R.id.action_logout){
//            SharedPreferences.Editor editor = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE).edit();
//            editor.remove("login");
//            editor.remove("password");
//            editor.commit();

            //Open Login Screen
            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Do you want to sign out?")
                    .setPositiveButton(R.string.yas, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            //Open Login Screen
                            Intent intent = new Intent(CreateIncident.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNeutralButton(android.R.string.cancel, null).create().show();

            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Save before Close?")
                .setMessage("Do you want to save your Incident?")
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CreateIncident.super.onBackPressed();
                        //Delete Incident
                        CreateIncidentFragment fragment = (CreateIncidentFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                        if(fragment.DELETE_ON_EXIT) {
                            fragment.deleteCurrentIncident();
                        }

//                        DataSource ds = new DataSource(CreateIncident.this);
//                        IncidentsCardFragment.getAdapter().changeCursor(ds.getData(MainActivity.getCurrentUser()));
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        CreateIncident.super.onBackPressed();
                        //Save Incident

//                        DataSource ds = new DataSource(CreateIncident.this);
//                        IncidentsCardFragment.getAdapter().changeCursor(ds.getData(MainActivity.getCurrentUser()));
                    }
                }).create().show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}
