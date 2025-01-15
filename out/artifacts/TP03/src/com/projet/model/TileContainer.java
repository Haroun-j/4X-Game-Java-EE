package com.projet.model;

public class TileContainer {
    private Ville ville;
    private Montagne montagne;
    private Foret foret;
    private Soldat soldat;

    public TileContainer() {}

    public Ville getVille() {
        return ville;
    }

    public void setVille(Ville ville) {
        this.ville = ville;
    }

    public Montagne getMontagne() {
        return montagne;
    }

    public void setMontagne(Montagne montagne) {
        this.montagne = montagne;
    }

    public Foret getForet() {
        return foret;
    }

    public void setForet(Foret foret) {
        this.foret = foret;
    }

    public Soldat getSoldat() {
        return soldat;
    }

    public void setSoldat(Soldat soldat) {
        this.soldat = soldat;
    }

    public boolean hasVille() {
        return ville != null;
    }

    public boolean hasMontagne() {
        return montagne != null;
    }

    public boolean hasForet() {
        return foret != null;
    }

    public boolean hasSoldat() {
        return soldat != null;
    }

    public boolean isEmpty() {
        return ville == null && montagne == null && foret == null && soldat == null;
    }
}
