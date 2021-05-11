package com.example.hintbox;

public class Cuber {
    public int x, y, w, h;
    public String color;

    Cuber (int x_, int y_, int w_, int h_, String c) {
        this.x = x_;
        this.y = y_;
        this.w = w_;
        this.h = h_;
        this.color = c;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public String getColor() {
        return color;
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int compareToX(int xC) {
        if (xC > this.x) {
            return xC;
        } else {
            return this.x;
        }
    }

    public int compareToY(int yC) {
        if (yC > this.y) {
            return yC;
        } else {
            return this.y;
        }
    }

}
