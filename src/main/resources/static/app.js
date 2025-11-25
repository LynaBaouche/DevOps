const API_BASE_URL = "http://localhost:8080/api";
let currentUser = null;

let referenceDate = new Date(); // La date qui sert de pivot (aujourd'hui par défaut)
let currentView = "month";      // 'month' ou 'week'
let allEventsCache = []; // Stocke tous les événements reçus du serveur
let selectedProchesIds = new Set(); // Stocke les IDs des proches cochés



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
    const params = new URLSearchParams(window.location.search);
    const justConnected = params.get("connected") === "true";

    if (user && justConnected) {
        currentUser = user;
        await afficherProfil();
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
    window.location.href = "index.html";
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

            alert("✅ Connexion réussie !");
            window.location.href = "index.html?connected=true";
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
async function loadApplicationData() {
    if (!currentUser) return;

    try {
        const allUsers = await fetchApi("/comptes");
        currentUser = allUsers.find(u => u.id === currentUser.id);

        await renderUserProfile();
        await renderModernUserProfile();// 🆕 Nouveau profil (groupes.html)
        await afficherProches();   //
        await renderUserGroupes();
        await renderAllGroupesList();
        await renderFeedPosts(currentUser.groupes[0]?.id);
    } catch (e) {
        console.error(" Erreur chargement données :", e);
    }
}
/* ============================
   👤 RENDU PROFIL MODERNE (Pour la page Groupes)
   ============================ */
async function renderModernUserProfile() {
    const container = document.getElementById("modern-user-profile");
    if (!container) return; // On n'est pas sur la page groupes.html

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
                    <span>🆔</span> N° Étudiant : ${currentUser.id}
                </div>

                <button class="btn-edit-profile">Modifier le profil</button>
            </div>
        </div>
    `;
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

/*  RECHERCHER UN COMPTE + AJOUTER AUX PROCHES
 */
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

            //  Affichage du profil trouvé avec bouton "Ajouter"
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

            //  Gestion du clic sur "Ajouter"
            document.getElementById("btn-add-friend").addEventListener("click", async () => {
                try {
                    const resAdd = await fetch(`${API_BASE_URL}/liens?idSource=${currentUser.id}&idCible=${data.id}`, {
                        method: "POST"
                    });
                    if (!resAdd.ok) throw new Error("Erreur lors de l'ajout");
                    alert(`${data.prenom} ${data.nom} a été ajouté à vos proches `);
                    await afficherProches();
                } catch (err) {
                    alert(" Impossible d'ajouter cette personne : " + err.message);
                }
            });
        } catch (err) {
            resultDiv.innerHTML = "<p style='color:red;'> Aucun compte trouvé.</p>";
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

/*
   AGENDA — Intégré au backend EtudLife
    */
document.addEventListener("DOMContentLoaded", async () => {
    // Si on est sur la page Agenda.html
    if (window.location.pathname.endsWith("Agenda.html")) {
        const utilisateur = JSON.parse(localStorage.getItem("utilisateur"));
        if (!utilisateur) {
            window.location.href = "login.html";
            return;
        }
        currentUser = utilisateur;
        initAgendaPage();
    }
});

async function initAgendaPage() {
    const btnLogout = document.getElementById("btnLogout");
    if (btnLogout) btnLogout.addEventListener("click", logout);

    const btnNewEvent = document.getElementById("btnNewEvent");
    const btnCancel = document.getElementById("btnCancel");
    const formEvent = document.getElementById("formEvent");

    if (btnNewEvent) btnNewEvent.addEventListener("click", () => togglePopup(true));
    if (btnCancel) btnCancel.addEventListener("click", () => togglePopup(false));
    if (formEvent) formEvent.addEventListener("submit", saveEvent);

    await chargerProchesSidebar();
    await afficherAgenda();

}
/* Ouvre / ferme la popup */
function togglePopup(show) {
    const popup = document.getElementById("popup");
    if (popup) popup.classList.toggle("hidden", !show);
}

/* Charge le calendrier du mois courant */
let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();

async function afficherAgenda() {
    const grid = document.getElementById("agenda-grid");
    if (!grid) return;

    grid.innerHTML = "";

    //  Noms des mois
    const monthNames = [
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    ];

    // 🏷Met à jour le titre du mois
    document.getElementById("month-title").textContent = `${monthNames[currentMonth]} ${currentYear}`;

    // Récupère le premier jour du mois
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

    // Correction pour commencer le lundi (par défaut JS commence dimanche=0)
    const startIndex = firstDay === 0 ? 6 : firstDay - 1;

    // Récupère les événements depuis le backend (ou le cache)
    const events = await fetchEvents();


    for (let i = 0; i < startIndex; i++) {
        const emptyDiv = document.createElement("div");
        emptyDiv.className = "day empty";
        grid.appendChild(emptyDiv);
    }


    for (let d = 1; d <= daysInMonth; d++) {

        const div = document.createElement("div");
        div.className = "day";
        div.innerHTML = `<strong>${d}</strong>`; // Affiche le numéro du jour


        const todayEvents = allEventsCache.filter(ev => {
            const date = new Date(ev.dateDebut);
            const isSameDay = date.getDate() === d && date.getMonth() === currentMonth && date.getFullYear() === currentYear;

            if (!isSameDay) return false;

            const isMine = ev.utilisateur.id === currentUser.id;
            const isSelectedFriend = selectedProchesIds.has(ev.utilisateur.id);

            return isMine || isSelectedFriend;
        });

        // Ajoute les événements dans la case du jour
        todayEvents.forEach(ev => {
            const eDiv = document.createElement("div");
            const isMine = ev.utilisateur.id === currentUser.id;

            eDiv.className = isMine ? "event event-mine" : "event event-other";

            if (!isMine) {
                eDiv.title = `Agenda de ${ev.utilisateur.prenom} ${ev.utilisateur.nom}`;
                eDiv.textContent = "Occupé";
            } else {
                eDiv.textContent = ev.titre;
            }

            div.appendChild(eDiv);
        });

        // Ajoute la case complète à la grille
        grid.appendChild(div);
    }

    renderToday(events);

    // Réattache les événements aux boutons (important si le DOM a changé)
    document.getElementById("prevMonth").onclick = () => changeMonth(-1);
    document.getElementById("nextMonth").onclick = () => changeMonth(1);
}
/* Changement de mois */
function changeMonth(offset) {
    currentMonth += offset;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    } else if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    afficherAgenda();
}

/*  Récupère les événements depuis le backend */
async function fetchEvents() {
    try {
        const res = await fetch(`${API_BASE_URL}/evenements/shared/${currentUser.id}`);
        if (!res.ok) throw new Error("Erreur API événements");
        allEventsCache = await res.json();
        return allEventsCache;
    } catch (err) {
        console.error(err);
        return [];
    }
}

/*  Ajout d’un nouvel événement */
async function saveEvent(e) {
    e.preventDefault();

    const event = {
        titre: document.getElementById("titre").value,
        description: document.getElementById("description").value,
        dateDebut: document.getElementById("dateDebut").value,
        dateFin: document.getElementById("dateFin").value
    };

    try {
        const res = await fetch(`${API_BASE_URL}/evenements/${currentUser.id}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(event)
        });

        if (!res.ok) throw new Error("Erreur création événement");
        togglePopup(false);
        await afficherAgenda();
    } catch (err) {
        alert("❌ " + err.message);
    }
}

