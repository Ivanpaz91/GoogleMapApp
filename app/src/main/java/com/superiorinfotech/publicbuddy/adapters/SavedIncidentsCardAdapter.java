package com.superiorinfotech.publicbuddy.adapters;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
//import io.vov.vitamio.MediaPlayer;
//import io.vov.vitamio.MediaPlayer;
//import io.vov.vitamio.widget.MediaController;
//import io.vov.vitamio.widget.VideoView;

import com.superiorinfotech.publicbuddy.CreateIncident;
import com.superiorinfotech.publicbuddy.FullScreenImageActivity;
import com.superiorinfotech.publicbuddy.MainActivity;
import com.superiorinfotech.publicbuddy.R;

import com.superiorinfotech.publicbuddy.VideoViewActivity;
import com.superiorinfotech.publicbuddy.db.DataSource;
import com.superiorinfotech.publicbuddy.db.model.Category;
import com.superiorinfotech.publicbuddy.db.model.Entity;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.db.model.MediaAttachment;
import com.superiorinfotech.publicbuddy.db.model.SubCategory;
import com.superiorinfotech.publicbuddy.db.model.SubcategoryValue;
import com.superiorinfotech.publicbuddy.db.model.User;
import com.superiorinfotech.publicbuddy.enums.IncidentsFragmentType;
import com.superiorinfotech.publicbuddy.enums.MediaType;
import com.superiorinfotech.publicbuddy.events.IncidentSendEvent;
import com.superiorinfotech.publicbuddy.events.IncidentUpdatedEvent;
import com.superiorinfotech.publicbuddy.dialogs.AudioPlayerDialog;
import com.superiorinfotech.publicbuddy.events.MapTouchEvent;
import com.superiorinfotech.publicbuddy.utils.DateConvert;
import com.superiorinfotech.publicbuddy.utils.MyMediaController;
import com.superiorinfotech.publicbuddy.utils.VideoStream;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;
import java.util.TimeZone;


/**
 * Created by alex on 03.02.15.
 */
public class SavedIncidentsCardAdapter extends RecyclerView.Adapter<SavedIncidentsCardAdapter.ViewHolder>{



    private static final String TAG = "IncidentAdapter";
    private static Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    private User mUser;

    private IncidentsFragmentType mType;
    ImageLoader imageLoader;

    String query;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public  String mMessageIncidentInfo;

    private VideoStream player;
    private SurfaceView surface;
    private SurfaceHolder sHolder;
    private DisplayImageOptions imageOptions;


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView mIncidentID;
        public TextView mDate;
        public TextView mComments;
        public TextView mOrganization;
        public TextView mEntity;
        public MapView  mMapView;

        public RelativeLayout mIconImageLayout;
        public RelativeLayout mIconVideoLayout;
        public RelativeLayout mIconAudioLayout;
        public TextView mIconImageText;
        public TextView mIconVideoText;
        public TextView mIconAudioText;
        public ImageView   mDeleteCardBtn;
        public ImageView   mSendCardBtn;
        public View view_card_fragment;
        public TextView mRemarks;

        public ViewHolder(View rootView) {
            super(rootView);

            mIncidentID = (TextView) rootView.findViewById(R.id.incidentId);
            mMapView    = (MapView) rootView.findViewById(R.id.map_view);
            mMapView.setOnClickListener(this);

            mIconImageLayout = (RelativeLayout) rootView.findViewById(R.id.layout_iconImage);
            mIconVideoLayout = (RelativeLayout) rootView.findViewById(R.id.layout_iconVideo);
            mIconAudioLayout = (RelativeLayout) rootView.findViewById(R.id.layout_iconAudio);

            mIconImageText = (TextView) rootView.findViewById(R.id.text_iconImage);
            mIconVideoText = (TextView) rootView.findViewById(R.id.text_iconVideo);
            mIconAudioText = (TextView) rootView.findViewById(R.id.text_iconAudio);
            mDeleteCardBtn = (ImageView) rootView.findViewById(R.id.btn_delete_incident);
            mSendCardBtn   = (ImageView) rootView.findViewById(R.id.btn_send_incident);


            mComments = (TextView) rootView.findViewById(R.id.comment);
            mRemarks = (TextView)rootView.findViewById(R.id.textView_remark);
            Typeface type = Typeface.createFromAsset(mContext.getAssets(), "fonts/OpenSans-Regular.ttf");

            mComments.setTypeface(type);

        //    Typeface type1 = Typeface.createFromAsset(mContext.getAssets(), "fonts/ostrich-bold.ttf");
             mIncidentID.setTypeface(type);

            mRemarks.setOnClickListener(this);
            mIncidentID.setOnClickListener(this);

            view_card_fragment = (View)rootView.findViewById(R.id.view_card_fragment);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.textView_remark){
                Intent intent = new Intent(mContext, CreateIncident.class);
                mCursor.moveToFirst();
                mCursor.move(getPosition());
                intent.putExtra(Incident.TYPE, mCursor.getLong(mCursor.getColumnIndex("_id")));
                mContext.startActivity(intent);
            }

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SavedIncidentsCardAdapter(Context context, User user, IncidentsFragmentType type) {
        mContext = context;
        mCursor = new DataSource(context).getData(user, type);
        mDataValid = mCursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
        mUser = user;
        mType = type;

        setImageLoaderConfig(context);
    }
//    public SavedIncidentsCardAdapter(Context context, User user, IncidentsFragmentType type,String query) {
//        mContext = context;
//        mCursor = new DataSource(context).getData(user, type,query);
//        mDataValid = mCursor != null;
//        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
//        mDataSetObserver = new NotifyingDataSetObserver();
//        if (mCursor != null) {
//            mCursor.registerDataSetObserver(mDataSetObserver);
//        }
//        mUser = user;
//        mType = type;
//
//        setImageLoaderConfig(context);
//    }

