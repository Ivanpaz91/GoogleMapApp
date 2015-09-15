package com.superiorinfotech.publicbuddy.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.superiorinfotech.publicbuddy.IncidentReporter;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.enums.MediaType;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 17.01.15.
 */
public class MediaAttachment implements Parcelable{
    public static final String TYPE = "MediaAttachment";
    private Long id;
    private MediaType type;
    private Long incidentID;
    private String data;
    private static List<MediaAttachment> attachmentsById = new ArrayList<>();

    public MediaAttachment(Incident incident, MediaType type, String filePath, int REQUIRED_SIZE){
        try {
            this.type = type;
            this.incidentID = incident.getID();
            Long incidentID = incident.getID();

            //First, create a record in DB
            ContentValues cv = new ContentValues();
            DataSource ds = new DataSource(IncidentReporter.getContext());
            cv.put("incidentID", incidentID);
            cv.put("attachmentType", type.toString());
            this.id = ds.getDatabase().insert("Attachments", null, cv);

            File mainFile = new File(filePath);
//            File preview  = new File(filePath+"_preview");

            //Create folder structure to to store media attachment
            final String EXTERNAL_FILES_DIR = IncidentReporter.getContext().getExternalFilesDir(null).getPath();
            File rootFolder = new File(EXTERNAL_FILES_DIR + File.separator + incident.getID());

            File attachmentFolder = new File(rootFolder, String.valueOf(id));
            attachmentFolder.mkdirs();

            //File was created in application's temporary files folder & need to be copied
            //todo
//            if(filePath.contains(EXTERNAL_FILES_DIR)){
                //Copy main file
                File attachment;
                if(this.type==MediaType.AUDIO) {
                     attachment = new File(attachmentFolder, "attachment.mp3");
                }else{
                     attachment = new File(attachmentFolder, "attachment");
                }


                InputStream in = new FileInputStream(mainFile);
                OutputStream out = new FileOutputStream(attachment, false);

                cv.clear();
                cv.put("attachmentData", attachment.getAbsolutePath());
                ds.getDatabase().update("Attachments", cv, "ROWID=?", new String[]{String.valueOf(id)});
                this.data = attachment.getAbsolutePath();

                try {
                    IOUtils.copy(in, out);
                }
                finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
//            }else{
//                cv.clear();
//                cv.put("attachmentData", filePath);
//                ds.getDatabase().update("Attachments", cv, "ROWID=?", new String[]{String.valueOf(id)});
//                this.data = filePath;
//            }

            //Create preview file

            if(this.type==MediaType.PHOTO) {
                //Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mainFile.getAbsolutePath(), o);

                //Find the correct scale value. It should be the power of 2.
                int scale = 1;
                while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;

                //Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeFile(mainFile.getAbsolutePath(), o2);

                try {
                    File preview = new File(attachmentFolder, "attachment_preview");
                    FileOutputStream out1 = new FileOutputStream(preview);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if(this.type==MediaType.VIDEO){
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mainFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND);

                try {
                    File preview = new File(attachmentFolder, "attachment_preview");
                    FileOutputStream out2 = new FileOutputStream(preview);
                    thumb.compress(Bitmap.CompressFormat.JPEG, 100, out2);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            if(this.type==MediaType.AUDIO){


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaAttachment(Long incidentID, Long id, MediaType type, String data){
        this.incidentID = incidentID;
        this.id = id;
        this.type = type;
        this.data = data;
    }

    public MediaAttachment(Parcel in){
        this.id = in.readLong();

    }

    public static List<MediaAttachment> getIncidentAttachments(Long incidentID){
        getIncidentAttachmentsById(incidentID);
        return attachmentsById;
    }
    public static void  getIncidentAttachmentsById(Long incidentID){
        ArrayList<MediaAttachment> attachments = new ArrayList<>();
        DataSource ds = new DataSource(IncidentReporter.getContext());
        Cursor cursor = ds.getDatabase().rawQuery("SELECT ROWID AS _id, attachmentData, attachmentType AS type FROM Attachments WHERE incidentID=?", new String[]{String.valueOf(incidentID)});
        if(cursor!=null && cursor.moveToFirst()) {
            while(!cursor.isAfterLast()){
                Long id = cursor.getLong(cursor.getColumnIndex("_id"));
                MediaType type = MediaType.getType(cursor.getString(cursor.getColumnIndex("type")));
                String data = cursor.getString(cursor.getColumnIndex("attachmentData"));
                MediaAttachment attachment = new MediaAttachment(incidentID, id, type, data);

                attachments.add(attachment);
                cursor.moveToNext();
            }

            cursor.close();
        }
        attachmentsById = attachments;
    }
    public static List<MediaAttachment> getIncidentAttachmentsByType(Long incidentID, MediaType type){

        List<MediaAttachment> attachments_out = new ArrayList<>();

        getIncidentAttachmentsById(incidentID);

        for(MediaAttachment attachment:attachmentsById){
            if(attachment.getType() == type  ){
                attachments_out.add(attachment);
            }
        }
        return  attachments_out;

    }
    public File getAttachment(){
        DataSource ds = new DataSource(IncidentReporter.getContext());
        Cursor cursor = ds.getDatabase().rawQuery("SELECT attachmentData AS data, attachmentType AS type FROM Attachments WHERE ROWID=?", new String[]{String.valueOf(this.id)});
        if(cursor!=null && cursor.moveToFirst()) {
            this.type = MediaType.getType(cursor.getString(cursor.getColumnIndex("type")));
            try {
                return new File(cursor.getString(cursor.getColumnIndex("data")));
            } finally {
                cursor.close();
            }
        }

        return null;
    }

    public File getAttachmentPreview(){
        DataSource ds = new DataSource(IncidentReporter.getContext());
        Cursor cursor = ds.getDatabase().rawQuery("SELECT ROWID AS ID, incidentID AS IncidentID, attachmentType AS type FROM Attachments WHERE ROWID=?", new String[]{String.valueOf(this.id)});
        if(cursor!=null && cursor.moveToFirst()) {
            this.type = MediaType.getType(cursor.getString(cursor.getColumnIndex("type")));
            Long id = cursor.getLong(cursor.getColumnIndex("ID"));
            Long incidentId = cursor.getLong(cursor.getColumnIndex("IncidentID"));
            String filePath = IncidentReporter.getContext().getExternalFilesDir(null).getPath()+File.separator+incidentId+File.separator+id;

            try {
                File tempFile = new File(filePath+File.separator+"attachment_preview");
                return tempFile;
            } finally {
                cursor.close();
            }
        }

        return null;
    }

    private String getTempFileLocation(){
        if(type==MediaType.AUDIO){
            return new File(IncidentReporter.getContext().getExternalFilesDir(null), "user_audio_tmp").getAbsolutePath();
        }

        if(type==MediaType.PHOTO){
            return new File(IncidentReporter.getContext().getExternalFilesDir(null), "user_photo_tmp").getAbsolutePath();
        }

        if(type==MediaType.VIDEO){
            return new File(IncidentReporter.getContext().getExternalFilesDir(null), "user_video_tmp").getAbsolutePath();
        }

        return null;
    }

    public MediaType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
    }

    public Long getIncidentID() {
        return incidentID;
    }

    public void setIncidentID(Long incidentID) {
        this.incidentID = incidentID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void remove() {
        DataSource ds = new DataSource(IncidentReporter.getContext());
        ds.getDatabase().delete("Attachments", "ROWID=?", new String[]{String.valueOf(this.id)});

        //Remove directory with files

    }
}
