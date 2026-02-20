
(function () {
    const API_BASE = "http://localhost:8080/api/chat";
    let sessionId = null;
    let mode = "AUTO";


    function el(tag, attrs = {}, children = []) {
        const e = document.createElement(tag);
        Object.entries(attrs).forEach(([k, v]) => {
            if (k === "class") e.className = v;
            else if (k === "style") Object.assign(e.style, v);
            else e.setAttribute(k, v);
        });
        children.forEach((c) =>
            e.appendChild(typeof c === "string" ? document.createTextNode(c) : c)
        );
        return e;
    }

    function addStyles() {
        if (document.getElementById("etudlife-chatbot-style")) return;

        const style = el("style", { id: "etudlife-chatbot-style" });

        style.textContent = `
:root{
  --etudlife-blue: #2f6fb2;
  --etudlife-blue-dark: #245a92;
}

#chatbot-btn{
  position:fixed; right:20px; bottom:20px; width:56px; height:56px; border-radius:50%;
  display:flex; align-items:center; justify-content:center; cursor:pointer;
  background:var(--etudlife-blue); color:#fff; font-size:22px; border:none;
  box-shadow:0 10px 25px rgba(0,0,0,.2); z-index:9999;
}#chatbot-header .subtitle{
  color:#fff;
  opacity:0.9;
}

#chatbot-btn:hover{ background:var(--etudlife-blue-dark); }

#chatbot-panel{
  position:fixed; right:20px; bottom:90px; width:340px; height:480px; background:#fff;
  border-radius:18px; box-shadow:0 10px 30px rgba(0,0,0,.25);
  display:none; flex-direction:column; overflow:hidden; z-index:9999;
}
#chatbot-header{
  padding:12px 14px;
  display:flex;
  justify-content:space-between;
  align-items:center;
  background:var(--etudlife-blue);
  color:#fff;
}

#chatbot-header .title{ font-weight:700; }
#chatbot-header .subtitle{ font-size:12px; color:#666; }
#chatbot-close{ border:none; background:transparent; cursor:pointer; font-size:16px; }
#chatbot-messages{ flex:1; padding:12px; overflow:auto; background:#fff; }
.chat-msg{ margin:8px 0; display:flex; }
.chat-msg.user{ justify-content:flex-end; }

.chat-bubble{
  max-width:80%; padding:10px 12px; border-radius:14px; font-size:14px; line-height:1.3;
  box-shadow:0 2px 8px rgba(0,0,0,.08); white-space:pre-wrap;
}
#chatbot-suggestions{
  display:flex; gap:8px; padding:10px; border-top:1px solid #eee; background:#fff;
}
#chatbot-suggestions button{
  flex:1;
  border:1px solid #ddd;
  background:#f8f8f8;
  border-radius:12px;
  padding:8px;
  cursor:pointer;
  font-size:12px;
  color:#111;              /* ← AJOUT IMPORTANT */
}

#chatbot-suggestions button.active{
  border-color: var(--etudlife-blue);
  background:#eef5ff;
  color:#1a3d6d;           /* ← couleur texte visible */
  font-weight:600;
}



.chat-msg.user .chat-bubble{ background:var(--etudlife-blue); color:#fff; border-bottom-right-radius:6px; }
.chat-msg.bot .chat-bubble{ background:#f1f1f1; color:#111; border-bottom-left-radius:6px; }

#chatbot-input{ display:flex; gap:10px; padding:10px; border-top:1px solid #eee; }
#chatbot-text{ flex:1; border:1px solid #ddd; border-radius:999px; padding:10px 12px; outline:none; }


#chatbot-send{ width:44px; border:none; border-radius:12px; background:var(--etudlife-blue); color:#fff; cursor:pointer; }
#chatbot-send:hover{ background:var(--etudlife-blue-dark); }
.chat-avatar{
  width:36px;
  height:36px;
  border-radius:50%;
  margin-right:8px;
  flex:0 0 36px;
}

.chat-msg.bot{
  align-items:flex-start;
}

.chat-msg.bot .chat-bubble{
  margin-top:2px;
}
.chatbot-msg, .message, .bot-message, .msg-bubble {
  white-space: pre-line;
}

`;
        document.head.appendChild(style);
    }

    function addMessage(role, text) {
        const messages = document.getElementById("chatbot-messages");
        const isUser = role === "user";

        const row = el("div", { class: `chat-msg ${isUser ? "user" : "bot"}` });

        // ✅ Avatar seulement pour le bot
        if (!isUser) {
            const avatar = el("img", {
                class: "chat-avatar",
                src: "images/etudlife.png",
                alt: "Bot"
            });
            row.appendChild(avatar);
        }

        const bubble = el("div", { class: "chat-bubble" }, [text]);

        row.appendChild(bubble);
        messages.appendChild(row);
        messages.scrollTop = messages.scrollHeight;
        return bubble;
    }


    function startDots(bubble) {
        let i = 0;
        bubble.textContent = "";
        const id = setInterval(() => {
            i = (i + 1) % 4;
            bubble.textContent = ".".repeat(i);
        }, 300);
        return () => clearInterval(id);
    }

    async function newSession() {
        const r = await fetch(`${API_BASE}/new-session`, { method: "POST" });
        const data = await r.json();
        sessionId = data.sessionId;
        return sessionId;
    }


    async function streamMessage(question) {
        if (!sessionId) await newSession();

        const bubble = addMessage("bot", "");
        const stopDots = startDots(bubble);

        const url = `${API_BASE}/stream?question=${encodeURIComponent(question)}&sessionId=${encodeURIComponent(sessionId)}&mode=${encodeURIComponent(mode)}`;
        const es = new EventSource(url);

        let started = false;

        es.addEventListener("chunk", (evt) => {
            if (!started) {
                stopDots();
                bubble.textContent = "";
                started = true;
            }

            if (evt.data && evt.data.startsWith("__SESSION__:")) {
                sessionId = evt.data.split(":")[1] || sessionId;
                return;
            }

            bubble.textContent += evt.data + " ";
            const messages = document.getElementById("chatbot-messages");
            messages.scrollTop = messages.scrollHeight;
        });

        es.addEventListener("done", () => {
            stopDots();
            es.close();
        });

        es.onerror = () => {
            stopDots();
            es.close();
            bubble.textContent += "\n\n(Désolé, une erreur est survenue.)";
        };
    }

    function mount() {
        addStyles();
        if (document.getElementById("chatbot-btn")) return;

        const btn = el("button", { id: "chatbot-btn", type: "button", "aria-label": "Chat" }, ["💬"]);
        const panel = el("div", { id: "chatbot-panel" });

        panel.appendChild(
            el("div", { id: "chatbot-header" }, [
                el("div", {}, [
                    el("div", { class: "title" }, ["EtudLife Assistant"]),
                ]),
                el("button", { id: "chatbot-close", type: "button" }, ["✕"]),
            ])
        );

        panel.appendChild(el("div", { id: "chatbot-messages" }));

        // ✅ 1) AJOUT : barre de choix du mode (SITE / REGLEMENT)
        panel.appendChild(
            el("div", { id: "chatbot-suggestions" }, [
                el("button", { type: "button", id: "btn-reg" }, ["📚 Règlement / examens"]),
                el("button", { type: "button", id: "btn-site" }, ["💻 Fonctionnement du site"]),
            ])
        );

        // input
        panel.appendChild(
            el("div", { id: "chatbot-input" }, [
                el("input", { id: "chatbot-text", placeholder: "Écris ton message..." }),
                el("button", { id: "chatbot-send", type: "button" }, ["➤"]),
            ])
        );

        document.body.appendChild(btn);
        document.body.appendChild(panel);

        addMessage("bot",
            "Bienvenue sur EtudLife !\n\n" +
            "EtudLife est votre plateforme étudiante pour :\n" +
            "- Gérer votre agenda et celui de vos proches\n" +
            "- Échanger via la messagerie\n" +
            "- Publier ou consulter des annonces\n" +
            "- Postuler à des offres de stage ou d’alternance\n" +
            "- Réserver des livres et des salles à la bibliothèque\n" +
            "️- Organiser votre quotidien avec des recettes étudiantes\n\n" +
            "- Je peux aussi répondre à vos questions sur le règlement intérieur et la charte de l’Université Paris Nanterre.\n\n" +
            "Que souhaitez-vous faire ?"
        );


        // ✅ 2) AJOUT : gestion des clics sur les boutons
        const btnReg = document.getElementById("btn-reg");
        const btnSite = document.getElementById("btn-site");

        function setMode(newMode) {
            mode = newMode; // "REGLEMENT" ou "SITE"

            btnReg.classList.toggle("active", mode === "REGLEMENT");
            btnSite.classList.toggle("active", mode === "SITE");

            // ✅ (optionnel) suggestions affichées
            if (mode === "REGLEMENT") {
                addMessage("bot", "vous pouvez me poser toutes les questions concernant le réglementaire de l'université Paris Nanterre : retard à un examen, fraude, plagiat, bizutage, examens en ligne.");
            } else {
                addMessage("bot", "Vous pouvez me poser toutes les questions concernant le fonctionnement du site EtudLife");
            }
        }

        btnReg.addEventListener("click", () => setMode("REGLEMENT"));
        btnSite.addEventListener("click", () => setMode("SITE"));

        // ouvrir/fermer
        btn.addEventListener("click", () => {
            panel.style.display = panel.style.display === "flex" ? "none" : "flex";
            if (panel.style.display === "flex") panel.style.flexDirection = "column";
        });

        document.getElementById("chatbot-close").addEventListener("click", () => {
            panel.style.display = "none";
        });

        const input = document.getElementById("chatbot-text");
        const sendBtn = document.getElementById("chatbot-send");

        async function onSend() {
            const q = input.value.trim();
            if (!q) return;

            input.value = "";
            addMessage("user", q);

            const normalized = q.toLowerCase().trim();
            const greetings = ["bonjour", "salut", "hello", "hey", "bonsoir"];

            if (greetings.includes(normalized)) {
                addMessage("bot", "Bonjour, comment puis-je vous aider ?");
                return;
            }

            await streamMessage(q); // ⚠️ streamMessage doit envoyer mode dans l’URL SSE (je te rappelle juste après)
        }

        sendBtn.addEventListener("click", onSend);
        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter") onSend();
        });

        window.addEventListener("beforeunload", () => {
            if (!sessionId) return;
            navigator.sendBeacon(`${API_BASE}/close?sessionId=${encodeURIComponent(sessionId)}`);
        });
    }


    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", mount);
    } else {
        mount();
    }
})();
