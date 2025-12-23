/*************************************************
 ✅ UTILISATEUR CONNECTÉ
 *************************************************/
function getUser() {
    return JSON.parse(localStorage.getItem("utilisateur"));
}

/*************************************************
 ❤️ FAVORIS PAR UTILISATEUR
 *************************************************/
function getFavorisKey() {
    const user = getUser();
    return user ? `favoris_user_${user.id}` : null;
}

function getFavoris() {
    const key = getFavorisKey();
    return key ? JSON.parse(localStorage.getItem(key)) || [] : [];
}

function isFavori(id) {
    return getFavoris().some(f => f.id === id);
}

function toggleFavori(annonce) {
    const key = getFavorisKey();
    if (!key) return;

    let favoris = getFavoris();
    const index = favoris.findIndex(f => f.id === annonce.id);

    if (index === -1) favoris.push(annonce);
    else favoris.splice(index, 1);

    localStorage.setItem(key, JSON.stringify(favoris));
}

function updateFavoriBadge() {
    const badge = document.getElementById("fav-count");
    if (badge) badge.textContent = getFavoris().length;
}

function syncCardFavori(id) {
    const btn = document.querySelector(`.btn-fav[data-id="${id}"]`);
    if (!btn) return;

    btn.classList.toggle("active", isFavori(id));
    btn.innerHTML = isFavori(id) ? "❤️" : "🤍";
}

/*************************************************
 👁️ VUES PAR UTILISATEUR
 *************************************************/
function getVuesKey() {
    const user = getUser();
    return user ? `vues_user_${user.id}` : null;
}

function getVues() {
    const key = getVuesKey();
    return key ? JSON.parse(localStorage.getItem(key)) || [] : [];
}

function hasAlreadySeen(id) {
    return getVues().includes(id);
}

function markAsSeen(id) {
    const key = getVuesKey();
    if (!key) return;

    const vues = getVues();
    if (!vues.includes(id)) {
        vues.push(id);
        localStorage.setItem(key, JSON.stringify(vues));
    }
}

/*************************************************
 🔗 CONSTANTES DOM (SÉCURISÉES)
 *************************************************/
const API_URL = "/api/annonces";
const annoncesList = document.getElementById("annonces-list");
const filters = document.querySelectorAll(".filter");
const searchInput = document.getElementById("searchInput");
const statsBox = document.getElementById("stats");

let annonces = [];
let currentAnnonce = null;

/*************************************************
 🔵 CHARGEMENT DES ANNONCES
 *************************************************/
async function loadAnnonces(cat = "toutes") {
    try {
        const res = await fetch(`${API_URL}?categorie=${cat}`);
        if (!res.ok) throw new Error("Erreur API annonces");

        annonces = await res.json();

        displayStats();
        displayAnnonces(annonces);
        updateFavoriBadge();
    } catch (e) {
        console.error("❌ Erreur chargement annonces", e);
    }
}

/*************************************************
 📊 STATISTIQUES
 *************************************************/
function displayStats() {
    if (!statsBox) return;

    const cats = ["logement", "cours", "emploi", "service", "objet"];

    statsBox.innerHTML = cats.map(c => `
        <div class="stats-box">
            <h2>${annonces.filter(a => a.categorie === c).length}</h2>
            <p>${c}</p>
        </div>
    `).join("");
}

/*************************************************
 🟦 AFFICHAGE DES ANNONCES
 *************************************************/
function displayAnnonces(list) {
    if (!annoncesList) return;

    if (!list.length) {
        annoncesList.innerHTML = "<p>Aucune annonce</p>";
        return;
    }

    annoncesList.innerHTML = list.map(a => `
        <div class="card-pro">
            <div class="card-img-wrapper">
                <img src="/images/${a.image || "default.jpg"}" class="card-img">
                <span class="cat-badge">${a.categorie}</span>
            </div>

            <div class="card-body">
                <h3>${a.titre}</h3>
                <p>${a.description.substring(0, 100)}...</p>

                <div class="price-date">
                    <span class="price">${a.prix.includes("€") ? a.prix : a.prix + " €"}</span>
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

                    <button class="btn-fav ${isFavori(a.id) ? "active" : ""}" data-id="${a.id}">
                        ${isFavori(a.id) ? "❤️" : "🤍"}
                    </button>
                </div>
            </div>
        </div>
    `).join("");
}

