package ui;

import java.awt.*;
import javax.swing.*;
import models.CheminItem;
import models.Lalana;
import models.Lavaka;
import models.Voyage;

public class CheminPanel extends JPanel {
    private boolean afficherChemin = false;
    private Voyage voyageActuel;
    private CheminItem cheminSelectionne;
    private Timer animationTimer;

    public void setAfficherChemin(boolean afficher) {
        this.afficherChemin = afficher;
    }

    public void setVoyageActuel(Voyage voyage) {
        this.voyageActuel = voyage;
    }

    public void setCheminSelectionne(CheminItem chemin) {
        this.cheminSelectionne = chemin;
    }

    public void setAnimationTimer(Timer timer) {
        this.animationTimer = timer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (!afficherChemin) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String msg = "Selectionnez un chemin et cliquez sur 'Demarrer' pour afficher";
            int msgWidth = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            return;
        }

        if (cheminSelectionne == null || voyageActuel == null) return;

        int y = getHeight() / 2;
        int startX = 50;
        int endX = getWidth() - 50;
        int totalWidth = endX - startX;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(8));
        g2d.drawLine(startX, y, endX, y);

        g2d.setColor(new Color(0, 0, 139));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String departLabel = voyageActuel.getDepart();
        g2d.drawString(departLabel, startX - 20, y - 15);
        g2d.fillOval(startX - 8, y - 8, 16, 16);

        String arriveeLabel = voyageActuel.getArrivee();
        int arriveeWidth = g2d.getFontMetrics().stringWidth(arriveeLabel);
        g2d.drawString(arriveeLabel, endX - arriveeWidth + 20, y - 15);
        g2d.fillOval(endX - 8, y - 8, 16, 16);

        double distanceTotale = voyageActuel.getDistanceTotale();
        double positionCumulee = 0;
        
        for (Lalana lalana : cheminSelectionne.getChemin()) {
            double ratio = lalana.getDistance() / distanceTotale;
            int segmentWidth = (int) (totalWidth * ratio);
            int segmentStartX = startX + (int) (totalWidth * (positionCumulee / distanceTotale));
            
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            String nomLalana = lalana.getNom();
            int nameWidth = g2d.getFontMetrics().stringWidth(nomLalana);
            g2d.drawString(nomLalana, segmentStartX + (segmentWidth - nameWidth) / 2, y + 25);
            
            if (!lalana.getLavakas().isEmpty()) {
                for (Lavaka lavaka : lalana.getLavakas()) {
                    double debutRatio = lavaka.getDebut() / lalana.getDistance();
                    double finRatio = lavaka.getFin() / lalana.getDistance();
                    
                    int lavakaStartX = segmentStartX + (int)(segmentWidth * debutRatio);
                    int lavakaWidth = (int)(segmentWidth * (finRatio - debutRatio));
                    
                    g2d.setColor(new Color(255, 100, 100, 150));
                    g2d.fillRect(lavakaStartX, y - 15, lavakaWidth, 30);
                }
            }
            
            positionCumulee += lalana.getDistance();
        }

        if (animationTimer != null && animationTimer.isRunning()) {
            double positionAbsolue = voyageActuel.getPositionAbsolue();
            double ratio = positionAbsolue / distanceTotale;
            int voitureX = startX + (int) (totalWidth * ratio);

            dessinerVoiture(g2d, voitureX, y);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String voitureLabel = voyageActuel.getVoiture().getNom();
            int voitureLabelWidth = g2d.getFontMetrics().stringWidth(voitureLabel);
            g2d.drawString(voitureLabel, voitureX - voitureLabelWidth / 2, y - 25);
        }
    }

    private void dessinerVoiture(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(33, 150, 243));
        int[] bodyX = {x - 15, x - 10, x - 10, x + 10, x + 10, x + 15};
        int[] bodyY = {y, y - 8, y - 12, y - 12, y - 8, y};
        g2d.fillPolygon(bodyX, bodyY, 6);
        
        g2d.fillRect(x - 18, y, 36, 8);
        
        g2d.setColor(new Color(200, 230, 255));
        g2d.fillRect(x - 8, y - 10, 7, 6);
        g2d.fillRect(x + 1, y - 10, 7, 6);
        
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - 15, y + 6, 8, 8);
        g2d.fillOval(x + 7, y + 6, 8, 8);
        
        g2d.setColor(Color.GRAY);
        g2d.fillOval(x - 13, y + 8, 4, 4);
        g2d.fillOval(x + 9, y + 8, 4, 4);
        
        g2d.setColor(new Color(25, 118, 210));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(bodyX, bodyY, 6);
        g2d.drawRect(x - 18, y, 36, 8);
    }
}