# Dossier Technique & Manuel Utilisateur
## Projet DevOps - Application EtudLife
**Version :** v0.3.0

**Auteurs :**
* Lyna Baouche
* Alicya-Pearl Marras
* Kenza Menad
* Dyhia Sellah

**Date :** 1er Janvier 2026

---

## 1. Présentation Générale

### 1.1 Objectif du Projet
Le projet **EtudLife** a pour vocation de centraliser les outils essentiels à la vie universitaire des étudiants de Nanterre. L'application vise à regrouper au sein d'une même interface les aspects sociaux, organisationnels et budgétaires de la vie étudiante.

### 1.2 Équipe & Contributeurs

| Membre | Rôle                                | GitHub                          |
| :--- |:------------------------------------|:--------------------------------|
| **Lyna Baouche** | Développeuse Fullstack / DevOps     | https://github.com/LynaBaouche  |
| **Alicya-Pearl Marras** | Développeuse Fullstack / DevOps     | https://github.com/alicyap      |
| **Kenza Menad** | Développeuse Fullstack / DevOps     | https://github.com/kenza-menad  |
| **Dyhia Sellah** | Développeuse Fullstack / DevOps     | https://github.com/DyhiaSellah1 |

### 1.3 Gestion de Projet & DevOps

Nous avons adopté une méthodologie inspirée des méthodes **Agile/Scrum**, adaptée à notre contexte universitaire et aux contraintes du projet.

* **Pilotage Agile (Lead : Lyna Baouche) :**
    * Organisation et pilotage des réunions de chaque sprint.
    * **Weekly Stand-up :** Remplacement du "Daily" par un point hebdomadaire pour synchroniser l'équipe.
    * **Sprints :** Cycles de développement courts ponctués par des releases.

* **Outils de Gestion :**
    * **Jira :** Suivi des tickets fonctionnels (Stories).
    * **Trello :** Gestion du Backlog technique.
    * **GitHub :** Gestion de version.

* **Pipeline CI/CD & Automatisation (Implémentation : Lyna Baouche) :**
  L'intégration et le déploiement sont automatisés via **GitHub Actions**.
    * **Gestion des Releases :** Création automatique des tags et des releases GitHub.
    * **Documentation :** Génération automatique des assets de release pour la documentation.
    * **UML :** Mise à jour automatique du diagramme de classe PlantUML à chaque push sur la branche principale via un workflow dédié (`update-uml.yml`).

---

## 2. Analyse Concurrentielle & UX

### 2.1 Étude de la concurrence
Comparaison effectuée avec les ENT classiques (Moodle) et les applications de BDE existantes.

### 2.2 Utilisabilité & Design
* **Interface :** Design System cohérent avec une palette dominante bleue.
* **Accessibilité :** Navigation simplifiée et responsive.
* **Architecture :** Simulation d'une Single Page Application (SPA).

---

## 3. Architecture Technique

### 3.1 Stack Technologique

L'application repose sur une architecture **REST API** robuste développée avec l'écosystème Spring.

**Backend :**
* **Langage :** Java 17 / 21.
* **Framework :** Spring Boot 3.
* **Architecture :** Modèle MVC / REST (Controller, Service, Repository).
* **ORM :** Hibernate (via Spring Data JPA).
* **API :** Architecture RESTful (Controller, Service, Repository).

**Base de Données :**
* **SGBD :** MySQL.
* **Hébergement :** AlwaysData (Cloud).

**Frontend :**
* **Technologies :** HTML5, CSS3, JavaScript (Vanilla).
* **Build Environment :** Gradle.

**Qualité & Tests :**
* **API Testing :** Une collection **Postman** complète a été intégrée pour valider les endpoints de l'API REST et assurer la non-régression.

### 3.2 Modélisation (UML)

La modélisation du projet est réalisée via **PlantUML**. Le diagramme est généré et mis à jour automatiquement par le pipeline CI/CD.

* **Relations Clés :**
    * `Compte` <-> `Groupe` (Relation Many-to-Many).
    * `Compte` <-> `Recette` (Favoris).
    * `Compte` <-> `Lien` (Système de "Proches").

