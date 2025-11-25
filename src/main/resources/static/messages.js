document.addEventListener("DOMContentLoaded", () => {
    const conversationList = document.getElementById("conversationList");
    const messagesContainer = document.getElementById("messagesContainer");
    const chatHeader = document.querySelector(".contact-nom");

    // 🔹 Conversations fictives
    const conversations = [
        {id: 1, nom: "Lyna Baouche", message: "Salut ! As-tu pu regarder le rapport que j’ai envoyé ?", heure: "14:30"},
        {id: 2, nom: "Alicya-Pearl Marras", message: "Parfait pour la présentation de demain !", heure: "13:45"},
        {id: 3, nom: "Dyhia Sellah", message: "Les tests API sont prêts, on peut commencer.", heure: "12:20"},
        {id: 4, nom: "Thomas Martin", message: "Merci pour les documents partagés !", heure: "11:15"},
        {id: 5, nom: "Sarah Dubois", message: "On se retrouve à la bibliothèque ?", heure: "10:30"},
    ];

    // 🔹 Messages fictifs pour la conversation principale
    const messages = [
        {
            auteur: "Lyna Baouche",
            contenu: "Salut ! J’espère que tu vas bien. J’ai terminé la première partie du rapport.",
            heure: "14:25",
            type: "received"
        },
        {
            auteur: "Moi",
            contenu: "Parfait ! Je vais le regarder tout de suite. Merci pour ton travail.",
            heure: "14:27",
            type: "sent"
        },
        {
            auteur: "Lyna Baouche",
            contenu: "As-tu pu regarder le rapport que j’ai envoyé ? J’aimerais avoir ton avis avant la réunion de demain.",
            heure: "14:30",
            type: "received"
        },
    ];

    // 📋 Afficher les conversations
    conversationList.innerHTML = conversations.map(conv => `
    <li onclick="openConversation(${conv.id})">
      <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=${conv.nom.split(' ')[0]}" />
      <div>
        <strong>${conv.nom}</strong><br>
        <small>${conv.message}</small>
      </div>
      <span class="heure">${conv.heure}</span>
    </li>
  `).join("");

    // Fonction d'ouverture d'une conversation (fictive)
    window.openConversation = function (id) {
        const conv = conversations.find(c => c.id === id);
        chatHeader.textContent = conv.nom;
        messagesContainer.innerHTML = messages.map(msg => `
      <div class="message ${msg.type}">
        ${msg.contenu}
        <div class="heure">${msg.heure}</div>
      </div>
    `).join("");
    };
});
