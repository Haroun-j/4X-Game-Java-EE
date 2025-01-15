package com.projet.utils;

public class PlayerPosition {
    private int x;
    private int y;

    public PlayerPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toJson() {
        return String.format("{\"x\":%d, \"y\":%d}", x, y);
    }
}