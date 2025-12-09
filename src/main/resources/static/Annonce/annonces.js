const API_URL = "/api/annonces";

const annoncesList = document.getElementById("annonces-list");
const filters = document.querySelectorAll(".filter");
const searchInput = document.getElementById("searchInput");
const statsBox = document.getElementById("stats");

let annonces = [];
let currentAnnonce = null;   // annonce actuellement affichée dans la modal

/* =========================================================
   🔵 CHARGEMENT DES ANNONCES
========================================================= */
async function loadAnnonces(cat = "toutes") {
    const res = await fetch(`${API_URL}?categorie=${cat}`);
    annonces = await res.json();

    displayStats();
    displayAnnonces(annonces);
    updateFavoriBadge();
}

/* =========================================================
   📊 STATISTIQUES
========================================================= */
function displayStats() {
    const cats = ["logement", "cours", "emploi", "service", "objet"];

    statsBox.innerHTML = cats.map(c => `
        <div class="stats-box">
            <h2>${annonces.filter(a => a.categorie === c).length}</h2>
            <p>${c}</p>
        </div>
    `).join("");
}

/* =========================================================
   🟦 AFFICHAGE DES CARTES ANNONCES
========================================================= */
function displayAnnonces(list) {
    annoncesList.innerHTML = list.map(a => `
        <div class="card-pro">

            <div class="card-img-wrapper">
                <img src="/images/${a.image}" class="card-img">
                <span class="cat-badge">${a.categorie}</span>
            </div>

            <div class="card-body">
                <h3>${a.titre}</h3>
                <p>${a.description.substring(0, 100)}...</p>

                <div class="price-date">
                    <span class="price">${a.prix}</span>
                    <span class="date">${a.datePublication}</span>
                </div>

                <div class="icons">
                    <p>📍 ${a.ville}</p>
                    <p>👤 ${a.auteur}</p>
                </div>

                <div class="card-actions">
                    <button class="btn-details" onclick="openDetails(${a.id})">
                        Voir détails
                    </button>

                    <button 
                        class="btn-fav ${isFavori(a.id) ? "active" : ""}" 
                        data-id="${a.id}">
                        ${isFavori(a.id) ? "❤️" : "🤍"}
                    </button>
                </div>
            </div>

        </div>
    `).join("");

    updateFavIcons();
}

/* =========================================================
   ❤️ FAVORIS (localStorage)
========================================================= */
function getFavoris() {
    return JSON.parse(localStorage.getItem("favoris")) || [];
}

function isFavori(id) {
    return getFavoris().some(f => f.id === id);
}

function toggleFavori(annonce) {
    let favoris = getFavoris();
    const index = favoris.findIndex(f => f.id === annonce.id);

    if (index === -1) favoris.push(annonce);
    else favoris.splice(index, 1);

    localStorage.setItem("favoris", JSON.stringify(favoris));
}

function updateFavoriBadge() {
    const badge = document.getElementById("fav-count");
    if (badge) badge.textContent = getFavoris().length;
}

function updateFavIcons() {
    document.querySelectorAll(".btn-fav").forEach(btn => {
        const id = Number(btn.dataset.id);

        if (isFavori(id)) {
            btn.classList.add("active");
            btn.textContent = "❤️";
        } else {
            btn.classList.remove("active");
            btn.textContent = "🤍";
        }
    });
}

/* =========================================================
   🟥 ÉCOUTEUR UNIQUE POUR LES CŒURS ❤️🤍
========================================================= */
document.addEventListener("click", (e) => {

    if (e.target.classList.contains("btn-fav")) {

        const id = Number(e.target.dataset.id);
        const annonce = annonces.find(a => a.id === id);

        if (annonce) toggleFavori(annonce);

        updateFavIcons();
        updateFavoriBadge();
    }
});

