package controllers;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import dao.*;
import models.*;
import services.MapService;

/**
 * Contrôleur pour la carte SIG interactive
 * Affiche les routes nationales (RN), leurs SIMBA et points kilométriques
 */
public class MapController extends JDialog {
    
    private JList<String> listRN;
    private DefaultListModel<String> rnListModel;
    private JTextArea txtSimbaInfo;
    private JButton btnRefresh;
    private JButton btnZoomReset;
    
    private MapService mapService;
    private HttpServer httpServer;
    private Desktop desktop;
    
    private Map<String, Lalana> lalanaMap;
    private Map<String, List<Simba>> simbasByLalana;
    private String selectedRN;
    
    private static final int SERVER_PORT = 8080;
    private Gson gson;

    public MapController(Frame parent) {
        super(parent, "Carte SIG - Routes Nationales", true);
        setSize(1400, 900);
        setLocationRelativeTo(parent);
        
        this.mapService = new MapService();
        this.lalanaMap = new HashMap<>();
        this.simbasByLalana = new HashMap<>();
        this.gson = new Gson();
        
        initComponents();
        chargerDonnees();
        demarrerServeur();
        ouvrirCarte();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Panel gauche - Liste des RN
        JPanel leftPanel = createLeftPanel();
        
        // Panel droit - Informations SIMBA
        JPanel rightPanel = createRightPanel();
        
        // Panel bas - Instructions
        JPanel bottomPanel = createBottomPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Routes Nationales (RN)"));
        panel.setBackground(Color.WHITE);

        // Liste des RN
        rnListModel = new DefaultListModel<>();
        listRN = new JList<>(rnListModel);
        listRN.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listRN.setFont(new Font("Arial", Font.PLAIN, 13));
        
        listRN.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    onRNSelected();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(listRN);
        
        // Boutons d'action
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnPanel.setBackground(Color.WHITE);
        
        btnRefresh = new JButton("🔄 Actualiser");
        btnRefresh.setBackground(Color.BLACK);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> chargerDonnees());
        
        btnZoomReset = new JButton("🌍 Réinitialiser zoom");
        btnZoomReset.setBackground(Color.DARK_GRAY);
        btnZoomReset.setForeground(Color.WHITE);
        btnZoomReset.setFocusPainted(false);
        btnZoomReset.addActionListener(e -> resetZoom());
        
        btnPanel.add(btnRefresh);
        btnPanel.add(btnZoomReset);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Informations SIMBA"));
        panel.setBackground(Color.WHITE);

        txtSimbaInfo = new JTextArea();
        txtSimbaInfo.setEditable(false);
        txtSimbaInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSimbaInfo.setText("Sélectionnez une route nationale pour voir ses SIMBA...");
        
