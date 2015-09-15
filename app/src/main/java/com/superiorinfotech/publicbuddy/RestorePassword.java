package com.superiorinfotech.publicbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.superiorinfotech.publicbuddy.api.ApiService;
import com.superiorinfotech.publicbuddy.utils.DataConnection;
import com.google.gson.JsonObject;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RestorePassword extends Activity{
    public static final String TYPE = "type";

    RadioButton mRestorePassword;
    RadioButton mChangePassword;
    RadioButton mRestoreUsername;

    LinearLayout mRestorePasswordGroup;
    LinearLayout mChangePasswordGroup;
    LinearLayout mRestoreUsernameGroup;

    Button mRestorePasswordBtn;
    Button mChangePasswordBtn;
    Button mRestoreUsernameBtn;

    RadioGroup mActionRG;

    //Restore Password Group
    EditText mUsername;
    EditText mEmail;

    //Restore Username Group
    EditText mFirstname;
    EditText mLastname;
    //Email field will be reused

    //Change Password Group
    //mUsername will be reused
    EditText mTempPassword;
    EditText mPassword;
    EditText mRepeatPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_password);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Hide ActionBar Title
     //   getActionBar().setDisplayShowTitleEnabled(false);

        mActionRG = (RadioGroup) findViewById(R.id.rgAction);

        mRestorePassword = (RadioButton) findViewById(R.id.rbRestorePassword);
        mChangePassword  = (RadioButton) findViewById(R.id.rbChangePassword);
        mRestoreUsername = (RadioButton) findViewById(R.id.rbRestoreUsername);

        mRestorePasswordGroup = (LinearLayout) findViewById(R.id.restorePasswordGroup);
        mChangePasswordGroup = (LinearLayout) findViewById(R.id.changePasswordGroup);
        mRestoreUsernameGroup = (LinearLayout) findViewById(R.id.restoreUsernameGroup);

        mRestorePasswordBtn = (Button) findViewById(R.id.restorePass_btn);
        mChangePasswordBtn  = (Button) findViewById(R.id.changePass_btn);
        mRestoreUsernameBtn = (Button) findViewById(R.id.restoreUsr_btn);

        mUsername = (EditText) findViewById(R.id.username);
        mEmail = (EditText) findViewById(R.id.email);

        mFirstname = (EditText) findViewById(R.id.restoreUsername_firstName);
        mLastname = (EditText) findViewById(R.id.restoreUsername_lastName);

        mTempPassword = (EditText) findViewById(R.id.tempPassword);
        mPassword     = (EditText) findViewById(R.id.newPassword);
        mRepeatPassword = (EditText) findViewById(R.id.retypeNewPassword);

