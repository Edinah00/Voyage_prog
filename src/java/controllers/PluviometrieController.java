package src.java.controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import src.java.dao.LalanaDAO;
import src.java.dao.PluviometrieDAO;
import src.java.models.Lalana;
import src.java.models.Pluviometrie;

/**
 * Contrôleur pour gérer l'ajout multiple de zones de pluviométrie
 */
public class PluviometrieController extends JDialog {

    private JComboBox<String> cbLalana;
    private JTextField txtDebut;
    private JTextField txtFin;
    private JTextField txtQuantitePluie;
    private JButton btnAjouterLigne;
    private JButton btnValiderTout;
    private JButton btnFermer;
    
    private JTable tableTemp;
    private DefaultTableModel tableModel;
    private List<Pluviometrie> pluviometriesEnAttente;
    
    private PluviometrieDAO pluviometrieDAO;
    private LalanaDAO lalanaDAO;
    private List<Lalana> lalanas;

    public PluviometrieController(Frame parent) {
        super(parent, "Ajouter Pluviométrie", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        pluviometrieDAO = new PluviometrieDAO();
        lalanaDAO = new LalanaDAO();
        pluviometriesEnAttente = new ArrayList<>();
        
        initComponents();
        chargerDonnees();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Panel formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter une zone de pluviométrie"));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Route:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbLalana = new JComboBox<>();
        formPanel.add(cbLalana, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("PK Début (km):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDebut = new JTextField();
        formPanel.add(txtDebut, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("PK Fin (km):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFin = new JTextField();
        formPanel.add(txtFin, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantité pluie (mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtQuantitePluie = new JTextField();
        formPanel.add(txtQuantitePluie, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        btnAjouterLigne = new JButton("+ Ajouter à la liste");
        btnAjouterLigne.setBackground(Color.BLACK);
        btnAjouterLigne.setForeground(Color.WHITE);
        btnAjouterLigne.setFocusPainted(false);
        btnAjouterLigne.addActionListener(e -> ajouterLigne());
        formPanel.add(btnAjouterLigne, gbc);

        // Tableau temporaire
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Zones en attente de validation"));
        tablePanel.setBackground(Color.WHITE);
        
        String[] colonnes = {"Route", "PK Début (km)", "PK Fin (km)", "Pluie (mm)", "Action"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Seule la colonne Action est éditable
            }
        };
        
        tableTemp = new JTable(tableModel);
        tableTemp.setRowHeight(30);
        tableTemp.setBackground(Color.WHITE);
        tableTemp.getTableHeader().setBackground(Color.BLACK);
        tableTemp.getTableHeader().setForeground(Color.WHITE);
        
        // Bouton supprimer dans le tableau
        tableTemp.getColumn("Action").setCellRenderer(new ButtonRenderer());
        tableTemp.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollTable = new JScrollPane(tableTemp);
        tablePanel.add(scrollTable, BorderLayout.CENTER);

        // Panel boutons bas
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        
        btnValiderTout = new JButton("✓ Valider tout");
        btnValiderTout.setBackground(Color.BLACK);
        btnValiderTout.setForeground(Color.WHITE);
        btnValiderTout.setFont(new Font("Arial", Font.BOLD, 14));
        btnValiderTout.setFocusPainted(false);
        btnValiderTout.addActionListener(e -> validerTout());
        
        btnFermer = new JButton("Fermer");
        btnFermer.setBackground(Color.LIGHT_GRAY);
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());
        
        btnPanel.add(btnValiderTout);
        btnPanel.add(btnFermer);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void chargerDonnees() {
        try {
            lalanas = lalanaDAO.findAll();
            cbLalana.removeAllItems();
            for (Lalana lalana : lalanas) {
                cbLalana.addItem(lalana.getNom());
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des routes: " + e.getMessage());
        }
    }

    private void ajouterLigne() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) {
            showError("Veuillez sélectionner une route");
            return;
        }

        try {
            double debut = Double.parseDouble(txtDebut.getText().trim());
            double fin = Double.parseDouble(txtFin.getText().trim());
            double quantitePluie = Double.parseDouble(txtQuantitePluie.getText().trim());

            if (debut < 0 || fin < 0 || quantitePluie < 0) {
                showError("Toutes les valeurs doivent être positives");
                return;
            }
            if (debut >= fin) {
                showError("Le PK début doit être inférieur au PK fin");
                return;
            }

            Pluviometrie pluvio = new Pluviometrie(nomLalana, debut, fin, quantitePluie);
            pluviometriesEnAttente.add(pluvio);

            Object[] row = {nomLalana, 
                           String.format("%.1f", debut),
                           String.format("%.1f", fin),
                           String.format("%.1f", quantitePluie),
                           "Supprimer"};
            tableModel.addRow(row);

            // Réinitialiser les champs
            txtDebut.setText("");
            txtFin.setText("");
            txtQuantitePluie.setText("");
            txtDebut.requestFocus();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides");
        }
    }

    private void supprimerLigne(int row) {
        if (row >= 0 && row < pluviometriesEnAttente.size()) {
            pluviometriesEnAttente.remove(row);
            tableModel.removeRow(row);
        }
    }

    private void validerTout() {
        if (pluviometriesEnAttente.isEmpty()) {
            showError("Aucune zone à valider");
            return;
        }

        try {
            pluviometrieDAO.insertMultiple(pluviometriesEnAttente);
            showInfo(pluviometriesEnAttente.size() + " zone(s) de pluviométrie ajoutée(s) avec succès!");
            
            pluviometriesEnAttente.clear();
            tableModel.setRowCount(0);
            
        } catch (SQLException e) {
            showError("Erreur lors de la validation: " + e.getMessage());
        }
    }

    // Classes pour le bouton dans le tableau
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(244, 67, 54));
            setForeground(Color.WHITE);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Supprimer" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(244, 67, 54));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                fireEditingStopped();
                supprimerLigne(currentRow);
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText("Supprimer");
            return button;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}