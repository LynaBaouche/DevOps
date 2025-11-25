const API_URL = "/api/annonces";

const annoncesList = document.getElementById("annonces-list");
const filters = document.querySelectorAll(".filter");
const searchInput = document.getElementById("searchInput");
const statsBox = document.getElementById("stats");

let annonces = [];

async function loadAnnonces(cat = "toutes") {
    const res = await fetch(`${API_URL}?categorie=${cat}`);
    annonces = await res.json();

    displayStats();
    displayAnnonces(annonces);
}

function displayStats() {
    const cats = ["logement", "cours", "emploi", "service", "objet"];

    statsBox.innerHTML = cats
        .map(c => `
            <div class="stats-box">
                <h2>${annonces.filter(a => a.categorie === c).length}</h2>
                <p>${c}</p>
            </div>
        `)
        .join("");
}

function displayAnnonces(list) {
    annoncesList.innerHTML = list
        .map(a => `
            <div class="card">
                <img src="/Annonce/images/${a.image}" alt="">

                <div class="card-content">
                    <span class="badge">${a.categorie}</span>
                    <h3>${a.titre}</h3>
                    <p>${a.description.substring(0,120)}...</p>
                    <div class="price">${a.prix}</div>
                </div>
            </div>
        `)
        .join("");
}

filters.forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelector(".filter.active")?.classList.remove("active");
        btn.classList.add("active");

        loadAnnonces(btn.dataset.cat);
    });
});

searchInput.addEventListener("input", () => {
    const q = searchInput.value.toLowerCase();

    displayAnnonces(
        annonces.filter(a =>
            a.titre.toLowerCase().includes(q) ||
            a.description.toLowerCase().includes(q)
        )
    );
});

loadAnnonces();