//-----LISTENERS-----------------------------------------------------------------------------------
        //RadioGroup selection listener
        mActionRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rbRestorePassword){
                    mUsername = (EditText) findViewById(R.id.username);
                    mEmail = (EditText) findViewById(R.id.email);

                    mRestorePasswordGroup.setVisibility(View.VISIBLE);
                    mChangePasswordGroup.setVisibility(View.GONE);
                    mRestoreUsernameGroup.setVisibility(View.GONE);
                    return;
                }

                if(checkedId == R.id.rbChangePassword){
                    mUsername = (EditText) findViewById(R.id.changePassUsername);

                    mRestorePasswordGroup.setVisibility(View.GONE);
                    mChangePasswordGroup.setVisibility(View.VISIBLE);
                    mRestoreUsernameGroup.setVisibility(View.GONE);
                    return;
                }

                if(checkedId == R.id.rbRestoreUsername){
                    mEmail = (EditText) findViewById(R.id.restoreUsername_email);

                    mRestorePasswordGroup.setVisibility(View.GONE);
                    mChangePasswordGroup.setVisibility(View.GONE);
                    mRestoreUsernameGroup.setVisibility(View.VISIBLE);
                    return;
                }
            }
        });

        //"Request Password" button
        mRestorePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DataConnection.isDataConnectionAvailable(RestorePassword.this)){
                    Crouton.makeText(RestorePassword.this, R.string.error_no_data_connection_available, Style.ALERT).show();
                    return;
                }
                if(checkRestorePasswordForm()) {
                    String username = mUsername.getText().toString();
                    String email = mEmail.getText().toString();
                    IncidentReporter.getApiService().restorePassword(username, email, new Callback<JsonObject>() {
                        @Override
                        public void success(JsonObject jsonObject, Response response) {
                            try {
                                JsonObject jForgotPasswordResult = jsonObject.get("ForgotPasswordResult").getAsJsonObject();
                                if (jForgotPasswordResult.get("opStatus").getAsInt() == 0) {
                                    Crouton.makeText(RestorePassword.this, jForgotPasswordResult.get("msgReturned").getAsString(), Style.CONFIRM).show();

                                    mChangePassword.setChecked(true);
                                }else{
                                    String message = "Error";
                                    if(!jsonObject.get("msgReturned").isJsonNull()){
                                        message = jsonObject.get("msgReturned").getAsString();
                                    }
                                    Crouton.makeText(RestorePassword.this, message, Style.ALERT).show();
                                }
                            }catch(Exception exception){
                                Log.e("RestorePassword", exception.getMessage());
                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });
                }
            }
        });

        //"Request Username" button
        mRestoreUsernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DataConnection.isDataConnectionAvailable(RestorePassword.this)){
                    Crouton.makeText(RestorePassword.this, R.string.error_no_data_connection_available, Style.ALERT).show();
                    return;
                }

                if(checkRestoreUsernameForm()) {
                    String firstname = mFirstname.getText().toString();
                    String lastname = mLastname.getText().toString();
                    String email = mEmail.getText().toString();
                    IncidentReporter.getApiService().restoreUsername(firstname, lastname, email, new Callback<JsonObject>() {
                        @Override
                        public void success(JsonObject jsonObject, Response response) {
                            try {
                                JsonObject jForgotPasswordResult = jsonObject.get("ForgotUserNameResult").getAsJsonObject();
                                if (jForgotPasswordResult.get("opStatus").getAsInt() == 0) {
                                    String message = getString(R.string.success);
                                    if(!jForgotPasswordResult.get("msgReturned").isJsonNull()){
                                        message = jForgotPasswordResult.get("msgReturned").getAsString();
                                    }
                                    Crouton.makeText(RestorePassword.this, message, Style.CONFIRM).show();

                                    final String username = jForgotPasswordResult.get("userName").getAsString();
                                    new AlertDialog.Builder(RestorePassword.this)
                                            .setTitle("Return to Login?")
                                            .setMessage("Your Username is: "+username+"\nWould you like to return back to Login?")
                                            .setNegativeButton(android.R.string.no, null)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    Intent intent = getIntent();
                                                    intent.putExtra("username", username);
                                                    setResult(Activity.RESULT_OK, intent);
                                                    finish();
                                                }
                                            }).create().show();
                                }else{
                                    String message = "Error";
                                    if(!jForgotPasswordResult.get("msgReturned").isJsonNull()){
                                        message = jForgotPasswordResult.get("msgReturned").getAsString();
                                    }
                                    Crouton.makeText(RestorePassword.this, message, Style.ALERT).show();
                                }
                            }catch(Exception exception){
                                Log.e("RestorePassword", exception.getMessage());
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });
                }
            }
        });

        //"Change Password" button
        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DataConnection.isDataConnectionAvailable(RestorePassword.this)){
                    Crouton.makeText(RestorePassword.this, R.string.error_no_data_connection_available, Style.ALERT).show();
                    return;
                }

                if(checkChangePasswordForm()){
                    String username = mUsername.getText().toString();
                    String tempPass = mTempPassword.getText().toString();
                    String password = mPassword.getText().toString();
                    IncidentReporter.getApiService().changePassword(username, tempPass, password, new Callback<JsonObject>() {
                        @Override
                        public void success(JsonObject jsonObject, Response response) {
                            try {
                                JsonObject jChangePasswordResult = jsonObject.get("ChangePasswordResult").getAsJsonObject();
                                if (jChangePasswordResult.get("opStatus").getAsInt() == 0) {
                                    String message = getString(R.string.success);
                                    if(!jChangePasswordResult.get("msgReturned").isJsonNull()){
                                        message = jChangePasswordResult.get("msgReturned").getAsString();
                                    }
                                    Crouton.makeText(RestorePassword.this, message, Style.CONFIRM).show();

                                    new AlertDialog.Builder(RestorePassword.this)
                                            .setTitle("Return to Login?")
                                            .setMessage(message+"\nWould you like to return back to Login?")
                                            .setNegativeButton(android.R.string.no, null)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    Intent intent = getIntent();
                                                    setResult(Activity.RESULT_OK, intent);
                                                    finish();
                                                }
                                            }).create().show();
                                }else{
                                    String message = "Error";
                                    if(!jChangePasswordResult.get("msgReturned").isJsonNull()){
                                        message = jChangePasswordResult.get("msgReturned").getAsString();
                                    }
                                    Crouton.makeText(RestorePassword.this, message, Style.ALERT).show();
                                }
                            }catch(Exception exception){
                                Log.e("RestorePassword", exception.getMessage());
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Crouton.makeText(RestorePassword.this, "Error", Style.ALERT).show();
                        }
                    });
                }
            }
        });

        //MUST BE LAST ACTION IN METHOD
        //If TYPE was provided via intent, need to force this type
        Intent intent = getIntent();
        String type = intent.getStringExtra(TYPE);
        if(type!=null && type.equals(LoginActivity.TYPE_CHANGE_PASSWORD)){
            mChangePassword.setChecked(true);
            mRestorePassword.setEnabled(false);
            mRestoreUsername.setEnabled(false);
        }
    }

    private Boolean checkRestorePasswordForm(){
        Boolean hasError = false;
        mUsername.setError(null);
        mEmail.setError(null);

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches()){
            mEmail.setError(getString(R.string.error_email_format));
            hasError = true;
        }

        if(TextUtils.isEmpty(mUsername.getText().toString())){
            mUsername.setError(getString(R.string.error_username_empty));
            hasError = true;
        }

        if(TextUtils.isEmpty(mEmail.getText().toString())){
            mEmail.setError(getString(R.string.error_email_empty));
            hasError = true;
        }

        return !hasError;
    }

    private Boolean checkChangePasswordForm(){
        Boolean hasError = false;
        Boolean hasBothPasswords = true;
        mUsername.setError(null);
        mTempPassword.setError(null);
        mPassword.setError(null);
        mRepeatPassword.setError(null);

        if(TextUtils.isEmpty(mUsername.getText().toString())){
            mUsername.setError(getString(R.string.error_username_empty));
            hasError = true;
        }

        if(TextUtils.isEmpty(mTempPassword.getText())){
            mTempPassword.setError(getString(R.string.error_temp_password_empty));
            hasError = true;
        }

        if(TextUtils.isEmpty(mPassword.getText())){
            mPassword.setError(getString(R.string.error_password_empty));
            hasError = true;
            hasBothPasswords = false;
        }

        if(TextUtils.isEmpty(mRepeatPassword.getText())){
            mRepeatPassword.setError(getString(R.string.error_field_is_required));
            hasError = true;
            hasBothPasswords = false;
        }

        if(hasBothPasswords && !mPassword.getText().toString().equals(mRepeatPassword.getText().toString())){
            mPassword.setError(getString(R.string.error_passwords_mismatch));
            mRepeatPassword.setError(getString(R.string.error_passwords_mismatch));
            hasError = true;
        }

        return !hasError;
    }

    private Boolean checkRestoreUsernameForm(){
        Boolean hasError = false;
        mFirstname.setError(null);
        mLastname.setError(null);
        mEmail.setError(null);

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches()){
            mEmail.setError(getString(R.string.error_email_format));
            hasError = true;
        }

        if(TextUtils.isEmpty(mFirstname.getText().toString())){
            mFirstname.setError(getString(R.string.error_firstname_empty));
            hasError = true;
        }

        if(TextUtils.isEmpty(mLastname.getText().toString())){
            mLastname.setError(getString(R.string.error_lastname_empty));
            hasError = true;
        }

        if(TextUtils.isEmpty(mEmail.getText().toString())){
            mEmail.setError(getString(R.string.error_email_empty));
            hasError = true;
        }

        return !hasError;
    }

    public void onDestroy(){
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

}
