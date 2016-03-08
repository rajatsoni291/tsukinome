package project_tsukinome.android_love.prateekmishra.com.tsukinome;

/**
 * Created by Prateek M on 02-Mar-16.
 */

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;

import java.io.IOException;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class FetchAddressIntentService extends IntentService {


    ResultReceiver mResultReceiver;
    LatLng latlng;
    boolean isCurrent;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent){
        Geocoder mGeocoder = new Geocoder(this, Locale.US);
        mResultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        isCurrent = intent.getBooleanExtra(Constants.IS_CURRENT_INTENT,false);
        String errorMessage = "";
        latlng = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        List<Address> addresses = null;
        try{
            addresses = mGeocoder.getFromLocation(latlng.latitude,latlng.longitude,1);
        }
        catch(IOException ioexception){
            errorMessage = getString(R.string.service_not_available);
            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
        }
        catch(IllegalArgumentException illegalArgumentException){
            errorMessage = getString(R.string.invalid_lat_lang_used);
            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
        }

        if(addresses == null || addresses.size() == 0){
            if(errorMessage.isEmpty()){
                errorMessage = getString(R.string.no_address_found);
                Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT,errorMessage);
        }
        else{
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList();
            for(int i=0; i<address.getMaxAddressLineIndex();i++)
                addressFragments.add(address.getAddressLine(i));
            deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"),addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        double lat = latlng.latitude, lng = latlng.longitude;
        bundle.putDouble(Constants.LATITUDE,lat);
        bundle.putDouble(Constants.LONGITUDE,lng);
        bundle.putString(Constants.RESULT_DATA_KEY,message);
        bundle.putBoolean(Constants.IS_CURRENT_INTENT_RESULT,isCurrent);
        mResultReceiver.send(resultCode,bundle);
    }
}
