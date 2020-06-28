package com.dimits.mahalladeliveryserver.ui.food_list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.dimits.mahalladeliveryserver.adapter.MyFoodListAdapter;
import com.dimits.mahalladeliveryserver.common.Common;
import com.dimits.mahalladeliveryserver.common.CommonAgr;
import com.dimits.mahalladeliveryserver.common.MySwiperHelper;
import com.dimits.mahalladeliveryserver.eventbus.AddonSizeEditEvent;
import com.dimits.mahalladeliveryserver.eventbus.ChangeMenuClick;
import com.dimits.mahalladeliveryserver.eventbus.ToastEvent;
import com.dimits.mahalladeliveryserver.model.BestDealsModel;
import com.dimits.mahalladeliveryserver.model.FoodModel;
import com.dimits.mahalladeliveryserver.R;
import com.dimits.mahalladeliveryserver.model.MostPopularModel;
import com.dimits.mahalladeliveryserver.ui.SizeAddonEditActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;

    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;
    private ImageView img_food;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                  ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {
           if(foodModels != null) {
               foodModelList = foodModels;
               adapter = new MyFoodListAdapter(getContext(),foodModelList);
               recycler_food_list.setAdapter(adapter);
               recycler_food_list.setLayoutAnimation(layoutAnimationController);
           }
        });
        return root;
    }

    private void initViews() {
        setHasOptionsMenu(true);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();


        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar())
                .setTitle(Common.categorySelected.getName()
                );

        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        //Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(),recycler_food_list,width/7) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9B0000"),
                        position -> {
                           if(foodModelList != null) {
                               Common.selectedFood = foodModelList.get(position);
                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                               builder.setTitle("DELETE")
                                       .setMessage("Do you want to delete this food ?")
                                       .setNegativeButton("CANCLE", (dialogInterface, which) -> {
                                           dialogInterface.dismiss();
                                       })
                                       .setPositiveButton("DELETE", (dialog, which) -> {
                                           FoodModel foodModel = adapter.getItemAtPosition(position);   //Get item in adapter
                                           if(foodModel.getPositionInList() == 0) {
                                               Common.categorySelected.getFoods().remove(position);   // if == -1, do nothing

                                           }
                                           else{
                                               Common.categorySelected.getFoods().remove(foodModel.getPositionInList());  //Remove by index saved
                                           }
                                           updateFood(Common.categorySelected.getFoods(),Common.ACTION.DELETE);
                                       });

                               AlertDialog dialog = builder.create();
                               dialog.show();
                           }
                        })
                );

                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        position -> {
                            FoodModel foodModel = adapter.getItemAtPosition(position);
                            if(foodModel.getPositionInList() == 0) {
                                showUpdateDialog(position,foodModel);
                            }
                            else {
                                showUpdateDialog(foodModel.getPositionInList(),foodModel);
                            }
                        })
                );
                buf.add(new MyButton(getContext(), "Size", 30, 0, Color.parseColor("#12005E"),
                        position -> {
                            FoodModel foodModel = adapter.getItemAtPosition(position);
                            if(foodModel.getPositionInList() == 0) {
                                Common.selectedFood = foodModelList.get(position);
                            }
                            else {
                                Common.selectedFood = foodModel;
                            }

                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));

                            if(foodModel.getPositionInList() == 0) {
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false,position));
                            }
                            else {
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false,foodModel.getPositionInList()));
                            }
                        })
                );

                buf.add(new MyButton(getContext(), "Addon", 30, 0, Color.parseColor("#336699"),
                        position -> {
                            FoodModel foodModel = adapter.getItemAtPosition(position);
                            if(foodModel.getPositionInList() == 0) {
                                Common.selectedFood = foodModelList.get(position);
                            }
                            else {
                                Common.selectedFood = foodModel;
                            }
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            if(foodModel.getPositionInList() == 0) {
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true,position));
                            }
                            else {
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true,foodModel.getPositionInList()));

                            }
                        })
                );
                buf.add(new MyButton(getContext(),"recommended", 25, 0, Color.parseColor("#9B0000"),
                        position -> {
                            if(foodModelList != null) {
                                Common.selectedFood = foodModelList.get(position);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Add")
                                        .setMessage("Do you want to add this food to recommended?")
                                        .setNegativeButton("CANCLE", (dialogInterface, which) -> {
                                            dialogInterface.dismiss();
                                        })
                                        .setPositiveButton("ADD", (dialog, which) -> {
                                            BestDealsModel bestDealsModel = new BestDealsModel();
                                            bestDealsModel.setFood_id(Common.selectedFood.getId());
                                            bestDealsModel.setMenu_id(Common.categorySelected.getMenu_id());
                                            bestDealsModel.setImage(Common.selectedFood.getImage());
                                            bestDealsModel.setName(Common.selectedFood.getName());

                                            addtoFirebase(bestDealsModel);
                                        });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        })
                );
                buf.add(new MyButton(getContext(), "Common Food", 25, 0, Color.parseColor("#7B0000"),
                        position -> {
                            if(foodModelList != null) {
                                Common.selectedFood = foodModelList.get(position);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Add")
                                        .setMessage("Do you want to add this food to common food?")
                                        .setNegativeButton("CANCLE", (dialogInterface, which) -> {
                                            dialogInterface.dismiss();
                                        })
                                        .setPositiveButton("ADD", (dialog, which) -> {
                                            MostPopularModel mostPopularModel = new MostPopularModel();

                                            mostPopularModel.setFood_id(Common.selectedFood.getId());
                                            mostPopularModel.setMenu_id(Common.categorySelected.getMenu_id());
                                            mostPopularModel.setImage(Common.selectedFood.getImage());
                                            mostPopularModel.setName(Common.selectedFood.getName());

                                            addtoFirebaseMostPopular(mostPopularModel);
                                        });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        })
                );

            }
        };

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_creat)
            showAddDialog();
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Create");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food,null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food);

        // Set Date

        Glide.with(getContext()).load(R.drawable.ic_add_white_24dp).into(img_food);

        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCLE", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("CREATE", (dialogInterface, which) -> {
            FoodModel updateFood = new FoodModel();
            updateFood.setId(UUID.randomUUID().toString());
            updateFood.setName(edt_food_name.getText().toString());
            updateFood.setDescription(edt_food_description.getText().toString());
            updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                    Double.parseDouble(edt_food_price.getText().toString()));

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
                        updateFood.setImage(uri.toString());
                        if (Common.categorySelected.getFoods() ==null)
                            Common.categorySelected.setFoods(new ArrayList<>());
                        Common.categorySelected.getFoods().add(updateFood);
                        updateFood(Common.categorySelected.getFoods(),Common.ACTION.CREAT);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    long progress = (100 * taskSnapshot.getBytesTransferred() /(long) taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }
            else {
                if (Common.categorySelected.getFoods() ==null)
                    Common.categorySelected.setFoods(new ArrayList<>());
                Common.categorySelected.getFoods().add(updateFood);
                updateFood(Common.categorySelected.getFoods(),Common.ACTION.CREAT);
            }
        });

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    private void addtoFirebaseMostPopular(MostPopularModel mostPopularModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.MOST_POPULAR)
                .push().setValue(mostPopularModel);
        Toast.makeText(getContext(),"Done",Toast.LENGTH_SHORT).show();
    }

    private void addtoFirebase(BestDealsModel bestDealsModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.BEST_DEALS)
                .push().setValue(bestDealsModel);
        Toast.makeText(getContext(),"Done",Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(int position,FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food,null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food);

        // Set Date
        edt_food_name.setText(new StringBuilder("")
                .append(foodModel.getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_food_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);

        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCLE", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("UPDATE", (dialogInterface, which) -> {
            FoodModel updateFood = foodModel;
            updateFood.setName(edt_food_name.getText().toString());
            updateFood.setDescription(edt_food_description.getText().toString());
            updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                    Double.parseDouble(edt_food_price.getText().toString()));

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
                        updateFood.setImage(uri.toString());
                        Common.categorySelected.getFoods().set(position,updateFood);
                        updateFood(Common.categorySelected.getFoods(),Common.ACTION.UPDATE);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    long progress = (100 * taskSnapshot.getBytesTransferred() /(long) taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                });
            }
            else {
                Common.categorySelected.getFoods().set(position,updateFood);
                updateFood(Common.categorySelected.getFoods(),Common.ACTION.UPDATE);
            }
        });

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    private void updateFood(List<FoodModel> foods, Common.ACTION action) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("foods",foods);

        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       foodListViewModel.getMutableLiveDataFoodList();
                       EventBus.getDefault().postSticky(new ToastEvent(action,true));
                   }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));


        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Clean text when click to Clean button
        ImageView cleanButton = (ImageView)searchView.findViewById(R.id.search_close_btn);
        cleanButton.setOnClickListener(v -> {
            EditText edt_search = (EditText)searchView.findViewById(R.id.search_src_text);
            //Clean text
            edt_search.setText("");
            //Clean query
            searchView.setQuery("",false);
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to origiral
            foodListViewModel.getMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());
        });

    }

    private void startSearchFood(String query) {
        List<FoodModel> resultFood = new ArrayList<>();
        for(int i = 0; i < Common.categorySelected.getFoods().size(); i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if(foodModel.getName().toLowerCase().contains(query.toLowerCase())) {
                foodModel.setPositionInList(i);
                resultFood.add(foodModel);
            }
        }
        foodListViewModel.getMutableLiveDataFoodList().setValue(resultFood);
    }
}