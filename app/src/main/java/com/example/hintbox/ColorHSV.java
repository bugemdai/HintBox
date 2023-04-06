package com.example.hintbox;

import org.opencv.core.Scalar;

public class ColorHSV {
    private String name;
    private int lowH, hightH, lowS, hightS, lowV, hightV;

    ColorHSV(String n, int lH, int hH, int lS, int hS, int lV, int hV){
        this.name = n;
        this.lowH = lH;
        this.hightH = hH;
        this.lowS = lS;
        this.hightS = hS;
        this.lowV = lV;
        this.hightV = hV;
    }

    public String getName () {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLowH() {
        return lowH;
    }

    public void setLowH(int lowH) {
        this.lowH = lowH;
    }

    public int getHightH() {
        return hightH;
    }

    public void setHightH(int hightH) {
        this.hightH = hightH;
    }

    public int getLowS() {
        return lowS;
    }

    public void setLowS(int lowS) {
        this.lowS = lowS;
    }

    public int getHightS() {
        return hightS;
    }

    public void setHightS(int hightS) {
        this.hightS = hightS;
    }

    public int getLowV() {
        return lowV;
    }

    public void setLowV(int lowV) {
        this.lowV = lowV;
    }

    public int getHightV() {
        return hightV;
    }

    public void setHightV(int hightV) {
        this.hightV = hightV;
    }

    public String toString() {
        return "Color: " + name + " lowH: " + lowH + " hightH: " + hightH + " lowS: " + lowS + " hightS: " + hightS + " lowV: " + lowV + " hightV: " + hightV;
    }

    public Scalar getColor() {
        return new Scalar(hightH, hightS, hightV);
    }
}
