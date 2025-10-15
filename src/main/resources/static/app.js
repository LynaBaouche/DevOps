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