/* 🔹 Affiche les événements du jour */
function renderToday(events) {
    const today = new Date().getDate();
    const list = document.getElementById("today-list");
    if (!list) return;
    list.innerHTML = "";

    const todayEvents = events.filter(e => new Date(e.dateDebut).getDate() === today);
    if (!todayEvents.length) {
        list.innerHTML = "<li>Aucun événement prévu aujourd'hui.</li>";
        return;
    }

    todayEvents.forEach(e => {
        const li = document.createElement("li");
        li.innerHTML = `
            <strong>${e.titre}</strong><br>
            ${new Date(e.dateDebut).toLocaleTimeString()} - 
            ${new Date(e.dateFin).toLocaleTimeString()}
        `;
        list.appendChild(li);
    });
}
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
async function chargerProchesSidebar() {
    const container = document.getElementById("proches-list-agenda");
    if (!container) return;

    try {
        // Récupère les proches via l'API existante
        const liens = await fetchApi(`/liens/${currentUser.id}/proches`);

        if (liens.length === 0) {
            container.innerHTML = "<li>Aucun proche ajouté.</li>";
            return;
        }

        container.innerHTML = "";
        liens.forEach(lien => {
            const ami = lien.compteCible;
            const li = document.createElement("li");
            li.className = "proche-item";

            // HTML: Checkbox + Avatar + Nom
            li.innerHTML = `
                <label class="friend-label">
                    <input type="checkbox" class="friend-checkbox" value="${ami.id}">
                    <div class="friend-info">
                        <div class="friend-avatar">${ami.prenom.charAt(0)}${ami.nom.charAt(0)}</div>
                        <span>${ami.prenom} ${ami.nom}</span>
                    </div>
                    <span class="status-dot"></span>
                </label>
            `;

            // Événement : Quand on coche/décoche
            const checkbox = li.querySelector("input");
            checkbox.addEventListener("change", (e) => {
                if (e.target.checked) {
                    selectedProchesIds.add(ami.id);
                } else {
                    selectedProchesIds.delete(ami.id);
                }
                // On rafraîchit l'agenda sans recharger la page
                afficherAgenda();
            });

            container.appendChild(li);
        });

    } catch (err) {
        console.error("Erreur chargement proches agenda", err);
    }
}