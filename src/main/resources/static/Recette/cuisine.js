/* =========================================
   🍳 LOGIQUE FRONTEND - CUISINE & BUDGET
   ========================================= */

const API_RECETTES_URL = `${API_BASE_URL}/recettes`;
const API_EVENEMENTS_URL = `${API_BASE_URL}/evenements`;

let menuSemaineGlobal = {}; // Pour stocker les données reçues

document.addEventListener("DOMContentLoaded", async () => {
    // Vérif connexion
    if (!currentUser) {
        window.location.href = "login.html";
        return;
    }
    await chargerMenuSemaine();
});

/* 🔄 Charge le menu depuis le Backend */
async function chargerMenuSemaine() {
    try {
        const res = await fetch(`${API_RECETTES_URL}/semaine`);
        if (!res.ok) throw new Error("Erreur chargement menu");

        menuSemaineGlobal = await res.json(); // Stocke le résultat
        genererAffichageSemaine(menuSemaineGlobal);

    } catch (err) {
        console.error(err);
        document.getElementById("weekly-grid").innerHTML = "<p>Impossible de charger le menu.</p>";
    }
}

/* 📅 Génère la grille HTML */
function genererAffichageSemaine(menuSemaine) {
    const grid = document.getElementById("weekly-grid");
    grid.innerHTML = "";

    const jours = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"];

    jours.forEach(jour => {
        const menuJour = menuSemaine[jour] || {}; // Récupère le menu ou vide

        const col = document.createElement("div");
        col.className = "day-column";
        col.innerHTML = `<div class="day-title">${jour}</div>`;

        // Slot Midi
        if (menuJour.midi) {
            col.appendChild(createRecipeCard(menuJour.midi, "Midi", jour));
        } else {
            col.innerHTML += `<div class="recipe-card empty">Reste (ou RU)</div>`;
        }

        // Slot Soir
        if (menuJour.soir) {
            col.appendChild(createRecipeCard(menuJour.soir, "Soir", jour));
        }

        grid.appendChild(col);
    });
}

/* 🏷️ Crée l'étiquette (Card) HTML */
function createRecipeCard(recette, moment, jourSemaine) {
    const card = document.createElement("div");
    card.className = "recipe-card";

    // Mapping catégorie -> Style
    let tagIcon = "🍽️";
    let tagClass = "";

    if (recette.categorie === "fish") { tagIcon = "🐟 Poisson"; tagClass = "fish"; }
    else if (recette.categorie === "vege") { tagIcon = "🥬 Végé"; tagClass = "vege"; }
    else if (recette.categorie === "meat") { tagIcon = "🥩 Viande"; tagClass = "meat"; }
    else if (recette.categorie === "dessert") { tagIcon = "🍰 Dessert"; tagClass = "dessert"; }

    card.innerHTML = `
        <span class="meal-type">${moment}</span>
        <div class="tags-row">
            <span class="tag ${tagClass}">${tagIcon}</span>
        </div>
        <div class="recipe-title">${recette.titre}</div>
        <div class="card-footer">
            <span class="price-tag">${recette.prixEstime} €</span>
            <span>⏱️ ${recette.tempsPreparation}</span>
        </div>
    `;

    // Clic pour ouvrir les détails (on passe aussi le jour pour l'agenda)
    card.addEventListener("click", () => openRecipeDetails(recette, jourSemaine, moment));

    return card;
}

/* 🔎 Ouvre la Modale avec les détails */
let currentRecipe = null;
let currentDay = null;
let currentMoment = null;

function openRecipeDetails(recette, jour, moment) {
    currentRecipe = recette;
    currentDay = jour;
    currentMoment = moment;

    const modal = document.getElementById("recipe-modal");

    document.getElementById("modal-title").textContent = recette.titre;
    document.getElementById("modal-price").textContent = `${recette.prixEstime} €`;
    document.getElementById("modal-time").textContent = recette.tempsPreparation;
    document.getElementById("modal-prep").innerText = recette.description; // innerText respecte les \n

    const ul = document.getElementById("modal-ingredients");
    if(recette.ingredients) {
        ul.innerHTML = recette.ingredients.map(ing => `<li>${ing}</li>`).join("");
    } else {
        ul.innerHTML = "<li>Ingrédients non détaillés</li>";
    }

    modal.classList.remove("hidden");
    modal.style.display = "flex";
}

function closeRecipeModal() {
    document.getElementById("recipe-modal").classList.add("hidden");
    document.getElementById("recipe-modal").style.display = "none";
}

/* ❤️ Favoris (Simulation) */
function toggleFavorite() {
    alert(`"${currentRecipe.titre}" ajouté aux favoris !`);
}

/* ✅ AJOUTER À L'AGENDA (Feature demandée)
   On convertit la recette en Événement
*/
async function addToAgenda() {
    if(!currentRecipe || !currentUser) return;

    // Calcul de la date (Prochain Lundi/Mardi... correspondant)
    const dateEvent = getDateOfNextDay(currentDay);

    // Heure par défaut
    if (currentMoment === "Midi") dateEvent.setHours(12, 0, 0);
    else dateEvent.setHours(19, 30, 0);

    // Date de fin (on ajoute 1h pour manger)
    const dateFin = new Date(dateEvent);
    dateFin.setHours(dateFin.getHours() + 1);

    // Création de l'objet événement pour le backend
    const nouvelEvenement = {
        titre: `🍽️ Repas : ${currentRecipe.titre}`,
        description: `Préparation : ${currentRecipe.tempsPreparation}\nCoût : ${currentRecipe.prixEstime}€`,
        dateDebut: dateEvent.toISOString(), // Format ISO pour le backend
        dateFin: dateFin.toISOString(),
        couleur: "#10b981" // Vert pour la cuisine
    };

    try {
        const res = await fetch(`${API_EVENEMENTS_URL}/${currentUser.id}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(nouvelEvenement)
        });

        if (!res.ok) throw new Error("Erreur lors de l'ajout");

        alert("✅ Recette ajoutée à votre agenda !");
        closeRecipeModal();
    } catch (err) {
        alert("Erreur : " + err.message);
    }
}

// Utilitaire pour trouver la date du prochain "Lundi", "Mardi"...
function getDateOfNextDay(dayName) {
    const days = { "Dimanche":0, "Lundi":1, "Mardi":2, "Mercredi":3, "Jeudi":4, "Vendredi":5, "Samedi":6 };
    const today = new Date();
    const resultDate = new Date();

    const targetDay = days[dayName];
    const currentDay = today.getDay();

    let distance = targetDay - currentDay;
    if (distance <= 0) {
        distance += 7; // Si c'est aujourd'hui ou passé, on prend la semaine prochaine (ou on laisse 0 pour aujourd'hui selon la logique voulue)
        // Pour faire simple : on propose toujours le jour à venir
        if(distance === 0 && today.getHours() > 20) distance = 7;
    }

    resultDate.setDate(today.getDate() + distance);
    return resultDate;
}

function genererNouveauMenu() {
    // Pour l'instant on recharge juste, car l'algo backend est aléatoire
    // Dans une version avancée, on enverrait le budget
    if(confirm("Générer une nouvelle combinaison de recettes ?")) {
        chargerMenuSemaine();
    }
}