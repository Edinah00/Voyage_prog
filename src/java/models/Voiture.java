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
public double getConsommation() {
    return consommation;
}
public void setConsommation(double consommation) {
    this.consommation = consommation;
}
public double getLargeur() {
    return largeur;
}
public void setLargeur(double largeur) {
    this.largeur = largeur;
}
public double getLongueur() {
    return longueur;
}
public void setLongueur(double longueur) {
    this.longueur = longueur;
}
    public Voiture(String nom, String type, double vitesseMaximale,
                   double longueur, double largeur,
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

    // ✅ MÉTHODE OFFICIELLE DEMANDÉE
   /*  public double calculerVitesseMoyenneReelle(
            List<Segment> segments,
            double vitesseSouhaitee) {

        if (segments == null || segments.isEmpty()) return 0;

        double somme = 0;
        double distanceTotale = 0;

        for (Segment s : segments) {
            somme += s.getContribution();
            distanceTotale += s.getDistance();
        }

        return distanceTotale == 0 ? 0 : somme / distanceTotale;
    }
   */
   
    public double getVitesseMaximale() {
        return vitesseMaximale;
    }

    public double getReservoir() {
        return reservoir;
    }

    @Override
    public String toString() {
        return nom + " (" + type + ")";
    }

    public String getNom() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNom'");
    }

    public String getType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    public double calculerVitesseMoyenneReelle(
            List<Segment> segments,
            double vitesseSouhaitee) {
        
        if (segments == null || segments.isEmpty()) {
            return 0;
        }

        double distanceTotale = 0;
        double tempsTotalHeures = 0;

        for (Segment s : segments) {
            double distance = s.getDistance();
            double vitesse = s.getVitesse();
            
            if (vitesse <= 0) {
                continue; // Éviter division par zéro
            }
            
            distanceTotale += distance;
            tempsTotalHeures += distance / vitesse; // Temps = Distance / Vitesse
        }

        // Vitesse moyenne = Distance totale / Temps total
        return tempsTotalHeures == 0 ? 0 : distanceTotale / tempsTotalHeures;
    }

    /**
     * Décompose une lalana en segments en tenant compte des lavakas
     */
    private List<Segment> decomposerLalana(Lalana lalana, double vitesseSouhaitee) {
        List<Segment> segments = new ArrayList<>();
        double vitesseNormale = Math.min(vitesseSouhaitee, this.vitesseMaximale);

        List<Lavaka> lavakas = lalana.getLavakas();
        double longueur = lalana.getDistance();

        if (lavakas == null || lavakas.isEmpty()) {
            segments.add(new Segment(longueur, vitesseNormale));
            return segments;
        }

        lavakas.sort((a, b) -> Double.compare(a.getDebut(), b.getDebut()));
        double position = 0;

        for (Lavaka l : lavakas) {
            if (position < l.getDebut()) {
                segments.add(new Segment(l.getDebut() - position, vitesseNormale));
            }

            double vitesseLavaka = Math.max(10, vitesseNormale * (1 - l.getRalentissement()));
            segments.add(new Segment(l.getFin() - l.getDebut(), vitesseLavaka));

            position = l.getFin();
        }

        if (position < longueur) {
            segments.add(new Segment(longueur - position, vitesseNormale));
        }

        return segments;
    }

    /**
     * Décompose un chemin complet en segments
     */
    private List<Segment> decomposerChemin(List<Lalana> chemin, double vitesseSouhaitee) {
        List<Segment> segments = new ArrayList<>();
        for (Lalana l : chemin) {
            segments.addAll(decomposerLalana(l, vitesseSouhaitee));
        }
        return segments;
    }

    /**
     * Génère un rapport détaillé du calcul de vitesse moyenne
     */
    public String afficherDetailsVitesseMoyenne(Voyage voyage) {
        if (voyage == null || voyage.getCheminChoisi() == null) {
            return "Aucun voyage défini";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════╗\n");
        sb.append("║       DÉTAILS DE LA VITESSE MOYENNE RÉELLE               ║\n");
        sb.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        sb.append("🚗 Voiture: ").append(this.toString()).append("\n");
        sb.append("🎯 Vitesse souhaitée: ").append(String.format("%.1f km/h", voyage.getVitesseMoyenne())).append("\n\n");
        
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("                    SEGMENTS DU TRAJET                     \n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        
        // Décomposer le chemin
        List<Segment> segments = decomposerChemin(voyage.getCheminChoisi(), voyage.getVitesseMoyenne());
        
        double distanceTotale = 0;
        double tempsTotalHeures = 0;
        int segmentNum = 1;
        
        for (Segment seg : segments) {
            double distance = seg.getDistance();
            double vitesse = seg.getVitesse();
            double temps = vitesse > 0 ? distance / vitesse : 0;
            
            distanceTotale += distance;
            tempsTotalHeures += temps;
            
            sb.append(String.format("Segment %d:\n", segmentNum++));
            sb.append(String.format("  📏 Distance: %.2f km\n", distance));
            sb.append(String.format("  ⚡ Vitesse: %.2f km/h\n", vitesse));
            sb.append(String.format("  ⏱️  Temps: %.4f h (%.1f min)\n\n", temps, temps * 60));
        }
        
        double vitesseReelle = tempsTotalHeures > 0 ? distanceTotale / tempsTotalHeures : 0;
        
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("                        RÉSULTAT                          \n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("📏 Distance totale: %.2f km\n", distanceTotale));
        sb.append(String.format("⏱️  Temps total: %.4f h (%.1f min)\n", tempsTotalHeures, tempsTotalHeures * 60));
        sb.append(String.format("🚗 Vitesse moyenne RÉELLE: %.2f km/h\n", vitesseReelle));
        sb.append(String.format("📉 Différence: %.2f km/h (%.1f%%)\n",
                voyage.getVitesseMoyenne() - vitesseReelle,
                ((voyage.getVitesseMoyenne() - vitesseReelle) / voyage.getVitesseMoyenne()) * 100));
        
        return sb.toString();
    }
}
