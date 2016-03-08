package project_tsukinome.android_love.prateekmishra.com.tsukinome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;                          //getSupportActionBar().setBackgroundDrawable(new ColorDrawable( Color.parseColor("#2196F3")));
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;
import android.location.Location;
import android.widget.TextView;
import android.widget.EditText;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng INDORE_LOCATION;

    private final double INDORE_LAT = 22.7253, INDORE_LNG = 75.8656;
    private boolean IS_CURRENT;
    private TextView mLongitude, mLatitude, mAddress;
    private EditText mSearch;
    private Button mType, mCurrentLoc;
    private AddressResultReceiver mResultReceiver;
    private String user_address;
    private LatLng mCurrent, mMarkLatLng;
    private boolean isConnected;
    private int PLACE_PICKER_REQUEST = 1, DESTINATION_STATUS = 0, ROTATION_STATUS = 0 ;
    private Marker mCurrentLocationMarker;
    private Location loc_co[], loc1, loc2, loc3;
    private PlaceAutocompleteFragment mAutocompleteFragment;
    private CameraPosition cameraPosition;

    private Bitmap icon, icon1;
    private Circle shape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.marker_avail);
        icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.marker_navail);

        if (savedInstanceState != null) {
            IS_CURRENT = savedInstanceState.getBoolean(Constants.IS_CURRENT);
        } else {
            IS_CURRENT = true;
        }
        loc1 = new Location("");
        loc1.setLatitude(22.73450374949157);
        loc1.setLongitude(75.81579811871052);
        loc2 = new Location("");
        loc2.setLatitude(22.734257295330366);
        loc2.setLongitude(75.81670135259628);
        loc3 = new Location("");
        loc3.setLatitude(22.7265233);
        loc3.setLongitude(75.871641);
        loc_co = new Location[]{loc1, loc2, loc3};


        mType = (Button) findViewById(R.id.type);
        mCurrentLoc = (Button) findViewById(R.id.current);
//        mAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
//        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
//                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
//                .build();
//        mAutocompleteFragment.setFilter(typeFilter);
//
//
//        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                Toast.makeText(MapsActivity.this,place.getName(),Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onError(Status status) {
//                Toast.makeText(MapsActivity.this,"Error : "+ status.toString(),Toast.LENGTH_SHORT ).show();
//            }
//        });


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .enableAutoManage(this, 0, this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .build();
        }
        INDORE_LOCATION = new LatLng(INDORE_LAT, INDORE_LNG);

        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean(Constants.IS_CURRENT, IS_CURRENT);
        super.onSaveInstanceState(bundle);
    }

//    private void displayPlacePicker() {
//        if( mGoogleApiClient == null || !mGoogleApiClient.isConnected() )
//            return;
//
//        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//        try {
//            startActivityForResult( builder.build( this ), PLACE_PICKER_REQUEST );
//        } catch ( GooglePlayServicesRepairableException e ) {
//            Toast.makeText(this,"GooglePlayServicesRepairableException thrown",Toast.LENGTH_LONG ).show();
//        } catch ( GooglePlayServicesNotAvailableException e ) {
//            Toast.makeText(this,"GooglePlayServicesNotAvailableException thrown",Toast.LENGTH_LONG ).show();
//        }
//    }

    private void startIntentService(LatLng latLng, boolean isCurrent) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, latLng);
        intent.putExtra(Constants.IS_CURRENT_INTENT, isCurrent);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            displayPlace(PlacePicker.getPlace(this, data));
            Toast.makeText(this, "PlacePicjer called", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPlace(Place place) {
        if (place == null)
            return;


        String content = "";
        if (!TextUtils.isEmpty(place.getName())) {
            content += "Name: " + place.getName() + "\n";
        }
        if (!TextUtils.isEmpty(place.getAddress())) {
            content += "Address: " + place.getAddress() + "\n";
        }
        if (!TextUtils.isEmpty(place.getPhoneNumber())) {
            content += "Phone: " + place.getPhoneNumber();
        }

        mAddress.setText(content);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mLatitude = (TextView) findViewById(R.id.latitude);
//        mLongitude = (TextView) findViewById(R.id.longitude);
//        mAddress = (TextView) findViewById(R.id.address);

        mSearch = (EditText) findViewById(R.id.search_query);
        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    search();
                    handled = true;
                }
                return handled;
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            RestoreMap mgr = new RestoreMap(this);
            cameraPosition = mgr.getSavedCameraPosition();
            if (cameraPosition != null) {
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.animateCamera(update);
            }
            int mapType = mgr.getSavedMapType();
            mMap.setMapType(mapType);
        }
    }

    @Override
    protected void onStop() {
        RestoreMap mgr = new RestoreMap(this);
        mgr.saveMapState(mMap);
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    LocationRequest mLocationRequest;
    Marker DestinationMarker;
    List<android.location.Address> mAddresses;

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mAddresses = new ArrayList();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

//        mMap.setMyLocationEnabled(true);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
                mCurrent = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if(cameraPosition == null){
                    cameraPosition = new CameraPosition.Builder()
                            .target(mCurrent)
                            .zoom(15)
                            .bearing(0)
                            .tilt(30)
                            .build();
                }
                if(mCurrentLocationMarker != null){
                    mCurrentLocationMarker.remove();
                    mCurrentLocationMarker = null;
                }
                mCurrentLocationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(mCurrent));
                isConnected = true;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                startIntentService(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),true);
                for( Location l : loc_co) {
                    if (mLastLocation.distanceTo(l) < 500.0){
                        mMarkLatLng = new LatLng(l.getLatitude(), l.getLongitude());
                        startIntentService(mMarkLatLng,false);
                    }
                }
        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(INDORE_LOCATION)
                    .zoom(16)
                    .bearing(0)
                    .tilt(30)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            Toast.makeText(this, "This app needs to access your location, please turn on your location.", Toast.LENGTH_LONG).show();
        }

        mType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mMap.getMapType()) {
                    case GoogleMap.MAP_TYPE_NORMAL:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        Toast.makeText(MapsActivity.this, "Satellite", Toast.LENGTH_LONG).show();
                        break;
                    case GoogleMap.MAP_TYPE_SATELLITE:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        Toast.makeText(MapsActivity.this, "Terrain", Toast.LENGTH_LONG).show();
                        break;
                    case GoogleMap.MAP_TYPE_TERRAIN:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        Toast.makeText(MapsActivity.this, "Hybrid", Toast.LENGTH_LONG).show();
                        break;
                    case GoogleMap.MAP_TYPE_HYBRID:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        Toast.makeText(MapsActivity.this, "Normal", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        mCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPosition = new CameraPosition.Builder()
                        .target(mCurrent)
                        .zoom(15)
                        .bearing(0)
                        .tilt(30)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        });


