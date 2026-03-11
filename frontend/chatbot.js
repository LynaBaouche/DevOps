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
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

:root {
  --c-blue:       #2563eb;
  --c-blue-dark:  #1d4ed8;
  --c-blue-light: #eff6ff;
  --c-bg:         #f5f7fb;
  --c-surface:    #ffffff;
  --c-border:     #e4e9f2;
  --c-text:       #111827;
  --c-muted:      #6b7280;
  --c-online:     #22c55e;
  --c-shadow:     0 24px 64px rgba(37,99,235,.14);
  --c-radius:     18px;
  --c-font:       'DM Sans', sans-serif;
  --c-grad:       linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
}

/* ── Toggle button ───────────────────────────────────────────── */
#chatbot-btn {
  position: fixed; right: 24px; bottom: 24px;
  width: 56px; height: 56px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; background: var(--c-grad);
  border: none; z-index: 9999;
  box-shadow: 0 8px 28px rgba(37,99,235,.45);
  transition: transform .2s ease, box-shadow .2s ease;
}
#chatbot-btn svg { width:24px; height:24px; fill:#fff; }
#chatbot-btn:hover  { transform: translateY(-3px); box-shadow: 0 14px 36px rgba(37,99,235,.5); }
#chatbot-btn:active { transform: scale(.94); }

/* ── Panel ───────────────────────────────────────────────────── */
#chatbot-panel {
  position: fixed; right: 24px; bottom: 96px;
  width: 360px; height: 540px;
  background: var(--c-surface); border-radius: var(--c-radius);
  box-shadow: var(--c-shadow); display: none; flex-direction: column;
  overflow: hidden; z-index: 9999; font-family: var(--c-font);
  border: 1px solid var(--c-border);
  animation: panelIn .28s cubic-bezier(.34,1.5,.64,1);
}
@keyframes panelIn {
  from { opacity:0; transform: translateY(16px) scale(.97); }
  to   { opacity:1; transform: translateY(0)    scale(1);   }
}

/* ── Header ──────────────────────────────────────────────────── */
#chatbot-header {
  padding: 14px 16px; display: flex;
  justify-content: space-between; align-items: center;
  background: var(--c-grad); flex-shrink: 0;
}
.cb-header-left  { display: flex; align-items: center; gap: 10px; }
.cb-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: rgba(255,255,255,.18); flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.cb-avatar svg { width:18px; height:18px; fill:#fff; }
.cb-title   { font-weight:700; font-size:14px; color:#fff; letter-spacing:-.1px; }
.cb-status  { display:flex; align-items:center; gap:5px; margin-top:2px; }
.cb-dot {
  width:7px; height:7px; border-radius:50%;
  background: var(--c-online); box-shadow: 0 0 6px var(--c-online);
  animation: blink 2.4s infinite;
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.35} }
.cb-status-text { font-size:11px; color:rgba(255,255,255,.8); }

#chatbot-close {
  width:28px; height:28px; border-radius:50%; border:none;
  background: rgba(255,255,255,.15); color:#fff;
  display:flex; align-items:center; justify-content:center;
  cursor:pointer; font-size:13px;
  transition: background .18s;
}
#chatbot-close:hover { background: rgba(255,255,255,.28); }

/* ── Messages ────────────────────────────────────────────────── */
#chatbot-messages {
  flex:1; padding:14px 12px; overflow-y:auto;
  background: var(--c-bg); display:flex;
  flex-direction:column; gap:6px; scroll-behavior:smooth;
}
#chatbot-messages::-webkit-scrollbar { width:3px; }
#chatbot-messages::-webkit-scrollbar-thumb { background:var(--c-border); border-radius:99px; }

.chat-msg { display:flex; align-items:flex-end; gap:8px; }
.chat-msg.user { justify-content:flex-end; }
.chat-msg.bot  { justify-content:flex-start; }

.chat-avatar {
  width:30px; height:30px; border-radius:50%;
  flex-shrink:0; object-fit:cover;
  border:2px solid #fff; box-shadow:0 2px 6px rgba(0,0,0,.1);
}

