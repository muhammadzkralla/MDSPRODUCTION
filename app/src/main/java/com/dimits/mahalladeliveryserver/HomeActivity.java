package com.dimits.mahalladeliveryserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.dimits.mahalladeliveryserver.common.Common;
import com.dimits.mahalladeliveryserver.common.CommonAgr;
import com.dimits.mahalladeliveryserver.eventbus.CategoryClick;
import com.dimits.mahalladeliveryserver.eventbus.ChangeMenuClick;
import com.dimits.mahalladeliveryserver.eventbus.ToastEvent;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        subscribeToTopic(Common.createTopicOrder());
        updatToken();
        SharedPreferences sharedPreferences = getSharedPreferences("com.vuducminh.nicefoodserver", MODE_PRIVATE);
        Switch sw = (Switch) findViewById(R.id.switch1);
        sw.setChecked(sharedPreferences.getBoolean("state",true));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    FirebaseDatabase.getInstance()
                            .getReference(Common.RESTAURANT_REF)
                            .child(Common.currentServerUser.getRestaurant())
                            .child("active").setValue("1");
                    Toast.makeText(HomeActivity.this, "your restaurant is now open", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("com.vuducminh.nicefoodserver", MODE_PRIVATE).edit();
                    editor.putBoolean("state",true);
                    editor.commit();


                }else {
                    FirebaseDatabase.getInstance()
                            .getReference(Common.RESTAURANT_REF)
                            .child(Common.currentServerUser.getRestaurant())
                            .child("active").setValue("0");
                    Toast.makeText(HomeActivity.this, "your restaurant is now closed", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("com.vuducminh.nicefoodserver", MODE_PRIVATE).edit();
                    editor.putBoolean("state",false);
                    editor.commit();
                }
            }




        });
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference(CommonAgr.RESTAURANT_REF);
        reference.child(Common.currentServerUser.getRestaurant())
                .child(Common.ORDER_REF)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Toast.makeText(HomeActivity.this, " you have a new order ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order,R.id.nav_shipper)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView tv_user = (TextView) headerView.findViewById(R.id.tv_user);
        Common.setSpanString("Welcome ", Common.currentServerUser.getName(), tv_user);

        menuClick = R.id.nav_category; // Default
    }


    private void updatToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId().addOnFailureListener(e -> Toast.makeText(HomeActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(instanceIdResult -> {
                    Common.updateToken(HomeActivity.this,instanceIdResult.getToken(),
                            true,false);
                });
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()) {
                        Toast.makeText(this,"Failed: "+task.isSuccessful(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event) {
        if (event.isSuccess()) {
            if (menuClick != R.id.nav_food_list) {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event) {
        if (event.getAction() == Common.ACTION.CREAT) {
            Toast.makeText(this, "Creat Success!", Toast.LENGTH_SHORT).show();
        }
        else  if (event.getAction() == Common.ACTION.UPDATE) {
            Toast.makeText(this, "Update Success!", Toast.LENGTH_SHORT).show();
        }

        else {
            Toast.makeText(this, "Delete Success!", Toast.LENGTH_SHORT).show();
        }
        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event) {
        if (event.isFromFoodList()) {
            //Clear
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);
        } else {
            //Clear
            navController.popBackStack(R.id.nav_food_list, true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_category: {
                if (menuItem.getItemId() != menuClick) {
                    //navController.popBackStack();  // //remove all back stack
                    navController.navigate(R.id.nav_category);
                }
                break;
            }

            case R.id.nav_order: {
                if (menuItem.getItemId() != menuClick)
                {
                    //navController.popBackStack();
                    navController.navigate(R.id.nav_order);
                }
                break;

            } case R.id.nav_best_deals: {
                if (menuItem.getItemId() != menuClick) {
                    //navController.popBackStack();
                    navController.navigate(R.id.nav_best_deals);
                }
                break;

            }
            case R.id.nav_most_popular: {
                if (menuItem.getItemId() != menuClick) {
                   // navController.popBackStack();
                    navController.navigate(R.id.nav_most_popular);
                }
                break;

            }
            case R.id.nav_sign_out: {
                signOut();
                break;
            }
            default:
                menuClick = -1;
                break;
        }
        menuClick = menuItem.getItemId();
        return true;
    }


    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.currentServerUser = null;

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
