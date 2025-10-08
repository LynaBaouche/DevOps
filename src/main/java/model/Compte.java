package model;


import jakarta.persistence.*;
import lombok.Data;

    @Entity
    @Data
    public class Compte {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String nom;
        private String email;
        private String motDePasse;

        @Enumerated(EnumType.STRING)
        private Role role; // PROF, ELEVE, ETUDIANT
    }


