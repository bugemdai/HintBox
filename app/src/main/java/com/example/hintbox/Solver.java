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

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

public class Solver extends AppCompatActivity {

    // UI components
    private TextView text1, text2, text3, text4, text5, text6, text7, text8;
    private CardView card1, card2, card3, card4, card5, card6, card7, card8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solver);

        setupBottomNavigation();

        // Retrieve the scrambled cube string from the intent
        String scrambledCube = getIntent().getExtras().getString("cube");

        initializeUIComponents();

        // Start the solving process in a separate thread
        startSolvingThread(scrambledCube);
    }

    /**
     * Sets up the bottom navigation bar and its item selection listener.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.solver);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::handleNavigationItemSelected);
    }

    /**
     * Handles navigation item selection.
     * @param item The selected menu item.
     * @return true if the item selection is handled, false otherwise.
     */
    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.solver:
                return true;
            case R.id.cube:
                navigateToActivity(MainActivity.class);
                return true;
            case R.id.setting:
                navigateToActivity(SettingCube.class);
                return true;
            default:
                return false;
        }
    }

    /**
     * Navigates to the specified activity.
     * @param activityClass The class of the activity to navigate to.
     */
    private void navigateToActivity(Class<?> activityClass) {
        startActivity(new Intent(getApplicationContext(), activityClass));
        overridePendingTransition(0, 0);
    }

    /**
     * Initializes UI components and sets their initial visibility.
     */
    private void initializeUIComponents() {
        text1 = findViewById(R.id.text1);
        card1 = findViewById(R.id.card1);
        setCardVisibility(card1, View.INVISIBLE);
        text2 = findViewById(R.id.text2);
        card2 = findViewById(R.id.card2);
        setCardVisibility(card2, View.INVISIBLE);
        text3 = findViewById(R.id.text3);
        card3 = findViewById(R.id.card3);
        setCardVisibility(card3, View.INVISIBLE);
        text4 = findViewById(R.id.text4);
        card4 = findViewById(R.id.card4);
        setCardVisibility(card4, View.INVISIBLE);
        text5 = findViewById(R.id.text5);
        card5 = findViewById(R.id.card5);
        setCardVisibility(card5, View.INVISIBLE);
        text6 = findViewById(R.id.text6);
        card6 = findViewById(R.id.card6);
        setCardVisibility(card6, View.INVISIBLE);
        text7 = findViewById(R.id.text7);
        card7 = findViewById(R.id.card7);
        setCardVisibility(card7, View.INVISIBLE);
        text8 = findViewById(R.id.text8);
        card8 = findViewById(R.id.card8);
        setCardVisibility(card8, View.INVISIBLE);
    }

    /**
     * Sets the visibility of a CardView.
     * @param card The CardView to set visibility for.
     * @param visibility The visibility state to set.
     */
    private void setCardVisibility(CardView card, int visibility) {
        card.setVisibility(visibility);
    }

    /**
     * Starts a new thread to solve the scrambled cube.
     * @param scrambledCube The scrambled cube string.
     */
    private void startSolvingThread(final String scrambledCube) {
        Thread solvingThread = new Thread(() -> {
            outputControl(scrambledCube, text1, text2, card1, card2);
            continueSearch(scrambledCube, text3, text4, text5, text6, text7, card3, card4, card5, card6, card7);
            findShorterSolutions(scrambledCube, text8, card8);
        });
        solvingThread.start();
    }

    /**
     * Updates the text of a TextView on the UI thread.
     * @param text The TextView to update.
     * @param value The text value to set.
     */
    private void setText(final TextView text, final String value) {
        runOnUiThread(() -> text.setText(value));
    }

    /**
     * Makes a CardView visible on the UI thread.
     * @param card The CardView to make visible.
     */
    private void setVisi(final CardView card) {
        runOnUiThread(() -> card.setVisibility(View.VISIBLE));
    }

    /**
     * Controls the output of the solving process.
     * @param scrambledCube The scrambled cube string.
     * @param t1 TextView for the first solution.
     * @param t2 TextView for the second solution.
     * @param c1 CardView for the first solution.
     * @param c2 CardView for the second solution.
     */
    public void outputControl(String scrambledCube, final TextView t1, final TextView t2, CardView c1, CardView c2) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.APPEND_LENGTH);
        setText(t1, result);
        setVisi(c1);

        result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.USE_SEPARATOR | Search.INVERSE_SOLUTION);
        setText(t2, result);
        setVisi(c2);
    }

    /**
     * Continues searching for solutions.
     * @param scrambledCube The scrambled cube string.
     * @param t1 TextView for the first continuation.
     * @param t2 TextView for the second continuation.
     * @param t3 TextView for the third continuation.
     * @param t4 TextView for the fourth continuation.
     * @param t5 TextView for the fifth continuation.
     * @param c1 CardView for the first continuation.
     * @param c2 CardView for the second continuation.
     * @param c3 CardView for the third continuation.
     * @param c4 CardView for the fourth continuation.
     * @param c5 CardView for the fifth continuation.
     */
    public void continueSearch(String scrambledCube, final TextView t1, final TextView t2, final TextView t3, final TextView t4, final TextView t5, CardView c1, CardView c2, CardView c3, CardView c4, CardView c5) {
        Search searchObj = new Search();
        String result = searchObj.solution(scrambledCube, 21, 500, 0, 0);
        setText(t1, result);
        setVisi(c1);

        result = searchObj.next(500, 0, 0);
        setText(t2, result);
        setVisi(c2);

        result = searchObj.next(1000, 0, 0);
        setText(t3, result);
        setVisi(c3);

        result = searchObj.next(1500, 0, 0);
        setText(t4, result);
        setVisi(c4);

        result = searchObj.next(3000, 0, 0);
        setText(t5, result);
        setVisi(c5);
    }

    /**
     * Finds shorter solutions for the scrambled cube.
     * @param scrambledCube The scrambled cube string.
     * @param t1 TextView for the shorter solution.
     * @param c1 CardView for the shorter solution.
     */
    public void findShorterSolutions(String scrambledCube, final TextView t1, CardView c1) {
        String result = new Search().solution(scrambledCube, 21, 100000000, 10000, 0);
        setText(t1, result);
        setVisi(c1);
    }
}
