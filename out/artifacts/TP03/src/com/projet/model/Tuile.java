package com.projet.model;

public class Tuile {
    private int x; // Removed 'final'
    private int y; // Removed 'final'
    private TuileType type;
    private Joueur owner;

    public Tuile(int x, int y, TuileType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.owner = null;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) { // Added setter
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) { // Added setter
        this.y = y;
    }

    public TuileType getType() {
        return type;
    }

    public void setType(TuileType type) {
        this.type = type;
    }

    public Joueur getOwner() {
        return owner;
    }

    public void setOwner(Joueur owner) {
        this.owner = owner;
    }
}
