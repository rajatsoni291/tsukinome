package project_tsukinome.android_love.prateekmishra.com.tsukinome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;                          //getSupportActionBar().setBackgroundDrawable(new ColorDrawable( Color.parseColor("#2196F3")));
import android.location.Geocoder;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.location.Location;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
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
import com.google.android.gms.maps.model.LatLngBounds;
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
    private AutoCompleteTextView mSearch;
    private Button mType, mCurrentLoc, mClear;
    private AddressResultReceiver mResultReceiver;
    private String user_address;
    private LatLng mCurrent, mMarkLatLng;
    private boolean isConnected;
    private int  DESTINATION_STATUS = 0;
    private Marker mCurrentLocationMarker,DestinationMarker;
    private ArrayList<Location> loc_co;
    private ArrayList<Marker> mOldMarkers;
    private CameraPosition cameraPosition;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds INDORE_LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(22.0000, 75.0000), new LatLng(22.9999, 75.9999));

    private Bitmap icon, icon1;
    private Circle shape, mCircle;
    private int distance = 500, mAreasFound = 0, mTrackDistance=0;

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

        mOldMarkers = new ArrayList<>();

        loc_co = new ArrayList<>(12);
       for(int i = 0 ; i < 12 ; i++)
           loc_co.add(new Location(""));

        loc_co.get(0).setLatitude(22.720356);
        loc_co.get(0).setLongitude(75.854155);
        loc_co.get(1).setLatitude(22.717184);
        loc_co.get(1).setLongitude(75.868658);
        loc_co.get(2).setLatitude(22.696028);
        loc_co.get(2).setLongitude(75.845114);
        loc_co.get(3).setLatitude(22.716759);
        loc_co.get(3).setLongitude(75.851084);
        loc_co.get(4).setLatitude(22.718257);
        loc_co.get(4).setLongitude(75.859313);
        loc_co.get(5).setLatitude(22.720089);
        loc_co.get(5).setLongitude(75.862464);
        loc_co.get(6).setLatitude(22.726815);
        loc_co.get(6).setLongitude(75.871641);
        loc_co.get(7).setLatitude(22.716299);
        loc_co.get(7).setLongitude(75.861295);
        loc_co.get(8).setLatitude(22.725266);
        loc_co.get(8).setLongitude(75.873702);
        loc_co.get(9).setLatitude(22.726333);
        loc_co.get(9).setLongitude(75.871569);
        loc_co.get(10).setLatitude(22.725061);
        loc_co.get(10).setLongitude(75.873199);
        loc_co.get(11).setLatitude(22.709768);
        loc_co.get(11).setLongitude(75.834580);


        if (savedInstanceState != null) {
            IS_CURRENT = savedInstanceState.getBoolean(Constants.IS_CURRENT);
        } else {
            IS_CURRENT = true;
        }



        mType = (Button) findViewById(R.id.type);
        mCurrentLoc = (Button) findViewById(R.id.current);
        mClear = (Button)findViewById(R.id.clear);


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


    private void startIntentService(LatLng latLng, boolean isCurrent) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, latLng);
        intent.putExtra(Constants.IS_CURRENT_INTENT, isCurrent);
        startService(intent);
    }

