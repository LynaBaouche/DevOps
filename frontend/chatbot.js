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
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

:root {
  --el-primary: #1a56db;
  --el-primary-light: #3b76f6;
  --el-primary-dark: #1341b0;
  --el-bg: #f8faff;
  --el-surface: #ffffff;
  --el-border: #e2e8f8;
  --el-text: #0f172a;
  --el-text-muted: #64748b;
  --el-user-bubble: linear-gradient(135deg, #1a56db, #3b76f6);
  --el-shadow: 0 20px 60px rgba(26,86,219,0.15);
  --el-radius: 20px;
  --el-font: 'Plus Jakarta Sans', sans-serif;
}

#chatbot-btn {
  position: fixed; right: 24px; bottom: 24px;
  width: 60px; height: 60px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; background: var(--el-user-bubble);
  color: #fff; font-size: 24px; border: none;
  box-shadow: 0 8px 32px rgba(26,86,219,0.4); z-index: 9999;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
#chatbot-btn:hover { transform: scale(1.08) translateY(-2px); box-shadow: 0 12px 40px rgba(26,86,219,0.5); }
#chatbot-btn:active { transform: scale(0.96); }

#chatbot-panel {
  position: fixed; right: 24px; bottom: 100px;
  width: 360px; height: 540px;
  background: var(--el-surface); border-radius: var(--el-radius);
  box-shadow: var(--el-shadow); display: none; flex-direction: column;
  overflow: hidden; z-index: 9999; font-family: var(--el-font);
  border: 1px solid var(--el-border);
  animation: slideUp 0.3s cubic-bezier(0.34,1.56,0.64,1);
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

#chatbot-header {
  padding: 16px 18px; display: flex;
  justify-content: space-between; align-items: center;
  background: var(--el-user-bubble); color: #fff; flex-shrink: 0;
}
.chatbot-header-info { display: flex; align-items: center; gap: 10px; }
.chatbot-header-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; flex-shrink: 0;
}
.chatbot-header-text .title { font-weight: 700; font-size: 15px; letter-spacing: -0.2px; }
.chatbot-header-text .subtitle {
  font-size: 11px; opacity: 0.8; margin-top: 1px;
  display: flex; align-items: center; gap: 4px;
}
.chatbot-online-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #4ade80; box-shadow: 0 0 6px #4ade80;
  display: inline-block; animation: pulse-dot 2s infinite;
}
@keyframes pulse-dot { 0%,100% { opacity:1; } 50% { opacity:0.4; } }

#chatbot-close {
  border: none; background: rgba(255,255,255,0.15);
  cursor: pointer; font-size: 14px; color: #fff;
  width: 30px; height: 30px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}
#chatbot-close:hover { background: rgba(255,255,255,0.3); }

#chatbot-messages {
  flex: 1; padding: 16px; overflow-y: auto;
  background: var(--el-bg); display: flex;
  flex-direction: column; gap: 4px; scroll-behavior: smooth;
}
#chatbot-messages::-webkit-scrollbar { width: 4px; }
#chatbot-messages::-webkit-scrollbar-track { background: transparent; }
#chatbot-messages::-webkit-scrollbar-thumb { background: var(--el-border); border-radius: 99px; }

.chat-msg { display: flex; align-items: flex-end; gap: 8px; margin: 2px 0; }
.chat-msg.user { justify-content: flex-end; }
.chat-msg.bot  { justify-content: flex-start; }

.chat-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  flex-shrink: 0; object-fit: cover;
  border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.chat-bubble {
  max-width: 78%; padding: 10px 14px; border-radius: 18px;
  font-size: 13.5px; line-height: 1.5; white-space: pre-wrap;
  word-break: break-word; font-family: var(--el-font);
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}
.chat-msg.user .chat-bubble {
  background: var(--el-user-bubble); color: #fff;
  border-bottom-right-radius: 5px;
  box-shadow: 0 4px 16px rgba(26,86,219,0.25);
}
.chat-msg.bot .chat-bubble {
  background: var(--el-surface); color: var(--el-text);
  border-bottom-left-radius: 5px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1px solid var(--el-border);
}

