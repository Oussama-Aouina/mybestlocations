package com.oussamaaouina.mybestlocation;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public interface IBaseGPSListner extends LocationListener, GpsStatus.Listener {
    public void onLocationChanged(Location location);
    public void onProviderEnabled(String provider);
    public void onProviderDisabled(String provider);
    public void onStatusChanged(String provider, int status, Bundle extras);
    public void onGpsStatusChanged(int event);

}
