package com.superiorinfotech.publicbuddy.db.model;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.MainActivity;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.enums.MediaType;
import com.superiorinfotech.publicbuddy.events.IncidentSendEvent;
import com.superiorinfotech.publicbuddy.events.IncidentUpdatedEvent;
import com.superiorinfotech.publicbuddy.utils.DataConnection;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by alex on 16.01.15.
 */
public class Incident implements Parcelable{
    public static final String TYPE = "Incident";
    private final String TABLE_NAME = "Incidents";

    public static final Integer DO_NOT_UPLOAD = -2;
    public static final Integer SAVED = -1;
    public static final Integer UPLOADING = 0;
    public static final Integer UPLOADED = 1;

    private DataSource ds;

    private Boolean isSaved = false;
    private Boolean hasUserLocation = false;

    //ID of current incident in DB
    private Long id;

    //
    private Long OrgID;
    private Long EntID;
    private Long CatID;
    private List<Long> SubCatIDs;
    private String Comment;
    private Location Location;
    private List<MediaAttachment> attachments;


    /**
     * Restore incident from DB based on it's ID
     */
    public Incident(){}
    public Incident(Long id){
        this.id = id;

        ds = new DataSource(IncidentReporter.getContext());
        SQLiteDatabase db = ds.getDatabase();
        String sql = "SELECT * FROM Incidents WHERE ROWID=?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});

        if(cursor!=null && cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                OrgID = cursor.getLong(cursor.getColumnIndex("Organization"));
                EntID = cursor.getLong(cursor.getColumnIndex("Entity"));
                CatID = cursor.getLong(cursor.getColumnIndex("Category"));
                Comment = cursor.getString(cursor.getColumnIndex("Comment"));
                Location = new Location("FROM DB");
                Location.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
                Location.setLongitude(cursor.getDouble(cursor.getColumnIndex("Longitude")));
                cursor.moveToNext();
            }
        }

