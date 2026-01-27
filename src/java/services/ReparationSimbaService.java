package src.java.services;

import src.java.dao.ReparationDAO;
import src.java.models.Reparation;
import src.java.models.Simba;
import java.sql.SQLException;
import java.util.List;

public class ReparationSimbaService {

    private ReparationDAO reparationDAO;

    public ReparationSimbaService() {
        this.reparationDAO = new ReparationDAO();
    }

    public double calculerCoutReparation(Simba simba, String materiau) throws SQLException {
        Reparation reparation = reparationDAO.findByMateriauAndProfondeur(materiau, simba.getProfondeur());
        
        if (reparation == null) {
            return 0;
        }
        
        return simba.getSurface() * reparation.getPrixParM2();
    }

    public double calculerCoutTotal(List<Simba> simbas, String materiau) throws SQLException {
        double coutTotal = 0;
        
        for (Simba simba : simbas) {
            coutTotal += calculerCoutReparation(simba, materiau);
        }
        
        return coutTotal;
    }

    public String obtenirDetailCout(Simba simba, String materiau) throws SQLException {
        Reparation reparation = reparationDAO.findByMateriauAndProfondeur(materiau, simba.getProfondeur());
        
        if (reparation == null) {
            return "Aucune reparation disponible pour cette profondeur";
        }
        
        double cout = simba.getSurface() * reparation.getPrixParM2();
        
        return String.format("Surface: %.2f m2 x Prix: %.0f Ar/m2 = %.0f Ar", 
            simba.getSurface(), reparation.getPrixParM2(), cout);
    }
}