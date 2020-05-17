package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallbackEventListener {
    void onListBestDealsloadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
