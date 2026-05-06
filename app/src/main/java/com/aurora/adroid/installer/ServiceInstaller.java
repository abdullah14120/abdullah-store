/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Service Installer - Privileged Extension Bridge
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.BuildConfig;
import com.aurora.adroid.Constants;
import com.aurora.adroid.util.Log;
import com.aurora.services.IPrivilegedCallback;
import com.aurora.services.IPrivilegedService;

import java.io.File;

public class ServiceInstaller extends InstallerBase {

    // تفعيل خيار الاستبدال وتجاوز قيود الإصدار الأقدم (Downgrade) إذا كانت الخدمة تدعم ذلك
    private static final int ACTION_INSTALL_REPLACE_EXISTING = 2;

    public ServiceInstaller(Context context) {
        super(context);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull String filePath) {
        xInstall(packageName, Uri.fromFile(new File(filePath)));
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull File fileName) {
        xInstall(packageName, Uri.fromFile(fileName));
    }

    // دعم التثبيت غير المقيد عبر الخدمة المميزة
    @Override
    public void installApkUnrestricted(@NonNull String packageName, @NonNull String filePath) {
        xInstall(packageName, Uri.fromFile(new File(filePath)));
    }

    private void xInstall(@NonNull String packageName, @NonNull Uri fileUri) {
        Log.i("ServiceInstaller [" + AuroraApplication.DEVELOPER_SIGNATURE + "]: Connecting to Privileged Extension for " + packageName);

        final ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                IPrivilegedService service = IPrivilegedService.Stub.asInterface(binder);
                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) {
                        Log.i("Privileged Install Result for " + packageName + ": " + returnCode);
                        // يمكن هنا إرسال Broadcast بنتيجة التثبيت إذا لزم الأمر
                    }
                };
                try {
                    // تنفيذ التثبيت بصلاحيات النظام عبر الخدمة
                    service.installPackage(
                            fileUri,
                            ACTION_INSTALL_REPLACE_EXISTING,
                            BuildConfig.APPLICATION_ID,
                            callback
                    );
                    Log.i("Install command sent via Abdullah Al-Tamimi engine.");
                } catch (RemoteException e) {
                    Log.e("RemoteException: Connecting to privileged service failed");
                } finally {
                    // فك الارتباط بالخدمة بعد إرسال الأمر لضمان عدم استهلاك الموارد
                    try {
                        context.getApplicationContext().unbindService(this);
                    } catch (Exception ignored) {}
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.e("Disconnected from privileged service");
            }
        };

        final Intent serviceIntent = new Intent(Constants.PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage(Constants.PRIVILEGED_EXTENSION_PACKAGE_NAME);
        
        try {
            boolean isBound = context.getApplicationContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (!isBound) {
                Log.e("Failed to bind to Privileged Service. Is the extension installed?");
            }
        } catch (Exception e) {
            Log.e("Binding Error: " + e.getMessage());
        }
    }
}
