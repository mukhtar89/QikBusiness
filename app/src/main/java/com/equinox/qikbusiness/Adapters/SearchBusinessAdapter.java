package com.equinox.qikbusiness.Adapters;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikbusiness.Models.Constants;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Place;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.StringManipulation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.equinox.qikbusiness.Models.Constants.BUSINESS;
import static com.equinox.qikbusiness.Models.Constants.BUSINESS_OUTLET;
import static com.equinox.qikbusiness.Models.Constants.BUSINESS_OWNER;

/**
 * Created by mukht on 11/11/2016.
 */

public class SearchBusinessAdapter extends RecyclerView.Adapter<SearchBusinessAdapter.PlaceListViewHolder> implements Filterable {

    private List<Place> placeList, fullPlaceList;
    private PlaceFilter placeFilter;
    private Activity activity;

    public SearchBusinessAdapter(Activity activity, List<Place> placeList) {
        this.activity = activity;
        this.placeList = placeList;
        fullPlaceList = placeList;
    }

    @Override
    public PlaceListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View holder = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_list_item, parent, false);
        return new PlaceListViewHolder(holder);
    }

    @Override
    public void onBindViewHolder(PlaceListViewHolder holder, int position) {
        final Place place = placeList.get(position);
        holder.placeVicinity.setText(place.getVicinity());
        holder.placeName.setText(StringManipulation.CapsFirst(place.getName()));
        if (place.getPhotos() != null) {
            holder.placeImage.setImageUrl(place.getPhotos().get(0).returnApiUrl(Constants.PLACES_API_KEY),
                    DataHolder.getInstance().getImageLoader());
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference ownershipListReference = DataHolder.userDatabaseReference.child(BUSINESS_OWNER).getRef();
                ownershipListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> ownershipList = (List<String>) dataSnapshot.getValue();
                        if (ownershipList == null)
                            ownershipList = new ArrayList<>();
                        if (!ownershipList.contains(place.getPlaceId()))
                            ownershipListReference.child(String.valueOf(ownershipList.size())).setValue(place.getPlaceId());
                        else Toast.makeText(activity, "Business is already added!", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
                final DatabaseReference ownersListReference = DataHolder.userDatabaseReference.getParent().getParent()
                        .child(BUSINESS_OUTLET).child(place.getPlaceId()).child(BUSINESS_OWNER).getRef();
                ownersListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> ownersList = (List<String>) dataSnapshot.getValue();
                        if (ownersList == null)
                            ownersList = new ArrayList<>();
                        if (!ownersList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            ownersListReference.child(String.valueOf(ownersList.size()))
                                    .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        else Toast.makeText(activity, "You are already owner of this business!", Toast.LENGTH_LONG).show();
                        activity.finish();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    class PlaceListViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView placeName, placeVicinity;
        NetworkImageView placeImage;

        PlaceListViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.place_list_card_layout);
            placeVicinity = (TextView) itemView.findViewById(R.id.place_list_vicinity);
            placeName = (TextView) itemView.findViewById(R.id.place_list_name);
            placeImage = (NetworkImageView) itemView.findViewById(R.id.place_list_image);
        }
    }

    @Override
    public Filter getFilter() {
        if (placeFilter == null)
            placeFilter = new PlaceFilter();
        return placeFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class PlaceFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint !=null && constraint.length() > 0) {
                List<Place> tempList = new ArrayList<>();
                // search content in friend list
                for (Place place : fullPlaceList) {
                    if (place.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(place);
                    }
                }
                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = fullPlaceList.size();
                filterResults.values = fullPlaceList;
            }
            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            placeList = (ArrayList<Place>) results.values;
            notifyDataSetChanged();
        }
    }
}
