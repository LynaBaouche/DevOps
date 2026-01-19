/* ============================================================
   CONFIG & INIT
============================================================ */
const API_URL = "http://localhost:8080/api";
const USER_ID = 1;

document.addEventListener("DOMContentLoaded", () => {
    loadBooks();
    loadReservations();

    const searchBtn = document.getElementById("searchButton");
    const searchInp = document.getElementById("searchInput");

    // Utilisation de addEventListener (plus robuste)
    if(searchBtn) {
        searchBtn.addEventListener("click", () => {
            console.log("Clic sur loupe"); // Pour vérifier dans la console (F12)
            executerRecherche();
        });
    }

    if(searchInp) {
        searchInp.addEventListener("keypress", (e) => {
            if (e.key === "Enter") executerRecherche();
        });
    }
});

/* ============================================================
   1. CHARGER ET RECHERCHER (Logique métier)
============================================================ */
async function loadBooks() {
    try {
        const res = await fetch(`${API_URL}/livres`);
        if (!res.ok) throw new Error("Erreur chargement");
        const books = await res.json();
        renderBooks(books);
    } catch (e) { console.error(e); }
}

async function executerRecherche() {
    const query = document.getElementById("searchInput").value;
    try {
        // On utilise search-global pour fouiller dans les 50 livres
        let url = (query.trim() === "")
            ? `${API_URL}/livres`
            : `${API_URL}/livres/search-global?query=${encodeURIComponent(query)}`;

        const res = await fetch(url);
        if (!res.ok) throw new Error("Erreur recherche");
        const books = await res.json();
        renderBooks(books); // On garde le même affichage
    } catch (e) { console.error(e); }
}

/* ============================================================
   2. AFFICHAGE (UI) - UN SEUL BOUTON RÉSERVER
============================================================ */
function renderBooks(books) {
    const booksGrid = document.getElementById("booksGrid");
    if (!booksGrid) return;
    booksGrid.innerHTML = "";

    books.forEach((b) => {
        const card = document.createElement("article");
        card.className = "book-card";

        // Sécurité : si b.disponible est 1 (SQL) ou true (Java), on considère que c'est dispo
        const isAvailable = (b.disponible === true || b.disponible === 1);

        card.innerHTML = `
      <div class="book-header">
        <div class="book-info">
          <div class="book-title">${b.titre || "Titre non renseigné"}</div>
          <div class="book-author">${b.auteur || "Auteur inconnu"}</div>
          <span class="badge ${isAvailable ? "badge-available" : "badge-borrowed"}">
            ${isAvailable ? "Disponible" : "Emprunté"}
          </span>
        </div>
      </div>
      <div class="book-footer">
        <span>${b.annee || '2024'} • ${b.pages || '0'} pages<br>ISBN : ${b.isbn || 'N/A'}</span>
        ${isAvailable
            ? `<button class="btn-primary" onclick="openModal(${b.id})">Réserver</button>`
            : `<button class="btn-disabled" disabled>Indisponible</button>`
        }
      </div>
    `;
        booksGrid.appendChild(card);
    });
}
/* ============================================================
   3. RÉSERVATIONS (Modale & Backend)
============================================================ */
let selectedBookId = null;
const modalOverlay = document.getElementById("bookModalOverlay");
const modalBody = document.getElementById("bookModalBody");

function openModal(bookId) {
    selectedBookId = bookId;
    modalBody.innerHTML = `
        <label>Date de récupération *</label>
        <input type="date" id="dateRecuperation" required style="width:100%; padding:8px; margin-top:10px;">
        <div style="margin-top:15px;">
            <label>Mode d'emprunt :</label><br>
            <input type="radio" name="type" id="domicile" checked> Emprunt à domicile<br>
            <input type="radio" name="type" id="consultation"> Consultation sur place
        </div>
    `;
    modalOverlay.style.display = "flex";
}

document.getElementById("bookModalCancel").onclick = () => modalOverlay.style.display = "none";

document.getElementById("bookModalConfirm").onclick = async () => {
    const date = document.getElementById("dateRecuperation").value;
    if (!date) return alert("Veuillez choisir une date !");

    const domicile = document.getElementById("domicile").checked;

    try {
        const url = `${API_URL}/reservation?iduser=${USER_ID}&livreId=${selectedBookId}&dateRecuperation=${date}&domicile=${domicile}`;
        const res = await fetch(url, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());

        modalOverlay.style.display = "none";
        showNotification("📚 Réservation réussie !");
        loadBooks();
        loadReservations();
    } catch (e) { alert("Erreur: " + e.message); }
};

async function loadReservations() {
    try {
        const res = await fetch(`${API_URL}/reservation/utilisateur/${USER_ID}`);
        if (res.ok) renderReservations(await res.json());
    } catch (e) { console.error(e); }
}

function renderReservations(list) {
    const resContainer = document.getElementById("reservationList");
    if (!resContainer) return;
    resContainer.innerHTML = list.length === 0 ? "<p>Aucun emprunt.</p>" : "";
    list.forEach(r => {
        const div = document.createElement("div");
        div.className = "reservation-card";
        div.innerHTML = `<p><strong>Livre ID:</strong> ${r.idLivre} - Retour : ${r.dateRecuperation}</p>`;
        resContainer.appendChild(div);
    });
}

function showNotification(message) {
    const toast = document.getElementById("notifToast");
    const toastMsg = document.getElementById("toastMessage");
    if (toast && toastMsg) {
        toastMsg.textContent = message;
        toast.style.display = "block";
        setTimeout(() => { toast.style.display = "none"; }, 4000);
    }
}