package me.rutgersdirect.rudirect.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;

import me.rutgersdirect.rudirect.R;
import me.rutgersdirect.rudirect.adapter.BusStopsPagerAdapter;
import me.rutgersdirect.rudirect.data.constants.AppData;
import me.rutgersdirect.rudirect.data.constants.RUDirectApplication;
import me.rutgersdirect.rudirect.data.model.BusStop;

public class BusStopsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 5001;

    private static boolean isActive; // Whether or not the activity is active
    private static boolean firstMapLoad = true;

    private GoogleApiClient mGoogleApiClient;
    private String busTag;
    private BusStop[] busStops;
    private boolean mResolvingError = false;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stops);

        // Gets the bus tag, stop titles, and stop times
        Intent intent = getIntent();
        busTag = intent.getStringExtra(AppData.BUS_TAG_MESSAGE);
        Parcelable[] busStopParceArray = intent.getParcelableArrayExtra(AppData.BUS_STOPS_MESSAGE);
        busStops = Arrays.copyOf(busStopParceArray, busStopParceArray.length, BusStop[].class);

        // Set the title to the name of the bus
        setTitle(RUDirectApplication.getBusData().getBusTagsToBusTitles().get(busTag));

        setupToolbar();
        setupViewPagerAndTabLayout();

        mGoogleApiClient = buildGoogleApiClient();
    }

    // Setup toolbar
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_toolbar_back, getTheme()));
        }
    }

    // Setup viewpager and tab layout
    private void setupViewPagerAndTabLayout() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.bus_stop_viewpager);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Setup tabs
        for (int i = 0; i < BusStopsPagerAdapter.NUM_OF_ITEMS; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(BusStopsPagerAdapter.TITLES[i]);
            tabLayout.addTab(tab);
        }

        // Setup on tab selected listener
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                viewPager.setCurrentItem(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* Do nothing */ }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* Do nothing */ }
        });

        if (firstMapLoad) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firstMapLoad = false;
                    viewPager.setAdapter(new BusStopsPagerAdapter(getFragmentManager()));
                }
            }, 100);
        } else {
            viewPager.setAdapter(new BusStopsPagerAdapter(getFragmentManager()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.abc_shrink_fade_out_from_bottom);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        isActive = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        isActive = true;
        super.onResume();
    }

    public String getBusTag() {
        return busTag;
    }

    public BusStop[] getBusStops() {
        return busStops;
    }

    public static boolean isActive() {
        return isActive;
    }

    public static void setIsActive(boolean isActive) {
        BusStopsActivity.isActive = isActive;
    }

    // Build Google Api Client for displaying maps
    private synchronized GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) { /* Do nothing */ }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    // Connection to Google Play Services failed
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mResolvingError && result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }
    }
}