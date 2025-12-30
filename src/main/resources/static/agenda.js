/*
   AGENDA — Intégré au backend EtudLife
    */
let currentDate = new Date(); // Date de référence
let currentView = 'month';    // 'month' ou 'week'
// Assurez-vous que ces variables existent dans la portée globale de app.js si elles ne le sont pas déjà
if (typeof allEventsCache === 'undefined') var allEventsCache = [];
if (typeof selectedProchesIds === 'undefined') var selectedProchesIds = new Set();
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

    document.getElementById("prevMonth").addEventListener("click", () => navigateDate(-1));
    document.getElementById("nextMonth").addEventListener("click", () => navigateDate(1));
    document.getElementById("btnViewMonth").addEventListener("click", () => switchView('month'));
    document.getElementById("btnViewWeek").addEventListener("click", () => switchView('week'));
    await chargerProchesSidebar();
    await fetchEvents();
    await afficherAgenda();

}
/* Ouvre / ferme la popup */
function togglePopup(show) {
    const popup = document.getElementById("popup");
    if (popup) popup.classList.toggle("hidden", !show);
}
function switchView(view) {
    currentView = view;

    // Mise à jour visuelle des boutons (Active / Inactive)
    document.getElementById("btnViewMonth").classList.toggle("active", view === 'month');
    document.getElementById("btnViewWeek").classList.toggle("active", view === 'week');

    // Adaptation CSS de la grille (pour avoir des cases plus hautes en semaine)
    const grid = document.getElementById("agenda-grid");
    if (view === 'week') {
        grid.classList.add("agenda-grid-week");
    } else {
        grid.classList.remove("agenda-grid-week");
    }

    // On rafraîchit l'affichage
    afficherAgenda();
}
function navigateDate(offset) {
    if (currentView === 'month') {
        // Ajoute ou retire 1 mois
        currentDate.setMonth(currentDate.getMonth() + offset);
    } else {
        // Ajoute ou retire 7 jours (1 semaine)
        currentDate.setDate(currentDate.getDate() + (offset * 7));
    }
    afficherAgenda();
}

async function afficherAgenda() {
    if (currentView === 'month') {
        renderMonthView();
    } else {
        renderWeekView();
    }
}
function renderMonthView() {
    const grid = document.getElementById("agenda-grid");
    if (!grid) return;
    grid.innerHTML = "";

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const monthNames = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];

    // Titre du mois (ID 'month-title' dans votre HTML)
    document.getElementById("month-title").textContent = `${monthNames[month]} ${year}`;

    // Calculs calendaires
    const firstDayIndex = new Date(year, month, 1).getDay(); // 0 = Dimanche
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    // Décalage pour commencer Lundi (Si Dimanche(0) -> 6 cases vides, sinon Jour-1)
    const startIndex = firstDayIndex === 0 ? 6 : firstDayIndex - 1;

    // 1. Cases vides du début
    for (let i = 0; i < startIndex; i++) {
        const emptyDiv = document.createElement("div");
        emptyDiv.className = "day empty";
        grid.appendChild(emptyDiv);
    }

    // 2. Jours du mois
    for (let d = 1; d <= daysInMonth; d++) {
        const dateDuJour = new Date(year, month, d);
        // On utilise la fonction partagée pour créer la case
        const div = createDayCell(dateDuJour, false);
        grid.appendChild(div);
    }
}
function renderWeekView() {
    const grid = document.getElementById("agenda-grid");
    grid.innerHTML = "";

    // On se cale sur le Lundi de la semaine actuelle
    const startOfWeek = new Date(currentDate);
    const day = startOfWeek.getDay(); // 0 (Dim) ... 6 (Sam)
    // Formule pour revenir au Lundi précédent (ou rester à Lundi)
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1);
    startOfWeek.setDate(diff);

    // Titre de la semaine
    const options = { day: 'numeric', month: 'long', year: 'numeric' };
    document.getElementById("month-title").textContent = `Semaine du ${startOfWeek.toLocaleDateString('fr-FR', options)}`;

    // On affiche 7 jours consécutifs
    for (let i = 0; i < 7; i++) {
        const currentDay = new Date(startOfWeek);
        currentDay.setDate(startOfWeek.getDate() + i);

        // On crée la case en mode "Semaine" (true)
        const div = createDayCell(currentDay, true);
        grid.appendChild(div);
    }
}
function createDayCell(dateObj, isWeekView) {
    const div = document.createElement("div");
    div.className = "day";

    const dayNum = dateObj.getDate();

    // HEADER de la case
    if (isWeekView) {
        // En semaine : "Lun 12" en bleu et gras
        const dayName = dateObj.toLocaleDateString('fr-FR', { weekday: 'short' });
        div.innerHTML = `<div style="border-bottom:1px solid #eee; margin-bottom:5px; padding-bottom:5px; color:#1e66ff; font-weight:bold; text-transform:capitalize;">${dayName} ${dayNum}</div>`;
    } else {
        // En mois : Juste le numéro "12"
        div.innerHTML = `<strong>${dayNum}</strong>`;
    }

    // FILTRAGE DES ÉVÉNEMENTS
    // On cherche dans le cache global les événements qui tombent ce jour-là
    const dayEvents = allEventsCache.filter(ev => {
        const evDate = new Date(ev.dateDebut);
        return evDate.getDate() === dayNum &&
            evDate.getMonth() === dateObj.getMonth() &&
            evDate.getFullYear() === dateObj.getFullYear() &&
            // Filtre : Soit c'est à moi, soit c'est un ami coché
            (ev.utilisateur.id === currentUser.id || selectedProchesIds.has(ev.utilisateur.id));
    });

    // AFFICHAGE DES ÉVÉNEMENTS
    dayEvents.forEach(ev => {
        const eDiv = document.createElement("div");
        const isMine = ev.utilisateur.id === currentUser.id;

        eDiv.className = isMine ? "event event-mine" : "event event-other";

        // Affichage de l'heure en vue semaine uniquement
        let timeString = "";
        if (isWeekView) {
            const time = new Date(ev.dateDebut).toLocaleTimeString('fr-FR', {hour: '2-digit', minute:'2-digit'});
            timeString = `<span style="font-weight:bold; margin-right:5px; font-size:0.9em;">${time}</span>`;
        }

        if (!isMine) {
            eDiv.title = `Agenda de ${ev.utilisateur.prenom}`;
            eDiv.innerHTML = `${timeString}Occupé (${ev.utilisateur.prenom})`;
        } else {
            eDiv.innerHTML = `${timeString}${ev.titre}`;
        }

        div.appendChild(eDiv);
    });

    return div;
}
/* Changement de mois */


/*  Récupère les événements depuis le backend */
async function fetchEvents() {
    try {
        const res = await fetch(`${API_BASE_URL}/evenements/shared/${currentUser.id}`);
        if (!res.ok) throw new Error("Erreur API événements");
        allEventsCache = await res.json();
        // Mise à jour sidebar "Aujourd'hui"
        if(typeof renderToday === 'function') renderToday(allEventsCache);
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
    const today = new Date();
    const list = document.getElementById("today-list");
    if (!list) return;
    list.innerHTML = "";

    const todayEvents = events.filter(e => {
        const d = new Date(e.dateDebut);
        return d.getDate() === today.getDate()
            && d.getMonth() === today.getMonth()
            && d.getFullYear() === today.getFullYear();
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