        attachments = MediaAttachment.getIncidentAttachments(id);
    }

    public Incident(Context context, User user){
        this.id = createNewIncident(context, user);
    }

    public Incident(Parcel in){
        id = in.readLong();
        isSaved = in.readByte() != 0;
        hasUserLocation = in.readByte() != 0;
        ds = new DataSource(IncidentReporter.getContext());
    }

    private Long createNewIncident(Context context, User user){
        ds = new DataSource(context);
        SQLiteDatabase db = ds.getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO Incidents(" +
                "User," +
                "Organization, " +
                "Entity, " +
                "Category, " +
                "Status, " +
                "Latitude, " +
                "longitude) " +
                "" +
                "VALUES(" +
                "?," +
                "null," +
                "null," +
                "null," +
                "-1," +
                "null, " +
                "null)");

        statement.bindLong(1, user.getUserId());
        Long id = statement.executeInsert();
        statement.clearBindings();
        ds.close();

        return id;
    }

    public void setOrganization(String organization){
        ContentValues cv = new ContentValues();
        cv.put("Organization", Organization.getID(organization));
        ds.getDatabase().update(TABLE_NAME, cv, "ROWID=?", new String[]{String.valueOf(id)});
    }

    public void setEntity(String entity) {
        ContentValues cv = new ContentValues();
        cv.put("Entity", Entity.getID(entity));
        ds.getDatabase().update(TABLE_NAME, cv, "ROWID=?", new String[]{String.valueOf(id)});
    }

    public void setCategory(String category) {
        ContentValues cv = new ContentValues();
        cv.put("Category", Category.getID(category));
        ds.getDatabase().update(TABLE_NAME, cv, "ROWID=?", new String[]{String.valueOf(id)});
    }

    public void delete(){
        ds.getDatabase().delete(TABLE_NAME, "ROWID=?", new String[]{String.valueOf(id)});
        //TODO Need to also delete corresponding folder!!!
    }
    public Boolean deleteById(String incidentId){
        try {
            new DataSource(IncidentReporter.getContext()).getDatabase().delete(TABLE_NAME, "ROWID=?", new String[]{incidentId});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //TODO Need to also delete corresponding folder!!!
        return true;
    }

    private Long createNewIncident(Context context, HashMap<String, String> params){
        DataSource ds = new DataSource(context);
        SQLiteDatabase db = ds.getDatabase();
        SQLiteStatement statement = db.compileStatement("INSERT INTO Incidents(" +
                "Organization, " +
                "Entity, " +
                "Category, " +
                "Status, " +
                "Latitude, " +
                "Longitude," +
                "Comment) " +
                "" +
                "VALUES(" +
                "(SELECT ROWID FROM Organizations WHERE Organization = ?)," +
                "(SELECT ROWID FROM Entities WHERE Entity = ?)," +
                "(SELECT ROWID FROM Categories WHERE Category = ?)," +
                "-1," +
                "?, " +
                "?," +
                "?)");
        statement.bindString(1, params.get(Organization.TYPE));
        statement.bindString(2, params.get(Entity.TYPE));
        statement.bindString(3, params.get(Category.TYPE));

        statement.bindString(4, params.get("Latitude"));
        statement.bindString(5, params.get("Longitude"));

        statement.bindString(6, params.get("Comment"));

        Long id = statement.executeInsert();
        statement.clearBindings();

        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeByte((byte) (isSaved ? 1 : 0));
        dest.writeByte((byte) (hasUserLocation ? 1 : 0));
    }

    public static final Parcelable.Creator<Incident> CREATOR
            = new Parcelable.Creator<Incident>() {
        public Incident createFromParcel(Parcel in) {
            return new Incident(in);
        }

        public Incident[] newArray(int size) {
            return new Incident[size];
        }
    };

    public Long getID(){
        return id;
    }

    public List<Long> getReportedSubcatogiries() {
        ArrayList<Long> result = new ArrayList<>();

        String sql = "SELECT dataValue FROM FillData WHERE incidentID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Long valueID = cursor.getLong(cursor.getColumnIndex("dataValue"));

                result.add(valueID);
                cursor.moveToNext();
            }
            cursor.close();
            ds.close();
        }

        return result;
    }

    public void setSubcategories(List<Long> subcategories){
        SQLiteDatabase db = ds.getDatabase();
        //First, clear old subcategories
        db.delete("FillData", "incidentID=?", new String[]{String.valueOf(id)});

        ContentValues cv = new ContentValues();
        cv.put("incidentID", id);
        for(Long subcategoryID : subcategories){
            cv.put("dataValue", subcategoryID);
            db.insert("FillData", null, cv);
        }
        db.close();
    }

    public void setComment(String comment){
        SQLiteDatabase db = ds.getDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Comment", comment);
        db.update("Incidents", cv, "ROWID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void setUploadedStatus(int i) {
        SQLiteDatabase db = ds.getDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Status", i);
        db.update("Incidents", cv, "ROWID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void setLocation(Location location) {
        SQLiteDatabase db = ds.getDatabase();
        ContentValues cv = new ContentValues();
        if(location!=null){
            cv.put("Latitude", location.getLatitude());
            cv.put("Longitude", location.getLongitude());
            db.update("Incidents", cv, "ROWID=?", new String[]{String.valueOf(id)});
        }



        db.close();
    }

    public void setAttachments(List<MediaAttachment> attachments){
        SQLiteDatabase db = ds.getDatabase();
        db.delete("Attachments", "incidentID=?", new String[]{String.valueOf(id)});
        ContentValues cv = new ContentValues();
        for(MediaAttachment attachment : attachments){
            cv.clear();
            cv.put("incidentID", attachment.getIncidentID());
            cv.put("attachmentType", attachment.getType().toString());
            cv.put("attachmentData", attachment.getData());
            ds.getDatabase().insert("Attachments", null, cv);
        }

        db.close();
    }

    public String getComments() {
        return Comment;
    }

    public Long getOrgID() {
        return OrgID;
    }

    public Long getEntID() {
        return EntID;
    }

    public Long getCatID() {
        return CatID;
    }

    public Location getLocation() {
        return Location;
    }

    public List<MediaAttachment> getAttachments() {
        return attachments;
    }

    public Boolean sendByIdFromLocal(final Context context,User user) {
//        //If Data Connection is not available - return
//        if (!DataConnection.isDataConnectionAvailable(context)) {
//            return false;
//        }


        //Iterate through all subcategories and add their length
        Map<String, String> subcategories = new HashMap<>();
        Integer subCtgrCount = 0;
     for (Long subcategory : getReportedSubcatogiries()) {
        ++subCtgrCount;
        subcategories.put("subCategoryID_" + subCtgrCount, String.valueOf(subcategory));
    }
        //set media files
        Map<String, TypedFile> mediaAttachments = new HashMap<String, TypedFile>();

        Integer imgCount = 0;
        Integer vidCount = 0;
        Integer audCount = 0;

        for (MediaAttachment attachment : attachments) {
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
        String remarks = getComments();
        Toast.makeText(context,"The incident is on Uploading!",Toast.LENGTH_SHORT).show();
        setUploadedStatus(0);
        EventBus.getDefault().post(new IncidentUpdatedEvent());

    IncidentReporter.getApiService().reportIncident(user.getUsername(),
                EntID.toString(),
                CatID.toString(),
                subcategories,
                String.valueOf(Location.getLatitude()),
                String.valueOf(Location.getLongitude()),
                remarks,
                mediaAttachments,
                new Callback<JsonObject>() {


                    @Override
                    public void success(JsonObject jsonObject, Response response) {
                        //TODO Update DB status
                        EventBus.getDefault().post(new IncidentSendEvent(0));
                    if(deleteById(String.valueOf(getID()))){

                        Boolean isSuccess = new Incident().deleteById(getID().toString());


                        if(!isSuccess){
                            setUploadedStatus(-1);
                            EventBus.getDefault().post(new IncidentSendEvent(1));
                            //todo
                            // send warning message
                        //   Toast.makeText(context,"Error while in delete in local!",Toast.LENGTH_SHORT).show();
                        }
                    }
                 }

                    @Override
                    public void failure(RetrofitError error) {
                        setUploadedStatus(-1);
                        EventBus.getDefault().post(new IncidentSendEvent(2));
                    }
                });



        return null;
    }
}
