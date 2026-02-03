package services;

import dao.PluviometrieDAO;
import dao.PluviometrieIntervalleDAO;
import models.Pluviometrie;
import models.PluviometrieIntervalle;
import models.Simba;
import java.sql.SQLException;

/**
 * Service pour déterminer automatiquement le matériau de réparation
 * basé sur la pluviométrie du lieu où se situe le Simba
 */
public class MateriauAutomatiqueService {

    private PluviometrieDAO pluviometrieDAO;
    private PluviometrieIntervalleDAO intervalleDAO;

    public MateriauAutomatiqueService() {
        this.pluviometrieDAO = new PluviometrieDAO();
        this.intervalleDAO = new PluviometrieIntervalleDAO();
    }

    /**
     * Détermine automatiquement le matériau à utiliser pour un Simba
     * 
     * PROCESSUS :
     * 1. Récupérer le PK du Simba
     * 2. Chercher la zone de pluviométrie correspondante
     * 3. Récupérer la quantité de pluie
     * 4. Trouver l'intervalle correspondant
     * 5. Retourner le matériau associé
     * 
     * @param simba Le simba à réparer
     * @return Le matériau à utiliser
     * @throws SQLException En cas d'erreur de base de données
     * @throws MateriauNotFoundException Si aucun matériau ne peut être déterminé
     */
    public String determinerMateriau(Simba simba) throws SQLException, MateriauNotFoundException {
        // Étape 1 : Récupérer le PK et le nom de la route
        double pk = simba.getPk();
        String nomLalana = simba.getLalanaNom();

        // Étape 2 : Chercher la zone de pluviométrie
        Pluviometrie pluvio = pluviometrieDAO.findByPK(nomLalana, pk);
        
        if (pluvio == null) {
            throw new MateriauNotFoundException(
                String.format("Aucune zone de pluviométrie trouvée pour le PK %.1f km sur %s", 
                    pk, nomLalana)
            );
        }

        // Étape 3 : Récupérer la quantité de pluie
        double quantitePluie = pluvio.getQuantitePluie();

        // Étape 4 : Trouver l'intervalle correspondant
        PluviometrieIntervalle intervalle = intervalleDAO.findByQuantitePluie(quantitePluie);
        
        if (intervalle == null) {
            throw new MateriauNotFoundException(
                String.format("Aucun intervalle de matériau trouvé pour %.1f mm de pluie", 
                    quantitePluie)
            );
        }

        // Étape 5 : Retourner le matériau
        return intervalle.getMateriau();
    }

    /**
     * Obtient les détails complets de la détermination du matériau
     * Utile pour afficher des informations à l'utilisateur
     */
    public DetailMateriauAutomatique obtenirDetailsMateriauPourSimba(Simba simba) 
            throws SQLException, MateriauNotFoundException {
        
        double pk = simba.getPk();
        String nomLalana = simba.getLalanaNom();

        // Récupérer la pluviométrie
        Pluviometrie pluvio = pluviometrieDAO.findByPK(nomLalana, pk);
        
        if (pluvio == null) {
            throw new MateriauNotFoundException(
                String.format("Aucune zone de pluviométrie trouvée pour le PK %.1f km sur %s", 
                    pk, nomLalana)
            );
        }

        double quantitePluie = pluvio.getQuantitePluie();

        // Récupérer l'intervalle
        PluviometrieIntervalle intervalle = intervalleDAO.findByQuantitePluie(quantitePluie);
        
        if (intervalle == null) {
            throw new MateriauNotFoundException(
                String.format("Aucun intervalle de matériau trouvé pour %.1f mm de pluie", 
                    quantitePluie)
            );
        }

        return new DetailMateriauAutomatique(
            simba,
            pluvio,
            intervalle,
            intervalle.getMateriau()
        );
    }

    /**
     * Classe interne pour encapsuler les détails de la détermination du matériau
     */
    public static class DetailMateriauAutomatique {
        private Simba simba;
        private Pluviometrie pluviometrie;
        private PluviometrieIntervalle intervalle;
        private String materiau;

        public DetailMateriauAutomatique(Simba simba, Pluviometrie pluviometrie,
                                        PluviometrieIntervalle intervalle, String materiau) {
            this.simba = simba;
            this.pluviometrie = pluviometrie;
            this.intervalle = intervalle;
            this.materiau = materiau;
        }

        public Simba getSimba() { return simba; }
        public Pluviometrie getPluviometrie() { return pluviometrie; }
        public PluviometrieIntervalle getIntervalle() { return intervalle; }
        public String getMateriau() { return materiau; }

        public String getExplication() {
            return String.format(
                "PK %.1f km → Zone [%.1f-%.1f km] → %.1f mm de pluie → Intervalle [%.1f-%.1f mm] → Matériau: %s",
                simba.getPk(),
                pluviometrie.getDebutLalana(),
                pluviometrie.getFinLalana(),
                pluviometrie.getQuantitePluie(),
                intervalle.getQuantiteMin(),
                intervalle.getQuantiteMax(),
                materiau
            );
        }
    }

    /**
     * Exception personnalisée pour les cas où le matériau ne peut pas être déterminé
     */
    public static class MateriauNotFoundException extends Exception {
        public MateriauNotFoundException(String message) {
            super(message);
        }
    }
}