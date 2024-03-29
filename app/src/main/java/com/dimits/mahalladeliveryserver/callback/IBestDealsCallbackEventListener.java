package com.dimits.mahalladeliveryserver.callback;

import com.dimits.mahalladeliveryserver.model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallbackEventListener {
    void onListBestDealsloadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
