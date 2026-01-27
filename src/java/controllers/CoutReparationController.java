package src.java.controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import src.java.dao.ReparationDAO;
import src.java.dao.SimbaDAO;
import src.java.models.Lalana;
import src.java.models.Reparation;
import src.java.models.Simba;
import src.java.services.ReparationSimbaService;

public class CoutReparationController extends JDialog {

    private JComboBox<String> cbChemin;
    private JPanel panelSimbaList;
    private JButton btnValider;
    private JButton btnFermer;
    
    private JTable tableResultats;
    private DefaultTableModel tableModel;
    private JLabel lblCoutChemin;
    private JLabel lblCoutTrajet;
    private JButton btnTrierAsc;
    private JButton btnTrierDesc;

    private List<Lalana> chemins;
    private List<Simba> simbasActuels;
    private Map<Simba, JComboBox<String>> materiauxComboBoxes;
    private ReparationSimbaService reparationService;
    private ReparationDAO reparationDAO;
    private SimbaDAO simbaDAO;
    private List<String> materiauxDisponibles;

    public CoutReparationController(Frame parent, List<Lalana> chemins) {
        super(parent, "Calcul du Cout de Reparation des Simba", true);
        setSize(1200, 800);
        setLocationRelativeTo(parent);
        
        this.chemins = chemins;
        this.reparationService = new ReparationSimbaService();
        this.reparationDAO = new ReparationDAO();
        this.simbaDAO = new SimbaDAO();
        this.materiauxComboBoxes = new HashMap<>();
        this.simbasActuels = new ArrayList<>();
        
        initComponents();
        chargerDonnees();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel supérieur : sélection et liste des simbas
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        
        // Sélection du chemin
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectionPanel.add(new JLabel("Chemin:"));
        cbChemin = new JComboBox<>();
        cbChemin.setPreferredSize(new Dimension(300, 30));
        cbChemin.addActionListener(e -> chargerSimbasDuChemin());
        selectionPanel.add(cbChemin);
        
        topPanel.add(selectionPanel, BorderLayout.NORTH);
        
        // Liste des simbas avec sélection de matériaux
        JPanel simbaPanel = new JPanel(new BorderLayout(0, 0));
        simbaPanel.setBorder(BorderFactory.createTitledBorder("Liste des points kilometriques"));
        
        panelSimbaList = new JPanel();
        panelSimbaList.setLayout(new BoxLayout(panelSimbaList, BoxLayout.Y_AXIS));
        panelSimbaList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollSimba = new JScrollPane(panelSimbaList);
        scrollSimba.setPreferredSize(new Dimension(0, 180));
        scrollSimba.setBorder(BorderFactory.createEmptyBorder());
        simbaPanel.add(scrollSimba, BorderLayout.CENTER);
        
        // Bouton valider
        JPanel btnValidatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnValider = new JButton("Valider et Calculer les Couts");
        btnValider.setBackground(new Color(33, 150, 243));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(new Font("Arial", Font.BOLD, 14));
        btnValider.setFocusPainted(false);
        btnValider.addActionListener(e -> calculerCouts());
        btnValider.setEnabled(false);
        btnValidatePanel.add(btnValider);
        simbaPanel.add(btnValidatePanel, BorderLayout.SOUTH);
        
        topPanel.add(simbaPanel, BorderLayout.CENTER);
        
        // Panel central : tableau des résultats
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Resultats du Calcul des Couts"));
        
        // Panel pour les boutons de tri
        JPanel triPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        triPanel.add(new JLabel("Trier par prix:"));
        
        btnTrierAsc = new JButton("↑ Ascendant");
        btnTrierAsc.setBackground(new Color(76, 175, 80));
        btnTrierAsc.setForeground(Color.WHITE);
        btnTrierAsc.setFont(new Font("Arial", Font.BOLD, 12));
        btnTrierAsc.setFocusPainted(false);
        btnTrierAsc.addActionListener(e -> trierParPrix(true));
        
        btnTrierDesc = new JButton("↓ Descendant");
        btnTrierDesc.setBackground(new Color(244, 67, 54));
        btnTrierDesc.setForeground(Color.WHITE);
        btnTrierDesc.setFont(new Font("Arial", Font.BOLD, 12));
        btnTrierDesc.setFocusPainted(false);
        btnTrierDesc.addActionListener(e -> trierParPrix(false));
        
        triPanel.add(btnTrierAsc);
        triPanel.add(btnTrierDesc);
        
        tablePanel.add(triPanel, BorderLayout.NORTH);
        
        String[] colonnes = {"Chemin", "PK (km)", "Surface (m²)", "Profondeur (m)", "Matériau", "Prix/m² (Ar)", "Coût Total (Ar)"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableResultats = new JTable(tableModel);
        tableResultats.setRowHeight(30);
        tableResultats.setFont(new Font("Arial", Font.PLAIN, 12));
        tableResultats.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Alignement des colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 7; i++) {
            tableResultats.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Largeurs des colonnes
        tableResultats.getColumnModel().getColumn(0).setPreferredWidth(180);
        tableResultats.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableResultats.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableResultats.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableResultats.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableResultats.getColumnModel().getColumn(5).setPreferredWidth(120);
        tableResultats.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        JScrollPane scrollTable = new JScrollPane(tableResultats);
        tablePanel.add(scrollTable, BorderLayout.CENTER);

        // Panel du bas avec les coûts totaux
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel pour les labels de coût
        JPanel coutPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        coutPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        lblCoutChemin = new JLabel("Cout du chemin sélectionné: 0 Ar");
        lblCoutChemin.setFont(new Font("Arial", Font.BOLD, 16));
        lblCoutChemin.setForeground(new Color(76, 175, 80));
        
        lblCoutTrajet = new JLabel("Cout total du trajet: 0 Ar");
        lblCoutTrajet.setFont(new Font("Arial", Font.BOLD, 18));
        lblCoutTrajet.setForeground(new Color(46, 125, 50));
        
        coutPanel.add(lblCoutChemin);
        coutPanel.add(lblCoutTrajet);
        
        bottomPanel.add(coutPanel, BorderLayout.NORTH);
        
        // Bouton fermer
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(e -> dispose());
        btnPanel.add(btnFermer);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void chargerDonnees() {
        try {
            // Charger les matériaux disponibles
            materiauxDisponibles = reparationDAO.getAllMateriaux();
            
            // Charger les chemins
            cbChemin.addItem("Tous les chemins du trajet");
            for (Lalana chemin : chemins) {
                cbChemin.addItem(chemin.getNom());
            }

        } catch (SQLException e) {
            showError("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void chargerSimbasDuChemin() {
        String cheminSelectionne = (String) cbChemin.getSelectedItem();
        if (cheminSelectionne == null) return;

        panelSimbaList.removeAll();
        materiauxComboBoxes.clear();
        simbasActuels.clear();
        tableModel.setRowCount(0);
        lblCoutChemin.setText("Cout du chemin sélectionné: 0 Ar");
        lblCoutTrajet.setText("Cout total du trajet: 0 Ar");

        try {
            if (cheminSelectionne.equals("Tous les chemins du trajet")) {
                // Charger tous les simbas de tous les chemins
                for (Lalana lalana : chemins) {
                    List<Simba> simbasLalana = simbaDAO.findByLalana(lalana.getNom());
                    if (!simbasLalana.isEmpty()) {
                        ajouterGroupeSimba(lalana.getNom(), simbasLalana);
                    }
                }
            } else {
                // Charger les simbas du chemin sélectionné
                List<Simba> simbas = simbaDAO.findByLalana(cheminSelectionne);
                if (!simbas.isEmpty()) {
                    ajouterGroupeSimba(cheminSelectionne, simbas);
                }
            }

            if (simbasActuels.isEmpty()) {
                JLabel lblVide = new JLabel("Aucun simba trouvé sur ce chemin");
                lblVide.setFont(new Font("Arial", Font.ITALIC, 14));
                lblVide.setForeground(Color.GRAY);
                lblVide.setBorder(new EmptyBorder(20, 20, 20, 20));
                panelSimbaList.add(lblVide);
                btnValider.setEnabled(false);
            } else {
                btnValider.setEnabled(true);
            }

        } catch (SQLException e) {
            showError("Erreur lors du chargement des simbas: " + e.getMessage());
        }

        panelSimbaList.revalidate();
        panelSimbaList.repaint();
    }

    private void ajouterGroupeSimba(String nomChemin, List<Simba> simbas) {
        // Ajouter chaque simba directement sans en-tête
        for (Simba simba : simbas) {
            simbasActuels.add(simba);
            
            JPanel simbaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
            simbaPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            simbaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            
            // Informations du simba
            JLabel lblInfo = new JLabel(String.format("PK %.1f km  |  Surface: %.2f m²  |  Profondeur: %.2f m", 
                simba.getPk(), simba.getSurface(), simba.getProfondeur()));
            lblInfo.setFont(new Font("Arial", Font.PLAIN, 12));
            lblInfo.setPreferredSize(new Dimension(450, 25));
            simbaPanel.add(lblInfo);
            
            // Label matériau
            JLabel lblMateriau = new JLabel("Matériau:");
            simbaPanel.add(lblMateriau);
            
            // ComboBox pour sélectionner le matériau
            JComboBox<String> cbMateriau = new JComboBox<>();
            cbMateriau.addItem("-- Sélectionner --");
            for (String materiau : materiauxDisponibles) {
                cbMateriau.addItem(materiau);
            }
            cbMateriau.setPreferredSize(new Dimension(150, 25));
            simbaPanel.add(cbMateriau);
            
            materiauxComboBoxes.put(simba, cbMateriau);
            
            panelSimbaList.add(simbaPanel);
        }
    }

    private void calculerCouts() {
        // Vérifier que tous les matériaux sont sélectionnés
        for (Map.Entry<Simba, JComboBox<String>> entry : materiauxComboBoxes.entrySet()) {
            String materiau = (String) entry.getValue().getSelectedItem();
            if (materiau == null || materiau.equals("-- Sélectionner --")) {
                showError("Veuillez sélectionner un matériau pour tous les simbas");
                return;
            }
        }

        try {
            tableModel.setRowCount(0);
            
            String cheminSelectionne = (String) cbChemin.getSelectedItem();
            double coutCheminSelectionne = 0;
            double coutTotalTrajet = 0;
            
            Map<String, Double> coutsParChemin = new HashMap<>();
            
            // Calculer pour chaque simba
            for (Map.Entry<Simba, JComboBox<String>> entry : materiauxComboBoxes.entrySet()) {
                Simba simba = entry.getKey();
                String materiau = (String) entry.getValue().getSelectedItem();
                
                Reparation reparation = reparationDAO.findByMateriauAndProfondeur(materiau, simba.getProfondeur());
                
                if (reparation == null) {
                    showError(String.format("Aucun intervalle de réparation trouvé pour le simba PK %.1f km\n" +
                        "Profondeur: %.2f m avec le matériau: %s", 
                        simba.getPk(), simba.getProfondeur(), materiau));
                    return;
                }
                
                double cout = simba.getSurface() * reparation.getPrixParM2();
                
                // Ajouter au tableau
                Object[] row = new Object[7];
                row[0] = simba.getLalanaNom();
                row[1] = String.format("%.1f", simba.getPk());
                row[2] = String.format("%.2f", simba.getSurface());
                row[3] = String.format("%.2f", simba.getProfondeur());
                row[4] = materiau;
                row[5] = String.format("%.0f", reparation.getPrixParM2());
                row[6] = String.format("%.0f", cout);
                
                tableModel.addRow(row);
                
                // Calculer les totaux
                String nomChemin = simba.getLalanaNom();
                coutsParChemin.put(nomChemin, coutsParChemin.getOrDefault(nomChemin, 0.0) + cout);
                coutTotalTrajet += cout;
            }
            
            // Calculer le coût du chemin sélectionné
            if (cheminSelectionne.equals("Tous les chemins du trajet")) {
                coutCheminSelectionne = coutTotalTrajet;
            } else {
                coutCheminSelectionne = coutsParChemin.getOrDefault(cheminSelectionne, 0.0);
            }
            
            // Mettre à jour les labels
            // if (cheminSelectionne.equals("Tous les chemins du trajet")) {
            //     lblCoutChemin.setText(String.format("Cout total de tous les chemins: %.0f Ar", coutTotalTrajet));
            //     lblCoutTrajet.setText(String.format("Cout total du trajet: %.0f Ar", coutTotalTrajet));
            // } else {
            //     lblCoutChemin.setText(String.format("Cout du chemin '%s': %.0f Ar", cheminSelectionne, coutCheminSelectionne));
            //     lblCoutTrajet.setText(String.format("Cout total du trajet: %.0f Ar", coutTotalTrajet));
            // }
            

            lblCoutTrajet.setText(String.format("Cout total du trajet: %.0f Ar", coutTotalTrajet));
            showInfo("Calcul effectué avec succès!");
            
        } catch (SQLException e) {
            showError("Erreur lors du calcul: " + e.getMessage());
        }
    }

    private void trierParPrix(boolean ascendant) {
        if (tableModel.getRowCount() == 0) {
            showError("Aucune donnée à trier. Veuillez d'abord calculer les coûts.");
            return;
        }
        
        // Récupérer toutes les lignes
        List<Object[]> lignes = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object[] ligne = new Object[7];
            for (int j = 0; j < 7; j++) {
                ligne[j] = tableModel.getValueAt(i, j);
            }
            lignes.add(ligne);
        }
        
        // Trier par coût total (colonne 6)
        lignes.sort((ligne1, ligne2) -> {
            try {
                String cout1Str = ((String) ligne1[6]).replace(" ", "");
                String cout2Str = ((String) ligne2[6]).replace(" ", "");
                double cout1 = Double.parseDouble(cout1Str);
                double cout2 = Double.parseDouble(cout2Str);
                
                if (ascendant) {
                    return Double.compare(cout1, cout2);
                } else {
                    return Double.compare(cout2, cout1);
                }
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Réinsérer les lignes triées
        tableModel.setRowCount(0);
        for (Object[] ligne : lignes) {
            tableModel.addRow(ligne);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}