package com.dimits.mahalladeliveryserver.ui.category;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.dimits.mahalladeliveryserver.common.Common;
import com.dimits.mahalladeliveryserver.common.CommonAgr;
import com.dimits.mahalladeliveryserver.common.MySwiperHelper;
import com.dimits.mahalladeliveryserver.eventbus.ToastEvent;
import com.dimits.mahalladeliveryserver.model.CategoryModel;
import com.dimits.mahalladeliveryserver.R;
import com.dimits.mahalladeliveryserver.adapter.MyCategoriesAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CategoryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private CategoryViewModel categoryViewModel;
    private Uri imageUri = null;




    //animation
    private Animation FadOpen, FadClose;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;



    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    List<CategoryModel> categoryModels;
    ImageView img_category;
    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {




        categoryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);

        unbinder = ButterKnife.bind(this, root);
        initView();
        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        categoryViewModel.getCategoryList().observe(getViewLifecycleOwner(), categoryModelList -> {
            dialog.dismiss();
            categoryModels = categoryModelList;
            adapter = new MyCategoriesAdapter(getContext(), categoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });
        return root;


    }


    private void initView() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();   Remove it to fix loading show when resume fragment
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_menu, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), getString(R.string.delete), 30, 0, Color.parseColor("#333639"),
                        position -> {
                            Common.categorySelected = categoryModels.get(position);

                            showDeleteDialog();
                        })
                );

                buf.add(new MyButton(getContext(), getString(R.string.update), 30, 0, Color.parseColor("#560027"),
                        position -> {
                            Common.categorySelected = categoryModels.get(position);

                            showUpdateDialog();
                        })
                );
            }
        };

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_creat){
            showAddDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.create));
        builder.setMessage(getString(R.string.pleasefillinformation));

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        Glide.with(getContext()).load(R.drawable.ic_add_black_24dp).into(img_category);


        //Set Event
        img_category.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton(getString(R.string.create), (dialogInterface, which) -> {



            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setName(edt_category_name.getText().toString());
            categoryModel.setFoods(new ArrayList<>());

            if (imageUri != null) {

                // firebase Storage upload image
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {

                        categoryModel.setImage(uri.toString());
                        addcategory(categoryModel);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            } else {
                addcategory(categoryModel);
            }

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete));
        builder.setMessage("هل تريد مسح هذا العنصر؟");
        builder.setNegativeButton(getString(R.string.cancel), (dialog1, which) ->
                dialog1.dismiss());
        builder.setPositiveButton(getString(R.string.delete), ((dialog1, which) -> deleteCategory()));
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCategory() {
        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .removeValue()
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, false));
                });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.update));
        builder.setMessage(getString(R.string.pleasefillinformation));

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);


        //Set Event
        img_category.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton(getString(R.string.update), (dialogInterface, which) -> {
            Map<String, Object> updateDate = new HashMap<>();
            updateDate.put("name", edt_category_name.getText().toString());

            if (imageUri != null) {

                // firebase Storage upload image
                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateDate.put("image", uri.toString());
                        updatecategory(updateDate);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            } else {
                updatecategory(updateDate);
            }

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updatecategory(Map<String, Object> updateDate) {
        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateDate)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, false));
                });
    }

    private void addcategory(CategoryModel categoryModel) {
        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .push()
                .setValue(categoryModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.CREAT, false));
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_category.setImageURI(imageUri);
            }
        }
    }


}