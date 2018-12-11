package com.example.psy.turnbyturn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

public class Main3Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        PermissionsListener {


    //Search activity
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    @SuppressLint("NotChinaMapView")
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private PermissionsManager permissionsManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main3);
        setContentView(R.layout.activity_main3);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Search
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
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
        getMenuInflater().inflate(R.menu.main3, menu);
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
            // Handle the camera action
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

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Main3Activity.this.mapboxMap = mapboxMap;
        initSearchFab();
        addUserLocations();

// Add the symbol layer icon to map for future use
        Bitmap icon = BitmapFactory.decodeResource(
                Main3Activity.this.getResources(), R.drawable.blue_marker);
        mapboxMap.addImage(symbolIconId, icon);

// Create an empty GeoJSON source using the empty feature collection
        setUpSource();

// Set up a new symbol layer for displaying the searched location's feature coordinates
        setupLayer();
        enableLocationComponent();
        initUserLocfab();
        //user location

    }

    private void initUserLocfab() {
        FloatingActionButton userLocFab = findViewById(R.id.fab_my_location);
        userLocFab.setOnClickListener(view -> enableLocationComponent());
    }

    private void initSearchFab() {
        FloatingActionButton searchFab = findViewById(R.id.fab_location_search);
        searchFab.setOnClickListener(view -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken())
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .addInjectedFeature(home)
                            .addInjectedFeature(work)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(Main3Activity.this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        });
    }

    private void addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office - demo")
                .geometry(Point.fromLngLat(-122.399854, 37.7884400))
                .placeName("85 2nd St, San Francisco, CA")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Mapbox DC Office - demo")
                .placeName("740 15th Street NW, Washington DC")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource() {
        GeoJsonSource geoJsonSource = new GeoJsonSource(geojsonSourceLayerId);
        mapboxMap.addSource(geoJsonSource);
    }

    private void setupLayer() {
        SymbolLayer selectedLocationSymbolLayer = new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId);
        selectedLocationSymbolLayer.withProperties(PropertyFactory.iconImage(symbolIconId));
        mapboxMap.addLayer(selectedLocationSymbolLayer);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

// Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

// Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above
            FeatureCollection featureCollection = FeatureCollection.fromFeatures(
                    new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())});

// Retrieve and update the source designated for showing a selected location's symbol layer icon
            GeoJsonSource source = mapboxMap.getSourceAs(geojsonSourceLayerId);
            if (source != null) {
                source.setGeoJson(featureCollection);
            }

// Move map camera to the selected location
            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                    .zoom(14)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 4000);
        }
    }

//    User Location
@SuppressWarnings( {"MissingPermission"})
private void enableLocationComponent() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

        LocationComponentOptions options = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapbox_plugins_green))
                .build();

        // Get an instance of the component
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Activate with options
        locationComponent.activateLocationComponent(this, options);

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    } else {
        permissionsManager = new PermissionsManager(this);
        permissionsManager.requestLocationPermissions(this);
    }
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
