package com.dimits.mahalladeliveryserver.ui.best_deals;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.dimits.mahalladeliveryserver.R;
import com.dimits.mahalladeliveryserver.adapter.MyBestDealsAdapter;
import com.dimits.mahalladeliveryserver.common.Common;
import com.dimits.mahalladeliveryserver.common.MySwiperHelper;
import com.dimits.mahalladeliveryserver.eventbus.ToastEvent;
import com.dimits.mahalladeliveryserver.model.BestDealsModel;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class BestDealsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private BestDealsViewModel mViewModel;
    Unbinder unbinder;
    @BindView(R.id.recycler_best_deal)
    RecyclerView recycler_best_deal;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBestDealsAdapter adapter;

    List<BestDealsModel> bestDealsModels;

    private Uri imageUri = null;
    ImageView img_best_deals;

    FirebaseStorage storage;
    StorageReference storageReference;



    public static BestDealsFragment newInstance() {
        return new BestDealsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel =
                new ViewModelProvider(this).get(BestDealsViewModel.class);
        View root = inflater.inflate(R.layout.best_deals_fragment, container, false);

        unbinder = ButterKnife.bind(this,root);
        initView();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        mViewModel.getBestDealsListMutable().observe(getViewLifecycleOwner(), (List<BestDealsModel> list) -> {
            dialog.dismiss();
            bestDealsModels = list;
            adapter = new MyBestDealsAdapter(bestDealsModels, getContext());
            recycler_best_deal.setAdapter(adapter);
            recycler_best_deal.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initView() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();   Remove it to fix loading show when resume fragment
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_best_deal.setLayoutManager(layoutManager);
        recycler_best_deal.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(),recycler_best_deal,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(), "مسح", 30, 0, Color.parseColor("#333639"),
                        position -> {
                            Common.bestDealsSelected = bestDealsModels.get(position);

                            showDeleteDialog();
                        }));

                buf.add(new MyButton(getContext(), "تعديل", 30, 0, Color.parseColor("#560027"),
                        position -> {
                            Common.bestDealsSelected = bestDealsModels.get(position);

                            showUpdateDialog();
                        })
                );
            }
        };

    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("مسح");
        builder.setMessage("هل تريد مسح هذا العنصر؟");
        builder.setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("مسح", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBestDeals();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void deleteBestDeals() {
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    mViewModel.loadBestDeals();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE,true));
                });
    }



    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("تعديل");
        builder.setMessage("االرجاء ملئ البيانات");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category,null);
        EditText edt_category_name = (EditText)itemView.findViewById(R.id.edt_category_name);
        img_best_deals = (ImageView)itemView.findViewById(R.id.img_category);

        //Set Data
        edt_category_name.setText(new StringBuilder("").append(Common.bestDealsSelected.getName()));
        Glide.with(getContext()).load(Common.bestDealsSelected.getImage()).into(img_best_deals);


        //Set Event
        img_best_deals.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("الغاء", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("تعديل", (dialogInterface, which) -> {
            Map<String,Object> updateDate = new HashMap<>();
            updateDate.put("name",edt_category_name.getText().toString());

            if(imageUri != null) {

                // firebase Storage upload image
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateDate.put("image",uri.toString());
                        updateBestDeals(updateDate);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }
            else {
                updateBestDeals(updateDate);
            }

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void updateBestDeals(Map<String, Object> updateDate) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .updateChildren(updateDate)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    mViewModel.loadBestDeals();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE,true));
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_best_deals.setImageURI(imageUri);
            }
        }
    }
}