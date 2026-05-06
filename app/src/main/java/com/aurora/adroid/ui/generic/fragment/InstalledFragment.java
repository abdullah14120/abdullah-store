/*
 * Developed & Refined by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Installed Apps Manager (Stable Build)
 */

package com.aurora.adroid.ui.generic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.InstalledItem;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.InstalledAppsViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class InstalledFragment extends BaseFragment {

    // حذفنا @BindView من هنا لأنها تسبب خطأ في معالجة الكائنات المخصصة
    ViewFlipper2 viewFlipper;
    
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.switch_system)
    SwitchMaterial switchSystem;

    private InstalledAppsViewModel model;
    private FastAdapter<InstalledItem> fastAdapter;
    private ItemAdapter<InstalledItem> itemAdapter;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_installed, container, false);
        
        // ربط العناصر المدعومة بـ ButterKnife
        unbinder = ButterKnife.bind(this, view);
        
        // الربط اليدوي للكائن المسبب للمشكلة (الحل الجراحي)
        viewFlipper = view.findViewById(R.id.viewFlipper);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Log.i("Installed Apps Engine [Abdullah Al-Tamimi]: Initializing...");
        
        setupRecycler();

        switchSystem.setChecked(PrefUtil.getBoolean(requireContext(), Constants.PREFERENCE_INCLUDE_SYSTEM));
        switchSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefUtil.putBoolean(requireContext(), Constants.PREFERENCE_INCLUDE_SYSTEM, isChecked);
            model.fetchInstalledApps(isChecked);
        });

        model = new ViewModelProvider(this).get(InstalledAppsViewModel.class);
        model.getData().observe(getViewLifecycleOwner(), installedItems -> {
            if (installedItems != null) {
                dispatchAppsToAdapter(installedItems);
            }
            swipeLayout.setRefreshing(false);
        });

        // تحسين أداء الـ RxBus لضمان تحديث القائمة فور حذف أي تطبيق
        AuroraApplication.getRxBus().getBus()
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.getType() == com.aurora.adroid.model.events.Event.TYPE.UNINSTALLED) {
                        removeItemByPackageName(event.getStringExtra());
                    }
                }, throwable -> Log.e("FIX_BUS_ERROR: " + throwable.getMessage()));

        swipeLayout.setOnRefreshListener(() -> model.fetchInstalledApps(switchSystem.isChecked()));
    }

    private void removeItemByPackageName(String packageName) {
        if (itemAdapter == null) return;
        int adapterPosition = -1;
        for (InstalledItem installedItem : itemAdapter.getAdapterItems()) {
            if (installedItem.getPackageName().equals(packageName)) {
                adapterPosition = itemAdapter.getAdapterPosition(installedItem);
                break;
            }
        }
        if (adapterPosition >= 0) {
            itemAdapter.remove(adapterPosition);
            updatePageData();
        }
    }

    private void updatePageData() {
        if (viewFlipper == null) return;
        if (itemAdapter != null && itemAdapter.getAdapterItemCount() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void dispatchAppsToAdapter(List<InstalledItem> installedItems) {
        itemAdapter.set(installedItems);
        updatePageData();
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();
        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final App app = item.getApp();
            final Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return true;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(fastAdapter);
        
        new FastScrollerBuilder(recyclerView)
                .useMd2Style()
                .build();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
