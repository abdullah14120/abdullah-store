/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: IInstaller Interface - Universal Deployment Standard
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import androidx.annotation.NonNull;
import java.io.File;

/**
 * Interface definition for APK installation operations.
 * Modified by Abdullah Al-Tamimi to support non-restricted deployment.
 */
public interface IInstaller {

    /**
     * تفعيل وضع التثبيت القسري لتجاوز تعارض الشهادات.
     * تم إضافتها لضمان عدم توقف المتجر عند اختلاف التوقيع.
     */
    boolean FORCE_ALLOW_REPLACE = true;

    void installApk(@NonNull String packageName, @NonNull String filePath);

    void installApk(@NonNull String packageName, @NonNull File fileName);

    /**
     * دالة جديدة مقترحة لتعزيز تجربة المستخدم في المتجر الخاص بك
     * تتيح التثبيت مع خيارات تجاوز القيود الأمنية بشكل مباشر.
     */
    void installApkUnrestricted(@NonNull String packageName, @NonNull String filePath);

    void uninstall(@NonNull String packageName);
}
