/* ============================================================
   CHARGER LES LIVRES DEPUIS LE BACKEND
============================================================ */

async function loadBooks() {
  try {
    const res = await fetch("http://localhost:8080/api/livre_bu");
    if (!res.ok) throw new Error(`HTTP ${res.status} - ${await res.text()}`);

    const books = await res.json();
    renderBooks(books);
  } catch (e) {
    console.error("Impossible de charger les livres :", e);
  }
}

const booksGrid = document.getElementById("booksGrid");

/* ============================================================
   AFFICHAGE DES LIVRES
============================================================ */

function renderBooks(books) {
  booksGrid.innerHTML = "";

  books.forEach((b) => {
    const card = document.createElement("article");
    card.className = "book-card";

    card.innerHTML = `
      <div class="book-header">
        <img src="cover_${b.id}.png" class="book-cover">
        <div>
          <div class="book-title">${b.titre}</div>
          <div class="book-author">${b.auteur}</div>
          <span class="badge ${b.disponible ? "badge-available" : "badge-borrowed"}">
            ${b.disponible ? "Disponible" : "Emprunté"}
          </span>
        </div>
      </div>

      <div class="book-footer">
        <span>${b.annee} • ${b.pages} pages<br>ISBN : ${b.isbn}</span>
        ${
          b.disponible
            ? `<button class="btn-primary" onclick="openModal(${b.id})">Réserver</button>`
            : `<button class="btn-disabled" disabled>Indisponible</button>`
        }
      </div>
    `;

    booksGrid.appendChild(card);
  });
}

/* ============================================================
   GESTION DU POPUP RÉSERVATION
============================================================ */

let selectedBookId = null;

const modalOverlay = document.getElementById("bookModalOverlay");
const modalBody = document.getElementById("bookModalBody");
const toast = document.getElementById("bookToast");
const toastText = document.getElementById("bookToastText");

function openModal(bookId) {
  selectedBookId = bookId;

  modalBody.innerHTML = `
    <label>Date de récupération *</label>
    <input type="date" id="dateRecuperation">

    <label>Type</label><br>
    <label><input type="radio" name="type" id="domicile" checked> Emprunt à domicile</label><br>
    <label><input type="radio" name="type" id="consultation"> Consultation sur place</label><br><br>

    <label>Commentaire</label><br>
    <textarea id="commentaire" placeholder="Optionnel"></textarea>
  `;

  modalOverlay.style.display = "flex";
}

document.getElementById("bookModalClose").onclick =
document.getElementById("bookModalCancel").onclick = () => {
  modalOverlay.style.display = "none";
};

/* ============================================================
   ENVOYER LA RÉSERVATION
============================================================ */

document.getElementById("bookModalConfirm").onclick = async () => {
  const date = document.getElementById("dateRecuperation").value;
  if (!date) return alert("La date est obligatoire !");

  const domicile = document.getElementById("domicile").checked;

  try {
    const url =
      `http://localhost:8080/api/reserver` +
      `?userId=1` +
      `&livreId=${encodeURIComponent(selectedBookId)}` +
      `&dateRecuperation=${encodeURIComponent(date)}` +
      `&domicile=${encodeURIComponent(domicile)}`;

    const res = await fetch(url, { method: "POST" });
    if (!res.ok) throw new Error(`HTTP ${res.status} - ${await res.text()}`);

    modalOverlay.style.display = "none";
    toastText.textContent = "Votre réservation a été enregistrée.";
    toast.style.display = "block";
    setTimeout(() => (toast.style.display = "none"), 3000);

    loadBooks();
  } catch (e) {
    alert("Erreur lors de la réservation.");
    console.error(e);
  }
};

/* ============================================================
   INIT
============================================================ */

document.addEventListener("DOMContentLoaded", loadBooks);
