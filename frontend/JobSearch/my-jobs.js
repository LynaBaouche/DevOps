const API_JOBS = "/api/jobs";
let allMyJobs = []; // Stockage local pour filtrer sans rappeler l'API

// 1. Chargement initial
document.addEventListener('DOMContentLoaded', () => {
    loadJobs();
    loadStats();
});

// 2. Récupérer les offres
async function loadJobs() {
    try {
        const res = await fetch(`${API_JOBS}/my-jobs`); // Récupère tout
        allMyJobs = await res.json();
        filterJobs('ALL'); // Affiche tout par défaut
    } catch (err) {
        console.error(err);
    }
}

// 3. Récupérer les stats (KPIs)
async function loadStats() {
    try {
        const res = await fetch(`${API_JOBS}/stats`);
        const stats = await res.json();

        document.getElementById('count-interesse').innerText = stats.interesse;
        document.getElementById('count-postule').innerText = stats.postule;
        document.getElementById('count-refuse').innerText = stats.refuse;
    } catch (err) {
        console.error(err);
    }
}

// 4. Filtrer l'affichage
function filterJobs(status) {
    // Gestion des boutons actifs
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active'); // L'événement click met le bouton en actif

    let filtered = allMyJobs;
    if (status !== 'ALL') {
        filtered = allMyJobs.filter(job => job.status === status);
    }

    const container = document.getElementById('my-jobs-container');
    if (filtered.length === 0) {
        container.innerHTML = '<p style="text-align:center; width:100%; color:#999;">Aucune offre dans cette catégorie.</p>';
        return;
    }

    container.innerHTML = filtered.map(job => `
        <div class="card-job status-${job.status}">
            <div class="card-body">
                <h3>${job.title || 'Titre inconnu'}</h3>
                <h4 style="color:#666;">${job.company || 'Entreprise'}</h4>
                <p>📍 ${job.location || 'France'}</p>
                <a href="${job.applyLink}" target="_blank" style="color:#3498db; text-decoration:none; display:block; margin-top:10px;">
                    Voir l'offre originale <i class="fas fa-external-link-alt"></i>
                </a>
            </div>
            
            <div class="card-actions">
                ${job.status !== 'POSTULE' ? `
                <button class="btn-icon" style="color:#2ecc71;" title="Marquer comme Postulé" 
                    onclick="updateStatus('${job.externalJobId}', 'POSTULE')">
                    <i class="fas fa-check-circle"></i> J'ai postulé
                </button>` : '<span style="color:#2ecc71; font-weight:bold;">Candidature envoyée !</span>'}

                ${job.status !== 'REFUSE' ? `
                <button class="btn-icon" style="color:#e74c3c;" title="Ne m'intéresse plus" 
                    onclick="confirmDelete('${job.externalJobId}')">
                    <i class="fas fa-times-circle"></i>
                </button>` : ''}
            </div>
        </div>
    `).join('');
}

// 5. Mettre à jour le statut (ex: Intéressé -> Postulé)
async function updateStatus(jobId, newStatus) {
    // On doit reconstruire l'objet minimum pour le DTO
    // Astuce : on cherche l'offre dans notre liste locale
    const job = allMyJobs.find(j => j.externalJobId === jobId);
    if (!job) return;

    const payload = {
        externalJobId: jobId,
        title: job.title,
        status: newStatus
    };

    await fetch(`${API_JOBS}/save`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    // On recharge tout pour mettre à jour les KPIs et la liste
    loadJobs();
    loadStats();
}

// 6. Confirmer la suppression ("X")
function confirmDelete(jobId) {
    if (confirm("Êtes-vous sûr de vouloir retirer cette offre ? Elle ira dans la corbeille.")) {
        updateStatus(jobId, 'REFUSE');
    }
}