package com.projet.model;

import java.util.ArrayList;
import java.util.List;

public class Joueur {
    private int id;
    private String name;
    private int score;
    private int productionPoints;

    private List<Soldat> soldats;
    private List<Ville> villes;

    public Joueur(int id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
        this.productionPoints = 0;
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
