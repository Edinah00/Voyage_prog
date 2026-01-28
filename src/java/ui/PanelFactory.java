package src.java.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.java.models.CheminItem;
import src.java.models.Voiture;

public class PanelFactory {

    public static JPanel creerPanelHaut(
            JComboBox<String> cbDepart,
            JComboBox<String> cbArrivee,
            JComboBox<Voiture> cbVoiture,
            JTextField txtVitesseMoyenne,
            JTextField txtHeureDepart,
            JButton btnRechercher,
            JButton btnGererLavaka,
            JButton btnGererPause,
            JButton btnGererReparation,
            JButton btnGererSimba,
            JButton btnCalculerCout,
            JButton btnGererPluviometrie,      // NOUVEAU
            JButton btnGererIntervalles,       // NOUVEAU
            JButton btnReinitialiser,
            Runnable onVoitureChanged,
            Runnable onReinitialiser) {

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // BLOC GAUCHE - Configuration du voyage
        JPanel blocGauche = new JPanel(new GridBagLayout());
        blocGauche.setBorder(BorderFactory.createTitledBorder("Configuration du Voyage"));
        blocGauche.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Depart
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        blocGauche.add(new JLabel("Depart:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        blocGauche.add(cbDepart, gbc);

        // Arrivee
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        blocGauche.add(new JLabel("Arrivee:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        blocGauche.add(cbArrivee, gbc);

        // Voiture
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        blocGauche.add(new JLabel("Voiture:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbVoiture.addActionListener(e -> onVoitureChanged.run());
        blocGauche.add(cbVoiture, gbc);

        // Vitesse moyenne
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        blocGauche.add(new JLabel("Vitesse moy. (km/h):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        blocGauche.add(txtVitesseMoyenne, gbc);

        // Heure depart
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        blocGauche.add(new JLabel("Heure depart (HH:mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        blocGauche.add(txtHeureDepart, gbc);

        // Boutons Rechercher et Reinitialiser
        JPanel btnPanelGauche = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanelGauche.setBackground(Color.WHITE);
        btnRechercher.setBackground(Color.BLACK);
        btnRechercher.setForeground(Color.WHITE);
        btnRechercher.setFocusPainted(false);
        btnPanelGauche.add(btnRechercher);
        
        btnReinitialiser.setBackground(Color.DARK_GRAY);
        btnReinitialiser.setForeground(Color.WHITE);
        btnReinitialiser.setFocusPainted(false);
        btnPanelGauche.add(btnReinitialiser);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        blocGauche.add(btnPanelGauche, gbc);

        // BLOC DROIT - Gestion des données
        JPanel blocDroit = new JPanel(new GridBagLayout());
        blocDroit.setBorder(BorderFactory.createTitledBorder("Gestion des Donnees"));
        blocDroit.setBackground(Color.WHITE);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;

        // STYLE NOIR ET BLANC
        btnGererLavaka.setBackground(Color.BLACK);
        btnGererLavaka.setForeground(Color.WHITE);
        btnGererLavaka.setFocusPainted(false);
        
        btnGererPause.setBackground(Color.BLACK);
        btnGererPause.setForeground(Color.WHITE);
        btnGererPause.setFocusPainted(false);
        
        btnGererReparation.setBackground(Color.BLACK);
        btnGererReparation.setForeground(Color.WHITE);
        btnGererReparation.setFocusPainted(false);
        
        btnGererSimba.setBackground(Color.BLACK);
        btnGererSimba.setForeground(Color.WHITE);
        btnGererSimba.setFocusPainted(false);
        
        btnCalculerCout.setBackground(Color.BLACK);
        btnCalculerCout.setForeground(Color.WHITE);
        btnCalculerCout.setFocusPainted(false);

        // NOUVEAUX BOUTONS - Style noir et blanc
        btnGererPluviometrie.setBackground(Color.BLACK);
        btnGererPluviometrie.setForeground(Color.WHITE);
        btnGererPluviometrie.setFocusPainted(false);

        btnGererIntervalles.setBackground(Color.BLACK);
        btnGererIntervalles.setForeground(Color.WHITE);
        btnGererIntervalles.setFocusPainted(false);

        // Ajout des boutons
        gbc2.gridx = 0; gbc2.gridy = 0;
        blocDroit.add(btnGererLavaka, gbc2);

        gbc2.gridy = 1;
        blocDroit.add(btnGererPause, gbc2);

        gbc2.gridy = 2;
        blocDroit.add(btnGererReparation, gbc2);

        gbc2.gridy = 3;
        blocDroit.add(btnGererSimba, gbc2);

        gbc2.gridy = 4;
        blocDroit.add(btnCalculerCout, gbc2);

        // NOUVEAUX BOUTONS
        gbc2.gridy = 5;
        blocDroit.add(btnGererPluviometrie, gbc2);

        gbc2.gridy = 6;
        blocDroit.add(btnGererIntervalles, gbc2);

        // Ajouter un espace vide pour aligner
        gbc2.gridy = 7;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        blocDroit.add(new JPanel(), gbc2);

        // Ajout des deux blocs au panneau principal
        mainPanel.add(blocGauche);
        mainPanel.add(blocDroit);

        return mainPanel;
    }

    public static JPanel creerPanelGauche(
            DefaultListModel<CheminItem> listModel,
            JList<CheminItem> listChemins,
            JButton btnDemarrer,
            JButton btnArreter,
            JLabel lblHeureActuelle,
            JLabel lblHeureArrivee,
            JLabel lblTemps,
            JLabel lblPosition,
            JLabel lblVitesse,
            JLabel lblCarburant,
            Runnable onSelectionChanged) {

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBorder(new EmptyBorder(0, 0, 0, 10));
        panel.setBackground(Color.WHITE);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Chemins disponibles"));
        listPanel.setBackground(Color.WHITE);

        listChemins.setCellRenderer(new CheminCellRenderer());
        listChemins.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectionChanged.run();
            }
        });
        listChemins.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listChemins);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        listPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        btnPanel.setBackground(Color.WHITE);
        
        // STYLE NOIR ET BLANC
        btnDemarrer.setBackground(Color.BLACK);
        btnDemarrer.setForeground(Color.WHITE);
        btnDemarrer.setFocusPainted(false);
        
        btnArreter.setBackground(Color.DARK_GRAY);
        btnArreter.setForeground(Color.WHITE);
        btnArreter.setFocusPainted(false);
        btnArreter.setEnabled(false);
        
        btnPanel.add(btnDemarrer);
        btnPanel.add(btnArreter);
        listPanel.add(btnPanel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel(new GridLayout(8, 1, 0, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informations du voyage"));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.add(lblHeureActuelle);
        infoPanel.add(lblHeureArrivee);
        infoPanel.add(lblTemps);
        infoPanel.add(lblPosition);
        infoPanel.add(lblVitesse);
        infoPanel.add(lblCarburant);

        panel.add(listPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }
}