.chat-bubble {
  max-width:78%; padding:10px 13px; border-radius:16px;
  font-size:13px; line-height:1.55; white-space:pre-wrap;
  word-break:break-word; font-family:var(--c-font);
  animation: msgIn .18s ease;
}
@keyframes msgIn {
  from { opacity:0; transform:translateY(5px); }
  to   { opacity:1; transform:translateY(0); }
}
.chat-msg.user .chat-bubble {
  background: var(--c-grad); color:#fff;
  border-bottom-right-radius:4px;
  box-shadow: 0 4px 14px rgba(37,99,235,.22);
}
.chat-msg.bot .chat-bubble {
  background: var(--c-surface); color: var(--c-text);
  border-bottom-left-radius:4px;
  box-shadow: 0 2px 10px rgba(0,0,0,.05);
  border: 1px solid var(--c-border);
}

/* ── Job cards ───────────────────────────────────────────────── */
.job-list-label {
  font-size:11px; font-weight:600; letter-spacing:.06em;
  text-transform:uppercase; color:var(--c-blue);
  margin-bottom:8px; padding-bottom:6px;
  border-bottom:1px solid var(--c-border);
}
.job-card {
  display:flex; align-items:center; justify-content:space-between;
  background: var(--c-bg); border:1px solid var(--c-border);
  border-radius:10px; padding:9px 11px; margin-bottom:6px;
  cursor:pointer; transition: all .16s ease;
  text-decoration:none;
}
.job-card:last-child { margin-bottom:0; }
.job-card:hover {
  background:#eff6ff; border-color:#93c5fd;
  transform:translateX(2px);
  box-shadow:0 3px 12px rgba(37,99,235,.1);
}
.job-card-info { flex:1; min-width:0; }
.job-card-title {
  font-weight:600; font-size:12px; color:var(--c-text);
  white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
  margin-bottom:2px;
}
.job-card-loc {
  font-size:11px; color:var(--c-muted);
  display:flex; align-items:center; gap:3px;
}
.job-card-loc svg { width:10px; height:10px; flex-shrink:0; }
.job-card-icon {
  width:22px; height:22px; border-radius:6px;
  background:#dbeafe; display:flex; align-items:center; justify-content:center;
  margin-left:8px; flex-shrink:0;
  transition: background .16s;
}
.job-card-icon svg { width:11px; height:11px; fill:#2563eb; }
.job-card:hover .job-card-icon { background:#2563eb; }
.job-card:hover .job-card-icon svg { fill:#fff; }

/* ── Mode buttons ────────────────────────────────────────────── */
#chatbot-suggestions {
  display:flex; gap:6px; padding:9px 12px;
  border-top:1px solid var(--c-border);
  background:var(--c-surface); flex-shrink:0;
}
#chatbot-suggestions button {
  flex:1; border:1.5px solid var(--c-border);
  background:var(--c-bg); border-radius:8px;
  padding:6px 4px; cursor:pointer; font-size:11px;
  color:var(--c-muted); font-family:var(--c-font);
  font-weight:500; transition:all .16s ease;
}
#chatbot-suggestions button:hover {
  border-color:#93c5fd; color:var(--c-blue); background:#eff6ff;
}
#chatbot-suggestions button.active {
  border-color:var(--c-blue); background:#eff6ff;
  color:var(--c-blue); font-weight:700;
}

/* ── Input ───────────────────────────────────────────────────── */
#chatbot-input {
  display:flex; gap:8px; padding:10px 12px;
  border-top:1px solid var(--c-border);
  background:var(--c-surface); flex-shrink:0;
}
#chatbot-text {
  flex:1; border:1.5px solid var(--c-border);
  border-radius:10px; padding:9px 13px; outline:none;
  font-size:13px; font-family:var(--c-font);
  color:var(--c-text); background:var(--c-bg);
  transition:border-color .18s, box-shadow .18s;
}
#chatbot-text:focus {
  border-color:#93c5fd;
  box-shadow:0 0 0 3px rgba(37,99,235,.1);
}
#chatbot-text::placeholder { color:var(--c-muted); }

