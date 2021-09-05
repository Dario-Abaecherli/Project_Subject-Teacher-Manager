-- 
--  Woche: 02 
--  Datum: 07.06.2021
--  Datei: DataBase_LMSystem.sql
--  Autor: Dario Abächerli
--  Beschreibung: Erstellen der Datenbank
--
DROP DATABASE LMSystem;

CREATE DATABASE IF NOT EXISTS LMSystem;

USE LMSystem;

CREATE TABLE Teacher(
	TeacherID int PRIMARY KEY AUTO_INCREMENT,
	TeacherPrename varchar(50),
	TeacherLastname varchar(50),
	TeacherShort varchar(3) UNIQUE
);

CREATE TABLE Subject(
	SubjectID int PRIMARY KEY AUTO_INCREMENT,
	FK_TeacherID int NOT NULL,
	SubjectShort varchar(5) UNIQUE,
	SubjectDescription varchar(150),
	SubjectAims varbinary(1000),
	SubjectData varbinary(1000),
	FOREIGN KEY (FK_TeacherID) REFERENCES Teacher(TeacherID)
);

INSERT INTO teacher(TeacherPrename, TeacherLastname, TeacherShort) VALUES ("Bucher", "Roland", "BuR");