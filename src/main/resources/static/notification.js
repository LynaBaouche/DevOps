document.addEventListener("DOMContentLoaded", () => {

    const user = JSON.parse(localStorage.getItem("utilisateur"));

    const notifBtn = document.getElementById("notifBtn");
    const notifDropdown = document.getElementById("notifDropdown");
    const notifList = document.getElementById("notifList");
    const notifBadge = document.getElementById("notifBadge");

    let isOpen = false;

    /* ======================
       🔔 TOGGLE DROPDOWN
       ====================== */
    notifBtn.addEventListener("click", async (e) => {
        e.stopPropagation();
        isOpen = !isOpen;

        notifDropdown.classList.toggle("hidden", !isOpen);

        if (isOpen) {
            await loadNotifications();
        }
    });

    document.addEventListener("click", () => {
        isOpen = false;
        notifDropdown.classList.add("hidden");
    });

    notifDropdown.addEventListener("click", e => e.stopPropagation());

    /* ======================
       🔴 BADGE
       ====================== */
    async function loadUnreadCount() {
        if (!user) {
            notifBadge.classList.add("hidden");
            return;
        }

        const res = await fetch(`/api/notifications/${user.id}/unread-count`);
        const count = await res.json();

        if (count > 0) {
            notifBadge.textContent = count > 9 ? "9+" : count; // 3, 4, 5...
            notifBadge.classList.remove("hidden");
        } else {
            notifBadge.classList.add("hidden");
        }
    }


    /* ======================
       📥 CONTENU DROPDOWN
       ====================== */
    async function loadNotifications() {
        notifList.innerHTML = "";

        if (!user) {
            notifList.innerHTML = `<li class="notif-item">Connectez-vous</li>`;
            return;
        }

        const res = await fetch(`/api/notifications/${user.id}`);
        const notifs = await res.json();

        if (notifs.length === 0) {
            notifList.innerHTML = `<li class="notif-item">Aucune notification</li>`;
            return;
        }

        notifs.forEach(n => {
            const li = document.createElement("li");
            li.className = "notif-item" + (n.isRead ? "" : " unread");

            li.innerHTML = `
                <div>${n.message}</div>
                <small>${new Date(n.createdAt).toLocaleString()}</small>
            `;

            li.addEventListener("click", async () => {
                if (!n.isRead) {
                    await fetch(`/api/notifications/${n.id}/read`, { method: "PUT" });
                }
                if (n.link) window.location.href = n.link;

                await loadNotifications();
                await loadUnreadCount();
            });

            notifList.appendChild(li);
        });

        await loadUnreadCount();
    }

    /* ======================
       🔄 AUTO
       ====================== */
    loadUnreadCount();
    setInterval(loadUnreadCount, 30000);

});
