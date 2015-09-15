package com.superiorinfotech.publicbuddy.db.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.db.DataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 12.01.15.
 */
public class Category implements Parcelable{
    public static final String TYPE = "Category";

    private String categoryName;
    private Long categoryID;

    private List<SubCategory> subCategories;

    private DataSource ds;

    private Category(Context context, Long id){
        this.categoryID = id;
        this.subCategories = new ArrayList<SubCategory>();
        ds = new DataSource(context);

        String sql = "SELECT categoryName FROM Categories WHERE categoryID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("categoryName"));
                cursor.close();
                ds.close();

                this.categoryName = name;
            }
        }

        fillSubCategories(context);

    }

    public Category (Context context, String name){
        this.categoryName = name;
        this.subCategories = new ArrayList<SubCategory>();
        ds = new DataSource(context);

        String sql = "SELECT categoryID FROM Categories WHERE categoryName=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{name});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("categoryID"));
                cursor.close();
                ds.close();

                this.categoryID = id;
            }
        }

        fillSubCategories(context);
    }

    public static Category newInstance(Context context, Long id){
        DataSource ds = new DataSource(context);

        String sql = "SELECT categoryName FROM Categories WHERE categoryID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("categoryName"));
                cursor.close();
                ds.close();

                return new Category(context, id, name);
            }
        }

        return null;
    }

    public Category(Parcel in){
        this.categoryName = in.readString();
        this.categoryID = in.readLong();
        in.readList(subCategories, List.class.getClassLoader());
    }

    private Category(Context context, Long id, String entityName){
        this.ds = new DataSource(context);
        this.categoryName = entityName;
        this.categoryID = id;
        this.subCategories = new ArrayList<SubCategory>();

        fillSubCategories(context);
    }

    private void fillSubCategories(Context context){
        String sql = "SELECT subCategoryID FROM SubCategories WHERE categoryID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(categoryID)});
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Long subCategoryID = cursor.getLong(cursor.getColumnIndex("subCategoryID"));

                subCategories.add(SubCategory.newInstance(context, subCategoryID));
                cursor.moveToNext();
            }
            cursor.close();
            ds.close();
        }
    }

    public boolean hasSubCategories(){
        return subCategories.size()>0;
    }

    public List<SubCategory> getSubCategories(){
        return subCategories;
    }

    public static Category createCategory(Context context,
                                        String categoryID,
                                        String categoryName,
                                        String entityID){

        ContentValues cv = new ContentValues();
        cv.put("entityID", entityID);
        cv.put("categoryName", categoryName);
        cv.put("categoryID", categoryID);
        Long id = new DataSource(context).getDatabase().insert("Categories", null, cv);

        return new Category(context, id);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getCategoryID() {
        return categoryID;
    }

    public void givePermission(User user) {
        //Add new credentials
        ContentValues cv = new ContentValues();
        cv.put("User", user.getUserId());
        cv.put("Category", categoryID);
        ds.getDatabase().insertWithOnConflict("CredentialsMapper_Category", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryName);
        dest.writeLong(categoryID);
        dest.writeList(subCategories);
    }

    public static Long getID(String category) {
        DataSource ds = new DataSource(IncidentReporter.getContext());

        String sql = "SELECT categoryID FROM Categories WHERE categoryName=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{category});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("categoryID"));
                cursor.close();
                ds.close();

                return id;
            }
        }

        ds.close();
        return null;
    }
}
