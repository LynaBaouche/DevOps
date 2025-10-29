const API_BASE_URL = 'http://localhost:8080/api';
let currentUser = null; // L'utilisateur "connecté"

// 1. Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', () => {
    // Attache les écouteurs aux boutons Connexion/Déconnexion
    document.getElementById('btn-login').addEventListener('click', login);
    document.getElementById('btn-logout').addEventListener('click', logout);

    // Attache les formulaires de l'application (qui est cachée)
    document.getElementById('form-search-compte').addEventListener('submit', searchCompte);
    document.getElementById('form-join-groupe').addEventListener('submit', joinGroupe);
    document.getElementById('form-create-post').addEventListener('submit', createPost);
});

// 2. Fonction de Connexion
async function login() {
    // Connexion simple sans pop-up
    const prenom = prompt("Entrez votre Prénom :");
    const nom = prompt("Entrez votre Nom :");

    if (!prenom || !nom) {
        alert("Connexion annulée.");
        return;
    }

    try {
        // Appelle l'API pour trouver le compte
        currentUser = await fetchApi(`/comptes/search?nom=${nom}&prenom=${prenom}`);

        // ---- SUCCÈS DE LA CONNEXION ----
        console.log("Connecté en tant que:", currentUser);

        // Bascule l'affichage
        document.getElementById('homepage-content').style.display = 'none';
        document.getElementById('app-container').style.display = 'grid';

        // Met à jour les boutons du header
        document.getElementById('btn-login').style.display = 'none';
        document.getElementById('btn-logout').style.display = 'block';

        // Charge toutes les données de l'application
        await loadApplicationData();

    } catch (error) {
        console.error('Erreur de connexion:', error);
        alert('Connexion échouée. Vérifiez votre nom et prénom.');
    }
}

// 3. Fonction de Déconnexion
function logout() {
    currentUser = null;

    // Bascule l'affichage
    document.getElementById('homepage-content').style.display = 'block';
    document.getElementById('app-container').style.display = 'none';

    // Met à jour les boutons du header
    document.getElementById('btn-login').style.display = 'block';
    document.getElementById('btn-logout').style.display = 'none';
}

// 4. Chargement des données de l'application (une fois connecté)
async function loadApplicationData() {
    if (!currentUser) return; // Sécurité

    // Recharge le currentUser pour avoir les listes à jour (groupes, etc.)
    const users = await fetchApi('/comptes'); //
    currentUser = users.find(u => u.id == currentUser.id);

    // Lance tous les rendus en parallèle
    await Promise.all([
        renderUserProfile(),
        renderUserProches(),
        renderUserGroupes(),
        renderAllGroupesList()
    ]);

    // Charge le fil du premier groupe (s'il existe)
    if (currentUser.groupes.length > 0) {
        await renderFeedPosts(currentUser.groupes[0].id);
    } else {
        document.getElementById('feed-posts').innerHTML = "<h4>Rejoignez un groupe pour voir les posts.</h4>";
    }
}

// 5. Rendu du Profil
async function renderUserProfile() {
    document.getElementById('user-profile').innerHTML = `
        <h4>${currentUser.nom} ${currentUser.prenom}</h4>
        <p>(ID: ${currentUser.id})</p>
    `;
}

// 6. Rendu des Proches
async function renderUserProches() {
    const liens = await fetchApi(`/liens/${currentUser.id}/proches`); //
    const list = document.getElementById('user-proches');
    if(liens.length === 0) {
        list.innerHTML = "<p>Vous n'avez aucun proche.</p>";
        return;
    }
    list.innerHTML = '<ul>' +
        liens.map(lien => `<li>${lien.compteCible.nom} ${lien.compteCible.prenom}</li>`).join('') +
        '</ul>';
}

// 7. Rendu des Groupes de l'utilisateur
async function renderUserGroupes() {
    const list = document.getElementById('user-groupes');
    if(currentUser.groupes.length === 0) {
        list.innerHTML = "<p>Vous n'avez rejoint aucun groupe.</p>";
    } else {
        list.innerHTML = '<ul>' +
            currentUser.groupes.map(g => `<li data-id="${g.id}" class="groupe-item">${g.nom}</li>`).join('') +
            '</ul>';
    }

    const selectPost = document.getElementById('select-my-groupes-post');
    selectPost.innerHTML = currentUser.groupes.map(g => `<option value="${g.id}">${g.nom}</option>`).join('');

    document.querySelectorAll('.groupe-item').forEach(item => {
        item.style.cursor = 'pointer';
        item.addEventListener('click', () => renderFeedPosts(item.dataset.id));
    });
}

