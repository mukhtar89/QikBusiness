package com.equinox.qikbusiness.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.equinox.qikbusiness.Models.Constants.ADDRESS;
import static com.equinox.qikbusiness.Models.Constants.BUSINESS;
import static com.equinox.qikbusiness.Models.Constants.BUSINESS_OUTLET;
import static com.equinox.qikbusiness.Models.Constants.GMAP_URL;
import static com.equinox.qikbusiness.Models.Constants.IS_PARTNER;
import static com.equinox.qikbusiness.Models.Constants.LOCATION_LAT;
import static com.equinox.qikbusiness.Models.Constants.LOCATION_LNG;
import static com.equinox.qikbusiness.Models.Constants.NAME;
import static com.equinox.qikbusiness.Models.Constants.PLACE_ID;
import static com.equinox.qikbusiness.Models.Constants.PROFILE_IMAGE;

/**
 * Created by mukht on 10/30/2016.
 */

public class Place {

    private String placeId, name, vicinity;
    private LatLng location;
    private Boolean openNow, isPartner;
    private List<Photo> photos;
    private String iconURL, gMapURL, webURL, profileImageURL;
    private Double totalRating;
    private List<RatingsManager> individualRatings;
    private String phoneNumber;
    private Periods periods;
    private GeoAddress address;

    public Place mergePlace(Place addPlace) {
        if (vicinity == null)
            vicinity = addPlace.getVicinity();
        if (location == null)
            location = addPlace.getLocation();
        if (openNow == null)
            openNow = addPlace.getOpenNow();
        if (isPartner == null)
            isPartner = addPlace.getPartner();
        if (iconURL == null)
            iconURL = addPlace.getIconURL();
        if (gMapURL == null)
            gMapURL = addPlace.getgMapURL();
        if (webURL == null)
            webURL = addPlace.getWebURL();
        if (totalRating == null)
            totalRating = addPlace.getTotalRating();
        if (address == null)
            address = addPlace.getAddress();
        if (phoneNumber == null)
            phoneNumber = addPlace.getPhoneNumber();
        if (periods == null)
            periods = addPlace.getPeriods();
        if (profileImageURL == null)
            profileImageURL = addPlace.getProfileImageURL();
        if (photos == null)
            photos = addPlace.getPhotos();
        return this;
    }

    public String getBasePath() {
        return address.getBasePath(BUSINESS) + BUSINESS_OUTLET;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(PLACE_ID, placeId);
        result.put(NAME, name);
        result.put(LOCATION_LAT, location.latitude);
        result.put(LOCATION_LNG, location.longitude);
        result.put(IS_PARTNER, isPartner);
        if (profileImageURL != null)
            result.put(PROFILE_IMAGE, profileImageURL);
        result.put(GMAP_URL, gMapURL);
        result.put(ADDRESS, address.toMap());
        return result;
    }

    @Exclude
    public Place fromMap(Map<String,Object> entry) {
        placeId = (String) entry.get(PLACE_ID);
        name = (String) entry.get(NAME);
        location = new LatLng((Double) entry.get(LOCATION_LAT),(Double) entry.get(LOCATION_LNG));
        isPartner = (Boolean) entry.get(IS_PARTNER);
        if (entry.containsKey(PROFILE_IMAGE))
            profileImageURL = (String) entry.get(PROFILE_IMAGE);
        gMapURL = (String) entry.get(GMAP_URL);
        address = new GeoAddress().fromMap((Map<String,Object>) entry.get(ADDRESS));
        return this;
    }

    public String getIconURL() {
        return iconURL;
    }
    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }
    public String getPlaceId() {
        return placeId;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVicinity() {
        return vicinity;
    }
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
    public LatLng getLocation() {
        return location;
    }
    public void setLocation(LatLng location) {
        this.location = location;
    }
    public Boolean getOpenNow() {
        return openNow;
    }
    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }
    public List<Photo> getPhotos() {
        return photos;
    }
    public void setPhotos(List<Photo> photos) { this.photos = photos;  }
    public Double getTotalRating() {
        return totalRating;
    }
    public void setTotalRating(Double totalRating) {
        this.totalRating = totalRating;
    }
    public String getgMapURL() {
        return gMapURL;
    }
    public void setgMapURL(String gMapURL) {
        this.gMapURL = gMapURL;
    }
    public String getWebURL() {
        return webURL;
    }
    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public GeoAddress getAddress() {
        return address;
    }
    public void setAddress(GeoAddress address) {
        this.address = address;
    }
    public Periods getPeriods() {
        return periods;
    }
    public void setPeriods(Periods periods) {
        this.periods = periods;
    }
    public Boolean getPartner() {
        return isPartner;
    }
    public void setPartner(Boolean partner) {
        isPartner = partner;
    }
    public String getProfileImageURL() {
        return profileImageURL;
    }
    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }
    public List<RatingsManager> getIndividualRatings() {
        return individualRatings;
    }
    public void setIndividualRatings(List<RatingsManager> individualRatings) {
        this.individualRatings = individualRatings;
    }
}
