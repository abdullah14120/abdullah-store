/*
 * Developed & Modernized by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Main Activity Controller (Stable Build v1.0)
 */

package com.aurora.adroid.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class AuroraActivity extends BaseActivity {
    
    @BindView(R.id.action1) AppCompatImageView action1;
    @BindView(R.id.multi_text_layout) MultiTextLayout multiTextLayout;
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationView;
    @BindView(R.id.navigation) NavigationView navigation;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.floaty) FloatingActionButton fab;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.i("FIX ENGINE Core Engine initialized by: Abdullah Al-Tamimi");

        handleFirstLaunchLogic();

        if (!DatabaseUtil.isDatabaseAvailable(this)) {
            startRepoSyncService();
        }

        setupToolbar();
        setupSearch();
        setupDrawer();
        setupNavigation();
        checkPermissions();

        // معالجة الروابط الخارجية (Intent Data)
        if (getIntent() != null) {
            onNewIntent(getIntent());
        }
    }

    private void handleFirstLaunchLogic() {
        boolean isFirstLaunch = PrefUtil.getBoolean(this, Constants.PREFERENCE_FIRST_LAUNCH_2, true);
        
        if (isFirstLaunch) {
            Log.i("First Launch: Injecting Abdullah Al-Tamimi Config...");
            
            StaticRepo fixRepo = new StaticRepo();
            fixRepo.setRepoName("FIX ENGINE");
            fixRepo.setRepoId("FIX_PRO_DEFAULT");
            fixRepo.setRepoUrl(Constants.DOWNLOAD_REPO_URL); 
            fixRepo.setRepoFingerprint("UNRESTRICTED");
            
            new RepoListManager(this).addToRepoMap(fixRepo);
            
            PrefUtil.putBoolean(this, Constants.PREFERENCE_FIRST_LAUNCH_2, false);
            PrefUtil.putString(this, Constants.PREFERENCE_INSTALLATION_METHOD, "3"); // SessionInstaller
        }
    }

    private void setupToolbar() {
        multiTextLayout.setTxtPrimary("FIX");
        multiTextLayout.setTxtSecondary("ENGINE");
    }

    private void setupNavigation() {
        int backGroundColor = ContextCompat.getColor(this, R.color.colorPrimary);
        bottomNavigationView.setBackgroundColor(ColorUtils.setAlphaComponent(backGroundColor, 250));
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_main);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == bottomNavigationView.getSelectedItemId())
                return false;
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Menu menu = bottomNavigationView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (matchDestination(destination, item.getItemId())) {
                    item.setChecked(true);
                }
            }
        });
        
        // الدخول المباشر لواجهة التطبيقات
        navController.navigate(R.id.welcomeFragment); 
    }

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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fab != null) fab.show();
        Util.startNotificationService(this);
    }

    @Override
    protected void onDestroy() {
        // تنظيف الذاكرة بشكل كامل عند الإغلاق
        disposable.clear();
        Glide.get(this).clearMemory();
        super.onDestroy();
    }

    private void setupSearch() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void setupDrawer() {
        action1.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START, true));

        navigation.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent(this, ContainerActivity.class);
            int id = item.getItemId();
            if (id == R.id.action_all_apps) {
                intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_INSTALLED);
            } else if (id == R.id.action_download) {
                intent = new Intent(this, DownloadsActivity.class);
            } else if (id == R.id.action_setting) {
                intent = new Intent(this, SettingsActivity.class);
            } else if (id == R.id.action_about) {
                intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_ABOUT);
            }
            
            startActivity(intent);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // أذونات Android 13+ (الصور والإشعارات)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        } else {
            // الأذونات التقليدية
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1337);
            }
        }
    }
}
