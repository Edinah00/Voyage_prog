package src.java.models;

import java.util.ArrayList;
import java.util.List;

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



public double calculerVitesseMoyenneReelle(Voyage voyage) {
    if (voyage == null || voyage.getCheminChoisi().isEmpty()) {
        return 0.0;
    }
    
    double distanceTotale = 0.0;
    double tempsTotalHeures = 0.0;
    double vitesseMoyenneSouhaitee = voyage.getVitesseMoyenne();
    
    // Pour chaque route du chemin
    for (Lalana lalana : voyage.getCheminChoisi()) {
        double distanceLalana = lalana.getDistance();
        List<Lavaka> lavakas = lalana.getLavakas();
        
        if (lavakas == null || lavakas.isEmpty()) {
            // Pas de lavaka : toute la route à vitesse normale
            double temps = distanceLalana / vitesseMoyenneSouhaitee;
            distanceTotale += distanceLalana;
            tempsTotalHeures += temps;
        } else {
            // Il y a des lavakas : décomposer la route
            // Trier les lavakas par position
            List<Lavaka> lavakasTries = new ArrayList<>(lavakas);
            lavakasTries.sort((l1, l2) -> Double.compare(l1.getDebut(), l2.getDebut()));
            
            double positionCourante = 0;
            
            for (Lavaka lavaka : lavakasTries) {
                double debutLavaka = lavaka.getDebut();
                double finLavaka = lavaka.getFin();
                
                // Partie AVANT le lavaka (route normale)
                if (positionCourante < debutLavaka) {
                    double distanceNormale = debutLavaka - positionCourante;
                    double tempsNormal = distanceNormale / vitesseMoyenneSouhaitee;
                    
                    distanceTotale += distanceNormale;
                    tempsTotalHeures += tempsNormal;
                }
                
                // Partie AVEC lavaka (vitesse réduite)
                double distanceLavaka = finLavaka - debutLavaka;
                double vitesseReduite = calculerVitesseDansLavaka(vitesseMoyenneSouhaitee, lavaka);
                double tempsLavaka = distanceLavaka / vitesseReduite;
                
                distanceTotale += distanceLavaka;
                tempsTotalHeures += tempsLavaka;
                
                positionCourante = finLavaka;
            }
            
            // Partie APRÈS le dernier lavaka
            if (positionCourante < distanceLalana) {
                double distanceFinale = distanceLalana - positionCourante;
                double tempsFinale = distanceFinale / vitesseMoyenneSouhaitee;
                
                distanceTotale += distanceFinale;
                tempsTotalHeures += tempsFinale;
            }
        }
    }
    
    // Vitesse moyenne réelle = Distance totale / Temps total
    if (tempsTotalHeures == 0) {
        return 0.0;
    }
    
    return distanceTotale / tempsTotalHeures;
}


private double calculerVitesseDansLavaka(double vitesseNormale, Lavaka lavaka) {
    double facteurReduction = 1 - lavaka.getRalentissement();
    double vitesseReduite = vitesseNormale * facteurReduction;
    
    double vitesseMinimale = 10.0;
    
    return Math.max(vitesseReduite, vitesseMinimale);
}

