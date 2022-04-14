package com.example.gps_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class FinishScreenActivity extends AppCompatActivity {
    //UI
    Button btn_EndRoute;
    TextView tv_winText;

    //Variables
    int starCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_screen);

        tv_winText = findViewById(R.id.tv_FinishScreenWinText);
        btn_EndRoute = findViewById(R.id.btn_FinishScreenFinishRoute);

        btn_EndRoute.setOnClickListener(view -> finish());

        Intent i = getIntent();
        starCount = Integer.parseInt(i.getStringExtra(getString(R.string.got_stars)));

        if (starCount == 0) {
            tv_winText.setText(getString(R.string.finishedRouteZeroStar));
        } else if (starCount == 1) {
            tv_winText.setText(String.format("%s %d %s", getString(R.string.finishedRouteStars), starCount, getString(R.string.oneStar)));
        } else if ((1 < starCount) && (starCount < 5)) {
            tv_winText.setText(String.format("%s %d %s", getString(R.string.finishedRouteStars), starCount, getString(R.string.maxFourStar)));
        } else {
            tv_winText.setText(String.format("%s %d %s", getString(R.string.finishedRouteStars), starCount, getString(R.string.minFiveStar)));
        }
    }

}

