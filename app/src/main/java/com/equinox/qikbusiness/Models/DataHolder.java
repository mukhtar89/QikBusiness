package com.equinox.qikbusiness.Models;

import android.location.Location;

import com.android.volley.toolbox.ImageLoader;
import com.equinox.qikbusiness.Utils.AppVolleyController;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.equinox.qikbusiness.Models.Constants.EMAIL;
import static com.equinox.qikbusiness.Models.Constants.NAME;
import static com.equinox.qikbusiness.Models.Constants.PHOTO;
import static com.equinox.qikbusiness.Models.Constants.ROLES;
import static com.equinox.qikbusiness.Models.Constants.USER_METADATA;

/**
 * Created by mukht on 11/2/2016.
 */
public class DataHolder {

    private static DataHolder ourInstance = new DataHolder();
    public static DataHolder getInstance() {
        return ourInstance;
    }

    private static String TAG = "DataHolder";
    private ImageLoader imageLoader = AppVolleyController.getInstance().getImageLoader();
    //public static HashMap<String, List<GroceryItem>> groceryItemMapping;
    public static Map<String,String> categoryImageMapping = new HashMap<>();
    public static Hashtable<String,Hashtable<String,Order>> orderList = new Hashtable<>();
    public Map<String,Place> placeMap = new Hashtable<>();
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference userDatabaseReference = null, businessDatabaseReference = null;
    public static User currentUser = null;
    public static Location location = null;
    public static List<String> ownershipList = null;
    public static final Boolean lock = true;

    public Map<String,Place> getPlaceMap() {
        return placeMap;
    }
    public ImageLoader getImageLoader() {
        if (imageLoader == null) imageLoader = AppVolleyController.getInstance().getImageLoader();
        return imageLoader;
    }

    public void setRole(final String role) {
        userDatabaseReference.child(ROLES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> userRoles = (List<String>) dataSnapshot.getValue();
                if (userRoles == null)
                    userRoles = new ArrayList<>();
                if (!userRoles.contains(role))
                    userDatabaseReference.child(ROLES).child(String.valueOf(userRoles.size())).setValue(role);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {          }
        });
    }

    public static void generateMetadata() {
        userDatabaseReference.child(USER_METADATA).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,String> userData = new HashMap<>();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (DataHolder.currentUser == null)
                    DataHolder.currentUser = new User();
                if (currentUser != null) {
                    userData.put(NAME, currentUser.getDisplayName());
                    DataHolder.currentUser.setName(currentUser.getDisplayName());
                    userData.put(EMAIL, currentUser.getEmail());
                    DataHolder.currentUser.setEmail(currentUser.getEmail());
                    userData.put(PHOTO, currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
                    DataHolder.currentUser.setPhotoURL(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
                }
                if (location != null) {
                    userData.put("latitude", String.valueOf(location.getLatitude()));
                    userData.put("longitude", String.valueOf(location.getLongitude()));
                    DataHolder.currentUser.setUserLocation(new LatLng(location.getLatitude(),location.getLongitude()));
                }
                //TODO add phone number and Address here too
                userDatabaseReference.child(USER_METADATA).setValue(userData);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
