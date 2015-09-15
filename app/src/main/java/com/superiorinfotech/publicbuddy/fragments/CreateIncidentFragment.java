package com.superiorinfotech.publicbuddy.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.CreateIncident;
import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.MainActivity;
import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.adapters.TitleSpinnerArrayAdapter;
import com.superiorinfotech.publicbuddy.api.CustomTypedByteArray;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Category;
import com.superiorinfotech.publicbuddy.db.model.Entity;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.db.model.MediaAttachment;
import com.superiorinfotech.publicbuddy.db.model.Organization;
import com.superiorinfotech.publicbuddy.db.model.SubCategory;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.dialogs.AudioPlayerDialog;
import com.superiorinfotech.publicbuddy.dialogs.FillFormDataDialog;
import com.superiorinfotech.publicbuddy.dialogs.MapDialog;
import com.superiorinfotech.publicbuddy.enums.MediaType;
import com.superiorinfotech.publicbuddy.events.IncidentSaveEvent;
import com.superiorinfotech.publicbuddy.events.IncidentSendEvent;
import com.superiorinfotech.publicbuddy.events.IncidentUpdatedEvent;
import com.superiorinfotech.publicbuddy.tasks.AsyncDeleteTask;
import com.superiorinfotech.publicbuddy.utils.CurrentLocation;
import com.superiorinfotech.publicbuddy.utils.DataConnection;
import com.superiorinfotech.publicbuddy.utils.Image;
import com.superiorinfotech.publicbuddy.utils.RecordAudio;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import java.io.*;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class CreateIncidentFragment extends Fragment implements LocationListener {
    private Spinner entity;
    private Spinner category;
    private ImageButton photo1;
    private ImageButton video1;
    private ImageButton audio1;
    private ImageButton mLocation;
    private TextView mOrganization;
    private TableLayout mPhotoTable;
    private TableLayout mVideoTable;
    private TableLayout mAudioTable;
    private TableLayout mLocationTable;
    private TableRow mFillFormDate;
    private EditText mComments;

    private Button mSave;
    private Button mSend;

    private Location location;
    private CurrentLocation mCurrentLocation;

    public final Integer ACTIVITY_TAKE_PHOTO = 1;
    public final Integer ACTIVITY_CHOOSE_PHOTO = 2;
    public final Integer ACTIVITY_CROP_PHOTO = 3;

    private final Integer ACTIVITY_TAKE_VIDEO = 4;
    private final Integer ACTIVITY_CHOOSE_VIDEO = 5;

    private final Integer ACTIVITY_RECORD_AUDIO = 6;
    private final Integer ACTIVITY_CHOOSE_AUDIO = 7;

    private final Integer DIALOG_MAP = 8;
    private final Integer DIALOG_FILL_FORM_DATA = 9;

    private ArrayList<MediaAttachment> mMediaAttachments;

    private Incident mIncident;
    private final User mUser;

    private Integer REQUIRED_SIZE = -1;

    public boolean DELETE_ON_EXIT = true;

    public CreateIncidentFragment() {
        // Required empty public constructor
        mUser = MainActivity.getCurrentUser();
    }

    public static CreateIncidentFragment newInstance(Long incidentID) {
        CreateIncidentFragment fragment = new CreateIncidentFragment();
        Incident incident = new Incident(incidentID);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Incident.TYPE, incident);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaAttachments = new ArrayList<MediaAttachment>();


        if (savedInstanceState != null) {
            mIncident = savedInstanceState.getParcelable(Incident.TYPE);
        } else {
            Bundle arguments = getArguments();
            if (arguments == null || arguments.getParcelable(Incident.TYPE) == null) {
                mIncident = new Incident(getActivity(), mUser);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_incident, container, false);
        mOrganization = (TextView) rootView.findViewById(R.id.organization);
        entity = (Spinner) rootView.findViewById(R.id.entity);
        category = (Spinner) rootView.findViewById(R.id.category);
        photo1 = (ImageButton) rootView.findViewById(R.id.photo1);
        video1 = (ImageButton) rootView.findViewById(R.id.video1);
        audio1 = (ImageButton) rootView.findViewById(R.id.audio1);
        mLocation = (ImageButton) rootView.findViewById(R.id.location1);
        mPhotoTable = (TableLayout) rootView.findViewById(R.id.photoFiles);
        mVideoTable = (TableLayout) rootView.findViewById(R.id.videoFiles);
        mAudioTable = (TableLayout) rootView.findViewById(R.id.audioFiles);
        mLocationTable = (TableLayout) rootView.findViewById(R.id.locationFiles);
        mFillFormDate = (TableRow) rootView.findViewById(R.id.fillData);
        mComments = (EditText) rootView.findViewById(R.id.remarks);

        mSave = (Button) rootView.findViewById(R.id.save);
        mSend = (Button) rootView.findViewById(R.id.send);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (savedInstanceState != null) {
            DELETE_ON_EXIT = savedInstanceState.getBoolean("DELETE_ON_EXIT");
            mMediaAttachments = savedInstanceState.getParcelableArrayList(MediaAttachment.TYPE);
            REQUIRED_SIZE = savedInstanceState.getInt("REQUIRED_SIZE");
            location = savedInstanceState.getParcelable("LOCATION");
            String locationMarker = savedInstanceState.getString("LOCATION_MARKER");
            addLocation(locationMarker);

            HashMap<String, TableLayout> tables = new HashMap<String, TableLayout>();
            tables.put(MediaType.AUDIO.toString(), mAudioTable);
            tables.put(MediaType.VIDEO.toString(), mVideoTable);
            tables.put(MediaType.PHOTO.toString(), mPhotoTable);

            //Restore media attachments from Bundle
            for (MediaAttachment attachment : mMediaAttachments) {
                if (attachment.getType() == MediaType.AUDIO) {
                    addAudio(attachment);
                }

                if (attachment.getType() == MediaType.PHOTO) {
                    addPhoto(attachment);
                }

                if (attachment.getType() == MediaType.VIDEO) {
                    addVideo(attachment);
                }
            }
        } else {
            //Query GPS for position only if location wasn't specified before
            Bundle arguments = getArguments();
            if (arguments == null || arguments.getParcelable(Incident.TYPE) == null) {
                mCurrentLocation = new CurrentLocation(getActivity(), new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location1) {
                        mLocation.setImageResource(R.drawable.location);
                        addLocation("D");
                        location = location1;
                        mIncident.setLocation(location);

                        mCurrentLocation.disconnect();

                    }
                });
            }

        }

        mOrganization.setText(mUser.getOrganization(getActivity()).getOrganizationName());

        entity.setAdapter(new TitleSpinnerArrayAdapter(getActivity(), R.layout.spinner_item_main, R.id.value, mUser.getSpinnerAdapter(Entity.TYPE, null), ""));
        entity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(Entity.TYPE, entity.getSelectedItem().toString());
                category.setAdapter(new TitleSpinnerArrayAdapter(getActivity(), R.layout.spinner_item_main, R.id.value, mUser.getSpinnerAdapter(Category.TYPE, params), ""));
                category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        final Category mCategory = new Category(getActivity(), category.getSelectedItem().toString());
                        if (mCategory != null && mCategory.hasSubCategories()) {
                            mFillFormDate.setVisibility(View.VISIBLE);
                            mFillFormDate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FragmentManager fm = getFragmentManager();
                                    FillFormDataDialog mFormDialog = new FillFormDataDialog();
                                    Bundle arguments = new Bundle();
                                    arguments.putParcelable(Incident.TYPE, mIncident);
                                    arguments.putParcelable(Category.TYPE, mCategory);
                                    mFormDialog.setArguments(arguments);
                                    mFormDialog.setTargetFragment(CreateIncidentFragment.this, DIALOG_FILL_FORM_DATA);
                                    mFormDialog.show(fm, "Fill Form Data");
                                }
                            });
                        } else {
                            mFillFormDate.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Add Photo for Incident
        photo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.select_image, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.take_photo) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                                return true;
                            }

                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getActivity().getExternalFilesDir(null), "user_img_tmp")));
                            startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);

                            return true;
                        }

                        if (menuItem.getItemId() == R.id.choose_photo) {
                            FilesFoldersDialogFragment dialog = FilesFoldersDialogFragment.newInstance(FilesFoldersDialogFragment.PICTURE_FILE);
                            dialog.setTargetFragment(CreateIncidentFragment.this, ACTIVITY_CHOOSE_PHOTO);
                            dialog.show(getFragmentManager(), "PHOTO_DIALOG");
                            return true;
                        }

                        return false;
                    }
                });
                popup.show();
            }
        });

        //Add Video Record for Incident
        video1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.select_video, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.record_video) {
                            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                                return true;
                            }

                            intent.putExtra("android.intent.extra.durationLimit", 60);
