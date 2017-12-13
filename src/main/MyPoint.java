package main;

import javafx.geometry.Point2D;

public class MyPoint extends Point2D {
    /**
     * Creates a new instance of {@code Point2D}.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     */
    public MyPoint(double x, double y) {
        super(x, y);
    }

    public boolean inElement = false;
}
