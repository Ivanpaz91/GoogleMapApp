package com.superiorinfotech.publicbuddy.db.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.superiorinfotech.publicbuddy.db.DataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 12.01.15.
 */
public class SubCategory {
    public static final String TYPE = "SubCategory";

    private String subCategoryName;
    private Long subCategoryID;
//    private String subCategoryValue;
    private List<SubcategoryValue> values;

    private DataSource ds;

    public SubCategory(Context context, Long id){
        this.subCategoryID = id;
        ds = new DataSource(context);
        values = new ArrayList<>();

        String sql = "SELECT subCategoryName FROM SubCategories WHERE subCategoryID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("subCategoryName"));
//                String value = cursor.getString(cursor.getColumnIndex("subCategoryValue"));
                cursor.close();
                ds.close();

                this.subCategoryName = name;
//                this.subCategoryValue = value;
            }
        }

        fillSubCategoryValues(context);

    }

    private void fillSubCategoryValues(Context context) {
        String sql = "SELECT subcategoryValueServerID, sabCategoryValue FROM SubCategoryValues WHERE subCategoryID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(subCategoryID)});
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Long id = cursor.getLong(cursor.getColumnIndex("subcategoryValueServerID"));
                String value = cursor.getString(cursor.getColumnIndex("sabCategoryValue"));

                values.add(new SubcategoryValue(id, value));
                cursor.moveToNext();
            }
            cursor.close();
            ds.close();
        }
    }

    public static SubCategory newInstance(Context context, Long id){
        DataSource ds = new DataSource(context);

        String sql = "SELECT subCategoryName FROM SubCategories WHERE subCategoryID=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{String.valueOf(id)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("subCategoryName"));
//                String value = cursor.getString(cursor.getColumnIndex("subCategoryValue"));
                cursor.close();
                ds.close();

                return new SubCategory(context, id, name/*, value*/);
            }
        }

        return null;
    }

    public static SubCategory newInstance(Context context, String name){
        DataSource ds = new DataSource(context);

        String sql = "SELECT subCategoryID FROM SubCategories WHERE subCategoryName=?";
        Cursor cursor = ds.getDatabase().rawQuery(sql, new String[]{name});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Long id = cursor.getLong(cursor.getColumnIndex("subCategoryID"));
                cursor.close();
                ds.close();

                return new SubCategory(context, id, name);
            }
        }

        return null;
    }

    private SubCategory(Context context, Long id, String entityName/*, String value*/){
        this.ds = new DataSource(context);
        this.subCategoryName = entityName;
        this.subCategoryID = id;
//        this.subCategoryValue = value;
        values = new ArrayList<>();

        fillSubCategoryValues(context);
    }

    public static SubCategory createSubCategory(Context context,
//                                          String subCategoryID,
                                          String subCategoryName,
                                          String categoryID/*,
                                          String value*/){

        ContentValues cv = new ContentValues();
        cv.put("categoryID", categoryID);
        cv.put("subCategoryName", subCategoryName);
//        cv.put("subCategoryID", subCategoryID);
//        cv.put("subCategoryValue", value);
        Long id = new DataSource(context).getDatabase().insert("SubCategories", null, cv);

        return new SubCategory(context, id);
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public Long getSubCategoryID() {
        return subCategoryID;
    }

//    public String getSubCategoryValue() {
//        return subCategoryValue;
//    }
    public List<SubcategoryValue> getValues(){
        return values;
    }

    public void givePermission(User user) {
        //Add new credentials
        ContentValues cv = new ContentValues();
        cv.put("User", user.getUserId());
        cv.put("SubCategory", subCategoryID);
        ds.getDatabase().insertWithOnConflict("CredentialsMapper_SubCategory", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        ds.close();
    }

    public void addValue(String id, String value) {
        ContentValues cv = new ContentValues();
        cv.put("subCategoryID", this.getSubCategoryID());
        cv.put("sabCategoryValue", value);
        cv.put("subcategoryValueServerID", id);
        ds.getDatabase().insertWithOnConflict("SubCategoryValues", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        ds.close();
    }
}
