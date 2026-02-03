package models;

/**
 * Représente un intervalle de pluviométrie associé à un matériau de réparation.
 * Permet de déterminer automatiquement quel matériau utiliser selon la quantité de pluie.
 */
public class PluviometrieIntervalle {
    private int id;
    private double quantiteMin; // en mm
    private double quantiteMax; // en mm
    private String materiau;

    public PluviometrieIntervalle(double quantiteMin, double quantiteMax, String materiau) {
        this.quantiteMin = quantiteMin;
        this.quantiteMax = quantiteMax;
        this.materiau = materiau;
    }

    public PluviometrieIntervalle(int id, double quantiteMin, double quantiteMax, String materiau) {
        this.id = id;
        this.quantiteMin = quantiteMin;
        this.quantiteMax = quantiteMax;
        this.materiau = materiau;
    }

    /**
     * Vérifie si une quantité de pluie donnée appartient à cet intervalle
     */
    public boolean correspondQuantite(double quantitePluie) {
        return quantitePluie >= quantiteMin && quantitePluie <= quantiteMax;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getQuantiteMin() {
        return quantiteMin;
    }

    public void setQuantiteMin(double quantiteMin) {
        this.quantiteMin = quantiteMin;
    }

    public double getQuantiteMax() {
        return quantiteMax;
    }

    public void setQuantiteMax(double quantiteMax) {
        this.quantiteMax = quantiteMax;
    }

    public String getMateriau() {
        return materiau;
    }

    public void setMateriau(String materiau) {
        this.materiau = materiau;
    }

    @Override
    public String toString() {
        return String.format("[%.1f - %.1f] mm → %s",
                quantiteMin, quantiteMax, materiau);
    }
}