package com.vuducminh.nicefoodserver.ui.best_deals;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodserver.callback.IBestDealsCallbackEventListener;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.model.BestDealsModel;

import java.util.ArrayList;
import java.util.List;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallbackEventListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<BestDealsModel>> bestDealsListMutable;
    private IBestDealsCallbackEventListener bestDealsCallbackListener;


    public BestDealsViewModel(){
        bestDealsCallbackListener = this;
    }

    public MutableLiveData<List<BestDealsModel>> getBestDealsListMutable() {
        if (bestDealsListMutable == null)
            bestDealsListMutable = new MutableLiveData<>();
        loadBestDeals();
        return bestDealsListMutable;
    }

    private void loadBestDeals() {
        List<BestDealsModel> temp = new ArrayList<>();
        DatabaseReference bestDealsRef = FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS);
        bestDealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot bestDealsSnapShot : dataSnapshot.getChildren())
                {
                    BestDealsModel bestDealsModel = bestDealsSnapShot.getValue(BestDealsModel.class);
                    bestDealsModel.setKey(bestDealsSnapShot.getKey());
                    temp.add(bestDealsModel);
                }
                bestDealsCallbackListener.onListBestDealsloadSuccess(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                bestDealsCallbackListener.onListBestDealsLoadFailed(databaseError.getMessage());

            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListBestDealsloadSuccess(List<BestDealsModel> bestDealsModels) {
        bestDealsListMutable.setValue(bestDealsModels);

    }

    @Override
    public void onListBestDealsLoadFailed(String message) {
        messageError.setValue(message);

    }
}
