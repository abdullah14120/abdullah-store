/*
 * Developed by: Abdullah Al-Tamimi
 * Project: Custom Android Store (Aurora Based)
 * Component: Glide Module - Image Processing & CDN Optimization
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aurora.adroid.util.Util;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

import okhttp3.OkHttpClient;

import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;

@GlideModule
public class AuroraGlide extends AppGlideModule {

    private static RequestOptions requestOptions() {
        // تم تعديل الـ Signature لضمان تحديث الأيقونات كل 12 ساعة بدلاً من 24
        // لضمان ظهور أي تغييرات تجريها على Cloudflare بسرعة أكبر.
        return new RequestOptions()
                .signature(new ObjectKey(System.currentTimeMillis() / (12 * 60 * 60 * 1000)))
                .centerInside() // تم التغيير لـ centerInside للحفاظ على أبعاد أيقونات التطبيقات الأصلية
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .encodeQuality(100)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // تخزين الصورة الأصلية والمعدلة لسرعة العرض
                .format(PREFER_ARGB_8888)
                .timeout(20000) // زيادة المهلة لـ 20 ثانية لضمان التحميل من سيرفرات بعيدة
                .skipMemoryCache(false);
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        
        // طباعة توقيع المطور في سجلات الشبكة عند تهيئة محرك الصور
        android.util.Log.i("GlideModule", "Initialized by: " + AuroraApplication.DEVELOPER_SIGNATURE);

        if (Util.isNetworkProxyEnabled(context))
            builder.proxy(Util.getNetworkProxy(context));
        
        return builder.build();
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // زيادة حجم الكاش لـ 100 ميجابايت لتجربة تصفح أسرع للتطبيقات الكثيرة
        int cacheSizeBytes = 1024 * 1024 * 100; 
        builder.setMemoryCache(new LruResourceCache(cacheSizeBytes / 2));
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cacheSizeBytes));
        builder.setDefaultRequestOptions(requestOptions());
        builder.setLogLevel(Log.ERROR);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        final OkHttpClient okHttpClient = getOkHttpClient(context);
        final OkHttpUrlLoader.Factory okHttpUrlLoader = new OkHttpUrlLoader.Factory(okHttpClient);
        registry.replace(GlideUrl.class, InputStream.class, okHttpUrlLoader);
    }
}
