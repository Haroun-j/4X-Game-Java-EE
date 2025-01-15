package com.projet.model;

import java.util.ArrayList;
import java.util.List;

public class Joueur {
    private int id;
    private String name;
    private int score;
    private int productionPoints;

    private int healthPoints;
    private List<Soldat> soldats;
    private List<Ville> villes;

    public Joueur(int id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
        this.productionPoints = 0;
        this.healthPoints = 100;
        this.soldats = new ArrayList<>();
        this.villes = new ArrayList<>();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getProductionPoints() {
        return productionPoints;
    }

    public void addProductionPoints(int points) {
        this.productionPoints += points;
    }
    public void spendProductionPoints(int points) {
        this.productionPoints -= points;
    }
    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = Math.max(0, healthPoints); // Ensure health doesn't go below 0
    }

    public void heal(int points) {
        this.healthPoints = Math.min(100, this.healthPoints + points); // Cap health at 100
    }

    public void takeDamageFromTown() {
        this.healthPoints = Math.max(0, this.healthPoints - 10); // Reduce health by 10, not below 0
    }

    public void takeDamageFromPlayer() {
        this.healthPoints = Math.max(0, this.healthPoints - 20); // Reduce health by 20, not below 0
    }
    public List<Soldat> getSoldats() {
        return soldats;
    }

    public void addSoldat(Soldat soldat) {
        this.soldats.add(soldat);
    }

    public void removeSoldat(Soldat soldat) {
        this.soldats.remove(soldat);
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public void addVille(Ville ville) {
        this.villes.add(ville);
    }

    public void removeVille(Ville ville) {
        this.villes.remove(ville);
    }


}
