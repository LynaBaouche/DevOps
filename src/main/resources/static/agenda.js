/*
   AGENDA — Intégré au backend EtudLife
*/
let currentDate = new Date(); // Date de référence
let currentView = "month";    // 'month' ou 'week'

// Variables globales
if (typeof allEventsCache === "undefined") var allEventsCache = [];
if (typeof selectedProchesIds === "undefined") var selectedProchesIds = new Set();


/* =========================
   INIT SPÉCIFIQUE AGENDA
   ========================= */
document.addEventListener("DOMContentLoaded", async () => {
    const path = window.location.pathname.toLowerCase();
    if (!path.endsWith("agenda.html")) {
        return;
    }

    const utilisateur = JSON.parse(localStorage.getItem("utilisateur"));
    if (!utilisateur) {
        window.location.href = "login.html";
        return;
    }

    currentUser = utilisateur;
    await initAgendaPage();
});

/* =========================
   INIT DE LA PAGE AGENDA
   ========================= */
async function initAgendaPage() {

    const btnLogout = document.getElementById("btnLogout");
    if (btnLogout) btnLogout.addEventListener("click", logout);

    const btnNewEvent = document.getElementById("btnNewEvent");
    const btnCancel = document.getElementById("btnCancel");
    const formEvent = document.getElementById("formEvent");
    const btnDelete = document.getElementById("btnDelete");

    if (btnNewEvent) btnNewEvent.addEventListener("click", () => openCreatePopup());
    if (btnCancel) btnCancel.addEventListener("click", () => togglePopup(false));
    if (formEvent) formEvent.addEventListener("submit", saveEvent);

    // bouton supprimer dans la popup
    if (btnDelete) {
        btnDelete.addEventListener("click", async () => {
            const id = document.getElementById("eventId").value;
            if (!id) return;
            if (!confirm("Supprimer cet événement ?")) return;

            try {
                await deleteEvenement(id);
                togglePopup(false);
                await fetchEvents();
                await afficherAgenda();
            } catch (err) {
                alert("❌ " + err.message);
            }
        });
    }

    const prevMonth = document.getElementById("prevMonth");
    const nextMonth = document.getElementById("nextMonth");
    const btnViewMonth = document.getElementById("btnViewMonth");
    const btnViewWeek = document.getElementById("btnViewWeek");

    if (prevMonth) prevMonth.addEventListener("click", () => navigateDate(-1));
    if (nextMonth) nextMonth.addEventListener("click", () => navigateDate(1));
    if (btnViewMonth) btnViewMonth.addEventListener("click", () => switchView("month"));
    if (btnViewWeek) btnViewWeek.addEventListener("click", () => switchView("week"));

    await chargerProchesSidebar();
    await fetchEvents();
    await afficherAgenda();
}

/* Ouvre / ferme la popup */
function togglePopup(show) {
    const popup = document.getElementById("popup");
    if (popup) popup.classList.toggle("hidden", !show);
}

/* Ouvrir popup en mode création */
function openCreatePopup() {
    const popupTitle = document.getElementById("popup-title");
    const inputId = document.getElementById("eventId");
    const inputTitre = document.getElementById("titre");
    const inputDescription = document.getElementById("description");
    const inputDateDebut = document.getElementById("dateDebut");
    const inputDateFin = document.getElementById("dateFin");
    const btnDelete = document.getElementById("btnDelete");

    if (inputId) inputId.value = "";
    if (inputTitre) inputTitre.value = "";
    if (inputDescription) inputDescription.value = "";
    if (inputDateDebut) inputDateDebut.value = "";
    if (inputDateFin) inputDateFin.value = "";
    if (popupTitle) popupTitle.textContent = "Ajouter un événement";
    if (btnDelete) btnDelete.classList.add("hidden");

    togglePopup(true);
}

/* Ouvrir popup en mode édition */
function openEditPopup(ev) {
    const popupTitle = document.getElementById("popup-title");
    const inputId = document.getElementById("eventId");
    const inputTitre = document.getElementById("titre");
    const inputDescription = document.getElementById("description");
    const inputDateDebut = document.getElementById("dateDebut");
    const inputDateFin = document.getElementById("dateFin");
    const btnDelete = document.getElementById("btnDelete");

    if (inputId) inputId.value = ev.id;
    if (inputTitre) inputTitre.value = ev.titre || "";
    if (inputDescription) inputDescription.value = ev.description || "";

    const dDebut = new Date(ev.dateDebut);
    const dFin = new Date(ev.dateFin);
    if (inputDateDebut) inputDateDebut.value = dDebut.toISOString().slice(0, 16);
    if (inputDateFin) inputDateFin.value = dFin.toISOString().slice(0, 16);

    if (popupTitle) popupTitle.textContent = "Modifier l'événement";
    if (btnDelete) btnDelete.classList.remove("hidden");

    togglePopup(true);
}