.job-cards-intro {
  font-weight: 700; font-size: 13px; color: var(--el-primary);
  margin-bottom: 10px; display: flex; align-items: center; gap: 6px;
}
.job-card {
  display: flex; align-items: center; justify-content: space-between;
  background: var(--el-bg); border: 1px solid var(--el-border);
  border-radius: 12px; padding: 10px 12px; margin-bottom: 7px;
  cursor: pointer; transition: all 0.18s ease;
}
.job-card:hover {
  background: #eff6ff; border-color: var(--el-primary-light);
  transform: translateX(3px); box-shadow: 0 4px 16px rgba(26,86,219,0.1);
}
.job-card-info { flex: 1; min-width: 0; }
.job-card-title {
  font-weight: 600; font-size: 12.5px; color: var(--el-text);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 3px;
}
.job-card-loc { font-size: 11px; color: var(--el-text-muted); display: flex; align-items: center; gap: 3px; }
.job-card-arrow {
  font-size: 16px; color: var(--el-primary);
  margin-left: 8px; flex-shrink: 0; opacity: 0.7;
  transition: opacity 0.15s, transform 0.15s;
}
.job-card:hover .job-card-arrow { opacity: 1; transform: translateX(2px); }

#chatbot-suggestions {
  display: flex; gap: 8px; padding: 10px 12px;
  border-top: 1px solid var(--el-border);
  background: var(--el-surface); flex-shrink: 0;
}
#chatbot-suggestions button {
  flex: 1; border: 1.5px solid var(--el-border);
  background: var(--el-bg); border-radius: 10px;
  padding: 7px 6px; cursor: pointer; font-size: 11.5px;
  color: var(--el-text-muted); font-family: var(--el-font);
  font-weight: 500; transition: all 0.18s ease;
}
#chatbot-suggestions button:hover {
  border-color: var(--el-primary-light); color: var(--el-primary); background: #eff6ff;
}
#chatbot-suggestions button.active {
  border-color: var(--el-primary); background: #eff6ff;
  color: var(--el-primary); font-weight: 700;
}

#chatbot-input {
  display: flex; gap: 8px; padding: 12px 14px;
  border-top: 1px solid var(--el-border);
  background: var(--el-surface); flex-shrink: 0;
}
#chatbot-text {
  flex: 1; border: 1.5px solid var(--el-border);
  border-radius: 12px; padding: 10px 14px; outline: none;
  font-size: 13.5px; font-family: var(--el-font);
  color: var(--el-text); background: var(--el-bg);
  transition: border-color 0.2s, box-shadow 0.2s;
}
#chatbot-text:focus {
  border-color: var(--el-primary-light);
  box-shadow: 0 0 0 3px rgba(26,86,219,0.1);
}
#chatbot-text::placeholder { color: var(--el-text-muted); }

#chatbot-send {
  width: 42px; height: 42px; border: none; border-radius: 12px;
  background: var(--el-user-bubble); color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; flex-shrink: 0;
  transition: transform 0.15s, box-shadow 0.15s;
  box-shadow: 0 4px 14px rgba(26,86,219,0.3);
}
#chatbot-send:hover { transform: scale(1.06); box-shadow: 0 6px 20px rgba(26,86,219,0.4); }
#chatbot-send:active { transform: scale(0.94); }

