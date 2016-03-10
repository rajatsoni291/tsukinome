package project_tsukinome.android_love.prateekmishra.com.tsukinome;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Prateek M on 06-Mar-16.
 */
public class RestoreMap {

    private SharedPreferences mapStatePrefs;
    private LatLng mLatLng;

    public RestoreMap(Context context){
        mapStatePrefs = context.getSharedPreferences(Constants.PREF_NAME,Context.MODE_PRIVATE);
    }

    public void saveMapState(GoogleMap map){
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = map.getCameraPosition();
        editor.putFloat(Constants.CAMERA_POSITION_LATITUDE, (float) position.target.latitude);
        editor.putFloat(Constants.CAMERA_POSITION_LONGITUDE, (float) position.target.longitude);
        editor.putFloat(Constants.CAMERA_POSITION_ZOOM, (float) position.zoom);
        editor.putFloat(Constants.CAMERA_POSITION_TILT, (float) position.tilt);
        editor.putFloat(Constants.CAMERA_POSITION_BEARING, (float) position.bearing);
        editor.putInt(Constants.CAMERA_POSITION_MAPTYPE,map.getMapType());
        editor.commit();
    }


    public CameraPosition getSavedCameraPosition(){
        double latitude = mapStatePrefs.getFloat(Constants.CAMERA_POSITION_LATITUDE,0);
        if(latitude == 0)
            return null;
        double longitude = mapStatePrefs.getFloat(Constants.CAMERA_POSITION_LONGITUDE,0);
        LatLng target = new LatLng(latitude,longitude);

        float zoom = mapStatePrefs.getFloat(Constants.CAMERA_POSITION_ZOOM,0);
        float bearing = mapStatePrefs.getFloat(Constants.CAMERA_POSITION_BEARING,0);
        float tilt = mapStatePrefs.getFloat(Constants.CAMERA_POSITION_TILT,0);

        CameraPosition position = new CameraPosition(target,zoom,tilt,bearing);
        return position;
    }

    public int getSavedMapType(){
        int mapType = mapStatePrefs.getInt(Constants.CAMERA_POSITION_MAPTYPE,0);
        if(mapType == 0)
            return GoogleMap.MAP_TYPE_NORMAL;
        return mapType;
    }

}

