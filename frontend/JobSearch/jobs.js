const API_JOBS = "/api/jobs";
const API_PREF = "https://etudlife-backend.onrender.com/api/preferences"; // Serveur distant pour le batch
let currentJobsCache = [];

// 1. Fonction de recherche
async function searchJobs() {
    const query = document.getElementById('jobQuery').value;
    const location = document.getElementById('jobLocation').value;
    const frequency = document.getElementById('jobFrequency').value;
    const container = document.getElementById('jobs-container');

    container.innerHTML = '<p style="text-align:center">Recherche en cours... ⏳</p>';
    // --- Sauvegarde automatique de la préférence pour le batch ---
    // --- Sauvegarde automatique de la préférence pour le batch ---
    const userStr = localStorage.getItem('utilisateur');
    if (!userStr) {
        alert("Vous devez être connecté pour effectuer cette action.");
        return;
    }
    const userObj = JSON.parse(userStr);
    const userId = userObj.id;
    try {
        await fetch(API_PREF, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                motsCles: query,
                localisation: location,
                frequence: frequency,
                compte: { id: userId} // ID par défaut pour les tests
            })
        });
        console.log("✅ Préférence enregistrée pour le batch de 02h00");
    } catch (err) {
        console.error("❌ Erreur sauvegarde batch:", err);
    }
    // --- Recherche des offres ---
    try {
        let url = `${API_JOBS}/search?query=${encodeURIComponent(query)}`;
        if(location) url += `&location=${encodeURIComponent(location)}`;

        const res = await fetch(url);
        const jobs = await res.json();

        if (!jobs || jobs.length === 0) {
            container.innerHTML = '<p style="text-align:center">Aucune offre trouvée.</p>';
            return;
        }

        // SAUVEGARDE LES OFFRES DANS LE CACHE GLOBAL
        currentJobsCache = jobs;
        displayJobs(jobs);

    } catch (err) {
        console.error(err);
        container.innerHTML = '<p style="color:red; text-align:center">Erreur technique lors de la recherche.</p>';
    }
}

// 2. Affichage des cartes
function displayJobs(jobs) {
    const container = document.getElementById('jobs-container');

    container.innerHTML = jobs.map(job => {
        // Sécurisation visuelle
        const title = job.job_title || 'Titre non disponible';
        const company = job.employer_name || 'Entreprise confidentielle';
        const city = job.job_city || 'France';
        const desc = job.job_description ? job.job_description.substring(0, 100) + '...' : '';
        const source = job.job_publisher || 'Web';
        const link = job.job_apply_link || '#';

        // 🚨 ICI LA MAGIE : On passe seulement l'ID (job.job_id), pas tout le JSON
        // On utilise l'ID pour retrouver l'offre dans currentJobsCache plus tard
        return `
        <div class="card-pro"> 
            <div class="card-body">
                <span class="job-source-badge" style="float:right; background:#eee; padding:2px 5px; font-size:0.8em; border-radius:4px;">
                    ${source}
                </span>
                
                <h3>${title}</h3>
                <p>🏢 <strong>${company}</strong></p>
                <p>📍 ${city}</p>
                <p style="font-size:0.9em; color:#666; margin: 10px 0;">${desc}</p>

                <div class="card-actions" style="margin-top:15px; display:flex; gap:10px; align-items:center;">
                    <a href="${link}" target="_blank" class="btn-details" style="text-decoration:none; color:#3498db; font-weight:bold;">
                        Voir l'offre 🔗
                    </a>
                    
                    <div class="status-btn-group" style="margin-left:auto; display:flex; gap:5px;">
                        <button class="btn-fav" onclick="saveJob('${job.job_id}', 'INTERESSE', this)" style="cursor:pointer; border:1px solid #ddd; background:white; padding:5px 10px; border-radius:5px;" title="Sauvegarder">
                            ❤️
                        </button>
                        
                        <button class="btn-blue" onclick="saveJob('${job.job_id}', 'POSTULE', this)" style="cursor:pointer; border:1px solid #ddd; background:#e3f2fd; padding:5px 10px; border-radius:5px;" title="J'ai postulé">
                            ✅
                        </button>
                    </div>
                </div>
            </div>
        </div>
        `;
    }).join('');
}


// 3. Sauvegarde de l'offre
async function saveJob(jobId, status, btnElement) {
    try {
        const job = currentJobsCache.find(j => j.job_id === jobId);

        if (!job) {
            console.error("Offre introuvable dans le cache pour l'ID:", jobId);
            return;
        }

        // 🛡️ CORRECTION ICI : On utilise la même clé que dans my-jobs.js
        const userId = localStorage.getItem('userId');

        if (!userId || userId === "null") {
            alert("Vous n'êtes pas connecté. Redirection vers la page de connexion.");
            window.location.href = "../login.html";
            return;
        }

        const payload = {
            compteId: userId, // L'ID est passé correctement ici
            externalJobId: job.job_id,
            title: job.job_title,
            company: job.employer_name,
            location: job.job_city,
            applyLink: job.job_apply_link,
            status: status
        };

        console.log("🚀 ENVOI AU BACKEND :", payload);

        const res = await fetch(`${API_JOBS}/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const originalText = btnElement.innerText;
            btnElement.innerText = "👌";
            btnElement.style.backgroundColor = "#dff0d8";
            setTimeout(() => {
                btnElement.innerText = originalText;
                btnElement.style.backgroundColor = "";
            }, 2000);
        } else {
            console.error("Erreur serveur (500).");
            alert("Erreur serveur lors de la sauvegarde.");
        }
    } catch (err) {
        console.error("Erreur JS:", err);
    }
}