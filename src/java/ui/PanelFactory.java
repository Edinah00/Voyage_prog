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
        JButton btnReinitialiser,  
        Runnable onVoitureChange,

        Runnable onReinitialiser   
    ) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(224, 224, 224));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Simulation de Voyage");
        titre.setFont(new Font("Arial", Font.BOLD, 24));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        selectionPanel.setBackground(new Color(224, 224, 224));

        selectionPanel.add(new JLabel("Depart:"));
        cbDepart.setPreferredSize(new Dimension(80, 25));
        selectionPanel.add(cbDepart);

        selectionPanel.add(new JLabel("Arrivee:"));
        cbArrivee.setPreferredSize(new Dimension(80, 25));
        selectionPanel.add(cbArrivee);

        selectionPanel.add(new JLabel("Voiture:"));
        cbVoiture.setPreferredSize(new Dimension(200, 25));
        cbVoiture.addActionListener(e -> onVoitureChange.run());
        selectionPanel.add(cbVoiture);

        selectionPanel.add(new JLabel("Vitesse moy. (km/h):"));
        txtVitesseMoyenne.setPreferredSize(new Dimension(60, 25));
        selectionPanel.add(txtVitesseMoyenne);

        selectionPanel.add(new JLabel("Heure depart:"));
        txtHeureDepart.setPreferredSize(new Dimension(60, 25));
        txtHeureDepart.setToolTipText("Format: HH:mm (ex: 08:30)");
        selectionPanel.add(txtHeureDepart);

        btnRechercher.setBackground(new Color(120, 120, 120));
        btnRechercher.setForeground(Color.WHITE);
        btnRechercher.setFont(new Font("Arial", Font.BOLD, 12));
        btnRechercher.setFocusPainted(false);
        selectionPanel.add(btnRechercher);

        btnGererLavaka.setBackground(new Color(120, 120, 120));
        btnGererLavaka.setForeground(Color.WHITE);
        btnGererLavaka.setFont(new Font("Arial", Font.BOLD, 12));
        btnGererLavaka.setFocusPainted(false);
        selectionPanel.add(btnGererLavaka);

        btnGererPause.setBackground(new Color(120, 120, 120));
        btnGererPause.setForeground(Color.WHITE);
        btnGererPause.setFont(new Font("Arial", Font.BOLD, 12));
        btnGererPause.setFocusPainted(false);
        selectionPanel.add(btnGererPause);

        btnReinitialiser.setBackground(new Color(120, 120, 120));
        btnReinitialiser.setForeground(Color.WHITE);
        btnReinitialiser.setFont(new Font("Arial", Font.BOLD, 12));
        btnReinitialiser.setFocusPainted(false);
        btnReinitialiser.addActionListener(e -> onReinitialiser.run());  
        selectionPanel.add(btnReinitialiser);

        panel.add(titre);
        panel.add(Box.createVerticalStrut(10));
        panel.add(selectionPanel);

        return panel;
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
        JButton btnDetailsVitesse,
         JLabel lblVitesseMoyenneReelle,
        JLabel lblCarburant,
        Runnable onCheminRepaint
    ) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblChemins = new JLabel("Chemins disponibles:");
        lblChemins.setFont(new Font("Arial", Font.BOLD, 14));
        lblChemins.setAlignmentX(Component.LEFT_ALIGNMENT);

        listChemins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listChemins.setCellRenderer(new CheminCellRenderer());
        listChemins.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listChemins.getSelectedValue() != null) {
                onCheminRepaint.run();
                
            }
        });

        JScrollPane scrollPane = new JScrollPane(listChemins);
        scrollPane.setPreferredSize(new Dimension(280, 300));
        scrollPane.setMaximumSize(new Dimension(280, 300));

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBoutons.setBackground(new Color(245, 245, 245));
        panelBoutons.setMaximumSize(new Dimension(280, 40));


        btnDemarrer.setPreferredSize(new Dimension(130, 30));
        btnDemarrer.setBackground(new Color(33, 150, 243));
        btnDemarrer.setForeground(Color.WHITE);
        btnDemarrer.setFont(new Font("Arial", Font.BOLD, 12));
        btnDemarrer.setFocusPainted(false);

        btnArreter.setPreferredSize(new Dimension(130, 30));
        btnArreter.setBackground(new Color(244, 67, 54));
        btnArreter.setForeground(Color.WHITE);
        btnArreter.setFont(new Font("Arial", Font.BOLD, 12));
        btnArreter.setFocusPainted(false);
        btnArreter.setEnabled(false);

        panelBoutons.add(btnDemarrer);
        panelBoutons.add(btnArreter);

        JPanel panelInfos = new JPanel();
        panelInfos.setLayout(new BoxLayout(panelInfos, BoxLayout.Y_AXIS));
        panelInfos.setBackground(new Color(245, 245, 245));
        panelInfos.setMaximumSize(new Dimension(280, 200));
        panelInfos.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Informations",
            0, 0,
            new Font("Arial", Font.BOLD, 14)
        ));

        lblHeureActuelle.setFont(new Font("Arial", Font.BOLD, 14));
        lblHeureActuelle.setForeground(new Color(33, 150, 243));
        lblHeureActuelle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblHeureArrivee.setFont(new Font("Arial", Font.PLAIN, 13));
        lblHeureArrivee.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTemps.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTemps.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPosition.setFont(new Font("Arial", Font.PLAIN, 13));
        lblPosition.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblVitesse.setFont(new Font("Arial", Font.PLAIN, 13));
        lblVitesse.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblCarburant.setFont(new Font("Arial", Font.PLAIN, 13));
        lblCarburant.setForeground(new Color(0, 128, 0));
        lblCarburant.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelInfos.add(lblHeureActuelle);
        panelInfos.add(Box.createVerticalStrut(5));
        panelInfos.add(lblHeureArrivee);
        panelInfos.add(Box.createVerticalStrut(8));
        panelInfos.add(lblTemps);
        panelInfos.add(Box.createVerticalStrut(5));
        panelInfos.add(lblPosition);
        panelInfos.add(Box.createVerticalStrut(5));
        panelInfos.add(lblVitesse);
        panelInfos.add(Box.createVerticalStrut(5));
        panelInfos.add(lblCarburant);
        panelInfos.add(lblVitesseMoyenneReelle);
            panelInfos.add(Box.createVerticalStrut(5));
            panelInfos.add(btnDetailsVitesse);
        panel.add(lblChemins);
        panel.add(Box.createVerticalStrut(5));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));
        panel.add(panelBoutons);
        panel.add(Box.createVerticalStrut(10));
        panel.add(panelInfos);
        panel.add(Box.createVerticalGlue());
        return panel;
    }
}