public String afficherDetailsVitesseMoyenne(Voyage voyage) {
    StringBuilder details = new StringBuilder();
    double distanceTotale = 0.0;
    double tempsTotalHeures = 0.0;
    double vitesseMoyenneSouhaitee = voyage.getVitesseMoyenne();
    
    details.append("╔═══════════════════════════════════════════════════════╗\n");
    details.append("║     CALCUL DE LA VITESSE MOYENNE RÉELLE               ║\n");
    details.append("╚═══════════════════════════════════════════════════════╝\n\n");
    
    details.append(String.format("Trajet: %s → %s\n", voyage.getDepart(), voyage.getArrivee()));
    details.append(String.format("Voiture: %s\n", this.nom));
    details.append(String.format("Vitesse souhaitée: %.1f km/h\n", vitesseMoyenneSouhaitee));
    details.append(String.format("Vitesse maximale: %.1f km/h\n\n", this.vitesseMaximale));
    
    int numRoute = 1;
    for (Lalana lalana : voyage.getCheminChoisi()) {
        details.append(String.format("─── Route %d: %s (%.1f km) ───\n", 
            numRoute, lalana.getNom(), lalana.getDistance()));
        
        double distanceLalana = lalana.getDistance();
        List<Lavaka> lavakas = lalana.getLavakas();
        
        if (lavakas == null || lavakas.isEmpty()) {
            // Pas de lavaka
            double temps = distanceLalana / vitesseMoyenneSouhaitee;
            
            details.append("  Segment NORMAL:\n");
            details.append(String.format("    • Distance: %.2f km\n", distanceLalana));
            details.append(String.format("    • Vitesse: %.2f km/h\n", vitesseMoyenneSouhaitee));
            details.append(String.format("    • Temps: %.2f h (%.0f min)\n\n", temps, temps * 60));
            
            distanceTotale += distanceLalana;
            tempsTotalHeures += temps;
        } else {
            // Avec lavakas
            List<Lavaka> lavakasTries = new ArrayList<>(lavakas);
            lavakasTries.sort((l1, l2) -> Double.compare(l1.getDebut(), l2.getDebut()));
            
            double positionCourante = 0;
            int numSegment = 1;
            
            for (Lavaka lavaka : lavakasTries) {
                double debutLavaka = lavaka.getDebut();
                double finLavaka = lavaka.getFin();
                
                // Segment normal avant le lavaka
                if (positionCourante < debutLavaka) {
                    double distanceNormale = debutLavaka - positionCourante;
                    double tempsNormal = distanceNormale / vitesseMoyenneSouhaitee;
                    
                    details.append(String.format("  Segment %d [NORMAL]:\n", numSegment));
                    details.append(String.format("    • Position: %.1f → %.1f km\n", 
                        positionCourante, debutLavaka));
                    details.append(String.format("    • Distance: %.2f km\n", distanceNormale));
                    details.append(String.format("    • Vitesse: %.2f km/h\n", vitesseMoyenneSouhaitee));
                    details.append(String.format("    • Temps: %.2f h (%.0f min)\n\n", 
                        tempsNormal, tempsNormal * 60));
                    
                    distanceTotale += distanceNormale;
                    tempsTotalHeures += tempsNormal;
                    numSegment++;
                }
                
                // Segment avec lavaka
                double distanceLavaka = finLavaka - debutLavaka;
                double vitesseReduite = calculerVitesseDansLavaka(vitesseMoyenneSouhaitee, lavaka);
                double tempsLavaka = distanceLavaka / vitesseReduite;
                
                details.append(String.format("  Segment %d [LAVAKA - Ralentissement: %.0f%%]:\n", 
                    numSegment, lavaka.getRalentissement() * 100));
                details.append(String.format("    • Position: %.1f → %.1f km\n", 
                    debutLavaka, finLavaka));
                details.append(String.format("    • Distance: %.2f km\n", distanceLavaka));
                details.append(String.format("    • Vitesse: %.2f km/h\n", vitesseReduite));
                details.append(String.format("    • Temps: %.2f h (%.0f min)\n\n", 
                    tempsLavaka, tempsLavaka * 60));
                
                distanceTotale += distanceLavaka;
                tempsTotalHeures += tempsLavaka;
                
                positionCourante = finLavaka;
                numSegment++;
            }
            
            // Segment final après le dernier lavaka
            if (positionCourante < distanceLalana) {
                double distanceFinale = distanceLalana - positionCourante;
                double tempsFinale = distanceFinale / vitesseMoyenneSouhaitee;
                
                details.append(String.format("  Segment %d [NORMAL]:\n", numSegment));
                details.append(String.format("    • Position: %.1f → %.1f km\n", 
                    positionCourante, distanceLalana));
                details.append(String.format("    • Distance: %.2f km\n", distanceFinale));
                details.append(String.format("    • Vitesse: %.2f km/h\n", vitesseMoyenneSouhaitee));
                details.append(String.format("    • Temps: %.2f h (%.0f min)\n\n", 
                    tempsFinale, tempsFinale * 60));
                
                distanceTotale += distanceFinale;
                tempsTotalHeures += tempsFinale;
            }
        }
        
        numRoute++;
        details.append("\n");
    }
    
    double vitesseMoyenneReelle = distanceTotale / tempsTotalHeures;
    
    details.append("╔═══════════════════════════════════════════════════════╗\n");
    details.append("║                      RÉSUMÉ                           ║\n");
    details.append("╚═══════════════════════════════════════════════════════╝\n");
    details.append(String.format("Distance totale: %.2f km\n", distanceTotale));
    details.append(String.format("Temps total: %.2f heures (%.0f minutes)\n", 
        tempsTotalHeures, tempsTotalHeures * 60));
    details.append(String.format("VITESSE MOYENNE RÉELLE: %.2f km/h\n", vitesseMoyenneReelle));
    details.append(String.format("Différence avec vitesse souhaitée: %.2f km/h (%.1f%%)\n",
        vitesseMoyenneSouhaitee - vitesseMoyenneReelle,
        ((vitesseMoyenneSouhaitee - vitesseMoyenneReelle) / vitesseMoyenneSouhaitee) * 100));
    
    return details.toString();
}
}