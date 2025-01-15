package com.projet.model;

public class Foret extends Tuile {
    private int resourceAmount;
    private String imagePath;

    public Foret(int x, int y, int resourceAmount, String imagePath) {
        super(x, y, TuileType.FORET);
        this.resourceAmount = resourceAmount;
        this.imagePath = imagePath;
    }

    public int getResourceAmount() { return resourceAmount; }
    public String getImagePath() { return imagePath; }
}
