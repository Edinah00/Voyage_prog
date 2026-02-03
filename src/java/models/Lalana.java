package models;

import java.util.ArrayList;
import java.util.List;

public class Lalana {

    private String nom;
    private String extremiteGauche;
    private String extremiteDroite;
    private double distance; 
    private double largeur; 
    private List<Lavaka> lavaka;
    private List<Pause> pauses;
    private List<Simba> simbas;

    public Lalana(String nom, String extremiteGauche, String extremiteDroite, double distance, double largeur) {
        this.nom = nom;
        this.extremiteGauche = extremiteGauche;
        this.extremiteDroite = extremiteDroite;
        this.distance = distance;
        this.largeur = largeur;
        this.lavaka = new ArrayList<>();
        this.pauses = new ArrayList<>();
        this.simbas = new ArrayList<>();
    }

    public void ajouterLavaka(Lavaka lavak) {
        lavaka.add(lavak);
    }

    public void ajouterPause(Pause pause) {
        pauses.add(pause);
    }

    public void ajouterSimba(Simba simba) {
        simbas.add(simba);
    }

    public boolean peutPasser(Voiture voiture) {
        return voiture.getLargeur() <= largeur / 2;
    }

    public String getNom() {
        return nom;
    }

    public String getExtremiteGauche() {
        return extremiteGauche;
    }

    public String getExtremiteDroite() {
        return extremiteDroite;
    }

    public double getDistance() {
        return distance;
    }

    public double getLargeur() {
        return largeur;
    }

    public List<Lavaka> getLavakas() {
        return lavaka;
    }

    public List<Pause> getPauses() {
        return pauses;
    }

    public List<Simba> getSimbas() {
        return simbas;
    }

    public void setExtremiteGauche(String extremiteGauche) {
        this.extremiteGauche = extremiteGauche;
    }

    public void setExtremiteDroite(String extremiteDroite) {
        this.extremiteDroite = extremiteDroite;
    }

    @Override
    public String toString() {
        return nom + " (" + extremiteGauche + " -> " + extremiteDroite + ")";
    }
}