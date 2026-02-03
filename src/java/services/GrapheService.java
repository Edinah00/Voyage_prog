package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Lalana;

public class GrapheService {

    private Map<String, List<Lalana>> graphe;

    public GrapheService() {
        this.graphe = new HashMap<>();
    }

    public void construireGraphe(List<Lalana> lalanas) {
        System.out.println("Construction du graphe avec " + lalanas.size() + " lalanas");
        
        graphe.clear();
        
        for (Lalana lalana : lalanas) {
            String extremiteGauche = lalana.getExtremiteGauche();
            String extremiteDroite = lalana.getExtremiteDroite();
            
            // Ajouter la route dans le sens gauche → droite
            if (!graphe.containsKey(extremiteGauche)) {
                graphe.put(extremiteGauche, new ArrayList<>());
            }
            graphe.get(extremiteGauche).add(lalana);
            System.out.println("Ajout de " + lalana.getNom() + " depuis " + extremiteGauche + " vers " + extremiteDroite);
            
            // NOUVEAU : Ajouter la route dans le sens inverse droite → gauche
            if (!graphe.containsKey(extremiteDroite)) {
                graphe.put(extremiteDroite, new ArrayList<>());
            }
            graphe.get(extremiteDroite).add(lalana);
            System.out.println("Ajout de " + lalana.getNom() + " depuis " + extremiteDroite + " vers " + extremiteGauche + " (inverse)");
        }
        
        System.out.println("\nRecapitulatif du graphe:");
        for (Map.Entry<String, List<Lalana>> entry : graphe.entrySet()) {
            System.out.println("Extremite " + entry.getKey() + " a " + entry.getValue().size() + " lalana(s) sortant(s)");
        }
    }

    public List<List<Lalana>> trouverTousLesChemins(String depart, String arrivee) {
        List<List<Lalana>> tousLesChemins = new ArrayList<>();
        List<Lalana> cheminActuel = new ArrayList<>();
        List<String> extremitesVisitees = new ArrayList<>();
        
        System.out.println("Recherche de chemins de " + depart + " vers " + arrivee);
        
        List<Lalana> sortants = graphe.get(depart);
        if (sortants != null) {
            System.out.println("Point de depart a " + sortants.size() + " chemins sortants");
            for (Lalana l : sortants) {
                System.out.println("  - " + l.getNom() + " vers " + l.getExtremiteDroite());
            }
        } else {
            System.out.println("Aucun chemin sortant depuis " + depart);
        }
        
        trouverCheminsRecursif(depart, arrivee, cheminActuel, extremitesVisitees, tousLesChemins);
        
        System.out.println("Nombre total de chemins trouves: " + tousLesChemins.size());
        
        return tousLesChemins;
    }

    private void trouverCheminsRecursif(String extremiteActuelle, String destination, 
                                    List<Lalana> cheminActuel, 
                                    List<String> extremitesVisitees,
                                    List<List<Lalana>> tousLesChemins) {
    
        extremitesVisitees.add(extremiteActuelle);
        
        System.out.println("Visite de " + extremiteActuelle + " (profondeur: " + extremitesVisitees.size() + ")");
        
        if (extremiteActuelle.equals(destination)) {
            System.out.println("  DESTINATION ATTEINTE! Chemin: ");
            for (Lalana l : cheminActuel) {
                System.out.print(l.getNom() + " -> ");
            }
            System.out.println();
            tousLesChemins.add(new ArrayList<>(cheminActuel));
            extremitesVisitees.remove(extremitesVisitees.size() - 1);
            return;
        }
        
        List<Lalana> sortants = graphe.get(extremiteActuelle);
        if (sortants == null) {
            sortants = new ArrayList<>();
        }
        
        System.out.println("  " + sortants.size() + " chemins sortants depuis " + extremiteActuelle);
        
        for (Lalana lalana : sortants) {
            // MODIFIÉ : Déterminer l'extrémité suivante selon le sens
            String extremiteSuivante;
            if (lalana.getExtremiteGauche().equals(extremiteActuelle)) {
                // On emprunte dans le sens normal (gauche → droite)
                extremiteSuivante = lalana.getExtremiteDroite();
            } else {
                // On emprunte dans le sens inverse (droite → gauche)
                extremiteSuivante = lalana.getExtremiteGauche();
            }
            
            System.out.println("    Examine chemin vers " + extremiteSuivante);
            
            if (!extremitesVisitees.contains(extremiteSuivante)) {
                System.out.println("      -> Non visite, on continue");
                cheminActuel.add(lalana);
                trouverCheminsRecursif(extremiteSuivante, destination, cheminActuel, extremitesVisitees, tousLesChemins);
                cheminActuel.remove(cheminActuel.size() - 1);
            } else {
                System.out.println("      -> Deja visite, on ignore");
            }
        }
        
        extremitesVisitees.remove(extremitesVisitees.size() - 1);
    }

    public List<Lalana> trouverCheminLePlusCourt(String depart, String arrivee) {
        List<List<Lalana>> tousLesChemins = trouverTousLesChemins(depart, arrivee);
        
        if (tousLesChemins.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Lalana> cheminLePlusCourt = tousLesChemins.get(0);
        double distanceMin = calculerDistanceTotale(cheminLePlusCourt);
        
        for (int i = 1; i < tousLesChemins.size(); i++) {
            List<Lalana> chemin = tousLesChemins.get(i);
            double distance = calculerDistanceTotale(chemin);
            
            if (distance < distanceMin) {
                distanceMin = distance;
                cheminLePlusCourt = chemin;
            }
        }
        
        return cheminLePlusCourt;
    }

    private double calculerDistanceTotale(List<Lalana> chemin) {
        double total = 0;
        for (Lalana lalana : chemin) {
            total += lalana.getDistance();
        }
        return total;
    }
    
    private String genererNomReverse(String extremite1, String extremite2) {
        String initial1 = extraireInitiale(extremite1);
        String initial2 = extraireInitiale(extremite2);
        return initial1 + initial2;
    }
    
    private String extraireInitiale(String nom) {
        if (nom == null || nom.isEmpty()) {
            return "";
        }
        return nom.substring(0, 1).toUpperCase();
    }
}