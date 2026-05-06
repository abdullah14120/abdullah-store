/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: AppInstaller Factory - Defaulting to Zero-Restriction Mode
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.content.Context;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PrefUtil;

public abstract class AppInstaller {

    private static volatile AppInstaller INSTANCE;

    public static AppInstaller getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppInstaller.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppInstaller() {
                        @Override
                        public InstallerBase getDefaultInstaller() {
                            // جلب القيمة من الإعدادات
                            String prefValue = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLATION_METHOD);
                            
                            Log.i("AppInstaller [" + AuroraApplication.DEVELOPER_SIGNATURE + "]: Method Selected -> " + prefValue);

                            switch (prefValue) {
                                case "0":
                                    return new NativeInstaller(context);
                                case "1":
                                    return new RootInstaller(context);
                                case "2":
                                    return new ServiceInstaller(context);
                                case "3":
                                    return new SessionInstaller(context);
                                default:
                                    // تعديل عبد الله التميمي: جعل SessionInstaller هو الخيار الافتراضي 
                                    // لضمان عمل "بدون قيود" فور تحميل المتجر.
                                    Log.i("Defaulting to SessionInstaller for Unrestricted access.");
                                    return new SessionInstaller(context);
                            }
                        }
                    };
                }
            }
        }
        return INSTANCE;
    }

    public abstract InstallerBase getDefaultInstaller();
}
