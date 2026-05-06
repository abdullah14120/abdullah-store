/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Native Installer - Standard Deployment Configuration
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.BuildConfig;
import com.aurora.adroid.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class NativeInstaller extends InstallerBase {

    public NativeInstaller(Context context) {
        super(context);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull String filePath) {
        final File fileName = new File(filePath);
        xInstall(packageName, fileName);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull File fileName) {
        xInstall(packageName, fileName);
    }

    // دعم التثبيت غير المقيد حتى في الوضع التقليدي
    @Override
    public void installApkUnrestricted(@NonNull String packageName, @NonNull String filePath) {
        final File fileName = new File(filePath);
        xInstall(packageName, fileName);
    }

    private void xInstall(String packageName, File fileName) {
        Log.i("NativeInstaller [" + AuroraApplication.DEVELOPER_SIGNATURE + "]: Preparing system installer for " + packageName);
        
        Intent intent;
        // في أندرويد 7 (Nougat) وما فوق، نستخدم الـ FileProvider مع منح الأذونات اللازمة
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            
            // تم تعديل الـ Authority لضمان عدم التعارض مع تطبيقات أخرى
            Uri contentUri = FileProvider.getUriForFile(context, 
                    StringUtils.joinWith(".", BuildConfig.APPLICATION_ID, "fileprovider"), 
                    fileName);
            
            intent.setData(contentUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            // للأجهزة القديمة جداً
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(fileName), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // إضافة Flags إضافية لمحاولة دفع النظام لتجاهل بعض القيود أثناء التثبيت اليدوي
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());

        try {
            context.startActivity(intent);
            Log.i("System Installer launched successfully by Abdullah Al-Tamimi engine.");
        } catch (Exception e) {
            Log.e("Native Install Failed: " + e.getMessage());
            // في حال فشل المثبت التقليدي، يفضل توجيه المستخدم لتفعيل Root أو Session Installer
        }
    }
}
