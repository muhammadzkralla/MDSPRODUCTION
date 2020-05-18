package com.vuducminh.nicefoodserver.callback;

import com.vuducminh.nicefoodserver.model.BestDealsModel;
import com.vuducminh.nicefoodserver.model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallBackListener {
    void onListMostPopularloadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
