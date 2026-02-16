form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const rawRemun = document.getElementById('remunerationMin').value;
    const preferenceData = {
        motsCles: document.getElementById('motsCles').value,
        localisation: document.getElementById('localisation').value,
        remunerationMin: rawRemun === "" ? null : parseInt(rawRemun),
        frequence: document.getElementById('frequence').value // "Quotidien", "Hebdomadaire", etc.
    };

    const searchCriteria = {
        motsCles: preferenceData.motsCles,
        location: preferenceData.localisation
    };
    loadJobs(searchCriteria);

    // 3. SAUVEGARDE POUR LE BATCH (Le Robot)
    try {
        const response = await fetch('/api/preferences', { // Nouvelle route
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(preferenceData)
        });

        if (response.ok) {
            // Petit feedback visuel sympa
            const btn = form.querySelector('button[type="submit"]');
            const originalText = btn.innerText;
            btn.innerText = "✅ Sauvegardé & Batch programmé !";
            setTimeout(() => btn.innerText = originalText, 3000);
        } else {
            console.warn("Erreur sauvegarde préférences");
        }
    } catch (error) {
        console.error("Erreur réseau :", error);
    }
});