package src.java.models;

public class PrixReparation {
    private int id;
    private int typeReparationId;
    private double profondeurMin;
    private double profondeurMax;
    private double prixParM2;

    public PrixReparation(int id, int typeReparationId, double profondeurMin, 
                          double profondeurMax, double prixParM2) {
        this.id = id;
        this.typeReparationId = typeReparationId;
        this.profondeurMin = profondeurMin;
        this.profondeurMax = profondeurMax;
        this.prixParM2 = prixParM2;
    }

    public PrixReparation(int typeReparationId, double profondeurMin, 
                          double profondeurMax, double prixParM2) {
        this.typeReparationId = typeReparationId;
        this.profondeurMin = profondeurMin;
        this.profondeurMax = profondeurMax;
        this.prixParM2 = prixParM2;
    }

    public boolean corresponda(double profondeur) {
        return profondeur >= profondeurMin && profondeur <= profondeurMax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeReparationId() {
        return typeReparationId;
    }

    public void setTypeReparationId(int typeReparationId) {
        this.typeReparationId = typeReparationId;
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
}