package com.superiorinfotech.publicbuddy.db.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.utils.MD5;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 12.01.15.
 */
public class User {
    public static final String TYPE = "User";

    private Long   userId;
    private String passwordHash;
    private String username;

    private DataSource ds;


    public User(Context context, Long userId){
        ds = new DataSource(context);
        this.userId = userId;

        String sql = "SELECT userName,password FROM Users WHERE ROWID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String password = cursor.getString(cursor.getColumnIndex("password"));
                String username = cursor.getString(cursor.getColumnIndex("userName"));
                cursor.close();
                ds.close();

                this.passwordHash = password;
                this.username = username;
            }
        }
    }

    public Long getUserId(){
        return userId;
    }

    public Boolean checkPassword(String password){
        return MD5.getHash(password).equals(passwordHash);
    }

    public static User createUser(Context context,
                           String firstName,
                           String lastName,
                           String userName,
                           String password,
                           String phone){

        DataSource ds = new DataSource(context);
        ContentValues cv = new ContentValues();
        cv.put("firstName", firstName);
        cv.put("lastName", lastName);
        cv.put("userName", userName);
        cv.put("password", MD5.getHash(password));
        cv.put("phone", phone);

        Long userId = null;


       userId = ds.getDatabase().insert("Users", null, cv);


       return new User(context, userId);


    }

    public ArrayList<String> getSpinnerAdapter(String type, HashMap<String, String> parameters){
        ArrayList<String> result = new ArrayList<String>();
        String sql = "";
        Cursor cursor = null;
        if(Entity.TYPE.equals(type)){
            sql = "SELECT" +
                  "     Entities.entityName " +
                  "FROM " +
                  "     Entities, CredentialsMapper_Entity " +
                  "WHERE " +
                  "     Entities.entityID = CredentialsMapper_Entity.Entity " +
                  "AND " +
                  "     CredentialsMapper_Entity.User = ?";

            cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        }

        if(Category.TYPE.equals(type)){
            sql = "SELECT" +
                    "     Categories.categoryName " +
                    "FROM " +
                    "     Categories, CredentialsMapper_Category " +
                    "WHERE " +
                    "     Categories.categoryID = CredentialsMapper_Category.Category " +
                    "AND " +
                    "     Categories.entityID = (SELECT entityID FROM Entities WHERE entityName = ?)" +
                    "AND " +
                    "     CredentialsMapper_Category.User = ?";

            cursor = ds.getDatabase().rawQuery(sql, new String[]{parameters.get(Entity.TYPE),String.valueOf(userId)});
        }


        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false){
                result.add(cursor.getString(0));
                cursor.moveToNext();
            }

        }

        return result;
    }

    public Organization getOrganization(Context context){
        String sql = "SELECT Organizations.organizationID " +
                     "FROM Organizations, CredentialsMapper_Organization " +
                     "WHERE Organizations.organizationID = CredentialsMapper_Organization.Organization " +
                     " AND " +
                     "CredentialsMapper_Organization.User = ?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("organizationID"));
                cursor.close();
                ds.close();

                return Organization.newInstance(context, id);
            }
        }

        return null;

    }

    public void clearCredentials(){
        //First we need to remove all old entity credentials for user
        ds.getDatabase().delete("CredentialsMapper_Organization", "User=?", new String[]{String.valueOf(this.getUserId())});
        ds.getDatabase().delete("CredentialsMapper_Entity", "User=?", new String[]{String.valueOf(this.getUserId())});
        ds.getDatabase().delete("CredentialsMapper_Category", "User=?", new String[]{String.valueOf(this.getUserId())});
        ds.getDatabase().delete("CredentialsMapper_SubCategory", "User=?", new String[]{String.valueOf(this.getUserId())});
    }

    public void clearUploadedIncidents(){
        ds.getDatabase().delete("Incidents", "User=? AND Status=?", new String[]{String.valueOf(this.getUserId()), String.valueOf(Incident.UPLOADED)});
    }
    public void clearAllIncidents(){
        ds.getDatabase().delete("Incidents", "User=?", new String[]{String.valueOf(this.getUserId())});
    }

    public String getUsername() {
        return username;
    }
}
