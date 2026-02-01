package src.java.services;

/**
 * Service pour générer le code HTML/JavaScript de la carte interactive
 * Utilise Leaflet pour l'affichage cartographique
 */
public class MapService {

    public String genererPageHTML() {
        return "<!DOCTYPE html>\n" +
"<html>\n" +
"<head>\n" +
"    <meta charset='utf-8' />\n" +
"    <title>Carte SIG - Routes Nationales Madagascar</title>\n" +
"    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
"    \n" +
"    <!-- Leaflet CSS -->\n" +
"    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />\n" +
"    \n" +
"    <style>\n" +
"        body {\n" +
"            margin: 0;\n" +
"            padding: 0;\n" +
"            font-family: Arial, sans-serif;\n" +
"        }\n" +
"        #map {\n" +
"            position: absolute;\n" +
"            top: 60px;\n" +
"            left: 0;\n" +
"            bottom: 0;\n" +
"            right: 0;\n" +
"        }\n" +
"        #header {\n" +
"            position: absolute;\n" +
"            top: 0;\n" +
"            left: 0;\n" +
"            right: 0;\n" +
"            height: 60px;\n" +
"            background: linear-gradient(135deg, #000000 0%, #434343 100%);\n" +
"            color: white;\n" +
"            display: flex;\n" +
"            align-items: center;\n" +
"            padding: 0 20px;\n" +
"            box-shadow: 0 2px 10px rgba(0,0,0,0.3);\n" +
"            z-index: 1000;\n" +
"        }\n" +
"        #header h1 {\n" +
"            margin: 0;\n" +
"            font-size: 24px;\n" +
"            font-weight: bold;\n" +
"        }\n" +
"        #info-box {\n" +
"            position: absolute;\n" +
"            top: 70px;\n" +
"            right: 10px;\n" +
"            background: white;\n" +
"            padding: 15px;\n" +
"            border-radius: 8px;\n" +
"            box-shadow: 0 2px 10px rgba(0,0,0,0.2);\n" +
"            z-index: 1000;\n" +
"            max-width: 300px;\n" +
"            max-height: 400px;\n" +
"            overflow-y: auto;\n" +
"        }\n" +
"        .simba-marker {\n" +
"            background-color: #ff4444;\n" +
"            border: 2px solid #cc0000;\n" +
"            border-radius: 50%;\n" +
"            width: 12px;\n" +
"            height: 12px;\n" +
"        }\n" +
"        .route-line {\n" +
"            stroke: #000000;\n" +
"            stroke-width: 4;\n" +
"            fill: none;\n" +
"        }\n" +
"        .route-line-selected {\n" +
"            stroke: #0066ff;\n" +
"            stroke-width: 6;\n" +
"            fill: none;\n" +
"        }\n" +
"    </style>\n" +
"</head>\n" +
"<body>\n" +
"    <div id='header'>\n" +
"        <h1>🗺️ Carte SIG - Routes Nationales Madagascar</h1>\n" +
"    </div>\n" +
"    \n" +
"    <div id='map'></div>\n" +
"    <div id='info-box' style='display: none;'>\n" +
"        <h3 style='margin-top: 0;'>Informations</h3>\n" +
"        <div id='info-content'></div>\n" +
"    </div>\n" +
"\n" +
"    <!-- Leaflet JS -->\n" +
"    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
"    \n" +
"    <script>\n" +
"        // Coordonnées de Madagascar (centre approximatif)\n" +
"        const MADAGASCAR_CENTER = [-18.8792, 47.5079];\n" +
"        \n" +
"        // Coordonnées approximatives des villes principales\n" +
"        const CITY_COORDS = {\n" +
"            'Antananarivo': [-18.8792, 47.5079],\n" +
"            'Toamasina': [-18.1443, 49.4026],\n" +
"            'Antsirabe': [-19.8658, 47.0368],\n" +
"            'Sambaina': [-19.0000, 47.2000],\n" +
"            'Ampefy': [-19.0333, 46.7167]\n" +
"        };\n" +
"        \n" +
"        // Initialiser la carte\n" +
"        const map = L.map('map').setView(MADAGASCAR_CENTER, 7);\n" +
"        \n" +
"        // Ajouter le fond de carte OpenStreetMap\n" +
"        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
"            attribution: '© OpenStreetMap contributors',\n" +
"            maxZoom: 18\n" +
"        }).addTo(map);\n" +
"        \n" +
"        // Variables globales\n" +
"        let routesData = [];\n" +
"        let routeLayers = {};\n" +
"        let simbaMarkers = {};\n" +
"        let selectedRN = null;\n" +
"        \n" +
"        // Charger les données depuis l'API\n" +
"        async function chargerDonnees() {\n" +
"            try {\n" +
"                const response = await fetch('http://localhost:8080/api/data');\n" +
"                const data = await response.json();\n" +
"                routesData = data.routes;\n" +
"                selectedRN = data.selectedRN;\n" +
"                afficherRoutes();\n" +
"            } catch (error) {\n" +
"                console.error('Erreur lors du chargement des données:', error);\n" +
"            }\n" +
"        }\n" +
"        \n" +
"        // Afficher toutes les routes\n" +
"        function afficherRoutes() {\n" +
"            // Effacer les couches existantes\n" +
"            Object.values(routeLayers).forEach(layer => map.removeLayer(layer));\n" +
"            Object.values(simbaMarkers).forEach(markers => {\n" +
"                markers.forEach(marker => map.removeLayer(marker));\n" +
"            });\n" +
"            \n" +
"            routeLayers = {};\n" +
"            simbaMarkers = {};\n" +
"            \n" +
"            // Afficher chaque route\n" +
"            routesData.forEach(route => {\n" +
"                const coordDebut = CITY_COORDS[route.extremiteGauche];\n" +
"                const coordFin = CITY_COORDS[route.extremiteDroite];\n" +
"                \n" +
"                if (!coordDebut || !coordFin) {\n" +
"                    console.warn('Coordonnées manquantes pour:', route.nom);\n" +
"                    return;\n" +
"                }\n" +
"                \n" +
"                // Créer la ligne de la route\n" +
"                const isSelected = route.nom === selectedRN;\n" +
"                const routeLine = L.polyline(\n" +
"                    [coordDebut, coordFin],\n" +
"                    {\n" +
"                        color: isSelected ? '#0066ff' : '#000000',\n" +
"                        weight: isSelected ? 6 : 4,\n" +
"                        opacity: 0.8\n" +
"                    }\n" +
"                ).addTo(map);\n" +
"                \n" +
"                // Popup pour la route\n" +
"                routeLine.bindPopup(\n" +
"                    `<b>${route.nom}</b><br>` +\n" +
"                    `Distance: ${route.distance} km<br>` +\n" +
"                    `SIMBA: ${route.simbas.length}`\n" +
"                );\n" +
"                \n" +
"                // Événement de clic sur la route\n" +
"                routeLine.on('click', () => {\n" +
"                    selectionnerRN(route.nom);\n" +
"                    afficherInfosRoute(route);\n" +
"                    envoyerCommandeVersJava('selectRN', route.nom);\n" +
"                });\n" +
"                \n" +
"                routeLayers[route.nom] = routeLine;\n" +
"                \n" +
"                // Afficher les SIMBA si la route est sélectionnée\n" +
"                if (isSelected) {\n" +
"                    afficherSIMBA(route, coordDebut, coordFin);\n" +
"                    map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });\n" +
"                }\n" +
"            });\n" +
"        }\n" +
"        \n" +
"        // Afficher les SIMBA d'une route\n" +
"        function afficherSIMBA(route, coordDebut, coordFin) {\n" +
"            const markers = [];\n" +
"            \n" +
"            route.simbas.forEach((simba, index) => {\n" +
"                // Calculer la position du SIMBA le long de la route\n" +
"                const ratio = simba.pk / route.distance;\n" +
"                const lat = coordDebut[0] + (coordFin[0] - coordDebut[0]) * ratio;\n" +
"                const lng = coordDebut[1] + (coordFin[1] - coordDebut[1]) * ratio;\n" +
"                \n" +
"                // Créer le marqueur\n" +
"                const marker = L.circleMarker([lat, lng], {\n" +
"                    radius: 8,\n" +
"                    fillColor: '#ff4444',\n" +
"                    color: '#cc0000',\n" +
"                    weight: 2,\n" +
"                    opacity: 1,\n" +
"                    fillOpacity: 0.8\n" +
"                }).addTo(map);\n" +
"                \n" +
"                // Popup pour le SIMBA\n" +
"                marker.bindPopup(\n" +
"                    `<b>SIMBA ${index + 1}</b><br>` +\n" +
"                    `PK: ${simba.pk.toFixed(2)} km<br>` +\n" +
"                    `Surface: ${simba.surface.toFixed(2)} m²<br>` +\n" +
"                    `Profondeur: ${simba.profondeur.toFixed(2)} m`\n" +
"                );\n" +
"                \n" +
"                // Événement de clic\n" +
"                marker.on('click', () => {\n" +
"                    afficherInfosSIMBA(simba, index + 1);\n" +
"                });\n" +
"                \n" +
"                markers.push(marker);\n" +
"            });\n" +
"            \n" +
"            simbaMarkers[route.nom] = markers;\n" +
"        }\n" +
"        \n" +
"        // Sélectionner une route\n" +
"        function selectionnerRN(nomRN) {\n" +
"            selectedRN = nomRN;\n" +
"            afficherRoutes();\n" +
"        }\n" +
"        \n" +
"        // Afficher les informations d'une route\n" +
"        function afficherInfosRoute(route) {\n" +
"            const infoBox = document.getElementById('info-box');\n" +
"            const infoContent = document.getElementById('info-content');\n" +
"            \n" +
"            infoContent.innerHTML = \n" +
"                `<h4 style='margin: 0 0 10px 0; color: #0066ff;'>${route.nom}</h4>` +\n" +
"                `<p style='margin: 5px 0;'><strong>Distance:</strong> ${route.distance} km</p>` +\n" +
"                `<p style='margin: 5px 0;'><strong>Départ:</strong> ${route.extremiteGauche}</p>` +\n" +
"                `<p style='margin: 5px 0;'><strong>Arrivée:</strong> ${route.extremiteDroite}</p>` +\n" +
"                `<p style='margin: 5px 0;'><strong>SIMBA:</strong> ${route.simbas.length}</p>`;\n" +
"            \n" +
"            infoBox.style.display = 'block';\n" +
"        }\n" +
"        \n" +
"        // Afficher les informations d'un SIMBA\n" +
"        function afficherInfosSIMBA(simba, numero) {\n" +
"            const infoBox = document.getElementById('info-box');\n" +
"            const infoContent = document.getElementById('info-content');\n" +
"            \n" +
"            infoContent.innerHTML = \n" +
"                `<h4 style='margin: 0 0 10px 0; color: #ff4444;'>SIMBA ${numero}</h4>` +\n" +
"                `<p style='margin: 5px 0;'><strong>PK:</strong> ${simba.pk.toFixed(2)} km</p>` +\n" +
"                `<p style='margin: 5px 0;'><strong>Surface:</strong> ${simba.surface.toFixed(2)} m²</p>` +\n" +
"                `<p style='margin: 5px 0;'><strong>Profondeur:</strong> ${simba.profondeur.toFixed(2)} m</p>`;\n" +
"            \n" +
"            infoBox.style.display = 'block';\n" +
"        }\n" +
"        \n" +
"        // Envoyer une commande vers Java\n" +
"        async function envoyerCommandeVersJava(action, rn) {\n" +
"            try {\n" +
"                await fetch('http://localhost:8080/api/command', {\n" +
"                    method: 'POST',\n" +
"                    headers: { 'Content-Type': 'application/json' },\n" +
"                    body: JSON.stringify({ action: action, rn: rn })\n" +
"                });\n" +
"            } catch (error) {\n" +
"                console.error('Erreur lors de l\\'envoi de la commande:', error);\n" +
"            }\n" +
"        }\n" +
"        \n" +
"        // Charger les données au démarrage\n" +
"        chargerDonnees();\n" +
"        \n" +
"        // Actualiser les données toutes les 2 secondes\n" +
"        setInterval(chargerDonnees, 2000);\n" +
"    </script>\n" +
"</body>\n" +
"</html>";
    }
}