/* Changer vue mois / semaine */
function switchView(view) {
    currentView = view;

    const btnMonth = document.getElementById("btnViewMonth");
    const btnWeek = document.getElementById("btnViewWeek");

    if (btnMonth) btnMonth.classList.toggle("active", view === "month");
    if (btnWeek) btnWeek.classList.toggle("active", view === "week");

    const grid = document.getElementById("agenda-grid");
    if (grid) {
        if (view === "week") {
            grid.classList.add("agenda-grid-week");
        } else {
            grid.classList.remove("agenda-grid-week");
        }
    }

    afficherAgenda();
}

/* Navigation précédente / suivante */
function navigateDate(offset) {
    if (currentView === "month") {
        currentDate.setMonth(currentDate.getMonth() + offset);
    } else {
        currentDate.setDate(currentDate.getDate() + offset * 7);
    }
    afficherAgenda();
}

/* Affichage global (en fonction de la vue) */
async function afficherAgenda() {
    if (currentView === "month") {
        renderMonthView();
    } else {
        renderWeekView();
    }
}

/* Vue mois */
function renderMonthView() {
    const grid = document.getElementById("agenda-grid");
    if (!grid) return;
    grid.innerHTML = "";

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const monthNames = [
        "Janvier","Février","Mars","Avril","Mai","Juin",
        "Juillet","Août","Septembre","Octobre","Novembre","Décembre"
    ];

    const title = document.getElementById("month-title");
    if (title) title.textContent = `${monthNames[month]} ${year}`;

    const firstDayIndex = new Date(year, month, 1).getDay(); // 0 = Dimanche
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const startIndex = firstDayIndex === 0 ? 6 : firstDayIndex - 1;

    for (let i = 0; i < startIndex; i++) {
        const emptyDiv = document.createElement("div");
        emptyDiv.className = "day empty";
        grid.appendChild(emptyDiv);
    }

    for (let d = 1; d <= daysInMonth; d++) {
        const dateDuJour = new Date(year, month, d);
        const div = createDayCell(dateDuJour, false);
        grid.appendChild(div);
    }
}

/* Vue semaine */
function renderWeekView() {
    const grid = document.getElementById("agenda-grid");
    if (!grid) return;
    grid.innerHTML = "";

    const startOfWeek = new Date(currentDate);
    const day = startOfWeek.getDay(); // 0 (Dim) ... 6 (Sam)
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1);
    startOfWeek.setDate(diff);

    const options = { day: "numeric", month: "long", year: "numeric" };
    const title = document.getElementById("month-title");
    if (title) title.textContent = `Semaine du ${startOfWeek.toLocaleDateString("fr-FR", options)}`;

    for (let i = 0; i < 7; i++) {
        const currentDay = new Date(startOfWeek);
        currentDay.setDate(startOfWeek.getDate() + i);
        const div = createDayCell(currentDay, true);
        grid.appendChild(div);
    }
}

/* Création d’une cellule jour */
function createDayCell(dateObj, isWeekView) {
    const div = document.createElement("div");
    div.className = "day";

    const dayNum = dateObj.getDate();

    if (isWeekView) {
        const dayName = dateObj.toLocaleDateString("fr-FR", { weekday: "short" });
        div.innerHTML =
            `<div style="border-bottom:1px solid #eee;margin-bottom:5px;padding-bottom:5px;color:#1e66ff;font-weight:bold;text-transform:capitalize;">
                ${dayName} ${dayNum}
             </div>`;
    } else {
        div.innerHTML = `<strong>${dayNum}</strong>`;
    }

    const dayEvents = allEventsCache.filter(ev => {
        const evDate = new Date(ev.dateDebut);
        return (
            evDate.getDate() === dayNum &&
            evDate.getMonth() === dateObj.getMonth() &&
            evDate.getFullYear() === dateObj.getFullYear() &&
            (ev.utilisateur.id === currentUser.id ||
                selectedProchesIds.has(ev.utilisateur.id))
        );
    });

    dayEvents.forEach(ev => {
        const eDiv = document.createElement("div");
        const isMine = ev.utilisateur.id === currentUser.id;

        eDiv.className = isMine ? "event event-mine" : "event event-other";

        let timeString = "";
        if (isWeekView) {
            const time = new Date(ev.dateDebut).toLocaleTimeString("fr-FR", {
                hour: "2-digit",
                minute: "2-digit"
            });
            timeString =
                `<span style="font-weight:bold;margin-right:5px;font-size:0.9em;">${time}</span>`;
        }

        if (!isMine) {
            eDiv.title = `Agenda de ${ev.utilisateur.prenom}`;
            eDiv.innerHTML = `${timeString}Occupé (${ev.utilisateur.prenom})`;
        } else {
            eDiv.innerHTML = `${timeString}${ev.titre}`;
            eDiv.style.cursor = "pointer";
            eDiv.addEventListener("click", () => openEditPopup(ev));
        }

        div.appendChild(eDiv);
    });

    return div;
}

