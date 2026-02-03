package controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import dao.PluviometrieIntervalleDAO;
import models.PluviometrieIntervalle;

/**
 * Contrôleur pour gérer l'ajout multiple d'intervalles de pluviométrie
 */
public class PluviometrieIntervalleController extends JDialog {

    private JTextField txtQuantiteMin;
    private JTextField txtQuantiteMax;
    private JTextField txtMateriau;
    private JButton btnAjouterLigne;
    private JButton btnValiderTout;
    private JButton btnFermer;
    
    private JTable tableTemp;
    private DefaultTableModel tableModel;
    private List<PluviometrieIntervalle> intervallesEnAttente;
    
    private PluviometrieIntervalleDAO intervalleDAO;

    public PluviometrieIntervalleController(Frame parent) {
        super(parent, "Ajouter Intervalles de Pluviométrie", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        intervalleDAO = new PluviometrieIntervalleDAO();
        intervallesEnAttente = new ArrayList<>();
        
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Panel formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Définir un intervalle pluie → matériau"));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Quantité min (mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtQuantiteMin = new JTextField();
        formPanel.add(txtQuantiteMin, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantité max (mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtQuantiteMax = new JTextField();
        formPanel.add(txtQuantiteMax, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Matériau:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtMateriau = new JTextField();
        txtMateriau.setToolTipText("Ex: Béton, Goudron, pave");
        formPanel.add(txtMateriau, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        btnAjouterLigne = new JButton("+ Ajouter à la liste");
        btnAjouterLigne.setBackground(Color.BLACK);
        btnAjouterLigne.setForeground(Color.WHITE);
        btnAjouterLigne.setFocusPainted(false);
        btnAjouterLigne.addActionListener(e -> ajouterLigne());
        formPanel.add(btnAjouterLigne, gbc);

        // Tableau temporaire
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Intervalles en attente de validation"));
        tablePanel.setBackground(Color.WHITE);
        
        String[] colonnes = {"Quantité Min (mm)", "Quantité Max (mm)", "Matériau", "Action"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        
        tableTemp = new JTable(tableModel);
        tableTemp.setRowHeight(30);
        tableTemp.setBackground(Color.WHITE);
        tableTemp.getTableHeader().setBackground(Color.BLACK);
        tableTemp.getTableHeader().setForeground(Color.WHITE);
        
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

    private void ajouterLigne() {
        try {
            double quantiteMin = Double.parseDouble(txtQuantiteMin.getText().trim());
            double quantiteMax = Double.parseDouble(txtQuantiteMax.getText().trim());
            String materiau = txtMateriau.getText().trim();

            if (materiau.isEmpty()) {
                showError("Veuillez entrer un matériau");
                return;
            }
            if (quantiteMin < 0 || quantiteMax < 0) {
                showError("Les quantités doivent être positives");
                return;
            }
            if (quantiteMin >= quantiteMax) {
                showError("La quantité min doit être inférieure à la quantité max");
                return;
            }

            PluviometrieIntervalle intervalle = new PluviometrieIntervalle(quantiteMin, quantiteMax, materiau);
            intervallesEnAttente.add(intervalle);

            Object[] row = {String.format("%.2f", quantiteMin),
                           String.format("%.2f", quantiteMax),
                           materiau,
                           "Supprimer"};
            tableModel.addRow(row);

            txtQuantiteMin.setText("");
            txtQuantiteMax.setText("");
            txtMateriau.setText("");
            txtQuantiteMin.requestFocus();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides");
        }
    }

    private void supprimerLigne(int row) {
        if (row >= 0 && row < intervallesEnAttente.size()) {
            intervallesEnAttente.remove(row);
            tableModel.removeRow(row);
        }
    }

    private void validerTout() {
        if (intervallesEnAttente.isEmpty()) {
            showError("Aucun intervalle à valider");
            return;
        }

        try {
            intervalleDAO.insertMultiple(intervallesEnAttente);
            showInfo(intervallesEnAttente.size() + " intervalle(s) ajouté(s) avec succès!");
            
            intervallesEnAttente.clear();
            tableModel.setRowCount(0);
            
        } catch (SQLException e) {
            showError("Erreur lors de la validation: " + e.getMessage());
        }
    }

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