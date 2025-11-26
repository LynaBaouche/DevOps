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
            await afficherProfil(); // 🔥 charge groupes, proches, posts, etc.
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

    // =========================================================
    // 🔑 GESTION DE L'UTILISATEUR ACTUEL (DYNAMIQUE)
    // =========================================================
    // ... (Logique utilisateur inchangée) ...
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
    // 🌍 ÉTAT DE L'APPLICATION ET OUTILS
    // =========================================================

    let activeConversationId = null;
    let activeConversationNom = null;
    let activeContactId = null;
    let conversationsData = [];

    // --- Fonctions d'aide (Utilitaire) ---

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

        if (diffDays === 0) {
            return "Aujourd'hui";
        } else if (diffDays === 1) {
            return "Hier";
        } else {
            return messageDate.toLocaleDateString('fr-FR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }
    }

    function formatIsoTime(isoTimestamp) {
        if (typeof isoTimestamp !== 'string' || !isoTimestamp) {
            return "Maintenant";
        }

        try {
            const date = new Date(isoTimestamp);

            if (isNaN(date.getTime())) {
                throw new Error("Date invalide après parsing.");
            }

            const hours = date.getHours();
            const minutes = String(date.getMinutes()).padStart(2, '0');

            return `${hours}:${minutes}`;

        } catch (e) {
            console.error("Erreur de formatage de date:", isoTimestamp, e.message);
            return "Erreur Heure";
        }
    }

    // --- Fonction d'affichage UI (Doit être définie tôt) ---

    // =========================================================
    // 3. AFFICHAGE DES MESSAGES DANS LE CHAT
    // =========================================================
    function displayMessages(messages) {
        if (messages.length === 0) {
            messagesContainer.innerHTML = '<p class="placeholder">Commencez la conversation !</p>';
            return;
        }

        messages.sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

        let messagesHTML = '';
        let lastDate = null;

        messages.forEach(msg => {
            const currentMessageDate = new Date(msg.timestamp).toLocaleDateString('fr-FR');

            if (currentMessageDate !== lastDate) {
                const dateLabel = formatDateSeparator(msg.timestamp);

                messagesHTML += `<div class="date-separator"><span>${dateLabel}</span></div>`;

                lastDate = currentMessageDate;
            }

            const isSent = msg.senderId === CURRENT_USER_ID;
            const typeClass = isSent ? "sent" : "received";
            const timeString = formatIsoTime(msg.timestamp);

            messagesHTML += `
                <div class="message ${typeClass}">
                    <span class="message-content">${msg.content}</span>
                    <div class="heure">${timeString}</div>
                </div>
            `;
        });

        messagesContainer.innerHTML = messagesHTML;
    }

    // --- Fonctions d'état (Logique) ---

    // =========================================================
    // 5. AJOUTER UN MESSAGE (APRÈS ENVOI) À L'UI
    // =========================================================

    function appendNewMessage(msg) {
        const placeholder = messagesContainer.querySelector('.placeholder');
        if (placeholder) {
            placeholder.remove();
        }

        const isSent = msg.senderId === CURRENT_USER_ID;
        const typeClass = isSent ? "sent" : "received";
        const timeString = msg.timestamp ? formatIsoTime(msg.timestamp) : formatIsoTime(new Date().toISOString());

        const messageHTML = `
            <div class="message ${typeClass}">
                <span class="message-content">${msg.content}</span>
                <div class="heure">${timeString}</div>
            </div>
        `;

        messagesContainer.insertAdjacentHTML('beforeend', messageHTML);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // =========================================================
    // 1.1 AFFICHAGE DES APERÇUS
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

        const convsToDisplay = uniqueConversations;

        conversationList.innerHTML = convsToDisplay.map(conv => {
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
    // 1. CHARGEMENT DES CONVERSATIONS (API)
    // =========================================================

    function loadConversations() {
        if (!CURRENT_USER_ID) return;

        fetch(`/api/conversations`, {
            method: 'GET',
            headers: {
                'X-User-ID': CURRENT_USER_ID,
            }
        })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 401) throw new Error("Accès non autorisé.");
                    throw new Error(`Erreur HTTP: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                conversationsData = data;
                displayConversations(conversationsData);
            })
            .catch(error => {
                console.error("Erreur API Conversations:", error);
                conversationList.innerHTML = `<li>Erreur de chargement des conversations: ${error.message}</li>`;
            });
    }

    // =========================================================
    // 2. OUVERTURE CONVERSATION (FONCTION GLOBALE ACCESSIBLE PAR ONCLICK)
    // =========================================================

    window.openConversation = function (conversationId, conversationNom, contactId) {
        if (!CURRENT_USER_ID) {
            alert("Veuillez vous connecter pour voir les messages.");
            return;
        }

        activeConversationId = conversationId;
        activeConversationNom = conversationNom;
        activeContactId = contactId;

        chatHeaderNom.textContent = conversationNom;
        chatHeaderStatut.textContent = "Chargement...";
        messagesContainer.innerHTML = '<p class="placeholder">Chargement des messages...</p>';

        document.querySelectorAll('.conversation-item').forEach(li => {
            li.classList.remove('active');
        });
        const selectedItem = document.querySelector(`.conversation-item[data-id="${conversationId}"]`);
        if (selectedItem) {
            selectedItem.classList.add('active');
        }


        fetch(`/api/conversations/${conversationId}/messages`, {
            method: 'GET',
            headers: {
                'X-User-ID': CURRENT_USER_ID,
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP: ${response.status}`);
                }
                return response.json();
            })
            .then(messages => {
                displayMessages(messages);
                chatHeaderStatut.textContent = "En ligne";
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            })
            .catch(error => {
                console.error("Erreur lors du chargement des messages:", error);
                chatHeaderStatut.textContent = "Erreur de chargement";
                messagesContainer.innerHTML = `<p class="placeholder error">Impossible de charger les messages: ${error.message}.</p>`;
            });
    };

    // =========================================================
    // 4. GESTION DE L'ENVOI DE MESSAGE (POST)
    // =========================================================

    // 🔑 NOUVEL ÉCOUTEUR D'ÉVÉNEMENT POUR LA TOUCHE ENTRÉE
    messageInput.addEventListener("keyup", function(event) {
        // Soumet le formulaire si la touche est 'Enter' (code 13 ou 'key')
        // mais pas si la touche Shift est également enfoncée (pour les sauts de ligne)
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault(); // Empêche l'ajout d'une nouvelle ligne dans le textarea
            messageForm.dispatchEvent(new Event('submit', { cancelable: true }));
        }
    });


    messageForm.addEventListener("submit", function(e) {
        e.preventDefault();

        if (!CURRENT_USER_ID || activeConversationId === null) {
            alert("Opération impossible, veuillez vous connecter et sélectionner une conversation.");
            return;
        }

        if (activeContactId === null || activeContactId === undefined) {
            alert("Erreur: ID du destinataire inconnu. Cliquez sur une conversation.");
            return;
        }

        // ⚠️ Nettoyage : On remplace les sauts de ligne créés par le textarea par des espaces (ou les laisser, selon le besoin de l'API)
        // Mais il est important de nettoyer les espaces avant/après.
        const content = messageInput.value.trim();
        if (content === "") {
            return;
        }

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
                if (!response.ok) {
                    return response.json().then(err => {
                        throw new Error(`Erreur ${response.status}: ${err.message || JSON.stringify(err)}`);
                    }).catch(() => {
                        throw new Error(`Erreur HTTP ${response.status}. Vérifiez les logs API.`);
                    });
                }

                const contentType = response.headers.get("content-type");
                if (response.status === 201 && contentType && contentType.includes("application/json")) {
                    return response.json();
                } else {
                    return {};
                }
            })
            .then(newMessage => {
                const messageToDisplay = Object.keys(newMessage).length > 0 ? newMessage : messageData;

                appendNewMessage(messageToDisplay);
                messageInput.value = "";
                messageInput.focus();

                loadConversations();
            })
            .catch(error => {
                console.error("Erreur lors de l'envoi du message:", error);
                alert("Erreur lors de l'envoi : " + error.message);
            });
    });

    // 🔑 Démarrer l'application en chargeant les aperçus de conversation
    loadConversations();
});