//
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mSearch = (AutoCompleteTextView) findViewById(R.id.search_query);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                INDORE_LAT_LNG_BOUNDS, null);
        mSearch.setAdapter(mPlaceArrayAdapter);
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
        mSearch.setOnItemClickListener(mAutocompleteClickListener);
        mClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mSearch.setText("");
            }
        });

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            LatLng placeFound = place.getLatLng();
            Location location = new Location("");
            location.setLatitude(placeFound.latitude);
            location.setLongitude(placeFound.longitude);
            CharSequence attributions = places.getAttributions();
            CameraPosition camera =  new CameraPosition.Builder()
                    .target(placeFound)
                    .zoom(15)
                    .bearing(0)
                    .tilt(30)
                    .build();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            if(DestinationMarker != null) {
                DestinationMarker.remove();
                shape.remove();
                shape = null;
                DestinationMarker = null;
                for(Marker marker : mOldMarkers)
                    marker.remove();
                mOldMarkers.clear();
            }
            DestinationMarker = mMap.addMarker(new MarkerOptions().position(placeFound).title(place.getName().toString()));
            shape = drawCircle(placeFound);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));
            scanForParkingArea(location);

            StringBuffer buffer = new StringBuffer();

            if(place.getName() != null && !Html.fromHtml(place.getName().toString()).equals(""))
            buffer.append("Name : "+ Html.fromHtml(place.getName().toString()) +" \n");

            if(place.getAddress()!= null && !Html.fromHtml(place.getAddress().toString()).equals(""))
            buffer.append("Address : "+ Html.fromHtml(place.getAddress().toString()) +" \n");

            if(place.getId() != null && !Html.fromHtml(place.getId().toString()).equals("") )
            buffer.append("Id : "+Html.fromHtml(place.getId().toString()) +" \n");

            if(place.getPhoneNumber() != null && !Html.fromHtml(place.getPhoneNumber().toString()).equals(""))
            buffer.append("Phone : "+ Html.fromHtml(place.getPhoneNumber().toString()) +" \n");

            if(place.getWebsiteUri() != null)
            buffer.append("Website : "+ place.getWebsiteUri().toString() +" \n");

            if (attributions != null) {
                buffer.append("Att. : "+Html.fromHtml(attributions.toString())+"\n");
            }
            showMessage("Data",buffer.toString());
        }
    };

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
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

    List<android.location.Address> mAddresses;

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mAddresses = new ArrayList();
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

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
                    mCircle.remove();
                    mCircle = null;
                }
                mCurrentLocationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(mCurrent));
                isConnected = true;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                startIntentService(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),true);
                scanForParkingArea(mLastLocation);
                mCircle = drawCircle(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
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


    private void scanForParkingArea(Location location){
        if(distance > 500)
            mTrackDistance = distance/2;
        for( Location l : loc_co) {
            if (location.distanceTo(l) < distance && location.distanceTo(l) > mTrackDistance){
                mAreasFound++;
                mMarkLatLng = new LatLng(l.getLatitude(), l.getLongitude());
                startIntentService(mMarkLatLng,false);
            }
        }
        if(mAreasFound < 2 && distance < 2000) {
            distance *= 2;
            scanForParkingArea(location);
        }
        else{
            mTrackDistance = 0;
            distance = 500;
        }
    }

    private void search(){
        String location = mSearch.getText().toString();
        if(location != null && !location.equals("")){

            Geocoder geo = new Geocoder(MapsActivity.this);
            try {
                mAddresses =  geo.getFromLocationName(location,1);
                if(mAddresses.size() > 0) {
                    android.location.Address address = mAddresses.get(0);
                    LatLng dest = new LatLng(address.getLatitude(), address.getLongitude());
                    Location destination = new Location("");
                    destination.setLatitude(dest.latitude);
                    destination.setLongitude(dest.longitude);
                    cameraPosition = new CameraPosition.Builder()
                            .target(dest)
                            .zoom(15)
                            .bearing(0)
                            .tilt(30)
                            .build();
                    if (DestinationMarker != null) {
                        DestinationMarker.remove();
                        DestinationMarker = null;
                        shape.remove();
                        shape = null;
                        for(Marker marker : mOldMarkers)
                            marker.remove();
                        mOldMarkers.clear();
                    }
                    DestinationMarker = mMap.addMarker(new MarkerOptions().position(dest));
                    shape = drawCircle(dest);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    DESTINATION_STATUS = 1;
                    startIntentService(dest, false);
                   scanForParkingArea(destination);
                }
                else{
                    Toast.makeText(this,"No results for "+mSearch.getText().toString(),Toast.LENGTH_SHORT).show();
                    return;
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
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Internal Error Occured.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mCurrentLocationMarker != null ) {
            mCurrentLocationMarker.remove();
        }
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

                if(IS_CURRENT) {
                 mCurrentLocationMarker.setTitle(user_address);
                }else if(IS_CURRENT == false && DESTINATION_STATUS == 0){
                    mOldMarkers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(newLoc).title(user_address)));
                }
                else{
                    DestinationMarker.setTitle(user_address);
                    DESTINATION_STATUS = 0;
                }

            }
        }
    }


