package src.java.models;

import java.util.ArrayList;
import java.util.List;

public class Voiture {

    private String nom;
    private String type;
    private double vitesseMaximale;  
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
    public List<SegmentChemin> decomposerLalana(Voyage voyage) {
        List<SegmentChemin> segments = new ArrayList<>();
        List<Lalana> chemin = voyage.getCheminChoisi();
        double vitesseMoyenne = voyage.getVitesseMoyenne();
        
        for (Lalana lalana : chemin) {
            double distanceChemin = lalana.getDistance();
            List<Lavaka> lavakas = lalana.getLavakas();
            
            if (lavakas.isEmpty()) {
                segments.add(new SegmentChemin(distanceChemin, vitesseMoyenne));
            } else {
                double positionActuelle = 0;
                
                for (Lavaka lavaka : lavakas) {
                    if (lavaka.getDebut() > positionActuelle) {
                        double distanceAvant = lavaka.getDebut() - positionActuelle;
                        segments.add(new SegmentChemin(distanceAvant, vitesseMoyenne));
                    }
                    
                    double distanceLavaka = lavaka.getFin() - lavaka.getDebut();
                    double vitesseDansLavaka = vitesseMoyenne * lavaka.getRalentissement();
                    segments.add(new SegmentChemin(distanceLavaka, vitesseDansLavaka));
                    
                    positionActuelle = lavaka.getFin();
                }
                
                if (positionActuelle < distanceChemin) {
                    double distanceRestante = distanceChemin - positionActuelle;
                    segments.add(new SegmentChemin(distanceRestante, vitesseMoyenne));
                }
            }
        }
        
        return segments;
    }

    public double calculVMRelle(List<SegmentChemin> segments, double distanceTotale) {
        double somme = 0;
        
        for (SegmentChemin segment : segments) {
            somme += segment.getDistance() * segment.getVitesse();
        }
        
        return somme / distanceTotale;
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