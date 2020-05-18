package com.vuducminh.nicefoodserver.ui.most_popular;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.adapter.MyBestDealsAdapter;
import com.vuducminh.nicefoodserver.adapter.MyMostPopularAdapter;
import com.vuducminh.nicefoodserver.model.BestDealsModel;
import com.vuducminh.nicefoodserver.model.MostPopularModel;
import com.vuducminh.nicefoodserver.ui.best_deals.BestDealsViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class MostPopularFragment extends Fragment {

    private MostPopularViewModel mViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_most_popular)
    RecyclerView recycler_most_popular;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMostPopularAdapter adapter;

    List<MostPopularModel> mostPopularModels;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel =
                new ViewModelProvider(this).get(MostPopularViewModel.class);
        View root = inflater.inflate(R.layout.most_popular_fragment, container, false);

        unbinder = ButterKnife.bind(this,root);
        initView();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        mViewModel.getMostPopularListMutable().observe(getViewLifecycleOwner(), (List<MostPopularModel> list) -> {
            dialog.dismiss();
            mostPopularModels = list;
            adapter = new MyMostPopularAdapter(getContext(),mostPopularModels);
            recycler_most_popular.setAdapter(adapter);
            recycler_most_popular.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initView() {


        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();   Remove it to fix loading show when resume fragment
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_most_popular.setLayoutManager(layoutManager);
        recycler_most_popular.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));
    }


}
