package com.example.hintbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class Solver extends AppCompatActivity {

    public TextView text1, text2, text3, text4, text5, text6, text7, text8;
    public CardView card1, card2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solver);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.solver);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.solver:
                        return true;
                    case R.id.cube:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.setting:
                        startActivity(new Intent(getApplicationContext(), SettingCube.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        Bundle extra = getIntent().getExtras();
        String scrambledCube = extra.getString("cube");

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);

        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);

//        text3 = findViewById(R.id.text3);
//        text4 = findViewById(R.id.text4);
//        text5 = findViewById(R.id.text5);
//        text6 = findViewById(R.id.text6);
//        text7 = findViewById(R.id.text6);
//        text8 = findViewById(R.id.text6);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                outputControl(scrambledCube, text1, text2);
                continueSearch(scrambledCube, text3, text4);
                findShorterSolutions(scrambledCube, text5);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public void outputControl(String scrambledCube, final TextView t1, final TextView t2) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.APPEND_LENGTH);
        setText(t1, result);
        card1.setVisibility(View.VISIBLE);

        result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.USE_SEPARATOR | Search.INVERSE_SOLUTION);
        setText(t2, result);
        card2.setVisibility(View.VISIBLE);
    }

    public void findShorterSolutions(String scrambledCube, final TextView t1) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 10000, 0);
        setText(t1, result);
    }

    public void continueSearch(String scrambledCube, final TextView t1, final TextView t2) {
        Search searchObj = new Search();
        String result = searchObj.solution(scrambledCube, 21, 500, 0, 0);
        setText(t1, result);

        result = searchObj.next(500, 0, 0);
        setText(t2, result);

//        result = searchObj.next(1000, 0, 0);
//        setText(t3, result);
//
//        result = searchObj.next(1500, 0, 0);
//        setText(t4, result);
//
//        result = searchObj.next(2000, 0, 0);
//        setText(t5, result);
    }
}