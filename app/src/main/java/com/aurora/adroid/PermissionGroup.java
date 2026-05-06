/*
 * Developed & Optimized by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Professional Permission Viewer (Unrestricted)
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionGroup extends LinearLayout {

    static private final String[] permissionPrefixes = new String[]{"android"};
    static private final String permissionSuffix = ".permission.";

    private PermissionGroupInfo permissionGroupInfo;
    private Map<String, String> permissionMap = new HashMap<>();
    private PackageManager pm;

    public PermissionGroup(Context context) {
        super(context);
        init();
    }

    public PermissionGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PermissionGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PermissionGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        // حقن بصمة المطور في سجلات النظام عند تهيئة الكلاس
        Log.i("PermissionGroup initialized by: Abdullah Al-Tamimi Engine");
        
        inflate(getContext(), R.layout.item_permission, this);
        pm = getContext().getPackageManager();
        
        // ضبط اتجاه العرض ليكون عمودياً واحترافياً
        this.setOrientation(VERTICAL);
    }

    static private String getReadableLabel(String label, String packageName) {
        if (TextUtils.isEmpty(label)) {
            return "";
        }
        List<String> permissionPrefixesList = new ArrayList<>(Arrays.asList(permissionPrefixes));
        permissionPrefixesList.add(packageName);
        for (String permissionPrefix : permissionPrefixesList) {
            if (label.startsWith(permissionPrefix + permissionSuffix)) {
                label = label.substring((permissionPrefix + permissionSuffix).length());
                label = label.replace("_", " ").toLowerCase();
                return StringUtils.capitalize(label);
            }
        }
        return StringUtils.capitalize(label);
    }

    public void setPermissionGroupInfo(final PermissionGroupInfo permissionGroupInfo) {
        this.permissionGroupInfo = permissionGroupInfo;
        ImageView imageView = findViewById(R.id.permission_group_icon);
        imageView.setImageDrawable(getPermissionGroupIcon(permissionGroupInfo));
        
        // استخدام اللون الأخضر الفسفوري (Fix Accent)
        imageView.setColorFilter(Util.getColorAttribute(getContext(), R.attr.colorAccent));
    }

    public void addPermission(PermissionInfo permissionInfo) {
        CharSequence label = permissionInfo.loadLabel(pm);
        CharSequence description = permissionInfo.loadDescription(pm);
        permissionMap.put(getReadableLabel(label.toString(), permissionInfo.packageName), 
                TextUtils.isEmpty(description) ? "" : description.toString());
        
        List<String> permissionLabels = new ArrayList<>(permissionMap.keySet());
        Collections.sort(permissionLabels);
        
        LinearLayout permissionLabelsView = findViewById(R.id.permission_labels);
        permissionLabelsView.removeAllViews();
        
        for (String permissionLabel : permissionLabels) {
            addPermissionLabel(permissionLabelsView, permissionLabel, permissionMap.get(permissionLabel));
        }
    }

    private void addPermissionLabel(LinearLayout permissionLabelsView, String label, String description) {
        TextView textView = new TextView(getContext());
        textView.setText("• " + label); // إضافة رصاصة (Bullet point) للمظهر الاحترافي
        textView.setTextSize(14);
        textView.setTextAppearance(getContext(), R.style.TextAppearance_Aurora_Line2);
        
        // لمسة جمالية: مسافات تليق بالواجهة الجديدة
        textView.setPadding(0, 10, 0, 10);
        textView.setTextColor(Util.getColorAttribute(getContext(), R.attr.colorAccent));
        
        textView.setOnClickListener(getOnClickListener(description));
        permissionLabelsView.addView(textView);
    }

    private Drawable getPermissionGroupIcon(PermissionGroupInfo permissionGroupInfo) {
        Drawable drawable;
        try {
            drawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                    ? getContext().getResources().getDrawable(permissionGroupInfo.icon, getContext().getTheme())
                    : getContext().getResources().getDrawable(permissionGroupInfo.icon);
        } catch (Resources.NotFoundException e) {
            drawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                    ? permissionGroupInfo.loadUnbadgedIcon(pm)
                    : permissionGroupInfo.loadIcon(pm);
        }
        drawable.setTint(Util.getColorAttribute(getContext(), R.attr.colorAccent));
        return drawable;
    }

    private OnClickListener getOnClickListener(final String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }

        CharSequence label = null == permissionGroupInfo ? "" : permissionGroupInfo.loadLabel(pm);
        final String title = TextUtils.isEmpty(label) ? "" : label.toString();
        
        return v -> {
            // تخصيص الدايلوج ليكون Dark و Sharp
            new MaterialAlertDialogBuilder(getContext(), R.style.Theme_MaterialComponents_Dialog_Alert)
                    .setIcon(getPermissionGroupIcon(permissionGroupInfo))
                    .setTitle((title.equals(permissionGroupInfo.name) || title.equals(permissionGroupInfo.packageName)) ? "تفاصيل الإذن" : title)
                    .setMessage(message)
                    .setPositiveButton("موافق", (dialog, which) -> dialog.dismiss())
                    .show();
        };
    }
}
