package com.example.gps_app;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class EditRouteActivity extends AppCompatActivity {

    //FILE LOAD
    String FILE_NAME;
    FileOutputStream fileOutputStream;

    //UI
    TextView tv_titleNameLabel, tv_titleDescriptionLabel;
    EditText et_name, et_description;
    Button btn_editRoute;

    //Variables
    ArrayList<Route> routeArrayList = new ArrayList<>();
    Route route;
    int routeID;

    //Gson
    Gson gson = new Gson();
    String json;
    Type type;

    //SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_route);
        FILE_NAME = getString(R.string.file_name);

        loadRoutes();

        Intent i = getIntent();
        routeID = Integer.parseInt(i.getStringExtra(getString(R.string.routeID)));
        setUI();

        btn_editRoute.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveEditedRoute();
        }
    });

    }

    public void setUI(){
        route = routeArrayList.get(routeID);

        tv_titleNameLabel = findViewById(R.id.tv_EditRouteNameTitle);
        tv_titleNameLabel.setText(route.getName());

        tv_titleDescriptionLabel = findViewById(R.id.tv_EditRouteTitleDescription);
        tv_titleDescriptionLabel.setText(route.getDescription());

        et_name = findViewById(R.id.et_EditRouteName);
        et_name.setText(route.getName());

        et_description = findViewById(R.id.et_EditRouteDescription);
        et_description.setText(route.getDescription());

        btn_editRoute = findViewById(R.id.btn_editRoute);
        btn_editRoute.setText(R.string.editRoute);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cl_editRoutesList);
        EditTaskAdapter adapter = new EditTaskAdapter(route.getTasks(), route.getID(),this);
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
        type = new TypeToken<ArrayList<Route>>() {}.getType();
        try {
            routeArrayList = (gson.fromJson(json, type));
        } catch (JsonSyntaxException e) {
            Toast.makeText(this, R.string.errorFindSharedPreferences,Toast.LENGTH_LONG).show();
        }
        if (routeArrayList == null) {
            routeArrayList = new ArrayList<>();
        }
    }

    public void saveEditedRoute(){
        if (routeArrayList != null){
            for (int i = 0; i < routeArrayList.size(); i++) {
                if(routeArrayList.get(i).getID() == route.getID()){
                    routeArrayList.get(i).setName(et_name.getText().toString());
                    routeArrayList.get(i).setDescription(et_description.getText().toString());
                }
            }
        }
        sharedPreferences = getSharedPreferences(getString(R.string.getSharedReferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        json = gson.toJson(routeArrayList);
        editor.putString(getString(R.string.getRoutesSharedReferences),json);
        editor.apply();

        try {
            fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(json.getBytes());
        }catch (FileNotFoundException e){
            Toast.makeText(this, R.string.errorFindSharedPreferences,Toast.LENGTH_LONG).show();
        }catch (IOException er){
            Toast.makeText(this, R.string.errorWriteSharedPreferences,Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.alertDialogBackPressTitle));
        builder.setMessage(getString(R.string.alertDialogMessageBackPressEditRouteScreen));
        builder.setNegativeButton(getString(R.string.alertDialogBackPressEditNegativeButton), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setPositiveButton(getString(R.string.alertDialogBackPressEditPositiveButton), (dialogInterface, i) -> {
            super.onBackPressed();
        });
        builder.show();

    }

    @Override
    protected void onStart() {
        loadRoutes();
        setUI();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        loadRoutes();
        setUI();
        super.onRestart();
    }


}