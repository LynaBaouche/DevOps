const API_BASE_URL = "http://localhost:8080/api";
let currentUser = null;

/* INITIALISATION */
document.addEventListener("DOMContentLoaded", async () => {
    const homepage = document.getElementById("homepage-content");
    const appContainer = document.getElementById("app-container");
    const btnLogin = document.getElementById("btn-login");
    const btnLogout = document.getElementById("btn-logout");

    // Affichage par défaut
    if (homepage) homepage.style.display = "block";
    if (appContainer) appContainer.style.display = "none";
    if (btnLogin) btnLogin.style.display = "block";
    if (btnLogout) btnLogout.style.display = "none";

    // Vérifie si connecté
    const user = JSON.parse(localStorage.getItem("utilisateur"));
    if (user) {
        currentUser = user;
        // NE PAS appeler afficherProfil() ici !
    }



    if (btnLogin) btnLogin.addEventListener("click", () => window.location.href = "login.html");
    if (btnLogout) btnLogout.addEventListener("click", logout);

});
/*
   DÉCONNEXION
  */
function logout() {
    localStorage.removeItem("utilisateur");
    currentUser = null;
    window.location.href = "/index.html";
}

/*
   CONNEXION (login.html)
    */
document.addEventListener("DOMContentLoaded", () => {
    const formLogin = document.getElementById("loginForm");
    if (!formLogin) return;

    formLogin.addEventListener("submit", async (e) => {
        e.preventDefault();
        document.querySelectorAll(".error").forEach(el => el.textContent = "");

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();

        let valid = true;
        if (!email.endsWith("@parisnanterre.fr")) {
            document.getElementById("emailError").textContent = "Seules les adresses @parisnanterre.fr sont autorisées.";
            valid = false;
        }
        if (password.length === 0) {
            document.getElementById("passwordError").textContent = "Veuillez entrer votre mot de passe.";
            valid = false;
        }
        if (!valid) return;

        try {
            const response = await fetch(`${API_BASE_URL}/comptes/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const text = await response.text();

            if (!response.ok) {
                document.getElementById("passwordError").textContent = text.includes("Mot de passe")
                    ? "Mot de passe incorrect."
                    : "Aucun compte trouvé avec cet email.Veuillez créer un compte.";
                return;
            }

            const user = JSON.parse(text);
            localStorage.setItem("utilisateur", JSON.stringify(user));
            localStorage.setItem("userId", user.id);



            alert("✅ Connexion réussie !");
            window.location.href = "/index.html";

        } catch (err) {
            document.getElementById("passwordError").textContent = "Erreur réseau : " + err.message;
        }
    });
});

/*  AFFICHAGE DU PROFIL*/
async function afficherProfil() {
    const homepage = document.getElementById("homepage-content");
    const appContainer = document.getElementById("app-container");
    const btnLogin = document.getElementById("btn-login");
    const btnLogout = document.getElementById("btn-logout");

    if (homepage) homepage.style.display = "none";
    if (appContainer) appContainer.style.display = "grid";
    if (btnLogin) btnLogin.style.display = "none";
    if (btnLogout) btnLogout.style.display = "block";

    await loadApplicationData();
}

/*
    CHARGEMENT DES DONNÉES
 */
let isLoadingAppData = false;

async function loadApplicationData() {
    if (!currentUser || isLoadingAppData) return;

    isLoadingAppData = true;
    try {
        const userMaj = await fetchApi(`/comptes/${currentUser.id}`);
        currentUser = userMaj;

        await renderUserProfile();
        await renderModernUserProfile();
        await afficherProches();
        await renderUserGroupes();
        await renderAllGroupesList();
        await renderFeedPosts(currentUser.groupes[0]?.id);
    } catch (e) {
        console.error("Erreur chargement données :", e);
    } finally {
        isLoadingAppData = false;
    }
}
/* ============================
   👤 RENDU PROFIL MODERNE (Pour la page Groupes)
   ============================ */
async function renderModernUserProfile() {
    const container = document.getElementById("modern-user-profile");
    if (!container) return;

    container.innerHTML = `
        <div class="profile-card-modern">
            <div class="profile-header-bg"></div>

            <div class="profile-avatar-container">
                <img src="images/compte.png" alt="Avatar" class="profile-avatar-modern">
            </div>

            <div class="profile-info-modern">
                <div class="profile-name">${currentUser.prenom} ${currentUser.nom}</div>
                <div class="profile-role">Étudiant(e)</div>

                <div class="profile-detail">
                    <span>📧</span> ${currentUser.email}
                </div>

                <div class="profile-detail">
                    <span>📞</span> ${currentUser.telephone || "Non renseigné"}
                </div>

                <div class="profile-detail">
                    <span>📍</span> ${currentUser.adresse || "Non renseignée"}
                </div>

                <div class="profile-detail">
                    <span>📝</span> ${currentUser.biographie || "Aucune biographie"}
                </div>

                <div class="profile-detail">
                    <span>🆔</span> N° Étudiant : ${currentUser.id}
                </div>

                <button class="btn-edit-profile" onclick="openEditProfile()">
                    Modifier le profil
                </button>
            </div>
        </div>
    `;
}

// ================= MODIFIER PROFIL =================

function openEditProfile() {
    if (!currentUser) return;

    // Pré-remplir les champs
    document.getElementById("editNom").value =
        `${currentUser.prenom} ${currentUser.nom}`;
    document.getElementById("editEmail").value = currentUser.email;
    document.getElementById("editTelephone").value = currentUser.telephone || "";
    document.getElementById("editAdresse").value = currentUser.adresse || "";
    document.getElementById("editBio").value = currentUser.biographie || "";

    document.getElementById("editProfileModal").classList.remove("hidden");
}

function closeEditProfile() {
    document.getElementById("editProfileModal").classList.add("hidden");
}

/*
    PROFIL UTILISATEUR
   */
async function renderUserProfile() {
    const profile = document.getElementById("user-profile");
    if (!profile) return;
    profile.innerHTML = `
        <h4>${currentUser.prenom} ${currentUser.nom}</h4>
        <p>(ID: ${currentUser.id})</p>
    `;
}

/*
    MES GROUPES
   */
async function renderUserGroupes() {
    const list = document.getElementById("user-groupes");
    const selectPost = document.getElementById("select-my-groupes-post");

    if (!currentUser.groupes?.length) {
        list.innerHTML = "<p>Vous n'avez rejoint aucun groupe.</p>";
        if (selectPost) selectPost.innerHTML = "<option>Aucun groupe</option>";
        return;
    }

    list.innerHTML = "<ul style='padding:0;'>" +
        currentUser.groupes.map((g, index) => `
            <li class="group-item ${index === 0 ? 'active' : ''}" 
                onclick="changerGroupeActif(${g.id}, this)">
                ${g.nom}
            </li>
        `).join("") +
        "</ul>";

    if (selectPost) {
        selectPost.innerHTML = currentUser.groupes
            .map(g => `<option value="${g.id}">${g.nom}</option>`)
            .join("");
    }
}
/**
 *  Fonction déclenchée au clic sur un groupe
 * @param {number} groupeId - L'ID du groupe cliqué
 * @param {HTMLElement} element - L'élément HTML cliqué (pour gérer le style active)
 */
async function changerGroupeActif(groupeId, element) {
    // 1. Gestion visuelle : Retirer la classe 'active' des autres et l'ajouter ici
    document.querySelectorAll('.group-item').forEach(item => {
        item.classList.remove('active');
    });
    element.classList.add('active');

    // 2. Charger le fil d'actualité de ce groupe
    await renderFeedPosts(groupeId);

    // 3. (Optionnel) Mettre à jour le selecteur "Publier dans" pour correspondre au groupe vu
    const selectPost = document.getElementById("select-my-groupes-post");
    if(selectPost) {
        selectPost.value = groupeId;
    }
}

/*
    TOUS LES GROUPES
 */
async function renderAllGroupesList() {
    const select = document.getElementById("select-all-groupes");
    if (!select) return;

    try {
        const allGroupes = await fetchApi("/groupes");
        const myIds = currentUser.groupes.map(g => g.id);
        const autres = allGroupes.filter(g => !myIds.includes(g.id));

        if (!autres.length) {
            select.innerHTML = "<option>Aucun groupe disponible</option>";
            return;
        }

        select.innerHTML = autres.map(g => `<option value="${g.id}">${g.nom}</option>`).join("");
    } catch (err) {
        console.error("⚠️ Erreur groupes :", err);
    }
}

/*  FIL D'ACTUALITÉ
*/
async function renderFeedPosts(groupeId) {
    const feed = document.getElementById("feed-posts");
    if (!feed) return;

    if (!groupeId) {
        feed.innerHTML = "<p>Aucun groupe sélectionné.</p>";
        return;
    }

    try {
        const posts = await fetchApi(`/posts/groupe/${groupeId}`);

        if (!posts.length) {
            feed.innerHTML = "<p>Aucun post dans ce groupe.</p>";
            return;
        }

        feed.innerHTML = posts.map(p => `
            <div class="post">
                <p><strong>${p.auteur?.prenom || "Inconnu"} ${p.auteur?.nom || ""}</strong></p>
                <p>${p.contenu}</p>
                <small>${new Date(p.dateCreation).toLocaleString()}</small>
            </div>
        `).join("");
    } catch (err) {
        feed.innerHTML = "<p>Erreur chargement des posts.</p>";
        console.error(" Erreur posts :", err);
    }
}
/* RECHERCHER UN COMPTE + AJOUTER AUX PROCHES */
document.addEventListener("DOMContentLoaded", () => {
    const formSearch = document.getElementById("form-search-compte");
    if (!formSearch) return;

    formSearch.addEventListener("submit", async (e) => {
        e.preventDefault();

        const nom = document.getElementById("search-nom").value.trim();
        const prenom = document.getElementById("search-prenom").value.trim();
        const resultDiv = document.getElementById("search-results");

        resultDiv.innerHTML = "<p>Recherche en cours...</p>";

        try {
            // 1. D'abord, on récupère la liste de mes proches actuels pour comparer
            const resProches = await fetch(`${API_BASE_URL}/liens/${currentUser.id}/proches`);
            const mesProches = await resProches.json();
            // On crée une liste simple contenant juste les IDs des amis : [1, 5, 12...]
            const mesProchesIds = mesProches.map(lien => lien.compteCible.id);

            // 2. Ensuite, on lance la recherche
            const res = await fetch(`${API_BASE_URL}/comptes/search?nom=${nom}&prenom=${prenom}`);
            const results = await res.json();

            if (results.length === 0) {
                resultDiv.innerHTML = "<p>Aucun compte trouvé.</p>";
                return;
            }

            // 3. On génère l'affichage en vérifiant chaque ID
            resultDiv.innerHTML = results.map(user => {

                // On ne s'affiche pas soi-même dans les résultats (optionnel mais mieux)
                if (user.id === currentUser.id) return "";

                // EST-CE QUE CET UTILISATEUR EST DÉJÀ MON AMI ?
                const estDejaAmi = mesProchesIds.includes(user.id);

                let boutonHtml;
                if (estDejaAmi) {
                    // 🔵 Bouton "Déjà ajouté" (Bleu ciel #87CEEB, désactivé)
                    boutonHtml = `
                        <button class="btn-already-added" disabled 
                                style="background-color: #87CEEB; color: white; border: none; padding: 5px 10px; border-radius: 5px; cursor: default;">
                            Déjà ajouté
                        </button>`;
                } else {
                    // 🟢 Bouton "Ajouter" (Standard, vert)
                    boutonHtml = `
                        <button class="btn-add-friend" data-id="${user.id}">
                            Ajouter
                        </button>`;
                }

                return `
                    <div class="result">
                        <div>
                            <p><strong>${user.prenom} ${user.nom}</strong></p>
                            <p>${user.email}</p>
                        </div>
                        ${boutonHtml}
                    </div>
                `;
            }).join("");

            // 4. On attache les événements UNIQUEMENT sur les boutons "Ajouter" (les verts)
            document.querySelectorAll(".btn-add-friend").forEach(btn => {
                btn.addEventListener("click", async () => {
                    const cibleId = btn.dataset.id;
                    const resAdd = await fetch(`${API_BASE_URL}/liens?idSource=${currentUser.id}&idCible=${cibleId}`, {
                        method: "POST"
                    });

                    if(resAdd.ok) {
                        alert("Proche ajouté !");
                        // On relance la recherche pour mettre à jour le bouton en "Déjà ajouté" instantanément
                        formSearch.dispatchEvent(new Event('submit'));
                        await afficherProches(); // Met à jour la colonne de droite
                    }
                });
            });

        } catch (err) {
            console.error(err);
            resultDiv.innerHTML = "<p style='color:red;'>Erreur lors de la recherche.</p>";
        }
    });
});
/* AFFICHER MES PROCHES
 */
async function afficherProches() {
    const prochesDiv = document.getElementById("user-proches");
    if (!prochesDiv || !currentUser) return;

    try {
        const liens = await fetchApi(`/liens/${currentUser.id}/proches`);

        if (!liens.length) {
            prochesDiv.innerHTML = "<p>Aucun proche ajouté pour le moment.</p>";
            return;
        }

        prochesDiv.innerHTML = `
            <ul>
                ${liens.map(l => `
                    <li>${l.compteCible.prenom} ${l.compteCible.nom}</li>
                `).join("")}
            </ul>
        `;
    } catch (err) {
        prochesDiv.innerHTML = "<p>Erreur de chargement des proches.</p>";
        console.error(err);
    }
}
/*  REJOINDRE UN GROUPE
 */
document.addEventListener("DOMContentLoaded", () => {
    const formJoin = document.getElementById("form-join-groupe");
    if (!formJoin) return;

    formJoin.addEventListener("submit", async (e) => {
        e.preventDefault();
        const select = document.getElementById("select-all-groupes");
        const groupeId = select.value;

        if (!groupeId || !currentUser) {
            alert("⚠️ Sélectionne un groupe avant de rejoindre.");
            return;
        }

        try {
            // 🔗 Appel API pour rejoindre le groupe
            const res = await fetch(`${API_BASE_URL}/groupes/${groupeId}/ajouter/${currentUser.id}`, {
                method: "POST"
            });
            if (!res.ok) throw new Error("Erreur lors de l’ajout au groupe");

            alert(" Groupe rejoint avec succès !");
            await rafraichirGroupes(); // 🔄 Met à jour les listes de groupes
        } catch (err) {
            alert(" Impossible de rejoindre le groupe : " + err.message);
        }
    });
});

/*  RAFRAÎCHIR LISTE DES GROUPES
    */
async function rafraichirGroupes() {
    try {
        // Récupère le compte mis à jour depuis le backend
        const userMaj = await fetchApi(`/comptes/${currentUser.id}`);
        currentUser = userMaj;

        // Recharge les sections
        await renderUserGroupes();
        await renderAllGroupesList();
    } catch (err) {
        console.error(" Erreur de mise à jour des groupes :", err);
    }
}
/* PUBLIER UN POST
 */
document.addEventListener("DOMContentLoaded", () => {
    const formPost = document.getElementById("form-create-post");
    if (!formPost) return;

    formPost.addEventListener("submit", async (e) => {
        e.preventDefault();

        const contenu = document.getElementById("post-contenu").value.trim();
        const groupeId = document.getElementById("select-my-groupes-post").value;

        if (!contenu) {
            alert(" Veuillez écrire quelque chose avant de publier.");
            return;
        }
        if (!groupeId) {
            alert(" Sélectionne un groupe dans lequel publier.");
            return;
        }

        try {
            const payload = {
                auteurId: currentUser.id,
                groupeId: parseInt(groupeId),
                contenu: contenu
            };

            const res = await fetch(`${API_BASE_URL}/posts`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!res.ok) throw new Error("Erreur lors de la publication");

            document.getElementById("post-contenu").value = "";
            alert(" Publication réussie !");

            //  Rafraîchit le fil d’actualité du groupe choisi
            await renderFeedPosts(groupeId);

        } catch (err) {
            alert(" Impossible de publier : " + err.message);
        }
    });
});

/* UTILITAIRE FETCH
  */
async function fetchApi(endpoint, options = {}) {
    const response = await fetch(API_BASE_URL + endpoint, options);
    if (!response.ok) throw new Error(`Erreur API (${response.status})`);
    return await response.json();
}
/* ======================================================
   👥 PAGE GROUPES - Initialisation spécifique
   ====================================================== */
document.addEventListener("DOMContentLoaded", async () => {
    // On vérifie si on est sur la page groupes.html
    if (window.location.pathname.endsWith("groupes.html")) {

        // 1. Vérification de sécurité
        const user = JSON.parse(localStorage.getItem("utilisateur"));
        if (!user) {
            window.location.href = "login.html"; // Pas connecté ? Dehors !
            return;
        }
        currentUser = user;

        // 2. Afficher la page (enlever le display: none)
        const appContainer = document.getElementById("app-container");
        if (appContainer) appContainer.style.display = "grid"; // On affiche la grille

        // 3. Activer le bouton déconnexion du header
        const btnLogout = document.getElementById("btn-logout");
        if (btnLogout) btnLogout.addEventListener("click", logout);

        // 4. Charger les données (C'est ça qui va afficher le profil et les groupes)
        await loadApplicationData();
    }
});



/* INSCRIPTION (inscription.html) */
document.addEventListener("DOMContentLoaded", () => {
    const formRegister = document.getElementById("inscreptionForm");
    if (!formRegister) return;  // si on n’est pas sur la page inscreption, on ne fait rien

    formRegister.addEventListener("submit", async (e) => {
        e.preventDefault();

        // On nettoie les erreurs
        document.querySelectorAll(".error").forEach(el => el.textContent = "");

        const prenom = document.getElementById("prenom").value.trim();
        const nom = document.getElementById("nom").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const confirmPassword = document.getElementById("confirmPassword").value.trim();
        const conditions = document.getElementById("conditions").checked;

        let valid = true;

        //  Prénom / Nom
        if (prenom.length < 2) {
            document.getElementById("prenomError").textContent = "Prénom invalide.";
            valid = false;
        }
        if (nom.length < 2) {
            document.getElementById("nomError").textContent = "Nom invalide.";
            valid = false;
        }

        //  Email parisnanterre
        if (!email.endsWith("@parisnanterre.fr")) {
            document.getElementById("emailError").textContent =
                "Utilisez une adresse @parisnanterre.fr";
            valid = false;
        }

        //  Mot de passe : 10 caractères mini + 1 chiffre
        if (password.length < 10 || !/\d/.test(password)) {
            document.getElementById("passwordError").textContent =
                "Au moins 10 caractères dont 1 chiffre.";
            valid = false;
        }

        // Confirmation mot de passe
        if (password !== confirmPassword) {
            document.getElementById("confirmError").textContent =
                "Les mots de passe ne correspondent pas.";
            valid = false;
        }

        // Conditions
        if (!conditions) {
            alert("Vous devez accepter les conditions d’utilisation.");
            valid = false;
        }

        if (!valid) return;

        //  Envoi au backend
        try {
            const res = await fetch(`${API_BASE_URL}/comptes`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    prenom,
                    nom,
                    email,
                    motDePasse: password
                })
            });

            if (!res.ok) {
                const msg = await res.text();
                // Par ex. "Un compte avec cet email existe déjà."
                document.getElementById("emailError").textContent = msg;
                return;
            }

            alert("🎉 Inscription réussie ! Vous pouvez maintenant vous connecter.");
            window.location.href = "login.html";

        } catch (err) {
            alert("Erreur lors de l'inscription : " + err.message);
        }
    });
});
/* Génère la liste des proches avec Checkbox */

/* ======================================================
   👥 PAGE Proches - Initialisation spécifique
   ====================================================== */
document.addEventListener("DOMContentLoaded", async () => {
    // On vérifie si on est sur la page groupes.html
    if (window.location.pathname.endsWith("proches.html")) {

        // 1. Vérification de sécurité
        const user = JSON.parse(localStorage.getItem("utilisateur"));
        if (!user) {
            window.location.href = "login.html";
            return;
        }
        currentUser = user;

        // 2. Afficher la page (enlever le display: none)
        const appContainer = document.getElementById("app-container");
        if (appContainer) appContainer.style.display = "grid"; // On affiche la grille

        // 3. Activer le bouton déconnexion du header
        const btnLogout = document.getElementById("btn-logout");
        if (btnLogout) btnLogout.addEventListener("click", logout);

        // 4. Charger les données (C'est ça qui va afficher le profil et les groupes)
        await loadApplicationData();
    }
});

document.addEventListener("DOMContentLoaded", () => {

    const user = JSON.parse(localStorage.getItem("utilisateur"));
    const connectedItems = document.querySelectorAll(".connected-only");
    const disconnectedItems = document.querySelectorAll(".disconnected-only");

    const homepage = document.getElementById("homepage-content");
    const appContainer = document.getElementById("app-container");

    if (user) {
        // Afficher le menu connecté
        connectedItems.forEach(el => el.style.display = "block");
        disconnectedItems.forEach(el => el.style.display = "none");

        // Afficher nom + email
        document.querySelector(".menu-title").textContent = user.prenom + " " + user.nom;
        document.querySelector(".menu-subtitle").textContent = user.email;

        // ----- ⚡ BOUTON PROFIL -----
        const btnProfil = document.getElementById("btn-profil");
        btnProfil.addEventListener("click", async () => {

            homepage.style.display = "none";     // cacher accueil
            appContainer.style.display = "grid"; // montrer profil

            currentUser = user;
            await afficherProfil(); // 🔥 charge groupes, proches, posts, etc.
        });

        // ----- ❌ Déconnexion -----
        document.getElementById("logout-btn").addEventListener("click", () => {
            localStorage.removeItem("utilisateur");
            window.location.href = "/index.html";
        });

    } else {
        // Afficher mode non connecté
        connectedItems.forEach(el => el.style.display = "none");
        disconnectedItems.forEach(el => el.style.display = "block");
    }
});
/* ======================================================
   🟢 GESTION DU MENU DÉROULANT (HEADER) - GLOBAL
   ====================================================== */
document.addEventListener("DOMContentLoaded", () => {
    const accountIcon = document.getElementById("account-icon");
    const dropdownMenu = document.getElementById("dropdown-menu");

    // 1. Ouvrir / Fermer le menu au clic
    if (accountIcon && dropdownMenu) {
        accountIcon.addEventListener("click", (e) => {
            e.stopPropagation(); // Empêche le clic de se propager
            const isVisible = dropdownMenu.style.display === "block";
            dropdownMenu.style.display = isVisible ? "none" : "block";
        });

        // 2. Fermer le menu si on clique ailleurs
        document.addEventListener("click", (e) => {
            if (!accountIcon.contains(e.target) && !dropdownMenu.contains(e.target)) {
                dropdownMenu.style.display = "none";
            }
        });
    }

    // 3. Mettre à jour les infos utilisateur dans le menu
    const user = JSON.parse(localStorage.getItem("utilisateur"));
    const connectedItems = document.querySelectorAll(".connected-only");
    const disconnectedItems = document.querySelectorAll(".disconnected-only");
    const menuTitle = document.querySelector(".menu-title");
    const menuSubtitle = document.querySelector(".menu-subtitle");

    if (user) {
        // Mode Connecté
        if (menuTitle) menuTitle.textContent = `${user.prenom} ${user.nom}`;
        if (menuSubtitle) menuSubtitle.textContent = user.email;

        connectedItems.forEach(el => el.style.display = "block");
        disconnectedItems.forEach(el => el.style.display = "none");

        // Gestion du clic sur "Profil" (Redirection ou affichage)
        const btnProfil = document.getElementById("btn-profil");
        if (btnProfil) {
            btnProfil.addEventListener("click", async (e) => {
                // Si on est sur index.html, on affiche le dashboard
                if (window.location.pathname.endsWith("index.html") || window.location.pathname === "/") {
                    e.preventDefault();
                    const homepage = document.getElementById("homepage-content");
                    const appContainer = document.getElementById("app-container");
                    if (homepage) homepage.style.display = "none";
                    if (appContainer) appContainer.style.display = "grid";
                    await loadApplicationData();
                } else {
                    // Sinon, on redirige vers l'accueil connecté
                    window.location.href = "/index.html?profil=true";
                }
            });
        }

    } else {
        // Mode Déconnecté
        if (menuTitle) menuTitle.textContent = "Invité";
        if (menuSubtitle) menuSubtitle.textContent = "Non connecté";

        connectedItems.forEach(el => el.style.display = "none");
        disconnectedItems.forEach(el => el.style.display = "block");
    }

    // 4. Gestion du bouton Déconnexion (Global)
    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        // On retire les anciens écouteurs pour éviter les doublons (cloneNode)
        const newLogoutBtn = logoutBtn.cloneNode(true);
        logoutBtn.parentNode.replaceChild(newLogoutBtn, logoutBtn);

        newLogoutBtn.addEventListener("click", () => {
            localStorage.removeItem("utilisateur");
            window.location.href = "/index.html";
        });
    }
});

// ================= SAUVEGARDE MODIFIER PROFIL =================
document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("editProfileForm");
    if (!form) return;

    form.addEventListener("submit", (e) => {
        e.preventDefault();

        const fullName = document.getElementById("editNom").value.trim();
        const email = document.getElementById("editEmail").value.trim();
        const telephone = document.getElementById("editTelephone").value.trim();
        const adresse = document.getElementById("editAdresse").value.trim();
        const bio = document.getElementById("editBio").value.trim();

        // Séparer prénom / nom
        const parts = fullName.split(" ");
        const prenom = parts.shift();
        const nom = parts.join(" ");

        // 🔁 Mise à jour utilisateur
        currentUser.prenom = prenom;
        currentUser.nom = nom;
        currentUser.email = email;
        currentUser.telephone = telephone;
        currentUser.adresse = adresse;
        currentUser.biographie = bio;

        // 💾 Sauvegarde locale
        localStorage.setItem("utilisateur", JSON.stringify(currentUser));

        // 🔄 Rafraîchir l’affichage
        renderModernUserProfile();
        renderUserProfile();

        // ❌ Fermer la popup
        closeEditProfile();

        alert("✅ Profil mis à jour !");
    });
});

document.addEventListener("DOMContentLoaded", async () => {
    // On ne fait ça que sur la page d'accueil
    const path = window.location.pathname;
    if (!path.endsWith("index.html") && path !== "/" && path !== "/index") {
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const showProfil = params.get("profil") === "true";

    if (showProfil) {
        const user = JSON.parse(localStorage.getItem("utilisateur"));
        if (!user) {
            // Si pas connecté, on renvoie vers le login
            window.location.href = "login.html";
            return;
        }

        currentUser = user;

        const homepage = document.getElementById("homepage-content");
        const appContainer = document.getElementById("app-container");

        // Afficher la partie profil / groupes
        if (homepage) homepage.style.display = "none";
        if (appContainer) appContainer.style.display = "grid";

        // Charger les données du profil (groupes, proches, posts, etc.)
        await loadApplicationData();

        // Puis scroller vers la colonne de gauche (Mon Profil)
        const profilSection = document.getElementById("profil-section");
        if (profilSection) {
            profilSection.scrollIntoView({ behavior: "smooth" });
        }
    }
});