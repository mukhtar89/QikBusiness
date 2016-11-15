package com.equinox.qikbusiness.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikbusiness.Adapters.BusinessPagerAdapter;
import com.equinox.qikbusiness.Models.Constants;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Place;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.FusedLocationService;
import com.equinox.qikbusiness.Utils.GetPlaceDetails;
import com.equinox.qikbusiness.Utils.LocationPermission;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button loginButton;
    private TextView loginName, loginEmail;
    private NetworkImageView loginImage;
    private FrameLayout profileFrame;
    private LocationPermission locationPermission;
    private CardView addMyBusinessButton;
    private ProgressDialog pDialog;
    private BusinessPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationService fusedLocationService;
    private GetPlaceDetails getPlaceDetails;
    private List<Place> placeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Log.d("AUTH", "OnAuthState: signed out");
                    loginButton.setVisibility(View.VISIBLE);
                    profileFrame.setVisibility(View.GONE);
                    loginEmail.setVisibility(View.GONE);
                    loginName.setVisibility(View.GONE);
                } else {
                    Log.d("AUTH", "OnAuthState: Signed in " + user.getUid());
                    DataHolder.userDatabaseReference =
                            DataHolder.database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    DataHolder.getInstance().setRole("business");
                    if (placeList.isEmpty())
                        DataHolder.userDatabaseReference.child(Constants.BUSINESS_OWNER).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<String> ownershipList = (List<String>) dataSnapshot.getValue();
                            if (ownershipList == null) {
                                pDialog.dismiss();
                                addMyBusinessButton.setVisibility(View.VISIBLE);
                            }
                            else {
                                addMyBusinessButton.setVisibility(View.GONE);
                                DataHolder.ownershipList = ownershipList;
                                getPlaceDetails.parseDetail(ownershipList);
                                DataHolder.userDatabaseReference.child(Constants.BUSINESS_OWNER).removeEventListener(this);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                    loginButton.setVisibility(View.GONE);
                    loginEmail.setVisibility(View.VISIBLE);
                    loginName.setVisibility(View.VISIBLE);
                    loginName.setText(user.getDisplayName());
                    loginEmail.setText(user.getEmail());
                    if (user.getPhotoUrl() != null){
                        profileFrame.setVisibility(View.VISIBLE);
                        loginImage.setImageUrl(user.getPhotoUrl().toString(), DataHolder.getInstance().getImageLoader());
                    }
                }
            }
        };

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navigationHeaderView = navigationView.getHeaderView(0);
        profileFrame =(FrameLayout) navigationHeaderView.findViewById(R.id.profile_bundle);
        loginButton = (Button) navigationHeaderView.findViewById(R.id.login_button);
        loginName = (TextView) navigationHeaderView.findViewById(R.id.login_name);
        loginEmail = (TextView) navigationHeaderView.findViewById(R.id.login_email);
        loginImage = (NetworkImageView) navigationHeaderView.findViewById(R.id.login_image);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        loginImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        mSectionsPagerAdapter = new BusinessPagerAdapter(getSupportFragmentManager(), placeList);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setScrollContainer(true);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();
        getPlaceDetails = new GetPlaceDetails(pDialog, placeDetailsFetchHandler);
        addMyBusinessButton = (CardView) findViewById(R.id.add_business_button);
        addMyBusinessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchBusinessActivity.class));
            }
        });

        locationPermission = new LocationPermission(this, this);
        fusedLocationService = new FusedLocationService(this, locationPermission, locationHandler);
        mGoogleApiClient = fusedLocationService.buildGoogleApiClient();
    }

    private Handler placeDetailsFetchHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            for (Place place : getPlaceDetails.returnPlaceDetail()) {
                if (!placeList.contains(place))  placeList.add(place);
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
            if (placeList.size() > 2) mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            return false;
        }
    });

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Handler locationHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (DataHolder.location == null)
                DataHolder.location = fusedLocationService.returnLocation();
            return true;
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Location is Set!", Toast.LENGTH_LONG).show();
                } else {
                    locationPermission = new LocationPermission(this, this);
                    locationPermission.getLocationPermission();
                    Toast.makeText(MainActivity.this, "Location Access is Denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
