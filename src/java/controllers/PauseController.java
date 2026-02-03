package controllers;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.LalanaDAO;
import dao.PauseDAO;
import models.Lalana;
import models.Pause;

public class PauseController extends JDialog {

    private JComboBox<String> cbLalana;
    private JTextField txtPosition;
    private JTextField txtHeureDebut;
    private JTextField txtHeureFin;
    private JButton btnAjouter;
    private JButton btnAnnuler;
    private JList<String> listPauses;
    private DefaultListModel<String> listModel;
    private JButton btnSupprimer;

    private boolean cheminsFiltres = false;

    private PauseDAO pauseDAO;
    private LalanaDAO lalanaDAO;
    private List<Lalana> lalanas;

    public PauseController(Frame parent) {
        super(parent, "Gestion des Pauses", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        pauseDAO = new PauseDAO();
        lalanaDAO = new LalanaDAO();
        
        initComponents();
        chargerDonnees();
    }

    public PauseController(Frame parent, List<Lalana> cheminsSelectionnes) {
        super(parent, "Gestion des Pauses", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        
        pauseDAO = new PauseDAO();
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter une Pause"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Chemin (Lalana):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbLalana = new JComboBox<>();
        cbLalana.addActionListener(e -> afficherPausesExistantes());
        formPanel.add(cbLalana, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Position (km):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPosition = new JTextField();
        formPanel.add(txtPosition, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Heure debut (HH:mm):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtHeureDebut = new JTextField("10:00");
        txtHeureDebut.setToolTipText("Format: HH:mm (ex: 10:00)");
        formPanel.add(txtHeureDebut, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Heure fin (HH:mm):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtHeureFin = new JTextField("11:00");
        txtHeureFin.setToolTipText("Format: HH:mm (ex: 11:00)");
        formPanel.add(txtHeureFin, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAjouter = new JButton("Ajouter");
        btnAjouter.setBackground(new Color(76, 175, 80));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.addActionListener(e -> ajouterPause());
        
        btnAnnuler = new JButton("Fermer");
        btnAnnuler.addActionListener(e -> dispose());
        
        btnPanel.add(btnAjouter);
        btnPanel.add(btnAnnuler);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        JPanel listePanel = new JPanel(new BorderLayout(5, 5));
        listePanel.setBorder(BorderFactory.createTitledBorder("Pauses existantes sur le chemin selectionne"));
        
        listModel = new DefaultListModel<>();
        listPauses = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listPauses);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        btnSupprimer = new JButton("Supprimer selectionne");
        btnSupprimer.setBackground(new Color(244, 67, 54));
        btnSupprimer.setForeground(Color.WHITE);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.addActionListener(e -> supprimerPause());
        
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
                afficherPausesExistantes();
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des chemins: " + e.getMessage());
        }
    }

    private void afficherPausesExistantes() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) return;

        listModel.clear();
        try {
            List<Pause> pauses = pauseDAO.findByLalana(nomLalana);
            if (pauses.isEmpty()) {
                listModel.addElement("Aucune pause sur ce chemin");
            } else {
                for (Pause pause : pauses) {
                    listModel.addElement(String.format(
                        "Position: %.1f km | De %s a %s",
                        pause.getPosition(), 
                        pause.getHeureDebutFormatee(), 
                        pause.getHeureFinFormatee()
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des pauses: " + e.getMessage());
        }
    }

    private void chargerDonneesCheminsFiltres() {
        cbLalana.removeAllItems();
        for (Lalana lalana : lalanas) {
            cbLalana.addItem(lalana.getNom());
        }
        if (cbLalana.getItemCount() > 0) {
            afficherPausesExistantes();
        }
    }

    private void ajouterPause() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        if (nomLalana == null) {
            showError("Veuillez selectionner un chemin");
            return;
        }

        try {
            double position = Double.parseDouble(txtPosition.getText().trim());
            LocalTime debut = LocalTime.parse(txtHeureDebut.getText().trim(), 
                                             DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime fin = LocalTime.parse(txtHeureFin.getText().trim(), 
                                           DateTimeFormatter.ofPattern("HH:mm"));

            if (position < 0) {
                showError("La position doit etre positive");
                return;
            }

            if (debut.isAfter(fin) || debut.equals(fin)) {
                showError("L'heure de debut doit etre avant l'heure de fin");
                return;
            }

            Lalana lalana = null;
            for (Lalana l : lalanas) {
                if (l.getNom().equals(nomLalana)) {
                    lalana = l;
                    break;
                }
            }
            if (lalana != null && position > lalana.getDistance()) {
                showError(String.format(
                    "La pause depasse la longueur du chemin (%.1f km)", 
                    lalana.getDistance()
                ));
                return;
            }

            Pause pause = new Pause(position, debut, fin);
            pauseDAO.insert(pause, nomLalana);

            showInfo("Pause ajoutee avec succes!");
            
            txtPosition.setText("");
            txtHeureDebut.setText("10:00");
            txtHeureFin.setText("11:00");
            
            afficherPausesExistantes();

        } catch (NumberFormatException e) {
            showError("Veuillez entrer une position valide");
        } catch (DateTimeParseException e) {
            showError("Format d'heure invalide. Utilisez HH:mm (ex: 10:00)");
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout de la pause: " + e.getMessage());
        }
    }

    private void supprimerPause() {
        String nomLalana = (String) cbLalana.getSelectedItem();
        int selectedIndex = listPauses.getSelectedIndex();
        
        if (nomLalana == null || selectedIndex < 0) {
            showError("Veuillez selectionner une pause a supprimer");
            return;
        }

        try {
            List<Pause> pauses = pauseDAO.findByLalana(nomLalana);
            if (selectedIndex < pauses.size()) {
                Pause pause = pauses.get(selectedIndex);
                
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous vraiment supprimer cette pause?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    pauseDAO.delete(pause.getId());
                    showInfo("Pause supprimee avec succes!");
                    
                    for (Lalana l : lalanas) {
                        l.getPauses().clear();  
                    }
                    pauseDAO.chargerPausesPourLalanas(lalanas);
                    
                    afficherPausesExistantes();
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