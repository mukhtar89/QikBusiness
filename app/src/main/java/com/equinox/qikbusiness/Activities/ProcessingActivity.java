package com.equinox.qikbusiness.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Order;
import com.equinox.qikbusiness.R;
import com.google.firebase.database.DatabaseReference;

import static com.equinox.qikbusiness.Models.Constants.ORDERS;
import static com.equinox.qikbusiness.Models.Constants.ORDER_ID;
import static com.equinox.qikbusiness.Models.Constants.PLACE_ID;

public class ProcessingActivity extends AppCompatActivity {

    private Order currentOrder;
    private DatabaseReference orderBusinessReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentOrder = DataHolder.orderList.get(getIntent().getStringExtra(PLACE_ID)).get(getIntent().getStringExtra(ORDER_ID));
        orderBusinessReference = DataHolder.database.getReference(currentOrder.getShop().getBasePath())
                .child(currentOrder.getShop().getPlaceId()).child(ORDERS).child(currentOrder.getId());
        getSupportActionBar().setTitle("Order ID: " + currentOrder.getId());
    }

}
