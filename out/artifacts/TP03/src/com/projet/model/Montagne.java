package com.projet.model;

public class Montagne extends Tuile {
    private String imagePath;

    public Montagne(int x, int y, String imagePath) {
        super(x, y, TuileType.MONTAGNE);
        this.imagePath = imagePath;
    }

    public String getImagePath() { return imagePath; }
}
