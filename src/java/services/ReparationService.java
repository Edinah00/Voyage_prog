
package src.java.services;

import src.java.dao.PrixReparationDAO;
import src.java.models.*;
import java.sql.SQLException;
import java.util.*;

public class ReparationService {

    private PrixReparationDAO prixDAO;

    public ReparationService() {
        this.prixDAO = new PrixReparationDAO();
    }

    /**
     * Calcule le coût de réparation d'un lavaka avec un type de réparation donné
     */
    public double calculerCoutLavaka(Lavaka lavaka, TypeReparation typeReparation) 
            throws SQLException {
        
        PrixReparation prix = prixDAO.findPrixPourProfondeur(
            typeReparation.getId(), 
            lavaka.getProfondeur()
        );
        
        if (prix == null) {
            return 0;
        }
        
        return lavaka.getSurface() * prix.getPrixParM2();
    }

    /**
     * Calcule le coût de réparation de tous les lavakas d'un chemin
     */
    public double calculerCoutChemin(Lalana lalana, TypeReparation typeReparation) 
            throws SQLException {
        
        double coutTotal = 0;
        
        for (Lavaka lavaka : lalana.getLavakas()) {
            double cout = calculerCoutLavaka(lavaka, typeReparation);
            coutTotal += cout;
        }
        
        return coutTotal;
    }

    /**
     * Calcule le coût de réparation pour plusieurs chemins (par exemple, tout le RN7)
     */
    public double calculerCoutChemins(List<Lalana> chemins, TypeReparation typeReparation) 
            throws SQLException {
        
        double coutTotal = 0;
        
        for (Lalana lalana : chemins) {
            coutTotal += calculerCoutChemin(lalana, typeReparation);
        }
        
        return coutTotal;
    }

    /**
     * Génère un rapport détaillé de réparation pour un chemin
     */
    public String genererRapportChemin(Lalana lalana, TypeReparation typeReparation) 
            throws SQLException {
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("╔═══════════════════════════════════════════════════════════╗\n");
        rapport.append("║         RAPPORT DE RÉPARATION - ").append(lalana.getNom()).append("\n");
        rapport.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        rapport.append("Type de réparation: ").append(typeReparation.getNom()).append("\n");
        rapport.append("Description: ").append(typeReparation.getDescription()).append("\n\n");
        
        rapport.append("═══════════════════════════════════════════════════════════\n");
        rapport.append("                    DÉTAILS DES LAVAKA                     \n");
        rapport.append("═══════════════════════════════════════════════════════════\n\n");
        
        double coutTotal = 0;
        int compteur = 1;
        
        for (Lavaka lavaka : lalana.getLavakas()) {
            PrixReparation prix = prixDAO.findPrixPourProfondeur(
                typeReparation.getId(), 
                lavaka.getProfondeur()
            );
            
            rapport.append(String.format("Lavaka %d:\n", compteur++));
            rapport.append(String.format("  📍 Point kilométrique: PK %.1f\n", 
                lavaka.getPointKilometrique()));
            rapport.append(String.format("  📍 Position: %.1f - %.1f km\n", 
                lavaka.getDebut(), lavaka.getFin()));
            rapport.append(String.format("  📏 Surface: %.1f m²\n", lavaka.getSurface()));
            rapport.append(String.format("  📊 Profondeur: %.2f m\n", lavaka.getProfondeur()));
            
            if (prix != null) {
                double cout = lavaka.getSurface() * prix.getPrixParM2();
                rapport.append(String.format("  💰 Prix/m²: %.0f Ar\n", prix.getPrixParM2()));
                rapport.append(String.format("  💵 Coût total: %.0f Ar\n\n", cout));
                coutTotal += cout;
            } else {
                rapport.append("  ⚠️  Aucun prix trouvé pour cette profondeur\n\n");
            }
        }
        
        rapport.append("═══════════════════════════════════════════════════════════\n");
        rapport.append("                        RÉSULTAT                          \n");
        rapport.append("═══════════════════════════════════════════════════════════\n\n");
        rapport.append(String.format("💰 COÛT TOTAL: %.0f Ar\n", coutTotal));
        rapport.append(String.format("💰 COÛT TOTAL: %.2f millions Ar\n", coutTotal / 1_000_000));
        
        return rapport.toString();
    }

    /**
     * Génère un rapport pour plusieurs chemins
     */
    public String genererRapportChemins(List<Lalana> chemins, TypeReparation typeReparation) 
            throws SQLException {
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("╔═══════════════════════════════════════════════════════════╗\n");
        rapport.append("║         RAPPORT DE RÉPARATION - MULTI-CHEMINS            ║\n");
        rapport.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        rapport.append("Type de réparation: ").append(typeReparation.getNom()).append("\n");
        rapport.append("Nombre de chemins: ").append(chemins.size()).append("\n\n");
        
        double coutGlobal = 0;
        
        for (Lalana lalana : chemins) {
            rapport.append("─────────────────────────────────────────────────────────\n");
            rapport.append("Chemin: ").append(lalana.getNom()).append("\n");
            rapport.append("─────────────────────────────────────────────────────────\n");
            
            double coutChemin = calculerCoutChemin(lalana, typeReparation);
            
            rapport.append(String.format("  Nombre de lavaka: %d\n", lalana.getLavakas().size()));
            rapport.append(String.format("  Coût: %.0f Ar (%.2f millions Ar)\n\n", 
                coutChemin, coutChemin / 1_000_000));
            
            coutGlobal += coutChemin;
        }
        
        rapport.append("═══════════════════════════════════════════════════════════\n");
        rapport.append("                    TOTAL GÉNÉRAL                         \n");
        rapport.append("═══════════════════════════════════════════════════════════\n\n");
        rapport.append(String.format("💰 COÛT TOTAL: %.0f Ar\n", coutGlobal));
        rapport.append(String.format("💰 COÛT TOTAL: %.2f millions Ar\n", coutGlobal / 1_000_000));
        
        return rapport.toString();
    }

    /**
     * Obtient les statistiques de réparation
     */
    public Map<String, Object> obtenirStatistiques(List<Lalana> chemins, TypeReparation typeReparation) 
            throws SQLException {
        
        Map<String, Object> stats = new HashMap<>();
        
        int nombreLavakas = 0;
        double surfaceTotale = 0;
        double profondeurMoyenne = 0;
        double coutTotal = 0;
        
        for (Lalana lalana : chemins) {
            for (Lavaka lavaka : lalana.getLavakas()) {
                nombreLavakas++;
                surfaceTotale += lavaka.getSurface();
                profondeurMoyenne += lavaka.getProfondeur();
                coutTotal += calculerCoutLavaka(lavaka, typeReparation);
            }
        }
        
        if (nombreLavakas > 0) {
            profondeurMoyenne /= nombreLavakas;
        }
        
        stats.put("nombreLavakas", nombreLavakas);
        stats.put("surfaceTotale", surfaceTotale);
        stats.put("profondeurMoyenne", profondeurMoyenne);
        stats.put("coutTotal", coutTotal);
        stats.put("coutMoyenParLavaka", nombreLavakas > 0 ? coutTotal / nombreLavakas : 0);
        
        return stats;
    }
}
