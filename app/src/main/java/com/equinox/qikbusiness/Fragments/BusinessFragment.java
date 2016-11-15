package com.equinox.qikbusiness.Fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikbusiness.Models.Constants;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Item;
import com.equinox.qikbusiness.Models.Order;
import com.equinox.qikbusiness.Models.Place;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.StringManipulation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mukht on 11/11/2016.
 */

public class BusinessFragment extends Fragment {

    private Place place;
    private NetworkImageView placeImagePoster, placeProfileImage;
    private TextView placePhoneNumber, placeOpenNow, placeRatingValue, placeAddress;
    private RatingBar placeRatingBar;
    private TextView incomingValue, processingValue, completedValue, cancelledValue;
    private DatabaseReference businessReference;
    private TableLayout incomingTable, processingTable, completedTable, cancelledTable;
    private List<Order> incomingOrders, processingOrders, completedOrders, cancelledOrders;

    public static Fragment newInstance(Place place) {
        BusinessFragment fragment = new BusinessFragment();
        fragment.place = place;
        fragment.businessReference = DataHolder.database.getReference().child(place.getPlaceId());
        fragment.incomingOrders = new ArrayList<>();
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
        placeImagePoster.setImageUrl(place.getPhoto().get(0).returnApiUrl(Constants.PLACES_API_KEY)
                , DataHolder.getInstance().getImageLoader());
        placeProfileImage.setImageUrl(place.getPhoto().get(0).returnApiUrl(Constants.PLACES_API_KEY)
                , DataHolder.getInstance().getImageLoader());
        placePhoneNumber.setText(place.getPhoneNumber());
        placeOpenNow.setText(place.getOpenNow() ? "OPEN" : "CLOSED");
        placeOpenNow.getBackground().setColorFilter(getResources()
                .getColor(place.getOpenNow() ? R.color.green: R.color.red), PorterDuff.Mode.SRC_ATOP);
        placeRatingValue.setText(place.getTotalRating().toString());
        placeAddress.setText(place.getAddress());
        placeRatingBar.setRating((float) (double) place.getTotalRating());

        incomingValue = (TextView) rootView.findViewById(R.id.order_incoming);
        incomingTable = (TableLayout) rootView.findViewById(R.id.order_incoming_list);
        businessReference.child("orders").child(Constants.ORDER_INCOMING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    incomingOrders.clear();
                    incomingValue.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    if (dataSnapshot.hasChildren()) {
                        Iterator<DataSnapshot> iteratorIncoming = dataSnapshot.getChildren().iterator();
                        HashMap<String, Object> iteratorObject;
                        Order tempOrder;
                        while (iteratorIncoming.hasNext()) {
                            tempOrder = new Order();
                            DataSnapshot orderShot = iteratorIncoming.next();
                            iteratorObject = (HashMap<String, Object>) orderShot.getValue();
                            tempOrder.setOrderId(orderShot.getKey());
                            tempOrder.setExchangeItem((Boolean) iteratorObject.get("exchangeItem"));
                            tempOrder.setTimestamp((Long) iteratorObject.get("timestamp"));
                            tempOrder.setOrderFrom((String) iteratorObject.get("orderFrom"));
                            HashMap<String, Object> orderItemMap = (HashMap<String, Object>) iteratorObject.get("orderItems");
                            List<Item> tempListItems = new ArrayList<>();
                            Item tempItem;
                            ArrayList<Object> itemMapList = new ArrayList<>(orderItemMap.values());
                            for (Object entry : itemMapList) {
                                tempItem = new Item();
                                tempListItems.add(tempItem.fromMap((HashMap<String, Object>) entry));
                            }
                            tempOrder.setOrderItems(tempListItems);
                            if (!incomingOrders.contains(tempOrder))
                                incomingOrders.add(tempOrder);
                        }
                        int i = 0;
                        incomingTable.removeAllViews();
                        for (final Order orderItem : incomingOrders) {
                            TableRow row = new TableRow(getContext());
                            TableRow.LayoutParams layoutParams =
                                    new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            row.setLayoutParams(layoutParams);
                            TextView indexIncoming = new TextView(getContext());
                            final TextView orderFrom = new TextView(getContext());
                            TextView orderValue = new TextView(getContext());
                            TextView orderTimestamp = new TextView(getContext());
                            indexIncoming.setText(String.valueOf(i + 1));
                            DataHolder.database.getReference().child(orderItem.getOrderFrom()).child(Constants.USER_METADATA)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            HashMap<String, String> userData = (HashMap<String, String>) dataSnapshot.getValue();
                                            orderItem.setOrderFromName(userData.get("name"));
                                            orderItem.setOrderFromEmail(userData.get("email  "));
                                            orderItem.setOrderFromPhotoURL(userData.get("photo"));
                                            orderItem.setLocationFrom(new LatLng(Double.valueOf(userData.get("latitude")),
                                                    Double.valueOf(userData.get("longitude"))));
                                            orderFrom.setText(orderItem.getOrderFromName());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                            orderValue.setText(String.valueOf(orderItem.getOrderValue()));
                            orderTimestamp.setText(StringManipulation.getDateCurrentTimeZone(orderItem.getTimestamp()));
                            row.addView(indexIncoming);
                            row.addView(orderFrom);
                            row.addView(orderValue);
                            row.addView(orderTimestamp);
                            incomingTable.addView(row, i++);
                        }
                    }
                } catch (Exception e) {
                    e.getMessage();
                    incomingOrders.clear();
                    incomingTable.removeAllViews();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        processingValue = (TextView) rootView.findViewById(R.id.order_processing);
        completedValue = (TextView) rootView.findViewById(R.id.order_complete);
        cancelledValue = (TextView) rootView.findViewById(R.id.order_cancelled);
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
