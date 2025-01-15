package com.projet.model;

public class Ville extends Tuile {
    private int defensePoints;
    private Integer ownerId;

    public Ville(int x, int y, int defensePoints) {
        super(x, y, TuileType.VILLE);
        this.defensePoints = defensePoints;
    }

    public int getDefensePoints() {
        return defensePoints;
    }

    public void setDefensePoints(int defensePoints) {
        this.defensePoints = Math.max(0, defensePoints);
    }

    public Integer getOwnerId() {
        return ownerId == null ? -1 : ownerId; // Return -1 for no owner
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public void takeDamage(int damage) {
        this.defensePoints = Math.max(0, this.defensePoints - damage);
    }

    public boolean isCaptured() {
        return this.defensePoints == 0;
    }
}
