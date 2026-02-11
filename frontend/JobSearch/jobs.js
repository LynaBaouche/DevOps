const API_JOBS = "/api/jobs";

// 1. Fonction de recherche
async function searchJobs() {
    const query = document.getElementById('jobQuery').value;
    const location = document.getElementById('jobLocation').value;
    const container = document.getElementById('jobs-container');

    container.innerHTML = '<p style="text-align:center">Recherche en cours... ⏳</p>';

    try {
        let url = `${API_JOBS}/search?query=${encodeURIComponent(query)}`;
        if(location) url += `&location=${encodeURIComponent(location)}`;

        const res = await fetch(url);
        const jobs = await res.json();

        if (!jobs || jobs.length === 0) {
            container.innerHTML = '<p style="text-align:center">Aucune offre trouvée.</p>';
            return;
        }

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
        // Sécurisation de l'objet pour l'injecter dans le HTML
        const jobData = encodeURIComponent(JSON.stringify(job));

        return `
        <div class="card-pro"> 
            <div class="card-body">
                <span class="job-source-badge" style="float:right; background:#eee; padding:2px 5px; font-size:0.8em; border-radius:4px;">
                    ${job.job_publisher || 'Web'}
                </span>
                
                <h3>${job.job_title || 'Titre non disponible'}</h3>
                <p>🏢 <strong>${job.employer_name || 'Entreprise confidentielle'}</strong></p>
                <p>📍 ${job.job_city || 'France'}</p>
                
                <p style="font-size:0.9em; color:#666; margin: 10px 0;">
                    ${job.job_description ? job.job_description.substring(0, 100) + '...' : ''}
                </p>

                <div class="card-actions" style="margin-top:15px; display:flex; gap:10px; align-items:center;">
                    <a href="${job.job_apply_link}" target="_blank" class="btn-details" style="text-decoration:none; color:#3498db; font-weight:bold;">
                        Voir l'offre 🔗
                    </a>
                    
                    <div class="status-btn-group" style="margin-left:auto; display:flex; gap:5px;">
                        <button class="btn-fav" onclick="saveJob('${jobData}', 'INTERESSE', this)" style="cursor:pointer; border:1px solid #ddd; background:white; padding:5px 10px; border-radius:5px;" title="Sauvegarder">
                            ❤️
                        </button>
                        
                        <button class="btn-blue" onclick="saveJob('${jobData}', 'POSTULE', this)" style="cursor:pointer; border:1px solid #ddd; background:#e3f2fd; padding:5px 10px; border-radius:5px;" title="J'ai postulé">
                            ✅
                        </button>
                        
                        <button onclick="banJob('${jobData}', this)" style="cursor:pointer; border:none; background:none; font-size:1.2em;" title="Ne m'intéresse pas">
                          ❌
                        </button>
                    </div>
                </div>
            </div>
        </div>
        `;
    }).join('');
}

// 3. Sauvegarde de l'offre
async function saveJob(jobJsonURI, status, btnElement) {
    try {
        const job = JSON.parse(decodeURIComponent(jobJsonURI));

        console.log("Tentative de sauvegarde pour ID :", job.job_id); // DEBUG

        // Vérification critique
        if (!job.job_id) {
            alert("Impossible de sauvegarder : L'ID de l'offre est manquant.");
            return;
        }

        const payload = {
            externalJobId: job.job_id, // C'est ici que ça bloquait avant
            title: job.job_title,
            company: job.employer_name,
            location: job.job_city,
            applyLink: job.job_apply_link,
            status: status
        };

        const res = await fetch(`${API_JOBS}/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            // Animation visuelle
            const originalText = btnElement.innerText;
            btnElement.innerText = "👌";
            btnElement.style.backgroundColor = "#dff0d8";

            setTimeout(() => {
                btnElement.innerText = originalText;
                btnElement.style.backgroundColor = "";
            }, 2000);
        } else {
            console.error("Erreur save:", res);
            alert("Erreur serveur lors de la sauvegarde.");
        }
    } catch (err) {
        console.error("Erreur saveJob:", err);
        alert("Erreur technique. Vérifiez la console.");
    }
}

// 4. Fonction pour Bannir (SORTIE DE LA FONCTION saveJob !)
async function banJob(jobJsonURI, btnElement) {
    if(!confirm("Cette offre ne sera plus affichée. Continuer ?")) return;

    // On utilise saveJob pour envoyer le statut REFUSE
    await saveJob(jobJsonURI, 'REFUSE', btnElement);

    // On cache la carte
    const card = btnElement.closest('.card-pro');
    if (card) {
        card.style.transition = "all 0.5s ease";
        card.style.opacity = '0.1';
        setTimeout(() => card.remove(), 500);
    }
}