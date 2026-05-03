const NOTIFICATION_API_URL = "/api/notifications";
let showAllNotifications = false;

document.addEventListener("DOMContentLoaded", () => {
    fetchNotifications();
});

async function fetchNotifications() {
    const list = document.getElementById("notification-list");
    const countBadge = document.getElementById("notif-count");

    if (!list || !countBadge) return;

    try {
        const response = await fetch(NOTIFICATION_API_URL);

        if (!response.ok) {
            throw new Error("Failed to load notifications");
        }

        const notifications = await response.json();

        const unreadCount = notifications.filter(notification => !notification.read).length;
        countBadge.textContent = unreadCount;

        list.innerHTML = "";

        if (notifications.length === 0) {
            list.innerHTML = `
                <li class="notification">
                    <div class="notif-content">
                        <div class="notif-icon">🔔</div>
                        <div class="notif-text">
                            <p class="notif-message">No notifications yet.</p>
                        </div>
                    </div>
                </li>
            `;
            return;
        }

        const visibleNotifications = showAllNotifications
            ? notifications
            : notifications.slice(0, 3);

        visibleNotifications.forEach(notification => {
            const li = document.createElement("li");
            li.className = notification.read ? "notification read" : "notification unread";

            li.innerHTML = `
                <div class="notif-content">
                    <div class="notif-icon">${getNotificationIcon(notification.type)}</div>

                    <div class="notif-text">
                        <p class="notif-message">${escapeHtml(notification.message)}</p>
                        <span class="notif-meta">
                            ${escapeHtml(notification.type)} • ${formatDate(notification.createdAt)}
                        </span>
                    </div>

                    ${
                notification.read
                    ? ""
                    : `<button class="notif-btn" onclick="markNotificationAsRead(${notification.id})">Read</button>`
            }
                </div>
            `;

            list.appendChild(li);
        });

        if (notifications.length > 3) {
            const toggleLi = document.createElement("li");
            toggleLi.className = "notification-toggle-row";

            toggleLi.innerHTML = `
                <button class="notif-view-toggle" onclick="toggleViewAllNotifications()">
                    ${showAllNotifications ? "View Less" : "View All"}
                </button>
            `;

            list.appendChild(toggleLi);
        }

    } catch (error) {
        console.error(error);
        list.innerHTML = `
            <li class="notification">
                <div class="notif-content">
                    <div class="notif-icon">⚠️</div>
                    <div class="notif-text">
                        <p class="notif-message" style="color:red;">Failed to load notifications.</p>
                    </div>
                </div>
            </li>
        `;
    }
}

function toggleViewAllNotifications() {
    showAllNotifications = !showAllNotifications;
    fetchNotifications();
}

async function markNotificationAsRead(id) {
    try {
        const response = await fetch(`${NOTIFICATION_API_URL}/${id}/read`, {
            method: "PUT"
        });

        if (!response.ok) {
            throw new Error("Failed to mark notification as read");
        }

        fetchNotifications();

    } catch (error) {
        console.error(error);
        showCustomAlert("Failed to mark notification as read.", "Error", "⚠️");
    }
}

function toggleNotifications() {
    const panel = document.getElementById("notification-panel");

    if (panel) {
        panel.classList.toggle("hidden");
    }
}

function getNotificationIcon(type) {
    switch (type) {
        case "PAYMENT_REMINDER":
            return "💰";
        case "PAYMENT_OVERDUE":
            return "⚠️";
        case "PAYMENT_PAID":
            return "✅";
        case "PAYMENT_UPDATED":
            return "✏️";
        case "SITE_VISIT_SCHEDULED":
            return "📅";
        case "SITE_VISIT_REMINDER":
            return "⏰";
        case "SITE_VISIT_CHECKED_IN":
            return "📍";
        case "SITE_VISIT_COMPLETED":
            return "✔️";
        default:
            return "🔔";
    }
}

function formatDate(dateValue) {
    if (!dateValue) return "-";
    return new Date(dateValue).toLocaleString();
}

function escapeHtml(text) {
    if (!text) return "";
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}
function showCustomAlert(message, title = "Notification", icon = "🔔") {
    const oldAlert = document.querySelector(".custom-alert-overlay");
    if (oldAlert) oldAlert.remove();

    const overlay = document.createElement("div");
    overlay.className = "custom-alert-overlay";

    overlay.innerHTML = `
        <div class="custom-alert-box">
            <div class="custom-alert-icon">${icon}</div>
            <h3>${escapeHtml(title)}</h3>
            <p>${escapeHtml(message)}</p>
            <button onclick="this.closest('.custom-alert-overlay').remove()">OK</button>
        </div>
    `;

    document.body.appendChild(overlay);
}