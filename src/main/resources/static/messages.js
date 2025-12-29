document.addEventListener("DOMContentLoaded", () => {

    const user = JSON.parse(localStorage.getItem("utilisateur"));
    const connectedItems = document.querySelectorAll(".connected-only");
    const disconnectedItems = document.querySelectorAll(".disconnected-only");

    const homepage = document.getElementById("homepage-content");
    const appContainer = document.getElementById("app-container");

    if (user) {
        // Afficher le menu connecté
        connectedItems.forEach(el => el.style.display = "block");
        disconnectedItems.forEach(el => el.style.display = "none");

        // Afficher nom + email
        document.querySelector(".menu-title").textContent = user.prenom + " " + user.nom;
        document.querySelector(".menu-subtitle").textContent = user.email;

        // ----- ⚡ BOUTON PROFIL -----
        const btnProfil = document.getElementById("btn-profil");
        btnProfil.addEventListener("click", async () => {

            homepage.style.display = "none";     // cacher accueil
            appContainer.style.display = "grid"; // montrer profil

            currentUser = user;
            // Assure-toi que la fonction afficherProfil() est définie ailleurs ou importée
            if (typeof afficherProfil === "function") {
                await afficherProfil();
            }
        });

        // ----- ❌ Déconnexion -----
        document.getElementById("logout-btn").addEventListener("click", () => {
            localStorage.removeItem("utilisateur");
            window.location.href = "index.html";
        });

    } else {
        // Afficher mode non connecté
        connectedItems.forEach(el => el.style.display = "none");
        disconnectedItems.forEach(el => el.style.display = "block");
    }
});

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

        let rawHour, rawMinute;

        try {
            // Détection : Est-ce un Nombre (Timestamp) ou une String ?
            const isNumeric = !isNaN(timestamp) && !String(timestamp).includes(":") && !String(timestamp).includes("-");

            // CAS 1 : C'est un Timestamp (Aperçu)
            if (typeof timestamp === 'number' || isNumeric) {
                let ts = Number(timestamp);
                if (String(ts).length < 12) ts = ts * 1000; // Conversion sec -> ms

                let date = new Date(ts);
                // On utilise l'heure locale (le navigateur fait UTC -> France)
                rawHour = date.getHours();
                rawMinute = date.getMinutes();
            }
            // CAS 2 : C'est du Texte (Message)
            else {
                let str = String(timestamp);
                let timePart = "";
                if (str.includes("T")) timePart = str.split("T")[1];
                else if (str.includes(" ")) timePart = str.split(" ")[1];
                else timePart = str;

                let parts = timePart.split(":");
                rawHour = parseInt(parts[0], 10);
                rawMinute = parseInt(parts[1], 10);
            }

            // UNIFORMISATION : ON AJOUTE +1 HEURE PARTOUT
            if (isNaN(rawHour)) return "--:--";

            let finalHour = (rawHour + 1) % 24;
            return `${String(finalHour).padStart(2, '0')}:${String(rawMinute).padStart(2, '0')}`;

        } catch (e) {
            console.error("Erreur formatIsoTime:", e, timestamp);
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
    // 1.1 AFFICHAGE APERÇUS
    // =========================================================
    function displayConversations(convs) {
        if (convs.length === 0) {
            conversationList.innerHTML = `<li>Vous n'avez aucune conversation.</li>`;
            return;
        }

        const uniqueConversations = [];
        const seenIds = new Set();

        convs.forEach(conv => {
            if (!seenIds.has(conv.conversationId)) {
                seenIds.add(conv.conversationId);
                uniqueConversations.push(conv);
            }
        });

        conversationList.innerHTML = uniqueConversations.map(conv => {
            const lastTimeFormatted = formatIsoTime(conv.lastMessageTimestamp);
            const previewContent = conv.lastMessageContent ?
                (conv.lastMessageContent.length > 30 ? conv.lastMessageContent.substring(0, 30) + '...' : conv.lastMessageContent) :
                "Nouvelle conversation";
            const contactIdForClick = conv.contactId || 'null';

            return `
                <li class="conversation-item" 
                    data-id="${conv.conversationId}" 
                    onclick="window.openConversation(${conv.conversationId}, '${conv.contactName}', ${contactIdForClick})"> 
                  <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=${conv.contactName ? conv.contactName.split(' ')[0] : 'default'}" />
                  <div>
                    <strong>${conv.contactName || 'Utilisateur inconnu'}</strong><br>
                    <small>${previewContent}</small>
                  </div>
                  <span class="heure">${lastTimeFormatted}</span>
                </li>
            `;
        }).join("");
    }

    // =========================================================
    // 1. CHARGEMENT CONVERSATIONS
    // =========================================================
    function loadConversations() {
        if (!CURRENT_USER_ID) return;

        fetch(`/api/conversations`, {
            method: 'GET',
            headers: { 'X-User-ID': CURRENT_USER_ID }
        })
            .then(response => {
                if (!response.ok) throw new Error("Erreur chargement");
                return response.json();
            })
            .then(data => {
                conversationsData = data;
                displayConversations(conversationsData);
            })
            .catch(error => console.error(error));
    }

    // =========================================================
    // 2. OUVERTURE CONVERSATION
    // =========================================================
    window.openConversation = function (conversationId, conversationNom, contactId) {
        if (!CURRENT_USER_ID) {
            alert("Veuillez vous connecter.");
            return;
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
    // 4. ENVOI MESSAGE
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