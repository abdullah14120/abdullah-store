/*
 * Developed & Refined by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Main Dashboard Controller (Unrestricted & Optimized)
 */

package com.aurora.adroid.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.items.RepoItem;
import com.aurora.adroid.model.items.cluster.GenericClusterItem;
import com.aurora.adroid.model.items.cluster.NewClusterItem;
import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.generic.activity.GenericAppActivity;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.ClusterAppsViewModel;
import com.aurora.adroid.viewmodel.IndexModel;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    @BindView(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
    @BindView(R.id.recycler_repo) RecyclerView recyclerViewIndices;
    @BindView(R.id.recycler_latest) RecyclerView recyclerViewUpdates;
    @BindView(R.id.recycler_new) RecyclerView recyclerViewNew;

    private FastItemAdapter<NewClusterItem> fastItemAdapterNew;
    private FastItemAdapter<GenericClusterItem> fastItemAdapterUpdates;
    private FastItemAdapter<RepoItem> fastItemAdapterIndices;
    
    private final CompositeDisposable disposable = new CompositeDisposable();
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("FIX Dashboard Engine initialized by: Abdullah Al-Tamimi");

        // ضبط الألوان برمجياً لضمان التوافق مع الهوية البصرية
        swipeLayout.setColorSchemeColors(Color.parseColor("#00E676"));
        swipeLayout.setProgressBackgroundColorSchemeColor(Color.parseColor("#161616"));

        setupNewApps();
        setupUpdatedApps();
        setupRepository();

        initViewModels();
        initRxBus();

        swipeLayout.setOnRefreshListener(this::startRepoSyncService);
    }

    private void initViewModels() {
        ClusterAppsViewModel clusterModel = new ViewModelProvider(requireActivity()).get(ClusterAppsViewModel.class);
        
        clusterModel.getNewAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            if (apps == null) return;
            disposable.add(Observable.fromIterable(apps)
                    .subscribeOn(Schedulers.io())
                    .map(NewClusterItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(clusterItems -> fastItemAdapterNew.set(clusterItems), 
                              throwable -> Log.e("FIX_UI_ERROR: " + throwable.getMessage())));
        });

        clusterModel.getUpdatedAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            if (apps == null) return;
            disposable.add(Observable.fromIterable(apps)
                    .subscribeOn(Schedulers.io())
                    .map(GenericClusterItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(clusterItems -> fastItemAdapterUpdates.set(clusterItems), 
                              throwable -> Log.e("FIX_UI_ERROR: " + throwable.getMessage())));
        });

        IndexModel indexModel = new ViewModelProvider(requireActivity()).get(IndexModel.class);
        indexModel.getAllIndicesLive().observe(getViewLifecycleOwner(), indices -> {
            if (indices == null) return;
            final RepoSyncManager repoSyncManager = new RepoSyncManager(requireContext());
            disposable.add(Observable.fromIterable(indices)
                    .filter(index -> repoSyncManager.isSynced(index.getRepoId()))
                    .map(RepoItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(indexItems -> {
                        fastItemAdapterIndices.set(indexItems);
                    }, throwable -> Log.e("FIX_REPO_ERROR: " + throwable.getMessage())));
        });
    }

    private void initRxBus() {
        disposable.add(AuroraApplication.getRxBus().getBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    switch (event.getType()) {
                        case SYNC_EMPTY:
                            swipeLayout.setRefreshing(false);
                            ContextUtil.toastLong(requireContext(), "المستودع فارغ حالياً");
                            break;
                        case SYNC_COMPLETED:
                            ContextUtil.toastLong(requireContext(), "تم تحديث محرك FIX بنجاح");
                            swipeLayout.setRefreshing(false);
                            break;
                        case SYNC_NO_UPDATES:
                            swipeLayout.setRefreshing(false);
                            break;
                    }
                }, throwable -> Log.e("FIX_BUS_ERROR: " + throwable.getMessage())));
    }

    private void startRepoSyncService() {
        if (SyncService.isServiceRunning()) {
            swipeLayout.setRefreshing(false);
            return;
        }

        Log.i("Manual Sync Triggered: Abdullah Al-Tamimi Repository");
        final Intent intent = new Intent(requireActivity(), SyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(intent);
        } else {
            requireActivity().startService(intent);
        }
    }

    @OnClick(R.id.header_new_apps)
    public void showAllNewApps() {
        Intent intent = new Intent(requireContext(), GenericAppActivity.class);
        intent.putExtra("LIST_TYPE", 0);
        startActivity(intent);
    }

    @OnClick(R.id.header_updated_apps)
    public void showAllUpdatedApps() {
        Intent intent = new Intent(requireContext(), GenericAppActivity.class);
        intent.putExtra("LIST_TYPE", 1);
        startActivity(intent);
    }

    private void setupRepository() {
        fastItemAdapterIndices = new FastItemAdapter<>();
        fastItemAdapterIndices.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), GenericAppActivity.class);
            intent.putExtra("LIST_TYPE", 3);
            intent.putExtra("REPO_ID", item.getIndex().getRepoId());
            intent.putExtra("REPO_NAME", item.getIndex().getName());
            startActivity(intent);
            return true;
        });
        recyclerViewIndices.setAdapter(fastItemAdapterIndices);
        recyclerViewIndices.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
    }

    private void setupNewApps() {
        fastItemAdapterNew = new FastItemAdapter<>();
        fastItemAdapterNew.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, item.getPackageName());
            intent.putExtra(Constants.STRING_REPO, item.getApp().getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return true;
        });

        recyclerViewNew.setAdapter(fastItemAdapterNew);
        recyclerViewNew.setLayoutManager(new GridLayoutManager(requireContext(), 2, RecyclerView.HORIZONTAL, false));
    }

    private void setupUpdatedApps() {
        fastItemAdapterUpdates = new FastItemAdapter<>();
        fastItemAdapterUpdates.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, item.getPackageName());
            intent.putExtra(Constants.STRING_REPO, item.getApp().getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return true;
        });

        recyclerViewUpdates.setAdapter(fastItemAdapterUpdates);
        recyclerViewUpdates.setLayoutManager(new GridLayoutManager(requireContext(), 2, RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swipeLayout.setRefreshing(false);
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }
}
