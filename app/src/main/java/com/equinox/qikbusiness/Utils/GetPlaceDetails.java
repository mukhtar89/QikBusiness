package com.equinox.qikbusiness.Utils;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.equinox.qikbusiness.Models.Constants;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.GeoAddress;
import com.equinox.qikbusiness.Models.Periods;
import com.equinox.qikbusiness.Models.Photo;
import com.equinox.qikbusiness.Models.Place;
import com.equinox.qikbusiness.Models.RatingsManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mukht on 11/2/2016.
 */

public class GetPlaceDetails {

    private String TAG = GetPlaceDetails.class.getSimpleName();
    private Dialog pDialog;
    private Handler placeHandler;
    private List<Place> placeList;

    public GetPlaceDetails(Dialog pDialog, Handler placeHandler) {
        this.pDialog = pDialog;
        this.placeHandler = placeHandler;
    }

    public synchronized void parseDetail(final Object arguments, final List<String> placeIds) {
        placeList = new ArrayList<>();
        String baseURL = "https://maps.googleapis.com/maps/api/place/details/json?";
        for (String placeId :  placeIds) {
            String urlArguments = "placeid=" + placeId + "&key=" + Constants.PLACES_API_KEY;
            JsonObjectRequest placeDetailsReq = new JsonObjectRequest(baseURL+urlArguments, null, placeJSONListener, placeJSONErrorListener);
            AppVolleyController.getInstance().addToRequestQueue(placeDetailsReq);
        }
        AppVolleyController.getInstance().getRequestQueue().addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        hidePDialog();
                        Message message = new Message();
                        if (arguments != null) message.obj = arguments;
                        if (placeHandler != null) placeHandler.sendMessage(message);
                    }
                },1000);
                AppVolleyController.getInstance().getRequestQueue().removeRequestFinishedListener(this);
            }
        });
    }

    private Response.Listener<JSONObject> placeJSONListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, response.toString());
            try {
                Place place = new Place();
                if (response.has("result")) {
                    JSONObject resultObj = response.getJSONObject("result");
                    JSONObject geometry = resultObj.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");
                    place.setLocation(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                    place.setIconURL(resultObj.getString("icon"));
                    place.setName(resultObj.getString("name"));
                    if (resultObj.has("opening_hours")) {
                        JSONObject openingHours = resultObj.getJSONObject("opening_hours");
                        place.setOpenNow(openingHours.getBoolean("open_now"));
                        Periods tempPeriods = new Periods();
                        Periods.CloseOpen[] tempCloseOpen;
                        JSONArray periodArray = openingHours.getJSONArray("periods");
                        for (int j = 0; j < periodArray.length(); j++) {
                            JSONObject periodObj = periodArray.getJSONObject(j);
                            JSONObject closeObj = periodObj.getJSONObject("close");
                            tempCloseOpen = tempPeriods.getNewCloseOpen();
                            tempCloseOpen[0].setDay(closeObj.getInt("day"));
                            tempCloseOpen[0].setTime(closeObj.getInt("time"));
                            JSONObject openObj = periodObj.getJSONObject("open");
                            tempCloseOpen = tempPeriods.getNewCloseOpen();
                            tempCloseOpen[1].setDay(openObj.getInt("day"));
                            tempCloseOpen[1].setTime(openObj.getInt("time"));
                            tempPeriods.getPeriods().add(tempCloseOpen);
                        }
                        place.setPeriods(tempPeriods);
                    }
                    if (resultObj.has("photos")) {
                        JSONArray photos = resultObj.getJSONArray("photos");
                        List<Photo> tempPhotos = new ArrayList<>();
                        for (int j=0; j<photos.length(); j++) {
                            JSONObject photoObj = photos.getJSONObject(j);
                            Photo tempPhoto = new Photo(photoObj.getInt("width"), photoObj.getInt("height"),
                                    null, photoObj.getString("photo_reference"));
                            tempPhotos.add(tempPhoto);
                        }
                        place.setPhotos(tempPhotos);
                    }
                    place.setPlaceId(resultObj.getString("place_id"));
                    if (resultObj.has("rating")) place.setTotalRating(resultObj.getDouble("rating"));
                    JSONArray ratingsArray = resultObj.getJSONArray("reviews");
                    List<RatingsManager> tempRatingsList = new ArrayList<>();
                    for (int j = 0; j < ratingsArray.length(); j++) {
                        JSONObject review = ratingsArray.getJSONObject(j);
                        RatingsManager rating = new RatingsManager(review.getString("author_name"),
                                review.has("profile_photo_url") ? review.getString("profile_photo_url") : null,
                                review.getInt("rating"), review.has("text") ? review.getString("text") : null, review.getInt("time"));
                        tempRatingsList.add(rating);
                    }
                    place.setIndividualRatings(tempRatingsList);
                    place.setgMapURL(resultObj.getString("url"));
                    place.setVicinity(resultObj.getString("vicinity"));
                    if (resultObj.has("website")) place.setWebURL(resultObj.getString("website"));

                    JSONArray addressObj = resultObj.getJSONArray("address_components");
                    GeoAddress address = new GeoAddress();
                    for (int i=addressObj.length()-1; i>=0; i--) {
                        JSONObject addressElement = addressObj.getJSONObject(i);
                        GeoAddress.GeoElement tempAddressElement = address.new GeoElement();
                        tempAddressElement.setName(addressElement.getString("short_name"));
                        JSONArray addressElementTypes = addressElement.getJSONArray("types");
                        for (int j=0; j<addressElementTypes.length(); j++)
                            if (!addressElementTypes.getString(j).equals("political"))
                                tempAddressElement.getTypes().add(addressElementTypes.getString(j));
                        address.getAddressElements().add(tempAddressElement);
                    }
                    place.setAddress(address);
                    synchronized (DataHolder.lock) {
                        placeList.add(place);
                        place.setPhoneNumber(resultObj.getString("international_phone_number"));
                        if (DataHolder.getInstance().getPlaceMap().containsKey(place.getPlaceId()))
                            DataHolder.getInstance().getPlaceMap().put(place.getPlaceId(),
                                    DataHolder.getInstance().getPlaceMap().get(place.getPlaceId()).mergePlace(place));
                        else DataHolder.getInstance().getPlaceMap().put(place.getPlaceId(), place);
                    }
                    //TODO add place to the type of Place: example, grocery, restaurant, etc
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener placeJSONErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d(TAG, "Error: " + error.getMessage());
            hidePDialog();
        }
    };

    private void hidePDialog() {
        if (pDialog != null)
            pDialog.dismiss();
    }

    public List<RatingsManager> returnRatingsList(String placeId) {
        return DataHolder.getInstance().getPlaceMap().get(placeId).getIndividualRatings();
    }

    public List<Place> returnPlaceDetail() {
        return placeList;
    }
}
