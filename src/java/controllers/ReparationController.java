package controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.ReparationDAO;
import models.Reparation;

public class ReparationController extends JDialog {

    private JTextField txtMateriau;
    private JTextField txtProfondeurMin;
    private JTextField txtProfondeurMax;
    private JTextField txtPrix;
    private JButton btnAjouter;
    private JButton btnAnnuler;
    private JList<String> listReparations;
    private DefaultListModel<String> listModel;
    private JButton btnSupprimer;

    private ReparationDAO reparationDAO;
    private List<Reparation> reparations;

    public ReparationController(Frame parent) {
        super(parent, "Gestion des Regles de Reparation", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        reparationDAO = new ReparationDAO();
        
        initComponents();
        chargerDonnees();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter une Regle de Reparation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Materiau:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtMateriau = new JTextField();
        formPanel.add(txtMateriau, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur min (m):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProfondeurMin = new JTextField();
        formPanel.add(txtProfondeurMin, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur max (m):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProfondeurMax = new JTextField();
        formPanel.add(txtProfondeurMax, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Prix par m2 (Ar):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPrix = new JTextField();
        formPanel.add(txtPrix, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAjouter = new JButton("Ajouter");
        btnAjouter.setBackground(new Color(76, 175, 80));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterReparation());
        
        btnAnnuler = new JButton("Fermer");
        btnAnnuler.addActionListener(e -> dispose());
        
        btnPanel.add(btnAjouter);
        btnPanel.add(btnAnnuler);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        JPanel listePanel = new JPanel(new BorderLayout(5, 5));
        listePanel.setBorder(BorderFactory.createTitledBorder("Regles de reparation existantes"));
        
        listModel = new DefaultListModel<>();
        listReparations = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listReparations);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        btnSupprimer = new JButton("Supprimer selectionne");
        btnSupprimer.setBackground(new Color(244, 67, 54));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.addActionListener(e -> supprimerReparation());
        
        listePanel.add(scrollPane, BorderLayout.CENTER);
        listePanel.add(btnSupprimer, BorderLayout.SOUTH);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(listePanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void chargerDonnees() {
        try {
            reparations = reparationDAO.findAll();
            afficherReparations();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des reparations: " + e.getMessage());
        }
    }

    private void afficherReparations() {
        listModel.clear();
        if (reparations.isEmpty()) {
            listModel.addElement("Aucune regle de reparation");
        } else {
            for (Reparation rep : reparations) {
                listModel.addElement(rep.toString());
            }
        }
    }

    private void ajouterReparation() {
        try {
            String materiau = txtMateriau.getText().trim();
            double profMin = Double.parseDouble(txtProfondeurMin.getText().trim());
            double profMax = Double.parseDouble(txtProfondeurMax.getText().trim());
            double prix = Double.parseDouble(txtPrix.getText().trim());

            if (materiau.isEmpty()) {
                showError("Veuillez entrer un materiau");
                return;
            }

            if (profMin < 0 || profMax < 0) {
                showError("Les profondeurs doivent etre positives");
                return;
            }

            if (profMin >= profMax) {
                showError("La profondeur min doit etre inferieure a la profondeur max");
                return;
            }

            if (prix <= 0) {
                showError("Le prix doit etre positif");
                return;
            }

            Reparation reparation = new Reparation(materiau, profMin, profMax, prix);
            reparationDAO.insert(reparation);

            showInfo("Regle de reparation ajoutee avec succes!");
            
            txtMateriau.setText("");
            txtProfondeurMin.setText("");
            txtProfondeurMax.setText("");
            txtPrix.setText("");
            
            chargerDonnees();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numeriques valides");
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void supprimerReparation() {
        int selectedIndex = listReparations.getSelectedIndex();
        
        if (selectedIndex < 0) {
            showError("Veuillez selectionner une regle a supprimer");
            return;
        }

        try {
            if (selectedIndex < reparations.size()) {
                Reparation rep = reparations.get(selectedIndex);
                
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous vraiment supprimer cette regle?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    reparationDAO.delete(rep.getId());
                    showInfo("Regle supprimee avec succes!");
                    chargerDonnees();
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}