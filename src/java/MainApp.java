package src.java;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.java.controllers.*;
import src.java.dao.*;
import src.java.models.*;
import src.java.services.*;
import src.java.ui.*;

public class MainApp extends JFrame {

   private JComboBox<String> cbDepart;
    private JComboBox<String> cbArrivee;
    private JComboBox<Voiture> cbVoiture;
    private JTextField txtVitesseMoyenne;
    private JTextField txtHeureDepart;
    private DefaultListModel<CheminItem> listModel;
    private JList<CheminItem> listChemins;
    private CheminPanel panelChemin;
    private JButton btnRechercher;
    private JButton btnDemarrer;
    private JButton btnArreter;
    private JLabel lblTemps;
    private JLabel lblPosition;
    private JLabel lblVitesse;
    private JLabel lblCarburant;
    private JLabel lblHeureActuelle;
    private JLabel lblHeureArrivee;
    private JButton btnReinitialiser;

    private List<String> extremites;
    private List<Lalana> lalanas;
    private List<Voiture> voitures;
    private GrapheService grapheService;
    private VoyageService voyageService;
    private Voyage voyageActuel;
    private Timer animationTimer;
    private List<List<Lalana>> cheminsTrouves;
    private long dernierTemps;
    private double dureeEstimee;
    private double vitesseMoyenneReelle;

