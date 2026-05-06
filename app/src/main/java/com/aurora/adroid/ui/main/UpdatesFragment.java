/*
 * Developed & Optimized by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Advanced Update Manager
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.UpdatesItem;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.generic.fragment.BaseFragment;
import com.aurora.adroid.ui.sheet.AppMenuSheet;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.UpdatesViewModel;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchGroup;
import com.tonyodev.fetch2.FetchListener;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdatesFragment extends BaseFragment {

    @BindView(R.id.viewFlipper) ViewFlipper2 viewFlipper;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.txt_update_all) AppCompatTextView txtUpdateAll;
    @BindView(R.id.btn_action) MaterialButton btnAction;

    private Fetch fetch;
    private final Set<UpdatesItem> selectedItems = new HashSet<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    private UpdatesViewModel model;
    private FastAdapter<UpdatesItem> fastAdapter;
    private ItemAdapter<UpdatesItem> itemAdapter;
    private SelectExtension<UpdatesItem> selectExtension;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // بصمة المطور عند تشغيل مدير التحديثات
        Log.i("FIX Update Engine [Abdullah Al-Tamimi]: Initialized");

        fetch = DownloadManager.getFetchInstance(requireContext());
        setupRecycler();

        model = new ViewModelProvider(requireActivity()).get(UpdatesViewModel.class);
        model.getAppsLiveData().observe(getViewLifecycleOwner(), updatesItems -> {
            dispatchAppsToAdapter(updatesItems);
            swipeLayout.setRefreshing(false);
        });

        disposable.add(AuroraApplication.getRxBus().getBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    switch (event.getType()) {
                        case BLACKLIST:
                        case INSTALLED:
                        case UNINSTALLED:
                            removeItemByPackageName(event.getStringExtra());
                            break;
                        case BULK_UPDATE_NOTIFY:
                            updatePageData();
                            break;
                    }
                }, throwable -> Log.e("FIX_BUS_ERROR: " + throwable.getMessage())));

        swipeLayout.setOnRefreshListener(() -> model.fetchUpdatableApps());
        
        // تلوين الـ SwipeRefresh بالهوية الجديدة
        swipeLayout.setColorSchemeColors(Util.getColorAttribute(requireContext(), R.attr.colorAccent));
    }

    private void removeItemByPackageName(String packageName) {
        int adapterPosition = -1;
        for (UpdatesItem updatesItem : itemAdapter.getAdapterItems()) {
            if (updatesItem.getPackageName().equals(packageName)) {
                adapterPosition = itemAdapter.getAdapterPosition(updatesItem);
                break;
            }
        }
        if (adapterPosition >= 0) {
            itemAdapter.remove(adapterPosition);
            updateItemList(packageName);
        }
    }

    private void updateItemList(String packageName) {
        AuroraApplication.removeFromOngoingUpdateList(packageName);
        updatePageData();
    }

    private void updatePageData() {
        updateText();
        updateButtons();
        updateButtonActions();

        if (itemAdapter != null && itemAdapter.getAdapterItemCount() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void dispatchAppsToAdapter(List<UpdatesItem> updatesItems) {
        itemAdapter.set(updatesItems);
        updatePageData();
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();
        selectExtension = new SelectExtension<>(fastAdapter);

        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final App app = item.getApp();
            final Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        fastAdapter.addExtension(selectExtension);
        fastAdapter.addEventHook(new UpdatesItem.CheckBoxClickEvent());

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            if (selected) selectedItems.add(item);
            else selectedItems.remove(item);
            updatePageData();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastAdapter);
    }

    private void updateText() {
        // نصوص حادة ومباشرة (دبلوماسية)
        if (selectExtension.getSelectedItems().size() > 0) {
            btnAction.setText("تحديث العناصر المختارة");
        } else {
            btnAction.setText("تحديث الكل فوراً");
        }
    }

    private void updateButtons() {
        final int size = itemAdapter.getAdapterItemCount();
        btnAction.setVisibility(size == 0 ? View.INVISIBLE : View.VISIBLE);
        txtUpdateAll.setVisibility(size == 0 ? View.INVISIBLE : View.VISIBLE);

        if (size > 0) {
            txtUpdateAll.setText(String.format("يوجد %d تحديثاً تقنياً متاحاً", size));
        }
    }

    private void updateButtonActions() {
        btnAction.setOnClickListener(null);
        btnAction.setEnabled(true);
        
        if (AuroraApplication.isBulkUpdateAlive()) {
            btnAction.setText("إلغاء العملية");
            btnAction.setOnClickListener(v -> {
                attachFetchCancelListener();
                btnAction.setEnabled(false);
            });
        } else {
            boolean selectiveUpdate = selectExtension.getSelectedItems().size() > 0;
            btnAction.setOnClickListener(v -> {
                btnAction.setEnabled(false);
                disposable.add(Observable.fromIterable(selectiveUpdate ? selectedItems : itemAdapter.getAdapterItems())
                        .map(UpdatesItem::getApp)
                        .toList()
                        .subscribe(apps -> {
                            AuroraApplication.setOngoingUpdateList(new ArrayList<>(apps));
                            Util.startBulkUpdateService(requireContext());
                        }, throwable -> Log.e("FIX_UPDATE_ERROR: " + throwable.getMessage())));
            });
        }
    }

    private void attachFetchCancelListener() {
        boolean selectiveUpdate = selectExtension.getSelectedItems().size() > 0;
        disposable.add(Observable.fromIterable(selectiveUpdate ? selectedItems : itemAdapter.getAdapterItems())
                .map(updatesItem -> updatesItem.getPackageName().hashCode())
                .subscribe(hashcode -> {
                    fetch.addListener(new AbstractFetchGroupListener() {
                        @Override
                        public void onAdded(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                            if (hashcode == groupId) { fetch.cancelGroup(groupId); fetch.removeListener(this); }
                        }
                    });
                }, throwable -> Log.e(throwable.getMessage()), () -> {
                    AuroraApplication.setOngoingUpdateList(new ArrayList<>());
                    Util.stopBulkUpdateService(requireContext());
                }));
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }
}
