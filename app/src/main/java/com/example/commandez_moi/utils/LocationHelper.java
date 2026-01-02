package com.example.commandez_moi.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private static final String TAG = "LocationHelper";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationListener listener;

    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude, String address);

        void onLocationError(String error);
    }

    public LocationHelper(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void getCurrentLocation(LocationListener listener) {
        this.listener = listener;

        if (!hasLocationPermission()) {
            listener.onLocationError("Permission de localisation non accordée");
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            String address = getAddressFromLocation(location.getLatitude(), location.getLongitude());
                            listener.onLocationReceived(location.getLatitude(), location.getLongitude(), address);
                        } else {
                            // Si la dernière localisation n'est pas disponible, demander une mise à jour
                            requestLocationUpdate(listener);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur de localisation", e);
                        listener.onLocationError("Impossible d'obtenir la localisation: " + e.getMessage());
                    });
        } catch (SecurityException e) {
            listener.onLocationError("Permission de localisation refusée");
        }
    }

    private void requestLocationUpdate(LocationListener listener) {
        if (!hasLocationPermission()) {
            listener.onLocationError("Permission de localisation non accordée");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    String address = getAddressFromLocation(location.getLatitude(), location.getLongitude());
                    listener.onLocationReceived(location.getLatitude(), location.getLongitude(), address);
                }
                stopLocationUpdates();
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            listener.onLocationError("Permission de localisation refusée");
        }
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                // Ville
                if (address.getLocality() != null) {
                    sb.append(address.getLocality());
                }

                // Région/État
                if (address.getAdminArea() != null) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(address.getAdminArea());
                }

                // Pays
                if (address.getCountryName() != null) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(address.getCountryName());
                }

                return sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur de géocodage", e);
        }
        return "Position: " + String.format(Locale.US, "%.4f, %.4f", latitude, longitude);
    }

    /**
     * Calcule la distance entre deux points en kilomètres
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Formate la distance pour l'affichage
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format(Locale.getDefault(), "%.0f m", distanceKm * 1000);
        } else if (distanceKm < 10) {
            return String.format(Locale.getDefault(), "%.1f km", distanceKm);
        } else {
            return String.format(Locale.getDefault(), "%.0f km", distanceKm);
        }
    }
}