    public MainApp() {
        super("Simulation de Voyage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        initComponents();
        initialiser();

        setVisible(true);
    }

    private void initComponents() {
        cbDepart = new JComboBox<>();
        cbArrivee = new JComboBox<>();
        cbVoiture = new JComboBox<>();
        txtVitesseMoyenne = new JTextField("80");
        txtHeureDepart = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        btnRechercher = new JButton("Rechercher Chemins");
        btnRechercher.addActionListener(e -> rechercherChemins());

        btnReinitialiser = new JButton("Reinitialiser");
        btnReinitialiser.addActionListener(e -> reinitialiser());

        JButton btnGererLavaka = new JButton("Ajouter Lavaka");
        btnGererLavaka.addActionListener(e -> ouvrirGestionLavaka());

        JButton btnGererPause = new JButton("Ajouter Pauses");
        btnGererPause.addActionListener(e -> ouvrirGestionPause());

        JButton btnGererReparation = new JButton("Ajouter Reparation");
        btnGererReparation.addActionListener(e -> ouvrirGestionReparation());

        JButton btnGererSimba = new JButton("Asiana Simba");
        btnGererSimba.addActionListener(e -> ouvrirGestionSimba());

        JButton btnCalculerCout = new JButton("Réparer");
        btnCalculerCout.addActionListener(e -> ouvrirCalculCout());

        JButton btnGererPluviometrie = new JButton("Ajouter Pluviométrie");
        btnGererPluviometrie.addActionListener(e -> ouvrirGestionPluviometrie());

        JButton btnGererIntervalles = new JButton("Intervalles Pluie");
        btnGererIntervalles.addActionListener(e -> ouvrirGestionIntervalles());

        // ═══════════════════════════════════════════════════════════════
        // NOUVEAU : Bouton Carte SIG
        // ═══════════════════════════════════════════════════════════════
        JButton btnCarteSIG = new JButton("🗺️ Carte SIG");
        btnCarteSIG.addActionListener(e -> ouvrirCarteSIG());

        listModel = new DefaultListModel<>();
        listChemins = new JList<>(listModel);

        btnDemarrer = new JButton("Demarrer");
        btnDemarrer.addActionListener(e -> demarrerVoyage());

        btnArreter = new JButton("Arreter");
        btnArreter.addActionListener(e -> arreterVoyage());

        lblHeureActuelle = new JLabel("Heure actuelle: --:--");
        lblHeureArrivee = new JLabel("Arrivee estimee: --:--");
        lblTemps = new JLabel("Temps ecoule: 0.00 h");
        lblPosition = new JLabel("Position: 0.0 / 0.0 km");
        lblVitesse = new JLabel("Vitesse: 0.0 km/h");
        lblCarburant = new JLabel("Carburant: 0.0 L");

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ═══════════════════════════════════════════════════════════════
        // MODIFIÉ : Ajout du paramètre btnCarteSIG à creerPanelHaut
        // ═══════════════════════════════════════════════════════════════
        mainPanel.add(PanelFactory.creerPanelHaut(
                cbDepart, cbArrivee, cbVoiture, txtVitesseMoyenne, txtHeureDepart,
                btnRechercher, btnGererLavaka, btnGererPause, btnGererReparation,
                btnGererSimba, btnCalculerCout, btnGererPluviometrie, btnGererIntervalles,
                btnCarteSIG,  // ← NOUVEAU PARAMÈTRE
                btnReinitialiser,
                () -> {
                    mettreAJourVitesseMoyenne();
                    mettreAJourListeChemins();
                },
                () -> reinitialiser()), BorderLayout.NORTH);

        mainPanel.add(PanelFactory.creerPanelGauche(
                listModel, listChemins,
                btnDemarrer, btnArreter,
                lblHeureActuelle, lblHeureArrivee, lblTemps, lblPosition, lblVitesse, lblCarburant,
                () -> {
                    panelChemin.setCheminSelectionne(listChemins.getSelectedValue());
                    panelChemin.repaint();
                }), BorderLayout.WEST);

        panelChemin = new CheminPanel();
        panelChemin.setPreferredSize(new Dimension(750, 600));
        panelChemin.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        mainPanel.add(panelChemin, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void initialiser() {
        grapheService = new GrapheService();
        voyageService = new VoyageService();
        chargerDonnees();
        initialiserComboBoxes();
    }

    private void initialiserComboBoxes() {
        for (String ext : extremites) {
            cbDepart.addItem(ext);
            cbArrivee.addItem(ext);
        }
        for (Voiture v : voitures) {
            cbVoiture.addItem(v);
        }
        mettreAJourVitesseMoyenne();
    }

    private void chargerDonnees() {
        try {
            LalanaDAO lalanaDAO = new LalanaDAO();
            lalanas = lalanaDAO.findAll();
            extremites = lalanaDAO.getAllExtremites();

            LavakaDAO lavakaDAO = new LavakaDAO();
            lavakaDAO.chargerLavakasPourLalanas(lalanas);

            PauseDAO pauseDAO = new PauseDAO();
            pauseDAO.chargerPausesPourLalanas(lalanas);

            SimbaDAO simbaDAO = new SimbaDAO();
            simbaDAO.chargerSimbasPourLalanas(lalanas);

            grapheService.construireGraphe(lalanas);

            VoitureDAO voitureDAO = new VoitureDAO();
            voitures = voitureDAO.findAll();

        } catch (Exception e) {
            showError("Erreur chargement donnees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void rechercherChemins() {
        String depart = (String) cbDepart.getSelectedItem();
        String arrivee = (String) cbArrivee.getSelectedItem();

        if (depart == null || arrivee == null) {
            showError("Selectionnez un point de depart et d'arrivee");
            return;
        }

        if (depart.equals(arrivee)) {
            showError("Le depart et l'arrivee doivent etre differents");
            return;
        }

        Voiture voiture = (Voiture) cbVoiture.getSelectedItem();
        if (voiture == null) {
            showError("Selectionnez une voiture");
            return;
        }

        double vitesseMoyenne;
        try {
            vitesseMoyenne = Double.parseDouble(txtVitesseMoyenne.getText().trim());
            if (vitesseMoyenne <= 0) {
                showError("La vitesse moyenne doit etre positive");
                return;
            }
            if (vitesseMoyenne > voiture.getVitesseMaximale()) {
                showError("La vitesse moyenne (" + vitesseMoyenne + " km/h) ne peut pas depasser\n" +
                        "la vitesse maximale de la voiture (" + voiture.getVitesseMaximale() + " km/h)");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Veuillez entrer une vitesse moyenne valide");
            return;
        }

        cheminsTrouves = grapheService.trouverTousLesChemins(depart, arrivee);

        if (cheminsTrouves.isEmpty()) {
            showError("Aucun chemin trouve entre " + depart + " et " + arrivee);
            return;
        }

        mettreAJourListeChemins();

        panelChemin.setAfficherChemin(false);
        panelChemin.repaint();
    }

    private void mettreAJourListeChemins() {
        if (cheminsTrouves == null || cheminsTrouves.isEmpty()) {
            return;
        }

        Voiture voiture = (Voiture) cbVoiture.getSelectedItem();
        if (voiture == null) {
            return;
        }

        listModel.clear();
        for (List<Lalana> chemin : cheminsTrouves) {
            CheminItem item = new CheminItem(chemin, voiture);
            listModel.addElement(item);
        }
    }

    private void demarrerVoyage() {
        Voiture voiture = (Voiture) cbVoiture.getSelectedItem();
        CheminItem cheminItem = listChemins.getSelectedValue();

        if (voiture == null) {
            showError("Selectionnez une voiture");
            return;
        }

        if (cheminItem == null) {
            showError("Selectionnez un chemin");
            return;
        }

        double vitesseMoyenne;
        try {
            vitesseMoyenne = Double.parseDouble(txtVitesseMoyenne.getText().trim());
            if (vitesseMoyenne <= 0) {
                showError("La vitesse moyenne doit etre positive");
                return;
            }
            if (vitesseMoyenne > voiture.getVitesseMaximale()) {
                showError("La vitesse moyenne (" + vitesseMoyenne + " km/h) ne peut pas depasser\n" +
                        "la vitesse maximale de la voiture (" + voiture.getVitesseMaximale() + " km/h)");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Veuillez entrer une vitesse moyenne valide");
            return;
        }

        LocalTime heureDepart;
        try {
            heureDepart = LocalTime.parse(txtHeureDepart.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showError("Format d'heure invalide. Utilisez HH:mm (ex: 08:30)");
            return;
        }

        if (!cheminItem.isCarburantSuffisant()) {
            showError("Carburant insuffisant pour ce chemin!\n" +
                    "Necessite: " + String.format("%.1f", cheminItem.getCarburantNecessaire()) + " L\n" +
                    "Reservoir: " + String.format("%.1f", voiture.getReservoir()) + " L");
            return;
        }

        String depart = (String) cbDepart.getSelectedItem();
        String arrivee = (String) cbArrivee.getSelectedItem();

        voyageActuel = new Voyage(depart, arrivee, voiture, vitesseMoyenne, heureDepart);
        voyageActuel.setCheminChoisi(cheminItem.getChemin());

        if (!voyageService.validerVoyage(voyageActuel)) {
            showError(voyageService.obtenirMessageErreur(voyageActuel));
            return;
        }

        dureeEstimee = voyageService.calculerDureeEstimee(voyageActuel);
        List<SegmentChemin> segments = voiture.decomposerLalana(voyageActuel);
        vitesseMoyenneReelle = voiture.calculVMRelle(segments, voyageActuel.getDistanceTotale());

        btnDemarrer.setEnabled(false);
        btnArreter.setEnabled(true);
        cbDepart.setEnabled(false);
        cbArrivee.setEnabled(false);
        cbVoiture.setEnabled(false);
        txtVitesseMoyenne.setEnabled(false);
        txtHeureDepart.setEnabled(false);
        listChemins.setEnabled(false);
        btnRechercher.setEnabled(false);

        panelChemin.setAfficherChemin(true);
        panelChemin.setVoyageActuel(voyageActuel);
        panelChemin.setCheminSelectionne(cheminItem);

        demarrerAnimation();
    }

    private void arreterVoyage() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        btnDemarrer.setEnabled(true);
        btnArreter.setEnabled(false);
        cbDepart.setEnabled(true);
        cbArrivee.setEnabled(true);
        cbVoiture.setEnabled(true);
        txtVitesseMoyenne.setEnabled(true);
        txtHeureDepart.setEnabled(true);
        listChemins.setEnabled(true);
        btnRechercher.setEnabled(true);
    }

    private void reinitialiser() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        voyageActuel = null;
        cheminsTrouves = null;
        dureeEstimee = 0;

        btnDemarrer.setEnabled(true);
        btnArreter.setEnabled(false);
        cbDepart.setEnabled(true);
        cbArrivee.setEnabled(true);
        cbVoiture.setEnabled(true);
        txtVitesseMoyenne.setEnabled(true);
        txtHeureDepart.setEnabled(true);
        listChemins.setEnabled(true);
        btnRechercher.setEnabled(true);

        txtHeureDepart.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        mettreAJourVitesseMoyenne();

        listModel.clear();

        lblHeureActuelle.setText("Heure actuelle: --:--");
        lblHeureArrivee.setText("Arrivee estimee: --:--");
        lblTemps.setText("Temps ecoule: 0.00 h");
        lblPosition.setText("Position: 0.0 / 0.0 km");
        lblVitesse.setText("Vitesse: 0.0 km/h");
        lblVitesse.setForeground(Color.BLACK);
        lblCarburant.setText("Carburant: 0.0 L");

        panelChemin.setAfficherChemin(false);
        panelChemin.repaint();

        chargerDonnees();

        showInfo("Interface reinitialisee avec succes!");
    }

    private void demarrerAnimation() {
        dernierTemps = System.nanoTime();

        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.nanoTime();
                double deltaTime = (now - dernierTemps) / 1_000_000_000.0;
                dernierTemps = now;

                voyageActuel.avancer(deltaTime * 1);

                panelChemin.setAnimationTimer(animationTimer);
                panelChemin.repaint();
                mettreAJourLabels();

                if (voyageActuel.isTermine()) {
                    animationTimer.stop();
                    btnDemarrer.setEnabled(true);
                    btnArreter.setEnabled(false);
                    cbDepart.setEnabled(true);
                    cbArrivee.setEnabled(true);
                    cbVoiture.setEnabled(true);
                    txtVitesseMoyenne.setEnabled(true);
                    txtHeureDepart.setEnabled(true);
                    listChemins.setEnabled(true);
                    btnRechercher.setEnabled(true);

                    double carburantUtilise = voyageActuel.getVoiture().calculerConsommation(
                            voyageActuel.getDistanceTotale());

                    showInfo("Voyage termine!\n" +
                            "Heure de depart: " + voyageActuel.getHeureDepartFormatee() + "\n" +
                            "Heure d'arrivee: " + voyageActuel.getHeureActuelleFormatee() + "\n" +
                            "Temps total: " + String.format("%.2f heures", voyageActuel.getTempsEcoule()) + "\n" +
                            "Distance: " + String.format("%.1f km", voyageActuel.getDistanceTotale()) + "\n" +
                            "Vitesse moyenne saisie: " + String.format("%.1f km/h", voyageActuel.getVitesseMoyenne())
                            + "\n" +
                            "Vitesse moyenne reelle: " + String.format("%.2f km/h", vitesseMoyenneReelle) + "\n" +
                            "Carburant utilise: " + String.format("%.1f L", carburantUtilise));
                }
            }
        });
        animationTimer.start();
    }

    private void mettreAJourLabels() {
        lblHeureActuelle.setText("Heure actuelle: " + voyageActuel.getHeureActuelleFormatee());
        lblHeureArrivee.setText("Arrivee estimee: " + voyageActuel.getHeureArriveeEstimeeFormatee(dureeEstimee));
        lblTemps.setText(String.format("Temps ecoule: %.2f h", voyageActuel.getTempsEcoule()));
        lblPosition.setText(String.format("Position: %.1f / %.1f km",
                voyageActuel.getPositionAbsolue(), voyageActuel.getDistanceTotale()));

        if (voyageActuel.isEnPause() && voyageActuel.getPauseActuelle() != null) {
            lblVitesse.setText("PAUSE jusqu'a " + voyageActuel.getPauseActuelle().getHeureFinFormatee());
            lblVitesse.setForeground(Color.RED);
        } else {
            lblVitesse.setText(String.format("Vitesse: %.1f km/h", voyageActuel.getVitesseEffective()));
            lblVitesse.setForeground(Color.BLACK);
        }

        double carburantUtilise = voyageActuel.getVoiture().calculerConsommation(
                voyageActuel.getPositionAbsolue());
        lblCarburant.setText(String.format("Carburant: %.1f L", carburantUtilise));
    }

    private void ouvrirGestionLavaka() {
        CheminItem cheminSelectionne = listChemins.getSelectedValue();

        LavakaController controller;
        if (cheminSelectionne != null) {
            controller = new LavakaController(this, cheminSelectionne.getChemin());
        } else {
            controller = new LavakaController(this);
        }

        controller.setVisible(true);

        try {
            for (Lalana l : lalanas) {
                l.getLavakas().clear();
            }

            LavakaDAO lavakaDAO = new LavakaDAO();
            lavakaDAO.chargerLavakasPourLalanas(lalanas);

            if (voyageActuel != null) {
                panelChemin.repaint();
            }
        } catch (Exception e) {
            showError("Erreur lors du rechargement des donnees: " + e.getMessage());
        }
    }

    private void ouvrirGestionReparation() {
        ReparationController controller = new ReparationController(this);
        controller.setVisible(true);
    }

    private void ouvrirGestionPause() {
        CheminItem cheminSelectionne = listChemins.getSelectedValue();

        PauseController controller;
        if (cheminSelectionne != null) {
            controller = new PauseController(this, cheminSelectionne.getChemin());
        } else {
            controller = new PauseController(this);
        }

        controller.setVisible(true);

        try {
            for (Lalana l : lalanas) {
                if (l.getPauses() != null) {
                    l.getPauses().clear();
                }
            }

            PauseDAO pauseDAO = new PauseDAO();
            pauseDAO.chargerPausesPourLalanas(lalanas);

            if (voyageActuel != null) {
                panelChemin.repaint();
            }
        } catch (Exception e) {
            showError("Erreur lors du rechargement des donnees: " + e.getMessage());
        }
    }

    private void ouvrirGestionSimba() {
        CheminItem cheminSelectionne = listChemins.getSelectedValue();

        SimbaController controller;
        if (cheminSelectionne != null) {
            controller = new SimbaController(this, cheminSelectionne.getChemin());
        } else {
            controller = new SimbaController(this);
        }

        controller.setVisible(true);

        try {
            for (Lalana l : lalanas) {
                l.getSimbas().clear();
            }

            SimbaDAO simbaDAO = new SimbaDAO();
            simbaDAO.chargerSimbasPourLalanas(lalanas);

            if (voyageActuel != null) {
                panelChemin.repaint();
            }
        } catch (Exception e) {
            showError("Erreur lors du rechargement des donnees: " + e.getMessage());
        }
    }

    private void ouvrirCalculCout() {
        CheminItem cheminSelectionne = listChemins.getSelectedValue();

        if (cheminSelectionne == null) {
            showError("Veuillez selectionner un chemin pour calculer le cout de reparation");
            return;
        }

        CoutReparationController controller = new CoutReparationController(this, cheminSelectionne.getChemin());
        controller.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        try {
            DatabaseConnection.closeAllConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    private void ouvrirGestionPluviometrie() {
        PluviometrieController controller = new PluviometrieController(this);
        controller.setVisible(true);
    }

    private void ouvrirGestionIntervalles() {
        PluviometrieIntervalleController controller = new PluviometrieIntervalleController(this);
        controller.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════
    // NOUVELLE MÉTHODE : Ouvrir la carte SIG
    // ═══════════════════════════════════════════════════════════════
    private void ouvrirCarteSIG() {
        MapController mapController = new MapController(this);
        mapController.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        try {
            DatabaseConnection.closeAllConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dispose();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}