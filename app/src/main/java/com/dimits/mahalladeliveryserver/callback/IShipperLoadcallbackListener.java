package com.dimits.mahalladeliveryserver.callback;

import android.widget.Button;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import com.dimits.mahalladeliveryserver.model.OrderModel;
import com.dimits.mahalladeliveryserver.model.ShipperModel;

import java.util.List;

public interface IShipperLoadcallbackListener {
    void onShipperLoadSuccess(List<ShipperModel> shipperModelList);
    void onShipperLoadSuccess(int position, OrderModel orderModel, List<ShipperModel> shipperModels,
                              AlertDialog dialog,
                              Button btn_ok, Button btn_cancle,
                              RadioButton rdi_shipping, RadioButton rdi_shipped,RadioButton rdi_cancelled,RadioButton rdi_delete,RadioButton rdi_restore_placed, RadioButton rdi_preparing);
    void onShipperLoadFailed(String message);
}
