const NOTIFICATION_API_URL = "/api/notifications";
let showAllNotifications = false;

document.addEventListener("DOMContentLoaded", () => {
    fetchNotifications();

    setInterval(fetchNotifications, 10000);

    // ✅ prevent auto-refresh from breaking "View All"
    setInterval(() => {
        if (!showAllNotifications) {
            fetchNotifications();
        }
    }, 10000);
});

async function fetchNotifications() {
    const list = document.getElementById("notification-list");
    const countBadge = document.getElementById("notif-count");

    try {
        const response = await fetch(NOTIFICATION_API_URL);
        const notifications = await response.json();

        const unreadCount = notifications.filter(n => !n.read).length;

        if (countBadge) {
            countBadge.textContent = unreadCount;
        }

        // Dashboard only has the bell count, not the list.
        // So stop here after updating the count.
        if (!list) return;

        list.innerHTML = "";

        if (notifications.length === 0) {
            list.innerHTML = `
                <li class="notification">
                    <p>No notifications yet.</p>
                </li>
            `;
            return;
        }

        // ✅ show 3 OR all
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
                    : `<button class="notif-btn"
                                onclick="event.stopPropagation(); markNotificationAsRead(${notification.id})">
                                Read
                               </button>`
            }
                </div>
            `;

            // ✅ CLICK → READ + REDIRECT
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

                    fetchNotifications();

                } catch (error) {
                    console.error(error);
                }
            });

            list.appendChild(li);
        });

        // ✅ VIEW ALL BUTTON (FIXED)
        if (notifications.length > 3) {
            const toggleLi = document.createElement("li");
            toggleLi.className = "notification-toggle-row";

            toggleLi.innerHTML = `
                <button class="notif-view-toggle"
                    onclick="event.stopPropagation(); toggleViewAllNotifications()">
                    ${showAllNotifications ? "View Less" : "View All"}
                </button>
            `;

            list.appendChild(toggleLi);
        }

    } catch (error) {
        console.error(error);
        list.innerHTML = `
            <li class="notification">
                <p style="color:red;">Failed to load notifications.</p>
            </li>
        `;
    }
}

// ✅ FIX: mark as read
function markNotificationAsRead(id) {
    fetch(`${NOTIFICATION_API_URL}/${id}/read`, {
        method: "PUT"
    }).then(() => fetchNotifications());
}

// ✅ IMPORTANT: THIS WAS MISSING / BROKEN
function toggleViewAllNotifications() {
    showAllNotifications = !showAllNotifications;
    fetchNotifications();
}

// ✅ open/close bell panel
function toggleNotifications() {
    const panel = document.getElementById("notification-panel");

    if (panel) {
        panel.classList.toggle("hidden");

        if (!panel.classList.contains("hidden")) {
            fetchNotifications();
        }
    }
}

// icons
function getNotificationIcon(type) {
    switch (type) {
        case "PAYMENT_REMINDER": return "💰";
        case "PAYMENT_OVERDUE": return "⚠️";
        case "PAYMENT_PAID": return "✅";
        case "PAYMENT_UPDATED": return "✏️";
        case "SITE_VISIT_SCHEDULED": return "📅";
        case "SITE_VISIT_REMINDER": return "🔔";
        case "REPORT": return "🔔";
        case "SITE_VISIT_REMINDER": return "⏰";
        case "SITE_VISIT_COMPLETED": return "✔️";
        case "REPORT_SUBMITTED": return "📝";
        default: return "🔔";
    }
}

// date formatting
function formatDate(dateValue) {
    return dateValue ? new Date(dateValue).toLocaleString() : "-";
}

// prevent XSS
function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}
function filterNotifications() {

    const search =
        document.getElementById(
            "notificationSearch"
        ).value.toLowerCase();

    const type =
        document.getElementById(
            "notificationTypeFilter"
        ).value;

    const date =
        document.getElementById(
            "notificationDateFilter"
        ).value;

    const notifications =
        document.querySelectorAll(
            "#notification-list .notification"
        );

    notifications.forEach(notification => {

        const message =
            notification.innerText.toLowerCase();

        const typeText =
            notification.querySelector(
                ".notif-meta"
            )?.innerText || "";

        let matchesSearch =
            message.includes(search);

        let matchesType =
            !type || typeText.includes(type);

        let matchesDate = true;

        if (date) {

            const notificationDate =
                new Date(typeText)
                    .toISOString()
                    .split("T")[0];

            matchesDate =
                notificationDate === date;
        }

        if (
            matchesSearch
            && matchesType
            && matchesDate
        ) {

            notification.style.display = "";

        } else {

            notification.style.display = "none";
        }
    });
}

function resetNotificationFilters() {

    document.getElementById(
        "notificationSearch"
    ).value = "";

    document.getElementById(
        "notificationTypeFilter"
    ).value = "";

    document.getElementById(
        "notificationDateFilter"
    ).value = "";

    filterNotifications();
}