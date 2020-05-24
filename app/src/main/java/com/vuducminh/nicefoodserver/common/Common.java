package com.vuducminh.nicefoodserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefoodserver.model.BestDealsModel;
import com.vuducminh.nicefoodserver.model.CategoryModel;
import com.vuducminh.nicefoodserver.model.FoodModel;
import com.vuducminh.nicefoodserver.model.MostPopularModel;
import com.vuducminh.nicefoodserver.model.ServerUserModel;
import com.vuducminh.nicefoodserver.model.TokenModel;
import com.vuducminh.nicefoodserver.R;

public class Common {
    public static final String SERVER_REF = "Server";
    public static final String CATEGORY_REF = "Category";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String ORDER_REF ="Orders" ;
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static final String URL_FCM = "https://fom.googleapis.com/";
    public static final String SHIPPER = "Shippers";
    public static final String SHIPPER_ORDER_REF = "ShippingOrderModel";
    public static final String RESTAURANT_REF = "Restaurant";
    public static final String BEST_DEALS = "BestDeals";
    public static final String MOST_POPULAR = "MostPopular";
    public static ServerUserModel currentServerUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static BestDealsModel bestDealsSelected;
    public static MostPopularModel mostPopularSelected;

    public static void setSpanString(String welcome, String name, TextView tv_user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        tv_user.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0: {
                return "Placed";
            }
            case 1: {
                return "On My Way";
            }
            case 2: {
                return "Shipped";
            }
            case 3: {
                return "preparing";
            }
            case -1: {
                return "Cancelled";
            }
            default:
                return "Unknown";
        }
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "Dimits_Mahalla_Delivery";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Mahalla Delivery", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Mahalla_elivery");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_restaurant_menu_black_24dp));

        if (pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }
    public static void updateToken(Context context, String newToken,boolean isServer,boolean isShipper) {
        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new TokenModel(Common.currentServerUser.getPhone(), newToken, isServer,isShipper))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/")
                .append(Common.currentServerUser.getRestaurant())
                .append("_")
                .append("new_order")
                .toString();
    }

}
