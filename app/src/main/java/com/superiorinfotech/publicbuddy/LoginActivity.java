package com.superiorinfotech.publicbuddy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Category;
import com.superiorinfotech.publicbuddy.db.model.Entity;
import com.superiorinfotech.publicbuddy.db.model.Organization;
import com.superiorinfotech.publicbuddy.db.model.SubCategory;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.tasks.UserLoginTask;
import com.superiorinfotech.publicbuddy.utils.DataConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    public static final int ACTIVITY_REGISTER = 1;
    public static final int ACTIVITY_RESTORE = 2;

    public static final String TYPE_CHANGE_PASSWORD = "PASSWORD_CHANGE";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextView mUsername;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox mRememberMe;
    private View mBottomFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);



        //Hide ActionBar Title

        if (getActionBar()!= null) {
            getActionBar().setDisplayShowTitleEnabled(false);
        }


        // Set up the login form.
        mUsername = (TextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress1);
        mBottomFormView = findViewById(R.id.layout_bottom_login);
        TextView mRegisterButton = (TextView) findViewById(R.id.register_btn);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, LoginActivity.ACTIVITY_REGISTER);
            }
        });

        TextView mRestorePassword = (TextView) findViewById(R.id.forgotPass_btn);
        mRestorePassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RestorePassword.class);
                startActivityForResult(intent, ACTIVITY_RESTORE);

            }
        });

        mRememberMe = (CheckBox) findViewById(R.id.rememberMe);
        int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
        mRememberMe.setButtonDrawable(id);
        //Check if "Remember my on this device" has been activated
        SharedPreferences prefs = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE);
        String storedLogin = prefs.getString("login", null);
        String storedPass  = prefs.getString("password", null);
        mRememberMe.setChecked(true);
        if(storedLogin!=null && storedPass!=null){
            //hide keyboard
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );

            mUsername.setText(storedLogin);
            mPasswordView.setText(storedPass);
           // attemptLogin();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LoginActivity.ACTIVITY_REGISTER) {
            //If Registration was successful, fill in Username
            if(resultCode == RESULT_OK){

            }
            if (resultCode == RESULT_CANCELED) {

            }

            return;
        }

        if (requestCode == LoginActivity.ACTIVITY_RESTORE){
            if(resultCode == RESULT_OK && data!=null){
                String username = data.getStringExtra("username");
                mUsername.setText(username);
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsername.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mUsername.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }else if(TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_username_password_empty));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_username_password_empty));
            focusView = mUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //Check if data connection available
            if(!DataConnection.isDataConnectionAvailable(LoginActivity.this)){
                Crouton.makeText(LoginActivity.this, R.string.error_no_data_connection_available, Style.ALERT).show();

                //Need to perform login using cached credentials
                DataSource ds = new DataSource(LoginActivity.this, username);
                User user = new User(LoginActivity.this, ds.getUserId());
                if (user.getUserId() != -1 && user.checkPassword(password)) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(User.TYPE, user.getUserId());
                    startActivity(intent);
                    finish();
                }
            }else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);

                IncidentReporter.getApiService().login(username, password, new Callback<JsonObject>() {
                    @Override
                    public void success(JsonObject jsonObject, Response response) {
                        showProgress(false);
                        if (jsonObject.get("opStatus").getAsInt() == 0) {
                            DataSource ds = new DataSource(LoginActivity.this, username);
                            User user = new User(LoginActivity.this, ds.getUserId());

                            //If login Successful but local user entry doesn't exist, need to create it
                           if (user.getUserId() == -1) {
                               JsonObject jUser = jsonObject.get("user").getAsJsonObject();
                               String firstName = jUser.get("firstName").getAsString();
                               String lastName = jUser.get("lastName").getAsString();
                               String userName = jUser.get("userName").getAsString();
//                          Should not obtain password value from JSON, since it's not there. Rather use the one, user specified.
                               String phone = jUser.get("phone").getAsString();
                               user = User.createUser(LoginActivity.this, firstName, lastName, userName, password, phone);
//                            } else{
//                                user = User.
//                            }
                           }else if(user.getUserId()!= 1){
                               user.clearAllIncidents();
                           }

                            //todo added this code ,testing mode
                           deleteIncidentsWhenAnotherLogin(username,password,user);


                            //If Response user;{} object contains "tempPasswordYN":true User must be forwarded to change password screen
                            if (jsonObject.get("user").getAsJsonObject().get("tempPasswordYN").getAsBoolean()) {
                                Intent intent = new Intent(LoginActivity.this, RestorePassword.class);
                                intent.putExtra(RestorePassword.TYPE, TYPE_CHANGE_PASSWORD);
                                startActivityForResult(intent, ACTIVITY_RESTORE);
                                return;
                            }

                            mAuthTask = new UserLoginTask(LoginActivity.this, jsonObject, user);
                            mAuthTask.execute((Void) null);



                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra(User.TYPE, user.getUserId());
                            startActivity(intent);

                            if (mRememberMe.isChecked()) {
                                SharedPreferences.Editor editor = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE).edit();
                                editor.putString("login", jsonObject.get("user").getAsJsonObject().get("userName").getAsString());
                                editor.putString("password", password);
                                editor.commit();
                            }else{
                                SharedPreferences.Editor editor = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE).edit();
                                editor.putString("login", null);
                                editor.putString("password", null);
                                editor.commit();
                            }

                            finish();
                        } else {
                            String message = "Error";
                            if(!jsonObject.get("msgReturned").isJsonNull()) {
                                message = jsonObject.get("msgReturned").getAsString();
                            }
                            Crouton.makeText(LoginActivity.this, message, Style.ALERT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        showProgress(false);
                    }
                });
            }
        }
    }

    private void deleteIncidentsWhenAnotherLogin(String newUsername,String newUserpassword,User user) {

        SharedPreferences prefs = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE);
        String oldName = prefs.getString("login", null);
        String oldPassword  = prefs.getString("password", null);

        if(!((newUsername.equals(oldName)) && (newUserpassword.equals(oldPassword)))){
            user.clearAllIncidents();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Stub for future username validation, if ever needed
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Stub for future password validation, if ever needed
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mBottomFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mBottomFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mBottomFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}



