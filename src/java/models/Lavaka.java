package src.java.models;

public class Lavaka {

    private double debut;
    private double fin;
    private double ralentissement; 
    private double pkilometre;
    private double surface;
    private double profondeur;
    public Lavaka(double debut, double fin, double ralentissement) {
        this.debut = debut;
        this.fin = fin;
        this.ralentissement = ralentissement;
    }

    public double getDebut() {
        return debut;
    }

    public double getFin() {
        return fin;
    }

    public double getRalentissement() {
        return ralentissement;
    }

    public double getLongueur() {
        return fin - debut;
    }
}