package models;

public class Segment {

    private double distance;
    private double vitesse;

    public Segment(double distance, double vitesse) {
        this.distance = distance;
        this.vitesse = vitesse;
    }

    public double getDistance() {
        return distance;
    }

    public double getVitesse() {
        return vitesse;
    }

    public double getContribution() {
        return vitesse * distance;
    }
}