//        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener(){
//            @Override
//            public void onCameraChange(CameraPosition position){
//
//            }
//        });


//            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
//
//                @Override
//                public void onMapClick(LatLng latLng) {
//                    mMarkLatLng = latLng;
//                    if(DestinationMarker == null) {
//                            DestinationMarker = new MarkerOptions()
//                                    .position(new LatLng(mMarkLatLng.latitude, mMarkLatLng.longitude))
//                                    .title("New Marker");
//                            mMap.addMarker(DestinationMarker);
//                            DestinationMarker.draggable(true);
//                    }
//                    else{
//
//                    }
////                    mLongitude.setText(toString().valueOf(mMarkLatLng.longitude));
////                    mLatitude.setText(toString().valueOf(mMarkLatLng.latitude));
////                    startIntentService(mMarkLatLng);
//                }
//            });
    }


    private void search(){
        String location = mSearch.getText().toString();
        if(location != null && !location.equals("")){

            Geocoder geo = new Geocoder(MapsActivity.this);
            try {
                mAddresses =  geo.getFromLocationName(location,1);
                android.location.Address address = mAddresses.get(0);
                LatLng dest = new LatLng(address.getLatitude(),address.getLongitude());
                Location destination = new Location("");
                destination.setLatitude(dest.latitude);
                destination.setLongitude(dest.longitude);
                cameraPosition = new CameraPosition.Builder()
                        .target(dest)
                        .zoom(15)
                        .bearing(0)
                        .tilt(30)
                        .build();
                if(DestinationMarker != null) {
                    DestinationMarker.remove();
                    DestinationMarker =  null;
                    shape.remove();
                    shape = null;
                }
                DestinationMarker = mMap.addMarker(new MarkerOptions().position(dest));
                shape = drawCircle(dest);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                DESTINATION_STATUS = 1;
                startIntentService(dest,false);
                for( Location l : loc_co) {
                    if (destination.distanceTo(l) < 1000.0){
                        mMarkLatLng = new LatLng(l.getLatitude(), l.getLongitude());
                        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(mMarkLatLng).title(user_address));
                        startIntentService(mMarkLatLng,true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Circle drawCircle( LatLng ll){
        CircleOptions options = new CircleOptions();
        options.center(ll)
                .radius(1000)
                .fillColor(0x230000FF)
                .strokeColor(0x800000FF)
                .strokeWidth(3);
        return mMap.addCircle(options);
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Internal Error Occured.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        if(mCurrentLocationMarker != null )
            mCurrentLocationMarker.remove();
        mCurrent = new LatLng(location.getLatitude(),location.getLongitude());
        mCurrentLocationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(mCurrent));
        startIntentService(new LatLng(location.getLatitude(),location.getLongitude()),true);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {


        LatLng newLoc;

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle bundle) {
                user_address = bundle.getString(Constants.RESULT_DATA_KEY);
                IS_CURRENT = bundle.getBoolean(Constants.IS_CURRENT_INTENT_RESULT);
                newLoc = new LatLng(bundle.getDouble(Constants.LATITUDE),bundle.getDouble(Constants.LONGITUDE));
                String unNamed = "Unnamed Rd";
                if (user_address.contains(unNamed)) {
                    user_address = user_address.replace(unNamed, "");
                    user_address = user_address.replaceFirst("\n", "");
                }
                //mAddress.setText(user_address);


                if(IS_CURRENT) {
//                    icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                            R.drawable.marker_avail);
//                    icon1 = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                            R.drawable.marker_navail);
                 mCurrentLocationMarker.setTitle(user_address);
                }else if(IS_CURRENT == false && DESTINATION_STATUS == 0){
//                    icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                            R.drawable.marker_navail);
                    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon1)).position(newLoc).title(user_address));
                }
                else{
                    DestinationMarker.setTitle(user_address);
                    DESTINATION_STATUS = 0;
                }

            }
        }
    }


