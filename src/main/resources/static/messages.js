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
    let conversationsData = [];

    // Fonction de formatage de la date pour le séparateur (Aujourd'hui, Hier, Date Complète)
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

    // Formatage de l'heure (HH:MM)
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

    // =========================================================
    // 1. CHARGEMENT ET AFFICHAGE DES CONVERSATIONS (API)
    // =========================================================

    function loadConversations() {
        if (!CURRENT_USER_ID) return;

        fetch(`/api/conversations`, {
            method: 'GET',
            headers: {
                // 🔑 Envoi de l'ID de l'utilisateur dans le header pour l'API
                'X-User-ID': CURRENT_USER_ID,
            }
        })
            .then(response => {
                if (!response.ok) {
                    // Gérer le cas où l'utilisateur n'est pas autorisé (401)
                    if (response.status === 401) throw new Error("Accès non autorisé. Vérifiez l'ID utilisateur.");
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

    // Affichage des aperçus
    function displayConversations(convs) {
        if (convs.length === 0) {
            conversationList.innerHTML = `<li>Vous n'avez aucune conversation.</li>`;
            return;
        }

        // 🔑 ÉTAPE CRITIQUE : Filtrer les doublons par conversationId
        const uniqueConversations = [];
        const seenIds = new Set();

        convs.forEach(conv => {
            // Seuls les ID non encore vus sont ajoutés
            if (!seenIds.has(conv.conversationId)) {
                seenIds.add(conv.conversationId);
                uniqueConversations.push(conv);
            }
        });
        // Note: Comme l'API trie déjà par DESC, on garde le premier message trouvé (le plus récent)

        const convsToDisplay = uniqueConversations;

        // Trié par l'API DESC, donc l'ordre est correct
        conversationList.innerHTML = convsToDisplay.map(conv => { // ⬅️ Utilise la liste filtrée
            const lastTimeFormatted = formatIsoTime(conv.lastMessageTimestamp);

            // Tronquer le contenu pour l'aperçu
            const previewContent = conv.lastMessageContent ?
                (conv.lastMessageContent.length > 30 ? conv.lastMessageContent.substring(0, 30) + '...' : conv.lastMessageContent) :
                "Nouvelle conversation";

            return `
                <li class="conversation-item" data-id="${conv.conversationId}" onclick="window.openConversation(${conv.conversationId}, '${conv.contactName}')">
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
    // 2. OUVERTURE CONVERSATION & CHARGEMENT MESSAGES (GET)
    // =========================================================

    window.openConversation = function (conversationId, conversationNom) {
        if (!CURRENT_USER_ID) {
            alert("Veuillez vous connecter pour voir les messages.");
            return;
        }

        activeConversationId = conversationId;
        activeConversationNom = conversationNom;

        chatHeaderNom.textContent = conversationNom;
        chatHeaderStatut.textContent = "Chargement...";
        messagesContainer.innerHTML = '<p class="placeholder">Chargement des messages...</p>';

        document.querySelectorAll('.conversation-item').forEach(li => {
            li.classList.remove('active');
        });
        document.querySelector(`.conversation-item[data-id="${conversationId}"]`).classList.add('active');


        fetch(`/api/conversations/${conversationId}/messages`, {
            method: 'GET',
            headers: {
                // 🔑 Envoi de l'ID de l'utilisateur dans le header
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
    // 3. AFFICHAGE D'UNE LISTE DE MESSAGES (AVEC SÉPARATEUR DE DATE)
    // =========================================================

    function displayMessages(messages) {
        if (messages.length === 0) {
            messagesContainer.innerHTML = '<p class="placeholder">Commencez la conversation !</p>';
            return;
        }

        // Tri (Sécurité) : le plus ancien en premier (ASC) - doit être fait par l'API
        messages.sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

        let messagesHTML = '';
        let lastDate = null;

        messages.forEach(msg => {
            const currentMessageDate = new Date(msg.timestamp).toLocaleDateString('fr-FR');

            // LOGIQUE DE SÉPARATEUR DE DATE
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

    // =========================================================
    // 4. GESTION DE L'ENVOI DE MESSAGE (POST)
    // =========================================================

    messageForm.addEventListener("submit", function(e) {
        e.preventDefault();

        if (!CURRENT_USER_ID || activeConversationId === null) {
            alert("Opération impossible, veuillez vous connecter et sélectionner une conversation.");
            return;
        }

        const content = messageInput.value.trim();
        if (content === "") {
            return;
        }

        const messageData = {
            // Note: senderId est transmis dans le body mais ignoré par l'API
            // (L'API utilise le X-User-ID du Header pour la sécurité)
            senderId: CURRENT_USER_ID,
            content: content
        };

        fetch(`/api/conversations/${activeConversationId}/messages`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 🔑 Envoi de l'ID de l'utilisateur dans le header
                'X-User-ID': CURRENT_USER_ID,
            },
            body: JSON.stringify(messageData),
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => {
                        throw new Error(`Erreur ${response.status}: ${err.message || 'Échec de l\'envoi'}`);
                    });
                }
                return response.json();
            })
            .then(newMessage => {
                appendNewMessage(newMessage);
                messageInput.value = "";
                messageInput.focus();

                // 🔑 Rafraîchir la liste pour mettre l'aperçu à jour
                loadConversations();
            })
            .catch(error => {
                console.error("Erreur lors de l'envoi du message:", error);
                alert("Erreur lors de l'envoi : " + error.message);
            });
    });

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
        const timeString = formatIsoTime(msg.timestamp);

        const messageHTML = `
            <div class="message ${typeClass}">
                <span class="message-content">${msg.content}</span>
                <div class="heure">${timeString}</div>
            </div>
        `;

        messagesContainer.insertAdjacentHTML('beforeend', messageHTML);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // 🔑 Démarrer l'application en chargeant les aperçus de conversation
    loadConversations();
});