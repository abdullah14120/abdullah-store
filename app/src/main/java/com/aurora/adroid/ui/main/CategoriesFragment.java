/*
 * Developed & Modernized by: Abdullah Al-Tamimi
 * Project: FIX ENGINE Store
 * Component: Categories Logic - High Contrast Management
 * * Original Copyright (C) 2019-20, Rahul Kumar Patel
 */

package com.aurora.adroid.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.section.CategoriesSection;
import com.aurora.adroid.task.CategoriesTask;
import com.aurora.adroid.util.Log;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CategoriesFragment extends Fragment {

    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // استخدام واجهة FIX ENGINE السوداء
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // تسجيل بدء محرك التصنيفات باسم المطور
        Log.i("Categories Engine [Abdullah Al-Tamimi]: Initializing...");
        
        fetchCategories();
    }

    private void setupRecycler(List<String> categoryList) {
        SectionedRecyclerViewAdapter adapter = new SectionedRecyclerViewAdapter();
        
        // ترتيب التصنيفات أبجدياً بشكل صارم
        Collections.sort(categoryList, String::compareToIgnoreCase);
        
        // استخدام التصميم الحاد للتصنيفات
        CategoriesSection section = new CategoriesSection(requireContext(), categoryList, getString(R.string.title_categories));
        adapter.addSection(section);
        
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        
        // تحسين أداء التمرير في الشاشات الطويلة
        recycler.setHasFixedSize(true);
    }

    private void fetchCategories() {
        disposable.add(Observable.fromCallable(() -> new CategoriesTask(requireContext())
                .getCategories())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categoryList -> {
                    if (categoryList != null && !categoryList.isEmpty()) {
                        setupRecycler(categoryList);
                    }
                }, throwable -> {
                    Log.e("FIX_CATEGORIES_ERROR: " + throwable.getMessage());
                }));
    }

    @Override
    public void onDestroyView() {
        // تنظيف الذاكرة لضمان استقرار التطبيق
        disposable.clear();
        super.onDestroyView();
    }
}
