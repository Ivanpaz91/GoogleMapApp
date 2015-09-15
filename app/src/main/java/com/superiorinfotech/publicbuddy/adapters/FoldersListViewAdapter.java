package com.superiorinfotech.publicbuddy.adapters;/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.List;

import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.fragments.FilesFoldersDialogFragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class FoldersListViewAdapter extends ArrayAdapter<String> {

	private Context mContext;
    private FilesFoldersDialogFragment mFragment;

    private int mItemType;
    private String mItemPath;
    private int mItemPosition;

	private List<String> mFileFolderNameList;
	private List<Integer> mFileFolderTypeList;
	private List<String> mFileFolderSizeList;
	private List<String> mFileFolderPathsList;
   
    public FoldersListViewAdapter(Context context,
                                  FilesFoldersDialogFragment fragment,
    							  List<String> nameList, 
    							  List<Integer> fileFolderTypeList,
    							  List<String> sizeList, 
    							  List<String> fileFolderPathsList) {
    	
    	super(context, -1, nameList);
    	
    	mContext = context;
        mFragment = fragment;

    	mFileFolderNameList = nameList;
    	mFileFolderTypeList = fileFolderTypeList;
    	mFileFolderSizeList = sizeList;
    	mFileFolderPathsList = fileFolderPathsList;
    	
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	FoldersViewHolder holder = null;
		if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_item, parent, false);
            ListView.LayoutParams params = (ListView.LayoutParams) convertView.getLayoutParams();
//            params.height = (int) mApp.convertDpToPixels(72.0f, mContext);
            convertView.setLayoutParams(params);
            
//            Typeface typeface = RobotoTypefaceManager.obtainTypeface(mContext, RobotoTypefaceManager.Typeface.PROXIMA_NOVA_REGULAR);

			holder = new FoldersViewHolder();
			holder.fileFolderIcon = (ImageView) convertView.findViewById(R.id.listViewLeftIcon);
			holder.fileFolderSizeText = (TextView) convertView.findViewById(R.id.listViewSubText);
			holder.fileFolderNameText = (TextView) convertView.findViewById(R.id.listViewTitleText);
            holder.overflowButton = (ImageButton) convertView.findViewById(R.id.listViewOverflow);
            holder.rightSubText = (TextView) convertView.findViewById(R.id.listViewRightSubText);

            holder.fileFolderIcon.setScaleX(0.5f);
            holder.fileFolderIcon.setScaleY(0.55f);
            holder.rightSubText.setVisibility(View.INVISIBLE);

			holder.fileFolderNameText.setTextColor(Color.parseColor("#2c2c2c"));
//			holder.fileFolderNameText.setTypeface(typeface);
			
			holder.fileFolderSizeText.setTextColor(Color.parseColor("#2c2c2c"));
//			holder.fileFolderSizeText.setTypeface(typeface);

//            holder.overflowButton.setImageResource(ThemeUtils.getIcon(mContext, "ic_action_overflow"));
            holder.overflowButton.setFocusable(false);
            holder.overflowButton.setFocusableInTouchMode(false);
			holder.overflowButton.setOnClickListener(overflowClickListener);

			convertView.setTag(holder);
		} else {
		    holder = (FoldersViewHolder) convertView.getTag();
		}
		
		holder.fileFolderNameText.setText(mFileFolderNameList.get(position));
		holder.fileFolderSizeText.setText(mFileFolderSizeList.get(position));
		
		//Set the icon based on whether the item is a folder or a file.
		if (mFileFolderTypeList.get(position)== FilesFoldersDialogFragment.FOLDER) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_folderblue);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersDialogFragment.FOLDER);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
            convertView.setTag(R.string.position, position);

		} else if (mFileFolderTypeList.get(position)== FilesFoldersDialogFragment.AUDIO_FILE) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_mp3);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersDialogFragment.AUDIO_FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
            convertView.setTag(R.string.position, position);

		} else if (mFileFolderTypeList.get(position)== FilesFoldersDialogFragment.PICTURE_FILE) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_png);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersDialogFragment.PICTURE_FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
            convertView.setTag(R.string.position, position);

		} else if (mFileFolderTypeList.get(position)== FilesFoldersDialogFragment.VIDEO_FILE) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_avi);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersDialogFragment.VIDEO_FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
            convertView.setTag(R.string.position, position);

		} else {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_default);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersDialogFragment.FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
            convertView.setTag(R.string.position, position);

		}
    	
    	return convertView;
	}

    /**
     * Click listener for overflow button.
     */
    private View.OnClickListener overflowClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            PopupMenu menu = new PopupMenu(mContext, v);
            menu.inflate(R.menu.file_folder_overflow_menu);
            menu.setOnMenuItemClickListener(popupMenuItemClickListener);
            mItemType = (Integer) ((View) v.getParent()).getTag(R.string.folder_list_item_type);
            mItemPath = (String) ((View) v.getParent()).getTag(R.string.folder_path);
            mItemPosition = (Integer) ((View) v.getParent()).getTag(R.string.position);
            menu.show();

        }

    };

    /**
     * Menu item click listener for the pop up menu.
     */
    private PopupMenu.OnMenuItemClickListener popupMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch(item.getItemId()) {
                case R.id.attach:
                    int fileIndex;
                    String folderPath;
                    if (mItemType== FilesFoldersDialogFragment.PICTURE_FILE || mItemType== FilesFoldersDialogFragment.VIDEO_FILE || mItemType== FilesFoldersDialogFragment.AUDIO_FILE) {
                        fileIndex = 0;
                        folderPath = FilesFoldersDialogFragment.currentDir;
                        for (int i=0; i < mItemPosition; i++) {
                            if (mFileFolderTypeList.get(i)== FilesFoldersDialogFragment.AUDIO_FILE)
                                fileIndex++;
                        }

                    } else {
                        fileIndex = 0;
                        folderPath = mItemPath;
                    }

                    mFragment.attachFile(mItemType, fileIndex, folderPath, "");
                    break;
                case R.id.rename:
                    mFragment.rename(mItemPath);
                    break;
                case R.id.copy:
                    mFragment.copyMove(mItemPath, false);
                    break;
                case R.id.move:
                    mFragment.copyMove(mItemPath, true);
                    break;
                case R.id.delete:
                    mFragment.deleteFile(new File(mItemPath));
                    break;
            }

            return false;
        }

    };

    static class FoldersViewHolder {
    	public TextView fileFolderNameText;
    	public TextView fileFolderSizeText;
    	public ImageView fileFolderIcon;
        public ImageButton overflowButton;
        public TextView rightSubText;
    }
   
}
