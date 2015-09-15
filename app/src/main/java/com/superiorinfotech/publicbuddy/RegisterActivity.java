package com.superiorinfotech.publicbuddy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.tasks.UserLoginTask;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity implements LoaderCallbacks<Cursor> {
    public static final Integer ACTIVITY_TAKE_PHOTO = 1;
    public static final Integer ACTIVITY_CHOOSE_PHOTO = 2;
    public static final Integer ACTIVITY_CROP_PHOTO = 3;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextView mPinView;
    private TextView mUsernameView;
    private EditText mPasswordView;
    private EditText mRetypePasswordView;
    private AutoCompleteTextView mEmailView;
    private EditText mFirstnameView;
    private EditText mLastnameView;
    private EditText mPrimaryPhoneView;

    private View mProgressView;
    private View mRegisterFormView;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Hide ActionBar Title
      // getActionBar().setDisplayShowTitleEnabled(false);

        mPinView            = (EditText) findViewById(R.id.pin);
        mUsernameView       = (EditText) findViewById(R.id.username);
        mPasswordView       = (EditText) findViewById(R.id.password);
        mRetypePasswordView = (EditText) findViewById(R.id.repeatpassword);
        mEmailView          = (AutoCompleteTextView) findViewById(R.id.email);
        mFirstnameView      = (EditText) findViewById(R.id.restoreUsername_firstName);
        mLastnameView       = (EditText) findViewById(R.id.restoreUsername_lastName);
        mPrimaryPhoneView   = (EditText) findViewById(R.id.phone);
        mContext = this;
        // Set up the login form.

        populateAutoComplete();


//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptRegister();
//                    return true;
//                }
//                return false;
//            }
//        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        ImageView mUserImage = (ImageView) findViewById(R.id.userImage);
        mUserImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(RegisterActivity.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.select_image, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.take_photo){
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (intent.resolveActivity(getPackageManager()) == null) {
                                return true;
                            }

                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getExternalFilesDir(null), "user_img_tmp")));
                            startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);


