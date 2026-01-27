package src.java.models;

public class Simba {
    private int id;
    private String lalanaNom;
    private double pk;
    private double surface;
    private double profondeur;

    public Simba(double pk, double surface, double profondeur) {
        this.pk = pk;
        this.surface = surface;
        this.profondeur = profondeur;
    }

    public Simba(int id, String lalanaNom, double pk, double surface, double profondeur) {
        this.id = id;
        this.lalanaNom = lalanaNom;
        this.pk = pk;
        this.surface = surface;
        this.profondeur = profondeur;
    }

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

    public double getPk() {
        return pk;
    }

    public void setPk(double pk) {
        this.pk = pk;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public double getProfondeur() {
        return profondeur;
    }

    public void setProfondeur(double profondeur) {
        this.profondeur = profondeur;
    }

    @Override
    public String toString() {
        return String.format("PK %.1f km - Surface: %.2f m² - Profondeur: %.2f m",
                pk, surface, profondeur);
    }
}