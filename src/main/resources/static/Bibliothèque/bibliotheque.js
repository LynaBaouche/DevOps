// ======== DONNÉES SIMULÉES ========
const zones = [
    { nom:"RDC – Espace individuel", type:"individuel", total:90, libres:52 },
    { nom:"Étage 1 – Silencieux", type:"silencieux", total:120, libres:87 },
    { nom:"Étage 2 – Groupe", type:"groupe", total:96, libres:18 },
    { nom:"Étage 3 – Révisions", type:"individuel", total:75, libres:4 },
    { nom:"Salle Informatique", type:"info", total:40, libres:26 }
];

function statusColor(zone) {
    const ratio = zone.libres / zone.total;
    if (ratio >= 0.4) return "success";
    if (ratio >= 0.15) return "warning";
    return "danger";
}

// ======== RENDER ========
function updatePage() {
    const total = zones.reduce((s, z) => s + z.total, 0);
    const libres = zones.reduce((s, z) => s + z.libres, 0);
    const occupees = total - libres;
    const percent = Math.round((occupees / total) * 100);

    document.getElementById("totalPlaces").innerText = total;
    document.getElementById("totalDisponibles").innerText = libres;
    document.getElementById("totalOccupees").innerText = occupees;
    document.getElementById("lastUpdateLabel").innerText = "à l’instant";

    document.getElementById("percent").innerText = percent + "%";
    document.getElementById("libresInfo").innerText = "Places libres : " + libres;
    document.getElementById("occupeesInfo").innerText = "Places occupées : " + occupees;

    const circle = document.getElementById("circle");
    const angle = (percent / 100) * 360;
    circle.style.background =
        `conic-gradient(#4d8fff ${angle}deg, #d9e6ff ${angle}deg)`;


    const cardsContainer = document.getElementById("cards");
    cardsContainer.innerHTML = "";

    zones.forEach(z => {
        const card = document.createElement("div");
        card.className = "card";

        const color = statusColor(z);

        card.innerHTML = `
            <div class="card-title">${z.nom}</div>
            <span class="badge ${color}">${color === "success" ? "Disponible" : color === "warning" ? "Élevée" : "Pleine"}</span>
            <p>${z.libres} / ${z.total} places libres</p>
            <div class="card-footer">Mis à jour à l’instant</div>
        `;

        cardsContainer.appendChild(card);
    });
}

document.addEventListener("DOMContentLoaded", updatePage);
