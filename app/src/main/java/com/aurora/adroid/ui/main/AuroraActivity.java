/*
 * Developed & Modernized by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Main Activity Controller
 */

package com.aurora.adroid.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.ui.generic.activity.BaseActivity;
import com.aurora.adroid.ui.generic.activity.ContainerActivity;
import com.aurora.adroid.ui.generic.activity.DownloadsActivity;
import com.aurora.adroid.ui.generic.activity.SearchActivity;
import com.aurora.adroid.ui.setting.SettingsActivity;
import com.aurora.adroid.ui.view.MultiTextLayout;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class AuroraActivity extends BaseActivity {
    
    @BindView(R.id.action1) AppCompatImageView action1;
    @BindView(R.id.multi_text_layout) MultiTextLayout multiTextLayout;
    @BindView(R.id.action2) AppCompatImageView action2;
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationView;
    @BindView(R.id.navigation) NavigationView navigation;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.floaty) FloatingActionButton fab;

    private CompositeDisposable disposable = new CompositeDisposable();
    
    // تعديل عبدالله التميمي: فتح شاشة الترحيب الافتراضية (0) التي سنعدلها لاحقاً 
    // لتصبح شاشة تطبيقاتك المباشرة
    private int fragmentCur = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // بصمة المطور في سجلات التشغيل
        Log.i("FIX ENGINE Core Engine initialized by: Abdullah Al-Tamimi");

        // 1. منطق التجاوز التلقائي (Auto-Initialization)
        handleFirstLaunchLogic();

        if (!DatabaseUtil.isDatabaseAvailable(this)) {
            // مزامنة تلقائية هادئة بدلاً من إزعاج المستخدم بدايلوج
            startRepoSyncService();
        }

        setupToolbar();
        setupSearch();
        setupDrawer();
        setupNavigation();
        checkPermissions();

        onNewIntent(getIntent());
    }

    /**
     * تعديل احترافي: التعامل مع التشغيل الأول وحقن مستودع عبدالله التميمي
     */
    private void handleFirstLaunchLogic() {
        boolean isFirstLaunch = PrefUtil.getBoolean(this, Constants.PREFERENCE_FIRST_LAUNCH_2, true);
        
        if (isFirstLaunch) {
            Log.i("First Launch Detected: Configuring Abdullah Al-Tamimi Repository...");
            
            // إضافة مستودع FIX ENGINE برمجياً
            StaticRepo fixRepo = new StaticRepo();
            fixRepo.setRepoName("FIX ENGINE");
            fixRepo.setRepoId("FIX_PRO_DEFAULT");
            fixRepo.setRepoUrl(Constants.DOWNLOAD_REPO_URL); 
            fixRepo.setRepoFingerprint("UNRESTRICTED");
            
            new RepoListManager(this).addToRepoMap(fixRepo);
            
            // تجاوز شاشة الترحيب (Intro) مستقبلاً
            PrefUtil.putBoolean(this, Constants.PREFERENCE_FIRST_LAUNCH_2, false);
            
            // ضبط الإعدادات الافتراضية لتكون "بدون قيود"
            PrefUtil.putString(this, Constants.PREFERENCE_INSTALLATION_METHOD, "3"); // SessionInstaller
        }
    }

    private void setupToolbar() {
        // تغيير اسم المتجر في التولبار
        multiTextLayout.setTxtPrimary("FIX");
        multiTextLayout.setTxtSecondary("ENGINE");
    }

    private void setupNavigation() {
        // تلوين الخلفية بالأسود العميق الذي اخترناه في الألوان
        int backGroundColor = ContextCompat.getColor(this, R.color.colorPrimary);
        bottomNavigationView.setBackgroundColor(ColorUtils.setAlphaComponent(backGroundColor, 250));
        navigation.setBackgroundColor(backGroundColor);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_main);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == bottomNavigationView.getSelectedItemId())
                return false;
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            final Menu menu = bottomNavigationView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (matchDestination(destination, item.getItemId())) {
                    item.setChecked(true);
                }
            }
        });

        // توجيه مباشر إلى الشاشة الرئيسية (Apps) بدلاً من أي شاشات أخرى
        navController.navigate(R.id.welcomeFragment); 
    }

    // ... (باقي الدوال مع الحفاظ على استقرار النظام) ...

    static boolean matchDestination(@NonNull NavDestination destination, @IdRes int destId) {
        NavDestination currentDestination = destination;
        while (currentDestination.getId() != destId && currentDestination.getParent() != null) {
            currentDestination = currentDestination.getParent();
        }
        return currentDestination.getId() == destId;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START, true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.show();
        Util.toggleSoftInput(this, false);
        Util.startNotificationService(this);
    }

    @Override
    protected void onDestroy() {
        try {
            Glide.with(this).pauseAllRequests();
            disposable.clear();
            disposable.dispose();
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    private void setupSearch() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
            fab.post(() -> fab.hide());
        });
    }

    private void setupDrawer() {
        action1.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.openDrawer(GravityCompat.START, true);
        });

        navigation.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent(this, ContainerActivity.class);
            switch (item.getItemId()) {
                case R.id.action_all_apps:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_INSTALLED);
                    startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_download:
                    startActivity(new Intent(this, DownloadsActivity.class), ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_setting:
                    startActivity(new Intent(this, SettingsActivity.class), ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_about:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_ABOUT);
                    startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });
    }

    private void startRepoSyncService() {
        Intent intent = new Intent(this, SyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1337);
        }
    }
}
