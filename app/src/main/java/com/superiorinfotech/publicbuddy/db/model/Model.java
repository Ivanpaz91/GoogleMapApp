package com.superiorinfotech.publicbuddy.db.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.utils.MD5;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Created by alex on 11.01.15.
 */
public class Model {
    public static final String TABLE_USERS = "" +
            "CREATE TABLE Users (" +
            "   ROWID     INTEGER PRIMARY KEY NOT NULL, " +
            "   firstName TEXT NOT NULL," +
            "   lastName  TEXT NOT NULL," +
            "   userName  TEXT NOT NULL UNIQUE collate nocase," +
            "   password  TEXT NOT NULL," +
            "   phone     TEXT" +
            ");";

    public static final String TABLE_CREDENTIALS_MAPPER_ORGANIZATION = "" +
            "CREATE TABLE CredentialsMapper_Organization (" +
            "   User         INTEGER NOT NULL REFERENCES Users(ROWID) PRIMARY KEY," +
            "   Organization INTEGER NOT NULL REFERENCES Organizations(organizationID)" +
            ");";

    public static final String TABLE_CREDENTIALS_MAPPER_ENTITY = "" +
            "CREATE TABLE CredentialsMapper_Entity (" +
            "   User         INTEGER NOT NULL REFERENCES Users(ROWID)," +
            "   Entity       INTEGER NOT NULL REFERENCES Entities(entityID)" +
            ");";

    public static final String TABLE_CREDENTIALS_MAPPER_CATEGORY = "" +
            "CREATE TABLE CredentialsMapper_Category (" +
            "   User         INTEGER NOT NULL REFERENCES Users(ROWID)," +
            "   Category     INTEGER NOT NULL REFERENCES Categories(categoryID)" +
            ");";

    public static final String TABLE_CREDENTIALS_MAPPER_SUBCATEGORY = "" +
            "CREATE TABLE CredentialsMapper_SubCategory (" +
            "   User         INTEGER NOT NULL REFERENCES Users(ROWID)," +
            "   SubCategory  INTEGER NOT NULL REFERENCES SubCategories(subCategoryID)" +
            ");";

    public static final String TABLE_ORGANIZATION = "" +
            "CREATE TABLE Organizations (" +
            "   organizationID   INTEGER NOT NULL PRIMARY KEY," +
            "   organizationName TEXT NOT NULL UNIQUE" +
            ");";

    public static final String TABLE_ENTITY = "CREATE TABLE Entities (" +
            "entityID     INTEGER NOT NULL PRIMARY KEY, " +
            "entityName   TEXT NOT NULL, " +
            "orgID        INTEGER NOT NULL REFERENCES Organizations(organizationID), " +
            "UNIQUE (entityName, orgID) ON CONFLICT REPLACE" +
            ");";

    public static final String TABLE_CATEGORY = "CREATE TABLE Categories (" +
            "categoryID   INTEGER NOT NULL PRIMARY KEY, " +
            "entityID     INTEGER NOT NULL REFERENCES Entities(entityID), " +
            "categoryName TEXT NOT NULL, " +
            "UNIQUE (categoryName, entityID) ON CONFLICT REPLACE" +
            ");";

    public static final String TABLE_SUB_CATEGORY = "CREATE TABLE SubCategories (" +
            "subCategoryID    INTEGER NOT NULL PRIMARY KEY, " +
            "categoryID       INTEGER NOT NULL REFERENCES Categories(categoryID), " +
            "subCategoryName  TEXT NOT NULL, " +
            "UNIQUE (subCategoryName, subCategoryID) ON CONFLICT REPLACE" +
            ");";

    public static final String TABLE_SUB_CATEGORY_VALUES = "CREATE TABLE SubCategoryValues (" +
            "subCategoryID    INTEGER NOT NULL REFERENCES SubCategories(subCategoryID), " +
            "subcategoryValueServerID INTEGER NOT NULL," +
            "sabCategoryValue TEXT NOT NULL, " +
            "UNIQUE (subCategoryID, sabCategoryValue) ON CONFLICT REPLACE" +
            ");";

    public static final String TABLE_INCIDENTS = "CREATE TABLE Incidents (" +
            "ROWID        INTEGER NOT NULL PRIMARY KEY," +
            "IncidentID   INTEGER, " +
            "User         INTEGER NOT NULL REFERENCES Users(ROWID) ON DELETE CASCADE," +
            "Organization INTEGER REFERENCES Organizations(organizationID)," +
            "Entity       INTEGER REFERENCES Entities(entityID)," +
            "Category     INTEGER REFERENCES Categories(categoryID)," +
            "Status       INTEGER, " +
            "DateCreated  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "DateModified INTEGER, " +
            "Latitude     TEXT, " +
            "Longitude    TEXT, " +
            "Comment      TEXT, " +
            "Temp         INTEGER DEFAULT 0" +
            ");";

    public static final String TABLE_INCIDENT_ATTACHMENTS = "CREATE TABLE Attachments (" +
            "ROWID          INTEGER NOT NULL PRIMARY KEY , " +
            "incidentID     INTEGER NOT NULL REFERENCES Incidents(ROWID) ON DELETE CASCADE, " +
            "attachmentType TEXT NOT NULL, " +
            "attachmentData TEXT" +
            ");";

