package services;

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
"        #missing-coords {\n" +
"            position: absolute;\n" +
"            bottom: 10px;\n" +
"            left: 10px;\n" +
"            background: #fff3cd;\n" +
"            border: 1px solid #ffc107;\n" +
"            padding: 10px 15px;\n" +
"            border-radius: 6px;\n" +
"            z-index: 1000;\n" +
"            font-size: 13px;\n" +
"            display: none;\n" +
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
"    <div id='missing-coords'>\n" +
"        ⚠️ <strong>Routes non affichées (coordonnées manquantes) :</strong> <span id='missing-list'></span>\n" +
"    </div>\n" +
"\n" +
"    <!-- Leaflet JS -->\n" +
"    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
"    \n" +
"    <script>\n" +
"        // Coordonnées de Madagascar (centre approximatif)\n" +
"        const MADAGASCAR_CENTER = [-20.0, 47.0];\n" +
"        \n" +
"        // Coordonnées de TOUTES les villes principales de Madagascar\n" +
"        const CITY_COORDS = {\n" +
"            // Grandes villes\n" +
"            'Antananarivo': [-18.9141, 47.5370],\n" +
"            'Toamasina':    [-18.1443, 49.4026],\n" +
"            'Antsirabe':    [-19.8658, 47.0368],\n" +
"            'Toliara':      [-23.5392, 43.5694],\n" +
"            'Mahajanga':    [-15.6667, 46.1667],\n" +
"            'Antsiranana':  [-12.2808, 49.2967],\n" +
"            'Fianarantsoa': [-20.4535, 47.1081],\n" +
"            'Tulear':       [-23.5392, 43.5694],\n" +
"\n" +
"            // Villes moyennes\n" +
"            'Sambaina':     [-19.0000, 47.2000],\n" +
"            'Ampefy':       [-19.0333, 46.7167],\n" +
"            'Ambohitanjaka':[-18.9833, 47.4500],\n" +
"            'Ambanja':      [-13.4000, 48.0167],\n" +
"            'Antalaha':     [-16.9000, 49.8833],\n" +
"            'Masoala':      [-15.9167, 50.1500],\n" +
"            'Mananara':     [-16.2667, 49.7667],\n" +
"            'Mananjara':    [-16.2167, 49.7500],\n" +
"            'Soubra':       [-17.3000, 49.7167],\n" +
"            'Nosy Be':      [-13.5333, 48.4000],\n" +
"            'Diego Suarez': [-12.2808, 49.2967],\n" +
"            'Majunga':      [-15.6667, 46.1667],\n" +
"            'Mombasa':      [-15.6667, 46.1667],\n" +
"            'Morondava':    [-20.1000, 44.0833],\n" +
"            'Fort Dauphin':  [-25.0333, 46.9833],\n" +
"            'Fort-Dauphin':  [-25.0333, 46.9833],\n" +
"            'Fte Dauphin':   [-25.0333, 46.9833],\n" +
"            'Tolagnaro':    [-25.0333, 46.9833],\n" +
"            'Farafangana':  [-24.4333, 47.5167],\n" +
"            'Ihosy':        [-22.3833, 46.0167],\n" +
"            'Betioky':      [-22.7167, 44.3667],\n" +
"            'Ranohirya':    [-22.5000, 45.5000],\n" +
"            'Ifaty':        [-23.0833, 43.6167],\n" +
"            'Manakara':     [-24.2667, 47.5167],\n" +
"            'Mananjary':    [-23.9667, 48.1000],\n" +
"            'Nosy Boraha':  [-23.5833, 47.6167],\n" +
"            'Ambilobe':     [-13.1667, 48.8500],\n" +
"            'Ankarana':     [-13.5833, 48.8000],\n" +
"            'Kie':          [-14.3333, 48.3833],\n" +
"            'Madamongou':   [-14.6667, 48.1833],\n" +
"            'Vohemar':      [-14.3167, 49.7833],\n" +
"            'Vohémar':      [-14.3167, 49.7833],\n" +
"            'Ankalambe':    [-14.8333, 48.6667],\n" +
"            'Ambongandrefana': [-15.0000, 49.0000],\n" +
"            'Belo-sur-Tsiribihina': [-19.4833, 44.6500],\n" +
"            'Manara':       [-16.2000, 49.7000],\n" +
"            'Barrages':     [-16.5000, 49.0000],\n" +
"            'Ankarana Reserve': [-13.5833, 48.8000],\n" +
"            'Ambohidrabibi': [-19.0000, 47.1000],\n" +
"            'Vakinankaratra': [-19.5000, 47.0000],\n" +
"            'Vakinankaratra': [-19.5000, 47.0000],\n" +
"            'Betampona':    [-17.9333, 49.3500],\n" +
"            'Pangalanes':   [-20.6667, 48.5000],\n" +
"            'Rakirovira':   [-17.4833, 49.5000],\n" +
"            'Maroantsetra': [-16.4000, 49.9167],\n" +
"            'Masoala':      [-15.9167, 50.1500],\n" +
"            'Ile Sainte-Marie': [-17.1333, 49.5500],\n" +
"            'Ile aux Nattes': [-17.3833, 49.6667],\n" +
"            'Sokirina':     [-18.4167, 49.0500],\n" +
"            'Vatomandry':   [-18.8333, 48.8000],\n" +
"            'Elapona':      [-18.9500, 48.3167],\n" +
"            'SLambda':      [-18.6333, 48.9500],\n" +
"            'Manara':       [-18.4500, 49.0000],\n" +
"            'Deported':     [-18.6000, 49.0500],\n" +
"            'Soatraka':     [-18.6500, 49.1000],\n" +
"            'Amparaibe':    [-18.0333, 49.3000],\n" +
"            'Foulpointe':   [-18.2333, 49.4333],\n" +
"            'Andevoranto':  [-18.7000, 48.9000],\n" +
"            'Ambanja':      [-13.4000, 48.0167],\n" +
"            'Antalaha':     [-16.9000, 49.8833],\n" +
"            'Andringitra':  [-21.7333, 46.6000],\n" +
"            'Ranomafana':   [-21.2500, 47.4333],\n" +
"            'Ambohy':       [-19.0000, 46.7000],\n" +
"            'Miarinarivo':  [-19.0000, 47.7500],\n" +
"            'Moramanga':    [-18.6833, 48.3833],\n" +
"            'Ambatomitsuka': [-18.7500, 48.4500],\n" +
"            'Amba':         [-19.0500, 47.5000],\n" +
"            'Anosy':        [-24.5000, 47.0000],\n" +
"            'Araroba':      [-24.0000, 47.0000],\n" +
"            'Kambana':      [-22.0000, 47.0000],\n" +
"            'Safo':         [-18.0000, 47.3000],\n" +
"            'Tsarabarivar': [-19.4500, 46.8500],\n" +
"            'Kalimanzo':    [-19.3000, 46.7500]\n" +
"        };\n" +
"        \n" +
"        // Initialiser la carte\n" +
"        const map = L.map('map').setView(MADAGASCAR_CENTER, 6);\n" +
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
"        let missingRoutes = [];\n" +
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
"            missingRoutes = [];\n" +
"            \n" +
"            // Afficher chaque route\n" +
"            routesData.forEach(route => {\n" +
"                const coordDebut = CITY_COORDS[route.extremiteGauche];\n" +
"                const coordFin = CITY_COORDS[route.extremiteDroite];\n" +
"                \n" +
"                if (!coordDebut || !coordFin) {\n" +
"                    // Collecter les routes manquantes pour les afficher\n" +
"                    let msg = route.nom + ' (';\n" +
"                    if (!coordDebut) msg += route.extremiteGauche + ' ';\n" +
"                    if (!coordFin)   msg += route.extremiteDroite;\n" +
"                    msg += ')';\n" +
"                    missingRoutes.push(msg);\n" +
"                    console.warn('Coordonnées manquantes pour:', route.nom, route.extremiteGauche, route.extremiteDroite);\n" +
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
"                // Label du nom de la route\n" +
"                const midLat = (coordDebut[0] + coordFin[0]) / 2;\n" +
"                const midLng = (coordDebut[1] + coordFin[1]) / 2;\n" +
"                L.marker([midLat, midLng], {\n" +
"                    icon: L.divIcon({\n" +
"                        className: '',\n" +
"                        html: '<div style=\"background:white;border:1px solid #ccc;border-radius:4px;padding:2px 6px;font-size:11px;font-weight:bold;white-space:nowrap;\">' + route.nom + '</div>',\n" +
"                        iconSize: [50, 20],\n" +
"                        iconAnchor: [25, 10]\n" +
"                    })\n" +
"                }).addTo(map);\n" +
"                \n" +
"                // Popup pour la route\n" +
"                routeLine.bindPopup(\n" +
"                    '<b>' + route.nom + '</b><br>' +\n" +
"                    'Distance: ' + route.distance + ' km<br>' +\n" +
"                    'De: ' + route.extremiteGauche + '<br>' +\n" +
"                    'À: ' + route.extremiteDroite + '<br>' +\n" +
"                    'SIMBA: ' + route.simbas.length\n" +
"                );\n" +
"                \n" +
"                // Événement de clic sur la route\n" +
"                routeLine.on('click', function() {\n" +
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
"            \n" +
"            // Afficher l'avertissement si des routes manquent\n" +
"            const missingDiv = document.getElementById('missing-coords');\n" +
"            if (missingRoutes.length > 0) {\n" +
"                document.getElementById('missing-list').textContent = missingRoutes.join(' | ');\n" +
"                missingDiv.style.display = 'block';\n" +
"            } else {\n" +
"                missingDiv.style.display = 'none';\n" +
"            }\n" +
"        }\n" +
"        \n" +
"        // Afficher les SIMBA d'une route\n" +
"        function afficherSIMBA(route, coordDebut, coordFin) {\n" +
"            const markers = [];\n" +
"            \n" +
"            route.simbas.forEach(function(simba, index) {\n" +
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
"                    '<b>SIMBA ' + (index + 1) + '</b><br>' +\n" +
"                    'PK: ' + simba.pk.toFixed(2) + ' km<br>' +\n" +
"                    'Surface: ' + simba.surface.toFixed(2) + ' m²<br>' +\n" +
"                    'Profondeur: ' + simba.profondeur.toFixed(2) + ' m'\n" +
"                );\n" +
"                \n" +
"                // Événement de clic\n" +
"                marker.on('click', function() {\n" +
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
"                '<h4 style=\"margin: 0 0 10px 0; color: #0066ff;\">' + route.nom + '</h4>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>Distance:</strong> ' + route.distance + ' km</p>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>Départ:</strong> ' + route.extremiteGauche + '</p>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>Arrivée:</strong> ' + route.extremiteDroite + '</p>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>SIMBA:</strong> ' + route.simbas.length + '</p>';\n" +
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
"                '<h4 style=\"margin: 0 0 10px 0; color: #ff4444;\">SIMBA ' + numero + '</h4>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>PK:</strong> ' + simba.pk.toFixed(2) + ' km</p>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>Surface:</strong> ' + simba.surface.toFixed(2) + ' m²</p>' +\n" +
"                '<p style=\"margin: 5px 0;\"><strong>Profondeur:</strong> ' + simba.profondeur.toFixed(2) + ' m</p>';\n" +
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
"        // Actualiser les données toutes les 3 secondes\n" +
"        setInterval(chargerDonnees, 3000);\n" +
"    </script>\n" +
"</body>\n" +
"</html>";
    }
}