//                            Intent intent = new Intent("com.android.camera.action.CROP");
//                            File file = new File(RegisterActivity.this.getCacheDir(), "user_img_tmp");
//                            Uri uri = Uri.fromFile(file);
//                            intent.setDataAndType(uri, "image/*");
//                            intent.putExtra("crop", "true");
//                            intent.putExtra("aspectX", 1);
//                            intent.putExtra("aspectY", 1);
//                            intent.putExtra("outputX", 96);
//                            intent.putExtra("outputY", 96);
//                            intent.putExtra("noFaceDetection", true);
//                            intent.putExtra("return-data", false);
//                            startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);

                            return true;
                        }

                        if(menuItem.getItemId() == R.id.choose_photo){

                            return true;
                        }

                        return false;
                    }
                });
                popup.show();
            }
        });


        mRegisterFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if (requestCode == ACTIVITY_TAKE_PHOTO && resultCode == RESULT_OK){
            File file = new File(getExternalFilesDir(null), "user_img_tmp");

            ImageView mUserImage = (ImageView) findViewById(R.id.userImage);
            mUserImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {

        // Reset errors.
        mPinView.setError(null);;
        mUsernameView.setError(null);;
        mPasswordView.setError(null);;
        mRetypePasswordView.setError(null);;
        mEmailView.setError(null);;
        mFirstnameView.setError(null);;
        mLastnameView.setError(null);;
        mPrimaryPhoneView.setError(null);;

        // Store values at the time of the login attempt.
        String pin = mPinView.getText().toString();
        final String username = mUsernameView.getText().toString();
        final String password = mPasswordView.getText().toString();
        String retypePassword = mRetypePasswordView.getText().toString();
        String email = mEmailView.getText().toString();
        String firstname = mFirstnameView.getText().toString();
        String lastname = mLastnameView.getText().toString();
        String primaryPhone = mPrimaryPhoneView.getText().toString();

        if (!checkFields()) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
//            mAuthTask = new UserRegisterTask(email, password);
//            mAuthTask.execute((Void) null);
            JsonObject request = new JsonObject();

            request.addProperty("email", email);
            request.addProperty("entityPin", pin);
            request.addProperty("firstName",firstname);
            request.addProperty("lastName",lastname);
            request.addProperty("msgReturned", "");
            request.addProperty("opStatus",0);
            request.addProperty("password",password);
            request.addProperty("phone",primaryPhone);
            request.addProperty("tempPasswordYN", false);
            request.addProperty("userName",username);
            IncidentReporter.getApiService().registerUser(request, new Callback<JsonObject>() {
                @Override
                public void success(JsonObject jsonObject, Response response) {
                    showProgress(false);

                    if(jsonObject.get("opStatus").getAsInt()==0) {
                        DataSource ds = new DataSource(RegisterActivity.this, username);
                        User user = new User(RegisterActivity.this, ds.getUserId());

                        //If login Successful but local user entry doesn't exist, need to create it
                        if (user.getUserId() == -1) {
                            JsonObject jUser = jsonObject.get("user").getAsJsonObject();
                            String firstName = jUser.get("firstName").getAsString();
                            String lastName = jUser.get("lastName").getAsString();
                            String userName = jUser.get("userName").getAsString();
//                          Should not obtain password value from JSON, since it's not there. Rather use the one, user specified.
                            String phone = jUser.get("phone").getAsString();
                            user = User.createUser(RegisterActivity.this, firstName, lastName, userName, password, phone);
                        }
                        Toast.makeText(mContext,"Registration Succeed!",Toast.LENGTH_LONG).show();

                        mAuthTask = new UserLoginTask(RegisterActivity.this, jsonObject, user);
                        mAuthTask.execute((Void) null);

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra(User.TYPE, user.getUserId());

                        SharedPreferences.Editor editor = getSharedPreferences(IncidentReporter.SHARED_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putString("login", username);
                        editor.putString("password", password);
                        editor.commit();

                        startActivity(intent);

                        finish();
                    }else{
                        int errorCode = jsonObject.get("opStatus").getAsInt();
                        String message = "Error";

                        if(!jsonObject.get("msgReturned").isJsonNull()){
                            message = jsonObject.get("msgReturned").getAsString();
                        }
                        message = message + ",Error Code:" + errorCode;
                        Crouton.makeText(RegisterActivity.this, message, Style.ALERT).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    showProgress(false);
                }
            });
        }
    }

    private boolean checkFields(){
        View focusView = null;
        Boolean cancel = false;

        // Store values at the time of the login attempt.
        String pin = mPinView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String retypePassword = mRetypePasswordView.getText().toString();
        String email = mEmailView.getText().toString();
        String firstname = mFirstnameView.getText().toString();
        String lastname = mLastnameView.getText().toString();
        String primaryPhone = mPrimaryPhoneView.getText().toString();

        //First check if fields are not empty

        // Check the PIN.
        // PIN must be ???
        //TODO check requirements for PIN
        if(TextUtils.isEmpty(pin)){
            mPinView.setError(getString(R.string.error_pin_empty));
            if(focusView==null) {
                focusView = mPinView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(username)){
            mUsernameView.setError(getString(R.string.error_username_empty));
            if(focusView==null) {
                focusView = mUsernameView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_password_empty));
            if(focusView==null) {
                focusView = mPasswordView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(retypePassword)){
            mRetypePasswordView.setError(getString(R.string.error_retypePassword_empty));
            if(focusView==null) {
                focusView = mRetypePasswordView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(email)){
            mEmailView.setError(getString(R.string.error_email_empty));
            if(focusView==null) {
                focusView = mEmailView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(firstname)){
            mFirstnameView.setError(getString(R.string.error_firstname_empty));
            if(focusView==null) {
                focusView = mFirstnameView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(lastname)){
            mLastnameView.setError(getString(R.string.error_lastname_empty));
            if(focusView==null) {
                focusView = mLastnameView;
            }
            cancel = true;
        }

        if(TextUtils.isEmpty(primaryPhone)){
            mPrimaryPhoneView.setError(getString(R.string.error_primaryPhone_empty));
            if(focusView==null) {
                focusView = mPinView;
            }
            cancel = true;
        }


        //Now check fields formats
        if(!checkPin()){
            mPinView.setError(getString(R.string.error_wrong_pin_empty));
            if(focusView==null) {
                focusView = mPinView;
            }
            cancel = true;
        }

        if(!checkPassword()){
            mPasswordView.setError(getString(R.string.error_wrong_password_empty));
            if(focusView==null) {
                focusView = mPasswordView;
            }
            cancel = true;
        }

        //Check if two passwords mach
        if(!password.equals(retypePassword)){
            mPasswordView.setError(getString(R.string.error_passwords_mismatch));
            mRetypePasswordView.setError(getString(R.string.error_passwords_mismatch));
            if(focusView==null) {
                focusView = mPasswordView;
            }
            cancel = true;
        }

        //Check Email format
        if(!checkEmail()){
            mEmailView.setError(getString(R.string.error_email_format));
            if(focusView==null) {
                focusView = mEmailView;
            }
            cancel = true;
        }

        //Check Phone number format
        if(primaryPhone!=null && !TextUtils.isEmpty(primaryPhone) && !Patterns.PHONE.matcher(primaryPhone).matches()){
            mPrimaryPhoneView.setError(getString(R.string.error_phone_format));
            if(focusView==null) {
                focusView = mPrimaryPhoneView;
            }
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
            return cancel;
        }


        return false;
    }

    private boolean checkPin(){
        //TODO: Stub for future PIN validation, if ever needed
        return true;
    }

    private boolean checkPassword(){
        //TODO: Stub for future PIN validation, if ever needed
        return true;
    }

    private boolean checkEmail(){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailView.getText()).matches();
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

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}



