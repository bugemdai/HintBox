package com.example.hintbox;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class Solver extends AppCompatActivity {

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

        // Full initialization, which will take about 200ms.
        // The solver will be about 5x~10x slower without full initialization.
        long startTime = System.nanoTime();
        Search.init();
        System.out.println("Init time: " + (System.nanoTime() - startTime) / 1.0E6 + " ms");

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
//        String scrambledCube = "DUUBULDBFRBFRRULLLBRDFFFBLURDBFDFDRFRULBLUFDURRBLBDUDL";
//        String scrambledCube =   "FRRRUUDDBURDURDDUBBFLBFFURFFFLBDLULURDLDLLFULBBDBBLRFR";
        Bundle extra = getIntent().getExtras();
        String scrambledCube =   extra.getString("cube");
        // scrambledCube can also be optained by specific moves
//        scrambledCube = Tools.fromScramble("R L2 D R F U2 F' L F' B2 D' R2 B2 R2 L2 U F2 L2 B2 U2 R2");
        System.out.println(scrambledCube);

//        simpleSolve(scrambledCube);
//        outputControl(scrambledCube);
        findShorterSolutions(scrambledCube);
//        continueSearch(scrambledCube);
    }

    public static void simpleSolve(String scrambledCube) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 0, 0);
        System.out.println(result);
    }

    public static void outputControl(String scrambledCube) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.APPEND_LENGTH);
        System.out.println(result);

        result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.USE_SEPARATOR | Search.INVERSE_SOLUTION);
        System.out.println(result);
    }

    public static void findShorterSolutions(String scrambledCube) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 10000, 0);
        System.out.println(result);
    }

    public static void continueSearch(String scrambledCube) {
        Search searchObj = new Search();
        String result = searchObj.solution(scrambledCube, 21, 500, 0, 0);
        System.out.println(result);

        result = searchObj.next(500, 0, 0);
        System.out.println(result);

        result = searchObj.next(500, 0, 0);
        System.out.println(result);

        result = searchObj.next(500, 0, 0);
        System.out.println(result);

        result = searchObj.next(500, 0, 0);
        System.out.println(result);
    }
}