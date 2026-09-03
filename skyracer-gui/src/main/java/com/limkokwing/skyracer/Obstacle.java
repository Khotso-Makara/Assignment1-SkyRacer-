package com.limkokwing.skyracer;

// falling obstacles
public class Obstacle {
    public double x;
    public double y;
    public final double width;
    public final double height;
    public double speed;

    public Obstacle(double x, double y, double width, double height, double speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public boolean intersects(double px, double py, double pw, double ph) {
        return x < px + pw && x + width > px &&
               y < py + ph && y + height > py;
    }
}
