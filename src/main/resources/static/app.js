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
        await chargerRecommandations();
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
/* ==========================================
   🔄 GESTION DES ONGLETS (VUES)
   ========================================== */
function switchGroupView(viewName) {
    const viewReco = document.getElementById("view-recommandations");
    const viewAll = document.getElementById("view-all-groups");
    const tabReco = document.getElementById("tab-reco");
    const tabAll = document.getElementById("tab-all");

    if (viewName === 'reco') {
        // Afficher Recommandations
        if(viewReco) viewReco.style.display = "block";
        if(viewAll) viewAll.style.display = "none";

        // Gérer la classe 'active'
        if(tabReco) tabReco.classList.add("active");
        if(tabAll) tabAll.classList.remove("active");
    } else {
        // Afficher Tous les groupes
        if(viewReco) viewReco.style.display = "none";
        if(viewAll) viewAll.style.display = "block";

        if(tabReco) tabReco.classList.remove("active");
        if(tabAll) tabAll.classList.add("active");

        // Optimisation : Si la grille "Tous les groupes" est vide, on la charge maintenant
        const grid = document.getElementById("all-groups-grid");
        if(grid && grid.innerHTML.trim() === "") {
            renderAllGroupesList();
        }
    }
}
/*Recommendations de groupes*/

/* ==========================================
   ✨ CHARGER RECOMMANDATIONS (Par Catégorie + Limité à 3)
   ========================================== */
async function chargerRecommandations() {
    const container = document.getElementById("reco-container");
    const noRecoMsg = document.getElementById("no-reco-msg");

    if (!container || !currentUser) return;

    try {
        const groupesReco = await fetchApi(`/groupes/recommandations/${currentUser.id}`);

        if (!groupesReco || groupesReco.length === 0) {
            if(noRecoMsg) noRecoMsg.style.display = "block";
            return;
        }

        // 1. Grouper les résultats par catégorie
        const groupesParCat = {};
        groupesReco.forEach(g => {
            const cat = g.categorie || "Autres";
            if (!groupesParCat[cat]) {
                groupesParCat[cat] = [];
            }
            groupesParCat[cat].push(g);
        });

        // 2. Générer le HTML
        let htmlContent = "";

        for (const [categorie, groupes] of Object.entries(groupesParCat)) {
            // ⚠️ RÈGLE MÉTIER : On ne garde que les 3 premiers
            const top3Groupes = groupes.slice(0, 3);

            htmlContent += `
                <div class="category-block">
                    <div class="category-header">
                        <strong>Vous aimez ${categorie} ?</strong> 
                        <span style="font-size:0.9em; color:#666; font-weight:normal;">Ces groupes pourraient vous plaire :</span>
                    </div>
                    
                    <div class="reco-grid-row">
                        ${top3Groupes.map(g => `
                            <div class="group-card reco-card">
                                <div class="card-badge">${g.categorie}</div>
                                <h4>${g.nom}</h4>
                                <p>${g.description}</p>
                                <button class="btn-join" onclick="rejoindreGroupe(${g.id})">Rejoindre</button>
                            </div>
                        `).join("")}
                    </div>
                </div>
            `;
        }

        container.innerHTML = htmlContent;

    } catch (e) {
        console.error("Erreur reco", e);
        if(noRecoMsg) noRecoMsg.style.display = "block";
    }
}
/* ======================================================
   🤝 ACTION : REJOINDRE UN GROUPE (Global)
   ====================================================== */
