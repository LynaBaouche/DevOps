const zones = [
    { etage: "Étage 0", type: "groupe", libres: 12, total: 40, confort: "Bon" },
    { etage: "Étage 1", type: "individuel", libres: 5, total: 30, confort: "Moyen" },
    { etage: "Étage 2", type: "silencieux", libres: 1, total: 20, confort: "Faible" },
    { etage: "Salle informatique", type: "info", libres: 7, total: 25, confort: "Bon" }
];

const cards = document.getElementById("cards");

function render() {
    let totalPlaces = 0;
    let totalLibres = 0;

    cards.innerHTML = "";

    zones.forEach(z => {
        totalPlaces += z.total;
        totalLibres += z.libres;

        cards.innerHTML += `
            <article class="card">
                <div class="card-title">${z.etage}</div>
                <div class="badge success">Places libres : ${z.libres}</div>
                <div class="badge warning">Occupées : ${z.total - z.libres}</div>
                <div class="card-footer">Confort : ${z.confort}</div>
            </article>
        `;
    });

    // Statistiques globales
    document.getElementById("totalPlaces").textContent = totalPlaces;
    document.getElementById("totalDisponibles").textContent = totalLibres;
    document.getElementById("totalOccupees").textContent = totalPlaces - totalLibres;

    // Pourcentage
    const pct = Math.round(((totalPlaces - totalLibres) / totalPlaces) * 100);

    document.getElementById("percent").textContent = pct + "%";
    document.getElementById("libresInfo").textContent = "Places libres : " + totalLibres;
    document.getElementById("occupeesInfo").textContent = "Places occupées : " + (totalPlaces - totalLibres);

    // Cercle dynamique
    const circle = document.getElementById("circle");
    circle.style.background = `conic-gradient(#2563eb ${pct * 3.6}deg, #dbeafe 0deg)`;

    // Dernière mise à jour
    document.getElementById("lastUpdateLabel").textContent =
        new Date().toLocaleTimeString();
}

document.addEventListener("DOMContentLoaded", render);
