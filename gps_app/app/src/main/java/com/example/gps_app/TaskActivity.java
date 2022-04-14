package com.example.gps_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class TaskActivity extends AppCompatActivity {

    //UI
    Button btn_Answer;
    ArrayList<ImageView> starsArray = new ArrayList<>();
    TextView question;

    //Variables
    int stars = 3;
    CheckBox[] checkBoxes = new CheckBox[6];
    Boolean[] checkBoxesValues = new Boolean[6];
    ArrayList<Answer> answerArrayList;
    Boolean[] goodAnswersValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        btn_Answer = findViewById(R.id.btn_TaskFinished);

        question = findViewById(R.id.tv_TaskQuestion);

        starsArray.add(findViewById(R.id.one_star));
        starsArray.add(findViewById(R.id.two_star));
        starsArray.add(findViewById(R.id.three_star));

        checkBoxes[0] = findViewById(R.id.cb_TaskCheckBox1);
        checkBoxes[1] = findViewById(R.id.cb_TaskCheckBox2);
        checkBoxes[2] = findViewById(R.id.cb_TaskCheckBox3);
        checkBoxes[3] = findViewById(R.id.cb_TaskCheckBox4);
        checkBoxes[4] = findViewById(R.id.cb_TaskCheckBox5);
        checkBoxes[5] = findViewById(R.id.cb_TaskCheckBox6);

        btn_Answer.setOnClickListener(view -> checkAnswers());

        Intent i = getIntent();
        answerArrayList = (ArrayList<Answer>) i.getSerializableExtra(getString(R.string.playerMapScreenTaskArrayList));
        String questionText = i.getStringExtra(getString(R.string.playerMapScreenQuestion));
        goodAnswersValues = new Boolean[answerArrayList.size()];
        checkBoxesValues = new Boolean[goodAnswersValues.length];
        question.setText(questionText);

        for (int j = 0; j < answerArrayList.size(); j++) {
            goodAnswersValues[j] = answerArrayList.get(j).getGoodAnswer();
        }

        for (int k = 0; k < 6; k++) {
            checkBoxes[k].setVisibility(View.INVISIBLE);
        }

        for (int j = 0; j < answerArrayList.size(); j++) {
            checkBoxes[j].setVisibility(View.VISIBLE);
            checkBoxes[j].setText(answerArrayList.get(j).answer);
        }
        if(savedInstanceState != null){
            stars = savedInstanceState.getInt("stars");
        }
        setStars();
    }

    public void setStars(){
        for(int i = 0; i < starsArray.size(); i++){
            starsArray.get(i).setImageResource(R.drawable.star_grey);
        }
        for(int i = 0; i < stars; i++){
            starsArray.get(i).setImageResource(R.drawable.star);
        }
    }

    public void checkAnswers() {
        for (int n = 0; n < answerArrayList.size(); n++) {
            checkBoxesValues[n] = (checkBoxes[n].isChecked());
        }
        if (!Arrays.equals(checkBoxesValues, goodAnswersValues)) {
            stars--;
            setStars();
        }
        if (Arrays.equals(checkBoxesValues, goodAnswersValues) || (stars == 0)) {
            endTask();
        }
    }

    public void endTask() {
        Intent data = new Intent();
        data.putExtra(getString(R.string.finishedTask), getString(R.string.finishedTaskResult));
        data.putExtra(getString(R.string.got_stars), stars);
        setResult(Activity.RESULT_OK, data);
        finish();
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.alertDialogBackPressTitle));
        builder.setMessage(getString(R.string.alertDialogMessageBackPressTaskScreen));
        builder.setNegativeButton(getString(R.string.alertDialogBackPressPlayerMapScreenNegativeButton), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setPositiveButton(getString(R.string.alertDialogBackPressPlayerMapScreenPositiveButton), (dialogInterface, i) -> {
            stars = 0;
            endTask();
            super.onBackPressed();
        });
        builder.show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("stars", stars);
    }
}