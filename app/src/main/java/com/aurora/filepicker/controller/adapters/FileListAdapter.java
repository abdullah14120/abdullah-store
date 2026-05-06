/*
 * Developed & Refined by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: FilePicker Adapter (Stable Build)
 */

package com.aurora.filepicker.controller.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.adroid.R;
import com.aurora.filepicker.controller.NotifyItemChecked;
import com.aurora.filepicker.model.DialogConfigs;
import com.aurora.filepicker.model.DialogProperties;
import com.aurora.filepicker.model.FileListItem;
import com.aurora.filepicker.model.MarkedItemList;
import com.aurora.filepicker.widget.MaterialCheckbox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FileListAdapter extends BaseAdapter {

    private ArrayList<FileListItem> fileListItems;
    private Context context;
    private DialogProperties dialogProperties;
    private NotifyItemChecked notifyItemChecked;

    public FileListAdapter(ArrayList<FileListItem> fileListItems, Context context, DialogProperties dialogProperties) {
        this.fileListItems = fileListItems;
        this.context = context;
        this.dialogProperties = dialogProperties;
    }

    @Override
    public int getCount() {
        return fileListItems == null ? 0 : fileListItems.size();
    }

    @Override
    public FileListItem getItem(int i) {
        return fileListItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.filepicker_item, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final FileListItem fileListItem = fileListItems.get(position);

        // تحسين إدارة الرسوم المتحركة
        view.clearAnimation();
        if (MarkedItemList.hasItem(fileListItem.getLocation())) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.marked_item_animation);
            view.setAnimation(animation);
        }

        // تحديد نوع الأيقونة وتلوينها
        if (fileListItem.isDirectory()) {
            viewHolder.imgType.setImageResource(R.drawable.ic_type_folder);
            int color = ContextCompat.getColor(context, R.color.colorPrimary);
            viewHolder.imgType.setColorFilter(color);
            
            if (dialogProperties.selectionType == DialogConfigs.FILE_SELECT) {
                viewHolder.materialCheckbox.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.materialCheckbox.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.imgType.setImageResource(R.drawable.ic_type_file);
            int color = ContextCompat.getColor(context, R.color.colorAccent);
            viewHolder.imgType.setColorFilter(color);

            if (dialogProperties.selectionType == DialogConfigs.DIR_SELECT) {
                viewHolder.materialCheckbox.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.materialCheckbox.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.imgType.setContentDescription(fileListItem.getFilename());
        viewHolder.txtFileName.setText(fileListItem.getFilename());

        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        Date date = new Date(fileListItem.getTime());

        if (position == 0 && fileListItem.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
            viewHolder.txtFileType.setText(R.string.label_parent_directory);
        } else {
            viewHolder.txtFileType.setText(context.getString(R.string.last_edit) + ": " + formatDate.format(date) + " " + formatTime.format(date));
        }

        if (viewHolder.materialCheckbox.getVisibility() == View.VISIBLE) {
            if (position == 0 && fileListItem.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
                viewHolder.materialCheckbox.setVisibility(View.INVISIBLE);
            }
            viewHolder.materialCheckbox.setChecked(MarkedItemList.hasItem(fileListItem.getLocation()));
        }

        viewHolder.materialCheckbox.setOnCheckedChangedListener((checkbox, isChecked) -> {
            fileListItem.setMarked(isChecked);
            if (fileListItem.isMarked()) {
                if (dialogProperties.selectionMode == DialogConfigs.MULTI_MODE) {
                    MarkedItemList.addSelectedItem(fileListItem);
                } else {
                    MarkedItemList.addSingleFile(fileListItem);
                }
            } else {
                MarkedItemList.removeSelectedItem(fileListItem.getLocation());
            }
            if (notifyItemChecked != null) {
                notifyItemChecked.notifyCheckBoxIsClicked();
            }
        });

        return view;
    }

    public void setNotifyItemCheckedListener(NotifyItemChecked notifyItemChecked) {
        this.notifyItemChecked = notifyItemChecked;
    }

    // تم حذف ButterKnife واستبداله بـ findViewById لضمان استقرار البناء
    public static class ViewHolder {
        ImageView imgType;
        TextView txtFileName;
        TextView txtFileType;
        MaterialCheckbox materialCheckbox;

        public ViewHolder(View itemView) {
            imgType = itemView.findViewById(R.id.image_type);
            txtFileName = itemView.findViewById(R.id.file_name);
            txtFileType = itemView.findViewById(R.id.file_type);
            materialCheckbox = itemView.findViewById(R.id.file_mark);
        }
    }
}