/*************************************************
 ❤️ CLIC SUR FAVORI (CARTE)
 *************************************************/
document.addEventListener("click", (e) => {
    if (!e.target.classList.contains("btn-fav")) return;

    const id = Number(e.target.dataset.id);
    const annonce = annonces.find(a => a.id === id);
    if (!annonce) return;

    toggleFavori(annonce);
    syncCardFavori(id);
    updateFavoriBadge();
});

/*************************************************
 🔍 RECHERCHE
 *************************************************/
if (searchInput) {
    searchInput.addEventListener("input", () => {
        const q = searchInput.value.toLowerCase();
        displayAnnonces(
            annonces.filter(a =>
                a.titre.toLowerCase().includes(q) ||
                a.description.toLowerCase().includes(q)
            )
        );
    });
}

/*************************************************
 🏷 FILTRES
 *************************************************/
filters.forEach(btn => {
    btn.addEventListener("click", () => {
        filters.forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        loadAnnonces(btn.dataset.cat);
    });
});

/*************************************************
 🪟 MODALE DÉTAILS
 *************************************************/
async function openDetails(id) {
    try {
        const user = getUser();
        const res = await fetch(`${API_URL}/${id}`);
        if (!res.ok) throw new Error("Annonce introuvable");

        const a = await res.json();
        currentAnnonce = a;

        if (user && user.id !== a.utilisateurId && !hasAlreadySeen(a.id)) {
            await fetch(`${API_URL}/${a.id}/vue`, { method: "PUT" });
            markAsSeen(a.id);
        }

        document.getElementById("modal-img").src = `/images/${a.image || "default.jpg"}`;
        document.getElementById("modal-title").textContent = a.titre;
        document.getElementById("modal-desc").textContent = a.description;
        document.getElementById("modal-prix").textContent = a.prix;
        document.getElementById("modal-auteur").textContent = a.auteur;
        document.getElementById("modal-ville").textContent = a.ville;
        document.getElementById("modal-date").textContent = a.datePublication;
        document.getElementById("modal-cat").textContent = a.categorie;

        const modalFavBtn = document.getElementById("modal-fav-btn");
        modalFavBtn.classList.toggle("active", isFavori(a.id));
        modalFavBtn.innerHTML = isFavori(a.id)
            ? "❤️ Retirer des favoris"
            : "🤍 Ajouter aux favoris";

        document.getElementById("detailsModal")?.classList.remove("hidden");

    } catch (err) {
        console.error("❌ Erreur détails annonce", err);
    }
}

/*************************************************
 ❤️ FAVORI DANS MODALE
 *************************************************/
document.getElementById("modal-fav-btn")?.addEventListener("click", () => {
    if (!currentAnnonce) return;

    toggleFavori(currentAnnonce);
    updateFavoriBadge();

    const btn = document.getElementById("modal-fav-btn");
    btn.classList.toggle("active", isFavori(currentAnnonce.id));
    btn.innerHTML = isFavori(currentAnnonce.id)
        ? "❤️ Retirer des favoris"
        : "🤍 Ajouter aux favoris";
});

/*************************************************
 ❌ FERMER MODALE
 *************************************************/
document.getElementById("closeModal")?.addEventListener("click", () => {
    document.getElementById("detailsModal")?.classList.add("hidden");
});

/*************************************************
 🚀 INITIALISATION
 *************************************************/
document.addEventListener("DOMContentLoaded", async () => {

    // Sécurité : uniquement sur annonces.html
    if (!annoncesList) return;

    await loadAnnonces("toutes");
    updateFavoriBadge();

    // Ouverture automatique depuis favoris
    const selectedId = localStorage.getItem("selected_annonce_id");
    if (selectedId) {
        localStorage.removeItem("selected_annonce_id");
        openDetails(Number(selectedId));
    }
});