//DEBUG
//                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getActivity().getExternalFilesDir(null), "user_video_tmp")));
                            startActivityForResult(intent, ACTIVITY_TAKE_VIDEO);

                            return true;
                        }

                        if (menuItem.getItemId() == R.id.choose_video) {
                            FilesFoldersDialogFragment dialog = FilesFoldersDialogFragment.newInstance(FilesFoldersDialogFragment.VIDEO_FILE);
                            dialog.setTargetFragment(CreateIncidentFragment.this, ACTIVITY_CHOOSE_VIDEO);
                            dialog.show(getFragmentManager(), "VIDEO_DIALOG");

                            return true;
                        }

                        return false;
                    }
                });
                popup.show();
            }
        });

        //Add Audio Record for Incident
        audio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.select_audio, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.record_audio) {
                            AudioButtonAlert(v);
                            return true;
                        }


//                        }

                        if (menuItem.getItemId() == R.id.choose_audio) {
                            FilesFoldersDialogFragment dialog = FilesFoldersDialogFragment.newInstance(FilesFoldersDialogFragment.AUDIO_FILE);
                            dialog.setTargetFragment(CreateIncidentFragment.this, ACTIVITY_CHOOSE_AUDIO);
                            dialog.show(getFragmentManager(), "AUDIO_DIALOG");
                            return true;
                        }

                        return false;
                    }
                });
                popup.show();
            }
        });

        ViewTreeObserver vto = photo1.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                REQUIRED_SIZE = photo1.getHeight();

                Bundle arguments = getArguments();
                if (arguments != null) {
                    Incident incident = arguments.getParcelable(Incident.TYPE);
                    if (incident != null) {
                        //To prevent deleting this Incident on exit
                        DELETE_ON_EXIT = false;

                        mIncident = incident;
                        Organization org = Organization.newInstance(getActivity(), mIncident.getOrgID());
                        if (org != null) {
                            mOrganization.setText(org.getOrganizationName());
                        }
                        AdapterView.OnItemSelectedListener listener = entity.getOnItemSelectedListener();
                        entity.setOnItemSelectedListener(null);

                        entity.setSelection(((ArrayAdapter) entity.getAdapter()).getPosition(Entity.newInstance(getActivity(), mIncident.getEntID()).getEntityName()), true);

                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put(Entity.TYPE, entity.getSelectedItem().toString());
                        category.setAdapter(new TitleSpinnerArrayAdapter(getActivity(), R.layout.spinner_item_main, R.id.value, mUser.getSpinnerAdapter(Category.TYPE, params), ""));
                        category.setSelection(((ArrayAdapter) category.getAdapter()).getPosition(Category.newInstance(getActivity(), mIncident.getCatID()).getCategoryName()), true);

                        entity.setOnItemSelectedListener(listener);
                        mComments.setText(mIncident.getComments());

                        location = incident.getLocation();
                        addLocation("M");

                        //Restore MediaAttachments
                        for (MediaAttachment attachment : incident.getAttachments()) {
                            mMediaAttachments.add(attachment);

                            if (attachment.getType() == MediaType.PHOTO) {
                                addPhoto(attachment);
                            }

                            if (attachment.getType() == MediaType.VIDEO) {
                                addVideo(attachment);
                            }

                            if (attachment.getType() == MediaType.AUDIO) {
                                addAudio(attachment);
                            }
                        }

                    }
                }

                ViewTreeObserver obs = photo1.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                MapDialog mMapDialog = new MapDialog();
                Bundle arguments = new Bundle();
                arguments.putParcelable("position", location);
                mMapDialog.setArguments(arguments);
                mMapDialog.setTargetFragment(CreateIncidentFragment.this, DIALOG_MAP);
                mMapDialog.show(fm, "Choose Incident location");
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIncident();

                getActivity().finish();

                EventBus.getDefault().post(new IncidentSaveEvent());

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //First, need to save incident
                saveIncident();
                //  mIncident.setUploadedStatus(Incident.UPLOADED);
                //If Data Connection is not available - return
                if (!DataConnection.isDataConnectionAvailable(getActivity())) {
                    getActivity().finish();
                    return;
                }

                String entityID = String.valueOf(Entity.getID(entity.getSelectedItem().toString()));
                String categoryID = String.valueOf(Category.getID(category.getSelectedItem().toString()));

                //Iterate through all subcategories and add their length
                Map<String, String> subcategories = new HashMap<>();
                Integer subCtgrCount = 0;
                for (Long subcategory : mIncident.getReportedSubcatogiries()) {
                    ++subCtgrCount;
                    subcategories.put("subCategoryID_" + subCtgrCount, String.valueOf(subcategory));
                }

                String latitude = null;
                String longitude = null;

                try {

                    latitude = String.valueOf(location.getLatitude());
                    longitude = String.valueOf(location.getLongitude());
                } catch (Exception e) {
                    //                    latitude = mCurrentLocation


                    Log.d("gps location", latitude + " :" + longitude);
                    e.printStackTrace();
                }


                String remarks = mComments.getText().toString();

                Map<String, TypedFile> mediaAttachments = new HashMap<String, TypedFile>();

                Integer imgCount = 0;
                Integer vidCount = 0;
                Integer audCount = 0;

                for (MediaAttachment attachment : mMediaAttachments) {
                    if (attachment.getType() == MediaType.PHOTO) {
                        ++imgCount;
                        mediaAttachments.put("image_" + imgCount, new TypedFile("image/jpeg", attachment.getAttachment()));
                    }

                    if (attachment.getType() == MediaType.VIDEO) {
                        ++vidCount;
                        mediaAttachments.put("video_" + vidCount, new TypedFile("video/mp4", attachment.getAttachment()));
                    }

                    if (attachment.getType() == MediaType.AUDIO) {
                        ++audCount;
                        mediaAttachments.put("audio_" + audCount, new TypedFile("audio/mp3", attachment.getAttachment()));
                    }
                }

                //Set DB Status to UPLOADING
                mIncident.setUploadedStatus(Incident.UPLOADING);
                Toast.makeText(getActivity(),"The incident is on Uploading!",Toast.LENGTH_SHORT).show();
                IncidentReporter.getApiService().reportIncident(mUser.getUsername(),
                        entityID,
                        categoryID,
                        subcategories,
                        latitude,
                        longitude,
                        remarks,
                        mediaAttachments,
                        new Callback<JsonObject>() {

                            @Override
                            public void success(JsonObject jsonObject, Response response) {
                                //TODO Update DB status
                              //  mIncident.setUploadedStatus(Incident.UPLOADED);

                                Boolean isSuccess = new Incident().deleteById(String.valueOf(mIncident.getID()));
                                if(isSuccess){
                                    EventBus.getDefault().post(new IncidentSendEvent(0));

                                }else{
                                    mIncident.setUploadedStatus(Incident.SAVED);
                                 //   Toast.makeText(getActivity(),"Error!",Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().post(new IncidentSendEvent(1));
                                }

                            }

                            @Override
                            public void failure(RetrofitError error) {
                                mIncident.setUploadedStatus(Incident.SAVED);

                              //  DataSource ds = new DataSource(IncidentReporter.getContext());
                                EventBus.getDefault().post(new IncidentSendEvent(2));

                            }
                        });

                getActivity().finish();
            }
        });

        mComments.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !TextUtils.isEmpty(mComments.getText())) {
                    mIncident.setComment(mComments.getText().toString());
                }
            }
        });
    }

    private void saveIncident() {
        mIncident.setOrganization(mOrganization.getText().toString());
        mIncident.setEntity(entity.getSelectedItem().toString());
        mIncident.setCategory(category.getSelectedItem().toString());
        mIncident.setAttachments(mMediaAttachments);
        mIncident.setComment(mComments.getText().toString());
        mIncident.setLocation(location);


//        DataSource ds = new DataSource(getActivity());
//        IncidentsCardFragment.getAdapter().refresh();
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("DELETE_ON_EXIT", DELETE_ON_EXIT);
        savedInstanceState.putParcelable(Incident.TYPE, mIncident);
        savedInstanceState.putParcelableArrayList(MediaAttachment.TYPE, mMediaAttachments);
        savedInstanceState.putInt("REQUIRED_SIZE", REQUIRED_SIZE);
        savedInstanceState.putParcelable("LOCATION", location);

        if (mLocation.getTag() != null) {
            savedInstanceState.putString("LOCATION_MARKER", mLocation.getTag().toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCurrentLocation != null) {
            try {
                mCurrentLocation.disconnect();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            addPhoto(new File(getActivity().getExternalFilesDir(null), "user_img_tmp").getAbsolutePath());
            return;
        }

        if (requestCode == ACTIVITY_CHOOSE_PHOTO && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra("path");
            if (filePath != null && !TextUtils.isEmpty(filePath)) {
                addPhoto(filePath);
            }
            return;
        }

        if (requestCode == ACTIVITY_TAKE_VIDEO && resultCode == Activity.RESULT_OK) {
//DEBUG
//            addVideo(new File(getActivity().getExternalFilesDir(null), "user_video_tmp").getAbsolutePath());

            try {
                AssetFileDescriptor videoAsset = getActivity().getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                FileInputStream fis = videoAsset.createInputStream();
                File tmpFile = new File(Environment.getExternalStorageDirectory(), "user_video_tmp");
                FileOutputStream fos = new FileOutputStream(tmpFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = fis.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fis.close();
                fos.close();

                addVideo(tmpFile.getAbsolutePath());
                //Delete file from Galery location
                new File(data.getData().getPath()).delete();
            } catch (IOException io_e) {
                // TODO: handle error
                io_e.printStackTrace();
            }

            return;
        }

        if (requestCode == ACTIVITY_CHOOSE_VIDEO && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra("path");
            if (filePath != null && !TextUtils.isEmpty(filePath)) {
                addVideo(filePath);
            }
            return;
        }

        if (requestCode == ACTIVITY_RECORD_AUDIO && resultCode == Activity.RESULT_OK) {
            String sourcePath = getRealPathFromURI(data.getData());
            File sourceF = new File(sourcePath);
            String destPath = new File(getActivity().getExternalFilesDir(null), "user_audio_tmp.mp3").getAbsolutePath();
            try {
                sourceF.renameTo(new File(destPath));
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            addAudio(new File(getActivity().getExternalFilesDir(null), "user_audio_tmp.mp3").getAbsolutePath());
            return;
        }

        if (requestCode == ACTIVITY_CHOOSE_AUDIO && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra("path");
            if (filePath != null && !TextUtils.isEmpty(filePath)) {
                addAudio(filePath);
            }
            return;
        }

        if (requestCode == DIALOG_MAP && resultCode == Activity.RESULT_OK) {
            LatLng latLng = data.getParcelableExtra("position");
            location = new Location("manual");
            if (latLng != null) {
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
            }
            addLocation("C");

            mIncident.setLocation(location);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void addPhoto(String filePath) {

        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = (ImageView) row.findViewById(R.id.preview);
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);
        final File file = new File(filePath);

        MediaAttachment attachment = new MediaAttachment(mIncident, MediaType.PHOTO, new File(filePath).getAbsolutePath(), REQUIRED_SIZE);

        //<---
        mPreview.setImageBitmap(BitmapFactory.decodeFile(attachment.getAttachmentPreview().getAbsolutePath()));
        mPreview.getLayoutParams().height = REQUIRED_SIZE;
        mPreview.getLayoutParams().width = REQUIRED_SIZE;
        mPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(intent);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                mMediaAttachments.remove(v.getTag());
            }
        });
        mPhotoTable.addView(row);

        mMediaAttachments.add(attachment);
        mDelete.setTag(attachment);
    }

    private void addPhoto(final MediaAttachment attachment) {
        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);
        final File file = attachment.getAttachmentPreview();

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());


        //<---
        mPreview.setImageBitmap(bitmap);
        mPreview.getLayoutParams().height = REQUIRED_SIZE;
        mPreview.getLayoutParams().width = REQUIRED_SIZE;
        mPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(attachment.getAttachment()), "image/*");
                startActivity(intent);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                mMediaAttachments.remove(v.getTag());
            }
        });
        mPhotoTable.addView(row);
        mDelete.setTag(attachment);
    }

    private void addVideo(String filePath) {
        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);
        final File file = new File(filePath);

        MediaAttachment attachment = new MediaAttachment(mIncident, MediaType.VIDEO, new File(filePath).getAbsolutePath(), REQUIRED_SIZE);