/* =========================================================
   🔍 RECHERCHE
========================================================= */
searchInput.addEventListener("input", () => {
    const q = searchInput.value.toLowerCase();

    displayAnnonces(
        annonces.filter(a =>
            a.titre.toLowerCase().includes(q) ||
            a.description.toLowerCase().includes(q)
        )
    );
});

/* =========================================================
   🏷 FILTRES
========================================================= */
filters.forEach(btn => {
    btn.addEventListener("click", () => {

        document.querySelector(".filter.active")?.classList.remove("active");
        btn.addEventListener("active");
        btn.classList.add("active");

        loadAnnonces(btn.dataset.cat);
    });
});

/* =========================================================
   🪟 MODAL DÉTAILS
========================================================= */
/* =========================================================
   🪟 MODAL DÉTAILS + INCRÉMENTATION DES VUES
========================================================= */
async function openDetails(id) {

    // 🔵 1) Incrémenter les vues AVANT de recharger l'annonce
    await fetch(`/api/annonces/${id}/vue`, { method: "PUT" });

    // 🔵 2) Récupérer l’annonce mise à jour
    const res = await fetch(`/api/annonces/${id}`);
    const a = await res.json();

    currentAnnonce = a; // IMPORTANT

    // 🔵 3) Remplir la modale
    document.getElementById("modal-img").src = `/images/${a.image}`;
    document.getElementById("modal-title").textContent = a.titre;
    document.getElementById("modal-desc").textContent = a.description;
    document.getElementById("modal-prix").textContent = a.prix;
    document.getElementById("modal-auteur").textContent = a.auteur;
    document.getElementById("modal-ville").textContent = a.ville;
    document.getElementById("modal-date").textContent = a.datePublication;
    document.getElementById("modal-cat").textContent = a.categorie;

    // 🔵 4) Mise à jour bouton favoris dans la modal
    const modalFavBtn = document.getElementById("modal-fav-btn");

    if (isFavori(a.id)) {
        modalFavBtn.textContent = "❤️ Retirer des favoris";
    } else {
        modalFavBtn.textContent = "🤍 Ajouter aux favoris";
    }

    // 🔵 5) Ouvrir la modale
    document.getElementById("detailsModal").classList.remove("hidden");
}

// --- Gestion favoris depuis la modal ---
const modalFavBtn = document.getElementById("modal-fav-btn");

modalFavBtn.addEventListener("click", () => {
    if (!currentAnnonce) return;

    toggleFavori(currentAnnonce);  // Ajoute ou retire

    // --- Mise à jour texte bouton ---
    if (isFavori(currentAnnonce.id)) {
        modalFavBtn.textContent = "❤️ Retirer des favoris";
    } else {
        modalFavBtn.textContent = "🤍 Ajouter aux favoris";
    }

    // --- Mise à jour du cœur sur la carte correspondante ---
    updateFavIcons();

    // --- Mise à jour compteur ---
    updateFavoriBadge();
});
/* =========================
   ❌ FERMETURE MODAL (CROIX)
========================= */
document.getElementById("closeModal").onclick = () => {
    document.getElementById("detailsModal").classList.add("hidden");
};
// Fermer la modal en cliquant sur l'arrière-plan
document.getElementById("modal-overlay")?.addEventListener("click", () => {
    document.getElementById("detailsModal").classList.add("hidden");
});

/* =========================================================
   🚀 INITIALISATION
========================================================= */
loadAnnonces();
updateFavoriBadge();
/* ======================================================
   🟢 GESTION DU MENU DÉROULANT (HEADER) - PAGE ANNONCES
   ====================================================== */
