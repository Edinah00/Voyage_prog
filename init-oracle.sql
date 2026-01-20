DROP TABLE lavaka CASCADE CONSTRAINTS;
DROP TABLE lalana CASCADE CONSTRAINTS;
DROP SEQUENCE lavaka_seq;
DROP TABLE pause CASCADE CONSTRAINTS;
DROP SEQUENCE pause_seq;

CREATE SEQUENCE lavaka_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE pause_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE lalana (
    nom VARCHAR2(100) PRIMARY KEY,
    extremite_gauche VARCHAR2(100) NOT NULL,
    extremite_droite VARCHAR2(100) NOT NULL,
    distance NUMBER(10, 2) NOT NULL,
    largeur NUMBER(10, 2) NOT NULL
);

CREATE TABLE pause (
    id NUMBER PRIMARY KEY,
    lalana_nom VARCHAR2(100) NOT NULL,
    position NUMBER(10, 2) NOT NULL,
    heure_debut VARCHAR2(5) NOT NULL,
    heure_fin VARCHAR2(5) NOT NULL,
    CONSTRAINT fk_pause_lalana FOREIGN KEY (lalana_nom) 
        REFERENCES lalana(nom) ON DELETE CASCADE,
    CONSTRAINT chk_position CHECK (position >= 0),
    CONSTRAINT chk_heure_format_debut CHECK (REGEXP_LIKE(heure_debut, '^[0-2][0-9]:[0-5][0-9]$')),
    CONSTRAINT chk_heure_format_fin CHECK (REGEXP_LIKE(heure_fin, '^[0-2][0-9]:[0-5][0-9]$'))
);

CREATE TABLE lavaka (
    id NUMBER PRIMARY KEY,
    debut NUMBER(10, 2) NOT NULL,
    fin NUMBER(10, 2) NOT NULL,
    ralentissement NUMBER(5, 4) NOT NULL,
    lalana_nom VARCHAR2(100) NOT NULL,
    CONSTRAINT fk_lavaka_lalana FOREIGN KEY (lalana_nom) 
        REFERENCES lalana(nom) ON DELETE CASCADE,
    CONSTRAINT chk_debut_fin CHECK (debut < fin),
    CONSTRAINT chk_ralentissement CHECK (ralentissement >= 0 AND ralentissement <= 1)
);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Sambaina', 'Antananarivo', 'Sambaina', 200.0, 7.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Sambaina-Antsirabe', 'Sambaina', 'Antsirabe', 120.0, 8.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Ampefy', 'Antananarivo', 'Ampefy', 163.0, 7.0);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Ampefy-Sambaina', 'Ampefy', 'Sambaina', 88.0, 7.0);


-- Route: Antananarivo → Sambaina
INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom)
VALUES (1, 30, 77, 0.22, 'Antananarivo-Sambaina');

-- Route: Sambaina → Antsirabe
INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom)
VALUES (2, 12, 25, 0.66, 'Sambaina-Antsirabe');

INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom)
VALUES (3, 56, 70, 0.11, 'Sambaina-Antsirabe');

-- Route: Ampefy → Sambaina
INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom)
VALUES (4, 25, 32, 0.15, 'Ampefy-Sambaina');
-- ============================================================
-- Création de la table VOYAGE
-- ============================================================
CREATE TABLE voyage (
    id NUMBER PRIMARY KEY,
    depart VARCHAR2(100) NOT NULL,
    arrivee VARCHAR2(100) NOT NULL,
    voiture_nom VARCHAR2(100) NOT NULL,
    vitesse_moyenne NUMBER(5, 2) NOT NULL,
    heure_depart TIMESTAMP NOT NULL,
    temps_ecoule NUMBER(10, 2) DEFAULT 0,
    position_actuelle NUMBER(10, 2) DEFAULT 0,
    index_chemin_actuel NUMBER DEFAULT 0,
    distance_totale NUMBER(10, 2) DEFAULT 0,
    termine NUMBER(1) DEFAULT 0,
    en_pause NUMBER(1) DEFAULT 0,
    CONSTRAINT fk_voyage_voiture FOREIGN KEY (voiture_nom) REFERENCES voiture(nom) ON DELETE CASCADE,
    CONSTRAINT chk_vitesse_moyenne CHECK (vitesse_moyenne > 0),
    CONSTRAINT chk_temps_ecoule CHECK (temps_ecoule >= 0),
    CONSTRAINT chk_position CHECK (position_actuelle >= 0),
    CONSTRAINT chk_termine CHECK (termine IN (0, 1)),
    CONSTRAINT chk_en_pause CHECK (en_pause IN (0, 1))
);

-- Séquence pour générer les IDs
CREATE SEQUENCE voyage_seq START WITH 1 INCREMENT BY 1;

-- ============================================================
-- Table de liaison pour le chemin choisi (List<Lalana>)
-- ============================================================

CREATE TABLE voyage_chemin (
    voyage_id NUMBER NOT NULL,
    lalana_nom VARCHAR2(100) NOT NULL,
    ordre NUMBER NOT NULL,
    CONSTRAINT pk_voyage_chemin PRIMARY KEY (voyage_id, ordre),
    CONSTRAINT fk_vchemin_voyage FOREIGN KEY (voyage_id) REFERENCES voyage(id) ON DELETE CASCADE,
    CONSTRAINT fk_vchemin_lalana FOREIGN KEY (lalana_nom) REFERENCES lalana(nom) ON DELETE CASCADE
);

-- ============================================================
-- INSERTION DU VOYAGE: Antananarivo → Antsirabe (70 km/h)
-- ============================================================

INSERT INTO voyage (id, depart, arrivee, voiture_nom, vitesse_moyenne, heure_depart, temps_ecoule, position_actuelle, index_chemin_actuel, distance_totale, termine, en_pause)
VALUES (voyage_seq.NEXTVAL, 'Antananarivo', 'Antsirabe', 'VMoyenne', 70, TIMESTAMP '2026-01-14 08:00:00', 0, 0, 0, 0, 0, 0);

-- Insertion du chemin: Antananarivo → Sambaina → Antsirabe
INSERT INTO voyage_chemin (voyage_id, lalana_nom, ordre)
VALUES (voyage_seq.CURRVAL, 'Antananarivo-Sambaina', 1);

INSERT INTO voyage_chemin (voyage_id, lalana_nom, ordre)
VALUES (voyage_seq.CURRVAL, 'Sambaina-Antsirabe', 2);

-- ============================================================
-- Mise à jour de la distance totale
-- ============================================================

UPDATE voyage v
SET distance_totale = (
    SELECT SUM(l.distance)
    FROM voyage_chemin vc
    JOIN lalana l ON vc.lalana_nom = l.nom
    WHERE vc.voyage_id = v.id
)
WHERE id = voyage_seq.CURRVAL;

COMMIT;