/* =========================
   Récupère les événements
   ========================= */
async function fetchEvents() {
    try {
        const url = `${API_BASE_URL}/evenements/shared/${currentUser.id}`;
        const res = await fetch(url);

        if (!res.ok) throw new Error("Erreur API événements");
        allEventsCache = await res.json();

        if (typeof renderToday === "function") renderToday(allEventsCache);
        return allEventsCache;
    } catch (err) {
        console.error("fetchEvents ERREUR", err);
        return [];
    }
}

/* =========================
   Création / Modification
   ========================= */
async function saveEvent(e) {
    e.preventDefault();

    const id = document.getElementById("eventId").value;

    const event = {
        titre: document.getElementById("titre").value,
        description: document.getElementById("description").value,
        dateDebut: document.getElementById("dateDebut").value,
        dateFin: document.getElementById("dateFin").value
    };

    try {
        if (!id) {
            // création
            const res = await fetch(`${API_BASE_URL}/evenements/${currentUser.id}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(event)
            });
            if (!res.ok) throw new Error("Erreur création événement");
        } else {
            // modification
            await updateEvenement(id, event);
        }

        togglePopup(false);
        await fetchEvents();
        await afficherAgenda();
    } catch (err) {
        alert("❌ " + err.message);
    }
}

/* Modifier un événement */
async function updateEvenement(id, evenement) {
    const res = await fetch(`${API_BASE_URL}/evenements/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(evenement)
    });
    if (!res.ok) {
        throw new Error("Erreur lors de la modification de l'événement");
    }
    return await res.json();
}

/* Supprimer un événement */
async function deleteEvenement(id) {
    const res = await fetch(`${API_BASE_URL}/evenements/${id}`, {
        method: "DELETE"
    });
    if (!res.ok && res.status !== 204) {
        throw new Error("Erreur lors de la suppression de l'événement");
    }
}

/* =========================
   Événements du jour
   ========================= */
function renderToday(events) {
    const today = new Date();
    const list = document.getElementById("today-list");
    if (!list) return;
    list.innerHTML = "";

    const todayEvents = events.filter(e => {
        const d = new Date(e.dateDebut);
        return (
            d.getDate() === today.getDate() &&
            d.getMonth() === today.getMonth() &&
            d.getFullYear() === today.getFullYear()
        );
    });

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

/* =========================
   Sidebar des proches
   ========================= */
async function chargerProchesSidebar() {
    const container = document.getElementById("proches-list-agenda");
    if (!container) return;

    try {
        const liens = await fetchApi(`/liens/${currentUser.id}/proches`);

        if (!liens.length) {
            container.innerHTML = "<li>Aucun proche ajouté.</li>";
            return;
        }

        container.innerHTML = "";
        liens.forEach(lien => {
            const ami = lien.compteCible;
            const li = document.createElement("li");
            li.className = "proche-item";

            li.innerHTML = `
                <label class="friend-label">
                    <input type="checkbox" class="friend-checkbox" value="${ami.id}">
                    <div class="friend-info">
                        <div class="friend-avatar">
                            ${ami.prenom.charAt(0)}${ami.nom.charAt(0)}
                        </div>
                        <span>${ami.prenom} ${ami.nom}</span>
                    </div>
                    <span class="status-dot"></span>
                </label>
            `;

            const checkbox = li.querySelector("input");
            checkbox.addEventListener("change", e => {
                if (e.target.checked) {
                    selectedProchesIds.add(ami.id);
                } else {
                    selectedProchesIds.delete(ami.id);
                }
                afficherAgenda();
            });

            container.appendChild(li);
        });
    } catch (err) {
        console.error("Erreur chargement proches agenda", err);
    }
}