.typing-dots span {
  display: inline-block; width: 7px; height: 7px;
  border-radius: 50%; background: var(--el-primary-light);
  margin: 0 2px; animation: bounce 1.2s infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%,80%,100% { transform: translateY(0); opacity: 0.5; }
  40%          { transform: translateY(-6px); opacity: 1; }
}
        `;
        document.head.appendChild(style);
    }

    // ── Typing dots ──────────────────────────────────────────────────────────
    function startDots(bubble) {
        bubble.innerHTML = "";
        const dots = document.createElement("div");
        dots.className = "typing-dots";
        dots.innerHTML = "<span></span><span></span><span></span>";
        bubble.appendChild(dots);
        return () => { bubble.innerHTML = ""; };
    }

    // ── Render job cards ─────────────────────────────────────────────────────
    function addJobCards(bubble, text) {
        bubble.innerHTML = "";
        bubble.style.background = "var(--el-surface)";
        bubble.style.padding = "12px 14px";

        const lines = text.split("\n");  // ← déplacé EN PREMIER
        let titre = "🎯 Vos offres intéressantes";
        const titreLine = lines.find(l => l.trim().startsWith("JOB_TITLE:"));
        if (titreLine) titre = titreLine.replace("JOB_TITLE:", "").trim();
        bubble.appendChild(el("div", { class: "job-cards-intro" }, [titre]));
        let count = 0;

        lines.forEach(line => {
            const trimmed = line.trim();
            if (!trimmed.startsWith("JOB_ITEM:")) return;

            const parts    = trimmed.replace("JOB_ITEM:", "").split("|");
            const title    = (parts[0] || "Offre sans titre").trim();
            const location = (parts[1] || "Localisation non précisée").trim();
            const link     = (parts[2] || "").trim();

            const card = el("div", { class: "job-card" });
            const info = el("div", { class: "job-card-info" });
            info.appendChild(el("div", { class: "job-card-title" }, [title]));
            info.appendChild(el("div", { class: "job-card-loc"   }, ["📍 " + location]));
            card.appendChild(info);

            if (link) {
                card.appendChild(el("span", { class: "job-card-arrow" }, ["↗"]));
                card.addEventListener("click", () => window.open(link, "_blank"));
            }

            bubble.appendChild(card);
            count++;
        });

        if (count === 0) {
            bubble.textContent = "Aucune offre intéressante trouvée.";
        }
    }

    // ── Add message row ──────────────────────────────────────────────────────
    function addMessage(role, text) {
        const messages = document.getElementById("chatbot-messages");
        const isUser   = role === "user";
        const row      = el("div", { class: `chat-msg ${isUser ? "user" : "bot"}` });

        if (!isUser) {
            row.appendChild(el("img", { class: "chat-avatar", src: "images/etudlife.png", alt: "Bot" }));
        }

        const bubble = el("div", { class: "chat-bubble" });

        if (!isUser && text.includes("JOB_ITEM:")) {
            addJobCards(bubble, text);
        } else {
            bubble.textContent = text;
        }

        row.appendChild(bubble);
        messages.appendChild(row);
        messages.scrollTop = messages.scrollHeight;
        return bubble;
    }

    // ── Session ──────────────────────────────────────────────────────────────
    async function newSession() {
        const r    = await fetch(`${API_BASE}/new-session`, { method: "POST" });
        const data = await r.json();
        sessionId  = data.sessionId;
        return sessionId;
    }

    // ── Send message via /message (pas /stream) ──────────────────────────────
    async function sendMessage(question) {
        if (!sessionId) await newSession();

        const messages = document.getElementById("chatbot-messages");
        const row      = el("div", { class: "chat-msg bot" });
        const bubble   = el("div", { class: "chat-bubble" });
        row.appendChild(el("img", { class: "chat-avatar", src: "images/etudlife.png", alt: "Bot" }));
        row.appendChild(bubble);
        messages.appendChild(row);
        messages.scrollTop = messages.scrollHeight;

        const stopDots = startDots(bubble);

        try {
            const res  = await fetch(`${API_BASE}/message`, {
                method:  "POST",
                headers: { "Content-Type": "application/json" },
                body:    JSON.stringify({ sessionId, question, mode })
            });

            const data = await res.json();
            stopDots();

            if (data.sessionId) sessionId = data.sessionId;

            const text = data.response || "";

            if (text.includes("JOB_ITEM:")) {
                addJobCards(bubble, text.trim());
            } else {
                bubble.textContent = text;
            }

        } catch (e) {
            stopDots();
            bubble.textContent = "Désolé, une erreur est survenue.";
        }

        messages.scrollTop = messages.scrollHeight;
    }

    // ── Mount ────────────────────────────────────────────────────────────────
    function mount() {
        addStyles();
        if (document.getElementById("chatbot-btn")) return;

        const btn   = el("button", { id: "chatbot-btn", type: "button", "aria-label": "Chat" }, ["💬"]);
        const panel = el("div",    { id: "chatbot-panel" });

        panel.appendChild(
            el("div", { id: "chatbot-header" }, [
                el("div", { class: "chatbot-header-info" }, [
                    el("div", { class: "chatbot-header-avatar" }, ["🎓"]),
                    el("div", { class: "chatbot-header-text"   }, [
                        el("div", { class: "title"    }, ["EtudLife Assistant"]),
                        el("div", { class: "subtitle" }, [
                            el("span", { class: "chatbot-online-dot" }),
                            "En ligne"
                        ]),
                    ]),
                ]),
                el("button", { id: "chatbot-close", type: "button" }, ["✕"]),
            ])
        );

        panel.appendChild(el("div", { id: "chatbot-messages" }));

        panel.appendChild(
            el("div", { id: "chatbot-suggestions" }, [
                el("button", { type: "button", id: "btn-reg"  }, ["📚 Règlement / examens"]),
                el("button", { type: "button", id: "btn-site" }, ["💻 Fonctionnement du site"]),
            ])
        );

        panel.appendChild(
            el("div", { id: "chatbot-input" }, [
                el("input",  { id: "chatbot-text", placeholder: "Écris ton message...", autocomplete: "off" }),
                el("button", { id: "chatbot-send", type: "button" }, ["➤"]),
            ])
        );

        document.body.appendChild(btn);
        document.body.appendChild(panel);

        addMessage("bot",
            "Bienvenue sur EtudLife ! 👋\n\n" +
            "Je peux vous aider à :\n" +
            "• Consulter vos offres de stage / alternance\n" +
            "• Gérer votre agenda et vos proches\n" +
            "• Réserver des livres et salles\n" +
            "• Répondre aux questions sur le règlement de Paris Nanterre\n\n" +
            "Que souhaitez-vous faire ?"
        );

        const btnReg  = document.getElementById("btn-reg");
        const btnSite = document.getElementById("btn-site");

        function setMode(newMode) {
            mode = newMode;
            btnReg.classList.toggle("active",  mode === "REGLEMENT");
            btnSite.classList.toggle("active", mode === "SITE");
            if (mode === "REGLEMENT") {
                addMessage("bot", "Posez-moi vos questions sur le règlement de l'Université Paris Nanterre : examens, fraude, plagiat, bizutage, retards…");
            } else {
                addMessage("bot", "Posez-moi vos questions sur le fonctionnement du site EtudLife.");
            }
        }

        btnReg.addEventListener("click",  () => setMode("REGLEMENT"));
        btnSite.addEventListener("click", () => setMode("SITE"));

        btn.addEventListener("click", () => {
            const isOpen = panel.style.display === "flex";
            panel.style.display = isOpen ? "none" : "flex";
            if (!isOpen) panel.style.flexDirection = "column";
        });

        document.getElementById("chatbot-close").addEventListener("click", () => {
            panel.style.display = "none";
        });

        const input   = document.getElementById("chatbot-text");
        const sendBtn = document.getElementById("chatbot-send");

        async function onSend() {
            const q = input.value.trim();
            if (!q) return;
            input.value = "";
            addMessage("user", q);

            const greetings = ["bonjour", "salut", "hello", "hey", "bonsoir"];
            if (greetings.includes(q.toLowerCase().trim())) {
                addMessage("bot", "Bonjour ! 😊 Comment puis-je vous aider ?");
                return;
            }

            await sendMessage(q);
        }

        sendBtn.addEventListener("click", onSend);
        input.addEventListener("keydown", (e) => { if (e.key === "Enter") onSend(); });

        window.addEventListener("beforeunload", () => {
            if (sessionId) navigator.sendBeacon(`${API_BASE}/close?sessionId=${encodeURIComponent(sessionId)}`);
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", mount);
    } else {
        mount();
    }
})();