![Diagramme de Classe](diagram_model.png)

---

## 4. Fonctionnalités Détaillées (User Guide)
### 4.1 Authentification & Sécurité
* Inscription avec validation de l'email universitaire.
* Connexion avec gestion de session locale.

### 4.2 Communauté : Groupes & Recommandations Intelligentes
L'expérience communautaire a été enrichie par un algorithme de matching.
* **Algorithme de Recommandation (Smart Matching) :**
    * Le système analyse les hobbies de l'utilisateur et les croise avec les catégories des groupes.
    * **Résultat :** Une section *"✨ Recommandé pour vous"* affiche les groupes les plus pertinents en tête de page.
    * *Règle métier :* Les groupes déjà rejoints sont automatiquement exclus des suggestions.
* **Exploration & Filtrage Dynamique :**
    * Section *"🌍 Explorer tous les groupes"* avec un **filtre par catégorie** (liste déroulante) qui met à jour la grille instantanément sans rechargement.
* **Interaction :** Bouton "Rejoindre" avec feedback immédiat et accès au fil d'actualité du groupe.
* **Fil d'actualité :** Publication et consultation de posts au sein des groupes rejoints.

### 4.3 Réseau Social : Proches
* **Recherche Avancée :** Moteur de recherche d'étudiants par Nom/Prénom connectée à l'API.
* **Gestion des Liens :**
    * Indicateur visuel dynamique : Le bouton d'ajout se désactive si l'étudiant est déjà dans la liste d'amis.
    * Mise à jour en temps réel de la barre latérale "Mes Proches".
* **Interaction :** Base pour le partage d'agenda et la messagerie.

### 4.4 Organisation : Agenda Partagé
* **Vues Multiples :**
    * Vue Mensuelle pour une vue d'ensemble.
    * **Vue Hebdomadaire** détaillée pour la gestion fine du temps.
* **Fonctionnalités Collaboratives :**
    * Ajout d'événements personnels.
    * **Vue "Proches" :** Possibilité de visualiser l'agenda et les disponibilités de ses proches (superposition de calendriers).

### 4.5 Vie Quotidienne : Cuisine
* **Module Recettes :** Consultation de fiches recettes adaptées aux étudiants, avec des recettes détaillées et variées, selon le budget, le régime alimentaire et le temps de préparation des différents plats.
* **Gestion des favoris :** l'utilisateur a la possibilité de mettre en favori une recette.
* **Interaction Agenda :** Possibilité d'ajouter des recettes à l'agenda (ex : planification des repas).

### 4.6 Ressources: Partage de Documents
* Upload et gestion de fichiers (PDF, DOCX).

---
## 5. Matrice de Responsabilités & Réalisations
| Fonctionnalité | Lyna Baouche | Alicya-Pearl Marras | Kenza Menad | Dyhia Sellah |
|--------------|:------------:|:-------------------:|:-----------:|:------------:|
| Architecture Backend | ✅ | ⬜ | ⬜ | ⬜ |
| Gestion des Releases & CI/CD | ✅ | ⬜ | ⬜ | ⬜ |
| Documentation & UML | ✅ | ⬜ | ⬜ | ⬜ |
| Organisation & Pilotage Agile | ✅ | ⬜ | ⬜ | ⬜ |
| Agenda (Mensuel / Hebdo / Proches) | ✅ | ⬜ | ⬜ | ⬜ |
| Proches | ✅ | ⬜ | ⬜ | ⬜ |
| Groupes & Publications | ✅ | ⬜ | ⬜ | ⬜ |
| Recettes | ✅ | ⬜ | ⬜ | ⬜ |
| Recommandation intelligente de groupes | ✅ | ⬜ | ⬜ | ⬜ |
| Tests Postman | ✅ | ✅| ✅ | ✅ |
## 6. Guide d'Installation & Déploiement

### Prérequis
* Java 17 ou 21 installé.
* Accès Internet pour les dépendances Gradle.

### Commandes de lancement
```bash
./gradlew bootRun