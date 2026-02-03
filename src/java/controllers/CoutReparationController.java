package controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.ReparationDAO;
import dao.SimbaDAO;
import models.Lalana;
import models.Reparation;
import models.Simba;
import services.ReparationSimbaService;
import services.MateriauAutomatiqueService;
import services.MateriauAutomatiqueService.MateriauNotFoundException;

/**
 * Contrôleur pour calculer automatiquement le coût de réparation des Simba
 * avec possibilité de filtrer par plage kilométrique
 */
public class CoutReparationController extends JDialog {

    private JComboBox<String> cbChemin;
    private JButton btnCalculer;
    private JButton btnFermer;
    private JButton btnTrierAsc;
    private JButton btnTrierDesc;
    private JButton btnReinitialiser;
    
    // NOUVEAUX COMPOSANTS POUR LE FILTRE
    private JTextField txtPointDebut;
    private JTextField txtPointFin;
    private JButton btnValiderFiltre;
    private JButton btnRetirerFiltre;
    private JLabel lblNbSimbasFiltres;
    private JCheckBox chkActiverFiltre;
    
    private JTable tableResultats;
    private DefaultTableModel tableModel;
    private JTable tableStats;
    private DefaultTableModel statsModel;
    
    private JLabel lblNbSimbas;
    private JLabel lblCoutTotal;
    private JLabel lblCoutMoyen;

    private List<Lalana> chemins;
    private List<Simba> simbasActuels;
    private List<Simba> simbasFiltres;  // NOUVEAU : liste filtrée
    private ReparationSimbaService reparationService;
    private ReparationDAO reparationDAO;
    private SimbaDAO simbaDAO;
    private MateriauAutomatiqueService materiauService;
    
    // NOUVEAUX ATTRIBUTS
    private boolean filtreActif = false;
    private double pointDebut = 0;
    private double pointFin = Double.MAX_VALUE;

