package com.superiorinfotech.publicbuddy.db.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.db.DataSource;

/**
 * Created by alex on 12.01.15.
 */
public class Entity {
    public static final String TYPE = "Entity";

    private String entityName;
    private Long entityID;

    private DataSource ds;

    public Entity(Context context, Long id){
        this.entityID = id;
        ds = new DataSource(context);

        String sql = "SELECT entityName FROM Entities WHERE entityID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("entityName"));
                cursor.close();

                this.entityName = name;
            }
        }

        ds.close();
    }

    public static Entity newInstance(Context context, Long id){
        DataSource ds = new DataSource(context);

        String sql = "SELECT entityName FROM Entities WHERE entityID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("entityName"));
                cursor.close();
                ds.close();

                return new Entity(context, id, name);
            }
        }

        ds.close();
        return null;
    }

    private Entity(Context context, Long id, String entityName){
        this.ds = new DataSource(context);
        this.entityName = entityName;
        this.entityID = id;
    }

    public static Entity createEntity(Context context,
                                      String entityID,
                                      String entityName,
                                      String orgID){

        ContentValues cv = new ContentValues();
        cv.put("entityID", entityID);
        cv.put("entityName", entityName);
        cv.put("orgID", orgID);
        DataSource ds = new DataSource(context);
        Long id = ds.getDatabase().insert("Entities", null, cv);
        ds.close();

        return new Entity(context, id);
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityID() {
        return entityID;
    }

    public void givePermission(User user) {
        //Add new credentials
        ContentValues cv = new ContentValues();
        cv.put("User", user.getUserId());
        cv.put("Entity", entityID);
        ds.getDatabase().insertWithOnConflict("CredentialsMapper_Entity", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        ds.close();
    }

    public static Long getID(String entity) {
        DataSource ds = new DataSource(IncidentReporter.getContext());

        String sql = "SELECT entityID FROM Entities WHERE entityName=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{entity});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("entityID"));
                cursor.close();
                ds.close();

                return id;
            }
        }

        ds.close();
        return null;
    }
}
