package src.java.models;

/**
 * Représente une zone de route avec une quantité de pluie spécifique.
 * Utilisé pour déterminer automatiquement le matériau de réparation.
 */
public class Pluviometrie {
    private int id;
    private String lalanaNom;
    private double debutLalana;  // PK début
    private double finLalana;     // PK fin
    private double quantitePluie; // en mm

    public Pluviometrie(String lalanaNom, double debutLalana, double finLalana, double quantitePluie) {
        this.lalanaNom = lalanaNom;
        this.debutLalana = debutLalana;
        this.finLalana = finLalana;
        this.quantitePluie = quantitePluie;
    }

    public Pluviometrie(int id, String lalanaNom, double debutLalana, double finLalana, double quantitePluie) {
        this.id = id;
        this.lalanaNom = lalanaNom;
        this.debutLalana = debutLalana;
        this.finLalana = finLalana;
        this.quantitePluie = quantitePluie;
    }

    /**
     * Vérifie si un PK donné appartient à cette zone de pluviométrie
     */
    public boolean contientPK(double pk) {
        return pk >= debutLalana && pk <= finLalana;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLalanaNom() {
        return lalanaNom;
    }

    public void setLalanaNom(String lalanaNom) {
        this.lalanaNom = lalanaNom;
    }

    public double getDebutLalana() {
        return debutLalana;
    }

    public void setDebutLalana(double debutLalana) {
        this.debutLalana = debutLalana;
    }

    public double getFinLalana() {
        return finLalana;
    }

    public void setFinLalana(double finLalana) {
        this.finLalana = finLalana;
    }

    public double getQuantitePluie() {
        return quantitePluie;
    }

    public void setQuantitePluie(double quantitePluie) {
        this.quantitePluie = quantitePluie;
    }

    @Override
    public String toString() {
        return String.format("PK %.1f - %.1f km | %s | Pluie: %.1f mm",
                debutLalana, finLalana, lalanaNom, quantitePluie);
    }
}