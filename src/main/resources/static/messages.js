document.addEventListener("DOMContentLoaded", () => {
    const conversationList = document.getElementById("conversationList");
    const messagesContainer = document.getElementById("messagesContainer");
    const chatHeaderNom = document.querySelector(".contact-nom");
    const chatHeaderStatut = document.getElementById("contactStatut");
    const messageForm = document.getElementById("messageForm");
    const messageInput = document.getElementById("messageInput");

    // --- VARIABLES POUR LE MENU CONTEXTUEL (CLIC DROIT) ---
    const contextMenu = document.getElementById("customContextMenu");
    const btnDeleteMessage = document.getElementById("btnDeleteMessage");
    let messageIdToDelete = null;

    const mainContainer = document.querySelector('.messagerie');
    const btnBack = document.getElementById('btnBackToConv');

    // =========================================================
    // 🔑 GESTION DE L'UTILISATEUR ACTUEL
    // =========================================================
    const user = JSON.parse(localStorage.getItem("utilisateur"));
    let CURRENT_USER_ID = null;

    if (user && user.id) {
        CURRENT_USER_ID = user.id;
        console.log("Utilisateur connecté ID:", CURRENT_USER_ID);
    } else {
        console.warn("Aucun utilisateur connecté trouvé.");
        chatHeaderStatut.textContent = "Déconnecté. Veuillez vous connecter.";
        messageInput.disabled = true;
    }

    // =========================================================
    // 🌍 ÉTAT DE L'APPLICATION
    // =========================================================

    let activeConversationId = null;
    let activeConversationNom = null;
    let activeContactId = null;
    let conversationsData = [];
    let statusInterval = null; // Timer pour le statut en ligne

    // =========================================================
    // 🛠️ FONCTIONS UTILITAIRES (HEURE & DATE)
    // =========================================================

    function formatDateSeparator(isoTimestamp) {
        if (!isoTimestamp) return "Date Inconnue";
        const now = new Date();
        const messageDate = new Date(isoTimestamp);
        const startOfDay = (date) => {
            const d = new Date(date);
            d.setHours(0, 0, 0, 0);
            return d.getTime();
        };
        const diffDays = Math.round((startOfDay(now) - startOfDay(messageDate)) / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return "Aujourd'hui";
        if (diffDays === 1) return "Hier";
        return messageDate.toLocaleDateString('fr-FR', {
            year: 'numeric', month: 'long', day: 'numeric'
        });
    }

    function formatIsoTime(timestamp) {
        if (!timestamp) return "";

        try {
            // Le timestamp arrive sous forme de string ISO : "2025-12-30T21:52:30"
            // Ou parfois sous forme de tableau : [2025, 12, 30, 21, 52]

            let str = String(timestamp);
            let timePart = "";

            // CAS 1 : C'est déjà une chaine propre (ex: "2025...T21:52...")
            if (str.includes("T")) {
                timePart = str.split("T")[1]; // On prend ce qu'il y a après le T
            }
            // CAS 2 : Format SQL avec espace (ex: "2025... 21:52...")
            else if (str.includes(" ")) {
                timePart = str.split(" ")[1];
            }
            // CAS 3 : C'est un tableau (Parfois Spring Boot sérialise comme ça)
            else if (Array.isArray(timestamp) && timestamp.length >= 5) {
                // On construit "HH:MM" manuellement
                let h = String(timestamp[3]).padStart(2, '0');
                let m = String(timestamp[4]).padStart(2, '0');
                return `${h}:${m}`;
            }
            // CAS 4 : Cas de secours, on essaie de lire directement
            else {
                timePart = str;
            }

            // À ce stade, timePart ressemble à "21:52:30.456" ou "21:52"
            // On coupe pour ne garder que les 5 premiers caractères "HH:mm"
            if (timePart.includes(":")) {
                return timePart.substring(0, 5);
            }

            return "--:--";

        } catch (e) {
            console.error("Erreur lecture heure:", e, timestamp);
            return "--:--";
        }
    }

    // =========================================================
    // 🗑️ GESTION DU CLIC DROIT (SUPPRESSION)
    // =========================================================

    // Fermer le menu si on clique ailleurs
    document.addEventListener("click", () => {
        if (contextMenu) contextMenu.style.display = "none";
    });

    // Action du bouton supprimer
    if (btnDeleteMessage) {
        btnDeleteMessage.addEventListener("click", () => {
            if (messageIdToDelete) deleteMessage(messageIdToDelete);
        });
    }

    function deleteMessage(messageId) {
        if(!confirm("Supprimer ce message ?")) return;

        fetch(`/api/messages/${messageId}`, {
            method: 'DELETE',
            headers: { 'X-User-ID': CURRENT_USER_ID }
        })
            .then(response => {
                if (response.ok) {
                    // Suppression visuelle immédiate
                    const msgElement = document.querySelector(`.message[data-msg-id="${messageId}"]`);
                    if (msgElement) msgElement.remove();

                    // Mettre à jour l'aperçu si c'était le dernier message (optionnel, reload simple ici)
                    loadConversations();
                } else {
                    alert("Erreur lors de la suppression.");
                }
            })
            .catch(err => console.error(err));
    }

    // Fonction pour attacher l'événement clic droit de façon intelligente
    function attachContextMenu(msgElement, messageId) {
        msgElement.addEventListener("contextmenu", (e) => {
            e.preventDefault(); // Bloque le menu natif du navigateur

            if (contextMenu) {
                // 1. On affiche le menu pour que JS puisse calculer sa taille
                contextMenu.style.display = "block";
                messageIdToDelete = messageId;

                // 2. Calculs de positionnement (Pour ne pas sortir de l'écran)
                const menuWidth = contextMenu.offsetWidth;
                const windowWidth = window.innerWidth;

                // Position de la souris
                let posX = e.pageX;
                let posY = e.pageY;

                // 🚨 LE FIX : Si le menu dépasse à droite de l'écran...
                if (posX + menuWidth > windowWidth) {
                    // ... on le positionne à GAUCHE de la souris
                    posX = posX - menuWidth;
                }

                // Application des positions calculées
                contextMenu.style.top = `${posY}px`;
                contextMenu.style.left = `${posX}px`;
            }
        });
    }

    // =========================================================
    // 🟢 GESTION STATUT DYNAMIQUE (POLLING)
    // =========================================================

    function startStatusPolling(contactId) {
        if (statusInterval) clearInterval(statusInterval);

        const checkStatus = () => {
            // 👇 MODIFICATION ICI : remplace 'users' par 'comptes'
            fetch(`/api/comptes/${contactId}/status`)
                .then(res => res.json())
                .then(data => {
                    const isOnline = data.online === true || data.status === "ONLINE";
                    if (isOnline) {
                        chatHeaderStatut.textContent = "En ligne 🟢";
                        chatHeaderStatut.style.color = "#2ecc71";
                    } else {
                        chatHeaderStatut.textContent = "Hors ligne";
                        chatHeaderStatut.style.color = "#95a5a6";
                    }
                })
                .catch(() => chatHeaderStatut.textContent = "");
        };

        checkStatus();
        statusInterval = setInterval(checkStatus, 5000);
    }

    // =========================================================
    // 3. AFFICHAGE DES MESSAGES
    // =========================================================


    // =========================================================
    // 🔙 GESTION DU BOUTON RETOUR (MOBILE)
    // =========================================================
    if (btnBack) {
        btnBack.addEventListener('click', () => {
            // On retire la classe qui affiche le chat
            mainContainer.classList.remove('mobile-active');

            // Optionnel : On désactive la sélection visuelle dans la liste
            document.querySelectorAll('.conversation-item').forEach(li => li.classList.remove('active'));

            // On arrête le polling pour économiser la batterie
            if (statusInterval) clearInterval(statusInterval);
        });
    }

    function displayMessages(messages) {
        messagesContainer.innerHTML = ''; // Reset

        if (messages.length === 0) {
            messagesContainer.innerHTML = '<p class="placeholder">Commencez la conversation !</p>';
            return;
        }

        // Tri robuste
        messages.sort((a, b) => {
            const dateA = new Date(String(a.timestamp).replace(" ", "T"));
            const dateB = new Date(String(b.timestamp).replace(" ", "T"));
            return dateA - dateB;
        });

        let lastDate = null;

        messages.forEach(msg => {
            // Séparateur de date
            const currentMessageDate = new Date(msg.timestamp).toLocaleDateString('fr-FR');
            if (currentMessageDate !== lastDate) {
                const dateLabel = formatDateSeparator(msg.timestamp);
                messagesContainer.insertAdjacentHTML('beforeend', `<div class="date-separator"><span>${dateLabel}</span></div>`);
                lastDate = currentMessageDate;
            }

            const isSent = msg.senderId === CURRENT_USER_ID;
            const typeClass = isSent ? "sent" : "received";
            const timeString = formatIsoTime(msg.timestamp);

            // Création élément message
            const msgDiv = document.createElement("div");
            msgDiv.className = `message ${typeClass}`;
            msgDiv.setAttribute("data-msg-id", msg.id); // Important pour la suppression

            msgDiv.innerHTML = `
                <span class="message-content">${msg.content}</span>
                <div class="heure">${timeString}</div>
            `;

            // ⚠️ C'EST ICI QUE LA MAGIE OPÈRE
            // On ajoute le menu contextuel UNIQUEMENT si c'est un message envoyé (isSent)
            if (isSent) {
                // Si le message a un ID, on active le clic droit
                if (msg.id) {
                    attachContextMenu(msgDiv, msg.id);
                }
            }
            // Si c'est un message reçu (else), on ne fait rien, donc pas de clic droit possible.

            messagesContainer.appendChild(msgDiv);
        });

        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // =========================================================
    // 5. AJOUTER UN MESSAGE (UI APRES ENVOI)
    // =========================================================
    function appendNewMessage(msg) {
        const placeholder = messagesContainer.querySelector('.placeholder');
        if (placeholder) placeholder.remove();

        const isSent = msg.senderId === CURRENT_USER_ID;
        const typeClass = isSent ? "sent" : "received";

        let timeString;
        if (msg.timestamp) {
            timeString = formatIsoTime(msg.timestamp);
        } else {
            // Fallback immédiat, on simule le formatage +1h aussi pour être cohérent
            const d = new Date();
            d.setHours(d.getHours() + 1);
            timeString = d.toLocaleTimeString('fr-FR', {hour: '2-digit', minute:'2-digit'});
        }

        const msgDiv = document.createElement("div");
        msgDiv.className = `message ${typeClass}`;
        // Si le backend renvoie l'ID du nouveau message, on le met. Sinon on met null.
        const newMsgId = msg.id || null;
        if(newMsgId) msgDiv.setAttribute("data-msg-id", newMsgId);

        msgDiv.innerHTML = `
            <span class="message-content">${msg.content}</span>
            <div class="heure">${timeString}</div>
        `;

        // Attacher le clic droit sur le nouveau message
        if (isSent && newMsgId) {
            attachContextMenu(msgDiv, newMsgId);
        }

        messagesContainer.appendChild(msgDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // =========================================================
    // 0. AFFICHAGE LISTE (Modifié pour gérer le clic "Nouveau")
    // =========================================================
    function displayConversations(convs) {
        if (convs.length === 0) {
            conversationList.innerHTML = `<li>Ajoutez des proches pour discuter !</li>`;
            return;
        }

        // On trie : d'abord les vraies convs par date, puis les nouvelles
        convs.sort((a, b) => {
            if (!a.lastMessageTimestamp) return -1; // Les nouvelles en haut (ou en bas selon pref)
            if (!b.lastMessageTimestamp) return 1;
            // Tri décroissant sur le String ISO
            return b.lastMessageTimestamp.localeCompare(a.lastMessageTimestamp);
        });

        conversationList.innerHTML = convs.map(conv => {
            const isNew = conv.conversationId === "new";
            const timeDisplay = isNew ? "" : formatIsoTime(conv.lastMessageTimestamp);

            // On passe 'new' ou l'ID réel dans le onclick
            // Attention aux guillemets pour les strings dans le HTML
            const convIdParam = isNew ? "'new'" : conv.conversationId;

            return `
                <li class="conversation-item" 
                    data-id="${conv.conversationId}" 
                    onclick="window.initAndOpenChat(${convIdParam}, ${conv.contactId}, '${conv.contactName.replace(/'/g, "\\'")}')"> 
                  <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=${conv.contactName.split(' ')[0]}" />
                  <div>
                    <strong>${conv.contactName}</strong>
                    <small>${conv.lastMessageContent}</small>
                  </div>
                  <span class="heure">${timeDisplay}</span>
                </li>
            `;
        }).join("");
    }

    // =========================================================
    // 1. CHARGEMENT CONVERSATIONS + PROCHES (FUSION)
    // =========================================================
    async function loadConversations() {
        if (!CURRENT_USER_ID) return;

        try {
            // 1. Récupérer les conversations existantes (Historique)
            const resConvs = await fetch(`/api/conversations`, {
                headers: { 'X-User-ID': CURRENT_USER_ID }
            });
            const existingConversations = await resConvs.json();

            // 2. Récupérer la liste des proches (Amis)
            const resProches = await fetch(`/api/liens/${CURRENT_USER_ID}/proches`);
            const liensProches = await resProches.json();

            // 3. FUSIONNER LES DEUX LISTES
            // On crée une Map pour accès rapide aux convs existantes par ID de contact
            const convMap = new Map();
            existingConversations.forEach(c => convMap.set(c.contactId, c));

            const finalList = [...existingConversations];

            // Pour chaque proche, s'il n'est PAS dans les conversations, on l'ajoute
            liensProches.forEach(lien => {
                // Dans un lien, la cible est l'ami (si je suis la source)
                const ami = lien.compteCible;

                if (!convMap.has(ami.id)) {
                    // Création d'une "fausse" conversation pour l'affichage
                    finalList.push({
                        conversationId: "new", // Marqueur spécial
                        contactId: ami.id,
                        contactName: ami.prenom + " " + ami.nom,
                        lastMessageContent: "Nouvelle discussion",
                        lastMessageTimestamp: null // Pas de date
                    });
                }
            });

            conversationsData = finalList;
            displayConversations(conversationsData);

        } catch (error) {
            console.error("Erreur chargement global:", error);
        }
    }


    // =========================================================
    // 2. INITIALISER ET OUVRIR (Nouvelle logique Clic)
    // =========================================================
    window.initAndOpenChat = async function(convIdOrTag, contactId, contactName) {

        let finalConversationId = convIdOrTag;

        // Si c'est une nouvelle conversation, on demande un ID au serveur
        if (convIdOrTag === "new") {
            try {
                chatHeaderStatut.textContent = "Initialisation...";
                const res = await fetch(`/api/conversations/init/${contactId}`, {
                    headers: { 'X-User-ID': CURRENT_USER_ID }
                });
                finalConversationId = await res.json(); // Le serveur renvoie un Long (ID)

                // On met à jour l'ID dans la liste locale pour ne plus refaire l'init
                const item = conversationsData.find(c => c.contactId === contactId);
                if(item) item.conversationId = finalConversationId;

            } catch (e) {
                console.error("Erreur init conv:", e);
                alert("Impossible de créer la conversation");
                return;
            }
        }

        // Une fois qu'on a le vrai ID, on ouvre normalement
        openConversation(finalConversationId, contactName, contactId);
    };

    // =========================================================
    // 3. OUVERTURE CONVERSATION
    // =========================================================
    window.openConversation = function (conversationId, conversationNom, contactId) {
        if (!CURRENT_USER_ID) {
            alert("Veuillez vous connecter.");
            return;
        }

        if (mainContainer) {
            mainContainer.classList.add('mobile-active');
        }

        activeConversationId = conversationId;
        activeConversationNom = conversationNom;
        activeContactId = contactId;

        chatHeaderNom.textContent = conversationNom;
        chatHeaderStatut.textContent = "Chargement...";

        // Gestion UI Active
        document.querySelectorAll('.conversation-item').forEach(li => li.classList.remove('active'));
        const selectedItem = document.querySelector(`.conversation-item[data-id="${conversationId}"]`);
        if (selectedItem) selectedItem.classList.add('active');

        // 🔥 LANCEMENT DU STATUT DYNAMIQUE
        if (contactId) {
            startStatusPolling(contactId);
        } else {
            if(statusInterval) clearInterval(statusInterval);
            chatHeaderStatut.textContent = "";
        }

        // Chargement Messages
        fetch(`/api/conversations/${conversationId}/messages`, {
            method: 'GET',
            headers: { 'X-User-ID': CURRENT_USER_ID }
        })
            .then(response => {
                if (!response.ok) throw new Error(`Erreur HTTP: ${response.status}`);
                return response.json();
            })
            .then(messages => {
                displayMessages(messages);
                // On laisse le statut dynamique gérer le texte, ou on met une valeur par défaut
                if(!contactId) chatHeaderStatut.textContent = "En ligne";
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            })
            .catch(error => {
                console.error(error);
                chatHeaderStatut.textContent = "Erreur";
                messagesContainer.innerHTML = `<p class="placeholder error">Erreur chargement.</p>`;
            });
    };

    // =========================================================
    // 5. ENVOI MESSAGE
    // =========================================================

    messageInput.addEventListener("keyup", function(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            messageForm.dispatchEvent(new Event('submit', { cancelable: true }));
        }
    });

    messageForm.addEventListener("submit", function(e) {
        e.preventDefault();

        if (!CURRENT_USER_ID || !activeConversationId || !activeContactId) {
            alert("Erreur destinataire ou connexion.");
            return;
        }

        const content = messageInput.value.trim();
        if (content === "") return;

        const messageData = {
            senderId: CURRENT_USER_ID,
            receiverId: activeContactId,
            content: content
        };

        fetch(`/api/conversations/${activeConversationId}/messages`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-ID': CURRENT_USER_ID,
            },
            body: JSON.stringify(messageData),
        })
            .then(response => {
                if (!response.ok) throw new Error("Erreur envoi");
                const contentType = response.headers.get("content-type");
                if (response.status === 201 && contentType && contentType.includes("application/json")) {
                    return response.json();
                }
                return {};
            })
            .then(newMessage => {
                // Si l'API renvoie le message complet, on l'utilise, sinon on utilise les données locales
                const messageToDisplay = Object.keys(newMessage).length > 0 ? newMessage : messageData;

                appendNewMessage(messageToDisplay);
                messageInput.value = "";
                messageInput.focus();
                loadConversations(); // Mettre à jour l'aperçu
            })
            .catch(error => {
                console.error(error);
                alert("Erreur envoi.");
            });
    });

    // =========================================================
    // ❤️ HEARTBEAT : JE DIS AU SERVEUR QUE JE SUIS LA
    // =========================================================
    // ❤️ HEARTBEAT INTELLIGENT
    function startMyHeartbeat() {
        if (!CURRENT_USER_ID) return;

        let heartbeatInterval = null;

        const sendPing = () => {
            // Sécurité : Si l'onglet est caché, on n'envoie rien
            if (document.visibilityState === 'hidden') return;

            fetch('/api/comptes/ping', {
                method: 'POST',
                headers: { 'X-User-ID': CURRENT_USER_ID }
            }).catch(e => console.error("Ping failed", e)); // Pas grave si ça fail
        };

        // 1. Démarrer le cycle
        const start = () => {
            if (heartbeatInterval) clearInterval(heartbeatInterval);
            sendPing();
            heartbeatInterval = setInterval(sendPing, 60000); // Toutes les minutes
        };

        // 2. Arrêter le cycle
        const stop = () => {
            if (heartbeatInterval) clearInterval(heartbeatInterval);
        };

        // 3. Écouter si l'utilisateur quitte ou revient sur l'onglet
        document.addEventListener("visibilitychange", () => {
            if (document.visibilityState === 'visible') {
                console.log("Bon retour ! Reprise du heartbeat.");
                start(); // On relance quand il revient
            } else {
                console.log("Onglet caché. Pause du heartbeat.");
                stop(); // On met en pause pour économiser
            }
        });

        // Lancement initial
        start();
    }

    // Démarrage
    loadConversations();
});