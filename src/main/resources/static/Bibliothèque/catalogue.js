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
        section: "Section B – Étage 1",
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
    },
    {
        id: 5,
        titre: "Statistiques Appliquées",
        auteur: "David S. Moore",
        dispo: true,
        section: "Section B – Étape 1",
        annee: "2023",
        pages: "768 pages",
        isbn: "978-2-8041-9876-2"
    },
    {
        id: 6,
        titre: "Communication Professionnelle",
        auteur: "Marie Dubois",
        dispo: true,
        section: "Section D – Étape 1",
        annee: "2022",
        pages: "432 pages",
        isbn: "978-2-7440-8901-4"
    }
];

const booksGrid = document.getElementById("booksGrid");
const searchInput = document.getElementById("searchInput");

let selectedBook = null;

function renderBooks() {
    const q = searchInput.value.toLowerCase();
    booksGrid.innerHTML = "";

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
                    <img src="cover_${book.id}.png" alt="${book.titre}" class="book-cover">
                    <div>
                        <div class="book-title">${book.titre}</div>
                        <div class="book-author">${book.auteur}</div>
                        <span class="badge ${badgeClass}">${badgeText}</span>
                        ${book.retour ? `<div class="book-meta" style="color:#dc2626;">Retour prévu : ${book.retour}</div>` : ""}
                        <div class="book-meta">${book.section}</div>
                    </div>
                </div>
                <div class="book-footer">
                    <span>${book.annee} • ${book.pages}<br>ISBN: ${book.isbn}</span>
                    ${book.dispo
                        ? `<button class="btn-primary" data-book="${book.id}">Réserver</button>`
                        : `<button class="btn-disabled" disabled>Indisponible</button>`
                    }
                </div>
            `;

            booksGrid.appendChild(card);
        });

    // attach events
    document.querySelectorAll(".btn-primary[data-book]").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = Number(btn.getAttribute("data-book"));
            openBookModal(id);
        });
    });
}

searchInput.addEventListener("input", renderBooks);

/* ===== Modal réservation d’ouvrage ===== */

const modalOverlay = document.getElementById("bookModalOverlay");
const modalClose = document.getElementById("bookModalClose");
const modalCancel = document.getElementById("bookModalCancel");
const modalConfirm = document.getElementById("bookModalConfirm");
const modalBody = document.getElementById("bookModalBody");
const toast = document.getElementById("bookToast");
const toastText = document.getElementById("bookToastText");

function openBookModal(id) {
    selectedBook = books.find(b => b.id === id);
    if (!selectedBook) return;

    modalBody.innerHTML = `
        <div style="display:flex; gap:12px; margin-bottom:10px;">
            <img src="cover_${selectedBook.id}.png"
                 alt="${selectedBook.titre}"
                 style="width:55px; height:80px; object-fit:cover; border-radius:4px;">
            <div>
                <strong>${selectedBook.titre}</strong><br>
                ${selectedBook.auteur}<br>
                <span style="font-size:12px; color:#6b7280;">${selectedBook.section}</span><br>
                <span style="font-size:12px; color:#6b7280;">ISBN: ${selectedBook.isbn}</span>
            </div>
        </div>

        <div>
            <label>Type de réservation</label><br>
            <label><input type="radio" name="type" value="domicile" checked> Emprunt à domicile</label><br>
            <label><input type="radio" name="type" value="consultation"> Consultation sur place</label>
        </div>

        <div>
            <label>Durée d'emprunt</label>
            <select id="dureeInput">
                <option value="7">7 jours</option>
                <option value="14" selected>14 jours</option>
                <option value="21">21 jours</option>
            </select>
        </div>

        <div>
            <label>Date de récupération souhaitée</label>
            <input type="date" id="dateInput">
        </div>

        <div>
            <label>Commentaires (optionnel)</label>
            <textarea id="commentInput" placeholder="Précisions sur votre demande..."></textarea>
        </div>
    `;

    modalOverlay.style.display = "flex";
}

function closeBookModal() {
    modalOverlay.style.display = "none";
}

modalClose.addEventListener("click", closeBookModal);
modalCancel.addEventListener("click", closeBookModal);

modalConfirm.addEventListener("click", () => {
    if (!selectedBook) return;

    closeBookModal();
    toastText.textContent =
        `Votre réservation pour "${selectedBook.titre}" a été enregistrée. ` +
        `Vous recevrez une notification lorsque le livre sera disponible.`;

    toast.style.display = "block";
    setTimeout(() => { toast.style.display = "none"; }, 3500);
});

document.addEventListener("DOMContentLoaded", renderBooks);
