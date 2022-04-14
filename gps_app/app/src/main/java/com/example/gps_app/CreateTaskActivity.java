package com.example.gps_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.Date;

public class CreateTaskActivity extends AppCompatActivity {

    //FILE LOAD
    String FILE_NAME;
    FileOutputStream fileOutputStream = null;

    //UI
    EditText question;
    EditText answer;
    TextView answerLabel;
    TextView taskName;
    CheckBox answerCheckBox;
    Button btn_saveTask;
    Button btn_addAnswer;

    //Variables
    ArrayList<Answer> answerArrayList = new ArrayList<>();
    ArrayList<Task> taskArrayList = new ArrayList<>();
    ArrayList<Route> routeArrayList = new ArrayList<>();
    ArrayList<mCoords> targetCoordinates = new ArrayList<>();
    String stringQuestion;
    Boolean addAnswerButton = true;
    Intent newRoute;
    Double[][] routeTargetCoordinates;
    String routeName;
    String routeDescription;
    int taskID = 0;
    Route route;
    int routeID;
    int taskCount;
    //Gson
    Gson gson = new Gson();
    String json;
    Type type;

    //SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //Date
    Date currentTime;

    //Task
    Task newTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tast);
        FILE_NAME = getString(R.string.file_name);
        loadRoutes();

        newRoute = getIntent();
        routeName = newRoute.getStringExtra(getString(R.string.newRouteName));
        routeDescription = newRoute.getStringExtra(getString(R.string.newRouteDescription));
        taskCount = Integer.parseInt(newRoute.getStringExtra(getString(R.string.newRouteTaskCount)));

        routeTargetCoordinates = (Double[][]) newRoute.getSerializableExtra(getString(R.string.newRouteTaskCoordinates));
        for (int i = 0; i < routeTargetCoordinates.length; i++) {
            targetCoordinates.add(new mCoords(routeTargetCoordinates[i][0], routeTargetCoordinates[i][1]));
        }

        taskName = findViewById(R.id.tv_CreateTaskTaskName);
        question = findViewById(R.id.et_CreateTaskQuestion);
        answer = findViewById(R.id.et_CreateTaskAnswer);
        answerLabel = findViewById(R.id.tv_CreateTaskAnswer);
        btn_saveTask = findViewById(R.id.btn_CreateTaskAddTask);
        btn_addAnswer = findViewById(R.id.btn_CreateTaskAddAnswer);
        answerCheckBox = findViewById(R.id.cb_CreateTaskAnswer);

        taskName.setText(String.format("%d%s", taskID + 1, getString(R.string.markerTitle)));
        currentTime = Calendar.getInstance().getTime();
        btn_saveTask.setVisibility(View.INVISIBLE);
        btn_saveTask.setText(R.string.saveTaskText);

        btn_addAnswer.setText(R.string.saveAnswerText);

        btn_saveTask.setOnClickListener(view -> {
            if (!question.getText().toString().equals("") && answerArrayList.size() >= 3) {
                if (taskArrayList.size() < taskCount) {
                    saveNewTask(taskID);
                    taskName.setText(String.format("%d%s", taskID + 1, getString(R.string.markerTitle)));
                    answerLabel.setVisibility(View.VISIBLE);
                    answer.setVisibility(View.VISIBLE);
                    answerCheckBox.setVisibility(View.VISIBLE);
                    answerLabel.setEnabled(true);
                    answer.setEnabled(true);
                    answerCheckBox.setEnabled(true);
                    btn_addAnswer.setText(getString(R.string.saveAnswerText));
                    addAnswerButton = true;
                    answerArrayList = new ArrayList<>();
                    question.setText("");
                    question.setEnabled(true);
                }
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.errorAddTask), Toast.LENGTH_LONG).show();
            }
        });

        btn_addAnswer.setOnClickListener(view -> {
            if (question.length() > 0) {
                question.setEnabled(false);
                if (addAnswerButton) {
                    if (answer.length() > 0) {
                        btn_addAnswer.setText(getString(R.string.nextAnswerText));
                        answerLabel.setVisibility(View.INVISIBLE);
                        answer.setVisibility(View.INVISIBLE);
                        answerCheckBox.setVisibility(View.INVISIBLE);
                        answerLabel.setEnabled(false);
                        answer.setEnabled(false);
                        answerCheckBox.setEnabled(false);
                        addAnswerButton = false;
                        addAnswer();
                    }
                } else {
                    btn_addAnswer.setText(getString(R.string.saveAnswerText));
                    answerLabel.setVisibility(View.VISIBLE);
                    answer.setVisibility(View.VISIBLE);
                    answerCheckBox.setVisibility(View.VISIBLE);
                    answerLabel.setEnabled(true);
                    answer.setEnabled(true);
                    answerCheckBox.setEnabled(true);
                    addAnswerButton = true;
                }
            }
            if (answerArrayList.size() >= 3) {
                btn_saveTask.setVisibility(View.VISIBLE);
            }
            if (answerArrayList.size() == 6) {
                btn_addAnswer.setVisibility(View.INVISIBLE);
            }
        });


    }

    public void makeAutoTask() {
        for (int j = 0; j < 4; j++) {
            answerArrayList.add(new Answer(String.format("%s%d", getString(R.string.autoCreatedAnswer), (j + 1)), false));
        }

        for (int i = 0; i < taskCount; i++) {
            double[] taskCoordinates = new double[2];
            taskCoordinates[0] = targetCoordinates.get(i).getLat();
            taskCoordinates[1] = targetCoordinates.get(i).getLng();
            taskArrayList.add(new Task(i, taskCoordinates, answerArrayList, getString(R.string.autoCreatedQuestion)));
        }
        routeID = routeArrayList.size();
        if (currentTime == null) {
            currentTime = Calendar.getInstance().getTime();
        }
        route = new Route(routeID, routeName, routeDescription, currentTime, taskArrayList, 0, 0);
        routeArrayList.add(route);
        saveRoute();
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


    public void addAnswer() {
        Answer i = new Answer(answer.getText().toString(), answerCheckBox.isChecked());
        answerArrayList.add(i);
        answer.setText("");
        if (answerCheckBox.isChecked()) {
            answerCheckBox.toggle();
        }
    }

    public void saveNewTask(int taskIndex) {
        stringQuestion = question.getText().toString();
        double[] taskCoordinates = new double[2];
        taskCoordinates[0] = targetCoordinates.get(taskIndex).getLat();
        taskCoordinates[1] = targetCoordinates.get(taskIndex).getLng();
        newTask = new Task(taskIndex, taskCoordinates, answerArrayList, stringQuestion);
        taskArrayList.add(newTask);
        btn_saveTask.setVisibility(View.INVISIBLE);
        taskID++;
        if (taskIndex == taskCount) {
            btn_addAnswer.setVisibility(View.INVISIBLE);
        }
        if (taskArrayList.size() == taskCount) {
            routeID = routeArrayList.size();
            if (currentTime == null) {
                currentTime = Calendar.getInstance().getTime();
            }
            route = new Route(routeID, routeName, routeDescription, currentTime, taskArrayList, 0, 0);
            routeArrayList.add(route);
            saveRoute();
        }
    }

    public void saveRoute() {
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
            makeAutoTask();
            super.onBackPressed();
        });
        builder.show();
    }

}