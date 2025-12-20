# 🎓 EtudLife

[![License](https://img.shields.io/badge/License-MIT-green.svg)](./LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/LynaBaouche/DevOps)](https://github.com/LynaBaouche/DevOps/releases)
[![Java CI with Gradle](https://github.com/LynaBaouche/DevOps/actions/workflows/gradle.yml/badge.svg)](https://github.com/LynaBaouche/DevOps/actions/workflows/gradle.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=LynaBaouche_DevOps&metric=alert_status)](https://sonarcloud.io/dashboard?id=LynaBaouche_DevOps)
> **Votre plateforme étudiante** — Centralisation des outils de vie universitaire : agenda partagé, groupes de travail, réseau social et partage de documents.

---

## 📘 Sommaire
- [🎓 EtudLife](#-etudlife)
    - [📘 Sommaire](#-sommaire)
    - [🌍 Aperçu](#-aperçu)
    - [🏗️ Architecture](#️-architecture)
    - [⚙️ Prérequis](#️-prérequis)
    - [🧩 Installation et configuration](#-installation-et-configuration)
        - [1️⃣ Cloner le projet](#1️⃣-cloner-le-projet)
        - [2️⃣ Configuration de la Base de Données](#2️⃣-configuration-de-la-base-de-données)
    - [🚀 Lancer l'application](#-lancer-lapplication)
    - [🧠 API (extrait)](#-api-extrait)
    - [👥 Équipe](#-équipe)

---

## 🌍 Aperçu

**EtudLife** est une application full-stack collaborative développée pour les étudiants de l'Université de Nanterre. Elle permet de :
- **Gérer son profil** et sécuriser l'accès (inscription/connexion avec validation universitaire).
- **Créer du lien social** en ajoutant des "proches" et en rejoignant des groupes d'intérêt.
- **S'organiser** grâce à un agenda personnel et une vue partagée des disponibilités des proches.
- **Partager** des ressources (documents de cours) et des informations (fil d'actualité des groupes).

**Version actuelle :** v0.2.0 (Sprint 2) - Intégration MySQL, Authentification, Agenda et Documents.

---

## 🏗️ Architecture

**Backend :**
- **Langage :** Java 17
- **Framework :** Spring Boot 3.x (Spring Web, Spring Data JPA, Validation)
- **Base de données :** MySQL (hébergée sur AlwaysData / ou locale)
- **Services Métiers :**
    - `CompteService` (Auth & Gestion utilisateurs)
    - `EvenementService` (Agenda & Partage)
    - `GroupeService` & `PostService` (Communauté)
    - `DocumentService` (Gestion de fichiers)

**Frontend :**
- **Technologies :** HTML5, CSS3, JavaScript (Vanilla ES6+)
- **Architecture :** Single-Page Application (SPA) simulée via `app.js`.
- **Communication :** Fetch API vers le Backend REST (`http://localhost:8080/api/...`).

**Diagramme de classes :**
- Voir le dossier `/docs` pour les diagrammes UML (PlantUML).

---

## ⚙️ Prérequis

| Outil | Version minimale | Description |
|--------|------------------|--------------|
| **Java** | 17 | Requis pour le Backend |
| **Gradle** | 8.x | Outil de build (inclus via wrapper) |
| **MySQL** | 8.0 | Base de données (Client ou Serveur) |
| **Navigateur** | Récent | Pour l'interface utilisateur |

---

## 🧩 Installation et configuration

### 1️⃣ Cloner le projet
```bash
git clone [https://github.com/LynaBaouche/DevOps.git](https://github.com/LynaBaouche/DevOps.git)
cd DevOps
```
### 2️⃣ Configuration de la Base de Données
**Le projet utilise des variables d'environnement pour sécuriser les identifiants de la base de données.**

Configurez les variables suivantes dans votre IDE (IntelliJ : Run/Debug Configurations) ou votre système :

| Variable | Description | Valeur       |
|----------|------------------|--------------|
| **DB_PASSWORD** | Mot de passe de la BDD| EtudLife2025 |


Note : Le fichier application.properties est configuré pour lire ces variables (${DB_PASSWORD}) ou utiliser une configuration par défaut pour le développement local.

---
## 🚀 Lancer l'application
Le frontend étant servi statiquement par Spring Boot, vous n'avez qu'une seule application à lancer.

1. ** Via le terminal (Linux/Mac) :**
```bash
./gradlew bootRun
```
2. ** Via le terminal (Windows) :**
```bash
gradlew.bat bootRun
```
3. ** Via Votre IDE :**
- Ouvrez le projet dans votre IDE habituel (ex : IntelliJ IDEA).
- Exécutez la classe principale `com.etudlife.EtudLifeApplication`.
- Assurez-vous que les variables d'environnement sont bien configurées.


Une fois le serveur démarré (log : Tomcat started on port 8080), ouvrez votre navigateur : **👉 http://localhost:8080**
---

## 🧠 API (extrait)
Voici quelques endpoints clés de l'API REST :
| Domaine| Méthode | Endpoint | Description |
|--------|---------|----------|--------------|
| **Auth** | POST | /api/auth/login | Connexion utilisateur |
|**Agenda**| GET| /api/evenements/shared/{id} | Récupérer événements partagés |
|**Agenda**| POST| /api/evenements | Créer un événement |
|**Groupes**| POST| /api/groupes | Lister les groupes disponibles, et rejoindre un groupe |
|**Documents**| GET| /api/documents/{id} | Télécharger un document |
|**Posts**| POST| /api/posts | Créer un post dans un groupe |
---
## 👥 Équipe
| Membre              | GitHub                                         | Num Étudiant |
|---------------------|------------------------------------------------|--------------|
| Lyna Baouche        | [@LynaBaouche](https://github.com/LynaBaouche) | 42008865     |
| Alicya-Pearl Marras | [@alicyap](https://github.com/alicyap)     | 44015522        |
| Kenza Menad         | [@kenza-menad](https://github.com/kenza-menad) | 44014761             |
| Dyhia Sellah        | [@DyhiaSellah1](https://github.com/DyhiaSellah1)      | 44015522             |
---

> Projet académique open-source — *EtudLife* © 2025
