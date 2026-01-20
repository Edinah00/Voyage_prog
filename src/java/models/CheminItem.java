package src.java.models;

import java.util.List;

public class CheminItem {
    private List<Lalana> chemin;
    private double distance;
    private double carburantNecessaire;
    private boolean carburantSuffisant;
    private double vitesseMoyenneReelle;

    public CheminItem(List<Lalana> chemin, Voiture voiture) {
        this.chemin = chemin;
        this.distance = 0;
        for (Lalana l : chemin) {
            this.distance += l.getDistance();
        }
        this.carburantNecessaire = voiture.calculerConsommation(this.distance);
        this.carburantSuffisant = voiture.peutParcourir(this.distance);
    }

public double getVitesseMoyenneReelle() {
    return vitesseMoyenneReelle;
}

public void setVitesseMoyenneReelle(double vitesseMoyenneReelle) {
    this.vitesseMoyenneReelle = vitesseMoyenneReelle;
}

    public List<Lalana> getChemin() {
        return chemin;
    }

    public double getDistance() {
        return distance;
    }

    public double getCarburantNecessaire() {
        return carburantNecessaire;
    }

    public boolean isCarburantSuffisant() {
        return carburantSuffisant;
    }

  @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    
    // Afficher le chemin
    sb.append("Chemin: ");
    for (int i = 0; i < chemin.size(); i++) {
        sb.append(chemin.get(i).getNom());
        if (i < chemin.size() - 1) {
            sb.append(" → ");
        }
    }
    
    // Afficher les statistiques
    sb.append(String.format(" | Distance: %.1f km", distance));
    sb.append(String.format(" | Carburant: %.1f L", carburantNecessaire));
    
    if (!carburantSuffisant) {
        sb.append(" ⚠️ INSUFFISANT");
    }
    
    return sb.toString();
}
}