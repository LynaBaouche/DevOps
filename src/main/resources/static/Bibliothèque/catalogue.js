const books = [
    {
        id: 1,
        titre: "Algorithmes et Structures de Données",
        auteur: "Thomas H. Cormen",
        dispo: true,
        section: "Section A – Étage 2",
        annee: "2022",
        pages: "1312 pages",
        isbn: "978-2-10-054526-1"
    },
    {
        id: 2,
        titre: "Mathématiques pour l'Informatique",
        auteur: "Kenneth H. Rosen",
        dispo: true,
        section: "Section B – Étape 1",
        annee: "2021",
        pages: "896 pages",
        isbn: "978-2-7440-7652-3"
    },
    {
        id: 3,
        titre: "Gestion de Projet Agile",
        auteur: "Mike Cohn",
        dispo: false,
        section: "Section C – Étape 3",
        annee: "2023",
        pages: "524 pages",
        isbn: "978-2-212-14320-6",
        retour: "2024-02-15"
    },
    {
        id: 4,
        titre: "Base de Données Relationnelles",
        auteur: "Ramez Elmasri",
        dispo: true,
        section: "Section A – Étage 2",
        annee: "2022",
        pages: "1024 pages",
        isbn: "978-2-7440-2635-1"
    }
];

const booksGrid = document.getElementById("booksGrid");
const searchInput = document.getElementById("searchInput");

function renderBooks() {
    booksGrid.innerHTML = "";

    const q = searchInput.value.toLowerCase();

    books
        .filter(b =>
            b.titre.toLowerCase().includes(q) ||
            b.auteur.toLowerCase().includes(q) ||
            b.isbn.toLowerCase().includes(q)
        )
        .forEach(book => {

            const card = document.createElement("article");
            card.className = "book-card";

            const badgeClass = book.dispo ? "badge-available" : "badge-borrowed";
            const badgeText = book.dispo ? "Disponible" : "Emprunté";

            card.innerHTML = `
                <div class="book-header">
                    <img src="cover_${book.id}.png" class="book-cover">
                    <div>
                        <div class="book-title">${book.titre}</div>
                        <div class="book-author">${book.auteur}</div>
                        <span class="badge ${badgeClass}">${badgeText}</span>
                        ${!book.dispo ? `<div class="book-meta" style="color:#dc2626;">Retour prévu : ${book.retour}</div>` : ""}
                        <div class="book-meta">${book.section}</div>
                    </div>
                </div>
                <div class="book-footer">
                    <span>${book.annee} · ${book.pages}<br>ISBN : ${book.isbn}</span>
                    ${book.dispo
                        ? `<button class="btn-primary" onclick="openBookModal(${book.id})">Réserver</button>`
                        : `<button class="btn-disabled" disabled>Indisponible</button>`
                    }
                </div>
            `;

            booksGrid.appendChild(card);
        });
}

searchInput.addEventListener("input", renderBooks);

/* ==== MODAL ==== */

const modalOverlay = document.getElementById("bookModalOverlay");
const modalClose = document.getElementById("bookModalClose");
const modalCancel = document.getElementById("bookModalCancel");
const modalConfirm = document.getElementById("bookModalConfirm");
const modalBody = document.getElementById("bookModalBody");
const toast = document.getElementById("bookToast");
const toastText = document.getElementById("bookToastText");

let selectedBook = null;

function openBookModal(id) {
    selectedBook = books.find(b => b.id === id);

    modalBody.innerHTML = `
        <div style="display:flex; gap:12px; margin-bottom:10px;">
            <img src="cover_${selectedBook.id}.png" style="width:60px;">
            <div>
                <strong>${selectedBook.titre}</strong><br>
                ${selectedBook.auteur}<br>
                <small>${selectedBook.section}</small><br>
                <small>ISBN : ${selectedBook.isbn}</small>
            </div>
        </div>

        <label>Type de réservation</label>
        <label><input type="radio" name="type" value="domicile" checked> Emprunt à domicile</label>
        <label><input type="radio" name="type" value="place"> Consultation sur place</label>

        <label>Durée d'emprunt</label>
        <select id="dureeInput">
            <option value="7">7 jours</option>
            <option value="14" selected>14 jours</option>
            <option value="21">21 jours</option>
        </select>

        <label>Date de récupération souhaitée</label>
        <input type="date" id="dateInput">

        <label>Commentaires</label>
        <textarea id="commentInput"></textarea>
    `;

    modalOverlay.style.display = "flex";
}

modalClose.onclick = modalCancel.onclick = () => {
    modalOverlay.style.display = "none";
};

modalConfirm.onclick = () => {
    modalOverlay.style.display = "none";
    toastText.textContent = `Votre réservation pour « ${selectedBook.titre} » a bien été enregistrée.`;
    toast.style.display = "block";
    setTimeout(() => toast.style.display = "none", 3000);
};

document.addEventListener("DOMContentLoaded", renderBooks);
