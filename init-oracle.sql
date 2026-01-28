DROP TABLE lavaka CASCADE CONSTRAINTS;
DROP TABLE lalana CASCADE CONSTRAINTS;
DROP TABLE simba CASCADE CONSTRAINTS;
DROP TABLE reparation CASCADE CONSTRAINTS;
DROP SEQUENCE lavaka_seq;
DROP TABLE pause CASCADE CONSTRAINTS;
DROP SEQUENCE pause_seq;
DROP SEQUENCE simba_seq;
DROP SEQUENCE reparation_seq;

CREATE SEQUENCE lavaka_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE pause_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE simba_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE reparation_seq START WITH 1 INCREMENT BY 1;

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

-- Lavaka : uniquement position et ralentissement
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

-- Table de réparation (matériaux disponibles)
CREATE TABLE reparation (
    id NUMBER PRIMARY KEY,
    materiau VARCHAR2(100) NOT NULL,
    profondeur_min NUMBER(10, 2) NOT NULL,
    profondeur_max NUMBER(10, 2) NOT NULL,
    prix_par_m2 NUMBER(10, 2) NOT NULL,
    CONSTRAINT chk_profondeur_range CHECK (profondeur_min < profondeur_max),
    CONSTRAINT chk_prix CHECK (prix_par_m2 > 0)
);

-- Simba : position (pk), surface et profondeur (PAS de matériau stocké)
CREATE TABLE simba (
    id NUMBER PRIMARY KEY,
    lalana_nom VARCHAR2(100) NOT NULL,
    pk NUMBER(10, 2) NOT NULL,
    surface NUMBER(10, 2) NOT NULL,
    profondeur NUMBER(10, 2) NOT NULL,
    CONSTRAINT fk_simba_lalana FOREIGN KEY (lalana_nom) 
        REFERENCES lalana(nom) ON DELETE CASCADE,
    CONSTRAINT chk_pk CHECK (pk >= 0),
    CONSTRAINT chk_surface CHECK (surface > 0),
    CONSTRAINT chk_profondeur CHECK (profondeur > 0)
);

COMMIT;

-- Insertion des routes
INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Sambaina', 'Antananarivo', 'Sambaina', 200.0, 7.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Toamasina', 'Antananarivo', 'Toamasina', 350.0, 7.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Sambaina-Antsirabe', 'Sambaina', 'Antsirabe', 120.0, 8.5);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Antananarivo-Ampefy', 'Antananarivo', 'Ampefy', 163.0, 7.0);

INSERT INTO lalana (nom, extremite_gauche, extremite_droite, distance, largeur) 
VALUES ('Ampefy-Sambaina', 'Ampefy', 'Sambaina', 88.0, 7.0);

COMMIT;

-- Insertion des règles de réparation (matériaux disponibles)

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'Béton', 0.0, 0.2, 10000);

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'Béton', 0.2, 0.4, 15000);

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'Béton', 0.4, 0.6, 20000);
INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'Béton', 0.6, 0.8, 25000);

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'Goudron', 0.0, 0.4, 15000);

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'Goudron', 0.4, 0.8, 20000);


INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'pave', 0.0, 0.2, 5000);

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'pave', 0.2, 0.5, 6000);

INSERT INTO reparation (id, materiau, profondeur_min, profondeur_max, prix_par_m2)
VALUES (reparation_seq.NEXTVAL, 'pave', 0.5, 0.9, 10000);

COMMIT;
    -- Supprimer les tables
DROP TABLE pluviometrie CASCADE CONSTRAINTS;
DROP TABLE pluviometrie_intervalle CASCADE CONSTRAINTS;

-- Supprimer les séquences
DROP SEQUENCE pluviometrie_seq;
DROP SEQUENCE pluviometrie_intervalle_seq;

COMMIT;
-- ============================================================
-- SCRIPT D'AJOUT DES TABLES PLUVIOMÉTRIE
-- À exécuter sur Oracle
-- ============================================================

-- Créer les séquences
CREATE SEQUENCE pluviometrie_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE pluviometrie_intervalle_seq START WITH 1 INCREMENT BY 1;

-- ============================================================
-- TABLE PLUVIOMETRIE
-- Représente une zone de route avec une quantité de pluie
-- ============================================================
CREATE TABLE pluviometrie (
    id NUMBER PRIMARY KEY,
    lalana_nom VARCHAR2(100) NOT NULL,
    debut_lalana NUMBER(10, 2) NOT NULL,
    fin_lalana NUMBER(10, 2) NOT NULL,
    quantite_pluie NUMBER(10, 2) NOT NULL,
    CONSTRAINT fk_pluviometrie_lalana FOREIGN KEY (lalana_nom) 
        REFERENCES lalana(nom) ON DELETE CASCADE,
    CONSTRAINT chk_pk_pluvio CHECK (debut_lalana < fin_lalana),
    CONSTRAINT chk_quantite_pluie CHECK (quantite_pluie >= 0)
);

-- ============================================================
-- TABLE PLUVIOMETRIE_INTERVALLE
-- Détermine quel matériau utiliser selon la quantité de pluie
-- ============================================================
CREATE TABLE pluviometrie_intervalle (
    id NUMBER PRIMARY KEY,
    quantite_min NUMBER(10, 2) NOT NULL,
    quantite_max NUMBER(10, 2) NOT NULL,
    materiau VARCHAR2(100) NOT NULL,
    CONSTRAINT chk_intervalle_pluvio CHECK (quantite_min < quantite_max),
    CONSTRAINT chk_quantite_min CHECK (quantite_min >= 0)
);
