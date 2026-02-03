package controllers;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.LalanaDAO;
import dao.SimbaDAO;
import models.Lalana;
import models.Simba;

public class SimbaController extends JDialog {

    private JComboBox<String> cbLalana;
    private JTextField txtPk;
    private JTextField txtSurface;
    private JTextField txtProfondeur;
    private JButton btnAjouter;
    private JButton btnAnnuler;
    private JList<String> listSimbas;
    private DefaultListModel<String> listModel;
    private JButton btnSupprimer;

    private SimbaDAO simbaDAO;
    private LalanaDAO lalanaDAO;
    private List<Lalana> lalanas;
    private boolean cheminsFiltres = false;

    public SimbaController(Frame parent) {
        super(parent, "Gestion des Simba (Nids-de-poule)", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        simbaDAO = new SimbaDAO();
        lalanaDAO = new LalanaDAO();
        
        initComponents();
        chargerDonnees();
    }

    public SimbaController(Frame parent, List<Lalana> cheminsSelectionnes) {
        super(parent, "Gestion des Simba (Nids-de-poule)", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        simbaDAO = new SimbaDAO();
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un Simba"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Chemin (Lalana):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbLalana = new JComboBox<>();
        cbLalana.addActionListener(e -> afficherSimbasExistants());
        formPanel.add(cbLalana, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Point kilométrique (km):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPk = new JTextField();
        formPanel.add(txtPk, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Surface (m²):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtSurface = new JTextField();
        formPanel.add(txtSurface, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur (m):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtProfondeur = new JTextField();
        formPanel.add(txtProfondeur, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAjouter = new JButton("Ajouter");
        btnAjouter.setBackground(new Color(76, 175, 80));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterSimba());
        
        btnAnnuler = new JButton("Fermer");
        btnAnnuler.addActionListener(e -> dispose());
        
        btnPanel.add(btnAjouter);
        btnPanel.add(btnAnnuler);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        JPanel listePanel = new JPanel(new BorderLayout(5, 5));
        listePanel.setBorder(BorderFactory.createTitledBorder("Simbas existants sur le chemin sélectionné"));
        
        listModel = new DefaultListModel<>();
        listSimbas = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listSimbas);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        btnSupprimer = new JButton("Supprimer sélectionné");
        btnSupprimer.setBackground(new Color(244, 67, 54));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.addActionListener(e -> supprimerSimba());
        
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
                afficherSimbasExistants();
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void chargerDonneesCheminsFiltres() {
        cbLalana.removeAllItems();
        for (Lalana lalana : lalanas) {
            cbLalana.addItem(lalana.getNom());
        }
        if (cbLalana.getItemCount() > 0) {
            afficherSimbasExistants();
        }
    }

    private void afficherSimbasExistants() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) return;

        listModel.clear();
        try {
            List<Simba> simbas = simbaDAO.findByLalana(nomLalana);
            if (simbas.isEmpty()) {
                listModel.addElement("Aucun simba sur ce chemin");
            } else {
                for (Simba simba : simbas) {
                    listModel.addElement(simba.toString());
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des simbas: " + e.getMessage());
        }
    }

    private void ajouterSimba() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) {
            showError("Veuillez sélectionner un chemin");
            return;
        }

        try {
            double pk = Double.parseDouble(txtPk.getText().trim());
            double surface = Double.parseDouble(txtSurface.getText().trim());
            double profondeur = Double.parseDouble(txtProfondeur.getText().trim());

            if (pk < 0) {
                showError("Le point kilométrique doit être positif");
                return;
            }
            if (surface <= 0) {
                showError("La surface doit être positive");
                return;
            }
            if (profondeur <= 0) {
                showError("La profondeur doit être positive");
                return;
            }

            Lalana lalana = null;
            for (Lalana l : lalanas) {
                if (l.getNom().equals(nomLalana)) {
                    lalana = l;
                    break;
                }
            }
            if (lalana != null && pk > lalana.getDistance()) {
                showError(String.format(
                    "Le PK dépasse la longueur du chemin (%.1f km)", 
                    lalana.getDistance()
                ));
                return;
            }

            Simba simba = new Simba(pk, surface, profondeur);
            simbaDAO.insert(simba, nomLalana);

            showInfo("Simba ajouté avec succès!");
            
            txtPk.setText("");
            txtSurface.setText("");
            txtProfondeur.setText("");
            
            afficherSimbasExistants();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des valeurs numériques valides");
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout du simba: " + e.getMessage());
        }
    }

    private void supprimerSimba() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        int selectedIndex = listSimbas.getSelectedIndex();
        
        if (nomLalana == null || selectedIndex < 0) {
            showError("Veuillez sélectionner un simba à supprimer");
            return;
        }

        try {
            List<Simba> simbas = simbaDAO.findByLalana(nomLalana);
            if (selectedIndex < simbas.size()) {
                Simba simba = simbas.get(selectedIndex);
                
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous vraiment supprimer ce simba?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    simbaDAO.delete(simba.getId());
                    showInfo("Simba supprimé avec succès!");
                    
                    for (Lalana l : lalanas) {
                        l.getSimbas().clear();
                    }
                    simbaDAO.chargerSimbasPourLalanas(lalanas);
                    
                    afficherSimbasExistants();
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