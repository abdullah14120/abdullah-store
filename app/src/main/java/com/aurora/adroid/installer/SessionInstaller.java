/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Session Installer - Zero Restriction Deployment
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import androidx.annotation.NonNull;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SessionInstaller extends InstallerBase {

    public SessionInstaller(Context context) {
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

    // الدالة الجديدة التي أضفناها في الواجهة IInstaller لتوفير تثبيت بلا قيود
    @Override
    public void installApkUnrestricted(@NonNull String packageName, @NonNull String filePath) {
        final File fileName = new File(filePath);
        xInstall(packageName, fileName);
    }

    private void xInstall(String packageName, File file) {
        final PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        try {
            // إعداد معاملات الجلسة مع حقن صلاحيات التجاوز
            final PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            
            // 1. السماح باستبدال التطبيق الحالي بغض النظر عن الإصدار أو التوقيع
            sessionParams.setInstallFlags(sessionParams.getInstallFlags() | 0x00000002); // INSTALL_REPLACE_EXISTING
            
            // 2. السماح بتثبيت إصدارات أقدم (Downgrade) لتجنب قيود التحديث
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                sessionParams.setInstallFlags(sessionParams.getInstallFlags() | 0x00000080); // INSTALL_ALLOW_DOWNGRADE
            }

            // 3. تجاوز التحقق من الشهادة الأمنية (لأجهزة أندرويد 10 وما فوق)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                sessionParams.setInstallFlags(sessionParams.getInstallFlags() | 0x00040000); // INSTALL_DISABLE_VERIFICATION
            }

            Log.i("SessionInstaller [" + AuroraApplication.DEVELOPER_SIGNATURE + "]: Initiating Unrestricted Session for " + packageName);

            final int sessionID = packageInstaller.createSession(sessionParams);
            final PackageInstaller.Session session = packageInstaller.openSession(sessionID);
            
            final InputStream inputStream = new FileInputStream(file);
            final OutputStream outputStream = session.openWrite(file.getName(), 0, file.length());

            IOUtils.copy(inputStream, outputStream);
            session.fsync(outputStream);
            
            inputStream.close();
            outputStream.close();

            // إعداد الـ Callback لاستقبال النتيجة في InstallerService
            final Intent callbackIntent = new Intent(context, InstallerService.class);
            
            // إضافة FLAG_MUTABLE لضمان التوافق مع أندرويد 12+
            int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingFlags |= PendingIntent.FLAG_MUTABLE;
            }

            final PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    sessionID,
                    callbackIntent,
                    pendingFlags);

            session.commit(pendingIntent.getIntentSender());
            session.close();
            
            Log.i("Session Committed Successfully by Abdullah Al-Tamimi");

        } catch (Exception e) {
            Log.e("Installation Session Failed: " + e.getMessage());
        }
    }
}
