package com.equinox.qikbusiness.Activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.equinox.qikbusiness.Adapters.SearchBusinessAdapter;
import com.equinox.qikbusiness.Enums.QikList;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Place;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.GetGooglePlaces;

import java.util.ArrayList;
import java.util.List;

public class SearchBusinessActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView searchBusinessList;
    private SearchBusinessAdapter searchBusinessAdapter;
    private List<Place> placeList = new ArrayList<>();
    private GetGooglePlaces getGooglePlaces;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_business);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        if (DataHolder.location == null) {
            Toast.makeText(this, "Please turn on your Location!", Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Loading...");
            pDialog.show();

            getGooglePlaces = new GetGooglePlaces(QikList.GROCERY, pDialog, updateDataListView);
            getGooglePlaces.parsePlaces(DataHolder.location);

            searchBusinessList = (RecyclerView) findViewById(R.id.business_search_result);
            searchBusinessAdapter = new SearchBusinessAdapter(this, placeList);
            searchBusinessList.setLayoutManager(new LinearLayoutManager(this));
            searchBusinessList.setHasFixedSize(true);
            searchBusinessList.setAdapter(searchBusinessAdapter);
            /*searchBusinessList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*if(position>0 && position <= friendList.size()) {
                    handelListItemClick((User)friendListAdapter.getItem(position - 1));
                }
                }
            });*/
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchBusinessAdapter.getFilter().filter(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.business_search_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    private Handler updateDataListView = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            placeList.addAll(getGooglePlaces.returnPlaceList());
            searchBusinessAdapter.notifyDataSetChanged();
            return false;
        }
    });
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchBusinessAdapter.getFilter().filter(newText);
        return false;
    }
}