    public static final String TABLE_INCIDENT_FILL_DATA = "CREATE TABLE FillData (" +
            "incidentID     INTEGER NOT NULL REFERENCES Incidents(ROWID) ON DELETE CASCADE," +
            "dataValue      INTEGER NOT NULL" +
            ");";

    public static void generateTestDBData(Context context, SQLiteDatabase db){
//        //Add 2 users
//        db.execSQL("INSERT INTO Users(User, Password) VALUES('Alex', '"+ MD5.getHash("alex")+"')");
//        db.execSQL("INSERT INTO Users(User, Password) VALUES('Anurag', '"+MD5.getHash("anurag")+"')");
//
//        //Add 2 Organizations
//        db.execSQL("INSERT INTO Organizations (Organization) VALUES ('ASoftStudio');");
//        db.execSQL("INSERT INTO Organizations (Organization) VALUES ('Superior IT');");
//
//        //Add 4 Entities: 2 per each Organization
//        db.execSQL("INSERT INTO Entities (Entity, Organization) VALUES('R&D', 1);");
//        db.execSQL("INSERT INTO Entities (Entity, Organization) VALUES('Accounting', 1);");
//        db.execSQL("INSERT INTO Entities (Entity, Organization) VALUES('Legal', 2);");
//        db.execSQL("INSERT INTO Entities (Entity, Organization) VALUES('IT', 2);");
//
//        db.execSQL("INSERT INTO Categories (Category, Entity) VALUES('Android', 1);");
//        db.execSQL("INSERT INTO Categories (Category, Entity) VALUES('iOS', 1);");
//        db.execSQL("INSERT INTO Categories (Category, Entity) VALUES('Blackberry', 1);");
//        db.execSQL("INSERT INTO Categories (Category, Entity) VALUES('1C', 2);");
//        db.execSQL("INSERT INTO Categories (Category, Entity) VALUES('Court', 3);");
//        db.execSQL("INSERT INTO Categories (Category, Entity) VALUES('Test-1', 4);");
//
//
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
//        byte[] data = outputStream.toByteArray();
//        SQLiteStatement statement = db.compileStatement("INSERT INTO Incidents(Organization, Entity, Category, Status, Image1, Image2, Image3, Video, Audio, Latitude, longitude) VALUES(1,1,1,-1,?, null, ?, null, null, '50.42456137', '30.50660157')");
//        statement.bindBlob(1, data);
//        statement.bindBlob(2, data);
//        statement.executeInsert();
//        statement.clearBindings();
//
//        statement = db.compileStatement("INSERT INTO Incidents(Organization, Entity, Category, Status, Image1, Image2, Image3, Video, Audio, Latitude, longitude) VALUES(1,1,1,0,?, null, null, ?, ?, '50.42856137', '30.50610157')");
//        statement.bindBlob(1, data);
//        statement.bindBlob(2, data);
//        statement.bindBlob(3, data);
//        statement.executeInsert();
//        statement.clearBindings();
//
//        statement = db.compileStatement("INSERT INTO Incidents(Organization, Entity, Category, Status, Image1, Image2, Image3, Video, Audio, Latitude, longitude) VALUES(1,1,1,1,?, null, null, null, ?, '50.32856137', '30.52610157')");
//        statement.bindBlob(1, data);
//        statement.bindBlob(2, data);
//        statement.executeInsert();
//        statement.clearBindings();
//
//        Random random = new Random();
////        for(int i=1; i<10; i++) {
////            Location tmpLoc = new Location("");
////            tmpLoc.setLatitude(50.42456137+(random.nextInt(i) / 10));
////            tmpLoc.setLongitude(30.50660157+(random.nextInt(i) / 10));
////
////            db.execSQL("INSERT INTO Incidents(Organization, Entity, Category, Status, Image1, Image2, Image3, Video, Audio, Latitude, longitude) VALUES(1,1,1,-1,null, null, null, null, null, '"+tmpLoc.getLatitude()+"', '"+tmpLoc.getLongitude()+"')");
////        }
//
//        //Add credentials
//        db.execSQL("INSERT INTO CredentialsMapper_Organization(User, Organization) VALUES(1,1)");
//        db.execSQL("INSERT INTO CredentialsMapper_Organization(User, Organization) VALUES(2,2)");
//
//        db.execSQL("INSERT INTO CredentialsMapper_Entity(User, Entity) VALUES(1,1)");
//        db.execSQL("INSERT INTO CredentialsMapper_Entity(User, Entity) VALUES(1,2)");
//        db.execSQL("INSERT INTO CredentialsMapper_Entity(User, Entity) VALUES(2,4)");
//
//        db.execSQL("INSERT INTO CredentialsMapper_Category(User, Category) VALUES(1,1)");
//        db.execSQL("INSERT INTO CredentialsMapper_Category(User, Category) VALUES(1,2)");
//        db.execSQL("INSERT INTO CredentialsMapper_Category(User, Category) VALUES(1,4)");
//        db.execSQL("INSERT INTO CredentialsMapper_Category(User, Category) VALUES(2,5)");
//        db.execSQL("INSERT INTO CredentialsMapper_Category(User, Category) VALUES(2,6)");

    }
}
