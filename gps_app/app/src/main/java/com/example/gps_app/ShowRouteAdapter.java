package com.example.gps_app;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class ShowRouteAdapter extends RecyclerView.Adapter<ShowRouteAdapter.ViewHolder> {
    private Route[] arrayRoutes;
    private Context context;

    public ShowRouteAdapter(Route[] arrayRoutes, Context context) {
        this.arrayRoutes = arrayRoutes;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_route, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Route myListData = arrayRoutes[position];
        holder.textView.setText(String.format("%s : %s", context.getString(R.string.routeName), myListData.getName()));
        holder.textView3.setText(String.format("%s : %s", context.getString(R.string.routeDescription), myListData.getDescription()));
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String date = dateFormat.format(myListData.getDate());
        holder.textView4.setText(String.format("%s : %s", context.getString(R.string.routeAddDate), date));
        String stars = String.valueOf(myListData.getStars());
        String maxStars = String.valueOf(myListData.getTasks().size() * 3);
        holder.textView5.setText(stars + " / " + maxStars);
        holder.textView6.setText(String.valueOf(myListData.getStartCount()));
        holder.constraintLayout.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), PlayerMapScreen.class);
            intent.putExtra(context.getString(R.string.playerMapScreenTaskArrayList), arrayRoutes[position].getTasks());
            intent.putExtra(context.getString(R.string.playerMapScreenRouteID), String.valueOf(position));
            view.getContext().startActivity(intent);
        });
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
        public TextView textView6;
        public RelativeLayout constraintLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.iv_RouteLogo);
            this.textView = itemView.findViewById(R.id.tv_RouteName);
            this.textView3 = itemView.findViewById(R.id.tv_RouteLocation);
            this.textView4 = itemView.findViewById(R.id.tv_RouteAddDate);
            this.textView5 = itemView.findViewById(R.id.tv_RouteStarCount);
            this.textView6 = itemView.findViewById(R.id.tv_RouteStartCount);
            constraintLayout = itemView.findViewById(R.id.constrain_item);
        }
    }

}