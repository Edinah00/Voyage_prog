DROP TABLE IF EXISTS voiture CASCADE;

CREATE TABLE voiture (
    nom VARCHAR(100) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    vitesse_maximale NUMERIC(10, 2) NOT NULL,
    longueur NUMERIC(10, 2) NOT NULL,
    largeur NUMERIC(10, 2) NOT NULL,
    reservoir NUMERIC(10, 2) NOT NULL,
    consommation NUMERIC(10, 2) NOT NULL
);

INSERT INTO voiture (nom, type, vitesse_maximale, longueur, largeur, reservoir, consommation) 
VALUES ('Petite Eco', 'Citadine', 80.0, 3.90, 1.75, 20.0, 5.0);

INSERT INTO voiture (nom, type, vitesse_maximale, longueur, largeur, reservoir, consommation) 
VALUES ('Golf', 'Berline', 90.0, 4.50, 1.80, 50.0, 10.0);

INSERT INTO voiture (nom, type, vitesse_maximale, longueur, largeur, reservoir, consommation) 
VALUES ('Gros 4x4', '4x4', 80.0, 5.20, 1.90, 50.0, 9.0);

INSERT INTO voiture (nom, type, vitesse_maximale, longueur, largeur, reservoir, consommation) 
VALUES ('Super Eco', 'Hybride', 85.0, 4.20, 1.78, 40.0, 4.0);

COMMIT;