document.addEventListener("DOMContentLoaded", () => {

    const accountIcon = document.getElementById("account-icon");
    const dropdownMenu = document.getElementById("dropdown-menu");

    // 1️⃣ Ouvrir / fermer le menu utilisateur
    if (accountIcon && dropdownMenu) {
        accountIcon.addEventListener("click", (e) => {
            e.stopPropagation();
            dropdownMenu.style.display =
                dropdownMenu.style.display === "block" ? "none" : "block";
        });

        // Fermer si on clique ailleurs
        document.addEventListener("click", (e) => {
            if (!accountIcon.contains(e.target) && !dropdownMenu.contains(e.target)) {
                dropdownMenu.style.display = "none";
            }
        });
    }

    // 2️⃣ Récupération utilisateur
    const user = JSON.parse(localStorage.getItem("utilisateur"));
    const connectedItems = document.querySelectorAll(".connected-only");
    const disconnectedItems = document.querySelectorAll(".disconnected-only");
    const menuTitle = document.querySelector(".menu-title");
    const menuSubtitle = document.querySelector(".menu-subtitle");

    if (user) {
        // Mode connecté
        connectedItems.forEach(el => el.style.display = "block");
        disconnectedItems.forEach(el => el.style.display = "none");

        menuTitle.textContent = `${user.prenom} ${user.nom}`;
        menuSubtitle.textContent = user.email;

        // Bouton PROFIL → retourne à l’accueil avec option profil
        const btnProfil = document.getElementById("btn-profil");
        if (btnProfil) {
            btnProfil.addEventListener("click", () => {
                window.location.href = "/index.html?profil=true";
            });
        }

    } else {
        // Mode invité
        connectedItems.forEach(el => el.style.display = "none");
        disconnectedItems.forEach(el => el.style.display = "block");

        menuTitle.textContent = "Invité";
        menuSubtitle.textContent = "Non connecté";
    }

    // 3️⃣ Gestion du bouton Déconnexion
    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        const newLogoutBtn = logoutBtn.cloneNode(true);
        logoutBtn.parentNode.replaceChild(newLogoutBtn, logoutBtn);

        newLogoutBtn.addEventListener("click", () => {
            localStorage.removeItem("utilisateur");
            window.location.href = "/index.html";
        });
    }
});
document.addEventListener("DOMContentLoaded", () => {

    // 👉 Vérifie si on est bien sur la page favoris
    if (!document.getElementById("favoris-list")) return;

    console.log("📌 Page Favoris détectée → chargement script favoris");

    const favoris = JSON.parse(localStorage.getItem("favoris")) || [];

    const favList = document.getElementById("favoris-list");
    const favCounter = document.getElementById("fav-count-display");
    const emptyBox = document.getElementById("empty-favoris");

    // Mise à jour compteur
    if (favCounter) favCounter.textContent = favoris.length;

    // Aucun favori → afficher section vide
    if (favoris.length === 0) {
        emptyBox.classList.remove("hidden");
        favList.innerHTML = "";
        return;
    }

    // Affiche les favoris
    favList.innerHTML = favoris.map(a => `
        <div class="card-pro">

            <div class="card-img-wrapper">
                <img src="/images/${a.image}" class="card-img">
                <span class="cat-badge">${a.categorie}</span>
            </div>

            <div class="card-body">
                <h3>${a.titre}</h3>
                <p>${a.description.substring(0, 100)}...</p>

                <div class="price-date">
                    <span class="price">${a.prix}</span>
                    <span class="date">${a.datePublication}</span>
                </div>

                <div class="icons">
                    <p>📍 ${a.ville}</p>
                    <p>👤 ${a.auteur}</p>
                </div>

                <div class="card-actions">
                    <button class="btn-details" onclick="openDetails(${a.id})">
                        Voir détails
                    </button>

                    <button class="btn-fav active" onclick="removeFavori(${a.id})">
                        ❤️
                    </button>
                </div>
            </div>
        </div>
    `).join("");
});


// 🔥 SUPPRESSION D'UN FAVORI → MISE À JOUR AUTOMATIQUE
function removeFavori(id) {
    let favoris = JSON.parse(localStorage.getItem("favoris")) || [];
    favoris = favoris.filter(f => f.id !== id);

    localStorage.setItem("favoris", JSON.stringify(favoris));

    location.reload(); // recharge proprement la page favoris
}


