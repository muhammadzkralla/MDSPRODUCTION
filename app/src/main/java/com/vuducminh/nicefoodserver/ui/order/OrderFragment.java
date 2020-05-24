package com.vuducminh.nicefoodserver.ui.order;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.vuducminh.nicefoodserver.HomeActivity;
import com.vuducminh.nicefoodserver.adapter.MyOrderAdapter;
import com.vuducminh.nicefoodserver.adapter.MyShipperSelectionAdapter;
import com.vuducminh.nicefoodserver.callback.IShipperLoadcallbackListener;
import com.vuducminh.nicefoodserver.common.BottomSheetOrderFragment;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.common.MySwiperHelper;
import com.vuducminh.nicefoodserver.eventbus.ChangeMenuClick;
import com.vuducminh.nicefoodserver.eventbus.LoadOrderEvent;
import com.vuducminh.nicefoodserver.model.FCMserver.FCMSendData;
import com.vuducminh.nicefoodserver.model.OrderModel;
import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.model.ShipperModel;
import com.vuducminh.nicefoodserver.model.ShippingOrderModel;
import com.vuducminh.nicefoodserver.model.TokenModel;
import com.vuducminh.nicefoodserver.remote.IFCMServer;
import com.vuducminh.nicefoodserver.remote.RetrofitFCMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends Fragment implements IShipperLoadcallbackListener {
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R.id.tv_order_filter)
    TextView tv_order_filter;

    private RecyclerView recycler_shipper;

    private Unbinder unbinder;

    private OrderViewModel orderViewModel;
    private LayoutAnimationController layoutAnimationControllerl;
    private MyOrderAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMServer ifcmServer;
    private MyShipperSelectionAdapter myShipperSelectionAdapter;

    private IShipperLoadcallbackListener shipperLoadcallbackListener;



    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

       /** FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference(CommonAgr.RESTAURANT_REF);
        reference.child(Common.currentServerUser.getRestaurant())
                .child(Common.ORDER_REF)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        LoadOrderEvent loadOrderEvent = new LoadOrderEvent();
                        orderViewModel.loadOrderByStatus(loadOrderEvent.getStatus());


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });**/




        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        //orderViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();


        orderViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getMutableLiveDataOrderModel().observe(this, orderModels -> {
            if (orderModels != null) {
                adapter = new MyOrderAdapter(getContext(), orderModels);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationControllerl);
                tv_order_filter.setText(new StringBuilder("Orders (")
                        .append(orderModels.size())
                        .append(")"));
            }
        });
        return root;
    }

    private void initViews() {

        ifcmServer = RetrofitFCMClient.getInstance().create(IFCMServer.class);

        shipperLoadcallbackListener = this;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_order, width / 6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {


               /** buf.add(new MyButton(getContext(), "Call", 30, 0, Color.parseColor("#560027"),
                        position -> {
                            Dexter.withActivity(getActivity())
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            OrderModel orderModel = adapter.getItemAtPosition(position);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel :")
                                                    .append(orderModel.getUserPhone()).toString()));
                                            startActivity(intent);

                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            Toast.makeText(getContext(), "You must accept " + response.getPermissionName(), Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                        }
                                    }).check();
                        })
                );**/
                buf.add(new MyButton(getContext(), "Edit", 30, 0, Color.parseColor("#336699"),
                        position -> {
                            showEditDialog(adapter.getItemAtPosition(position), position);

                        })
                );
            }
        };

        setHasOptionsMenu(true);
        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        //layoutAnimationControllerl = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }

    private void showEditDialog(OrderModel orderModel, int position) {
        View layout_dialog;
        AlertDialog.Builder builder;
        if (orderModel.getOrderStatus() == 0) {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_preparing, null);

            //recycler_shipper = layout_dialog.findViewById(R.id.recycler_shippers);
            builder = new AlertDialog.Builder(getContext())
                    .setView(layout_dialog);
        } else if (orderModel.getOrderStatus() == -1) {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_cancelled, null);
            builder = new AlertDialog.Builder(getContext())
                    .setView(layout_dialog);
        }
        else if(orderModel.getOrderStatus() == 3){
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipping, null);
            builder = new AlertDialog.Builder(getContext())
                    .setView(layout_dialog);
        }
        else {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipped, null);
            builder = new AlertDialog.Builder(getContext())
                    .setView(layout_dialog);
        }

        //View
        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancle = (Button) layout_dialog.findViewById(R.id.btn_cancle);

        RadioButton rdi_preparing = (RadioButton) layout_dialog.findViewById(R.id.rdi_preparing);
        RadioButton rdi_shipping = (RadioButton) layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_shipped = (RadioButton) layout_dialog.findViewById(R.id.rdi_shipped);
        RadioButton rdi_cancelled = (RadioButton) layout_dialog.findViewById(R.id.rdi_canncelled);
        RadioButton rdi_delete = (RadioButton) layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_placed = (RadioButton) layout_dialog.findViewById(R.id.rdi_restore_placed);

        TextView tv_status = (TextView) layout_dialog.findViewById(R.id.tv_status);

        //Set Data
        tv_status.setText(new StringBuilder("Order Stauts(")
                .append(Common.convertStatusToString(orderModel.getOrderStatus())));
        //Create Dialog
        AlertDialog dialog = builder.create();

        if (orderModel.getOrderStatus() == 0) {
            loadShipperList(position, orderModel, dialog, btn_ok, btn_cancle,
                    rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed,rdi_preparing);
        } else {
            showDialog(position, orderModel, dialog, btn_ok, btn_cancle,
                    rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed,rdi_preparing);
        }


    }

    private void loadShipperList(int position, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancle, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed,RadioButton rdi_preparing) {
        List<ShipperModel> tempList = new ArrayList<>();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference(CommonAgr.SHIPPER);
        Query shipperActive = shipperRef.orderByChild("active").equalTo(true);
        shipperActive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot shipperSnapshot : dataSnapshot.getChildren()) {
                    ShipperModel shipperModel = shipperSnapshot.getValue(ShipperModel.class);
                    shipperModel.setKey(shipperSnapshot.getKey());
                    tempList.add(shipperModel);
                }
                shipperLoadcallbackListener.onShipperLoadSuccess(position, orderModel, tempList,
                        dialog,
                        btn_ok, btn_cancle,
                        rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed,rdi_preparing);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                shipperLoadcallbackListener.onShipperLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void showDialog(int position, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancle, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed,RadioButton rdi_preparing) {
        dialog.show();
        //Custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancle.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btn_ok.setOnClickListener(v -> {
            if (rdi_cancelled != null && rdi_cancelled.isChecked()) {
                dialog.dismiss();
                updateOrder(position, orderModel, -1);
            }
            else if ( rdi_shipping != null&&rdi_shipping.isChecked()) {
                dialog.dismiss();
                updateOrder(position, orderModel, 1);
//                updateOrder(position, orderModel, 1);
            }
            else if (rdi_shipped != null && rdi_shipped.isChecked()) {
                dialog.dismiss();
                updateOrder(position, orderModel, 2);
            }
            else if (rdi_restore_placed != null && rdi_restore_placed.isChecked()) {
                dialog.dismiss();
                updateOrder(position, orderModel, 0);
            }

            else if (rdi_preparing != null && rdi_preparing.isChecked()){
                dialog.dismiss();
                updateOrder(position, orderModel, 3);
            }

            else if (rdi_delete != null && rdi_delete.isChecked()) {
                deleteOrder(position, orderModel);
            }


        });
    }

   /** private void createShippingOrder(ShipperModel shipperModel, OrderModel orderModel, AlertDialog dialog,int position) {
        ShippingOrderModel shippingOrder = new ShippingOrderModel();
        shippingOrder.setShipperPhone(shipperModel.getPhone());
        shippingOrder.setShipperName(shipperModel.getName());
        shippingOrder.setOrderModel(orderModel);
        shippingOrder.setStartTrip(false);
        shippingOrder.setCurrentLat(-1.0);
        shippingOrder.setCurrentLng(-1.0);

        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.SHIPPER_ORDER_REF)
                .push()
                .setValue(shippingOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Order has been sent to shipper", Toast.LENGTH_SHORT).show();
//                        updateOrder(position, orderModel, 1);

                        //First, get token of user
                        FirebaseDatabase.getInstance()
                                .getReference(CommonAgr.TOKEN_REF)
                                .child(shipperModel.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                                            Map<String, String> notiData = new HashMap<>();
                                            notiData.put(CommonAgr.NOTI_TITLE, "You have new order need ship");
                                            notiData.put(CommonAgr.NOTI_CONTENT, new StringBuilder("You have new order need ship to ")
                                                    .append(orderModel.getUserPhone()).toString());


                                            FCMSendData sendDate = new FCMSendData(tokenModel.getToken(), notiData);

                                            compositeDisposable.add(ifcmServer.sendNotification(sendDate)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {
                                                        dialog.dismiss();
                                                        if (fcmResponse.getSuccess() == 1) {
                                                            updateOrder(position, orderModel, 1);
                                                        } else {
                                                            Toast.makeText(getContext(), "Failed to send to shipper ! Order wasn't update!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));
                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

    }**/

    private void updateOrder(int position, OrderModel orderModel, int status) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {
            Map<String, Object> updateDate = new HashMap<>();
            updateDate.put("orderStatus", status);

            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.RESTAURANT_REF)
                    .child(Common.currentServerUser.getRestaurant())
                    .child(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateDate)
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(aVoid -> {

                        //Show dialog
                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
                        dialog.show();

                        //First, get token of user
                        FirebaseDatabase.getInstance()
                                .getReference(CommonAgr.TOKEN_REF)
                                .child(orderModel.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                                            Map<String, String> notiData = new HashMap<>();
                                            notiData.put(CommonAgr.NOTI_TITLE, "Your order was update");
                                            notiData.put(CommonAgr.NOTI_CONTENT, new StringBuilder("Your order ")
                                                    .append(orderModel.getKey())
                                                    .append(" was update to")
                                                    .append(Common.convertStatusToString(status)).toString());

                                            FCMSendData sendDate = new FCMSendData(tokenModel.getToken(), notiData);

                                            compositeDisposable.add(ifcmServer.sendNotification(sendDate)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {
                                                        dialog.dismiss();
                                                        if (fcmResponse.getSuccess() == 1) {
                                                            Toast.makeText(getContext(), "Update order success!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getContext(), "Update order success but failed to send notofication", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));
                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        adapter.removeItem(position);
                        adapter.notifyItemRemoved(position);
                        updateTextCounter();

                    });
        } else {
            Toast.makeText(getContext(), "Order number must not be null or empty", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateTextCounter() {
        tv_order_filter.setText(new StringBuilder("Orders (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    private void deleteOrder(int position, OrderModel orderModel) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {


            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.RESTAURANT_REF)
                    .child(Common.currentServerUser.getRestaurant())
                    .child(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .removeValue()
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(aVoid -> {
                        adapter.removeItem(position);
                        adapter.notifyItemRemoved(position);
                        updateTextCounter();
                        Toast.makeText(getContext(), "Delete order success!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Order number must not be null or empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_filter) {
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(), "OrderFilter");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class)) {
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event) {
        orderViewModel.loadOrderByStatus(event.getStatus());
    }

    @Override
    public void onShipperLoadSuccess(List<ShipperModel> shipperModelList) {

    }

    @Override
    public void onShipperLoadSuccess(int position, OrderModel orderModel, List<ShipperModel> shipperModels, AlertDialog dialog, Button btn_ok, Button btn_cancle, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed, RadioButton rdi_preparing) {
        if (recycler_shipper != null) {
            recycler_shipper.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recycler_shipper.setLayoutManager(layoutManager);
            recycler_shipper.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

            myShipperSelectionAdapter = new MyShipperSelectionAdapter(getContext(), shipperModels);
            recycler_shipper.setAdapter(myShipperSelectionAdapter);
        }
        showDialog(position, orderModel,
                dialog,
                btn_ok, btn_cancle,
                rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed,rdi_preparing);
    }

    @Override
    public void onShipperLoadFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }
}