// 8. Rendu de TOUS les groupes (pour les rejoindre)
async function renderAllGroupesList() {
    const allGroupes = await fetchApi('/groupes'); //
    const select = document.getElementById('select-all-groupes');
    const myGroupIds = currentUser.groupes.map(g => g.id);
    const otherGroupes = allGroupes.filter(g => !myGroupIds.includes(g.id));
    select.innerHTML = otherGroupes.map(g => `<option value="${g.id}">${g.nom}</option>`).join('');
}

// 9. Rendu des Posts d'un groupe
async function renderFeedPosts(groupeId) {
    const posts = await fetchApi(`/posts/groupe/${groupeId}`);
    const feed = document.getElementById('feed-posts');
    let html = `<h4>Posts du groupe (ID ${groupeId})</h4>`;

    if(posts.length === 0) {
        html += "<p>Aucun post dans ce groupe pour le moment.</p>";
    } else {
        html += posts.map(post => `
            <div class="post">
                <div class="post-header"><strong>${post.auteur.nom}</strong> - ${new Date(post.dateCreation).toLocaleString()}</div>
                <div class="post-content">${post.contenu}</div>
            </div>
        `).join('');
    }
    feed.innerHTML = html;
}

// 10. Action: Rechercher un compte
async function searchCompte(e) {
    e.preventDefault();
    const nom = document.getElementById('search-nom').value;
    const prenom = document.getElementById('search-prenom').value;

    try {
        const compte = await fetchApi(`/comptes/search?nom=${nom}&prenom=${prenom}`); //
        document.getElementById('search-results').innerHTML = `
            <div class="result">
                <span>${compte.nom} ${compte.prenom} (ID: ${compte.id})</span>
                <button type="button" onclick="addProche(${compte.id})">Ajouter</button>
            </div>
        `;
    } catch(err) {
        document.getElementById('search-results').innerHTML = `<p>Compte introuvable.</p>`;
    }
}

// 11. Action: Ajouter un proche
async function addProche(cibleId) {
    if (cibleId === currentUser.id) {
        alert("Vous ne pouvez pas vous ajouter vous-même !");
        return;
    }
    await fetchApi(`/liens?idSource=${currentUser.id}&idCible=${cibleId}`, { method: 'POST' }); //
    alert("Proche ajouté !");
    await renderUserProches();
}

// 12. Action: Rejoindre un groupe
async function joinGroupe(e) {
    e.preventDefault();
    const groupeId = document.getElementById('select-all-groupes').value;
    if(!groupeId) {
        alert("Aucun groupe à rejoindre.");
        return;
    }
    await fetchApi(`/groupes/${groupeId}/ajouter/${currentUser.id}`, { method: 'POST' }); //
    alert("Groupe rejoint !");
    await loadApplicationData(); // Recharge tout
}

// 13. Action: Créer un post
async function createPost(e) {
    e.preventDefault();
    const contenu = document.getElementById('post-contenu').value;
    const groupeId = document.getElementById('select-my-groupes-post').value;

    if(!groupeId) {
        alert("Vous devez rejoindre un groupe avant de poster.");
        return;
    }

    const postData = { auteurId: currentUser.id, groupeId: groupeId, contenu: contenu };

    await fetchApi('/posts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(postData)
    });
    document.getElementById('form-create-post').reset();
    await renderFeedPosts(groupeId);
}

// 14. Fonction utilitaire pour appeler l'API
async function fetchApi(endpoint, options = {}) {
    try {
        const response = await fetch(API_BASE_URL + endpoint, options);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur API: ${response.status} - ${errorText}`);
        }
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            return await response.json();
        } else {
            return;
        }
    } catch (error) {
        console.error('Erreur Fetch:', error);
        throw error;
    }
    // Quand on clique sur "Ressources partagées"
    document.querySelector('h3').addEventListener('click', () => {
        document.getElementById('uploadModal').style.display = 'flex';
    });

// Fermer la modale si on clique à l’extérieur
    document.getElementById('uploadModal').addEventListener('click', (e) => {
        if (e.target.id === 'uploadModal') e.target.style.display = 'none';
    });
}

