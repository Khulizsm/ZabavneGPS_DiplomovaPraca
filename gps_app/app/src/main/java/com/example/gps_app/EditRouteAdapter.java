package com.example.gps_app;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class EditRouteAdapter extends RecyclerView.Adapter<EditRouteAdapter.ViewHolder> {
    //FILE LOAD
    String FILE_NAME = "maps.txt";
    FileOutputStream fileOutputStream;

    //View
    private final Context context;

    //Route
    ArrayList<Route> routeArrayList = new ArrayList<>();
    private final Route[] arrayRoutes;
    Route route;

    //Gson
    Gson gson = new Gson();
    String json;

    //SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public EditRouteAdapter(Route[] routesArray, Context context) {
        this.arrayRoutes = routesArray;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.edit_route_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        route = arrayRoutes[position];
        holder.textView.setText(String.format("%s : %s", context.getString(R.string.routeName), route.getName()));
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            holder.textView3.setVisibility(View.VISIBLE);
            holder.textView3.setText(String.format("%s : %s", context.getString(R.string.routeDescription), route.getDescription()));
        }
        else{
                holder.textView3.setVisibility(View.GONE);
        }
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String date = dateFormat.format(route.getDate());
        holder.textView4.setText(String.format("%s : %s", context.getString(R.string.routeAddDate), date));
        String stars = String.valueOf(route.getStars());
        String maxStars = String.valueOf(route.getTasks().size() * 3);
        holder.textView5.setText(String.format("%s / %s", stars, maxStars));
        holder.imgBTN.setOnClickListener(view -> deleteRoute(position));

        holder.constraintLayout.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), EditRouteActivity.class);
            intent.putExtra(context.getString(R.string.routeID), String.valueOf(position));
            view.getContext().startActivity(intent);
        });
    }

    public void deleteRoute(int ID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.alertDialogDeleteButtonPressTitle));
        builder.setMessage(context.getString(R.string.alertDialogDeleteButtonPressMessage));
        builder.setNegativeButton(context.getString(R.string.alertDialogDeleteButtonPressNegativeButton), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setPositiveButton(context.getString(R.string.alertDialogDeleteButtonPressPositiveButton), (dialogInterface, i) -> {
            routeArrayList = new ArrayList<Route>(Arrays.asList(arrayRoutes));
            routeArrayList.remove(ID);
            if (routeArrayList.size() > 0) {
                for (int j = 0; j < routeArrayList.size(); j++) {
                    routeArrayList.get(j).setID(j);
                }
            }
            saveRoutes();
        });
        builder.show();
    }

    public void saveRoutes() {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.getSharedReferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        json = gson.toJson(routeArrayList);
        editor.putString(context.getString(R.string.getRoutesSharedReferences), json);
        editor.apply();
        try {
            fileOutputStream = context.openFileOutput(FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(json.getBytes());
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.errorFindSharedPreferences, Toast.LENGTH_LONG).show();
        } catch (IOException er) {
            Toast.makeText(context, R.string.errorWriteSharedPreferences, Toast.LENGTH_LONG).show();
        }
        setListData();
    }

    public void setListData() {

        Intent intent = new Intent(context, MainEditorActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    @Override
    public int getItemCount() {
        return arrayRoutes.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textView3;
        public TextView textView4;
        public TextView textView5;
        public ImageButton imgBTN;
        public RelativeLayout constraintLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.iv_RouteLogo);
            this.textView = (TextView) itemView.findViewById(R.id.tv_RouteName);
            this.textView3 = (TextView) itemView.findViewById(R.id.tv_RouteLocation);
            this.textView4 = (TextView) itemView.findViewById(R.id.tv_RouteAddDate);
            this.textView5 = (TextView) itemView.findViewById(R.id.tv_RouteStarCount);
            this.imgBTN = (ImageButton) itemView.findViewById(R.id.ib_RouteDeleteButton);
            constraintLayout = (RelativeLayout) itemView.findViewById(R.id.edit_route_item_layout);
        }
    }

}