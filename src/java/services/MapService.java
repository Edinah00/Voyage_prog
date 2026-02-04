package services;

/**
 * Service qui génère la page HTML avec Leaflet pour afficher
 * les routes nationales de Madagascar sur une carte interactive.
 *
 * Elle est utilisée par MapController via mapService.genererPageHTML().
 * Le GeoJSON des routes est intégré directement dans le HTML généré.
 */
public class MapService {

    // GeoJSON simplifié des routes nationales (coordonnées tirées de madagascar_routes.geojson)
    private static final String ROUTES_GEOJSON = """
    {
      "type": "FeatureCollection",
      "features": [
        {
          "type": "Feature",
          "properties": { "ref": "RN1", "name": "Route Nationale 1", "from": "Antananarivo", "to": "Ambanja" },
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [47.5079,-18.8792],[47.55,-18.83],[47.6,-18.76],[47.7,-18.65],
              [47.8,-18.55],[47.9,-18.45],[48.0,-18.30],[48.1,-18.15],
              [48.2,-18.00],[48.3,-17.85],[48.4,-17.70],[48.5,-17.50],
              [48.6,-17.30],[48.65,-17.00],[48.7,-16.70],[48.75,-16.40],
              [48.8,-16.10],[48.85,-15.80],[48.9,-15.50],[48.95,-15.20],
              [49.0,-14.90],[49.05,-14.60],[49.0,-14.30],[48.95,-14.00],
              [48.9,-13.80],[48.85,-13.70],[48.8,-13.65],[48.7,-13.62],
              [48.6,-13.60],[48.5,-13.62],[48.4519,-13.6856]
            ]
          }
        },
        {
          "type": "Feature",
          "properties": { "ref": "RN2", "name": "Route Nationale 2", "from": "Antananarivo", "to": "Toamasina" },
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [47.5079,-18.8792],[47.6,-18.85],[47.7,-18.82],[47.8,-18.80],
              [47.9,-18.78],[48.0,-18.75],[48.1,-18.72],[48.2167,-18.95],
              [48.3,-18.80],[48.4,-18.70],[48.5,-18.60],[48.6,-18.50],
              [48.7,-18.40],[48.8,-18.30],[48.9,-18.25],[49.0,-18.20],
              [49.1,-18.18],[49.2,-18.16],[49.3,-18.15],[49.4026,-18.1443]
            ]
          }
        },
        {
          "type": "Feature",
          "properties": { "ref": "RN3", "name": "Route Nationale 3", "from": "Antananarivo", "to": "Mahajanga" },
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [47.5079,-18.8792],[47.45,-18.85],[47.40,-18.82],[47.35,-18.80],
              [47.30,-18.75],[47.25,-18.70],[47.20,-18.65],[47.15,-18.60],
              [47.1167,-18.3167],[47.05,-18.00],[47.0,-17.70],[46.95,-17.40],
              [46.9,-17.10],[46.85,-16.80],[46.8,-16.50],[46.75,-16.20],
              [46.7,-16.00],[46.65,-15.90],[46.6,-15.85],[46.55,-15.80],
              [46.5,-15.78],[46.4,-15.75],[46.3167,-15.7167]
            ]
          }
        },
        {
          "type": "Feature",
          "properties": { "ref": "RN7", "name": "Route Nationale 7", "from": "Antananarivo", "to": "Toliara" },
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [47.5079,-18.8792],[47.45,-18.95],[47.40,-19.00],[47.35,-19.05],
              [47.30,-19.10],[47.25,-19.15],[47.20,-19.20],[47.15,-19.30],
              [47.12,-19.50],[47.10,-19.70],[47.09,-19.85],[47.08,-20.00],
              [47.07,-20.20],[47.065,-20.40],[47.06,-20.60],[47.058,-20.80],
              [47.056,-21.00],[47.055,-21.20],[47.086,-21.453],[47.05,-21.60],
              [47.0,-21.80],[46.95,-22.00],[46.9,-22.20],[46.85,-22.40],
              [46.8,-22.60],[46.7,-22.80],[46.5,-23.00],[46.3,-23.10],
              [46.1,-23.20],[45.9,-23.25],[45.7,-23.30],[45.5,-23.33],
              [45.3,-23.35],[45.1,-23.355],[44.9,-23.355],[44.7,-23.355],
              [44.5,-23.355],[44.3,-23.355],[44.1,-23.355],[43.67,-23.355]
            ]
          }
        },
        {
          "type": "Feature",
          "properties": { "ref": "RN10", "name": "Route Nationale 10", "from": "Toliara", "to": "Fort Dauphin" },
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [43.67,-23.355],[43.8,-23.50],[44.0,-23.65],[44.2,-23.75],
              [44.4,-23.80],[44.6,-23.90],[44.8,-24.00],[45.0,-24.10],
              [45.2,-24.20],[45.4,-24.30],[45.6,-24.40],[45.8,-24.50],
              [46.0,-24.60],[46.2,-24.70],[46.4,-24.80],[46.6,-24.90],
              [46.8,-25.00],[46.9833,-25.0308]
            ]
          }
        }
      ]
    }
    """;

