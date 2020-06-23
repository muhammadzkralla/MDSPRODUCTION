package com.dimits.mahalladeliveryserver.ui.category;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.dimits.mahalladeliveryserver.callback.ICategoryCallbackListener;
import com.dimits.mahalladeliveryserver.common.Common;
import com.dimits.mahalladeliveryserver.common.CommonAgr;
import com.dimits.mahalladeliveryserver.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel implements ICategoryCallbackListener {

    private MutableLiveData<List<CategoryModel>> categoryList;
    private DatabaseReference categoryRef;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private ICategoryCallbackListener categoryCallbackListener;





    public CategoryViewModel() {
        categoryCallbackListener = this;
    }


    public MutableLiveData<List<CategoryModel>> getCategoryList() {
        if(categoryList == null) {
            categoryList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategories();
        }
        return categoryList;
    }

    public void loadCategories() {
        
        List<CategoryModel> tempList = new ArrayList<>();
         categoryRef = FirebaseDatabase.getInstance().getReference(CommonAgr.RESTAURANT_REF)
                 .child(Common.currentServerUser.getRestaurant())
                 .child(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnaShot : dataSnapshot.getChildren()) {
                        CategoryModel categoryModel = itemSnaShot.getValue(CategoryModel.class);
                        categoryModel.setMenu_id(itemSnaShot.getKey());
                        tempList.add(categoryModel);
                    }
                    if (tempList.size() > 0)
                        categoryCallbackListener.onCategoryLoadSuccess(tempList);
                    else
                        categoryCallbackListener.onCategoryLoadFailed("Category empty!");
                }else
                    categoryCallbackListener.onCategoryLoadFailed("Category not exist!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                categoryCallbackListener.onCategoryLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
        categoryList.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
        messageError.setValue(message);
    }
}