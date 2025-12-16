# Dossier Technique & Manuel Utilisateur
## Projet DevOps - Application EtudLife
**Version :** v0.2.0

**Auteurs :**
* Lyna Baouche
* Alicya-Pearl Marras
* Kenza Menad
* Dyhia Sellah

**Date :** 16 Décembre 2025

---

## 1. Présentation Générale

### 1.1 Objectif du Projet
Le projet **EtudLife** a pour vocation de centraliser les outils essentiels à la vie universitaire des étudiants de Nanterre. L'application vise à regrouper au sein d'une même interface les aspects sociaux, organisationnels et budgétaires de la vie étudiante.

### 1.2 Équipe & Contributeurs

| Membre | Rôle | GitHub                          |
| :--- | :--- |:--------------------------------|
| **Lyna Baouche** | Développeuse Fullstack / DevOps | https://github.com/LynaBaouche  |
| **Alicya-Pearl Marras** | Développeuse Fullstack / DevOps | https://github.com/alicyap      |
| **Kenza Menad** | Développeuse Fullstack / DevOps | https://github.com/kenza-menad  |
| **Dyhia Sellah** | Développeuse Fullstack / DevOps | https://github.com/DyhiaSellah1 |

### 1.3 Gestion de Projet & DevOps

Nous avons adopté une méthodologie inspirée des méthodes **Agile/Scrum**, adaptée à notre contexte universitaire et aux contraintes du projet.

* **Rituels Agiles Adaptés :**
    * **Weekly Stand-up :** Remplacement du "Daily" par un point hebdomadaire pour synchroniser l'équipe.
    * **Sprints :** Cycles de développement courts ponctués par des releases.
    * **Cérémonies :** Sprint Planning pour définir les objectifs et Rétrospectives pour l'amélioration continue.

* **Outils de Gestion :**
    * **Jira :** Utilisé pour le suivi des tickets fonctionnels (User Stories).
    * **Trello :** Utilisé pour la gestion des tickets techniques associés aux fonctionnalités (Backlog technique).
    * **GitHub :** Gestion de version et hébergement du code source.

* **Stratégie de Branches (Gitflow simplifié) :**
  Nous utilisons une stratégie basée sur des branches `Master/Main` pour la production et des `Feature branches` pour le développement des nouvelles fonctionnalités.

* **Pipeline CI/CD :**
  L'intégration et le déploiement continus sont assurés par **GitHub Actions**. La qualité du code est surveillée via **SonarCloud**.

---

## 2. Analyse Concurrentielle & UX

### 2.1 Étude de la concurrence
Comparaison effectuée avec les ENT classiques (Moodle) et les applications de BDE existantes.

### 2.2 Utilisabilité & Design
* **Interface :** Design System cohérent avec une palette dominante bleue.
* **Accessibilité :** Navigation simplifiée et responsive (adaptée mobile/desktop).
* **Architecture :** Simulation d'une Single Page Application (SPA) pour assurer la fluidité de navigation sans rechargement complet des pages.

---

## 3. Architecture Technique

### 3.1 Stack Technologique

L'application repose sur une architecture **REST API** robuste développée avec l'écosystème Spring.

**Backend :**
* **Langage :** Java 17.
* **Framework :** Spring Boot 3.
* **ORM :** Hibernate (via Spring Data JPA) pour la persistance des données.
* **API :** Architecture RESTful.

**Base de Données :**
* **SGBD :** MySQL.
* **Hébergement :** AlwaysData (Cloud).

**Frontend :**
* **Technologies :** HTML5, CSS3, JavaScript (Vanilla).
* **Build Environment :** Gradle.

**Outils de Test :**
* **API Testing :** Postman est utilisé pour valider les endpoints de l'API REST.

### 3.2 Modélisation (UML)

La modélisation du projet est réalisée via **PlantUML** pour générer les diagrammes de classes.

* **Relations Clés :**
    * `Compte` <-> `Groupe` (Relation Many-to-Many pour l'appartenance aux groupes).
    * `Compte` <-> `Recette` (Gestion des favoris ou création de recettes).

*(Insérer ici l'image du diagramme de classe généré)*

---

## 4. Fonctionnalités Détaillées (User Guide)

*(Sections à compléter selon l'avancement)*

### 4.1 Authentification & Sécurité
* Inscription avec validation de l'email universitaire.
* Connexion avec gestion de session locale.

### 4.2 Communauté: Groupes & Publications
* Rejoindre des groupes d'intérêt et fil d'actualité dédié.

### 4.3 Réseau Social: Proches & Messagerie
* Recherche d'étudiants et ajout en "Proche".
* Messagerie privée.

### 4.4 Organisation: Agenda Partagé
* Vues mensuelle/hebdomadaire et gestion d'événements.
* Vue partagée des disponibilités.

### 4.5 Ressources: Partage de Documents
* Upload et gestion de fichiers (PDF, DOCX).

### 4.6 Vie Quotidienne: Cuisine & Budget
* Fiches recettes et génération de menu selon budget.

### 4.7 Assistant IA (Expérimental)
* Concept de Chatbot RAG.

---

## 5. Guide d'Installation & Déploiement

### Prérequis
* Java 17 installé.
* Accès Internet pour les dépendances Gradle.

### Commandes de lancement
```bash
./gradlew bootRun