        JScrollPane scrollPane = new JScrollPane(txtSimbaInfo);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLACK),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblInstructions = new JLabel(
            "<html><b>Instructions :</b> " +
            "Cliquez sur une RN dans la liste → la carte zoome dessus | " +
            "Cliquez sur une RN sur la carte → elle est sélectionnée dans la liste | " +
            "Cliquez sur un SIMBA → voir ses détails</html>"
        );
        lblInstructions.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(e -> {
            arreterServeur();
            dispose();
        });
        
        panel.add(lblInstructions, BorderLayout.CENTER);
        panel.add(btnFermer, BorderLayout.EAST);
        
        return panel;
    }

    private void chargerDonnees() {
        try {
            LalanaDAO lalanaDAO = new LalanaDAO();
            List<Lalana> lalanas = lalanaDAO.findAll();
            
            SimbaDAO simbaDAO = new SimbaDAO();
            simbaDAO.chargerSimbasPourLalanas(lalanas);
            
            lalanaMap.clear();
            simbasByLalana.clear();
            rnListModel.clear();
            
            for (Lalana lalana : lalanas) {
                lalanaMap.put(lalana.getNom(), lalana);
                List<Simba> simbas = lalana.getSimbas();
                simbasByLalana.put(lalana.getNom(), simbas);
                
                String displayText = String.format("%s (%.1f km, %d SIMBA)", 
                    lalana.getNom(), 
                    lalana.getDistance(),
                    simbas.size());
                rnListModel.addElement(displayText);
            }
            
            // Envoyer les données à la carte
            envoyerDonneesVersCart();
            
        } catch (SQLException e) {
            showError("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void onRNSelected() {
        int selectedIndex = listRN.getSelectedIndex();
        if (selectedIndex < 0) return;
        
        String displayText = rnListModel.getElementAt(selectedIndex);
        String rnNom = displayText.split(" \\(")[0];
        
        selectedRN = rnNom;
        
        // Afficher les SIMBA de cette RN
        afficherSimbasInfo(rnNom);
        
        // Zoomer sur la RN dans la carte
        zoomSurRN(rnNom);
    }

    private void afficherSimbasInfo(String rnNom) {
        List<Simba> simbas = simbasByLalana.get(rnNom);
        Lalana lalana = lalanaMap.get(rnNom);
        
        if (simbas == null || simbas.isEmpty()) {
            txtSimbaInfo.setText("Aucun SIMBA sur cette route.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append(String.format(" ROUTE: %s\n", rnNom));
        sb.append(String.format(" Distance: %.1f km | Largeur: %.1f m\n", 
            lalana.getDistance(), lalana.getLargeur()));
        sb.append("═══════════════════════════════════════════════\n\n");
        sb.append(String.format("Nombre de SIMBA: %d\n\n", simbas.size()));
        
        for (int i = 0; i < simbas.size(); i++) {
            Simba simba = simbas.get(i);
            sb.append(String.format("SIMBA %d\n", i + 1));
            sb.append(String.format("  📍 PK: %.2f km\n", simba.getPk()));
            sb.append(String.format("  📏 Surface: %.2f m²\n", simba.getSurface()));
            sb.append(String.format("  📊 Profondeur: %.2f m\n", simba.getProfondeur()));
            sb.append("\n");
        }
        
        txtSimbaInfo.setText(sb.toString());
        txtSimbaInfo.setCaretPosition(0);
    }

    private void zoomSurRN(String rnNom) {
        // Envoyer commande de zoom à la carte web
        Map<String, String> command = new HashMap<>();
        command.put("action", "zoomRN");
        command.put("rn", rnNom);
        // Cette commande sera traitée par le JavaScript de la carte
    }

    private void resetZoom() {
        Map<String, String> command = new HashMap<>();
        command.put("action", "resetZoom");
    }

    private void demarrerServeur() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
            
            // Handler pour la page HTML
            httpServer.createContext("/", new MapPageHandler());
            
            // Handler pour les données
            httpServer.createContext("/api/data", new DataApiHandler());
            
            // Handler pour les commandes depuis la carte
            httpServer.createContext("/api/command", new CommandApiHandler());
            
            httpServer.setExecutor(null);
            httpServer.start();
            
            System.out.println("Serveur de carte démarré sur le port " + SERVER_PORT);
            
        } catch (IOException e) {
            showError("Erreur lors du démarrage du serveur: " + e.getMessage());
        }
    }

    private void arreterServeur() {
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("Serveur de carte arrêté");
        }
    }

    private void ouvrirCarte() {
        try {
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.browse(new java.net.URI("http://localhost:" + SERVER_PORT));
            } else {
                showInfo("Ouvrez votre navigateur à l'adresse: http://localhost:" + SERVER_PORT);
            }
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    private void envoyerDonneesVersCart() {
        // Les données seront récupérées par la carte via /api/data
    }

    // ========== HANDLERS HTTP ==========
    
    class MapPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = mapService.genererPageHTML();
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }
    
    class DataApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> data = new HashMap<>();
            
            List<Map<String, Object>> rnList = new ArrayList<>();
            for (Map.Entry<String, Lalana> entry : lalanaMap.entrySet()) {
                Map<String, Object> rnData = new HashMap<>();
                Lalana lalana = entry.getValue();
                
                rnData.put("nom", lalana.getNom());
                rnData.put("distance", lalana.getDistance());
                rnData.put("extremiteGauche", lalana.getExtremiteGauche());
                rnData.put("extremiteDroite", lalana.getExtremiteDroite());
                
                List<Map<String, Object>> simbaList = new ArrayList<>();
                for (Simba simba : simbasByLalana.get(lalana.getNom())) {
                    Map<String, Object> simbaData = new HashMap<>();
                    simbaData.put("id", simba.getId());
                    simbaData.put("pk", simba.getPk());
                    simbaData.put("surface", simba.getSurface());
                    simbaData.put("profondeur", simba.getProfondeur());
                    simbaList.add(simbaData);
                }
                rnData.put("simbas", simbaList);
                
                rnList.add(rnData);
            }
            
            data.put("routes", rnList);
            data.put("selectedRN", selectedRN);
            
            String json = gson.toJson(data);
            byte[] response = json.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }
    
    class CommandApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> command = gson.fromJson(body, Map.class);
                
                String action = command.get("action");
                
                if ("selectRN".equals(action)) {
                    String rnNom = command.get("rn");
                    SwingUtilities.invokeLater(() -> selectionnerRNDansList(rnNom));
                }
                
                String response = "{\"status\":\"ok\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
            }
        }
    }

    private void selectionnerRNDansList(String rnNom) {
        for (int i = 0; i < rnListModel.size(); i++) {
            String displayText = rnListModel.getElementAt(i);
            if (displayText.startsWith(rnNom + " ")) {
                listRN.setSelectedIndex(i);
                listRN.ensureIndexIsVisible(i);
                break;
            }
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
        arreterServeur();
        super.dispose();
    }
}