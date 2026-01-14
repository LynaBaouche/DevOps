/* ============================================================
   CONFIG & INIT
============================================================ */
const API_URL = "http://localhost:8080/api";
const USER_ID = 1; // ID statique pour tes tests scolaires

document.addEventListener("DOMContentLoaded", () => {
    loadBooks();
    loadReservations();
});

/* ============================================================
   1. CHARGER ET AFFICHER LE CATALOGUE (Table livre_bu)
============================================================ */
async function loadBooks() {
    try {
        // Appelle le Controller LivreController
        const res = await fetch(`${API_URL}/livres`);
        if (!res.ok) throw new Error("Erreur lors du chargement des livres");

        const books = await res.json();
        renderBooks(books);
    } catch (e) {
        console.error("Erreur Catalogue:", e);
    }
}

function renderBooks(books) {
    const booksGrid = document.getElementById("booksGrid");
    if (!booksGrid) return;
    booksGrid.innerHTML = "";

    books.forEach((b) => {
        const card = document.createElement("article");
        card.className = "book-card";

        // Utilise les noms exacts de ton entité LivreBu.java
        card.innerHTML = `
      <div class="book-header">
        <div class="book-info">
          <div class="book-title">${b.titre}</div>
          <div class="book-author">${b.auteur}</div>
          <span class="badge ${b.disponible ? "badge-available" : "badge-borrowed"}">
            ${b.disponible ? "Disponible" : "Emprunté"}
          </span>
        </div>
      </div>
      <div class="book-footer">
        <span>${b.annee} • ${b.pages} pages<br>ISBN : ${b.isbn}</span>
        ${b.disponible
            ? `<button class="btn-primary" onclick="openModal(${b.id})">Réserver</button>`
            : `<button class="btn-disabled" disabled>Indisponible</button>`
        }
      </div>
    `;
        booksGrid.appendChild(card);
    });
}

/* ============================================================
   2. GESTION DU POPUP DE RÉSERVATION
============================================================ */
let selectedBookId = null;
const modalOverlay = document.getElementById("bookModalOverlay");
const modalBody = document.getElementById("bookModalBody");

function openModal(bookId) {
    selectedBookId = bookId;
    modalBody.innerHTML = `
    <label>Date de récupération *</label>
    <input type="date" id="dateRecuperation" required>
    <br><br>
    <label>Mode d'emprunt :</label><br>
    <input type="radio" name="type" id="domicile" checked> Emprunt à domicile<br>
    <input type="radio" name="type" id="consultation"> Consultation sur place
  `;
    modalOverlay.style.display = "flex";
}

// Fermeture de la modale
document.getElementById("bookModalCancel").onclick = () => modalOverlay.style.display = "none";

/* ============================================================
   3. ENVOYER LA RÉSERVATION AU BACKEND (Table reservation)
============================================================ */
document.getElementById("bookModalConfirm").onclick = async () => {
    const date = document.getElementById("dateRecuperation").value;
    if (!date) return alert("Veuillez choisir une date !");

    const domicile = document.getElementById("domicile").checked;

    try {
        // Envoie les données au ReservationController
        const url = `${API_URL}/reservation?iduser=${USER_ID}&livreId=${selectedBookId}&dateRecuperation=${date}&domicile=${domicile}`;

        const res = await fetch(url, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());

        modalOverlay.style.display = "none";
        alert("Réservation confirmée !");

        // Rafraîchir les deux listes
        loadBooks();
        loadReservations();
    } catch (e) {
        alert("Erreur: " + e.message);
    }
};

/* ============================================================
   4. AFFICHER MES EMPRUNTS (Table reservation)
============================================================ */
async function loadReservations() {
    try {
        // Récupère les données de la table reservation
        const res = await fetch(`${API_URL}/reservation/utilisateur/${USER_ID}`);
        if (!res.ok) return;

        const data = await res.json();
        renderReservations(data);
    } catch (e) {
        console.error("Erreur Mes Emprunts:", e);
    }
}

function renderReservations(list) {
    const resContainer = document.getElementById("reservationList"); // Assure-toi d'avoir cet ID dans ton HTML
    if (!resContainer) return;
    resContainer.innerHTML = "";

    if (list.length === 0) {
        resContainer.innerHTML = "<p>Aucune réservation en cours.</p>";
        return;
    }

    list.forEach(r => {
        const div = document.createElement("div");
        div.className = "reservation-card";
        div.innerHTML = `
            <p><strong>Livre ID:</strong> ${r.idLivre}</p>
            <p><strong>Date de retour prévue:</strong> ${r.dateRecuperation}</p>
            <span class="status-badge">Confirmée</span>
        `;
        resContainer.appendChild(div);
    });
}
document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');

    if (searchQuery) {
        // Appelle ta fonction de filtrage avec searchQuery
        filterBooks(searchQuery);
    }
});