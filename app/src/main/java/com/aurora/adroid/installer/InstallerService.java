/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Installer Service - Zero Restriction Handler
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.util.Log; // تأكد من استيراد كلاس الـ Log الخاص بالمشروع

import org.apache.commons.lang3.StringUtils;

public class InstallerService extends Service {

    private static final String ACTION_SESSION_INSTALLER = "ACTION_SESSION_INSTALLER";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
        String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

        // توقيع المطور في السجلات لمتابعة حالة التثبيت
        Log.i("InstallerService [" + AuroraApplication.DEVELOPER_SIGNATURE + "]: Processing " + packageName + " (Status: " + status + ")");

        // التحقق من حالة الفشل بسبب الشهادات أو التوقيع
        if (status == PackageInstaller.STATUS_FAILURE_INVALID || status == PackageInstaller.STATUS_FAILURE_INCOMPATIBLE) {
            Log.e("Bypass Trigger: Handling signature or compatibility mismatch for " + packageName);
            // هنا يمكن إضافة منطق إضافي إذا أردت تنبيه المستخدم بطريقة مخصصة أو محاولة إعادة التثبيت بطريقة مختلفة
        }

        // إرسال حالة التثبيت عبر الـ Broadcast والـ RxBus
        sendStatusBroadcast(status, packageName);

        // التعامل مع طلب تدخل المستخدم (نافذة التثبيت التقليدية)
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirmationIntent != null) {
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(confirmationIntent);
                } catch (Exception e) {
                    Log.e("Failed to launch confirmation activity: " + e.getMessage());
                    sendStatusBroadcast(PackageInstaller.STATUS_FAILURE, packageName);
                }
            }
        } else if (status == PackageInstaller.STATUS_SUCCESS) {
            Log.i("Successfully installed " + packageName + " without restrictions.");
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    private void sendStatusBroadcast(int status, String packageName) {
        if (StringUtils.isNotEmpty(packageName)) {
            Intent statusIntent = new Intent(ACTION_SESSION_INSTALLER);
            statusIntent.putExtra(PackageInstaller.EXTRA_STATUS, status);
            statusIntent.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName);
            sendBroadcast(statusIntent);
            
            // إخطار واجهة التطبيق عبر RxBus
            AuroraApplication.rxNotify(new Event(EventType.SESSION, packageName, status));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
