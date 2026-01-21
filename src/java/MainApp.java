package src.java;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.java.controllers.LavakaController;
import src.java.controllers.PauseController;
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
    private JLabel lblVitesseMoyenneReelle;
    private JComboBox<String> cbCheminPause;
    private JTextField txtPositionPause;
    private JTextField txtHeureDebutPause;
    private JTextField txtHeureFinPause;
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

    public MainApp() {
        super("Hi-voyage");
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

        JButton btnGererLavaka = new JButton("Gerer Lavaka");
        btnGererLavaka.addActionListener(e -> ouvrirGestionLavaka());

        JButton btnGererPause = new JButton("Gerer Pauses");
        btnGererPause.addActionListener(e -> ouvrirGestionPause());

        btnReinitialiser = new JButton("Reinitialiser");

        listModel = new DefaultListModel<>();
        listChemins = new JList<>(listModel);

        btnDemarrer = new JButton("Demarrer");
        btnDemarrer.addActionListener(e -> demarrerVoyage());

        btnArreter = new JButton("Arreter");
        btnArreter.addActionListener(e -> arreterVoyage());
        JButton btnDetailsVitesse = new JButton("Détails Vitesse");
        btnDetailsVitesse.addActionListener(e -> afficherDetailsVitesse());

        lblHeureActuelle = new JLabel("Heure actuelle: --:--");
        lblHeureArrivee = new JLabel("Arrivee estimee: --:--");
        lblTemps = new JLabel("Temps ecoule: 0.00 h");
        lblPosition = new JLabel("Position: 0.0 / 0.0 km");
        lblVitesse = new JLabel("Vitesse: 0.0 km/h");
        lblCarburant = new JLabel("Carburant: 0.0 L");
        lblVitesseMoyenneReelle = new JLabel("Vitesse moyenne réelle: -- km/h");
        lblVitesseMoyenneReelle.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainPanel.add(PanelFactory.creerPanelHaut(
                cbDepart, cbArrivee, cbVoiture, txtVitesseMoyenne, txtHeureDepart,
                btnRechercher, btnGererLavaka, btnGererPause, btnReinitialiser, 
                () -> {
                    mettreAJourVitesseMoyenne();
                    mettreAJourListeChemins();
                },
                () -> reinitialiser()), BorderLayout.NORTH);

        mainPanel.add(PanelFactory.creerPanelGauche(
                listModel, listChemins,
                btnDemarrer, btnArreter,
                lblHeureActuelle, lblHeureArrivee, lblTemps, lblPosition, lblVitesse, btnDetailsVitesse,
                lblVitesseMoyenneReelle, lblCarburant,
                
                () -> {
                    panelChemin.setCheminSelectionne(listChemins.getSelectedValue());
                    panelChemin.repaint();
                    afficherVitesseReelleCheminSelectionne();
                }

        ), BorderLayout.WEST);

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

    private void afficherVitesseReelleCheminSelectionne() {
        CheminItem selected = listChemins.getSelectedValue();
        if (selected == null) {
            lblVitesseMoyenneReelle.setText("Vitesse moyenne réelle: -- km/h");
            return;
        }

        Voiture voiture = (Voiture) cbVoiture.getSelectedItem();
        if (voiture == null) {
            return;
        }

        try {
            double vitesseMoyenne = Double.parseDouble(txtVitesseMoyenne.getText().trim());
            
            // Décomposer le chemin en segments
            List<Segment> segments = decomposerChemin(selected.getChemin(), voiture, vitesseMoyenne);
            
            // Calculer la vitesse réelle
            double vitesseReelle = voiture.calculerVitesseMoyenneReelle(segments, vitesseMoyenne);

            // Afficher
            lblVitesseMoyenneReelle.setText(
                    String.format("Vitesse moyenne réelle: %.2f km/h", vitesseReelle));

            // Colorer en fonction de la différence
            double difference = vitesseMoyenne - vitesseReelle;
            if (difference > 10) {
                lblVitesseMoyenneReelle.setForeground(Color.RED);
            } else if (difference > 5) {
                lblVitesseMoyenneReelle.setForeground(new Color(255, 140, 0)); // Orange
            } else {
                lblVitesseMoyenneReelle.setForeground(new Color(0, 150, 0)); // Vert
            }

        } catch (Exception ex) {
            lblVitesseMoyenneReelle.setText("Vitesse moyenne réelle: -- km/h");
            lblVitesseMoyenneReelle.setForeground(Color.BLACK);
        }
    }

    private void initialiserComboBoxes() {
        if (extremites == null || extremites.isEmpty()) {
            showError("Aucune extrémité trouvée dans la base.");
            return;
        }
        for (String ext : extremites) {
            cbDepart.addItem(ext);
            cbArrivee.addItem(ext);
        }
        for (Voiture v : voitures) {
            cbVoiture.addItem(v);
        }
        mettreAJourVitesseMoyenne();
    }

    private void mettreAJourVitesseMoyenne() {
        Voiture voiture = (Voiture) cbVoiture.getSelectedItem();
        if (voiture != null) {
            txtVitesseMoyenne.setText(String.valueOf((int) voiture.getVitesseMaximale()));
        }
    }

    private void afficherDetailsVitesse() {
        CheminItem cheminItem = listChemins.getSelectedValue();

        if (cheminItem == null) {
            showError("Veuillez sélectionner un chemin dans la liste");
            return;
        }

        Voiture voiture = (Voiture) cbVoiture.getSelectedItem();
        if (voiture == null) {
            showError("Veuillez sélectionner une voiture");
            return;
        }

        double vitesseMoyenne;
        try {
            vitesseMoyenne = Double.parseDouble(txtVitesseMoyenne.getText().trim());
        } catch (NumberFormatException e) {
            showError("Vitesse moyenne invalide");
            return;
        }

        // Décomposer le chemin en segments
        List<Segment> segments = decomposerChemin(cheminItem.getChemin(), voiture, vitesseMoyenne);
        
        // Générer les détails
        StringBuilder details = new StringBuilder();
        details.append("╔═══════════════════════════════════════════════════════════╗\n");
        details.append("║       DÉTAILS DE LA VITESSE MOYENNE RÉELLE               ║\n");
        details.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        details.append("🚗 Voiture: ").append(voiture.toString()).append("\n");
        details.append("🎯 Vitesse souhaitée: ").append(String.format("%.1f km/h", vitesseMoyenne)).append("\n\n");
        
        details.append("═══════════════════════════════════════════════════════════\n");
        details.append("                    SEGMENTS DU TRAJET                     \n");
        details.append("═══════════════════════════════════════════════════════════\n\n");
        
        double distanceTotale = 0;
        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            distanceTotale += seg.getDistance();
            details.append(String.format("Segment %d:\n", i + 1));
            details.append(String.format("  📏 Distance: %.2f km\n", seg.getDistance()));
            details.append(String.format("  ⚡ Vitesse: %.2f km/h\n", seg.getVitesse()));
            details.append(String.format("  📊 Contribution: %.2f\n\n", seg.getContribution()));
        }
        
        double vitesseReelle = voiture.calculerVitesseMoyenneReelle(segments, vitesseMoyenne);
        
        details.append("═══════════════════════════════════════════════════════════\n");
        details.append("                        RÉSULTAT                          \n");
        details.append("═══════════════════════════════════════════════════════════\n\n");
        details.append(String.format("📏 Distance totale: %.2f km\n", distanceTotale));
        details.append(String.format("🚗 Vitesse moyenne RÉELLE: %.2f km/h\n", vitesseReelle));
        details.append(String.format("📉 Différence: %.2f km/h (%.1f%%)\n",
                vitesseMoyenne - vitesseReelle,
                ((vitesseMoyenne - vitesseReelle) / vitesseMoyenne) * 100));

        // Afficher dans une fenêtre
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 550));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "📊 Détails de la Vitesse Moyenne Réelle",
                JOptionPane.INFORMATION_MESSAGE);
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

            grapheService.construireGraphe(lalanas);

            VoitureDAO voitureDAO = new VoitureDAO();
            voitures = voitureDAO.findAll();
            System.out.println("Extrémités chargées : " + extremites);

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

    /* ==========================================================
                   DÉCOMPOSITION & CALCUL VITESSE
       ========================================================== */

    public static List<Segment> decomposerLalana(
            Lalana lalana,
            double vitesseSouhaitee,
            double vitesseMaxVoiture) {

        List<Segment> segments = new ArrayList<>();
        double vitesseNormale = Math.min(vitesseSouhaitee, vitesseMaxVoiture);

        List<Lavaka> lavakas = lalana.getLavakas();
        double longueur = lalana.getDistance();

        if (lavakas == null || lavakas.isEmpty()) {
            segments.add(new Segment(longueur, vitesseNormale));
            return segments;
        }

        lavakas.sort((a, b) -> Double.compare(a.getDebut(), b.getDebut()));
        double position = 0;

        for (Lavaka l : lavakas) {

            if (position < l.getDebut()) {
                segments.add(new Segment(
                        l.getDebut() - position,
                        vitesseNormale
                ));
            }

            double vitesseLavaka = Math.max(
                    10,
                    vitesseNormale * (1 - l.getRalentissement())
            );

            segments.add(new Segment(
                    l.getFin() - l.getDebut(),
                    vitesseLavaka
            ));

            position = l.getFin();
        }

        if (position < longueur) {
            segments.add(new Segment(
                    longueur - position,
                    vitesseNormale
            ));
        }

        return segments;
    }

    private List<Segment> decomposerChemin(
            List<Lalana> chemin,
            Voiture voiture,
            double vitesseSouhaitee) {

        List<Segment> segments = new ArrayList<>();

        for (Lalana l : chemin) {
            segments.addAll(
                    decomposerLalana(
                            l,
                            vitesseSouhaitee,
                            voiture.getVitesseMaximale()
                    )
            );
        }
        return segments;
    }

    /* ==========================================================
                     CALCUL & AFFICHAGE
       ========================================================== */


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
        lblVitesseMoyenneReelle.setText("Vitesse moyenne réelle: -- km/h");
        lblVitesseMoyenneReelle.setForeground(Color.BLACK);
        
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
                    
                    // Calculer la vitesse moyenne réelle
                    List<Segment> segments = decomposerChemin(
                        voyageActuel.getCheminChoisi(),
                        voyageActuel.getVoiture(),
                        voyageActuel.getVitesseMoyenne()
                    );
                    double vitesseMoyenneReelle = voyageActuel.getVoiture()
                            .calculerVitesseMoyenneReelle(segments, voyageActuel.getVitesseMoyenne());
                    
                    showInfo("🏁 Voyage terminé!\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "⏰ Heure de départ: " + voyageActuel.getHeureDepartFormatee() + "\n" +
                            "⏰ Heure d'arrivée: " + voyageActuel.getHeureActuelleFormatee() + "\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📏 Distance: " + String.format("%.1f km", voyageActuel.getDistanceTotale()) + "\n" +
                            "⏱️  Temps total: " + String.format("%.2f heures (%.0f min)",
                                    voyageActuel.getTempsEcoule(), voyageActuel.getTempsEcoule() * 60)
                            + "\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "🎯 Vitesse souhaitée: " + String.format("%.1f km/h", voyageActuel.getVitesseMoyenne())
                            + "\n" +
                            "🚗 Vitesse moyenne RÉELLE: " + String.format("%.2f km/h", vitesseMoyenneReelle) + "\n" +
                            "📉 Différence: " + String.format("%.2f km/h (%.1f%%)",
                                    voyageActuel.getVitesseMoyenne() - vitesseMoyenneReelle,
                                    ((voyageActuel.getVitesseMoyenne() - vitesseMoyenneReelle)
                                            / voyageActuel.getVitesseMoyenne()) * 100)
                            + "\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "⛽ Carburant utilisé: " + String.format("%.1f L", carburantUtilise));
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
        
        // Calculer la vitesse réelle pour l'affichage en temps réel
        List<Segment> segments = decomposerChemin(
            voyageActuel.getCheminChoisi(),
            voyageActuel.getVoiture(),
            voyageActuel.getVitesseMoyenne()
        );
        double vitesseReelle = voyageActuel.getVoiture().calculerVitesseMoyenneReelle(segments, voyageActuel.getVitesseMoyenne());
        lblVitesseMoyenneReelle.setText(String.format("Vitesse moy. réelle: %.2f km/h", vitesseReelle));
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

    private void ajouterPause() {
        try {
            String chemin = (String) cbCheminPause.getSelectedItem();
            if (chemin == null) {
                showError("Veuillez selectionner un chemin");
                return;
            }

            double position = Double.parseDouble(txtPositionPause.getText().trim());
            LocalTime debut = LocalTime.parse(txtHeureDebutPause.getText().trim(),
                    DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime fin = LocalTime.parse(txtHeureFinPause.getText().trim(),
                    DateTimeFormatter.ofPattern("HH:mm"));

            if (position < 0) {
                showError("La position doit etre positive");
                return;
            }

            Lalana lalana = null;
            for (Lalana l : lalanas) {
                if (l.getNom().equals(chemin)) {
                    lalana = l;
                    break;
                }
            }

            if (lalana != null && position > lalana.getDistance()) {
                showError(String.format("La position depasse la longueur du chemin (%.1f km)",
                        lalana.getDistance()));
                return;
            }

            if (debut.isAfter(fin) || debut.equals(fin)) {
                showError("L'heure de debut doit etre avant l'heure de fin");
                return;
            }

            Pause pause = new Pause(position, debut, fin);
            PauseDAO pauseDAO = new PauseDAO();
            pauseDAO.insert(pause, chemin);

            pauseDAO.chargerPausesPourLalanas(lalanas);

            showInfo("Pause ajoutee avec succes!");

            txtPositionPause.setText("");
            txtHeureDebutPause.setText("10:00");
            txtHeureFinPause.setText("11:00");

        } catch (NumberFormatException ex) {
            showError("Veuillez entrer une position valide");
        } catch (DateTimeParseException ex) {
            showError("Format d'heure invalide. Utilisez HH:mm (ex: 10:00)");
        } catch (Exception ex) {
            showError("Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
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

    public static void main(String[] args) throws SQLException {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}