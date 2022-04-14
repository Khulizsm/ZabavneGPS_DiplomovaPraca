package com.example.gps_app;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainEditorActivity extends AppCompatActivity {
    //UID
    Button btn_addNewRoute;
    //SharedReferences
    SharedPreferences sharedPreferences;

    //Variables
    ArrayList<Route> routeArrayList = new ArrayList<>();
    ;
    Route[] arrayRoutes;

    //Gson
    Gson gson = new Gson();
    String json;
    Type type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_editor);

        loadRoutes();

        if (routeArrayList != null) {
            arrayRoutes = routeArrayList.toArray(new Route[0]);
        }

        setItems();

        btn_addNewRoute = findViewById(R.id.btn_MainEditorAddRoute);
        btn_addNewRoute.setOnClickListener(view -> {
            Intent i = new Intent(MainEditorActivity.this, CreateRouteActivity.class);
            startActivity(i);
        });

    }

    public void setItems() {
        if (routeArrayList != null) {
            arrayRoutes = routeArrayList.toArray(new Route[0]);
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rc_MainEditorExistingRoutes);
        EditRouteAdapter adapter = new EditRouteAdapter(arrayRoutes, this);
        recyclerView.setHasFixedSize(true);
        int ori = this.getResources().getConfiguration().orientation;
        if(ori == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        else{
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
            recyclerView.setLayoutManager(layoutManager);
        }
        recyclerView.setAdapter(adapter);
    }


    public void loadRoutes() {
        sharedPreferences = getSharedPreferences(getString(R.string.getSharedReferences), MODE_PRIVATE);
        json = sharedPreferences.getString(getString(R.string.getRoutesSharedReferences), null);
        type = new TypeToken<ArrayList<Route>>() {
        }.getType();
        try {
            routeArrayList = (gson.fromJson(json, type));
        } catch (JsonSyntaxException e) {
            Toast.makeText(this, getString(R.string.errorFindSharedPreferences), Toast.LENGTH_LONG).show();
        }

        if (routeArrayList == null) {
            routeArrayList = new ArrayList<>();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadRoutes();
        setItems();
    }

}