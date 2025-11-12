//package com.etudlife.config;
//
//import com.etudlife.model.Compte;
//import com.etudlife.model.Groupe;
//import com.etudlife.service.CompteService;
//import com.etudlife.service.GroupeService;
//import com.etudlife.service.PostService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    private final CompteService compteService;
//    private final GroupeService groupeService;
//    private final PostService postService;
//
//    public DataInitializer(CompteService compteService, GroupeService groupeService, PostService postService) {
//        this.compteService = compteService;
//        this.groupeService = groupeService;
//        this.postService = postService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("--- 🌱 Initialisation de la base de données ---");
//
//        // --- 1. Création de comptes avec email + mot de passe ---
//        Compte c1 = compteService.creerCompte(new Compte("Lyna", "Baouche", "lyna@parisnanterre.fr", "etudlife123"));
//        Compte c2 = compteService.creerCompte(new Compte("Alicya", "Marras", "alicya@parisnanterre.fr", "etudlife123"));
//        Compte c3 = compteService.creerCompte(new Compte("Dyhia", "Sellah", "dyhia@parisnanterre.fr", "etudlife123"));
//        Compte c4 = compteService.creerCompte(new Compte("Kenza", "Menad", "kenza@parisnanterre.fr", "etudlife123"));
//        Compte c5 = compteService.creerCompte(new Compte("Jean", "Dupont", "jean@parisnanterre.fr", "etudlife123"));
//        Compte c6 = compteService.creerCompte(new Compte("Paul", "Martin", "paul@parisnanterre.fr", "etudlife123"));
//
//        // --- 2. Création des groupes ---
//        Groupe g1 = groupeService.creerGroupe("Entraide MIAGE", "Partage de cours et astuces pour le M1 MIAGE.");
//        Groupe g2 = groupeService.creerGroupe("BookLife", "Club de lecture du campus. On lit de tout !");
//        Groupe g3 = groupeService.creerGroupe("Run Together", "Pour organiser des footings autour de l'université.");
//        Groupe g4 = groupeService.creerGroupe("Code With Us", "Projets de code, défis et veille technologique.");
//        Groupe g5 = groupeService.creerGroupe("Jeux de Société", "On se retrouve à la BU ou au RU pour jouer.");
//
//        // --- 3. Ajouter des membres aux groupes ---
//        groupeService.ajouterMembre(g1.getId(), c1.getId());
//        groupeService.ajouterMembre(g1.getId(), c2.getId());
//        groupeService.ajouterMembre(g1.getId(), c3.getId());
//        groupeService.ajouterMembre(g1.getId(), c4.getId());
//
//        groupeService.ajouterMembre(g2.getId(), c1.getId());
//        groupeService.ajouterMembre(g2.getId(), c5.getId());
//
//        groupeService.ajouterMembre(g3.getId(), c2.getId());
//        groupeService.ajouterMembre(g3.getId(), c6.getId());
//
//        groupeService.ajouterMembre(g4.getId(), c1.getId());
//        groupeService.ajouterMembre(g4.getId(), c3.getId());
//        groupeService.ajouterMembre(g4.getId(), c5.getId());
//
//        // --- 4. Créer quelques posts de démo ---
//        postService.creerPost(c1.getId(), g1.getId(), "Salut l'équipe ! Quelqu'un a des infos sur le partiel de DevOps ?");
//        postService.creerPost(c3.getId(), g1.getId(), "J'ai mis mes notes du cours de Java Avancé sur le drive !");
//        postService.creerPost(c5.getId(), g2.getId(), "Bonjour, je viens de finir 'Dune'. Quelqu'un l'a lu ?");
//        postService.creerPost(c1.getId(), g4.getId(), "Je teste le nouveau Spring Boot 3, c'est vraiment top !");
//
//        System.out.println("--- ✅ Données initiales chargées ---");
//    }
//}
