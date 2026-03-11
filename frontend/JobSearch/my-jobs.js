const API_JOBS = "/api/jobs";
let allMyJobs = [];

// 1. Chargement initial
document.addEventListener('DOMContentLoaded', () => {
    loadJobs();
    loadStats();
});

// 2. Récupérer les offres
async function loadJobs() {
    try {
        // 🛡️ CORRECTION : Récupération robuste de l'ID
        const userStr = localStorage.getItem('utilisateur');
        if (!userStr || userStr === "null") {
            console.warn("⚠️ Utilisateur non connecté, arrêt du chargement des offres.");
            return;
        }
        const userObj = JSON.parse(userStr);
        const userId = userObj.id;

        const res = await fetch(`${API_JOBS}/my-jobs?compteId=${userId}`);

        if (!res.ok) throw new Error("Erreur 400: Mauvaise requête");

        allMyJobs = await res.json();
        filterJobs('ALL');
    } catch (err) {
        console.error(err);
    }
}

// 3. Récupérer les stats (KPIs)
async function loadStats() {
    try {
        const userStr = localStorage.getItem('utilisateur');
        if (!userStr || userStr === "null") return;
        const userObj = JSON.parse(userStr);
        const userId = userObj.id;

        const res = await fetch(`${API_JOBS}/stats?compteId=${userId}`);

        if (!res.ok) throw new Error("Erreur 400: Mauvaise requête");

        const stats = await res.json();

        // ✅ ON REMET LES NOMS EXACTS ENVOYÉS PAR TON JAVA
        document.getElementById('count-interesse').innerText = stats.interesse || 0;
        document.getElementById('count-postule').innerText = stats.postule || 0;
        document.getElementById('count-refuse').innerText = stats.refuse || 0;
    } catch (err) {
        console.error(err);
    }
}

// 4. Mettre à jour le statut (ex: Intéressé -> Postulé)
async function updateStatus(jobId, newStatus) {
    const job = allMyJobs.find(j => j.externalJobId === jobId);
    if (!job) return;

    // 🛡️ CORRECTION : Récupération robuste de l'ID
    const userStr = localStorage.getItem('utilisateur');
    if (!userStr || userStr === "null") {
        alert("Erreur de session : Impossible de trouver votre identifiant. Veuillez vous reconnecter.");
        return;
    }
    const userObj = JSON.parse(userStr);
    const userId = userObj.id;

    const payload = {
        compteId: userId,
        externalJobId: jobId,
        title: job.title,
        status: newStatus
    };

    await fetch(`${API_JOBS}/save`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    loadJobs();
    loadStats();
}

// 5. Confirmer la suppression ("X")
function confirmDelete(jobId) {
    if (confirm("Êtes-vous sûr de vouloir retirer cette offre ? Elle ira dans la corbeille.")) {
        updateStatus(jobId, 'REFUSE');
    }
}

// 6. Filtrer l'affichage
function filterJobs(status) {
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));

    let filtered = allMyJobs;

    if (status !== 'ALL') {
        filtered = allMyJobs.filter(job => job.status === status);
    }

    const container = document.getElementById('my-jobs-container');
    container.innerHTML = "";

    // Sécurité supplémentaire si filtered n'est pas un tableau
    if (!Array.isArray(filtered) || filtered.length === 0) {
        container.innerHTML = '<div style="text-align:center; width:100%; padding:40px; color:#666;">Aucune offre ici pour le moment. 😴</div>';
        return;
    }

    container.innerHTML = filtered.map(job => {
        if (job.status === 'SUGGESTION') {
            return `
            <div class="card-job" style="border-left: 5px solid #9b59b6;">
                <div class="card-body">
                    <span style="background:#9b59b6; color:white; padding:2px 8px; border-radius:10px; font-size:0.7em; float:right;">NOUVEAU</span>
                    <h3>${job.title}</h3>
                    <h4 style="color:#666;">${job.company}</h4>
                    <p>📍 ${job.location}</p>
                    <p style="font-size:0.9em; margin-top:5px; color:#888;">Trouvé par votre assistant 🤖</p>
                    <a href="${job.applyLink}" target="_blank" style="color:#3498db; text-decoration:none; display:block; margin-top:10px;">
                        Voir l'annonce <i class="fas fa-external-link-alt"></i>
                    </a>
                </div>
                <div class="card-actions" style="justify-content:space-around;">
                    <button class="btn-icon" style="color:#e74c3c;" onclick="updateStatus('${job.externalJobId}', 'REFUSE')" title="Refuser">
                        <i class="fas fa-times-circle"></i> Non merci
                    </button>
                    <button class="btn-icon" style="color:#2ecc71;" onclick="updateStatus('${job.externalJobId}', 'INTERESSE')" title="Garder">
                        <i class="fas fa-heart"></i> Garder
                    </button>
                </div>
            </div>`;
        }

        return `
        <div class="card-job status-${job.status}">
            <div class="card-body">
                <h3>${job.title}</h3>
                <h4 style="color:#666;">${job.company}</h4>
                <p>📍 ${job.location}</p>
                <a href="${job.applyLink}" target="_blank" style="color:#3498db; text-decoration:none; display:block; margin-top:10px;">
                    Voir l'offre originale <i class="fas fa-external-link-alt"></i>
                </a>
            </div>
            <div class="card-actions">
                ${job.status !== 'POSTULE' ? `
                <button class="btn-icon" style="color:#2ecc71;" title="J'ai postulé" 
                    onclick="updateStatus('${job.externalJobId}', 'POSTULE')">
                    <i class="fas fa-check-circle"></i> J'ai postulé
                </button>` : '<span style="color:#2ecc71; font-weight:bold;">Candidature envoyée !</span>'}

                ${job.status !== 'REFUSE' ? `
                <button class="btn-icon" style="color:#e74c3c;" title="Supprimer" 
                    onclick="confirmDelete('${job.externalJobId}')">
                    <i class="fas fa-trash"></i>
                </button>` : ''}
            </div>
        </div>
        `;
    }).join('');
}