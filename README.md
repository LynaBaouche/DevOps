# 🎓 EtudLife

## 📘 Présentation du projet

**EtudLife** est une application web collaborative destinée aux étudiants de l’Université de Nanterre.  
Elle centralise les fonctionnalités essentielles à la vie étudiante : création de liens entre étudiants,
publication et consultation de posts, et regroupement selon des centres d’intérêt communs. (à compléter ensuite)

Le projet est développé dans le cadre du cours de Projet DevOps 1
---

## 👥 Équipe du projet

| Nom | 
|-----|
| [Lyna Baouche]  |
| [Alicya-Pearl Marras] | 
| [Dyhia Sellah] | 
| [Kenza Menad] |
---

## 🚀 Objectifs et motivation

L’objectif est de fournir une **plateforme unifiée** pour la communauté étudiante de Nanterre, favorisant les interactions,
la création de groupes, et le partage d’informations pertinentes.  
Le projet met en avant la **simplicité d’utilisation** et la **pertinence fonctionnelle**, avec un backend Java RESTful
et un frontend JavaScript moderne.

---

## 🧩 Architecture générale

### 🏗️ Schéma d’architecture

```
Frontend (JS, React/Vue)
     ↓ (requêtes HTTP REST en JSON)
Backend (Java, Spring Boot)
     ↓
Base de données (MySQL)
```

---

## 🛠️ Technologies utilisées

| Composant | Technologie |
|------------|--------------|
| Langage backend | Java 17 |
| Framework backend | Spring Boot (REST API) |
| Build tool | Gradle |
| Base de données | MySQL |
| IDE | IntelliJ IDEA |
| Tests API | Postman |
| Gestion de version | Git / GitHub |
| Frontend | JavaScript (Framework libre) |

---

## 📦 Structure du projet

```
EtudLife/
├── src/
│   ├── main/
│   │   ├── java/com/etudlife/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── EtudlifeApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   └── test/
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🧮 Répartition du travail (Sprint 1)

| Membre | Tâches principales | Statut |
|---------|-------------------|--------|
| [Nom 1] | Configuration Spring Boot / Gradle | ✅ |
| [Nom 2] | Création API REST - Feature 1 | 🟡 |
| [Nom 3] | Frontend - appels API | 🔵 |
| [Nom 4] | Tests et documentation | 🟡 |

---

## 🧠 Fonctionnalités (Sprint 1)

### 🥇 Feature 1 — Créer des liens entre étudiants (ajouter un proche)

**Objectif :**
Permettre à un étudiant d’ajouter un autre étudiant comme proche (ami),
et d’interagir avec ses posts.

#### 🔧 Détails techniques

- Route : `POST /api/proches`
- Données envoyées :
  ```json
  {
    "idUtilisateur": 1,
    "idProche": 2
  }
  ```
- Réponse :
  ```
  200 OK - "Utilisateur 1 a ajouté le proche 2"
  ```

#### 🧩 Classes concernées
- `Etudiant`
- `Relation`
- `ProcheController`
- `ProcheRepository`
- `ProcheService`

#### 🔄 Diagramme de classes (simplifié)

```
Etudiant
 ├─ id
 ├─ nom
 ├─ prenom
 └─ email

Relation
 ├─ id
 ├─ etudiantSource (Etudiant)
 ├─ etudiantCible (Etudiant)
 └─ dateCreation
```

---

### 🥈 Feature 2 — Créer des groupes en fonction des hobbies

**Objectif :**
Former des groupes d’étudiants partageant les mêmes centres d’intérêt
(hobbies, associations, matières, etc.)

#### 🔧 Détails techniques

- Route : `POST /api/groupes`
- Exemple JSON :
  ```json
  {
    "nomGroupe": "Amateurs de sport",
    "hobby": "Football",
    "createurId": 1
  }
  ```
- Réponse :
  ```
  201 Created - Groupe "Amateurs de sport" créé avec succès.
  ```

#### 🧩 Classes concernées
- `Groupe`
- `Etudiant`
- `GroupeService`
- `GroupeController`

#### 🔄 Diagramme de classes

```
Groupe
 ├─ id
 ├─ nom
 ├─ hobby
 └─ listeMembres (List<Etudiant>)
```

---

## 💬 Effet “Waouh” prévu ✨

- Fil d’actualité intelligent : chaque étudiant voit uniquement les posts de ses proches.
- Suggestion automatique de nouveaux amis ou groupes selon les centres d’intérêt.
- Interface front intuitive, moderne et dynamique.

---

## ✅ Prochaines étapes (Sprint 2)

- Implémentation réelle de la BDD MySQL (tables Étudiant, Relation, Groupe, Post)
- Sécurité et authentification
- Interface utilisateur interactive

---

## 📚 Installation et exécution

1. Cloner le dépôt :
   ```bash
   git clone https://github.com/[votre_repo]/etudlife.git
   ```
2. Ouvrir le projet dans **IntelliJ IDEA**
3. Vérifier les dépendances Gradle :
   ```
   ./gradlew build
   ```
4. Lancer le serveur :
   ```
   ./gradlew bootRun
   ```
5. Tester les endpoints avec Postman :
   ```
   POST http://localhost:8080/api/proches
   POST http://localhost:8080/api/groupes
   ```

---

© 2025 – Projet universitaire M1 MIAGE – Université Paris Nanterre
