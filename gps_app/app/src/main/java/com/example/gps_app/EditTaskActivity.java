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

public class EditTaskActivity extends AppCompatActivity {

    public ArrayList<Answer> taskList = new ArrayList<>(); ;
     String FILE_NAME;

    //UI
    TextView taskName;
    EditText question;

    FileOutputStream fileOutputStream = null;

    CheckBox[] checkBoxes= new CheckBox[6];
    TextView[] textViews = new TextView[6];
    EditText[] editTexts = new EditText[6];
    Button bnt_editTask;
    ArrayList<Answer> answerArrayList = new ArrayList<>();

    ArrayList<Route> routeArrayList = new ArrayList<>();
    int ID, taskID;

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
        setContentView(R.layout.activity_edit_task);
        FILE_NAME = "maps.txt";

        loadRoute();

        Intent i = getIntent();

        taskName = findViewById(R.id.tv_EditTaskName);
        taskName.setText(i.getStringExtra(getString(R.string.get_eName)));

        question = findViewById(R.id.et_EditTaskQuestion);
        question.setText(i.getStringExtra(getString(R.string.get_eQuestion)));


        taskList = (ArrayList<Answer>) i.getSerializableExtra(getString(R.string.get_eTask));
        ID = Integer.parseInt(i.getStringExtra(getString(R.string.get_eRouteID)));
        taskID = Integer.parseInt(i.getStringExtra(getString(R.string.get_eTaskID)));

        bnt_editTask = findViewById(R.id.btn_editTask);

        textViews[0] = findViewById(R.id.tv_EditTaskAnswer1);
        textViews[1] = findViewById(R.id.tv_EditTaskAnswer2);
        textViews[2] = findViewById(R.id.tv_EditTaskAnswer3);
        textViews[3] = findViewById(R.id.tv_EditTaskAnswer4);
        textViews[4] = findViewById(R.id.tv_EditTaskAnswer5);
        textViews[5] = findViewById(R.id.tv_EditTaskAnswer6);

        editTexts[0] = findViewById(R.id.et_EditTaskAnswer1);
        editTexts[1] = findViewById(R.id.et_EditTaskAnswer2);
        editTexts[2] = findViewById(R.id.et_EditTaskAnswer3);
        editTexts[3] = findViewById(R.id.et_EditTaskAnswer4);
        editTexts[4] = findViewById(R.id.et_EditTaskAnswer5);
        editTexts[5] = findViewById(R.id.et_EditTaskAnswer6);

        checkBoxes[0] = findViewById(R.id.cb_EditTaskAnswer1);
        checkBoxes[1] = findViewById(R.id.cb_EditTaskAnswer2);
        checkBoxes[2] = findViewById(R.id.cb_EditTaskAnswer3);
        checkBoxes[3] = findViewById(R.id.cb_EditTaskAnswer4);
        checkBoxes[4] = findViewById(R.id.cb_EditTaskAnswer5);
        checkBoxes[5] = findViewById(R.id.cb_EditTaskAnswer6);

        for (int j = 0; j < 6; j++) {
            textViews[j].setVisibility(View.INVISIBLE);
            editTexts[j].setVisibility(View.INVISIBLE);
            checkBoxes[j].setVisibility(View.INVISIBLE);
        }

        for (int k = 0; k < taskList.size(); k++) {
            textViews[k].setVisibility(View.VISIBLE);
            textViews[k].setText("Odpoveď " + String.valueOf(k+1));

            editTexts[k].setVisibility(View.VISIBLE);
            editTexts[k].setText(taskList.get(k).getAnswer());

            checkBoxes[k].setVisibility(View.VISIBLE);
            checkBoxes[k].setChecked((Boolean)(taskList.get(k).getGoodAnswer()));
        }

        bnt_editTask.setOnClickListener(view -> saveEditedTask());


    }


    public void saveEditedTask(){
        for (int i = 0; i < taskList.size(); i++) {
           String ooo =  editTexts[i].getText().toString();
           Boolean checkbox = checkBoxes[i].isChecked();
           Answer point = new Answer(ooo, checkbox);
           answerArrayList.add(point);
        }
        saveTaskToRout();
    }



    public void loadRoute(){
        sharedPreferences = getSharedPreferences(getString(R.string.getSharedReferences), MODE_PRIVATE);
        json = sharedPreferences.getString(getString(R.string.getRoutesSharedReferences), null);
        type = new TypeToken<ArrayList<Route>>(){}.getType();
        try {
            routeArrayList = (gson.fromJson(json, type));
        } catch (JsonSyntaxException e) {
            Toast.makeText(this, R.string.errorFindSharedPreferences,Toast.LENGTH_LONG).show();
        }
        if(routeArrayList == null){
            routeArrayList = new ArrayList<>();
        }
    }

    public void saveTaskToRout(){
        for (int i = 0; i < routeArrayList.size(); i++) {
            if(routeArrayList.get(i).getID() == ID){
                for (int j = 0; j < routeArrayList.get(i).getTasks().size(); j++){
                    if(routeArrayList.get(i).getTasks().get(j).getTaskID() == taskID) {
                        routeArrayList.get(i).getTasks().get(j).setAnswerArrayList(answerArrayList);
                        routeArrayList.get(i).getTasks().get(j).setTaskQuestion(question.getText().toString());
                    }
                }
            }
        }
        saveEditedRoute();
    }

    void saveEditedRoute(){
            sharedPreferences = getSharedPreferences(getString(R.string.getSharedReferences), MODE_PRIVATE);
            editor = sharedPreferences.edit();
            json = gson.toJson(routeArrayList);
            editor.putString(getString(R.string.getRoutesSharedReferences),json);
            editor.apply();
            try {
                fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
                fileOutputStream.write(json.getBytes());

            }catch (FileNotFoundException e){
                Toast.makeText(this, getString(R.string.errorFindSharedPreferences),Toast.LENGTH_LONG).show();
            }catch (IOException er){
                Toast.makeText(this, getString(R.string.errorWriteSharedPreferences),Toast.LENGTH_LONG).show();
            }
            finish();
        }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.alertDialogBackPressTitle));
        builder.setMessage(getString(R.string.alertDialogMessageBackPressEditTaskScreen));
        builder.setNegativeButton(getString(R.string.alertDialogBackPressEditNegativeButton), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setPositiveButton(getString(R.string.alertDialogBackPressEditPositiveButton), (dialogInterface, i) -> {
            super.onBackPressed();
        });
        builder.show();

    }

    }
