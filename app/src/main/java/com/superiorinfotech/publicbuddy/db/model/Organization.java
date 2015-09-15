package com.superiorinfotech.publicbuddy.db.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.db.DataSource;

/**
 * Created by alex on 11.01.15.
 */
public class Organization {
    public static final String TYPE = "Organization";
    private String organizationName;
    private Long organizationID;

    private DataSource ds;

    private Organization(Context context, Long id){
        this.organizationID = id;
        ds = new DataSource(context);

        String sql = "SELECT organizationName FROM Organizations WHERE organizationID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("organizationName"));
                cursor.close();
                ds.close();

                this.organizationName = name;
            }
        }

    }

    public static Organization newInstance(Context context, Long id){
        DataSource ds = new DataSource(context);

        String sql = "SELECT organizationName FROM Organizations WHERE organizationID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("organizationName"));
                cursor.close();
                ds.close();

                return new Organization(context, id, name);
            }
        }

        ds.close();
        return null;
    }

    private Organization(Context context, Long id, String organizationName){
        this.ds = new DataSource(context);
        this.organizationName = organizationName;
        this.organizationID = id;
    }

    public static Organization createOrganization(Context context,
                                           String organizationID,
                                           String organizationName){

        ContentValues cv = new ContentValues();
        cv.put("organizationID", organizationID);
        cv.put("organizationName", organizationName);
        Long id = new DataSource(context).getDatabase().insert("Organizations", null, cv);

        return new Organization(context, id);
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public Long getOrganizationID() {
        return organizationID;
    }

    public void givePermission(User user) {
        ContentValues cv = new ContentValues();
        cv.put("User", user.getUserId());
        cv.put("Organization", organizationID);
        ds.getDatabase().insertWithOnConflict("CredentialsMapper_Organization", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        ds.close();
    }

    public static Long getID(String organization) {
        DataSource ds = new DataSource(IncidentReporter.getContext());

        String sql = "SELECT organizationID FROM Organizations WHERE organizationName=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{organization});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("organizationID"));
                cursor.close();
                ds.close();

                return id;
            }
        }

        ds.close();
        return null;
    }
}