    /**
     * Génère la page HTML complète avec Leaflet qui affiche les routes.
     * Le MapController lance un HttpServer sur le port 8080 et sert cette page.
     * Le JS intégré :
     *   - affiche toutes les routes en toile de fond
     *   - met en valeur la route sélectionnée (polling /api/command)
     *   - supporte zoomRN et resetZoom via les commandes
     */
    public String genererPageHTML() {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <title>Carte SIG – Routes Nationales de Madagascar</title>

            <!-- Leaflet CSS -->
            <link rel="stylesheet"
                  href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>

            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body, #map { width: 100%; height: 100%; }

                /* Légende superposée */
                .legend-panel {
                    position: absolute; bottom: 30px; right: 10px; z-index: 1000;
                    background: rgba(255,255,255,0.92); border-radius: 8px;
                    padding: 12px 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.25);
                    font-family: Arial, sans-serif; font-size: 13px;
                }
                .legend-panel h4 { margin-bottom: 6px; font-size: 14px; }
                .legend-row { display: flex; align-items: center; gap: 8px; margin: 3px 0; }
                .legend-line { width: 28px; height: 4px; border-radius: 2px; }
                .legend-line.normal  { background: #3388ff; }
                .legend-line.selected { background: #ff4444; }

                /* Popup Simba */
                .simba-popup { font-family: Arial, sans-serif; font-size: 12px; }
                .simba-popup strong { color: #c0392b; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <div class="legend-panel">
                <h4>Légende</h4>
                <div class="legend-row"><div class="legend-line normal"></div> Route</div>
                <div class="legend-row"><div class="legend-line selected"></div> Sélectionnée</div>
            </div>

            <!-- Leaflet JS -->
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

            <script>
            // ─── Initialisation de la carte ──────────────────────────────
            const map = L.map('map', {
                center: [-20.0, 47.0],   // centre approximatif de Madagascar
                zoom: 6
            });

            // Fond de carte OpenStreetMap
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(map);

            // ─── GeoJSON des routes (intégré par le serveur Java) ────────
            const routesGeoJSON = %s;

            // ─── État ─────────────────────────────────────────────────────
            let allLayers = {};          // nom_route -> L.geoJSON layer
            let simbaMarkers = {};       // nom_route -> [L.marker, ...]
            let selectedRN = null;

            // ─── Style des routes ─────────────────────────────────────────
            function styleRoute(feature) {
                const isSelected = feature.properties.ref === selectedRN
                                || feature.properties.name === selectedRN;
                return {
                    color:   isSelected ? '#ff4444' : '#3388ff',
                    weight:  isSelected ? 5          : 3,
                    opacity: 1
                };
            }

            // ─── Création des couches ─────────────────────────────────────
            routesGeoJSON.features.forEach(feature => {
                const layer = L.geoJSON(feature, {
                    style: styleRoute,
                    onEachFeature: (f, l) => {
                        l.bindPopup(
                            '<strong>' + f.properties.ref + '</strong><br/>' +
                            f.properties.name + '<br/>' +
                            f.properties.from + ' → ' + f.properties.to
                        );
                    }
                }).addTo(map);

                // Clé = ref (ex "RN1") et aussi name pour la recherche
                allLayers[feature.properties.ref]  = layer;
                allLayers[feature.properties.name] = layer;
            });

            // ─── Charger les Simbas depuis l'API Java ─────────────────────
            async function chargerSimbas() {
                try {
                    const res  = await fetch('/api/data');
                    const data = await res.json();

                    // Nettoyer les anciens marqueurs
                    Object.values(simbaMarkers).flat().forEach(m => map.removeLayer(m));
                    simbaMarkers = {};

                    if (!data.routes) return;

                    data.routes.forEach(route => {
                        const markers = [];
                        if (route.simbas) {
                            route.simbas.forEach(s => {
                                // On place le marqueur sur le premier point de la route
                                // En réalité il faudrait interpoler selon le PK –
                                // ici on utilise le premier point comme approximation.
                                const routeFeature = routesGeoJSON.features.find(
                                    f => f.properties.name === route.nom || f.properties.ref === route.nom
                                );
                                if (!routeFeature) return;

                                const coords = routeFeature.geometry.coordinates;
                                // Interpolation linéaire sur la polyligne selon le ratio PK/distance
                                const ratio = Math.min(1, Math.max(0, s.pk / route.distance));
                                const idx   = Math.min(coords.length - 2, Math.floor(ratio * (coords.length - 1)));
                                const frac  = (ratio * (coords.length - 1)) - idx;
                                const lng   = coords[idx][0] + frac * (coords[idx+1][0] - coords[idx][0]);
                                const lat   = coords[idx][1] + frac * (coords[idx+1][1] - coords[idx][1]);

                                const icon = L.divIcon({
                                    className: '',
                                    html: '<div style="width:12px;height:12px;background:#c0392b;border:2px solid #fff;border-radius:50%;box-shadow:0 1px 3px rgba(0,0,0,.4);"></div>',
                                    iconSize: [12, 12],
                                    iconAnchor: [6, 6]
                                });

                                const m = L.marker([lat, lng], { icon })
                                    .bindPopup(
                                        '<div class="simba-popup">' +
                                        '<strong>Simba – ' + route.nom + '</strong><br/>' +
                                        'PK : ' + s.pk + ' km<br/>' +
                                        'Surface : ' + s.surface + ' m²<br/>' +
                                        'Profondeur : ' + s.profondeur + ' m</div>'
                                    )
                                    .addTo(map);
                                markers.push(m);
                            });
                        }
                        simbaMarkers[route.nom] = markers;
                    });
                } catch (e) {
                    console.warn('Impossible de charger les simbas :', e);
                }
            }

            // ─── Polling des commandes depuis le serveur Java ─────────────
            async function pollCommands() {
                try {
                    const res = await fetch('/api/command');
                    const cmd = await res.json();

                    if (cmd && cmd.action) {
                        if (cmd.action === 'zoomRN' && cmd.rn) {
                            zoomSurRoute(cmd.rn);
                        } else if (cmd.action === 'resetZoom') {
                            map.setView([-20.0, 47.0], 6);
                            highlightRoute(null);
                        }
                    }
                } catch (e) { /* ignore */ }
            }

            // ─── Zoom sur une route + mise en valeur ──────────────────────
            function zoomSurRoute(rnName) {
                selectedRN = rnName;
                highlightRoute(rnName);

                // Chercher la couche pour faire fitBounds
                const layer = allLayers[rnName];
                if (layer) {
                    map.fitBounds(layer.getBounds(), { padding: [40, 40] });
                }

                // Recharger les simbas à chaque sélection
                chargerSimbas();
            }

            // ─── Remettre à jour les couleurs des routes ──────────────────
            function highlightRoute(rnName) {
                routesGeoJSON.features.forEach(feature => {
                    const key   = feature.properties.ref;
                    const layer = allLayers[key];
                    if (!layer) return;

                    const isSelected = (feature.properties.ref === rnName
                                     || feature.properties.name === rnName);
                    layer.setStyle({
                        color:  isSelected ? '#ff4444' : '#3388ff',
                        weight: isSelected ? 5          : 3
                    });

                    // Mettre la route sélectionnée au-dessus
                    if (isSelected) layer.bringToFront();
                });
            }

            // ─── Démarrage ────────────────────────────────────────────────
            chargerSimbas();
            setInterval(pollCommands, 800);   // polling toutes les 800 ms
            </script>
        </body>
        </html>
        """.formatted(ROUTES_GEOJSON);
    }
}