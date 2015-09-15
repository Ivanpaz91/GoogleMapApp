package com.superiorinfotech.publicbuddy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.superiorinfotech.publicbuddy.db.model.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by alex on 11.01.15.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "IncidentReporter.db";
    private static final int DATABASE_VERSION = 1;

    private Context context;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

      //  exportDatabse(context, DATABASE_NAME);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Model.TABLE_USERS);

        db.execSQL(Model.TABLE_ORGANIZATION);
        db.execSQL(Model.TABLE_ENTITY);
        db.execSQL(Model.TABLE_CATEGORY);
        db.execSQL(Model.TABLE_SUB_CATEGORY);
        db.execSQL(Model.TABLE_SUB_CATEGORY_VALUES);
        db.execSQL(Model.TABLE_INCIDENTS);
        db.execSQL(Model.TABLE_INCIDENT_ATTACHMENTS);
        db.execSQL(Model.TABLE_INCIDENT_FILL_DATA);

        db.execSQL(Model.TABLE_CREDENTIALS_MAPPER_ORGANIZATION);
        db.execSQL(Model.TABLE_CREDENTIALS_MAPPER_ENTITY);
        db.execSQL(Model.TABLE_CREDENTIALS_MAPPER_CATEGORY);
        db.execSQL(Model.TABLE_CREDENTIALS_MAPPER_SUBCATEGORY);

        Model.generateTestDBData(context, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void exportDatabse(Context context, String databaseName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+context.getPackageName()+"//databases//"+databaseName+"";
                String backupDBPath = "backupname.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