    public CoutReparationController(Frame parent, List<Lalana> chemins) {
        super(parent, "Calcul Automatique - Réparation par Pluviométrie", true);
        setSize(1400, 900);
        setLocationRelativeTo(parent);
        
        this.chemins = chemins;
        this.reparationService = new ReparationSimbaService();
        this.reparationDAO = new ReparationDAO();
        this.simbaDAO = new SimbaDAO();
        this.materiauService = new MateriauAutomatiqueService();
        this.simbasActuels = new ArrayList<>();
        this.simbasFiltres = new ArrayList<>();
        
        initComponents();
        chargerDonnees();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // ==================== HEADER ====================
        JPanel headerPanel = createHeaderPanel();
        
        // ==================== CENTER - Tableau principal ====================
        JPanel centerPanel = createCenterPanel();
        
        // ==================== RIGHT - Statistiques et actions ====================
        JPanel rightPanel = createRightPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK),
            new EmptyBorder(0, 0, 15, 0)
        ));

        // Panel principal avec titre et sélection
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Système de Réparation Automatique");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitre.setForeground(Color.BLACK);
        
        JLabel lblSubtitle = new JLabel("Détermination du matériau par analyse pluviométrique");
        lblSubtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        lblSubtitle.setForeground(Color.GRAY);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(lblTitre);
        textPanel.add(lblSubtitle);
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        selectionPanel.setBackground(Color.WHITE);
        
        JLabel lblChemin = new JLabel("Sélectionner le chemin:");
        lblChemin.setFont(new Font("Arial", Font.BOLD, 14));
        selectionPanel.add(lblChemin);
        
        cbChemin = new JComboBox<>();
        cbChemin.setPreferredSize(new Dimension(300, 35));
        cbChemin.setFont(new Font("Arial", Font.PLAIN, 13));
        cbChemin.addActionListener(e -> chargerSimbasDuChemin());
        selectionPanel.add(cbChemin);
        
        topPanel.add(textPanel, BorderLayout.WEST);
        topPanel.add(selectionPanel, BorderLayout.EAST);
        
        // ==================== NOUVEAU PANEL DE FILTRE ====================
        JPanel filtrePanel = createFiltrePanel();
        
        headerPanel.add(topPanel, BorderLayout.NORTH);
        headerPanel.add(filtrePanel, BorderLayout.SOUTH);

        return headerPanel;
    }

    /**
     * NOUVEAU : Création du panel de filtre par plage kilométrique
     */
    private JPanel createFiltrePanel() {
        JPanel filtrePanel = new JPanel(new BorderLayout(10, 10));
        filtrePanel.setBackground(new Color(245, 245, 245));
        filtrePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Panel de gauche : activation du filtre
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(new Color(245, 245, 245));
        
        chkActiverFiltre = new JCheckBox("Activer le filtre par plage kilométrique");
        chkActiverFiltre.setFont(new Font("Arial", Font.BOLD, 13));
        chkActiverFiltre.setBackground(new Color(245, 245, 245));
        chkActiverFiltre.addActionListener(e -> toggleFiltre());
        leftPanel.add(chkActiverFiltre);

        // Panel central : champs de saisie
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        centerPanel.setBackground(new Color(245, 245, 245));
        
        JLabel lblDebut = new JLabel("PK Début (km):");
        lblDebut.setFont(new Font("Arial", Font.PLAIN, 12));
        centerPanel.add(lblDebut);
        
        txtPointDebut = new JTextField(8);
        txtPointDebut.setFont(new Font("Arial", Font.PLAIN, 12));
        txtPointDebut.setEnabled(false);
        centerPanel.add(txtPointDebut);
        
        JLabel lblFin = new JLabel("PK Fin (km):");
        lblFin.setFont(new Font("Arial", Font.PLAIN, 12));
        centerPanel.add(lblFin);
        
        txtPointFin = new JTextField(8);
        txtPointFin.setFont(new Font("Arial", Font.PLAIN, 12));
        txtPointFin.setEnabled(false);
        centerPanel.add(txtPointFin);

        // Panel de droite : boutons d'action
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(new Color(245, 245, 245));
        
        btnValiderFiltre = new JButton("✓ Appliquer");
        btnValiderFiltre.setBackground(Color.BLACK);
        btnValiderFiltre.setForeground(Color.WHITE);
        btnValiderFiltre.setFont(new Font("Arial", Font.BOLD, 12));
        btnValiderFiltre.setFocusPainted(false);
        btnValiderFiltre.setEnabled(false);
        btnValiderFiltre.addActionListener(e -> appliquerFiltre());
        rightPanel.add(btnValiderFiltre);
        
        btnRetirerFiltre = new JButton("✕ Retirer");
        btnRetirerFiltre.setBackground(Color.DARK_GRAY);
        btnRetirerFiltre.setForeground(Color.WHITE);
        btnRetirerFiltre.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRetirerFiltre.setFocusPainted(false);
        btnRetirerFiltre.setEnabled(false);
        btnRetirerFiltre.addActionListener(e -> retirerFiltre());
        rightPanel.add(btnRetirerFiltre);
        
        lblNbSimbasFiltres = new JLabel("");
        lblNbSimbasFiltres.setFont(new Font("Arial", Font.BOLD, 12));
        lblNbSimbasFiltres.setForeground(new Color(0, 100, 0));
        rightPanel.add(lblNbSimbasFiltres);

        filtrePanel.add(leftPanel, BorderLayout.WEST);
        filtrePanel.add(centerPanel, BorderLayout.CENTER);
        filtrePanel.add(rightPanel, BorderLayout.EAST);

        return filtrePanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        
        // Barre d'outils du tableau
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(Color.WHITE);
        
        // Info automatique
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(245, 245, 245));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(8, 10, 8, 10)
        ));
        
        JLabel iconLabel = new JLabel("⚡");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(iconLabel);
        
        JLabel lblInfo = new JLabel("Les matériaux sont déterminés automatiquement selon la pluviométrie de chaque zone");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(60, 60, 60));
        infoPanel.add(lblInfo);
        
        // Boutons de tri
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        sortPanel.setBackground(Color.WHITE);
        
        JLabel lblTri = new JLabel("Trier:");
        lblTri.setFont(new Font("Arial", Font.BOLD, 12));
        sortPanel.add(lblTri);
        
        btnTrierAsc = new JButton("↑ Prix Croissant");
        btnTrierAsc.setBackground(Color.WHITE);
        btnTrierAsc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            new EmptyBorder(5, 15, 5, 15)
        ));
        btnTrierAsc.setFocusPainted(false);
        btnTrierAsc.addActionListener(e -> trierParPrix(true));
        
        btnTrierDesc = new JButton("↓ Prix Décroissant");
        btnTrierDesc.setBackground(Color.WHITE);
        btnTrierDesc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            new EmptyBorder(5, 15, 5, 15)
        ));
        btnTrierDesc.setFocusPainted(false);
        btnTrierDesc.addActionListener(e -> trierParPrix(false));
        
        sortPanel.add(btnTrierAsc);
        sortPanel.add(btnTrierDesc);
        
        toolbarPanel.add(infoPanel, BorderLayout.WEST);
        toolbarPanel.add(sortPanel, BorderLayout.EAST);
        
        // Tableau des résultats
        String[] colonnes = {"Chemin", "PK", "Surface", "Prof.", "Pluie", "Matériau", "Prix/m²", "Coût Total"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableResultats = new JTable(tableModel);
        tableResultats.setRowHeight(35);
        tableResultats.setFont(new Font("Arial", Font.PLAIN, 13));
        tableResultats.setGridColor(new Color(230, 230, 230));
        tableResultats.setShowVerticalLines(true);
        tableResultats.setShowHorizontalLines(true);
        tableResultats.setBackground(Color.WHITE);
        
        // Header du tableau
        tableResultats.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tableResultats.getTableHeader().setBackground(Color.BLACK);
        tableResultats.getTableHeader().setForeground(Color.WHITE);
        tableResultats.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // Alignement et largeurs
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        tableResultats.getColumnModel().getColumn(0).setPreferredWidth(200);
        tableResultats.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableResultats.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableResultats.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableResultats.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tableResultats.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableResultats.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableResultats.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableResultats.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableResultats.getColumnModel().getColumn(5).setPreferredWidth(120);
        tableResultats.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tableResultats.getColumnModel().getColumn(6).setPreferredWidth(120);
        tableResultats.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableResultats.getColumnModel().getColumn(7).setPreferredWidth(150);
        tableResultats.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        
        JScrollPane scrollTable = new JScrollPane(tableResultats);
        scrollTable.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(scrollTable, BorderLayout.CENTER);
        
        return centerPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setPreferredSize(new Dimension(320, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Panel des actions
        JPanel actionsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        actionsPanel.setBorder(createStyledBorder("Actions"));
        actionsPanel.setBackground(Color.WHITE);
        
        btnCalculer = new JButton("🔄 Calculer Automatiquement");
        btnCalculer.setPreferredSize(new Dimension(0, 45));
        btnCalculer.setBackground(Color.BLACK);
        btnCalculer.setForeground(Color.WHITE);
        btnCalculer.setFont(new Font("Arial", Font.BOLD, 14));
        btnCalculer.setFocusPainted(false);
        btnCalculer.addActionListener(e -> calculerCoutsAutomatique());
        btnCalculer.setEnabled(false);
        
        btnReinitialiser = new JButton("↺ Réinitialiser");
        btnReinitialiser.setPreferredSize(new Dimension(0, 40));
        btnReinitialiser.setBackground(Color.WHITE);
        btnReinitialiser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            new EmptyBorder(5, 10, 5, 10)
        ));
        btnReinitialiser.setFont(new Font("Arial", Font.PLAIN, 13));
        btnReinitialiser.setFocusPainted(false);
        btnReinitialiser.addActionListener(e -> reinitialiser());
        
        btnFermer = new JButton("✕ Fermer");
        btnFermer.setPreferredSize(new Dimension(0, 40));
        btnFermer.setBackground(Color.WHITE);
        btnFermer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(5, 10, 5, 10)
        ));
        btnFermer.setFont(new Font("Arial", Font.PLAIN, 13));
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());
        
        actionsPanel.add(btnCalculer);
        actionsPanel.add(btnReinitialiser);
        actionsPanel.add(btnFermer);

        // Panel des statistiques
        JPanel statsPanel = new JPanel(new BorderLayout(0, 10));
        statsPanel.setBorder(createStyledBorder("Statistiques"));
        statsPanel.setBackground(Color.WHITE);
        
        // Indicateurs
        JPanel indicateursPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        indicateursPanel.setBackground(Color.WHITE);
        indicateursPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        lblNbSimbas = createStatLabel("Nombre de Simbas:", "0");
        lblCoutMoyen = createStatLabel("Coût moyen:", "0 Ar");
        lblCoutTotal = createStatLabel("COÛT TOTAL:", "0 Ar");
        lblCoutTotal.setFont(new Font("Arial", Font.BOLD, 16));
        
        indicateursPanel.add(lblNbSimbas);
        indicateursPanel.add(lblCoutMoyen);
        indicateursPanel.add(lblCoutTotal);
        
        // Tableau récapitulatif par matériau
        String[] colStats = {"Matériau", "Nb", "Total (Ar)"};
        statsModel = new DefaultTableModel(colStats, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableStats = new JTable(statsModel);
        tableStats.setRowHeight(30);
        tableStats.setFont(new Font("Arial", Font.PLAIN, 12));
        tableStats.setBackground(new Color(250, 250, 250));
        tableStats.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tableStats.getTableHeader().setBackground(new Color(220, 220, 220));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableStats.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableStats.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        
        JScrollPane scrollStats = new JScrollPane(tableStats);
        scrollStats.setPreferredSize(new Dimension(0, 200));
        scrollStats.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        statsPanel.add(indicateursPanel, BorderLayout.NORTH);
        statsPanel.add(scrollStats, BorderLayout.CENTER);

        rightPanel.add(actionsPanel, BorderLayout.NORTH);
        rightPanel.add(statsPanel, BorderLayout.CENTER);

        return rightPanel;
    }

    private JLabel createStatLabel(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        lblLabel.setForeground(Color.GRAY);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 14));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(lblLabel, BorderLayout.WEST);
        panel.add(lblValue, BorderLayout.EAST);
        
        return lblValue;
    }

    private TitledBorder createStyledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            title
        );
        border.setTitleFont(new Font("Arial", Font.BOLD, 13));
        return border;
    }

    private void chargerDonnees() {
        try {
            cbChemin.addItem("── Tous les chemins du trajet ──");
            for (Lalana chemin : chemins) {
                cbChemin.addItem(chemin.getNom());
            }
        } catch (Exception e) {
            showError("Erreur lors du chargement: " + e.getMessage());
        }
    }

    private void chargerSimbasDuChemin() {
        String cheminSelectionne = (String) cbChemin.getSelectedItem();
        if (cheminSelectionne == null) return;

        simbasActuels.clear();
        reinitialiser();

        try {
            if (cheminSelectionne.startsWith("──")) {
                for (Lalana lalana : chemins) {
                    List<Simba> simbasLalana = simbaDAO.findByLalana(lalana.getNom());
                    simbasActuels.addAll(simbasLalana);
                }
            } else {
                simbasActuels = simbaDAO.findByLalana(cheminSelectionne);
            }

            btnCalculer.setEnabled(!simbasActuels.isEmpty());
            lblNbSimbas.setText(simbasActuels.size() + " simba(s)");

        } catch (SQLException e) {
            showError("Erreur lors du chargement: " + e.getMessage());
        }
    }

    /**
     * NOUVEAU : Active/désactive le filtre
     */
    private void toggleFiltre() {
        boolean actif = chkActiverFiltre.isSelected();
        txtPointDebut.setEnabled(actif);
        txtPointFin.setEnabled(actif);
        btnValiderFiltre.setEnabled(actif);
        
        if (!actif && filtreActif) {
            // Nettoyer le filtre sans toucher à la checkbox (évite la boucle infinie)
            filtreActif = false;
            pointDebut = 0;
            pointFin = Double.MAX_VALUE;
            simbasFiltres.clear();
            txtPointDebut.setText("");
            txtPointFin.setText("");
            btnRetirerFiltre.setEnabled(false);
            lblNbSimbasFiltres.setText("");
            
            // Recalculer avec la liste complète
            if (!simbasActuels.isEmpty()) {
                calculerCoutsAutomatique();
            }
        }
    }

    /**
     * NOUVEAU : Applique le filtre par plage kilométrique
     */
    private void appliquerFiltre() {
        try {
            // Validation des champs
            if (txtPointDebut.getText().trim().isEmpty() || txtPointFin.getText().trim().isEmpty()) {
                showError("Veuillez saisir les deux points (début et fin)");
                return;
            }

            pointDebut = Double.parseDouble(txtPointDebut.getText().trim());
            pointFin = Double.parseDouble(txtPointFin.getText().trim());

            // Validation de la logique
            if (pointDebut < 0 || pointFin < 0) {
                showError("Les points kilométriques doivent être positifs");
                return;
            }

            if (pointDebut >= pointFin) {
                showError("Le point de début doit être inférieur au point de fin");
                return;
            }

            // Filtrer les Simbas
            simbasFiltres = getSimbaFiltre(simbasActuels, pointDebut, pointFin);

            if (simbasFiltres.isEmpty()) {
                // Activer le filtre même s'il est vide
                filtreActif = true;
                btnRetirerFiltre.setEnabled(true);
                lblNbSimbasFiltres.setText("→ 0 simba dans la plage");
                
                // Vider le tableau et mettre les statistiques à zéro
                tableModel.setRowCount(0);
                statsModel.setRowCount(0);
                lblNbSimbas.setText("0 simba(s)");
                lblCoutTotal.setText("0 Ar");
                lblCoutMoyen.setText("0 Ar");
                
                showInfo("Aucun Simba trouvé dans la plage [" + pointDebut + " - " + pointFin + "] km\n\nCoût total: 0 Ar");
                return;
            }

            // Activer le filtre
            filtreActif = true;
            btnRetirerFiltre.setEnabled(true);
            lblNbSimbasFiltres.setText("→ " + simbasFiltres.size() + " simba(s) dans la plage");
            
            // Recalculer automatiquement avec la liste filtrée
            calculerCoutsAutomatique();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides");
        }
    }

    /**
     * NOUVEAU : Retire le filtre et restaure la liste complète
     */
    private void retirerFiltre() {
        // Désactiver temporairement l'écouteur pour éviter la boucle
        filtreActif = false;
        pointDebut = 0;
        pointFin = Double.MAX_VALUE;
        simbasFiltres.clear();
        txtPointDebut.setText("");
        txtPointFin.setText("");
        btnRetirerFiltre.setEnabled(false);
        lblNbSimbasFiltres.setText("");
        
        // Décocher la checkbox SANS déclencher l'événement
        txtPointDebut.setEnabled(false);
        txtPointFin.setEnabled(false);
        btnValiderFiltre.setEnabled(false);
        chkActiverFiltre.setSelected(false);
        
        // Recalculer avec la liste complète
        if (!simbasActuels.isEmpty()) {
            calculerCoutsAutomatique();
        }
    }

   
    private List<Simba> getSimbaFiltre(List<Simba> simbas, double pkDebut, double pkFin) {
        List<Simba> resultat = new ArrayList<>();
        
        for (Simba simba : simbas) {
            double pk = simba.getPk();
            if (pk >= pkDebut && pk <= pkFin) {
                resultat.add(simba);
            }
        }
        
        return resultat;
    }

    
    private void calculerCoutsAutomatique() {
        try {
            tableModel.setRowCount(0);
            statsModel.setRowCount(0);
            
            // MODIFIÉ : Utiliser la liste filtrée si le filtre est actif
            List<Simba> simbasATraiter = filtreActif ? simbasFiltres : simbasActuels;
            
            double coutTotal = 0;
            int erreursCount = 0;
            StringBuilder erreursDetails = new StringBuilder();
            Map<String, Integer> countByMateriau = new HashMap<>();
            Map<String, Double> coutByMateriau = new HashMap<>();
            
            for (Simba simba : simbasATraiter) {
                try {
                    MateriauAutomatiqueService.DetailMateriauAutomatique detail = 
                        materiauService.obtenirDetailsMateriauPourSimba(simba);
                    
                    String materiau = detail.getMateriau();
                    double quantitePluie = detail.getPluviometrie().getQuantitePluie();
                    
                    Reparation reparation = reparationDAO.findByMateriauAndProfondeur(
                        materiau, simba.getProfondeur());
                    
                    if (reparation == null) {
                        erreursCount++;
                        erreursDetails.append(String.format(
                            "• PK %.1f km: Pas de prix pour %s à %.2f m\n",
                            simba.getPk(), materiau, simba.getProfondeur()));
                        continue;
                    }
                    
                    double cout = simba.getSurface() * reparation.getPrixParM2();
                    
                    Object[] row = {
                        simba.getLalanaNom(),
                        String.format("%.1f km", simba.getPk()),
                        String.format("%.2f m²", simba.getSurface()),
                        String.format("%.2f m", simba.getProfondeur()),
                        String.format("%.0f mm", quantitePluie),
                        materiau,
                        String.format("%,.0f Ar", reparation.getPrixParM2()),
                        String.format("%,.0f Ar", cout)
                    };
                    
                    tableModel.addRow(row);
                    coutTotal += cout;
                    
                    countByMateriau.put(materiau, countByMateriau.getOrDefault(materiau, 0) + 1);
                    coutByMateriau.put(materiau, coutByMateriau.getOrDefault(materiau, 0.0) + cout);
                    
                } catch (MateriauNotFoundException e) {
                    erreursCount++;
                    erreursDetails.append(String.format("• PK %.1f km: %s\n", 
                        simba.getPk(), e.getMessage()));
                }
            }
            
            // Mise à jour des statistiques
            lblNbSimbas.setText(simbasATraiter.size() + " simba(s)");
            lblCoutTotal.setText(String.format("%,.0f Ar", coutTotal));
            lblCoutMoyen.setText(String.format("%,.0f Ar", 
                simbasATraiter.size() > 0 ? coutTotal / simbasATraiter.size() : 0));
            
            // Tableau par matériau
            for (Map.Entry<String, Integer> entry : countByMateriau.entrySet()) {
                String mat = entry.getKey();
                statsModel.addRow(new Object[]{
                    mat,
                    entry.getValue(),
                    String.format("%,.0f", coutByMateriau.get(mat))
                });
            }
            
            // Message de résultat
            String messageFiltre = filtreActif ? 
                String.format("\nPlage filtrée: [%.1f - %.1f] km", pointDebut, pointFin) : "";
            
            if (erreursCount > 0) {
                showWarning("Calcul terminé avec " + erreursCount + " erreur(s):" + messageFiltre + "\n\n" + 
                    erreursDetails.toString());
            } else {
                showInfo("✓ Calcul automatique terminé avec succès!" + messageFiltre + "\n\n" +
                    String.format("%d simba(s) traité(s)\nCoût total: %,.0f Ar", 
                    simbasATraiter.size(), coutTotal));
            }
            
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void trierParPrix(boolean ascendant) {
        if (tableModel.getRowCount() == 0) {
            showError("Aucune donnée à trier.");
            return;
        }
        
        List<Object[]> lignes = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object[] ligne = new Object[8];
            for (int j = 0; j < 8; j++) {
                ligne[j] = tableModel.getValueAt(i, j);
            }
            lignes.add(ligne);
        }
        
        lignes.sort((l1, l2) -> {
            try {
                String s1 = ((String) l1[7]).replaceAll("[^0-9]", "");
                String s2 = ((String) l2[7]).replaceAll("[^0-9]", "");
                double c1 = Double.parseDouble(s1);
                double c2 = Double.parseDouble(s2);
                return ascendant ? Double.compare(c1, c2) : Double.compare(c2, c1);
            } catch (Exception e) {
                return 0;
            }
        });
        
        tableModel.setRowCount(0);
        for (Object[] ligne : lignes) {
            tableModel.addRow(ligne);
        }
    }

    private void reinitialiser() {
        tableModel.setRowCount(0);
        statsModel.setRowCount(0);
        lblNbSimbas.setText("0");
        lblCoutMoyen.setText("0 Ar");
        lblCoutTotal.setText("0 Ar");
        retirerFiltre();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Avertissement", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
}