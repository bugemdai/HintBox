package com.example.hintbox;

import org.opencv.core.Scalar;

// Class representing a color in the HSV color space
public class ColorHSV {
    private String name; // Name of the color
    private int lowH, highH, lowS, highS, lowV, highV; // HSV range values

    // Constructor to initialize the color with its name and HSV range
    ColorHSV(String n, int lH, int hH, int lS, int hS, int lV, int hV){
        this.name = n;
        this.lowH = lH;
        this.highH = hH;
        this.lowS = lS;
        this.highS = hS;
        this.lowV = lV;
        this.highV = hV;
    }

    // Getter and setter for the color name
    public String getName () {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter for low and high H values
    public int getLowH() {
        return lowH;
    }

    public void setLowH(int lowH) {
        this.lowH = lowH;
    }

    public int getHighH() {
        return highH;
    }

    public void setHighH(int highH) {
        this.highH = highH;
    }

    // Getter and setter for low and high S values
    public int getLowS() {
        return lowS;
    }

    public void setLowS(int lowS) {
        this.lowS = lowS;
    }

    public int getHighS() {
        return highS;
    }

    public void setHighS(int highS) {
        this.highS = highS;
    }

    // Getter and setter for low and high V values
    public int getLowV() {
        return lowV;
    }

    public void setLowV(int lowV) {
        this.lowV = lowV;
    }

    public int getHighV() {
        return highV;
    }

    public void setHighV(int highV) {
        this.highV = highV;
    }

    // Returns a string representation of the color
    public String toString() {
        return "Color: " + name + " lowH: " + lowH + " highH: " + highH + " lowS: " + lowS + " highS: " + highS + " lowV: " + lowV + " highV: " + highV;
    }

    /**
     * Returns a Scalar object representing the color in HSV format.
     * The Scalar is constructed using the high values of H, S, and V.
     */
    public Scalar getColor() {
        return new Scalar(highH, highS, highV);
    }
}