//
        mPreview.setImageBitmap(BitmapFactory.decodeFile(attachment.getAttachmentPreview().getAbsolutePath()));
        mPreview.getLayoutParams().height = REQUIRED_SIZE;
        mPreview.getLayoutParams().width = REQUIRED_SIZE;
        mPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                startActivity(intent);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                mMediaAttachments.remove(container.getTag());
            }
        });
        mVideoTable.addView(row);

        mMediaAttachments.add(attachment);
        row.setTag(attachment);
    }

    private void addVideo(final MediaAttachment attachment) {
        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);
        final File file = attachment.getAttachmentPreview();

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        //<---
        mPreview.setImageBitmap(bitmap);
        mPreview.getLayoutParams().height = REQUIRED_SIZE;
        mPreview.getLayoutParams().width = REQUIRED_SIZE;
        mPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(attachment.getAttachment()), "video/*");
                startActivity(intent);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                mMediaAttachments.remove(container.getTag());
            }
        });
        mVideoTable.addView(row);

        row.setTag(attachment);
    }

    private void addAudio(String filePath) {
        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);
        final File file = new File(filePath);
        mPreview.setImageResource(R.drawable.audio);

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                startActivity(intent);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                mMediaAttachments.remove(container.getTag());
            }
        });
        mAudioTable.addView(row);

        MediaAttachment attachment = new MediaAttachment(mIncident, MediaType.AUDIO, new File(filePath).getAbsolutePath(), REQUIRED_SIZE);
        mMediaAttachments.add(attachment);
        row.setTag(attachment);
    }

    private void addAudio(final MediaAttachment attachment) {
        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);

        //<---
        mPreview.setImageResource(R.drawable.audio);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(attachment.getAttachment()), "audio/*");
                startActivity(intent);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                mMediaAttachments.remove(container.getTag());
            }
        });
        mAudioTable.addView(row);

        row.setTag(attachment);
    }

    private void addLocation(String marker) {
        mLocation.setTag(marker);
        // Inflate your row "template" and fill out the fields.
        TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        ImageView mDelete = (ImageView) row.findViewById(R.id.removeRow);

        if (marker != null && !TextUtils.isEmpty(marker)) {
            if (marker.equalsIgnoreCase("d")) {
                mPreview.setImageResource(R.drawable.location_gps);
            } else {
                mPreview.setImageResource(R.drawable.location_user);
            }

        }

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row);
                container.invalidate();

                location = null;
            }
        });
        mLocationTable.removeAllViews();
        mLocationTable.addView(row);

    }

    @Override
    public void onLocationChanged(Location loc) {
        mLocation.setImageResource(R.drawable.location);
        addLocation("D");
        this.location = loc;
        mIncident.setLocation(location);

        mCurrentLocation.disconnect();
    }

    public void deleteCurrentIncident() {
        mIncident.delete();
    }

    private void AudioButtonAlert(final View view) {



        AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
        alertbox.setMessage("Are you willing to record the audio?");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                         recordAudio(view);
                    }
                });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

            // do something when the button is clicked
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });

        // display box
        alertbox.show();


    }

    private void recordAudio(View v) {
        v.setEnabled(false);

        final RecordAudio mAudioRecorder = new RecordAudio(getActivity());
        mAudioRecorder.startRecording();

        // Inflate your row "template" and fill out the fields.
        final TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.tablelayout_mediafiles_row, null);
        ImageView mPreview = ((ImageView) row.findViewById(R.id.preview));
        final ImageView deleteRecord = (ImageView) row.findViewById(R.id.removeRow);
        mPreview.setImageResource(R.drawable.audio);
        deleteRecord.setVisibility(View.INVISIBLE);
        final ImageButton mStopRecord = (ImageButton) row.findViewById(R.id.stopRecord);
        mStopRecord.setVisibility(View.VISIBLE);
        mStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioRecorder.stopRecording();
                mStopRecord.setVisibility(View.GONE);
                deleteRecord.setVisibility(View.VISIBLE);
                audio1.setEnabled(true);

                MediaAttachment attachment = new MediaAttachment(mIncident, MediaType.AUDIO, new File(IncidentReporter.getContext().getExternalFilesDir(null).getPath(), "user_audio_tmp.mp3").getAbsolutePath(), REQUIRED_SIZE);
                mMediaAttachments.add(attachment);
                row.setTag(attachment);
            }
        });

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioRecorder.getRecordStatus() == 0) {
//                                            Intent intent = new Intent();
//                                            intent.setAction(android.content.Intent.ACTION_VIEW);

                    View row = (View) v.getParent();
                    TableRow row1 = (TableRow) row.getParent();
                    MediaAttachment attachment = (MediaAttachment) row1.getTag();

                    List<MediaAttachment> attachments = new LinkedList<MediaAttachment>();
                    attachments.add(attachment);

                    AudioPlayerDialog dialog = new AudioPlayerDialog(getActivity(), attachments, false);


                } else {
                    Toast.makeText(getActivity(), "you have to stop audio before preview", Toast.LENGTH_SHORT).show();
                }

            }
        });
//                                File file = new File(getActivity().getExternalFilesDir(null), "user_img_tmp");

        deleteRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // row is your row, the parent of the clicked button
                View row = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row.getParent());
                // delete the row and invalidate your view so it gets redrawn
                TableRow row1 = (TableRow) row.getParent();
                MediaAttachment attachment = (MediaAttachment) row1.getTag();
                container.removeView(row);
                container.invalidate();


                mMediaAttachments.remove(attachment);

                File file = new File(attachment.getData());
                boolean deleted = file.delete();
                if (deleted) {

                } else {
                    Toast.makeText(getActivity(), "Delete Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAudioTable.addView(row);
    }


}