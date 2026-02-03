package controllers;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import com.google.gson.Gson;
import com.sun.net.httpserver.*;

import dao.*;
import models.*;
import services.MapService;

public class MapController extends JDialog {

    private JList<String> listRN;
    private DefaultListModel<String> rnListModel;
    private JTextArea txtSimbaInfo;

    private MapService mapService = new MapService();
    private HttpServer httpServer;
    private Desktop desktop;

    private Map<String, Lalana> lalanaMap = new HashMap<>();
    private Map<String, List<Simba>> simbasByLalana = new HashMap<>();

    private Map<String, String> lastCommand = new HashMap<>();
    private String selectedRN;

    private static final int SERVER_PORT = 8080;
    private Gson gson = new Gson();

    public MapController(Frame parent) {
        super(parent, "Carte SIG - Routes Nationales", true);
        setSize(1400, 900);
        setLocationRelativeTo(parent);

        initUI();
        chargerDonnees();
        demarrerServeur();
        ouvrirCarte();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        rnListModel = new DefaultListModel<>();
        listRN = new JList<>(rnListModel);
        listRN.addListSelectionListener(this::onRNSelected);

        txtSimbaInfo = new JTextArea();
        txtSimbaInfo.setEditable(false);

        JButton btnReset = new JButton("Reset Zoom");
        btnReset.addActionListener(e -> resetZoom());

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JScrollPane(listRN), BorderLayout.CENTER);
        left.add(btnReset, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JScrollPane(txtSimbaInfo), BorderLayout.CENTER);

        main.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
        add(main);
    }

    private void chargerDonnees() {
        try {
            LalanaDAO lalanaDAO = new LalanaDAO();
            List<Lalana> lalanas = lalanaDAO.findAll();

            SimbaDAO simbaDAO = new SimbaDAO();
            simbaDAO.chargerSimbasPourLalanas(lalanas);

            rnListModel.clear();
            lalanaMap.clear();
            simbasByLalana.clear();

            for (Lalana l : lalanas) {
                lalanaMap.put(l.getNom(), l);
                simbasByLalana.put(l.getNom(), l.getSimbas());
                rnListModel.addElement(l.getNom());
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void onRNSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        String rn = listRN.getSelectedValue();
        if (rn == null) return;

        selectedRN = rn;
        afficherSimbasInfo(rn);
        zoomSurRN(rn);
    }

    private void afficherSimbasInfo(String rn) {
        List<Simba> simbas = simbasByLalana.get(rn);
        if (simbas == null) return;

        StringBuilder sb = new StringBuilder("SIMBA de " + rn + "\n\n");
        for (Simba s : simbas) {
            sb.append("PK: ").append(s.getPk())
              .append(" | Surface: ").append(s.getSurface())
              .append(" | Prof: ").append(s.getProfondeur()).append("\n");
        }
        txtSimbaInfo.setText(sb.toString());
    }

    private void zoomSurRN(String rnNom) {
        envoyerCommandeCarte("zoomRN", rnNom);
    }

    private void resetZoom() {
        envoyerCommandeCarte("resetZoom", null);
    }

    private void envoyerCommandeCarte(String action, String rn) {
        try {
            var url = new java.net.URL("http://localhost:" + SERVER_PORT + "/api/command");
            var conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            Map<String, String> cmd = new HashMap<>();
            cmd.put("action", action);
            if (rn != null) cmd.put("rn", rn);

            conn.getOutputStream().write(gson.toJson(cmd).getBytes(StandardCharsets.UTF_8));
            conn.getResponseCode();
            conn.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void demarrerServeur() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);

            httpServer.createContext("/", e -> {
                byte[] resp = mapService.genererPageHTML().getBytes(StandardCharsets.UTF_8);
                e.sendResponseHeaders(200, resp.length);
                e.getResponseBody().write(resp);
                e.close();
            });

            httpServer.createContext("/api/data", new DataHandler());
            httpServer.createContext("/api/command", new CommandHandler());

            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class DataHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            Map<String, Object> data = new HashMap<>();
            data.put("selectedRN", selectedRN);

            List<Map<String, Object>> routes = new ArrayList<>();
            for (String rn : lalanaMap.keySet()) {
                Lalana l = lalanaMap.get(rn);
                Map<String, Object> r = new HashMap<>();
                r.put("nom", l.getNom());
                r.put("distance", l.getDistance());
                r.put("simbas", simbasByLalana.get(rn));
                routes.add(r);
            }

            data.put("routes", routes);

            byte[] resp = gson.toJson(data).getBytes();
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        }
    }

    class CommandHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equals(ex.getRequestMethod())) {
                lastCommand = gson.fromJson(
                        new String(ex.getRequestBody().readAllBytes()), Map.class);
                ex.sendResponseHeaders(200, 2);
                ex.getResponseBody().write("OK".getBytes());
                ex.close();
                return;
            }

            byte[] resp = gson.toJson(lastCommand).getBytes();
            lastCommand = new HashMap<>();
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        }
    }

    private void ouvrirCarte() {
        try {
            desktop = Desktop.getDesktop();
            desktop.browse(new java.net.URI("http://localhost:" + SERVER_PORT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        if (httpServer != null) httpServer.stop(0);
        super.dispose();
    }
}
