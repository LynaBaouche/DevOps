const API_JOBS = "/api/jobs";

// 1. Fonction de recherche
async function searchJobs() {
    const query = document.getElementById('jobQuery').value;
    const location = document.getElementById('jobLocation').value;
    const container = document.getElementById('jobs-container');

    container.innerHTML = '<p>Recherche en cours... ⏳</p>';

    try {
        // Appel au backend qui contacte JSearch
        const res = await fetch(`${API_JOBS}/search?query=${encodeURIComponent(query)}&location=${encodeURIComponent(location)}`);
        const jobs = await res.json();

        if (jobs.length === 0) {
            container.innerHTML = '<p>Aucune offre trouvée (vérifiez les quotas API ou les filtres).</p>';
            return;
        }

        displayJobs(jobs);

    } catch (err) {
        console.error(err);
        container.innerHTML = '<p style="color:red">Erreur lors de la recherche.</p>';
    }
}

// 2. Affichage des cartes
function displayJobs(jobs) {
    const container = document.getElementById('jobs-container');

    container.innerHTML = jobs.map(job => {
        // On sécurise les données pour éviter les bugs si un champ est vide
        const jobData = encodeURIComponent(JSON.stringify(job));

        return `
        <div class="card-pro">
            <div class="card-body">
                <span class="job-source-badge">${job.job_publisher || 'Web'}</span>
                
                <h3>${job.job_title}</h3>
                <p>🏢 <strong>${job.employer_name}</strong></p>
                <p>📍 ${job.job_city || 'Lieu non précisé'}</p>
                
                <p style="font-size:0.9em; color:#666; margin: 10px 0;">
                    ${job.job_description ? job.job_description.substring(0, 100) + '...' : ''}
                </p>

                <div class="card-actions" style="flex-direction:column; align-items:stretch;">
                    <a href="${job.job_apply_link}" target="_blank" class="btn-details" style="text-align:center; margin-bottom:10px;">
                        Voir l'offre originale 🔗
                    </a>
                    
                    <div class="status-btn-group">
                        <button class="btn-fav" onclick="saveJob('${jobData}', 'INTERESSE', this)">
                            ❤️ Intéressé
                        </button>
                        <button class="btn-blue" onclick="saveJob('${jobData}', 'POSTULE', this)" style="font-size:0.8em;">
                            ✅ J'ai postulé
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
    const job = JSON.parse(decodeURIComponent(jobJsonURI));

    const payload = {
        externalJobId: job.job_id,
        title: job.job_title,
        company: job.employer_name,
        location: job.job_city,
        applyLink: job.job_apply_link,
        status: status
    };

    try {
        const res = await fetch(`${API_JOBS}/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            // Feedback visuel
            const originalText = btnElement.innerText;
            btnElement.innerText = "Sauvegardé ! 👌";
            btnElement.style.backgroundColor = "#4CAF50";
            btnElement.style.color = "white";
            setTimeout(() => {
                btnElement.innerText = originalText;
                btnElement.style.backgroundColor = "";
                btnElement.style.color = "";
            }, 2000);
        } else {
            alert("Erreur lors de la sauvegarde.");
        }
    } catch (err) {
        console.error(err);
        alert("Erreur technique.");
    }
}