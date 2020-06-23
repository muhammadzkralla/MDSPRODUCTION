package com.dimits.mahalladeliveryserver.callback;

import com.dimits.mahalladeliveryserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListerner {
    void onOrderLoadSuccess(List<OrderModel> orderModels);
    void onOrderLoadFailed(String message);
}
