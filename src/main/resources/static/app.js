/* ======================================================
   🌐 JS ÉTUDLIFE — VERSION COMPLÈTE ET FONCTIONNELLE
   ====================================================== */

const API_BASE_URL = "http://localhost:8080/api";
let currentUser = null;

/* ============================
   🚀 INITIALISATION
   ============================ */
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
    const params = new URLSearchParams(window.location.search);
    const justConnected = params.get("connected") === "true";

    if (user && justConnected) {
        currentUser = user;
        await afficherProfil();
    }

    if (btnLogin) btnLogin.addEventListener("click", () => window.location.href = "login.html");
    if (btnLogout) btnLogout.addEventListener("click", logout);
});

/* ============================
   🔐 DÉCONNEXION
   ============================ */
function logout() {
    localStorage.removeItem("utilisateur");
    currentUser = null;
    window.location.href = "index.html";
}

/* ============================
   🔑 CONNEXION (login.html)
   ============================ */
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
                    : "Aucun compte trouvé avec cet email.";
                return;
            }

            const user = JSON.parse(text);
            localStorage.setItem("utilisateur", JSON.stringify(user));

            alert("✅ Connexion réussie !");
            window.location.href = "index.html?connected=true";
        } catch (err) {
            document.getElementById("passwordError").textContent = "Erreur réseau : " + err.message;
        }
    });
});

/* ============================
   👤 AFFICHAGE DU PROFIL
   ============================ */
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

/* ============================
   🧩 CHARGEMENT DES DONNÉES
   ============================ */
async function loadApplicationData() {
    if (!currentUser) return;

    try {
        const allUsers = await fetchApi("/comptes");
        currentUser = allUsers.find(u => u.id === currentUser.id);

        await renderUserProfile();
        await afficherProches();   // 👈 AJOUT ICI
        await renderUserGroupes();
        await renderAllGroupesList();
        await renderFeedPosts(currentUser.groupes[0]?.id);
    } catch (e) {
        console.error("❌ Erreur chargement données :", e);
    }
}


/* ============================
   🧍 PROFIL UTILISATEUR
   ============================ */
async function renderUserProfile() {
    const profile = document.getElementById("user-profile");
    if (!profile) return;
    profile.innerHTML = `
        <h4>${currentUser.prenom} ${currentUser.nom}</h4>
        <p>(ID: ${currentUser.id})</p>
    `;
}

/* ============================
   👥 MES GROUPES
   ============================ */
async function renderUserGroupes() {
    const list = document.getElementById("user-groupes");
    const selectPost = document.getElementById("select-my-groupes-post");

    if (!currentUser.groupes?.length) {
        list.innerHTML = "<p>Vous n'avez rejoint aucun groupe.</p>";
        if (selectPost) selectPost.innerHTML = "<option>Aucun groupe</option>";
        return;
    }

    list.innerHTML = "<ul>" +
        currentUser.groupes.map(g => `<li>${g.nom}</li>`).join("") +
        "</ul>";

    if (selectPost) {
        selectPost.innerHTML = currentUser.groupes
            .map(g => `<option value="${g.id}">${g.nom}</option>`)
            .join("");
    }
}

/* ============================
   🌍 TOUS LES GROUPES
   ============================ */
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

/* ============================
   📰 FIL D'ACTUALITÉ
   ============================ */
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
        console.error("❌ Erreur posts :", err);
    }
}

/* ============================
   🔍 RECHERCHER UN COMPTE + AJOUTER AUX PROCHES
   ============================ */
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
            const res = await fetch(`${API_BASE_URL}/comptes/search?nom=${nom}&prenom=${prenom}`);
            if (!res.ok) throw new Error("Aucun compte trouvé");
            const data = await res.json();

            // ✅ Affichage du profil trouvé avec bouton "Ajouter"
            resultDiv.innerHTML = `
                <div class="result">
                    <div>
                        <p><strong>${data.prenom} ${data.nom}</strong></p>
                        <p>Email : ${data.email}</p>
                        <p>ID : ${data.id}</p>
                    </div>
                    <button id="btn-add-friend" data-id="${data.id}">Ajouter</button>
                </div>
            `;

            // ✅ Gestion du clic sur "Ajouter"
            document.getElementById("btn-add-friend").addEventListener("click", async () => {
                try {
                    const resAdd = await fetch(`${API_BASE_URL}/liens?idSource=${currentUser.id}&idCible=${data.id}`, {
                        method: "POST"
                    });
                    if (!resAdd.ok) throw new Error("Erreur lors de l'ajout");
                    alert(`${data.prenom} ${data.nom} a été ajouté à vos proches ✅`);
                    await afficherProches();
                } catch (err) {
                    alert("❌ Impossible d'ajouter cette personne : " + err.message);
                }
            });
        } catch (err) {
            resultDiv.innerHTML = "<p style='color:red;'>❌ Aucun compte trouvé.</p>";
        }
    });
});
/* ============================
   🤝 AFFICHER MES PROCHES
   ============================ */
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
/* ============================
   ➕ REJOINDRE UN GROUPE
   ============================ */
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

            alert("✅ Groupe rejoint avec succès !");
            await rafraichirGroupes(); // 🔄 Met à jour les listes de groupes
        } catch (err) {
            alert("❌ Impossible de rejoindre le groupe : " + err.message);
        }
    });
});

/* ============================
   🔄 RAFRAÎCHIR LISTE DES GROUPES
   ============================ */
async function rafraichirGroupes() {
    try {
        // Récupère le compte mis à jour depuis le backend
        const userMaj = await fetchApi(`/comptes/${currentUser.id}`);
        currentUser = userMaj;

        // Recharge les sections
        await renderUserGroupes();
        await renderAllGroupesList();
    } catch (err) {
        console.error("❌ Erreur de mise à jour des groupes :", err);
    }
}
/* ============================
   📝 PUBLIER UN POST
   ============================ */
document.addEventListener("DOMContentLoaded", () => {
    const formPost = document.getElementById("form-create-post");
    if (!formPost) return;

    formPost.addEventListener("submit", async (e) => {
        e.preventDefault();

        const contenu = document.getElementById("post-contenu").value.trim();
        const groupeId = document.getElementById("select-my-groupes-post").value;

        if (!contenu) {
            alert("⚠️ Veuillez écrire quelque chose avant de publier.");
            return;
        }
        if (!groupeId) {
            alert("⚠️ Sélectionne un groupe dans lequel publier.");
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
            alert("✅ Publication réussie !");

            // 🔄 Rafraîchit le fil d’actualité du groupe choisi
            await renderFeedPosts(groupeId);

        } catch (err) {
            alert("❌ Impossible de publier : " + err.message);
        }
    });
});

/* ============================
   🔧 UTILITAIRE FETCH
   ============================ */
async function fetchApi(endpoint, options = {}) {
    const response = await fetch(API_BASE_URL + endpoint, options);
    if (!response.ok) throw new Error(`Erreur API (${response.status})`);
    return await response.json();
}

