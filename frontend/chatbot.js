// frontend/chatbot.js
(function () {
    const API_BASE = "http://localhost:8080/api/chat"; // en prod: ton domaine backend

    let sessionId = null;

    function el(tag, attrs = {}, children = []) {
        const e = document.createElement(tag);
        Object.entries(attrs).forEach(([k, v]) => {
            if (k === "class") e.className = v;
            else if (k === "style") Object.assign(e.style, v);
            else e.setAttribute(k, v);
        });
        children.forEach((c) => e.appendChild(typeof c === "string" ? document.createTextNode(c) : c));
        return e;
    }

    function addStyles() {
        if (document.getElementById("etudlife-chatbot-style")) return;
        const style = el("style", { id: "etudlife-chatbot-style" });
        style.textContent = `
#chatbot-btn{
  position:fixed; right:20px; bottom:20px; width:56px; height:56px; border-radius:50%;
  display:flex; align-items:center; justify-content:center; cursor:pointer;
  background:#24c2a0; color:#fff; font-size:22px; border:none;
  box-shadow:0 10px 25px rgba(0,0,0,.2); z-index:9999;
}
#chatbot-panel{
  position:fixed; right:20px; bottom:90px; width:340px; height:480px; background:#fff;
  border-radius:18px; box-shadow:0 10px 30px rgba(0,0,0,.25);
  display:none; flex-direction:column; overflow:hidden; z-index:9999;
}
#chatbot-header{ padding:12px 14px; display:flex; justify-content:space-between; align-items:center; background:#f7f7f7; }
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
.chat-msg.user .chat-bubble{ background:#24c2a0; color:#fff; border-bottom-right-radius:6px; }
.chat-msg.bot .chat-bubble{ background:#f1f1f1; color:#111; border-bottom-left-radius:6px; }
#chatbot-input{ display:flex; gap:10px; padding:10px; border-top:1px solid #eee; }
#chatbot-text{ flex:1; border:1px solid #ddd; border-radius:999px; padding:10px 12px; outline:none; }
#chatbot-send{ width:44px; border:none; border-radius:12px; background:#24c2a0; color:#fff; cursor:pointer; }
    `;
        document.head.appendChild(style);
    }

    function addMessage(role, text) {
        const messages = document.getElementById("chatbot-messages");
        const row = el("div", { class: `chat-msg ${role === "user" ? "user" : "bot"}` });
        const bubble = el("div", { class: "chat-bubble" }, [text]);
        row.appendChild(bubble);
        messages.appendChild(row);
        messages.scrollTop = messages.scrollHeight;
    }

    async function newSession() {
        const r = await fetch(`${API_BASE}/new-session`, { method: "POST" });
        const data = await r.json();
        sessionId = data.sessionId;
        return sessionId;
    }

    async function sendMessage(question) {
        if (!sessionId) await newSession();

        const r = await fetch(`${API_BASE}/message`, {
            method: "POST",
            headers: { "Content-Type": "application/json; charset=utf-8" },
            body: JSON.stringify({ sessionId, question }),
        });

        if (!r.ok) {
            const t = await r.text();
            addMessage("bot", `Erreur serveur: ${t}`);
            return;
        }

        const data = await r.json();
        if (data.sessionId && data.sessionId !== sessionId) sessionId = data.sessionId;

        addMessage("bot", data.response || "(vide)");
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
                    el("div", { class: "subtitle" }, ["Pose ta question (texte)"]),
                ]),
                el("button", { id: "chatbot-close", type: "button" }, ["✕"]),
            ])
        );

        panel.appendChild(el("div", { id: "chatbot-messages" }));
        panel.appendChild(
            el("div", { id: "chatbot-input" }, [
                el("input", { id: "chatbot-text", placeholder: "Écris ton message..." }),
                el("button", { id: "chatbot-send", type: "button" }, ["➤"]),
            ])
        );

        document.body.appendChild(btn);
        document.body.appendChild(panel);

  addMessage("bot", "Bonjour, Comment puis-je vous aider ?");


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

            // 🔥 Réponse locale pour les salutations
            const normalized = q.toLowerCase().trim();
            const greetings = ["bonjour", "salut", "hello", "hey", "bonsoir"];

            if (greetings.includes(normalized)) {
                addMessage("bot", "Bonjour, Comment puis-je vous aider ?");
                return; // ⛔ on n'appelle PAS l'API
            }

            // Sinon → appel normal à l'API
            await sendMessage(q);
        }


        sendBtn.addEventListener("click", onSend);
        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter") onSend();
        });

        // Effacer la session quand on quitte le site
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
