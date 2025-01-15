package com.projet.model;

public class Soldat extends Tuile {

    private static int nextId = 1;
    private int soldierId;
    private String name;
    private int playerId;
    private int healthPoints;

    public Soldat(int x, int y, String name, int playerId) {
        super(x, y, TuileType.EMPTY);
        this.soldierId = nextId++;
        this.name = name;
        this.playerId = playerId;
        this.healthPoints = 100;
    }

    public static void resetNextId(int newId) {
        nextId = newId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getSoldierId() {
        return soldierId;
    }

    public void setSoldierId(int soldierId) {
        this.soldierId = soldierId;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int hp) {
        this.healthPoints = Math.max(0, hp);
    }

    public void heal(int amount) {
        this.healthPoints = Math.min(100, this.healthPoints + amount);
    }

    public void takeDamage(int amount) {
        this.healthPoints = Math.max(0, this.healthPoints - amount);
    }

    public boolean isDead() {
        return this.healthPoints <= 0;
    }

    public void takeDamageFromTown() {
        this.healthPoints = Math.max(0, this.healthPoints - 10); // Reduce health by 10, not below 0
    }

    public void takeDamageFromPlayer() {
        this.healthPoints = Math.max(0, this.healthPoints - 20); // Reduce health by 20, not below 0
    }
}
