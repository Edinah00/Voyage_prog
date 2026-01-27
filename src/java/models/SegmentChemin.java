package src.java.models;

public class SegmentChemin {
    private double distance;
    private double vitesse;

    public SegmentChemin(double distance, double vitesse) {
        this.distance = distance;
        this.vitesse = vitesse;
    }

    public double getDistance() {
        return distance;
    }

    public double getVitesse() {
        return vitesse;
    }
}