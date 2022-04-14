package com.example.gps_app;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;


public class EditTaskAdapter extends RecyclerView.Adapter<EditTaskAdapter.ViewHolder> {
    final private ArrayList<Task> taskArrayList;
    final private int routeID;
    final private Context context;

    //Route
    ArrayList<Route> routeArrayList = new ArrayList<>();

    //Gson
    Gson gson = new Gson();
    String json;//Gson
    Type type;

    //SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //FILE LOAD
    String FILE_NAME = "maps.txt";
    FileOutputStream fileOutputStream;

    public EditTaskAdapter(ArrayList<Task> taskList, int ID, Context context) {
        this.routeID = ID;
        this.taskArrayList = taskList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_task, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.TaskName.setText(String.format("%d %s", holder.getAdapterPosition() + 1, context.getString(R.string.editTaskName)));
        holder.TaskLatitude.setText(String.format("%s %.5f", context.getString(R.string.editTaskLat), taskArrayList.get(position).getTargetCoordinates()[0]));
        holder.TaskLongitude.setText(String.format("%s %.5f", context.getString(R.string.editTaskLng), taskArrayList.get(position).getTargetCoordinates()[1]));
        if (taskArrayList.get(position).getAnswerArrayList().size() == 1) {
            holder.QuestionCount.setText(String.format("%d %s", taskArrayList.get(position).getAnswerArrayList().size(), context.getString(R.string.editTaskOneAnswer)));
        } else if ((1 < taskArrayList.get(position).getAnswerArrayList().size()) && (taskArrayList.get(position).getAnswerArrayList().size() < 5)) {
            holder.QuestionCount.setText(String.format("%d %s", taskArrayList.get(position).getAnswerArrayList().size(), context.getString(R.string.editTaskMaxFourAnswers)));
        } else {
            holder.QuestionCount.setText(String.format("%d %s", taskArrayList.get(position).getAnswerArrayList().size(), context.getString(R.string.editTaskMinFiveAnswers)));
        }
        holder.TaskItemLayout.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), EditTaskActivity.class);
            intent.putExtra(context.getString(R.string.get_eName), String.valueOf(position + 1) + context.getString(R.string.editTaskName));
            intent.putExtra(context.getString(R.string.get_eQuestion), taskArrayList.get(position).getTaskQuestion());
            intent.putExtra(context.getString(R.string.get_eTask), taskArrayList.get(position).getAnswerArrayList());
            intent.putExtra(context.getString(R.string.get_eRouteID), String.valueOf(routeID));
            intent.putExtra(context.getString(R.string.get_eTaskID), String.valueOf(taskArrayList.get(position).getTaskID()));
            view.getContext().startActivity(intent);
        });
        holder.imageButton.setOnClickListener(view -> deleteTask(position));
    }

    public void deleteTask(int ID) {
        if (taskArrayList.size() > 2) {
            loadRoutes();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setTitle(context.getString(R.string.alertDialogDeleteButtonPressTitle));
            builder.setMessage(context.getString(R.string.alertDialogDeleteTaskButtonPress));
            builder.setNegativeButton(context.getString(R.string.alertDialogDeleteButtonPressNegativeButton), (dialogInterface, i) -> {
                dialogInterface.cancel();
            });
            builder.setPositiveButton(context.getString(R.string.alertDialogDeleteButtonPressPositiveButton), (dialogInterface, i) -> {
                routeArrayList.get(routeID).getTasks().remove(ID);
                if (routeArrayList.size() > 0) {
                    if (routeArrayList.get(routeID).getTasks().size() > 0) {
                        for (int j = 0; j < routeArrayList.get(routeID).getTasks().size(); j++) {
                            routeArrayList.get(routeID).getTasks().get(j).setTaskID(j);
                        }
                    }
                }
                saveTasks();
            });
            builder.show();
        } else {
            Toast.makeText(context, context.getString(R.string.errorMinimumTaskSize), Toast.LENGTH_LONG).show();
        }
    }

    public void loadRoutes() {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.getSharedReferences), 0);
        json = sharedPreferences.getString(context.getString(R.string.getRoutesSharedReferences), null);
        type = new TypeToken<ArrayList<Route>>() {
        }.getType();
        try {
            routeArrayList = (gson.fromJson(json, type));
        } catch (JsonSyntaxException e) {
            Toast.makeText(context, context.getString(R.string.errorFindSharedPreferences), Toast.LENGTH_LONG).show();
        }

        if (routeArrayList == null) {
            routeArrayList = new ArrayList<>();
        }
    }

    public void saveTasks() {
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
        Intent intent = new Intent(context, EditRouteActivity.class);
        intent.putExtra(context.getString(R.string.routeID), String.valueOf(routeID));
        context.startActivity(intent);
        ((Activity) context).finish();
    }


    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView TaskImage;
        public ImageView QuestionImage;
        public TextView TaskName;
        public TextView TaskLatitude;
        public TextView TaskLongitude;
        public TextView QuestionCount;
        public ImageButton imageButton;
        public RelativeLayout TaskItemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.TaskImage = itemView.findViewById(R.id.iv_RouteLogo);
            this.QuestionImage = itemView.findViewById(R.id.iv_AnswerImage);
            this.TaskName = itemView.findViewById(R.id.tv_EditTaskName);
            this.TaskLatitude = itemView.findViewById(R.id.Lat);
            this.TaskLongitude = itemView.findViewById(R.id.Lon);
            this.QuestionCount = itemView.findViewById(R.id.tv_editRouteAnswersCount);
            this.imageButton = itemView.findViewById(R.id.btn_TaskDelete);
            this.TaskItemLayout = itemView.findViewById(R.id.task_item_layout);
        }
    }

}