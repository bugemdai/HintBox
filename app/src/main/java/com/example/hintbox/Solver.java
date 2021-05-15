package com.example.hintbox;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;



public class Solver extends AppCompatActivity {

    public TextView text1, text2, text3, text4, text5, text6, text7, text8;

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
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), Home.class));
                        overridePendingTransition(0,0);
                        return true;
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

        /** prepare scrambledCube as
         *
         *             |************|
         *             |*U1**U2**U3*|
         *             |************|
         *             |*U4**U5**U6*|
         *             |************|
         *             |*U7**U8**U9*|
         *             |************|
         * ************|************|************|************|
         * *L1**L2**L3*|*F1**F2**F3*|*R1**R2**R3*|*B1**B2**B3*|
         * ************|************|************|************|
         * *L4**L5**L6*|*F4**F5**F6*|*R4**R5**R6*|*B4**B5**B6*|
         * ************|************|************|************|
         * *L7**L8**L9*|*F7**F8**F9*|*R7**R8**R9*|*B7**B8**B9*|
         * ************|************|************|************|
         *             |************|
         *             |*D1**D2**D3*|
         *             |************|
         *             |*D4**D5**D6*|
         *             |************|
         *             |*D7**D8**D9*|
         *             |************|
         *
         * -> U1 U2 ... U9 R1 ... R9 F1 ... F9 D1 ... D9 L1 ... L9 B1 ... B9
         */

        Bundle extra = getIntent().getExtras();
        String scrambledCube = extra.getString("cube");

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        text5 = findViewById(R.id.text5);
        text6 = findViewById(R.id.text6);
        text7 = findViewById(R.id.text6);
        text8 = findViewById(R.id.text6);

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

        result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.USE_SEPARATOR | Search.INVERSE_SOLUTION);
        setText(t2, result);
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