DROP TABLE prix_reparation CASCADE CONSTRAINTS;
DROP TABLE type_reparation CASCADE CONSTRAINTS;
DROP TABLE voyage_chemin CASCADE CONSTRAINTS;
DROP TABLE voyage CASCADE CONSTRAINTS;
DROP TABLE lavaka CASCADE CONSTRAINTS;
DROP TABLE pause CASCADE CONSTRAINTS;
DROP TABLE lalana CASCADE CONSTRAINTS;

DROP SEQUENCE lavaka_seq;
DROP SEQUENCE pause_seq;
DROP SEQUENCE type_reparation_seq;
DROP SEQUENCE prix_reparation_seq;
DROP SEQUENCE voyage_seq;



CREATE SEQUENCE lavaka_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE pause_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE type_reparation_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE prix_reparation_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE voyage_seq START WITH 1 INCREMENT BY 1;

-- ============================================================
-- TABLE LALANA
-- ============================================================
CREATE TABLE lalana (
    nom VARCHAR2(100) PRIMARY KEY,
    extremite_gauche VARCHAR2(100) NOT NULL,
    extremite_droite VARCHAR2(100) NOT NULL,
    distance NUMBER(10, 2) NOT NULL,
    largeur NUMBER(10, 2) NOT NULL
);

-- ============================================================
-- TABLE PAUSE
-- ============================================================
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

-- ============================================================
-- TABLE LAVAKA (avec tous les champs)
-- ============================================================
CREATE TABLE lavaka (
    id NUMBER PRIMARY KEY,
    debut NUMBER(10, 2) NOT NULL,
    fin NUMBER(10, 2) NOT NULL,
    ralentissement NUMBER(5, 4) NOT NULL,
    lalana_nom VARCHAR2(100) NOT NULL,
    point_kilometrique NUMBER(10, 2),
    surface NUMBER(10, 2) DEFAULT 0,
    profondeur NUMBER(5, 2) DEFAULT 0,
    CONSTRAINT fk_lavaka_lalana FOREIGN KEY (lalana_nom) 
        REFERENCES lalana(nom) ON DELETE CASCADE,
    CONSTRAINT chk_debut_fin CHECK (debut < fin),
    CONSTRAINT chk_ralentissement CHECK (ralentissement >= 0 AND ralentissement <= 1),
    CONSTRAINT chk_pk CHECK (point_kilometrique >= 0),
    CONSTRAINT chk_surface CHECK (surface >= 0),
    CONSTRAINT chk_profondeur_lavaka CHECK (profondeur >= 0)
);

-- ============================================================
-- TABLE TYPE_REPARATION
-- ============================================================
CREATE TABLE type_reparation (
    id NUMBER PRIMARY KEY,
    nom VARCHAR2(50) NOT NULL UNIQUE,
    description VARCHAR2(200)
);

-- ============================================================
-- TABLE PRIX_REPARATION
-- ============================================================
CREATE TABLE prix_reparation (
    id NUMBER PRIMARY KEY,
    type_reparation_id NUMBER NOT NULL,
    profondeur_min NUMBER(5, 2) NOT NULL,
    profondeur_max NUMBER(5, 2) NOT NULL,
    prix_par_m2 NUMBER(10, 2) NOT NULL,
    CONSTRAINT fk_prix_type FOREIGN KEY (type_reparation_id) 
        REFERENCES type_reparation(id) ON DELETE CASCADE,
    CONSTRAINT chk_profondeur_min CHECK (profondeur_min >= 0),
    CONSTRAINT chk_profondeur_max CHECK (profondeur_max > profondeur_min),
    CONSTRAINT chk_prix CHECK (prix_par_m2 > 0)
);

-- ============================================================
-- TABLE VOYAGE
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

-- ============================================================
-- TABLE VOYAGE_CHEMIN
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
-- INSERTIONS - LALANA
-- ============================================================

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Sambaina', 'Antananarivo', 'Sambaina', 200.0, 7.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Sambaina-Antsirabe', 'Sambaina', 'Antsirabe', 120.0, 8.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Ampefy', 'Antananarivo', 'Ampefy', 163.0, 7.0);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Ampefy-Sambaina', 'Ampefy', 'Sambaina', 88.0, 7.0);
-- ============================================================
-- INSERTIONS - LAVAKA (avec toutes les données)
-- ============================================================


INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom, point_kilometrique, surface, profondeur)
VALUES (1, 30, 77, 0.22, 'Antananarivo-Sambaina', 1, 235.0, 0.6);
INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom, point_kilometrique, surface, profondeur)
VALUES (5, 25, 56, 0.22, 'Antananarivo-Sambaina', 2, 20.0, 0.4);

INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom, point_kilometrique, surface, profondeur)
VALUES (2, 12, 25, 0.66, 'Sambaina-Antsirabe', 18.5, 156.0, 0.7);

INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom, point_kilometrique, surface, profondeur)
VALUES (3, 56, 70, 0.11, 'Sambaina-Antsirabe', 63.0, 98.0, 0.4);

INSERT INTO lavaka (id, debut, fin, ralentissement, lalana_nom, point_kilometrique, surface, profondeur)
VALUES (4, 25, 32, 0.15, 'Ampefy-Sambaina', 28.5, 120.0, 0.5);

-- ============================================================
-- INSERTIONS - TYPE_REPARATION
-- ============================================================
INSERT INTO type_reparation (id, nom, description) 
VALUES (type_reparation_seq.NEXTVAL, 'Goudron', 'Réparation au goudron bitumineux de qualité supérieure');

INSERT INTO type_reparation (id, nom, description) 
VALUES (type_reparation_seq.NEXTVAL, 'Beton', 'Réparation au béton armé');

INSERT INTO type_reparation (id, nom, description) 
VALUES (type_reparation_seq.NEXTVAL, 'Pavet', 'Réparation avec pavés autobloquants');

-- ============================================================
-- INSERTIONS - PRIX_REPARATION
-- ============================================================
-- Prix pour Goudron B+
INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 1, 0.3, 0.5, 110000);

INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 1, 0.6, 1.0, 900000);

INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 1, 0.3, 0.5, 110000);

INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 1, 0.6, 1.0, 900000);

-- Prix pour Béton
INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 2, 0.5, 0.7, 250000);

INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 2, 0.8, 0.9, 300000);

-- Prix pour Pavé
INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 3, 0.2, 0.4, 150000);

INSERT INTO prix_reparation (id, type_reparation_id, profondeur_min, profondeur_max, prix_par_m2)
VALUES (prix_reparation_seq.NEXTVAL, 3, 0.5, 0.8, 200000);

-- ============================================================
-- INSERTIONS - VOYAGE
-- ============================================================
INSERT INTO voyage (id, depart, arrivee, voiture_nom, vitesse_moyenne, heure_depart, temps_ecoule, position_actuelle, index_chemin_actuel, distance_totale, termine, en_pause)
VALUES (voyage_seq.NEXTVAL, 'Antananarivo', 'Antsirabe', 'VMoyenne', 70, TIMESTAMP '2026-01-14 08:00:00', 0, 0, 0, 320, 0, 0);

-- ============================================================
-- INSERTIONS - VOYAGE_CHEMIN
-- ============================================================
INSERT INTO voyage_chemin (voyage_id, lalana_nom, ordre)
VALUES (voyage_seq.CURRVAL, 'Antananarivo-Sambaina', 1);

INSERT INTO voyage_chemin (voyage_id, lalana_nom, ordre)
VALUES (voyage_seq.CURRVAL, 'Sambaina-Antsirabe', 2);

-- ============================================================
-- VUE UTILE
-- ============================================================
CREATE OR REPLACE VIEW v_lavaka_reparation AS
SELECT 
    l.id,
    l.lalana_nom,
    l.point_kilometrique as pk,
    l.debut,
    l.fin,
    l.surface,
    l.profondeur,
    l.ralentissement,
    tr.nom as type_reparation,
    pr.prix_par_m2,
    (l.surface * pr.prix_par_m2) as cout_reparation
FROM lavaka l
LEFT JOIN type_reparation tr ON 1=1
LEFT JOIN prix_reparation pr ON pr.type_reparation_id = tr.id
    AND l.profondeur >= pr.profondeur_min 
    AND l.profondeur <= pr.profondeur_max
ORDER BY l.lalana_nom, l.point_kilometrique;

COMMIT;