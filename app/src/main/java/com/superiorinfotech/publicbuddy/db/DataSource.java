package com.superiorinfotech.publicbuddy.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;

/**
 * Created by alex on 11.01.15.
 */
public class DataSource {
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private Context context;
    private static Long userId;

    public DataSource(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    public DataSource(Context context, String currentUser) {
        this.context = context;
        dbHelper = new DBHelper(context);
        userId = getUserId(currentUser);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public void openRead() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public SQLiteDatabase getDatabase(){
        open();
        return database;
    }

    //TODO Change for real getter
    public Cursor getData(User user, IncidentsFragmentType type){
        open();

        String sql = "" +
                "SELECT " +
                "   Incidents.ROWID AS _id, " +
                "   Incidents.Entity AS entity_id, " +
                "   Incidents.Category AS category_id, " +
                "   Incidents.IncidentID, " +
                "   Incidents.Comment, "+
                "   DateCreated as datetime, " +
                "   Organizations.organizationName AS Organization, " +
                "   Entities.entityName AS Entity, " +
                "   Categories.categoryName AS Category, " +
                "   Status, " +
                "   (SELECT COUNT(ROWID) FROM Attachments WHERE incidentID=Incidents.ROWID AND attachmentType='PHOTO' GROUP BY(incidentID)) AS Images, " +
                "   (SELECT COUNT(ROWID) FROM Attachments WHERE incidentID=Incidents.ROWID AND attachmentType='VIDEO' GROUP BY(incidentID)) AS Video, " +
                "   (SELECT COUNT(ROWID) FROM Attachments WHERE incidentID=Incidents.ROWID AND attachmentType='AUDIO' GROUP BY(incidentID)) AS Audio, " +
                "   case when Latitude is null and Longitude is null then 0 else 1 end  AS Location," +
                "   Latitude," +
                "   Longitude " +
                "FROM " +
                "        Incidents " +
                "LEFT JOIN Organizations ON Incidents.Organization = Organizations.organizationID " +
                "LEFT JOIN Entities ON Incidents.Entity = Entities.entityID " +
                "LEFT JOIN Categories ON Categories.categoryID = Incidents.Category " +
                "WHERE" +
                "    Incidents.User=? "  ;
//                "AND Incidents.Status<>?";
        if(type == null){
            return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId())});
        }
//        if(type == IncidentsFragmentType.Local){
//            sql = sql + "AND Incidents.Status = -1" +   "ORDER BY datetime(DateCreated) DESC ";
//
//        }else if (type == IncidentsFragmentType.Remote){
//            sql = sql + "AND Incidents.Status = 1" + "ORDER BY datetime(DateCreated) DESC";
//
//        }
//
//        return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId())});
        if(type == IncidentsFragmentType.Local){
            sql = sql + "AND Incidents.Status=?" +   "ORDER BY datetime(DateCreated) DESC ";
            return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId()), String.valueOf(Incident.SAVED)});
        }else if (type == IncidentsFragmentType.Remote){
            sql = sql + "AND Incidents.Status=?" +   "ORDER BY datetime(DateCreated) DESC" + ",Incidents.IncidentID DESC";
            return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId()), String.valueOf(Incident.UPLOADED)});
        }else{
            sql = sql + "AND Incidents.Status=?" +   "ORDER BY datetime(DateCreated) DESC ";
            return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId()), String.valueOf(Incident.SAVED)});
        }

      // return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId()), String.valueOf(Incident.UPLOADED)});
    }

//    public Cursor getDataByRowId(User user,String rowId){
//        open();
//
//        String sql = "" +
//                "SELECT " +
//                "   Incidents.ROWID AS _id, " +
//                "   Incidents.Entity AS entity_id, " +
//                "   Incidents.Category AS category_id, " +
//                "   Incidents.Comment, "+
//                "   DateCreated as datetime, " +
//                "   Organizations.organizationName AS Organization, " +
//                "   Entities.entityName AS Entity, " +
//                "   Categories.categoryName AS Category, " +
//                "   Status, " +
//                "   (SELECT COUNT(ROWID) FROM Attachments WHERE incidentID=Incidents.ROWID AND attachmentType='PHOTO' GROUP BY(incidentID)) AS Images, " +
//                "   (SELECT COUNT(ROWID) FROM Attachments WHERE incidentID=Incidents.ROWID AND attachmentType='VIDEO' GROUP BY(incidentID)) AS Video, " +
//                "   (SELECT COUNT(ROWID) FROM Attachments WHERE incidentID=Incidents.ROWID AND attachmentType='AUDIO' GROUP BY(incidentID)) AS Audio, " +
//                "   case when Latitude is null and Longitude is null then 0 else 1 end  AS Location," +
//                "   Latitude," +
//                "   Longitude " +
//                "FROM " +
//                "        Incidents " +
//                "LEFT JOIN Organizations ON Incidents.Organization = Organizations.organizationID " +
//                "LEFT JOIN Entities ON Incidents.Entity = Entities.entityID " +
//                "LEFT JOIN Categories ON Categories.categoryID = Incidents.Category " +
//                "WHERE" +
//                "    Incidents.User=? AND _id =? AND Incidents.Status = -1 ASC LIMIT 1 "  ;
////                "AND Incidents.Status<>?";
//
//
//
//        return database.rawQuery(sql, new String[]{String.valueOf(user.getUserId()), rowId});
//    }

    public Long getUserId(String username) {
        open();
        String sql = "SELECT ROWID FROM Users WHERE userName=?";
        Cursor cursor = database.rawQuery(sql, new String[]{username});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("ROWID"));
                cursor.close();
                close();

                this.userId = id;
                return id;
            }
        }

        //Username was not found. Return -1
        return -1L;
    }

    public Long getUserId(){
        return userId;
    }
}
