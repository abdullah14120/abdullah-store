/*
 * Developed & Refined by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: System Constants & Cloud Integration
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid;

public class Constants {
    // توقيع المحرك المخصص
    public static final String TAG = "FIX ENGINE | Abdullah Al-Tamimi";

    // إعدادات المستودع الخاص بـ Cloudflare
    public static final String CLOUDFLARE_REPO_URL = "https://your-subdomain.pages.dev/"; // ضع رابطك هنا
    public static final String IMG_URL_PREFIX = "icons/"; // المسار داخل Cloudflare
    public static final String DATA_FILE_NAME = "apps.json"; // ملفك الذي برمجناه سابقاً
    
    // تعطيل فحص التوقيع الرقمي للمستودع (index-v1.jar) لضمان السرعة والسهولة
    public static final String SIGNED_FILE_NAME = "apps.json"; 

    public static final String SERVICE_PACKAGE = "com.aurora.services";
    public static final String REPO_AVAILABLE = "REPO_AVAILABLE";
    public static final String DATABASE_AVAILABLE = "DATABASE_AVAILABLE";
    public static final String DATABASE_DATE = "DATABASE_DATE";

    public static final String JAR = "jar";
    public static final String JSON = ".json";

    public static final String FILE_FAVOURITES = "/favourite.json";
    public static final String FILE_BLACKLIST = "/blacklist.json";

    public static final String NOTIFICATION_CHANNEL_ALERT = "FIX_NOTIFICATION_CHANNEL_ALERT";
    public static final String NOTIFICATION_CHANNEL_GENERAL = "FIX_NOTIFICATION_CHANNEL_GENERAL";

    public static final String PRIVILEGED_EXTENSION_PACKAGE_NAME = "com.aurora.services";
    public static final String PRIVILEGED_EXTENSION_SERVICE_INTENT = "com.aurora.services.IPrivilegedService";

    // ثوابت الإعدادات (Preferences)
    public static final String PREFERENCE_REPO_HEADER_MAP = "PREFERENCE_REPO_HEADER_MAP";
    public static final String PREFERENCE_FIRST_LAUNCH_2 = "PREFERENCE_FIRST_LAUNCH_2";
    public static final String PREFERENCE_BLACKLIST_PACKAGE_LIST = "PREFERENCE_BLACKLIST_PACKAGE_LIST";
    public static final String PREFERENCE_FAVOURITE_APPS = "PREFERENCE_FAVOURITE_APPS";
    public static final String PREFERENCE_REPO_MAP = "PREFERENCE_REPO_MAP";
    public static final String PREFERENCE_SYNC_MAP = "PREFERENCE_SYNC_MAP";
    public static final String PREFERENCE_DEFAULT_REPO_MAP = "PREFERENCE_DEFAULT_REPO_MAP";
    public static final String PREFERENCE_INCLUDE_SYSTEM = "PREFERENCE_INCLUDE_SYSTEM";
    public static final String PREFERENCE_INSTALLATION_AUTO = "PREFERENCE_INSTALLATION_AUTO";
    public static final String PREFERENCE_INSTALLATION_TYPE = "PREFERENCE_INSTALLATION_TYPE";
    public static final String PREFERENCE_INSTALLATION_METHOD = "PREFERENCE_INSTALLATION_METHOD";
    public static final String PREFERENCE_INSTALLATION_DELETE = "PREFERENCE_INSTALLATION_DELETE";
    public static final String PREFERENCE_INSTALLATION_PROFILE = "PREFERENCE_INSTALLATION_PROFILE";
    public static final String PREFERENCE_NOTIFICATION_TOGGLE = "PREFERENCE_NOTIFICATION_TOGGLE";
    public static final String PREFERENCE_UPDATES_INTERVAL = "PREFERENCE_UPDATES_INTERVAL";
    public static final String PREFERENCE_UPDATES_SUGGESTED = "PREFERENCE_UPDATES_SUGGESTED";
    public static final String PREFERENCE_UPDATES_EXPERIMENTAL = "PREFERENCE_UPDATES_EXPERIMENTAL";
    public static final String PREFERENCE_UI_THEME_2 = "PREFERENCE_UI_THEME_2";
    public static final String PREFERENCE_UI_TRANSPARENT = "PREFERENCE_UI_TRANSPARENT";
    public static final String PREFERENCE_DOWNLOAD_DIRECTORY = "PREFERENCE_DOWNLOAD_DIRECTORY";
    public static final String PREFERENCE_DOWNLOAD_INTERNAL = "PREFERENCE_DOWNLOAD_INTERNAL";
    public static final String PREFERENCE_DOWNLOAD_WIFI = "PREFERENCE_DOWNLOAD_WIFI";
    public static final String PREFERENCE_DOWNLOAD_ACTIVE = "PREFERENCE_DOWNLOAD_ACTIVE";
    public static final String PREFERENCE_DOWNLOAD_DEBUG = "PREFERENCE_DOWNLOAD_DEBUG";
    public static final String PREFERENCE_DOWNLOAD_STRATEGY = "PREFERENCE_DOWNLOAD_STRATEGY";
    public static final String PREFERENCE_ENABLE_PROXY = "PREFERENCE_ENABLE_PROXY";
    public static final String PREFERENCE_PROXY_HOST = "PREFERENCE_PROXY_HOST";
    public static final String PREFERENCE_PROXY_PORT = "PREFERENCE_PROXY_PORT";
    public static final String PREFERENCE_PROXY_TYPE = "PREFERENCE_PROXY_TYPE";
    public static final String PREFERENCE_REPO_UPDATE_INTERVAL = "PREFERENCE_REPO_UPDATE_INTERVAL";
    public static final String PREFERENCE_MIRROR_CHECKED = "PREFERENCE_MIRROR_CHECKED";
    public static final String PREFERENCE_LAUNCH_SERVICES = "PREFERENCE_LAUNCH_SERVICES";
    public static final String PREFERENCE_LOCALE_CUSTOM = "PREFERENCE_LOCALE_CUSTOM";
    public static final String PREFERENCE_LOCALE_LANG = "PREFERENCE_LOCALE_LANG";
    public static final String PREFERENCE_LOCALE_LIST = "PREFERENCE_LOCALE_LIST";
    public static final String PREFERENCE_LOCALE_COUNTRY = "PREFERENCE_LOCALE_COUNTRY";

    // أسماء الشاشات (Fragments)
    public static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    public static final String FRAGMENT_ABOUT = "FRAGMENT_ABOUT";
    public static final String FRAGMENT_INSTALLED = "FRAGMENT_INSTALLED";
    public static final String FRAGMENT_BLACKLIST = "FRAGMENT_BLACKLIST";
    public static final String FRAGMENT_FAV_LIST = "FRAGMENT_FAV_LIST";
    public static final String FRAGMENT_REPOSITORY = "FRAGMENT_REPOSITORY";

    public static final String INTENT_PACKAGE_NAME = "INTENT_PACKAGE_NAME";
    public static final String INT_EXTRA = "INT_EXTRA";
    public static final String FLOAT_EXTRA = "FLOAT_EXTRA";
    public static final String STRING_EXTRA = "STRING_EXTRA";
    public static final String STRING_REPO = "STRING_REPO";

    // ثوابت التحميل وتحديد الهوية
    public static final String DOWNLOAD_PACKAGE_NAME = "DOWNLOAD_PACKAGE_NAME";
    public static final String DOWNLOAD_DISPLAY_NAME = "DOWNLOAD_DISPLAY_NAME";
    public static final String DOWNLOAD_VERSION_NAME = "DOWNLOAD_VERSION_NAME";
    public static final String DOWNLOAD_VERSION_CODE = "DOWNLOAD_VERSION_CODE";
    public static final String DOWNLOAD_ICON_URL = "DOWNLOAD_ICON_URL";
    public static final String DOWNLOAD_APK_NAME = "DOWNLOAD_APK_NAME";

    public static final String DOWNLOAD_REPO_ID = "FIX_REPO_01";
    public static final String DOWNLOAD_REPO_NAME = "Abdullah Al-Tamimi Store";
    public static final String DOWNLOAD_REPO_URL = CLOUDFLARE_REPO_URL;
    public static final String DOWNLOAD_REPO_FINGERPRINT = "UNRESTRICTED";
}
