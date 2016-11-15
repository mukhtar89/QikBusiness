package com.equinox.qikbusiness.Utils;

import android.app.Dialog;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.equinox.qikbusiness.Enums.QikList;
import com.equinox.qikbusiness.Models.Constants;
import com.equinox.qikbusiness.Models.Photo;
import com.equinox.qikbusiness.Models.Place;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by mukht on 10/30/2016.
 */

public class GetGooglePlaces {

    private String TAG = GetGooglePlaces.class.getSimpleName();
    private String NORMAL = "1", SECONDARY = "2";
    private QikList placeType;
    private Dialog pDialog;
    private Handler placeHandler;
    private List<Place> placeList = new ArrayList<>();
    private HashSet<String> loadedPlaces;

    public GetGooglePlaces(QikList placeType, Dialog pDialog, Handler placeHandler) {
        this.placeType = placeType;
        this.pDialog = pDialog;
        this.placeHandler = placeHandler;
        loadedPlaces = new HashSet<>();
    }

    public void parsePlaces(final Location location) {
        placeList = new ArrayList<>();
        String baseURL = "https://maps.googleapis.com/maps/api/place/search/json?";
        String urlArguments = "location="+location.getLatitude()+","+location.getLongitude()+"&radius=10000"
                 + "&type=" + placeType.getTypeName() + "&sensor=true_or_false&key=" + Constants.PLACES_API_KEY;
        JsonObjectRequest placeReq = new JsonObjectRequest(baseURL+urlArguments, null, placesListener, placesErrorListener);
        baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        urlArguments = "location="+location.getLatitude()+","+location.getLongitude()+"&radius=10000"
                + "&keyword=" + placeType.getKeyword()+ "&sensor=true_or_false&key=" + Constants.PLACES_API_KEY;
        JsonObjectRequest placeReqSecondary = new JsonObjectRequest(baseURL+urlArguments, null, placesListener, placesErrorListener);
        // Adding request to request queue
        AppVolleyController.getInstance().addToRequestQueue(placeReq, NORMAL);
        AppVolleyController.getInstance().addToRequestQueue(placeReqSecondary, SECONDARY);
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

    private Response.Listener<JSONObject> placesListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, response.toString());
            try {
                if (response.has("results")) {
                    JSONArray listObjects = response.getJSONArray("results");
                    // Parsing json
                    for (int i = 0; i < listObjects.length(); i++) {
                        JSONObject obj = listObjects.getJSONObject(i);
                        Place place = new Place();
                        if (!loadedPlaces.contains(obj.getString("place_id"))){
                            JSONObject location = obj.getJSONObject("geometry").getJSONObject("location");
                            place.setLocation(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                            place.setIconURL(obj.getString("icon"));
                            place.setName(obj.getString("name"));
                            if (obj.has("opening_hours")) {
                                JSONObject openingHours = obj.getJSONObject("opening_hours");
                                place.setOpenNow(openingHours.getBoolean("open_now"));
                            }
                            JSONArray photos = obj.getJSONArray("photos");
                            JSONObject photo = photos.getJSONObject(0);
                            Photo tempPhoto = new Photo(photo.getInt("width"), photo.getInt("height"), null, photo.getString("photo_reference"));
                            List<Photo> photoList= new ArrayList<>();
                            photoList.add(tempPhoto);
                            place.setPhoto(photoList);
                            place.setPlaceId(obj.getString("place_id"));
                            place.setVicinity(obj.getString("vicinity"));
                            placeList.add(place);
                            loadedPlaces.add(obj.getString("place_id"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener placesErrorListener = new Response.ErrorListener() {
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

    public List<Place> returnPlaceList() {
        return placeList;
    }
}
