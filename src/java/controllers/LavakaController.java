package src.java.controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.java.dao.LalanaDAO;
import src.java.dao.LavakaDAO;
import src.java.models.Lalana;
import src.java.models.Lavaka;

public class LavakaController extends JDialog {

    private JComboBox<String> cbLalana;
    private JTextField txtDebut;
    private JTextField txtFin;
    private JTextField txtRalentissement;
    private JButton btnAjouter;
    private JButton btnAnnuler;
    private JList<String> listLavakas;
    private DefaultListModel<String> listModel;
    private JButton btnSupprimer;

    private LavakaDAO lavakaDAO;
    private LalanaDAO lalanaDAO;
    private List<Lalana> lalanas;
    private boolean cheminsFiltres = false;  // NOUVEAU

    // Constructeur original (tous les chemins)
    public LavakaController(Frame parent) {
        super(parent, "Gestion des Lavaka (Obstacles)", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        lavakaDAO = new LavakaDAO();
        lalanaDAO = new LalanaDAO();
        
        initComponents();
        chargerDonnees();
    }

    // NOUVEAU : Constructeur avec chemins filtrés
    public LavakaController(Frame parent, List<Lalana> cheminsSelectionnes) {
        super(parent, "Gestion des Lavaka (Obstacles)", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        lavakaDAO = new LavakaDAO();
        lalanaDAO = new LalanaDAO();
        this.lalanas = cheminsSelectionnes;
        this.cheminsFiltres = true;
        
        initComponents();
        chargerDonneesCheminsFiltres();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un Lavaka"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Chemin (Lalana):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbLalana = new JComboBox<>();
        cbLalana.addActionListener(e -> afficherLavakasExistants());
        formPanel.add(cbLalana, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Position debut (km):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDebut = new JTextField();
        formPanel.add(txtDebut, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Position fin (km):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFin = new JTextField();
        formPanel.add(txtFin, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Ralentissement (0-1):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtRalentissement = new JTextField();
        txtRalentissement.setToolTipText("Ex: 0.30 pour 30% de reduction de vitesse");
        formPanel.add(txtRalentissement, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAjouter = new JButton("Ajouter");
        btnAjouter.setBackground(new Color(76, 175, 80));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterLavaka());
        
        btnAnnuler = new JButton("Fermer");
        btnAnnuler.addActionListener(e -> dispose());
        
        btnPanel.add(btnAjouter);
        btnPanel.add(btnAnnuler);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        JPanel listePanel = new JPanel(new BorderLayout(5, 5));
        listePanel.setBorder(BorderFactory.createTitledBorder("Lavaka existants sur le chemin selectionne"));
        
        listModel = new DefaultListModel<>();
        listLavakas = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listLavakas);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        btnSupprimer = new JButton("Supprimer selectionne");
        btnSupprimer.setBackground(new Color(244, 67, 54));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.addActionListener(e -> supprimerLavaka());
        
        listePanel.add(scrollPane, BorderLayout.CENTER);
        listePanel.add(btnSupprimer, BorderLayout.SOUTH);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(listePanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void chargerDonnees() {
        try {
            lalanas = lalanaDAO.findAll();
            cbLalana.removeAllItems();
            for (Lalana lalana : lalanas) {
                cbLalana.addItem(lalana.getNom());
            }
            if (cbLalana.getItemCount() > 0) {
                afficherLavakasExistants();
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des chemins: " + e.getMessage());
        }
    }

    // NOUVELLE MÉTHODE : Charger uniquement les chemins filtrés
    private void chargerDonneesCheminsFiltres() {
        cbLalana.removeAllItems();
        for (Lalana lalana : lalanas) {
            cbLalana.addItem(lalana.getNom());
        }
        if (cbLalana.getItemCount() > 0) {
            afficherLavakasExistants();
        }
    }

    private void afficherLavakasExistants() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) return;

        listModel.clear();
        try {
            List<Lavaka> lavakas = lavakaDAO.findByLalana(nomLalana);
            if (lavakas.isEmpty()) {
                listModel.addElement("Aucun lavaka sur ce chemin");
            } else {
                for (Lavaka lavaka : lavakas) {
                    listModel.addElement(String.format(
                        "Position: %.1f - %.1f km | Ralentissement: %.0f%%",
                        lavaka.getDebut(), 
                        lavaka.getFin(), 
                        lavaka.getRalentissement() * 100
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des lavakas: " + e.getMessage());
        }
    }

    private void ajouterLavaka() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) {
            showError("Veuillez selectionner un chemin");
            return;
        }

        try {
            double debut = Double.parseDouble(txtDebut.getText().trim());
            double fin = Double.parseDouble(txtFin.getText().trim());
            double ralentissement = Double.parseDouble(txtRalentissement.getText().trim());

            if (debut < 0 || fin < 0) {
                showError("Les positions doivent etre positives");
                return;
            }
            if (debut >= fin) {
                showError("La position de debut doit etre inferieure a la position de fin");
                return;
            }
            if (ralentissement < 0 || ralentissement > 1) {
                showError("Le ralentissement doit etre entre 0 et 1 (ex: 0.30 pour 30%)");
                return;
            }

            Lalana lalana = null;
            for (Lalana l : lalanas) {
                if (l.getNom().equals(nomLalana)) {
                    lalana = l;
                    break;
                }
            }
            if (lalana != null && fin > lalana.getDistance()) {
                showError(String.format(
                    "Le lavaka depasse la longueur du chemin (%.1f km)", 
                    lalana.getDistance()
                ));
                return;
            }

            Lavaka lavaka = new Lavaka(debut, fin, ralentissement);
            lavakaDAO.insert(lavaka, nomLalana);

            showInfo("Lavaka ajoute avec succes!");
            
            txtDebut.setText("");
            txtFin.setText("");
            txtRalentissement.setText("");
            
            afficherLavakasExistants();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numeriques valides");
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout du lavaka: " + e.getMessage());
        }
    }

    private void supprimerLavaka() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        int selectedIndex = listLavakas.getSelectedIndex();
        
        if (nomLalana == null || selectedIndex < 0) {
            showError("Veuillez selectionner un lavaka a supprimer");
            return;
        }

        try {
            List<Lavaka> lavakas = lavakaDAO.findByLalana(nomLalana);
            if (selectedIndex < lavakas.size()) {
                Lavaka lavaka = lavakas.get(selectedIndex);
                
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous vraiment supprimer ce lavaka?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    lavakaDAO.delete(lavaka.getDebut(), nomLalana);
                    showInfo("Lavaka supprime avec succes!");
                    
                    // Nettoyer et recharger
                    for (Lalana l : lalanas) {
                        l.getLavakas().clear();
                    }
                    lavakaDAO.chargerLavakasPourLalanas(lalanas);
                    
                    afficherLavakasExistants();
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