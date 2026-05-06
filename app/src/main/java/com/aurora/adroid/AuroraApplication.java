/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Modification: Zero-Restrictions & Auto-Deployment Configuration
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 * Licensed under the GNU General Public License v3.
 */

package com.aurora.adroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.model.App;
import com.aurora.adroid.receiver.PackageManagerReceiver;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

public class AuroraApplication extends Application {

    // توقيع المطور للمشروع المعدل
    public static final String DEVELOPER_SIGNATURE = "Abdullah Al-Tamimi";

    private static RxBus rxBus = null;
    private static List<App> ongoingUpdateList = new ArrayList<>();
    private static boolean isRooted = false;
    private static boolean bulkUpdateAlive = false;

    private PackageManagerReceiver packageManagerReceiver;

    public static RxBus getRxBus() {
        return rxBus;
    }

    public static void rxNotify(Event event) {
        rxBus.getBus().accept(event);
    }

    public static boolean isBulkUpdateAlive() {
        return bulkUpdateAlive;
    }

    public static void setBulkUpdateAlive(boolean updating) {
        AuroraApplication.bulkUpdateAlive = updating;
    }

    public static List<App> getOngoingUpdateList() {
        return ongoingUpdateList;
    }

    public static void setOngoingUpdateList(List<App> ongoingUpdateList) {
        AuroraApplication.ongoingUpdateList = ongoingUpdateList;
    }

    public static void removeFromOngoingUpdateList(String packageName) {
        Iterator<App> iterator = ongoingUpdateList.iterator();
        while (iterator.hasNext()) {
            if (packageName.equals(iterator.next().getPackageName()))
                iterator.remove();
        }
        if (ongoingUpdateList.isEmpty())
            setBulkUpdateAlive(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // طباعة توقيع المطور عند بدء التشغيل في الـ Logcat
        Log.i("--- System Initialized by: " + DEVELOPER_SIGNATURE + " ---");

        setupTheme();

        rxBus = new RxBus();

        // تعديل: تهيئة الـ Shell مع تفعيل خيارات تجاوز القيود برمجياً
        AsyncTask.execute(() -> {
            Shell.getShell(shell -> {
                if (shell.isRoot()) {
                    Log.i("Root Access Granted - Bypassing System Restrictions");
                    isRooted = true;
                    
                    // تعطيل فحص التطبيقات من مصادر غير معروفة وتفعيل التثبيت الصامت عبر النظام
                    Shell.su("settings put global install_non_market_apps 1").exec();
                    Shell.su("settings put global package_verifier_enable 0").exec();
                } else {
                    Log.e("Root Unavailable - Falling back to Session Installer");
                    isRooted = false;
                }
            });
        });

        packageManagerReceiver = new PackageManagerReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
            }
        };

        registerReceiver(packageManagerReceiver, PackageUtil.getFilter());

        // تنظيف جلسات التثبيت القديمة لضمان عدم حدوث تعارض (Conflict)
        AsyncTask.execute(() -> Util.clearOldInstallationSessions(this));

        // بدء خدمة التنبيهات
        Util.startNotificationService(this);

        // معالج الأخطاء العالمي - تم التعديل لضمان عدم توقف المتجر عند وجود أخطاء في التوقيع
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.e("RxError suppressed: " + throwable.getMessage());
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace();
            }
        });
    }

    private void setupTheme() {
        ViewUtil.switchTheme(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            unregisterReceiver(packageManagerReceiver);
            AppDatabase.destroyInstance();
        } catch (Exception ignored) {
        }
    }
}
