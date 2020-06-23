package com.dimits.mahalladeliveryserver.callback;

import com.dimits.mahalladeliveryserver.model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallBackListener {
    void onListMostPopularloadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
