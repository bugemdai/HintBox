package com.example.hintbox;

// This class represents a graphical object with position, size, and color attributes.
public class GraphicObject {
    // Position of the object on the x-axis
    private int xPosition;
    // Position of the object on the y-axis
    private int yPosition;
    // Width of the object
    private int width;
    // Height of the object
    private int height;
    // Color of the object
    private String color;

    // Constructor to initialize the graphical object with position, size, and color
    public GraphicObject(int xPosition, int yPosition, int width, int height, String color) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    // Getter for yPosition
    public int getYPosition() {
        return yPosition;
    }

    // Getter for xPosition
    public int getXPosition() {
        return xPosition;
    }

    // Getter for color
    public String getColor() {
        return color;
    }

    // Getter for height
    public int getHeight() {
        return height;
    }

    // Getter for width
    public int getWidth() {
        return width;
    }

    // Setter for yPosition
    public void setYPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    // Setter for xPosition
    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    // Setter for color
    public void setColor(String color) {
        this.color = color;
    }

    // Setter for height
    public void setHeight(int height) {
        this.height = height;
    }

    // Setter for width
    public void setWidth(int width) {
        this.width = width;
    }

    // Compares the given xPosition with the object's xPosition and returns the maximum
    public int compareToXPosition(int xPosition) {
        return Math.max(xPosition, this.xPosition);
    }

    // Compares the given yPosition with the object's yPosition and returns the maximum
    public int compareToYPosition(int yPosition) {
        return Math.max(yPosition, this.yPosition);
    }
}