#chatbot-send {
  width:40px; height:40px; border:none; border-radius:10px;
  background:var(--c-grad); color:#fff; cursor:pointer;
  display:flex; align-items:center; justify-content:center;
  flex-shrink:0; box-shadow:0 4px 12px rgba(37,99,235,.3);
  transition:transform .15s, box-shadow .15s;
}
#chatbot-send svg { width:16px; height:16px; fill:#fff; }
#chatbot-send:hover { transform:scale(1.06); box-shadow:0 6px 18px rgba(37,99,235,.4); }
#chatbot-send:active { transform:scale(.93); }

/* ── Typing dots ─────────────────────────────────────────────── */
.typing-dots { display:flex; align-items:center; gap:4px; padding:2px 0; }
.typing-dots span {
  width:6px; height:6px; border-radius:50%;
  background:#93c5fd; animation:td 1.2s infinite;
}
.typing-dots span:nth-child(2) { animation-delay:.2s; }
.typing-dots span:nth-child(3) { animation-delay:.4s; }
@keyframes td {
  0%,80%,100% { transform:translateY(0); opacity:.4; }
  40%         { transform:translateY(-5px); opacity:1; }
}
        `;
        document.head.appendChild(style);
    }

    // ── Icons (inline SVG) ────────────────────────────────────────────────────
    const SVG = {
        chat: `<svg viewBox="0 0 24 24"><path d="M20 2H4a2 2 0 00-2 2v18l4-4h14a2 2 0 002-2V4a2 2 0 00-2-2z"/></svg>`,
        bot:  `<svg viewBox="0 0 24 24"><path d="M12 2a4 4 0 014 4 4 4 0 01-4 4 4 4 0 01-4-4 4 4 0 014-4m0 10c4.42 0 8 1.79 8 4v2H4v-2c0-2.21 3.58-4 8-4z"/></svg>`,
        pin:  `<svg viewBox="0 0 24 24"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 110-5 2.5 2.5 0 010 5z"/></svg>`,
        arr:  `<svg viewBox="0 0 24 24"><path d="M5 12h14M12 5l7 7-7 7" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>`,
        send: `<svg viewBox="0 0 24 24"><path d="M2 21l21-9L2 3v7l15 2-15 2v7z"/>`,
    };

    function svgEl(key) {
        const d = document.createElement("div");
        d.innerHTML = SVG[key];
        return d.firstElementChild;
    }

    // ── Typing dots ───────────────────────────────────────────────────────────
    function startDots(bubble) {
        bubble.innerHTML = "";
        const d = document.createElement("div");
        d.className = "typing-dots";
        d.innerHTML = "<span></span><span></span><span></span>";
        bubble.appendChild(d);
        return () => { bubble.innerHTML = ""; };
    }

    // ── Job cards ─────────────────────────────────────────────────────────────
    function addJobCards(bubble, text) {
        bubble.innerHTML = "";
        bubble.style.cssText = "background:var(--c-surface);padding:12px 13px;max-width:90%;";

        const lines = text.split("\n");

        let titre = "Vos offres";
        const titreLine = lines.find(l => l.trim().startsWith("JOB_TITLE:"));
        if (titreLine) titre = titreLine.replace("JOB_TITLE:", "").trim();

        const label = el("div", { class: "job-list-label" }, [titre]);
        bubble.appendChild(label);

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

            const loc = el("div", { class: "job-card-loc" });
            loc.appendChild(svgEl("pin"));
            loc.appendChild(document.createTextNode(location));
            info.appendChild(loc);
            card.appendChild(info);

            const iconBox = el("div", { class: "job-card-icon" });
            iconBox.appendChild(svgEl("arr"));
            card.appendChild(iconBox);

            if (link) {
                card.addEventListener("click", () => window.open(link, "_blank"));
            }

            bubble.appendChild(card);
            count++;
        });

        if (count === 0) bubble.textContent = "Aucune offre trouvée.";
    }

    // ── Add message ───────────────────────────────────────────────────────────
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

    // ── Session ───────────────────────────────────────────────────────────────
    async function newSession() {
        const r   = await fetch(`${API_BASE}/new-session`, { method: "POST" });
        const d   = await r.json();
        sessionId = d.sessionId;
        return sessionId;
    }

    // ── Send message ──────────────────────────────────────────────────────────
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

    // ── Mount ─────────────────────────────────────────────────────────────────
    function mount() {
        addStyles();
        if (document.getElementById("chatbot-btn")) return;

        // Toggle button
        const btn = document.createElement("button");
        btn.id = "chatbot-btn";
        btn.setAttribute("aria-label", "Chat");
        btn.appendChild(svgEl("chat"));

        const panel = el("div", { id: "chatbot-panel" });

        // Header
        const headerLeft = el("div", { class: "cb-header-left" });
        const avatar = el("div", { class: "cb-avatar" });
        avatar.appendChild(svgEl("bot"));
        headerLeft.appendChild(avatar);

        const headerText = el("div", {});
        headerText.appendChild(el("div", { class: "cb-title" }, ["EtudLife Assistant"]));
        const status = el("div", { class: "cb-status" });
        status.appendChild(el("span", { class: "cb-dot" }));
        status.appendChild(el("span", { class: "cb-status-text" }, ["En ligne"]));
        headerText.appendChild(status);
        headerLeft.appendChild(headerText);

        const closeBtn = el("button", { id: "chatbot-close", type: "button" }, ["✕"]);

        panel.appendChild(el("div", { id: "chatbot-header" }, [headerLeft, closeBtn]));
        panel.appendChild(el("div", { id: "chatbot-messages" }));
        panel.appendChild(
            el("div", { id: "chatbot-suggestions" }, [
                el("button", { type: "button", id: "btn-reg"  }, ["Reglement / examens"]),
                el("button", { type: "button", id: "btn-site" }, ["Fonctionnement du site"]),
            ])
        );

        // Send button with SVG
        const sendBtn = document.createElement("button");
        sendBtn.id = "chatbot-send";
        sendBtn.type = "button";
        sendBtn.innerHTML = `<svg viewBox="0 0 24 24" width="16" height="16" fill="#fff"><path d="M2 21l21-9L2 3v7l15 2-15 2v7z"/></svg>`;

        panel.appendChild(
            el("div", { id: "chatbot-input" }, [
                el("input", { id: "chatbot-text", placeholder: "Ecris ton message...", autocomplete: "off" }),
                sendBtn,
            ])
        );

        document.body.appendChild(btn);
        document.body.appendChild(panel);

        addMessage("bot",
            "Bienvenue sur EtudLife !\n\n" +
            "Je peux vous aider a :\n" +
            "- Consulter vos offres de stage / alternance\n" +
            "- Gerer votre agenda et vos proches\n" +
            "- Reserver des livres et salles\n" +
            "- Repondre aux questions sur le reglement de Paris Nanterre\n\n" +
            "Que souhaitez-vous faire ?"
        );

        const btnReg  = document.getElementById("btn-reg");
        const btnSite = document.getElementById("btn-site");

        function setMode(newMode) {
            mode = newMode;
            btnReg.classList.toggle("active",  mode === "REGLEMENT");
            btnSite.classList.toggle("active", mode === "SITE");
            addMessage("bot", mode === "REGLEMENT"
                ? "Posez-moi vos questions sur le reglement de l'Universite Paris Nanterre : examens, fraude, plagiat, retards..."
                : "Posez-moi vos questions sur le fonctionnement du site EtudLife."
            );
        }

        btnReg.addEventListener("click",  () => setMode("REGLEMENT"));
        btnSite.addEventListener("click", () => setMode("SITE"));

        btn.addEventListener("click", () => {
            const isOpen = panel.style.display === "flex";
            panel.style.display = isOpen ? "none" : "flex";
            if (!isOpen) panel.style.flexDirection = "column";
        });

        closeBtn.addEventListener("click", () => { panel.style.display = "none"; });

        const input = document.getElementById("chatbot-text");

        async function onSend() {
            const q = input.value.trim();
            if (!q) return;
            input.value = "";
            addMessage("user", q);
            const greetings = ["bonjour", "salut", "hello", "hey", "bonsoir"];
            if (greetings.includes(q.toLowerCase().trim())) {
                addMessage("bot", "Bonjour ! Comment puis-je vous aider ?");
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