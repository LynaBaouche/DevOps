<div align="center">

# Dossier Technique & Manuel Utilisateur

## Projet DevOps - Application EtudLife

**Verson :** v1.0.0

**Auteurs :**

* Lyna Baouche
* Alicya-Pearl Marras
* Kenza Menad
* Dyhia Sellah

**Date :** 02 Février 2026

</div>

---

---

## 📑 Sommaire

**1. Architecture Technique**

* [3.1 Stack Technologique](#31-stack-technologique)
* [3.2 Modélisation (UML) & Structure des Données](#32-modélisation-uml--structure-des-données)

**2. Fonctionnalités Détaillées (User Guide)**

**4. [Tests effectués](#6-tests-effectués)**

**5. [Guide d'Installation & Déploiement](#7-guide-dinstallation--déploiement)**

---

## 1. Architecture Technique

## 2. Fonctionnalités Détaillées 



2.1 Chasseur d'offres de stages Feature
---------------------------------------
* La fonctionnalité Chasseur de Stages transforme l'application en un agent proactif pour l'étudiant. Au lieu d'une recherche manuelle répétitive, le système automatise la veille technologique.
* Les étudiants définissent leurs critères de recherche (secteur, localisation, durée, type de stage) une seule fois, et le système s'occupe du reste.

![JobSearch-Diagramme_de_Séquence___Recherche_de_Stages__Synchrone_.png](diagrammes_de_sequence/JobSearch-Diagramme_de_S%C3%A9quence___Recherche_de_Stages__Synchrone_.png)
![JobSearchBatch-Diagramme_de_Séquence___Batch_Automatisé__Nightly_Job_.png](diagrammes_de_sequence/JobSearchBatch-Diagramme_de_S%C3%A9quence___Batch_Automatis%C3%A9__Nightly_Job_.png)
---------------------------------------

2.2 Agent IA EtudLife
---------------------------------------

* L’Agent IA EtudLife transforme l’application en assistant intelligent contextuel pour les étudiants.

* Il repose sur une architecture RAG (Retrieval Augmented Generation).

* Il conserve l’historique des conversations via Redis.

* Il utilise une API externe d’intelligence artificielle fournie par Google (Gemini).

* Il supporte le streaming temps réel via Server-Sent Events (SSE).

### Règles Métiers

* **Gestion des sessions** : chaque conversation possède un identifiant unique et expire après 30 minutes d’inactivité.

* **Détection d’intentions simples** : les salutations et remerciements sont traités immédiatement pour fournir une réponse rapide.

* **Modes de question** : les questions sont classées en AUTO, SITE ou REGLEMENT selon leur nature.

* **RAG documentaire** : le chatbot répond uniquement à partir des documents disponibles, garantissant des réponses fiables et contextualisées.

* **Streaming interactif** : les réponses longues peuvent être envoyées progressivement pour améliorer l’expérience utilisateur.

### Classes Impliquées
* `ChatController` endpoints REST pour créer une session, envoyer un message, consulter l’historique et fermer une session.

* `ChatStreamController` endpoint REST pour le streaming des réponses en temps réel.

* `ChatService` logique métier principale, traitement des questions et génération des réponses.

* `ChatSessionService` gestion des sessions et de l’historique conversationnel.

* `ChatStreamService` gestion du découpage et de l’envoi progressif des réponses pour le streaming.

* `GeminiClient` appel à l’API externe Gemini pour générer les réponses.

* `PdfKnowledgeBase` recherche des extraits pertinents dans la base documentaire.
### Algorithme & Logique Backend :

Le chatbot expose plusieurs endpoints REST permettant de créer une session, envoyer un message, consulter l’historique, fermer une session ou activer le streaming temps réel.

Lorsqu’un message est reçu, les données sont validées. Si nécessaire, une nouvelle session est générée et stockée dans Redis avec un TTL de 30 minutes. Le message est ensuite enregistré afin de conserver l’historique conversationnel.

Le système intercepte d’abord certaines intentions simples (salutations, remerciements) pour éviter un appel inutile à l’API externe. Le mode de fonctionnement (AUTO, SITE, REGLEMENT) est ensuite déterminé, puis une recherche est effectuée dans la base documentaire via le mécanisme RAG.

Si aucun contenu pertinent n’est trouvé, une réponse standardisée est renvoyée afin d’éviter toute hallucination. Sinon, un prompt structuré est construit et envoyé à l’API Gemini. La réponse générée est sauvegardée dans Redis puis retournée au frontend avec les sources associées.

En mode streaming, la réponse est découpée et envoyée progressivement via SSE afin d’améliorer l’expérience utilisateur.

![agentia.png](diagrammes_de_sequence/agentia.png)
![agent ia.PNG](images/agent%20ia.PNG)
![agentia1.PNG](images/agentia1.PNG)
![agentia2.PNG](images/agentia2.PNG)

---------------------------------------
## 7. Guide d'Installation & Déploiement

### Prérequis

* Java 17 ou 21 installé.
* Accès Internet pour les dépendances Gradle.

### Commandes de lancement

```bash
./gradlew bootRun
```
