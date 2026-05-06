/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Root Installer - Direct Shell Deployment (Unrestricted)
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.topjohnwu.superuser.Shell;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RootInstaller extends InstallerBase {

    public RootInstaller(Context context) {
        super(context);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull String filePath) {
        executeRootInstall(packageName, filePath);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull File fileName) {
        executeRootInstall(packageName, fileName.getAbsolutePath());
    }

    // دعم التثبيت غير المقيد عبر الروت مباشرة
    @Override
    public void installApkUnrestricted(@NonNull String packageName, @NonNull String filePath) {
        executeRootInstall(packageName, filePath);
    }

    /**
     * تنفيذ التثبيت عبر الروت مع حقن خيارات التجاوز
     * تعديل: عبدالله التميمي
     */
    private void executeRootInstall(String packageName, String filePath) {
        if (Shell.getShell().isRoot()) {
            // استخدام قالب التثبيت مع إضافة -r و -d (Downgrade) لضمان عدم توقف العملية بسبب الشهادات
            String command = String.format("pm install -r -d --user %s %s", 
                    Util.getInstallationProfile(context), filePath);
            
            Log.i("RootInstaller [" + AuroraApplication.DEVELOPER_SIGNATURE + "]: Executing restricted bypass for " + packageName);

            disposable.add(Observable.fromCallable(() -> Shell.su(command).exec())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> iLog(result, packageName, Type.INSTALL), throwable -> {
                        Toast.makeText(context, R.string.string_install_failed, Toast.LENGTH_SHORT).show();
                        eLog(packageName, Type.INSTALL);
                    }));
        } else {
            notifyNoRoot(packageName);
        }
    }

    private void iLog(Shell.Result result, String packageName, Type type) {
        if (result.isSuccess()) {
            Log.i(StringUtils.joinWith(StringUtils.SPACE, "Success [" + AuroraApplication.DEVELOPER_SIGNATURE + "]:", packageName));
        } else {
            // طباعة تفاصيل الخطأ في الـ Log للمساعدة في تخطي حماية النظام
            Log.e("Root Install Error Detail: " + result.getOut());
            eLog(packageName, type);
        }
    }

    private void eLog(String packageName, Type type) {
        Log.e(StringUtils.joinWith(StringUtils.SPACE, context.getString(type == Type.INSTALL
                ? R.string.string_install_failed
                : R.string.string_uninstall_failed), packageName));
    }

    private void notifyNoRoot(String packageName) {
        AuroraApplication.rxNotify(new Event(EventType.NO_ROOT, packageName));
        Toast.makeText(context, R.string.string_no_root, Toast.LENGTH_SHORT).show();
    }
}
