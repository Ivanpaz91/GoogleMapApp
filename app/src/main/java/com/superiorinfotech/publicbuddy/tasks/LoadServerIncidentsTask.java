package com.superiorinfotech.publicbuddy.tasks;

/**
 * Created by alex on 20.01.15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.MainActivity;
import com.superiorinfotech.publicbuddy.adapters.SavedIncidentsCardAdapter;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Category;
import com.superiorinfotech.publicbuddy.db.model.Entity;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.db.model.MediaAttachment;
import com.superiorinfotech.publicbuddy.db.model.Organization;
import com.superiorinfotech.publicbuddy.db.model.SubCategory;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;
import com.superiorinfotech.publicbuddy.enums.MediaType;
import com.superiorinfotech.publicbuddy.utils.DateConvert;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This task is used to asynchronously parse JSON returned by WebService for Login request.
 * Response JSON contains information about Organization/Entity/Category/Subcategory which
 * current user has access to. This JSON must be parsed and:
 * - If DB has no such Organization - insert it
 * - If DB has no such Entity - insert it
 * - If DB has no such Category - insert it
 * - If DB has no Subcategory from the list - insert them
 * - Clear CredentialsMapper Table and create new entries for the user.
 */
public class LoadServerIncidentsTask extends AsyncTask<Void, Void, Boolean> {

    private final JsonObject json;
    private Context context;
    private SavedIncidentsCardAdapter mAdapter;
    User user;
    private ArrayList<MediaAttachment> mMediaAttachments;

    public LoadServerIncidentsTask(Context context, JsonObject json, User user, SavedIncidentsCardAdapter adapter) {
        this.context = context;
        this.json = json;
        this.user = user;
        this.mAdapter = adapter;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //First we need to remove all old credentials for user
        user.clearUploadedIncidents();

        DataSource ds = new DataSource(IncidentReporter.getContext());
        SQLiteDatabase database = ds.getDatabase();

        JsonArray allIncidentsResult = json.get("GetAllIncidentsResult").getAsJsonArray();
         Integer REQUIRED_SIZE = -1;
        for(JsonElement jIncident: allIncidentsResult) {

            JsonObject jIncidentObject = jIncident.getAsJsonObject();
            if(!jIncidentObject.get("msgReturned").getAsString().equals("No Incident Found")){
                Integer imageCount = jIncidentObject.get("imageCount").getAsInt();
                Integer audioCount = jIncidentObject.get("audioCount").getAsInt();
                Integer videoCount = jIncidentObject.get("videoCount").getAsInt();
                JsonArray imagePaths = jIncidentObject.getAsJsonArray("imagePaths");
                JsonArray audioPaths = jIncidentObject.getAsJsonArray("audioPaths");
                JsonArray videoPaths = jIncidentObject.getAsJsonArray("videoPaths");

                String comments;
                Long categoryID = jIncidentObject.get("categoryID").getAsLong();
                if(jIncidentObject.get("comments")!=null){
                     comments = jIncidentObject.get("comments").getAsString().replace("\\", "");
                }else{
                    comments="";
                }
                String createDate;
                if(jIncidentObject.get("createdDate")!=null){
                    createDate = jIncidentObject.get("createdDate").getAsString().replace("\\", "");
                }else{
                    createDate="";
                }


                String folderPath = jIncidentObject.get("folderPath").getAsString();


                Long incidentID = jIncidentObject.get("incidentID").getAsLong();
                Double latitude = jIncidentObject.get("latitude").getAsDouble();
                Double longitude = jIncidentObject.get("longitude").getAsDouble();


                Long orgEntityID = jIncidentObject.get("orgEntityID").getAsLong();


                String userName = jIncidentObject.get("userName").getAsString();

                ContentValues cv = new ContentValues();
                cv.put("IncidentID", incidentID);
                cv.put("User", user.getUserId());
//            cv.put("Organization", );
                cv.put("Entity", orgEntityID);
                cv.put("Category", categoryID);
                cv.put("Status", Incident.UPLOADED);
               // cv.put("DateCreated", DateConvert.convert1(createDate));
                cv.put("DateCreated", createDate);

                cv.put("Latitude", latitude);
                cv.put("Longitude", longitude);
                cv.put("Comment", comments);
//            cv.put("", );
//            cv.put("", );
//            cv.put("", );

                long _id = 0;

                _id = database.insert("Incidents", null, cv);

                if (_id != -1) {

                    //set media values

                    cv.clear();
                    cv.put("incidentID", _id);
                    if (imageCount > 0) {
                        for (JsonElement imagePath : imagePaths) {
                            cv.put("attachmentType", MediaType.PHOTO.toString());
                            cv.put("attachmentData", imagePath.getAsString());
                            ds.getDatabase().insert("Attachments", null, cv);
                            cv.clear();
                            cv.put("incidentID", _id);
                        }
                    }
                    if (audioCount > 0) {
                        for (JsonElement audioPath : audioPaths) {
                            cv.put("attachmentType", MediaType.AUDIO.toString());
                            cv.put("attachmentData", audioPath.getAsString());
                            database.insert("Attachments", null, cv);
                            cv.clear();
                            cv.put("incidentID", _id);
                        }
                    }
                    if (videoCount > 0) {
                        for (JsonElement videoPath : videoPaths) {
                            cv.put("attachmentType", MediaType.VIDEO.toString());
                            cv.put("attachmentData", videoPath.getAsString());
                            database.insert("Attachments", null, cv);
                            cv.clear();
                            cv.put("incidentID", _id);
                        }
                    }

                }
            }


        }

        ds.close();

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        DataSource ds = new DataSource(IncidentReporter.getContext());
        mAdapter.changeCursor(ds.getData(MainActivity.getCurrentUser(), IncidentsFragmentType.Remote));
    }

    @Override
    protected void onCancelled() {
//        mAuthTask = null;
    }
}