async function rejoindreGroupe(groupeId) {
    if (!currentUser) {
        alert("⚠️ Vous devez être connecté pour rejoindre un groupe.");
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/groupes/${groupeId}/ajouter/${currentUser.id}`, {
            method: "POST"
        });

        if (res.ok) {
            alert("✅ Groupe rejoint avec succès !");
            // On recharge les données pour mettre à jour les listes "Mes Groupes" et "Recommandations"
            await loadApplicationData();
        } else {
            const text = await res.text();
            alert("❌ Erreur : " + text);
        }
    } catch (err) {
        console.error(err);
        alert("❌ Erreur réseau lors de la tentative de rejoindre le groupe.");
    }
}

/* ======================================================
   🌍 CHARGER TOUS LES GROUPES (AVEC FILTRE)
   ====================================================== */
let allGroupsCache = []; // Pour filtrer sans refaire de requête

async function renderAllGroupesList() {
    const container = document.getElementById("all-groups-grid");
    const filterSelect = document.getElementById("category-filter");

    if (!container) return;

    try {
        // 1. Récupérer tous les groupes
        const allGroupes = await fetchApi("/groupes");

        // 2. Filtrer pour ne garder que ceux que je n'ai PAS encore rejoints
        const myGroupIds = currentUser.groupes.map(g => g.id);
        allGroupsCache = allGroupes.filter(g => !myGroupIds.includes(g.id));

        // 3. Initialiser le menu déroulant des catégories (une seule fois)
        if (filterSelect && filterSelect.options.length <= 1) {
            // Récupère les catégories uniques présentes dans les données
            const categories = [...new Set(allGroupsCache.map(g => g.categorie).filter(c => c))];

            categories.forEach(cat => {
                const opt = document.createElement("option");
                opt.value = cat;
                opt.textContent = cat;
                filterSelect.appendChild(opt);
            });

            // Ajout de l'événement de changement
            filterSelect.addEventListener("change", () => {
                filterAndDisplayGroups(filterSelect.value, container);
            });
        }

        // 4. Affichage initial (Tout afficher)
        filterAndDisplayGroups("all", container);

    } catch (err) {
        console.error("⚠️ Erreur groupes :", err);
        container.innerHTML = "<p>Impossible de charger les groupes.</p>";
    }
}

/* Fonction interne pour afficher selon le filtre */
function filterAndDisplayGroups(category, container) {
    // Filtrage
    const filtered = category === "all"
        ? allGroupsCache
        : allGroupsCache.filter(g => g.categorie === category);

    // Affichage vide
    if (filtered.length === 0) {
        container.innerHTML = "<p>Aucun groupe disponible dans cette catégorie.</p>";
        return;
    }

    // Génération des cartes
    container.innerHTML = filtered.map(g => `
        <div class="group-card">
            <div class="card-badge">${g.categorie || 'Général'}</div>
            <h3>${g.nom}</h3>
            <p>${g.description}</p>
            <button class="btn-join" onclick="rejoindreGroupe(${g.id})">Rejoindre</button>
        </div>
    `).join("");
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
/* RECHERCHER PROCHES */
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

            const createdUser = await res.json();
            localStorage.setItem("tempUserId", createdUser.id);

            alert("🎉 Compte créé ! Dites-nous ce que vous aimez.");
            window.location.href = "hobbies.html";

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
        connectedItems.forEach(el => el.style.display = "block");
        disconnectedItems.forEach(el => el.style.display = "none");

        const menuTitle = document.querySelector(".menu-title");
        const menuSubtitle = document.querySelector(".menu-subtitle");
        if (menuTitle) menuTitle.textContent = user.prenom + " " + user.nom;
        if (menuSubtitle) menuSubtitle.textContent = user.email;

        // ----- ⚡ BOUTON PROFIL -----
        const btnProfil = document.getElementById("btn-profil");
        if (btnProfil) {                            // ✅ sécurité
            btnProfil.addEventListener("click", async () => {
                if (homepage && appContainer) {
                    homepage.style.display = "none";
                    appContainer.style.display = "grid";
                }
                currentUser = user;
                await afficherProfil();
            });
        }

        // ----- ❌ Déconnexion -----
        const logoutHeaderBtn = document.getElementById("logout-btn");
        if (logoutHeaderBtn) {                      // ✅ sécurité
            logoutHeaderBtn.addEventListener("click", () => {
                localStorage.removeItem("utilisateur");
                window.location.href = "/index.html";
            });
        }

    } else {
        connectedItems.forEach(el => el.style.display = "none");
        disconnectedItems.forEach(el => el.style.display = "block");
    }
});


// ================= SAUVEGARDE MODIFIER PROFIL =================
document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("editProfileForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const fullName = document.getElementById("editNom").value.trim();
        const email = document.getElementById("editEmail").value.trim();
        const telephone = document.getElementById("editTelephone").value.trim();
        const adresse = document.getElementById("editAdresse").value.trim();
        const bio = document.getElementById("editBio").value.trim();

        // 🔐 VALIDATIONS
        // 1) Email parisnanterre
        if (!email.endsWith("@parisnanterre.fr")) {
            alert("L’adresse email doit être une adresse @parisnanterre.fr");
            return;
        }

        // 2) Téléphone : exactement 10 chiffres
        const phoneDigits = telephone.replace(/\D/g, ""); // enlève espaces, tirets…
        if (phoneDigits.length !== 10) {
            alert("Le numéro de téléphone doit contenir exactement 10 chiffres.");
            return;
        }

        // Séparer prénom / nom
        const parts = fullName.split(" ");
        const prenom = parts.shift();
        const nom = parts.join(" ");

        // Payload envoyé au backend
        const payload = {
            prenom,
            nom,
            email,
            telephone: phoneDigits,
            adresse,
            biographie: bio
        };

        try {
            const res = await fetch(`${API_BASE_URL}/comptes/${currentUser.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                alert("Erreur lors de la sauvegarde du profil");
                return;
            }

            // ✅ Réponse backend → on met à jour currentUser + localStorage
            const userMaj = await res.json();
            currentUser = userMaj;
            localStorage.setItem("utilisateur", JSON.stringify(currentUser));

            // 🔄 Recharge l’affichage (profil, groupes, etc.)
            await loadApplicationData();

            closeEditProfile();
            alert("✅ Profil mis à jour !");
        } catch (err) {
            alert("Erreur réseau : " + err.message);
        }
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