package com.example.gps_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class CreateRouteActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //UI
    EditText routeName, routeDescription;
    Button btn_addNewRoute, btn_deleteLastMarker;

    //MAP
    private GoogleMap MyGoogleMap;
    public static final int PERMISSION_FINE_LOCATION = 99;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;
    MarkerOptions markerSettings;
    CircleOptions circleSettings;
    Marker yourMarker = null, targetMarker = null;
    Circle targetMarkerCircle = null;
    ArrayList<Marker> markerArrayList = new ArrayList<>();
    ArrayList<Circle> circleArrayList = new ArrayList<>();
    LatLng yourPosition;
    Circle yourCircle;
    int circleRadius = 50;
    int circleColor = 0x4dff0000;
    int circleStrokeWidth = 2;
    int markerWidth = 75;
    int markerHeight = 75;
    Bitmap MarkerImage, ScaledMarkerImage;
    BitmapDescriptor MarkerIcon;
    LatLng mCurrentPlace = null;
    //ROUTE
    ArrayList<Route> routeArrayList = new ArrayList<>();
    int routeID;

    //Gson
    Gson gson = new Gson();
    String json;
    Type type;

    //SharedPreferences
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);
        loadRoutes();

        routeName = findViewById(R.id.et_CreateRouteName);
        routeDescription = findViewById(R.id.et_createRouteDescription);

        btn_deleteLastMarker = findViewById(R.id.btn_createRouteDeleteLastMarker);
        btn_deleteLastMarker.setOnClickListener(view -> {
            deleteLastMarker();
        });

        btn_addNewRoute = findViewById(R.id.btn_createRouteFinished);
        btn_addNewRoute.setOnClickListener(view -> {
            if (!routeName.getText().toString().equals("") && routeName.getText().toString().length() <= 24) {
                if (!routeDescription.getText().toString().equals("") && routeDescription.getText().toString().length() <= 48) {
                    if (markerArrayList.size() >= 2) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setCancelable(true);
                        builder.setTitle(getString(R.string.alertDialogAddRouteTitle));
                        builder.setMessage(getString(R.string.alertDialogAddRouteMessage));
                        builder.setNegativeButton(getString(R.string.alertDialogAddRouteNegativeButton), (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        });
                        builder.setPositiveButton(getString(R.string.alertDialogAddRoutePositiveButton), (dialogInterface, i) -> {
                            addRoute();
                        });
                        builder.show();
                    } else {
                        Toast.makeText(getBaseContext(), getString(R.string.errorAddRouteTasks), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.errorAddRouteDescription), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.errorAddRouteName), Toast.LENGTH_LONG).show();
            }
        });


        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gm_EditMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    public void loadRoutes() {
        sharedPreferences = getSharedPreferences(getString(R.string.getSharedReferences), MODE_PRIVATE);
        json = sharedPreferences.getString(getString(R.string.getRoutesSharedReferences), null);
        type = new TypeToken<ArrayList<Route>>() {
        }.getType();
        try {
            routeArrayList = (gson.fromJson(json, type));
        } catch (JsonSyntaxException e) {
            Toast.makeText(this, R.string.errorFindSharedPreferences, Toast.LENGTH_LONG).show();
        }

        if (routeArrayList == null) {
            routeArrayList = new ArrayList<>();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callGPS();
            } else {
                Toast.makeText(this, R.string.errorGPSPermission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void callGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CreateRouteActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location myLocation) {
                    if (yourMarker == null) {
                        PutYourMarker(myLocation);
                    }
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    public void PutYourMarker(Location location) {
        if (MyGoogleMap != null) {
            MarkerImage = BitmapFactory.decodeResource(getResources(), R.drawable.you);
            ScaledMarkerImage = Bitmap.createScaledBitmap(MarkerImage, markerWidth, markerHeight, false);
            MarkerIcon = BitmapDescriptorFactory.fromBitmap(ScaledMarkerImage);

            yourPosition = new LatLng(0, 0);
            if (location != null) {
                mCurrentPlace = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, getString(R.string.errorPositionNotFound), Toast.LENGTH_LONG).show();
                mCurrentPlace = new LatLng(0, 0);
            }
            markerSettings = new MarkerOptions();
            markerSettings.position(mCurrentPlace);
            markerSettings.icon(MarkerIcon);
            markerSettings.title(getString(R.string.yourPosition));
            yourMarker = MyGoogleMap.addMarker(markerSettings);
            yourPosition = mCurrentPlace;

            circleSettings = new CircleOptions();
            circleSettings.center(yourPosition);
            circleSettings.radius(circleRadius);
            circleSettings.strokeColor(Color.BLACK);
            circleSettings.fillColor(circleColor);
            circleSettings.strokeWidth(circleStrokeWidth);
            yourCircle = MyGoogleMap.addCircle(circleSettings);

            MyGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourPosition, 15.0f));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MyGoogleMap = googleMap;

        MyGoogleMap.setOnMapLongClickListener(point -> {
            LatLng targetLatLng = new LatLng(point.latitude, point.longitude);
            markerSettings = new MarkerOptions();
            markerSettings.position(targetLatLng);
            markerSettings.title(String.valueOf(markerArrayList.size() + 1) + getString(R.string.markerTitle));
            targetMarker = MyGoogleMap.addMarker(markerSettings);
            markerArrayList.add(targetMarker);

            circleSettings = new CircleOptions();
            circleSettings.center(point);
            circleSettings.radius(circleRadius);
            circleSettings.strokeColor(Color.BLACK);
            circleSettings.fillColor(circleColor);
            circleSettings.strokeWidth(circleStrokeWidth);
            targetMarkerCircle = MyGoogleMap.addCircle(circleSettings);
            circleArrayList.add(targetMarkerCircle);
        });
        MyGoogleMap.setOnMarkerClickListener(this);

    }

    public void deleteLastMarker() {
        if (markerArrayList.size() > 0) {
            markerArrayList.get(markerArrayList.size() - 1).remove();
            circleArrayList.get(circleArrayList.size() - 1).remove();
            markerArrayList.remove(markerArrayList.size() - 1);
            circleArrayList.remove(circleArrayList.size() - 1);
        } else {
            Toast.makeText(this, getString(R.string.errorDeleteMarker), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    public void addRoute() {

        if (routeArrayList.size() == 0) {
            routeID = 0;
        } else {
            routeID = (routeArrayList.get(routeArrayList.size() - 1).getID()) + 1;
        }

        Double[][] coords = new Double[markerArrayList.size()][2];
        for (int i = 0; i < markerArrayList.size(); i++) {
            coords[i][0] = markerArrayList.get(i).getPosition().latitude;
            coords[i][1] = markerArrayList.get(i).getPosition().longitude;
        }
        Intent newRoute = new Intent(this, CreateTaskActivity.class);
        newRoute.putExtra(getString(R.string.newRouteName), routeName.getText().toString());
        newRoute.putExtra(getString(R.string.newRouteDescription), routeDescription.getText().toString());
        newRoute.putExtra(getString(R.string.newRouteTaskCoordinates), coords);
        newRoute.putExtra(getString(R.string.newRouteTaskCount), String.valueOf(coords.length));
        startActivity(newRoute);
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.alertDialogBackPressTitle));
        builder.setMessage(getString(R.string.alertDialogMessageBackPressCreateRouteMapScreen));
        builder.setNegativeButton(getString(R.string.alertDialogBackPressPlayerMapScreenNegativeButton), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setPositiveButton(getString(R.string.alertDialogBackPressPlayerMapScreenPositiveButton), (dialogInterface, i) -> {
            super.onBackPressed();
        });
        builder.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        callGPS();
    }
}
