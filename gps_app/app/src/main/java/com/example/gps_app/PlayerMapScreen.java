package com.example.gps_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class PlayerMapScreen extends FragmentActivity implements OnMapReadyCallback {

    //FILE LOAD
    String FILE_NAME;
    FileOutputStream fileOutputStream = null;

    //UI
    TextView tv_yourLatitude, tv_yourLongitude, tv_targetLatitude, tv_targetLongitude, tv_starCount;

    //MAP
    private GoogleMap MyGoogleMap;
    public static final int PERMISSION_FINE_LOCATION = 99;
    public static final int DEFAULT_UPDATE_INTERVAL = 2;
    public static final int UPDATE_INTERVAL = 2;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    LocationRequest locationRequest;
    MarkerOptions markerSettings;
    CircleOptions circleSettings;
    Marker yourMarker = null, targetMarker = null;
    Circle targetCircle = null;
    int circleRadius = 75;
    int circleColor = 0x4dff0000;
    int circleStrokeWidth = 2;
    int markerWidth = 75;
    int markerHeight = 75;
    int distanceRadius = 50;
    Bitmap MarkerImage, ScaledMarkerImage;
    BitmapDescriptor YourMarkerIcon, TargetMarkerICon;
    ArrayList<Marker> OldMarker = new ArrayList<>();
    ArrayList<Marker> MyMarker = new ArrayList<>();
    ArrayList<Circle> OldCircles = new ArrayList<>();

    //NewActivity
    ActivityResultLauncher<Intent> resultLauncher;

    //ROUTE
    ArrayList<Route> routeArrayList = new ArrayList<>();
    int routeID;

    //TASK
    ArrayList<Task> taskArrayList = new ArrayList<>();

    //Answer
    ArrayList<Answer> AnswersArrayList = new ArrayList<>();

    //Gson
    Gson gson = new Gson();
    String json;
    Type type;

    //SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //Variables
    int starCount = 0;
    int maxStar = 0;
    int targetPoint = 0;
    double targetLat = 0;
    double targetLng = 0;
    String question = "";
    boolean mapping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_map_screen);

        FILE_NAME = getString(R.string.file_name);
        setUI();

        Intent intent = getIntent();
        taskArrayList = (ArrayList<Task>) intent.getSerializableExtra(getString(R.string.playerMapScreenTaskArrayList));
        routeID = Integer.parseInt(intent.getStringExtra(getString(R.string.playerMapScreenRouteID)));

        getTargetData();

        maxStar = taskArrayList.size() * 3;

        getResultLauncher();

        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setFastestInterval(1000 * UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                callGPS();
                updateTargetUI(targetLat, targetLng);
                updatePlayerUI(locationResult.getLastLocation());

            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gm_PlayerMapScreen);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        updateStar();

        callGPS();

        startLocationUpdates();
    }

    public void setUI() {
        tv_yourLatitude = findViewById(R.id.tv_PlayerMapScreenYourLatitudeValue);
        tv_yourLongitude = findViewById(R.id.tv_PlayerMapScreenYourLongitudeValue);
        tv_targetLatitude = findViewById(R.id.tv_PlayerMapScreenTargetLatitudeValue);
        tv_targetLongitude = findViewById(R.id.tv_PlayerMapScreenTargetLongitudeValue);
        tv_starCount = findViewById(R.id.tv_PlayerMapScreenStar);

        MarkerImage = BitmapFactory.decodeResource(getResources(), R.drawable.target);
        ScaledMarkerImage = Bitmap.createScaledBitmap(MarkerImage, markerWidth, markerHeight, false);
        TargetMarkerICon = BitmapDescriptorFactory.fromBitmap(ScaledMarkerImage);

        MarkerImage = BitmapFactory.decodeResource(getResources(), R.drawable.you);
        ScaledMarkerImage = Bitmap.createScaledBitmap(MarkerImage, markerWidth, markerHeight, false);
        YourMarkerIcon = BitmapDescriptorFactory.fromBitmap(ScaledMarkerImage);

    }

    public void getResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String finishedTask = data.getStringExtra(getString(R.string.finishedTask));
                            if (finishedTask.equals(getString(R.string.finishedTaskResult))) {
                                targetPoint++;
                                starCount += data.getIntExtra(getString(R.string.got_stars), 0);
                                if (targetPoint < taskArrayList.size()) {
                                    getTargetData();
                                    mapping = false;
                                    for (Marker marker : OldMarker) { marker.remove(); }
                                    for (Circle circle : OldCircles) { circle.remove(); }
                                } else {
                                    stopLocationUpdates();
                                    for (Marker marker : OldMarker) { marker.remove(); }
                                    for (Circle circle : OldCircles) { circle.remove(); }
                                    stopLocationUpdates();
                                    LoadMaps();
                                    Intent newActivity = new Intent(this, FinishScreenActivity.class);
                                    newActivity.putExtra(getString(R.string.got_stars), String.valueOf(starCount));
                                    startActivity(newActivity);
                                    finish();
                                }
                                updateStar();
                            } else {
                                return;
                            }
                        }
                    }
                });
    }

    public void LoadMaps() {
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
        SaveMaps();
    }

    public void SaveMaps() {
        for (int i = 0; i < routeArrayList.size(); i++) {
            if (routeArrayList.get(i).getID() == routeID) {
                if (routeArrayList.get(i).getStars() < starCount) {
                    routeArrayList.get(i).setStars(starCount);

                }
                routeArrayList.get(i).setStartCount((routeArrayList.get(i).getStartCount()) + 1);
            }
        }

        sharedPreferences = getSharedPreferences(getString(R.string.getSharedReferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        json = gson.toJson(routeArrayList);
        editor.putString(getString(R.string.getRoutesSharedReferences), json);
        editor.apply();

        try {
            fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(json.getBytes());
        } catch (FileNotFoundException e) {
            Toast.makeText(this, R.string.errorFindSharedPreferences, Toast.LENGTH_LONG).show();
        } catch (IOException er) {
            Toast.makeText(this, R.string.errorWriteSharedPreferences, Toast.LENGTH_LONG).show();
        }
    }

    public void getTargetData() {
        targetLat = taskArrayList.get(targetPoint).getTargetCoordinates()[0];
        targetLng = taskArrayList.get(targetPoint).getTargetCoordinates()[1];
        question = taskArrayList.get(targetPoint).getTaskQuestion();
        AnswersArrayList = taskArrayList.get(targetPoint).getAnswerArrayList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callGPS();
                } else {
                    Toast.makeText(this, getString(R.string.errorGPSPermission), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void callGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(PlayerMapScreen.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                currentLocation = location;
                MapMarker(currentLocation.getLatitude(), currentLocation.getLongitude());
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
        ;
    }

    private void updateTargetUI(double lan, double lon) {
        tv_targetLatitude.setText(String.valueOf(lan));
        tv_targetLongitude.setText(String.valueOf(lon));
    }


    private void updatePlayerUI(Location location) {
        if (location != null) {
            tv_yourLatitude.setText(String.valueOf(location.getLatitude()));
            tv_yourLongitude.setText(String.valueOf(location.getLongitude()));
            checkPosition(location);
        }
        return;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MyGoogleMap = googleMap;
    }


    public void MapMarker(double lat, double lon) {
        if (MyGoogleMap != null) {
            LatLng lastLocationPlaced = new LatLng(0, 0);
            if (!mapping) {
                LatLng latLng = new LatLng(targetLat, targetLng);
                markerSettings = new MarkerOptions();
                markerSettings.position(latLng);
                markerSettings.title(getString(R.string.target));
                markerSettings.alpha(0.7f);
                markerSettings.icon(TargetMarkerICon);
                targetMarker = MyGoogleMap.addMarker(markerSettings);

                circleSettings = new CircleOptions();
                circleSettings.center(latLng);
                circleSettings.radius(circleRadius);
                circleSettings.strokeColor(Color.BLACK);
                circleSettings.fillColor(circleColor);
                circleSettings.strokeWidth(circleStrokeWidth);
                targetCircle = MyGoogleMap.addCircle(circleSettings);
                OldCircles.add(targetCircle);
                OldMarker.add(targetMarker);

                mapping = true;
            }
            LatLng mCurrentPlace = new LatLng(lat, lon);
            markerSettings.position(mCurrentPlace);
            markerSettings.icon(YourMarkerIcon);
            markerSettings.title(getString(R.string.you));
            markerSettings.alpha(0.7f);
            if (MyMarker.size() == 0) {
                yourMarker = MyGoogleMap.addMarker(markerSettings);
                MyMarker.add(yourMarker);
            } else {
                MyMarker.get(0).remove();
                MyMarker.clear();
                yourMarker = MyGoogleMap.addMarker(markerSettings);
                MyMarker.add(yourMarker);
            }
            lastLocationPlaced = mCurrentPlace;
            MyGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationPlaced, 15.0f));
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }


    private void checkPosition(Location location) {
        float[] distance = new float[2];

        Location.distanceBetween(location.getLatitude(), location.getLongitude(), targetLat, targetLng, distance);

        if (distance[0] > distanceRadius) {
            Toast.makeText(getBaseContext(), getString(R.string.outside), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.inside), Toast.LENGTH_LONG).show();
            stopLocationUpdates();
            openTaskActivity();
        }
    }

    public void updateStar() {
        tv_starCount.setText(starCount + " / " + maxStar);
    }


    public void openTaskActivity() {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra(getString(R.string.playerMapScreenTaskArrayList), AnswersArrayList);
        intent.putExtra(getString(R.string.playerMapScreenQuestion), question);
        resultLauncher.launch(intent);
    }

    @Override
    public void onBackPressed() {
        stopLocationUpdates();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.alertDialogBackPressTitle));
        builder.setMessage(getString(R.string.alertDialogMessageBackPressPlayerMapScreen));
        builder.setNegativeButton(getString(R.string.alertDialogBackPressPlayerMapScreenNegativeButton), (dialogInterface, i) -> {
            startLocationUpdates();
            dialogInterface.cancel();
        });
        builder.setPositiveButton(getString(R.string.alertDialogBackPressPlayerMapScreenPositiveButton), (dialogInterface, i) -> {
            LoadMaps();
            super.onBackPressed();
        });
        builder.show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }
}
