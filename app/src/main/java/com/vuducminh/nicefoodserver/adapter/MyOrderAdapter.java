package com.vuducminh.nicefoodserver.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vuducminh.nicefoodserver.callback.IRecyclerClickListener;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.model.CartItem;
import com.vuducminh.nicefoodserver.model.OrderModel;
import com.vuducminh.nicefoodserver.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter  extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>{

    private Context context;
    private List<OrderModel> orderModelList;
    SimpleDateFormat simpleDateFormat;


    public MyOrderAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        this.simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrderModel orderModel = orderModelList.get(position);
        Glide.with(context)
                .load(orderModel.getCartItemList().get(0).getFoodImage())
                .into(holder.img_food_image);
        holder.tv_order_numner.setText(orderModelList.get(position).getKey());
        Common.setSpanStringColor("Order date ",simpleDateFormat.format(orderModel.getCreateDate()),
                holder.tv_time, Color.parseColor("#336699"));
        Common.setSpanStringColor("Order status ",Common.convertStatusToString(orderModel.getOrderStatus()),
                holder.tv_order_status, Color.parseColor("#00579A"));
        Common.setSpanStringColor("Name ",orderModel.getUserName(),
                holder.tv_name, Color.parseColor("#00574B"));
        Common.setSpanStringColor("Num of items: ",orderModel.getCartItemList() == null ? "0" :
                String.valueOf(orderModel.getCartItemList().size()),
                holder.tv_num_item, Color.parseColor("#4B647D"));
        Common.setSpanStringColor("Phone : ",orderModel.getUserPhone(),
                holder.phone, Color.parseColor("#00574B"));
        Common.setSpanStringColor("Comment : ",orderModel.getCommet(),
                holder.comment, Color.parseColor("#00574B"));
        Common.setSpanStringColor("address : ",orderModel.getShippingAddress(),
                holder.address, Color.parseColor("#00574B"));




        holder.setRecyclerClickListener((view, pos) ->

                showDialog(orderModelList.get(pos).getCartItemList()));

    }

    private void showDialog(List<CartItem> cartItemList) {
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout_dialog);
        Button btn_ok = (Button)layout_dialog.findViewById(R.id.btn_ok);
        RecyclerView recycler_order_detail = (RecyclerView)layout_dialog.findViewById(R.id.recycler_order_detail);
        recycler_order_detail.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_order_detail.setLayoutManager(layoutManager);
        recycler_order_detail.addItemDecoration(new DividerItemDecoration(context,layoutManager.getOrientation()));
        MyOrderDetailAdapter myOrderDetailAdapter = new MyOrderDetailAdapter(context,cartItemList);
        recycler_order_detail.setAdapter(myOrderDetailAdapter);


        //show dialog
        AlertDialog dialog =builder.create();
        dialog.show();
        //custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public OrderModel getItemAtPosition(int position) {
        return orderModelList.get(position);
    }

    public void removeItem(int position) {
        orderModelList.remove(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;
        IRecyclerClickListener recyclerClickListener;

        public void setRecyclerClickListener(IRecyclerClickListener recyclerClickListener) {
            this.recyclerClickListener = recyclerClickListener;
        }

        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.tv_order_numner)
        TextView tv_order_numner;
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.tv_order_status)
        TextView tv_order_status;
        @BindView(R.id.tv_num_item)
        TextView tv_num_item;
        @BindView(R.id.phone)
        TextView phone;
        @BindView(R.id.comment)
        TextView comment;
        @BindView(R.id.address)
        TextView address;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            recyclerClickListener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
