const NOTIFICATION_API_URL = "/api/notifications";
let showAllNotifications = false;

document.addEventListener("DOMContentLoaded", () => {
    fetchNotifications();
    setInterval(fetchNotifications, 10000); // auto refresh
});

async function fetchNotifications() {
    const list = document.getElementById("notification-list");
    const countBadge = document.getElementById("notif-count");

    if (!list || !countBadge) return;

    try {
        const response = await fetch(NOTIFICATION_API_URL);
        const notifications = await response.json();

        const unreadCount = notifications.filter(n => !n.read).length;
        countBadge.textContent = unreadCount;

        list.innerHTML = "";

        const visibleNotifications = showAllNotifications
            ? notifications
            : notifications.slice(0, 3);

        visibleNotifications.forEach(notification => {

            const li = document.createElement("li");
            li.className = notification.read ? "notification read" : "notification unread";

            li.innerHTML = `
                <div class="notif-content" style="cursor:pointer;">
                    <div class="notif-icon">${getNotificationIcon(notification.type)}</div>

                    <div class="notif-text">
                        <p class="notif-message">${escapeHtml(notification.message)}</p>
                        <span class="notif-meta">
                            ${notification.type} • ${formatDate(notification.createdAt)}
                        </span>
                    </div>

                    ${
                notification.read
                    ? ""
                    : `<button class="notif-btn" onclick="event.stopPropagation(); markNotificationAsRead(${notification.id})">Read</button>`
            }
                </div>
            `;

            // 🔥 CLICK = READ + REDIRECT
            li.addEventListener("click", async () => {
                try {
                    if (!notification.read) {
                        await fetch(`${NOTIFICATION_API_URL}/${notification.id}/read`, {
                            method: "PUT"
                        });
                    }

                    if (notification.link) {
                        window.location.href = notification.link;
                    }

                } catch (error) {
                    console.error(error);
                }
            });

            list.appendChild(li);
        });

    } catch (error) {
        console.error(error);
    }
}

function markNotificationAsRead(id) {
    fetch(`${NOTIFICATION_API_URL}/${id}/read`, {
        method: "PUT"
    }).then(fetchNotifications);
}

function toggleNotifications() {
    const panel = document.getElementById("notification-panel");
    if (panel) {
        panel.classList.toggle("hidden");
        if (!panel.classList.contains("hidden")) {
            fetchNotifications();
        }
    }
}

function getNotificationIcon(type) {
    switch (type) {
        case "PAYMENT_REMINDER": return "💰";
        case "PAYMENT_OVERDUE": return "⚠️";
        case "PAYMENT_PAID": return "✅";
        case "PAYMENT_UPDATED": return "✏️";
        case "SITE_VISIT_SCHEDULED": return "📅";
        default: return "🔔";
    }
}

function formatDate(dateValue) {
    return dateValue ? new Date(dateValue).toLocaleString() : "-";
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}