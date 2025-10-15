// ============================
// 🌐 Fonctions existantes
// ============================

// Petites interactions (pas d'API ici)
const smoothTo = (id) => {
    const el = document.querySelector(id);
    if (!el) return;
    window.scrollTo({ top: el.offsetTop - 60, behavior: 'smooth' });
};

// Liens nav
document.getElementById('go-profile')?.addEventListener('click', (e)=>{ e.preventDefault(); smoothTo('#profile'); });
document.getElementById('go-courses')?.addEventListener('click', (e)=>{ e.preventDefault(); smoothTo('#courses'); });
document.getElementById('go-planning')?.addEventListener('click', (e)=>{ e.preventDefault(); smoothTo('#planning'); });

// Boutons CTA
document.getElementById('btn-profile')?.addEventListener('click', ()=> smoothTo('#profile'));
document.getElementById('btn-courses')?.addEventListener('click', ()=> smoothTo('#courses'));


// ============================
// 🎓 Partie Profil Étudiant
// ============================

// Fonction de chargement depuis ton backend
async function chargerProfilEtudiant(id) {
    try {
        const response = await fetch(`http://localhost:8080/api/comptes/${id}`);
        if (!response.ok) throw new Error("Erreur API");
        const data = await response.json();

        // Remplissage du profil
        const nomComplet = document.getElementById("nomComplet");
        if (nomComplet) nomComplet.textContent = `${data.prenom} ${data.nom}`;

        const userName = document.getElementById("user-name");
        if (userName) userName.textContent = data.prenom;

        const email = document.getElementById("email");
        if (email) email.textContent = data.email || "non renseigné";

        const ville = document.getElementById("ville");
        if (ville) ville.textContent = data.ville || "Paris, France";

        const age = document.getElementById("age");
        if (age) age.textContent = data.age ? `${data.age} ans` : "21 ans";

        // Exemple pour afficher les groupes ou liens s’ils existent
        if (data.groupes?.length) {
            console.log("Groupes de l'étudiant :", data.groupes);
        }

    } catch (err) {
        console.error("Erreur lors du chargement du profil :", err);
    }
}

// Appel automatique si on est sur la page profil
if (window.location.pathname.includes("profil.html")) {
    chargerProfilEtudiant(1); // exemple id=1
}
