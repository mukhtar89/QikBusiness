package com.equinox.qikbusiness.Fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikbusiness.Activities.LoginActivity;
import com.equinox.qikbusiness.Adapters.SelectOrderListAdapter;
import com.equinox.qikbusiness.Enums.OrderStatus;
import com.equinox.qikbusiness.Models.Constants;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Order;
import com.equinox.qikbusiness.Models.Place;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.StringManipulation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.equinox.qikbusiness.Models.Constants.BUSINESS_OUTLET;
import static com.equinox.qikbusiness.Models.Constants.ORDERS;
import static com.equinox.qikbusiness.Models.Constants.ORDER_ID;
import static com.equinox.qikbusiness.Models.Constants.PLACE_ID;

/**
 * Created by mukht on 11/11/2016.
 */

public class EmployeeFragment extends Fragment {

    private Place place;
    private NetworkImageView placeImagePoster, placeProfileImage;
    private TextView placePhoneNumber, placeOpenNow, placeRatingValue, placeAddress;
    private RatingBar placeRatingBar;
    private DatabaseReference businessReference;
    private static Hashtable<String,Hashtable<OrderStatus,TextView>> overviewCounterText = new Hashtable<>();
    private static Hashtable<String,Hashtable<OrderStatus,Integer>> statusCounter = new Hashtable<>();
    private static Hashtable<String,Hashtable<OrderStatus,TableLayout>> statusTable = new Hashtable<>();
    private static Hashtable<String,TableRow> orderRowMap = new Hashtable<>();
    private static Hashtable<String,Hashtable<OrderStatus,View>> statusViews = new Hashtable<>();
    private static Hashtable<String,TableLayout> overviewTable = new Hashtable<>();
    private static Hashtable<String,Hashtable<OrderStatus,TableRow>> overviewTableRow = new Hashtable<>();
    private static Hashtable<String,LinearLayout> mainLayout = new Hashtable<>();
    private static Hashtable<String,Hashtable<OrderStatus,SelectOrderListAdapter>> selectOrderListAdapter= new Hashtable<>();
    private static Hashtable<String,Hashtable<OrderStatus,List<Order>>> orderTable = new Hashtable<>();

    public static Fragment newInstance(Place place) {
        EmployeeFragment fragment = new EmployeeFragment();
        fragment.place = place;
        fragment.businessReference = DataHolder.userDatabaseReference.getParent()
                .getParent().child(BUSINESS_OUTLET).child(place.getPlaceId());
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.place_list_fragment, container, false);
        placeImagePoster = (NetworkImageView) rootView.findViewById(R.id.imgPoster);
        placeProfileImage = (NetworkImageView) rootView.findViewById(R.id.profile_img);
        placePhoneNumber = (TextView) rootView.findViewById(R.id.place_phone_number);
        placeOpenNow = (TextView) rootView.findViewById(R.id.place_open_now);
        placeRatingValue = (TextView) rootView.findViewById(R.id.place_rating_value);
        placeAddress = (TextView) rootView.findViewById(R.id.place_address);
        placeRatingBar = (RatingBar) rootView.findViewById(R.id.place_rating_bar);
        placeImagePoster.setImageUrl(place.getPhotos().get(0).returnApiUrl(Constants.PLACES_API_KEY)
                , DataHolder.getInstance().getImageLoader());
        placeProfileImage.setImageUrl(place.getPhotos().get(0).returnApiUrl(Constants.PLACES_API_KEY)
                , DataHolder.getInstance().getImageLoader());
        placePhoneNumber.setText(place.getPhoneNumber());
        placeOpenNow.setText(place.getOpenNow() ? "OPEN" : "CLOSED");
        placeOpenNow.getBackground().setColorFilter(getResources()
                .getColor(place.getOpenNow() ? R.color.green : R.color.red), PorterDuff.Mode.SRC_ATOP);
        placeRatingValue.setText(place.getTotalRating().toString());
        placeAddress.setText(place.getAddress().getFullAddress());
        placeRatingBar.setRating((float) (double) place.getTotalRating());

