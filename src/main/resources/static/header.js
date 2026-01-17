document.addEventListener("DOMContentLoaded", async () => {
    // ============================================================
    // 1. CONFIGURATION
    // ============================================================
    const isSubFolder = window.location.pathname.split('/').length > 2 && !window.location.pathname.endsWith('/');
    const basePath = isSubFolder ? "../" : "";

    // ============================================================
    // 2. INJECTION DU HEADER (AVEC LE SOUS-TITRE EMAIL RESTAURÉ)
    // ============================================================

    if (!document.querySelector(".main-header")) {
        const headerHTML = `
    <header class="main-header">
        <div class="header-container">
            
            <div class="header-left">
                <button id="mobile-menu-btn" class="hamburger-btn">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M4 6H20M4 12H20M4 18H20" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </button>

                <a href="${basePath}index.html" class="logo">
                    <img src="${basePath}images/etudlife.png" alt="Logo EtudLife" class="logo-icon">
                    <span class="logo-text"><strong>Etud</strong>Life</span>
                </a>
            </div>

            <nav class="header-nav" id="header-nav">
                <div class="mobile-nav-header">
                    <span class="mobile-nav-title">Menu</span>
                    <button id="close-menu-btn">&times;</button>
                </div>
                <a href="${basePath}index.html">Accueil</a>
                <a href="${basePath}Agenda.html">Agenda</a>
                <a href="${basePath}upload.html">Cours</a>
                <a href="${basePath}Bibliotheque/bibliotheque.html">Bibliothèque</a>
                <a href="${basePath}campus.html">Campus</a>
                <a href="${basePath}Recette/cuisine.html">Cuisine</a>
                <a href="${basePath}Annonce/annonces.html">Annonces</a>
                <a href="${basePath}messages.html">Messages</a>
                <a href="${basePath}proches.html">Ajouter un proche</a>
            </nav>

            <div class="header-right">
                <div class="notif-wrapper">
                    <button id="notifBtn" class="notif-btn">
                        <img src="${basePath}images/cloche-de-notification.png" alt="Notifications">
                        <span id="notifBadge" class="notif-badge hidden">0</span>
                    </button>
                    <div id="notifDropdown" class="notif-dropdown hidden">
                        <ul id="notifList" class="notif-list">
                            <li class="notif-item" style="text-align:center; color:#888;">Chargement...</li>
                        </ul>
                        <a class="notif-footer" href="${basePath}notification.html">Voir toutes les notifications</a>
                    </div>
                </div>

                <div class="account-menu">
                    <img src="${basePath}images/compte.png" class="icon-img" id="account-icon">
                    <div class="dropdown-menu" id="dropdown-menu">
                        <p class="menu-title">Invité</p>
                        <p class="menu-subtitle">Non connecté</p> 
                        <a href="${basePath}index.html?profil=true" id="btn-profil-header" class="menu-item connected-only" style="display:none;">Profil</a>
                        <a id="logout-btn-header" class="menu-item connected-only" href="#">Se déconnecter</a>
                        <a href="${basePath}login.html" class="menu-item disconnected-only">Se connecter</a>
                        <a href="${basePath}inscription.html" class="menu-item disconnected-only">Créer un compte</a>
                        <a href="#" class="menu-item">Aide</a>
                    </div>
                </div>
            </div>
        </div>
        <div class="mobile-overlay" id="mobile-overlay"></div>
    </header>`;
        document.body.insertAdjacentHTML("afterbegin", headerHTML);
    }

    // ============================================================
    // 3. LOGIQUE NAVIGATION & USER (AVEC EMAIL)
    // ============================================================
    const currentPath = window.location.pathname;
    document.querySelectorAll(".header-nav a").forEach(link => {
        link.classList.remove("active");
        const href = link.getAttribute("href");
        if (href && href !== "#" && currentPath.toLowerCase().includes(href.replace('../', '').toLowerCase())) {
            link.classList.add("active");
        }
    });

    const user = JSON.parse(localStorage.getItem("utilisateur"));
    const connectedItems = document.querySelectorAll(".connected-only");
    const disconnectedItems = document.querySelectorAll(".disconnected-only");
    const menuTitle = document.querySelector(".menu-title");
    const menuSubtitle = document.querySelector(".menu-subtitle"); // RECUPERATION DU SOUS-TITRE

    if (user) {
        connectedItems.forEach(el => el.style.display = "block");
        disconnectedItems.forEach(el => el.style.display = "none");

        // ✅ ON REMPLIT LES INFOS COMME DANS L'ANCIENNE VERSION
        if (menuTitle) menuTitle.textContent = user.prenom + " " + user.nom;
        if (menuSubtitle) menuSubtitle.textContent = user.email; // L'email s'affiche ici

        const logoutBtn = document.getElementById("logout-btn-header");
        // Force le curseur "main"
        if (logoutBtn) logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("utilisateur");
            window.location.href = `${basePath}login.html`;
        });
    } else {
        connectedItems.forEach(el => el.style.display = "none");
        disconnectedItems.forEach(el => el.style.display = "block");
    }

    // ============================================================
    // 4. 🔥 GESTION DU CLIC PROFIL (ROBUSTE)
    // ============================================================
    const accountIcon = document.getElementById("account-icon");
    const dropdownMenu = document.getElementById("dropdown-menu");
    const notifDropdown = document.getElementById("notifDropdown"); // On a besoin de ça pour fermer l'autre

    if (accountIcon && dropdownMenu) {
        accountIcon.addEventListener("click", (e) => {
            e.stopPropagation(); // Empêche de fermer immédiatement

            const isHidden = dropdownMenu.style.display === "none" || dropdownMenu.style.display === "";

            if (isHidden) {
                // 1. On ferme les notifications si elles sont ouvertes (pour pas avoir 2 menus)
                if (notifDropdown) {
                    notifDropdown.style.display = "none";
                    notifDropdown.classList.add("hidden");
                }

                // 2. On ouvre le menu profil
                dropdownMenu.style.display = "block";
            } else {
                // 3. Sinon on ferme
                dropdownMenu.style.display = "none";
            }
        });
    }

    // ============================================================
    // 5. 🔥 SYSTÈME NOTIFICATIONS (GARDÉ INTACT)
    // ============================================================
    const notifBtn = document.getElementById("notifBtn");
    const notifList = document.getElementById("notifList");
    const notifBadge = document.getElementById("notifBadge");

    // A. TRAITEMENT DE LA REQUÊTE PUT (Le "Post-it" serveur)
    const pendingNotifId = localStorage.getItem("mark_notif_read");
    if (pendingNotifId && user) {
        console.log("🔒 Envoi de la confirmation de lecture au serveur pour:", pendingNotifId);
        localStorage.removeItem("mark_notif_read");
        fetch(`/api/notifications/${pendingNotifId}/read`, {
            method: "PUT",
            headers: {'Content-Type': 'application/json'}
        }).catch(e => console.error("Erreur PUT background", e));
    }

    // B. FONCTION D'AFFICHAGE INTELLIGENTE
    async function fetchNotifications() {
        if (!user) return;
        try {
            const res = await fetch(`/api/notifications/${user.id}?t=${Date.now()}`);
            let notifs = await res.json();

            // Mémoire locale
            const readHistory = JSON.parse(localStorage.getItem("etudlife_read_history") || "[]");
            notifs.forEach(n => {
                if (readHistory.includes(n.id.toString()) || readHistory.includes(n.id)) {
                    n.isRead = true;
                }
            });

            // Badge
            const unreadCount = notifs.filter(n => !n.isRead).length;
            if (unreadCount > 0) {
                notifBadge.textContent = unreadCount > 9 ? '9+' : unreadCount;
                notifBadge.classList.remove("hidden");
                notifBadge.style.display = "flex";
            } else {
                notifBadge.classList.add("hidden");
                notifBadge.style.display = "none";
            }

            // Liste
            if (notifs.length === 0) {
                notifList.innerHTML = '<li class="notif-empty">Aucune notification</li>';
            } else {
                notifList.innerHTML = notifs.slice(0, 5).map(n => `
            <li class="notif-item ${n.isRead ? '' : 'unread'}" 
            data-id="${n.id}" data-link="${n.link || ''}" data-read="${n.isRead}">
            
            <div class="notif-content">${n.message}</div>
            <div class="notif-date">
                ${new Date(n.createdAt).toLocaleDateString()} ${new Date(n.createdAt).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                })}
            </div>
            
        </li>
    `).join("");
            }
        } catch (e) {
            console.error("Err notifs", e);
        }
    }

    // C. GESTION DU CLIC NOTIF
    if (notifList) {
        notifList.addEventListener("click", (e) => {
            const item = e.target.closest(".notif-item");
            if (!item) return;

            const notifId = item.dataset.id;
            const notifLink = item.dataset.link;
            const isRead = item.dataset.read === "true";

            if (!isRead && notifId) {
                localStorage.setItem("mark_notif_read", notifId);
                let readHistory = JSON.parse(localStorage.getItem("etudlife_read_history") || "[]");
                if (!readHistory.includes(notifId)) {
                    readHistory.push(notifId);
                    if (readHistory.length > 50) readHistory.shift();
                    localStorage.setItem("etudlife_read_history", JSON.stringify(readHistory));
                }
            }

            if (notifLink && notifLink !== "null" && notifLink !== "") {
                window.location.href = notifLink;
            } else {
                window.location.reload();
            }
        });
    }

    // D. Ouverture Menu Notif
    if (notifBtn) {
        notifBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            const isHidden = notifDropdown.style.display === "none" || notifDropdown.style.display === "";
            if (isHidden) {
                notifDropdown.style.display = "block";
                notifDropdown.classList.remove("hidden");
                // On ferme le menu profil s'il est ouvert
                if (dropdownMenu) dropdownMenu.style.display = "none";
                fetchNotifications();
            } else {
                notifDropdown.style.display = "none";
                notifDropdown.classList.add("hidden");
            }
        });
        fetchNotifications();
        setInterval(fetchNotifications, 60000);
    }

    // FERMETURE GLOBALE AU CLIC AILLEURS
    document.addEventListener("click", (e) => {
        // Fermer profil si clic dehors
        if (dropdownMenu && dropdownMenu.style.display === "block" && !dropdownMenu.contains(e.target) && !accountIcon.contains(e.target)) {
            dropdownMenu.style.display = "none";
        }
        // Fermer notifs si clic dehors
        if (notifDropdown && notifDropdown.style.display === "block" && !notifDropdown.contains(e.target) && !notifBtn.contains(e.target)) {
            notifDropdown.style.display = "none";
            notifDropdown.classList.add("hidden");
        }
    });

    // ============================================================
    // 6. 🍔 GESTION MENU MOBILE (HAMBURGER)
    // ============================================================
    const hamburgerBtn = document.getElementById("mobile-menu-btn");
    const closeMenuBtn = document.getElementById("close-menu-btn");
    const headerNav = document.getElementById("header-nav");
    const overlay = document.getElementById("mobile-overlay");

    function toggleMenu() {
        const isActive = headerNav.classList.contains("active");

        if (isActive) {
            headerNav.classList.remove("active");
            overlay.classList.remove("active");
            setTimeout(() => overlay.style.display = "none", 300); // Attendre la transition
        } else {
            overlay.style.display = "block";
            // Petit délai pour permettre la transition d'opacité CSS
            setTimeout(() => {
                headerNav.classList.add("active");
                overlay.classList.add("active");
            }, 10);
        }
    }

    if (hamburgerBtn) hamburgerBtn.addEventListener("click", toggleMenu);
    if (closeMenuBtn) closeMenuBtn.addEventListener("click", toggleMenu);
    if (overlay) overlay.addEventListener("click", toggleMenu);
});

function ajouterNotificationMenu(message) {
    const dateActuelle = new Date().toLocaleString();
    const listeNotifs = document.querySelector('.dropdown-notifications'); // Votre sélecteur de menu

    // Création de l'élément
    const nouvelleNotif = `
        <div class="notification-item">
            <p><strong>Système</strong> : ${message}</p>
            <small>${dateActuelle}</small>
        </div>
    `;

    if (listeNotifs) {
        listeNotifs.insertAdjacentHTML('afterbegin', nouvelleNotif);

        // Mise à jour du compteur (point rouge)
        const badge = document.querySelector('.notification-badge');
        if (badge) badge.style.display = 'block';
    }
}