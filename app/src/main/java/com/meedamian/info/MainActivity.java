package com.meedamian.info;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.meedamian.info.databinding.ActivityMainBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private LocalData ld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bootstrap Data-Binding
        ActivityMainBinding amb = DataBindingUtil.setContentView(this, R.layout.activity_main);
        final StateData sd = new StateData(this);
        amb.setState(sd);
        sd.setRootView(amb.getRoot());

        // Bootstrap model
        ld = new LocalData(this, sd, new GeoChecker(this));


        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayShowTitleEnabled(false);


        // Setup Google Map Fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            sd.setGoogleMap(googleMap);
            }
        });

        MainActivityPermissionsDispatcher.initGeoWithPermissionCheck(this);
        MainActivityPermissionsDispatcher.initGeoWithPermissionCheck(this);

        Receiver.setAlarm(this);
    }

    @NeedsPermission(SimChecker.PERMISSION)
    protected void initSim() {
        new SimChecker(this);
    }

    @NeedsPermission(GeoChecker.PERMISSION)
    protected void initGeo() {
        new GeoChecker(this);
    }


    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0
            ? getResources().getDimensionPixelSize(resourceId)
            : 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onPause() {
        ld.save();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.location:
                ld.refreshLocation();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}