        mainLayout.put(place.getPlaceId(), (LinearLayout) rootView.findViewById(R.id.business_fragment_main_layout));
        overviewTable.put(place.getPlaceId(), (TableLayout) rootView.findViewById(R.id.overview_table));
        overviewCounterText.put(place.getPlaceId(), new Hashtable<OrderStatus, TextView>());
        statusCounter.put(place.getPlaceId(), new Hashtable<OrderStatus, Integer>());
        statusTable.put(place.getPlaceId(), new Hashtable<OrderStatus, TableLayout>());
        statusViews.put(place.getPlaceId(), new Hashtable<OrderStatus, View>());
        overviewTableRow.put(place.getPlaceId(), new Hashtable<OrderStatus, TableRow>());
        selectOrderListAdapter.put(place.getPlaceId(), new Hashtable<OrderStatus, SelectOrderListAdapter>());
        orderTable.put(place.getPlaceId(), new Hashtable<OrderStatus, List<Order>>());
        DataHolder.orderList.put(place.getPlaceId(), new Hashtable<String, Order>());
        businessReference.child(ORDERS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Order order = fetchOrderParsing(dataSnapshot);
                if (order != null) {
                    createStatusEntry(order.getOrderStatus());
                    addOrderItem(order);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Order order = fetchOrderParsing(dataSnapshot);
                if (order != null) {
                    removeOrderItem(order);
                    removeStatusEntry(order);
                    createStatusEntry(order.getOrderStatus());
                    addOrderItem(order);
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        setRetainInstance(true);
        return rootView;
    }

    private Order fetchOrderParsing(DataSnapshot dataSnapshot) {
        Order order = null;
        try {
            if (dataSnapshot.hasChildren()) {
                order = new Order();
                order.setId(dataSnapshot.getKey());
                order.fromMap((HashMap<String, Object>) dataSnapshot.getValue());
                order.setShop(place);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        return order;
    }

    private void createStatusEntry(final OrderStatus orderStatus) {
        if (!overviewTableRow.get(place.getPlaceId()).containsKey(orderStatus)) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            TableRow overviewRow = (TableRow) inflater.inflate(R.layout.order_status_overview_row, null);
            ImageView icon = (ImageView) overviewRow.findViewById(R.id.overview_item_icon);
            TextView statusName = (TextView) overviewRow.findViewById(R.id.overview_item_text);
            TextView counterText = (TextView) overviewRow.findViewById(R.id.overview_item_count);
            icon.setBackgroundResource(orderStatus.getIcon());
            icon.setColorFilter(getResources().getColor(orderStatus.getColor()));
            statusName.setText(orderStatus.getName());
            statusName.setTextColor(getResources().getColor(orderStatus.getColor()));
            counterText.setTextColor(getResources().getColor(orderStatus.getColor()));
            overviewCounterText.get(place.getPlaceId()).put(orderStatus,counterText);
            statusCounter.get(place.getPlaceId()).put(orderStatus, 0);
            overviewTableRow.get(place.getPlaceId()).put(orderStatus,overviewRow);
            overviewTable.get(place.getPlaceId()).addView(overviewRow);

            View statusView = inflater.inflate(R.layout.order_status_display_card, null);
            CardView statusCard = (CardView) statusView.findViewById(R.id.order_status_card);
            LinearLayout background = (LinearLayout) statusView.findViewById(R.id.order_card_header_background);
            ImageView iconCard = (ImageView) statusView.findViewById(R.id.order_card_header_icon);
            TextView titleCard = (TextView) statusView.findViewById(R.id.order_card_header_title);
            TableLayout listOrders = (TableLayout) statusView.findViewById(R.id.order_card_list);
            background.setBackgroundColor(getResources().getColor(orderStatus.getColor()));
            iconCard.setBackgroundResource(orderStatus.getIcon());
            titleCard.setText(orderStatus.getName() + " Orders");

            final List<Order> selectOrderList = new ArrayList<>();
            orderTable.get(place.getPlaceId()).put(orderStatus, selectOrderList);
            selectOrderListAdapter.get(place.getPlaceId()).put(orderStatus
                    , new SelectOrderListAdapter(selectOrderList,getActivity()));
            statusCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog selectorDialog = new AlertDialog.Builder(getActivity())
                            .setView(R.layout.dialog_select_from_list)
                            .setTitle("Select an Order")
                            .setIcon(R.drawable.logo)
                            .create();
                    selectorDialog.show();
                    ListView selectorList = (ListView) selectorDialog.findViewById(R.id.order_select_list);
                    selectorList.setAdapter(selectOrderListAdapter.get(place.getPlaceId()).get(orderStatus));
                    final Button okayButton = (Button) selectorDialog.findViewById(R.id.dialog_yes_button);
                    final Button cancelButton = (Button) selectorDialog.findViewById(R.id.dialog_no_button);
                    okayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getActivity(), "Please select an Order!", Toast.LENGTH_SHORT).show();
                            selectorDialog.dismiss();
                        }
                    });
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectorDialog.dismiss();
                        }
                    });
                    selectorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            okayButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Order tempOrder = selectOrderList.get(position);
                                    Intent directionsIntent = new Intent(getActivity(), LoginActivity.class);
                                    directionsIntent.putExtra(ORDER_ID, tempOrder.getId());
                                    directionsIntent.putExtra(PLACE_ID, place.getPlaceId());
                                    startActivity(directionsIntent);
                                    selectorDialog.dismiss();
                                }
                            });
                            selectOrderListAdapter.get(place.getPlaceId()).get(orderStatus).selectedPosition(position);
                            selectOrderListAdapter.get(place.getPlaceId()).get(orderStatus).notifyDataSetChanged();
                        }
                    });
                }
            });
            statusTable.get(place.getPlaceId()).put(orderStatus,listOrders);
            statusViews.get(place.getPlaceId()).put(orderStatus,statusView);
            mainLayout.get(place.getPlaceId()).addView(statusView);
        } else  {
            overviewTableRow.get(place.getPlaceId()).get(orderStatus).setVisibility(View.VISIBLE);
            statusViews.get(place.getPlaceId()).get(orderStatus).setVisibility(View.VISIBLE);
        }
    }

    private void removeStatusEntry(Order order) {
        if (DataHolder.orderList.get(place.getPlaceId()).containsKey(order.getId())) {
            OrderStatus orderStatus = DataHolder.orderList.get(place.getPlaceId()).get(order.getId()).getOrderStatus();
            if (overviewTableRow.get(place.getPlaceId()).containsKey(orderStatus)
                    && statusCounter.get(place.getPlaceId()).get(orderStatus) == 0) {
                overviewTableRow.get(place.getPlaceId()).get(orderStatus).setVisibility(View.GONE);
                statusViews.get(place.getPlaceId()).get(orderStatus).setVisibility(View.GONE);
                orderTable.get(place.getPlaceId()).remove(orderStatus);
                selectOrderListAdapter.get(place.getPlaceId()).remove(orderStatus);
            }
            else {
                orderTable.get(place.getPlaceId()).get(orderStatus).remove(order);
                selectOrderListAdapter.get(place.getPlaceId()).get(orderStatus).notifyDataSetChanged();
            }
            DataHolder.orderList.get(place.getPlaceId()).remove(order.getId());
        }
    }

    private void addOrderItem(Order order) {
        DataHolder.orderList.get(order.getShop().getPlaceId()).put(order.getId(), order);
        orderTable.get(place.getPlaceId()).get(order.getOrderStatus()).add(order);
        selectOrderListAdapter.get(place.getPlaceId()).get(order.getOrderStatus()).notifyDataSetChanged();
        overviewCounterText.get(place.getPlaceId()).get(order.getOrderStatus())
                .setText(String.valueOf(statusCounter.get(place.getPlaceId()).get(order.getOrderStatus())+1));
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        TableRow row = (TableRow) inflater.inflate(R.layout.order_status_individual_table_row, null);
        TextView indexRow = (TextView) row.findViewById(R.id.row_index);
        TextView orderFrom = (TextView) row.findViewById(R.id.row_from);
        TextView orderValue = (TextView) row.findViewById(R.id.row_value);
        TextView orderTimestamp = (TextView) row.findViewById(R.id.row_timestamp);
        indexRow.setTextColor(getResources().getColor(order.getOrderStatus().getColor()));
        orderFrom.setTextColor(getResources().getColor(order.getOrderStatus().getColor()));
        orderValue.setTextColor(getResources().getColor(order.getOrderStatus().getColor()));
        orderTimestamp.setTextColor(getResources().getColor(order.getOrderStatus().getColor()));
        indexRow.setText(String.valueOf(statusCounter.get(place.getPlaceId()).get(order.getOrderStatus())+1));
        orderFrom.setText(StringManipulation.CapsFirst(order.getFrom().getName()));
        if (order.getOrderValue()!= null)
            orderValue.setText(DataHolder.currentUser.getLocalCurrency() + " " + String.valueOf(order.getOrderValue()));
        else orderValue.setText("N/A");
        orderTimestamp.setText(StringManipulation.getFormattedTime(order.getTimestamp()));
        statusCounter.get(place.getPlaceId()).put(order.getOrderStatus()
                , statusCounter.get(place.getPlaceId()).get(order.getOrderStatus())+1);
        statusTable.get(place.getPlaceId()).get(order.getOrderStatus())
                .addView(row, statusCounter.get(place.getPlaceId()).get(order.getOrderStatus())-1);
        orderRowMap.put(order.getId(),row);
    }

    private void removeOrderItem(Order order) {
        Order oldOrder = DataHolder.orderList.get(place.getPlaceId()).get(order.getId());
        if (oldOrder != null) {
            if (statusCounter.get(place.getPlaceId()).get(oldOrder.getOrderStatus()) > 0) {
                statusCounter.get(place.getPlaceId()).put(oldOrder.getOrderStatus()
                        , statusCounter.get(place.getPlaceId()).get(oldOrder.getOrderStatus()) - 1);
                overviewCounterText.get(place.getPlaceId()).get(oldOrder.getOrderStatus())
                        .setText(String.valueOf(statusCounter.get(place.getPlaceId()).get(oldOrder.getOrderStatus())));
                statusTable.get(place.getPlaceId()).get(oldOrder.getOrderStatus())
                        .removeView(orderRowMap.get(order.getId()));
                orderRowMap.remove(order.getId());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
