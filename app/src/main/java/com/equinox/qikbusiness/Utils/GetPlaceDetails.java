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

    public void parseDetail(final List<String> placeIds) {
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
                // notifying list adapter about data changes
                // so that it renders the list view with updated data
                hidePDialog();
                placeHandler.sendMessage(new Message());
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
                    JSONObject openingHours = resultObj.getJSONObject("opening_hours");
                    place.setOpenNow(openingHours.getBoolean("open_now"));
                    JSONArray photos = resultObj.getJSONArray("photos");
                    List<Photo> tempPhotos = new ArrayList<>();
                    for (int j=0; j<photos.length(); j++) {
                        JSONObject photoObj = photos.getJSONObject(j);
                        Photo tempPhoto = new Photo(photoObj.getInt("width"), photoObj.getInt("height"),
                                null, photoObj.getString("photo_reference"));
                        tempPhotos.add(tempPhoto);
                    }
                    place.setPhoto(tempPhotos);
                    place.setPlaceId(resultObj.getString("place_id"));
                    place.setTotalRating(resultObj.getDouble("rating"));
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
                    place.setWebURL(resultObj.getString("website"));
                    place.setAddress(resultObj.getString("formatted_address"));
                    place.setPhoneNumber(resultObj.getString("international_phone_number"));
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
                    placeList.add(place);
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

    public List<Place> returnPlaceDetail() {
        return placeList;
    }
}
