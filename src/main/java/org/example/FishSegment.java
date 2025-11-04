package org.example;

import java.awt.geom.Point2D;

public class FishSegment {
    public Point2D.Double position;
    public double angle;
    public int size;

    public FishSegment(double x, double y, int size) {
        this.position = new Point2D.Double(x, y);
        this.angle = 0;
        this.size = size;
    }
}