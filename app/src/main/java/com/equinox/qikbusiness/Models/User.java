package com.equinox.qikbusiness.Models;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.HashMap;

import static com.equinox.qikbusiness.Models.Constants.LOCATION_LAT;
import static com.equinox.qikbusiness.Models.Constants.LOCATION_LNG;

/**
 * Created by mukht on 11/16/2016.
 */

public class User {

    private String id, name, email, photoURL, phone, localCurrency, localCurrencySymbol, featuredAddress;
    private LatLng userLocation;
    private GeoAddress address;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhotoURL() {
        return photoURL;
    }
    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public LatLng getUserLocation() {
        return userLocation;
    }
    public void setUserLocation(LatLng userLocation) {
        this.userLocation = userLocation;
    }
    public GeoAddress getAddress() {
        return address;
    }
    public void setAddress(GeoAddress address) {
        this.address = address;
    }
    public String getLocalCurrency() {
        return localCurrency;
    }
    public void setLocalCurrency(String localCurrency) {
        this.localCurrency = localCurrency;
    }
    public String getLocalCurrencySymbol() {
        return localCurrencySymbol;
    }
    public void setLocalCurrencySymbol(String localCurrencySymbol) {
        this.localCurrencySymbol = localCurrencySymbol;
    }
    public String getFeaturedAddress() {
        if (address == null) return featuredAddress;
        return address.getFullAddress();
    }
    public void setFeaturedAddress(String featuredAddress) {
        this.featuredAddress = featuredAddress;
    }

    @Exclude
    public HashMap<String,Object> toMap() {
        HashMap<String,Object> userMap = new HashMap<>();
        userMap.put("id",id);
        userMap.put("name",name);
        userMap.put("email",email);
        userMap.put("photoURL",photoURL);
        userMap.put(LOCATION_LAT,userLocation.latitude);
        userMap.put(LOCATION_LNG,userLocation.longitude);
        userMap.put("featuredAddress",address.getFullAddress());
        return userMap;
    }

    @Exclude
    public User fromMap(HashMap<String,Object> userMap) {
        id = (String) userMap.get("id");
        name = (String) userMap.get("name");
        email = (String) userMap.get("email");
        photoURL = (String) userMap.get("photoURL");
        userLocation = new LatLng((Double) userMap.get(LOCATION_LAT), (Double) userMap.get(LOCATION_LNG));
        featuredAddress = (String) userMap.get("featuredAddress");
        return this;
    }
}