    private void setImageLoaderConfig(Context context) {

//        BitmapFactory.Options resizeOptions = new BitmapFactory.Options();
//        resizeOptions.inSampleSize = 3; // decrease size 3 times
//        resizeOptions.inScaled = true;

        imageOptions = new DisplayImageOptions.Builder()

                .showImageForEmptyUri(R.drawable.ribbon_image)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
//                .decodingOptions(resizeOptions)
//                .postProcessor(new BitmapProcessor() {
//                    @Override
//                    public Bitmap process(Bitmap bmp) {
//                        return Bitmap.createScaledBitmap(bmp, 300, 300, false);
//                    }
//                }).build();

        ImageLoaderConfiguration config =new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(imageOptions)
        .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SavedIncidentsCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_incident_card, parent, false);
        // set the view's size, margins, paddings and layout parameters


        ViewHolder vh = new ViewHolder(rootView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(holder, mCursor);
    }

    public void onBindViewHolder(ViewHolder holder, final Cursor cursor) {


        Double lat = cursor.getDouble(cursor.getColumnIndex("Latitude"));
        Double lng = cursor.getDouble(cursor.getColumnIndex("Longitude"));
        final LatLng position = new LatLng(lat, lng);
        Integer idColumnIndex = -1;
        if(mType==IncidentsFragmentType.Local) {

            idColumnIndex = cursor.getColumnIndex("_id");
            holder.mDeleteCardBtn.setVisibility(View.VISIBLE);
            holder.mDeleteCardBtn.setTag(cursor.getString(idColumnIndex));

            holder.mSendCardBtn.setVisibility(View.VISIBLE);
            holder.mSendCardBtn.setTag(cursor.getString(idColumnIndex));
           // final Integer finalIdColumnIndex = idColumnIndex;
            holder.mDeleteCardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String i = (String)v.getTag();
                    Boolean isSuccess = new Incident().deleteById(i);
                    if(isSuccess){
                        EventBus.getDefault().post(new IncidentUpdatedEvent());
                        Toast.makeText(mContext,"Delete Succeed!",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(mContext,"Error!",Toast.LENGTH_LONG).show();
                    }
                }
            });
            holder.mSendCardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String i = (String) v.getTag();
                    new Incident(Long.parseLong(i)).sendByIdFromLocal(mContext,MainActivity.getCurrentUser());

                }
            });
            holder.mRemarks.setClickable(true);

        }else if(mType==IncidentsFragmentType.Remote){
            idColumnIndex = cursor.getColumnIndex("IncidentID");
            holder.mRemarks.setClickable(false);
            holder.mRemarks.setEnabled(false);
        }


        if (idColumnIndex != -1) {
            //   final List<MediaAttachment> attachments = MediaAttachment.getIncidentAttachments(idColumnIndex.longValue());


            final String entityId = cursor.getString(cursor.getColumnIndex("entity_id"));
            final String categoryId = cursor.getString(cursor.getColumnIndex("category_id"));
            //todo sub cateogry info show
            holder.mIncidentID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String entityName =  Entity.newInstance(mContext,Long.parseLong(entityId)).getEntityName();
                        String categoryName =  Category.newInstance(mContext,Long.parseLong(categoryId)).getCategoryName();
                        //List<SubcategoryValue> subCategories = new SubCategory(mContext,Long.parseLong(categoryId)).getValues();
                        mMessageIncidentInfo ="Entity: " + entityName +"; Category:" + categoryName;
                        Crouton.makeText((Activity) mContext,mMessageIncidentInfo , Style.INFO).show();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });



            final String id = cursor.getString(idColumnIndex);
            final String dateTime =  cursor.getString(cursor.getColumnIndex("datetime"));
            final String reportId =    id + "-" +entityId +"-" + categoryId;
            if(mType==IncidentsFragmentType.Remote) {
               // holder.mIncidentID.setText("ID:" + reportId + "\n" +   DateConvert.convert3(dateTime));
                holder.mIncidentID.setText("Report ID:" + reportId + "\n" + dateTime);
            }else{


                holder.mIncidentID.setText("Report ID:" + DateConvert.convert4(dateTime));
            }

            //show cards by query

            holder.mMapView.onCreate(null);


            holder.mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    if (position.latitude != 0.0f && position.longitude != 0.0f) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        if(mType==IncidentsFragmentType.Local){
                            googleMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title("Title")
                                    .snippet(DateConvert.convert4(dateTime))
                                            // .snippet(DateConvert.convert3(dateTime))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        }else if(mType==IncidentsFragmentType.Remote) {
                            googleMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title("Title")
                                    .snippet(id)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                        builder.include(new LatLng(position.latitude, position.longitude));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f));

                    }
                }
            });

            int imageCount = cursor.getInt(cursor.getColumnIndex("Images"));
            int videoCount = cursor.getInt(cursor.getColumnIndex("Video"));
            int audioCount = cursor.getInt(cursor.getColumnIndex("Audio"));
            if(cursor.getString(cursor.getColumnIndex("Comment"))!=null){
                holder.mComments.setText(cursor.getString(cursor.getColumnIndex("Comment")));
                holder.mComments.setMovementMethod(new ScrollingMovementMethod());
            }

            if(query!=null){
                if(holder.mIncidentID.getText().toString().contains(query)||holder.mComments.getText().toString().contains(query)){
                    holder.view_card_fragment.setVisibility(View.VISIBLE);
                }else{
                    holder.view_card_fragment.setVisibility(View.GONE);
                }
            }



            //image icon click
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            //get id
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            //image button click action
            if(imageCount > 0){
                setPhotoView(imageCount,holder , inflater, _id);
            }  else{
            holder.mIconImageLayout.setVisibility(View.INVISIBLE);
             }
            if(videoCount > 0) {
                setVideoView(videoCount, holder, inflater, _id);
            }else{
                holder.mIconVideoLayout.setVisibility(View.INVISIBLE);
            }
            if(audioCount > 0) {
                setAudioView(audioCount, holder, inflater, _id);
            }else{
                holder.mIconAudioLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setVideoView(final int videoCount, ViewHolder holder, final LayoutInflater inflater, final int finalIdColumnIndex) {


        holder.mIconVideoLayout.setVisibility(View.VISIBLE);
        final int[] current_video = {0};
        holder.mIconVideoLayout.setTag(finalIdColumnIndex);
        holder.mIconVideoText.setText(String.valueOf(videoCount));
        holder.mIconVideoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, VideoViewActivity.class);

                intent.putExtra("count",videoCount);
                intent.putExtra("_id",finalIdColumnIndex);
                if(mType== IncidentsFragmentType.Local) {
                    intent.putExtra("type",true);
                }else{
                    intent.putExtra("type",false);
                }

                mContext.startActivity(intent);

            }

        });
    }
    private void setAudioView(final int audioCount, ViewHolder holder, final LayoutInflater inflater, final int finalIdColumnIndex) {



        holder.mIconAudioLayout.setVisibility(View.VISIBLE);

        holder.mIconAudioLayout.setTag(finalIdColumnIndex);
        holder.mIconAudioText.setText(String.valueOf(audioCount));
        holder.mIconAudioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AudioPlayerDialog dialog;
                int id = (int)v.getTag();
                final List<MediaAttachment> attachments = MediaAttachment.getIncidentAttachmentsByType((long)id, MediaType.AUDIO);

                if(mType==IncidentsFragmentType.Local ) {
                    dialog = new AudioPlayerDialog(mContext,attachments,false);
                }else {
                    dialog = new AudioPlayerDialog(mContext,attachments,true);
                }

            }
        });
    }
    private void setPhotoView(final int imageCount,ViewHolder holder, final LayoutInflater inflater,  final int  finalIdColumnIndex) {

            holder.mIconImageLayout.setVisibility(View.VISIBLE);
            holder.mIconImageText.setText(String.valueOf(imageCount));
            final int[] current_image = {0};
            current_image[0] = 0;
            holder.mIconImageLayout.setTag(finalIdColumnIndex);
            holder.mIconImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int id = (int)v.getTag();
                    Dialog settingsDialog = new Dialog(mContext);
                    settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    View view = inflater.inflate(R.layout.dialog_attachment_layout, null);
                    final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progress_dialog);

                    settingsDialog.setContentView(view);
                    settingsDialog.show();

                    final ImageView imageView = (ImageView) view.findViewById(R.id.imageView_item);
                    imageView.setVisibility(View.VISIBLE);


                    final Button prevBtn = (Button) view.findViewById(R.id.btn_prev_item);
                    final Button forwardBtn = (Button) view.findViewById(R.id.btn_forward_item);
                    final TextView pageNumber = (TextView)view.findViewById(R.id.textView_number_page);


                        //hide next prev button when one image
                    if(imageCount == 1){
                        prevBtn.setVisibility(View.GONE);
                        forwardBtn.setVisibility(View.GONE);
                    }else{
                        prevBtn.setVisibility(View.GONE);
                        forwardBtn.setVisibility(View.VISIBLE);
                    }
                    final List<MediaAttachment> attachments = MediaAttachment.getIncidentAttachmentsByType((long)id, MediaType.PHOTO);
                    final String imagePath;
                    final int imageSize = attachments.size();

                    //set Page number

                    pageNumber.setText("1/" + imageSize);
                    pageNumber.bringToFront();
                    try {
                        if(mType==IncidentsFragmentType.Local) {
                             imagePath ="file:///" + attachments.get(current_image[0]).getData();

                        }else{
                            imagePath = attachments.get(current_image[0]).getData();
                        }
                        //go to full screen mode when click
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, FullScreenImageActivity.class);
                                intent.putExtra("path",imagePath);
                                mContext.startActivity(intent);
                            }
                        });
                        imageLoader.displayImage(imagePath, imageView,imageOptions,new SimpleImageLoadingListener(){
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                progressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    prevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String path;
                            current_image[0]--;
                            if (current_image[0] < 1) {
                                prevBtn.setVisibility(View.INVISIBLE);
                                forwardBtn.setVisibility(View.VISIBLE);
//                                current_image[0] = current_image[0] + imageCount;
                            }else{
                                prevBtn.setVisibility(View.VISIBLE);
                                forwardBtn.setVisibility(View.VISIBLE);
                            }
                            if(mType==IncidentsFragmentType.Local) {
                                path ="file:///" + attachments.get(current_image[0]).getData();


                            }else{
                                path = attachments.get(current_image[0]).getData();
                            }
                            pageNumber.setText(String.valueOf(current_image[0] + 1) + "/" + imageSize);
                            pageNumber.bringToFront();
                            ImageLoader.getInstance().displayImage(path, imageView,new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    progressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    progressBar.setVisibility(View.GONE);


                                }
                            });
                        }
                    });
                    forwardBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            current_image[0]++;
                            if (current_image[0] >= imageCount - 1 ) {
                            //    current_image[0] = 0;
                                prevBtn.setVisibility(View.VISIBLE);
                                forwardBtn.setVisibility(View.INVISIBLE);

                            }else{
                                prevBtn.setVisibility(View.VISIBLE);
                                forwardBtn.setVisibility(View.VISIBLE);
                            }
                            String path;
                            if(mType==IncidentsFragmentType.Local) {
                                path ="file:///" + attachments.get(current_image[0]).getData();

                            }else{
                                try {
                                    path = attachments.get(current_image[0]).getData();
                                } catch (Exception e) {
                                    path = attachments.get(0).getData();
                                    e.printStackTrace();
                                }
                            }
                            pageNumber.setText(String.valueOf(current_image[0] + 1) + "/" + imageSize);
                            pageNumber.bringToFront();
                            ImageLoader.getInstance().displayImage(path, imageView,new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                    progressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    progressBar.setVisibility(View.GONE);

                                }
                            });
                        }
                    });

                    //
                }
            });


    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
//    private String getDataSource(String path) throws IOException {
//        if (!URLUtil.isNetworkUrl(path)) {
//            return path;
//        } else {
//
//            URL url = new URL(path);
//            URLConnection cn = url.openConnection();
//            cn.connect();
//            InputStream stream = cn.getInputStream();
//            if (stream == null)
//                throw new RuntimeException("stream is null");
//            File temp = File.createTempFile("mediaplayertmp", "dat",IncidentReporter.g.getCacheDir());
//            temp.deleteOnExit();
//            String tempPath = temp.getAbsolutePath();
//            FileOutputStream out = new FileOutputStream(temp);
//            byte buf[] = new byte[128];
//            do {
//                int numread = stream.read(buf);
//                Log.i(TAG, "Buffer Printing via Array: " + Arrays.toString(buf));
//                if (numread <= 0)
//                    break;
//                out.write(buf, 0, numread);
//            } while (true);
//            try {
//                stream.close();
//            } catch (IOException ex) {
//                Log.e(TAG, "error: " + ex.getMessage(), ex);
//            }
//            return tempPath;
//        }
//    }
    public void setQuery(String text){
        query = text;
    }
}
