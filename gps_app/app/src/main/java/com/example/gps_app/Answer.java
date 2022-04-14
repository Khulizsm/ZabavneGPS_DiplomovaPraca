package com.example.gps_app;

import android.app.Application;


import java.io.Serializable;

public class Answer extends Application implements Serializable {
    String answer;
    Boolean GoodAnswer;

    public Answer(String question, Boolean isGoodAnswer) {
        this.answer = question;
        this.GoodAnswer = isGoodAnswer;
    }

    public String getAnswer() {
        return answer;
    }


    public Boolean getGoodAnswer() {
        return GoodAnswer;
    }

    public void setGoodAnswer(Boolean isTrue) {
        GoodAnswer = isTrue;
    }
}
