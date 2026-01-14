package src.java.models;

public class Voiture {

    private String nom;
    private String type;
    private double vitesseMaximale;  // Changé de vitesseMoyenne à vitesseMaximale
    private double longueur;        
    private double largeur;
    private double reservoir;
    private double consommation;

    public Voiture(String nom, String type, double vitesseMaximale, double longueur, double largeur, 
                   double reservoir, double consommation) {
        this.nom = nom;
        this.type = type;
        this.vitesseMaximale = vitesseMaximale;
        this.longueur = longueur;
        this.largeur = largeur;
        this.reservoir = reservoir;
        this.consommation = consommation;
    }

    public double calculerConsommation(double distance) {
        return (distance / 100.0) * consommation;
    }

    public boolean peutParcourir(double distance) {
        return calculerConsommation(distance) <= reservoir;
    }

    public String getNom() {
        return nom;
    }

    public String getType() {
        return type;
    }

    public double getVitesseMaximale() {  // Changé de getVitesseMoyenne
        return vitesseMaximale;
    }

    public double getLongueur() {
        return longueur;
    }

    public double getLargeur() {
        return largeur;
    }

    public double getReservoir() {
        return reservoir;
    }

    public double getConsommation() {
        return consommation;
    }

    @Override
    public String toString() {
        return nom + " (" + type + ") - Max: " + vitesseMaximale + " km/h";
    }
}