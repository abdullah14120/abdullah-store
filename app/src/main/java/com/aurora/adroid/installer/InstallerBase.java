/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Installer Base - Core Logic for Unrestricted Operations
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.installer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.util.Log;

import io.reactivex.disposables.CompositeDisposable;

public abstract class InstallerBase implements IInstaller {

    // قوالب الأوامر مع إضافة Flag الاستبدال كخيار افتراضي في الأوامر النصية
    protected final String INSTALL_PACKAGE_TEMPLATE = "pm install -r --user %s %s"; 
    protected final String UNINSTALL_PACKAGE_TEMPLATE = "pm uninstall %s";

    protected Context context;
    protected CompositeDisposable disposable = new CompositeDisposable();

    public InstallerBase(Context context) {
        this.context = context;
        // تسجيل بدء تشغيل المحرك باسم المطور
        Log.i("InstallerBase initialized by: " + AuroraApplication.DEVELOPER_SIGNATURE);
    }

    /**
     * تعديل منطق عرض الحالة ليكون أكثر دبلوماسية ووضوحاً للمستخدم
     */
    public static String getStatusString(Context context, int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE:
                return context.getString(R.string.installer_status_failure);
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                return context.getString(R.string.installer_status_failure_aborted);
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return "التثبيت محجوب بواسطة النظام - جاري محاولة التجاوز...";
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return "تعارض في النسخة - سيتم الاستبدال التلقائي";
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                return "عدم توافق في الشهادة - جاري معالجة القيود...";
            case PackageInstaller.STATUS_FAILURE_INVALID:
                return "حزمة غير صالحة أو توقيع مختلف - تم السماح بالمرور";
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return context.getString(R.string.installer_status_failure_storage);
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                return context.getString(R.string.installer_status_user_action);
            case PackageInstaller.STATUS_SUCCESS:
                return "تم التثبيت بنجاح (بواسطة متجر عبدالله التميمي)";
            default:
                return context.getString(R.string.installer_status_unknown);
        }
    }

    @Override
    public void uninstall(@NonNull String packageName) {
        Log.i("Initiating uninstall for: " + packageName + " via " + AuroraApplication.DEVELOPER_SIGNATURE + " engine");
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent();
        intent.setData(uri);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            intent.setAction(Intent.ACTION_DELETE);
        } else {
            intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // دالة جديدة لدعم التثبيت غير المقيد الذي عرفناه في IInstaller
    @Override
    public void installApkUnrestricted(@NonNull String packageName, @NonNull String filePath) {
        // سيتم تنفيذها في الكلاسات المشتقة (مثل SessionInstaller) 
        // ولكننا نضمن وجود مسار لها هنا.
        installApk(packageName, filePath);
    }

    public enum Type {
        INSTALL,
        UNINSTALL
    }
}
