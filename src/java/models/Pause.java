package src.java.models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Pause {
    private int id;
    private String lalanaNom;
    private double position;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public Pause(double position, LocalTime heureDebut, LocalTime heureFin) {
        this.position = position;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public Pause(int id, String lalanaNom, double position, LocalTime heureDebut, LocalTime heureFin) {
        this.id = id;
        this.lalanaNom = lalanaNom;
        this.position = position;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public boolean doitPauserMaintenant(LocalTime heureActuelle, double positionVoiture, double positionSurChemin) {
        if (Math.abs(positionSurChemin - position) < 0.5) {
            return !heureActuelle.isBefore(heureDebut) && heureActuelle.isBefore(heureFin);
        }
        return false;
    }

    public double getDureeRestante(LocalTime heureActuelle) {
        if (heureActuelle.isBefore(heureDebut)) {
            return 0;
        }
        if (heureActuelle.isAfter(heureFin) || heureActuelle.equals(heureFin)) {
            return 0;
        }
        
        long secondes = java.time.Duration.between(heureActuelle, heureFin).getSeconds();
        return secondes / 3600.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLalanaNom() { return lalanaNom; }
    public void setLalanaNom(String lalanaNom) { this.lalanaNom = lalanaNom; }
    public double getPosition() { return position; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }

    public String getHeureDebutFormatee() {
        return heureDebut.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getHeureFinFormatee() {
        return heureFin.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}