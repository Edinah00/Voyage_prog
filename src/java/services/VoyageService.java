package src.java.services;

import java.util.List;
import src.java.models.Lalana;
import src.java.models.Lavaka;
import src.java.models.Voyage;

public class VoyageService {

    public double calculerDureeEstimee(Voyage voyage) {
        List<Lalana> chemin = voyage.getCheminChoisi();
        double dureeTotal = 0;
        double vitesseMoyenne = voyage.getVitesseMoyenne();  
        
        for (Lalana lalana : chemin) {
            double distanceChemin = lalana.getDistance();
            
            List<Lavaka> lavakas = lalana.getLavakas();
            
            if (lavakas.isEmpty()) {
                dureeTotal += distanceChemin / vitesseMoyenne;
            } else {
                double distanceParcourue = 0;
                
                for (Lavaka lavaka : lavakas) {
                    double distanceAvantLavaka = lavaka.getDebut() - distanceParcourue;
                    if (distanceAvantLavaka > 0) {
                        dureeTotal += distanceAvantLavaka / vitesseMoyenne;
                    }
                    
                    double longueurLavaka = lavaka.getLongueur();
                    double vitesseDansLavaka = vitesseMoyenne * (1 - lavaka.getRalentissement());
                    dureeTotal += longueurLavaka / vitesseDansLavaka;
                    
                    distanceParcourue = lavaka.getFin();
                }
                
                double distanceRestante = distanceChemin - distanceParcourue;
                if (distanceRestante > 0) {
                    dureeTotal += distanceRestante / vitesseMoyenne;
                }
            }
        }
        
        return dureeTotal;
    }

    public boolean validerVoyage(Voyage voyage) {
        if (voyage.getDepart() == null || voyage.getArrivee() == null) {
            return false;
        }
        
        if (voyage.getVoiture() == null) {
            return false;
        }
        
        if (voyage.getCheminChoisi().isEmpty()) {
            return false;
        }
        
        if (voyage.getVitesseMoyenne() <= 0) {
            return false;
        }
        
        if (voyage.getVitesseMoyenne() > voyage.getVoiture().getVitesseMaximale()) {
            return false;
        }
        
        return voyage.estValide();
    }

    public String obtenirMessageErreur(Voyage voyage) {
        if (voyage.getDepart() == null) {
            return "Point de depart non defini";
        }
        
        if (voyage.getArrivee() == null) {
            return "Point d'arrivee non defini";
        }
        
        if (voyage.getVoiture() == null) {
            return "Voiture non selectionnee";
        }
        
        if (voyage.getCheminChoisi().isEmpty()) {
            return "Aucun chemin selectionne";
        }
        
        if (voyage.getVitesseMoyenne() <= 0) {
            return "Vitesse moyenne invalide";
        }
        
        if (voyage.getVitesseMoyenne() > voyage.getVoiture().getVitesseMaximale()) {
            return "Vitesse moyenne (" + voyage.getVitesseMoyenne() + " km/h) depasse la vitesse maximale (" + 
                   voyage.getVoiture().getVitesseMaximale() + " km/h)";
        }
        
        for (Lalana lalana : voyage.getCheminChoisi()) {
            if (!lalana.peutPasser(voyage.getVoiture())) {
                return "La voiture est trop large pour le chemin: " + lalana.getNom();
            }
        }
        
        return "";
    }
}