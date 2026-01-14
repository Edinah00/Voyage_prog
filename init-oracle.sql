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

COMMIT;