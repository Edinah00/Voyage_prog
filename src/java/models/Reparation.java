package models;

public class Reparation {

    private int id;
    private String materiau;
    private double profondeurMin;
    private double profondeurMax;
    private double prixParM2;

    public Reparation(String materiau, double profondeurMin, double profondeurMax, double prixParM2) {
        this.materiau = materiau;
        this.profondeurMin = profondeurMin;
        this.profondeurMax = profondeurMax;
        this.prixParM2 = prixParM2;
    }

    public Reparation(int id, String materiau, double profondeurMin, double profondeurMax, double prixParM2) {
        this.id = id;
        this.materiau = materiau;
        this.profondeurMin = profondeurMin;
        this.profondeurMax = profondeurMax;
        this.prixParM2 = prixParM2;
    }

    // MODIFIÉ: Intervalle ]profondeur_min, profondeur_max]
    // Ouvert à gauche (>) et fermé à droite (<=)
    public boolean correspondProfondeur(double profondeur) {
        return profondeur > profondeurMin && profondeur <= profondeurMax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMateriau() {
        return materiau;
    }

    public void setMateriau(String materiau) {
        this.materiau = materiau;
    }

    public double getProfondeurMin() {
        return profondeurMin;
    }

    public void setProfondeurMin(double profondeurMin) {
        this.profondeurMin = profondeurMin;
    }

    public double getProfondeurMax() {
        return profondeurMax;
    }

    public void setProfondeurMax(double profondeurMax) {
        this.profondeurMax = profondeurMax;
    }

    public double getPrixParM2() {
        return prixParM2;
    }

    public void setPrixParM2(double prixParM2) {
        this.prixParM2 = prixParM2;
    }

    @Override
    public String toString() {
        return String.format("%s ]%.2f - %.2f] m - %.0f Ar/m2", 
            materiau, profondeurMin, profondeurMax, prixParM2);
    }
}