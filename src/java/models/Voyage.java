package src.java.models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Voyage {
    private String depart;
    private String arrivee;
    private Voiture voiture;
    private double vitesseMoyenne;
    private LocalTime heureDepart;  // NOUVEAU: heure de départ
    
    private List<Lalana> cheminChoisi;
    private double tempsEcoule;
    private double positionActuelle;
    private int indexCheminActuel;
    
    private double distanceTotale;
    private boolean termine;
    private boolean enPause;
    private Pause pauseActuelle;

    public Voyage(String depart, String arrivee, Voiture voiture, double vitesseMoyenne, LocalTime heureDepart) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.voiture = voiture;
        this.vitesseMoyenne = vitesseMoyenne;
        this.heureDepart = heureDepart;
        this.cheminChoisi = new ArrayList<>();
        this.tempsEcoule = 0;
        this.positionActuelle = 0;
        this.indexCheminActuel = 0;
        this.termine = false;
        this.enPause = false;
        this.pauseActuelle = null;
    }

    public void setCheminChoisi(List<Lalana> chemin) {
        this.cheminChoisi = new ArrayList<>(chemin);
        calculerDistanceTotale();
    }

    private void calculerDistanceTotale() {
        distanceTotale = 0;
        for (Lalana lalana : cheminChoisi) {
            distanceTotale += lalana.getDistance();
        }
    }

    public double getVitesseEffective() {
        if (termine) {
            return vitesseMoyenne;
        }
        
        if (indexCheminActuel >= cheminChoisi.size()) {
            return vitesseMoyenne;
        }
        
        Lalana cheminActuel = cheminChoisi.get(indexCheminActuel);
        double vitesse = vitesseMoyenne;
        
        for (Lavaka lavaka : cheminActuel.getLavakas()) {
            if (positionActuelle >= lavaka.getDebut() && positionActuelle <= lavaka.getFin()) {
                vitesse *= (1 - lavaka.getRalentissement());
                break;
            }
        }
        
        return vitesse;
    }

    public void avancer(double deltaTime) {
        if (termine) return;
        
        if (indexCheminActuel >= cheminChoisi.size()) {
            termine = true;
            return;
        }
        
        Lalana cheminActuel = cheminChoisi.get(indexCheminActuel);
        LocalTime heureActuelle = getHeureActuelle();
        
        for (Pause pause : cheminActuel.getPauses()) {
            if (pause.doitPauserMaintenant(heureActuelle, positionActuelle, positionActuelle)) {
                if (!enPause) {
                    enPause = true;
                    pauseActuelle = pause;
                }
                
                double dureeRestante = pause.getDureeRestante(heureActuelle);
                if (dureeRestante > 0) {
                    tempsEcoule += deltaTime;
                    return;
                } else {
                    enPause = false;
                    pauseActuelle = null;
                }
            }
        }
        
        double vitesse = getVitesseEffective();
        double distanceParcourue = vitesse * deltaTime;
        
        positionActuelle += distanceParcourue;
        tempsEcoule += deltaTime;
        
        if (positionActuelle >= cheminActuel.getDistance()) {
            positionActuelle = positionActuelle - cheminActuel.getDistance();
            indexCheminActuel++;
            
            if (indexCheminActuel >= cheminChoisi.size()) {
                termine = true;
                positionActuelle = 0;
            }
        }
    }

    public double getPositionAbsolue() {
        double position = 0;
        
        for (int i = 0; i < indexCheminActuel && i < cheminChoisi.size(); i++) {
            position += cheminChoisi.get(i).getDistance();
        }
        
        if (termine) {
            return distanceTotale;
        }
        
        position += positionActuelle;
        return position;
    }

    public double getPositionRelative() {
        if (cheminChoisi.isEmpty() || indexCheminActuel >= cheminChoisi.size()) {
            if (termine) {
                return 1.0;
            }
            return 0.0;
        }
        
        Lalana cheminActuel = cheminChoisi.get(indexCheminActuel);
        double ratio = positionActuelle / cheminActuel.getDistance();
        return Math.min(1.0, Math.max(0.0, ratio));
    }

    public boolean estValide() {
        if (cheminChoisi.isEmpty()) {
            return false;
        }
        
        for (Lalana lalana : cheminChoisi) {
            if (!lalana.peutPasser(voiture)) {
                return false;
            }
        }
        
        return true;
    }

    // NOUVEAU: Calculer l'heure d'arrivée estimée
    public LocalTime getHeureArriveeEstimee(double dureeEstimee) {
        int heures = (int) dureeEstimee;
        int minutes = (int) ((dureeEstimee - heures) * 60);
        return heureDepart.plusHours(heures).plusMinutes(minutes);
    }

    // NOUVEAU: Calculer l'heure actuelle pendant le voyage
    public LocalTime getHeureActuelle() {
        int heures = (int) tempsEcoule;
        int minutes = (int) ((tempsEcoule - heures) * 60);
        return heureDepart.plusHours(heures).plusMinutes(minutes);
    }

    // NOUVEAU: Formater l'heure
    public String getHeureActuelleFormatee() {
        return getHeureActuelle().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getHeureArriveeEstimeeFormatee(double dureeEstimee) {
        return getHeureArriveeEstimee(dureeEstimee).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getHeureDepartFormatee() {
        return heureDepart.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isEnPause() {
        return enPause;
    }

    public Pause getPauseActuelle() {
        return pauseActuelle;
    }

    public String getDepart() {
        return depart;
    }

    public String getArrivee() {
        return arrivee;
    }

    public Voiture getVoiture() {
        return voiture;
    }

    public double getVitesseMoyenne() {
        return vitesseMoyenne;
    }

    public void setVitesseMoyenne(double vitesseMoyenne) {
        this.vitesseMoyenne = vitesseMoyenne;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(LocalTime heureDepart) {
        this.heureDepart = heureDepart;
    }

    public List<Lalana> getCheminChoisi() {
        return new ArrayList<>(cheminChoisi);
    }

    public double getTempsEcoule() {
        return tempsEcoule;
    }

    public double getPositionActuelle() {
        return positionActuelle;
    }

    public double getDistanceTotale() {
        return distanceTotale;
    }

    public boolean isTermine() {
        return termine;
    }

    public int getIndexCheminActuel() {
        return indexCheminActuel;
    }
}