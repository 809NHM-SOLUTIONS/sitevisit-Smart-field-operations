const SITE_VISIT_API_URL = "/api/site-visit-reminders";

document.addEventListener("DOMContentLoaded", () => {
    fetchSiteVisitReminders();
});

async function fetchSiteVisitReminders() {
    const tbody = document.getElementById("siteVisitReminderTableBody");

    if (!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="5" style="text-align:center;">Loading site visit reminders...</td>
        </tr>
    `;

    try {
        const response = await fetch(SITE_VISIT_API_URL);

        if (!response.ok) {
            throw new Error("Failed to load site visit reminders");
        }

        const visits = await response.json();

        tbody.innerHTML = "";

        if (visits.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align:center;">No upcoming site visit reminders.</td>
                </tr>
            `;
            return;
        }

        visits.forEach(visit => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${escapeHtml(visit.company?.name || "-")}</td>
                <td>${visit.visitDate || "-"}</td>
                <td>${visit.visitTime || "-"}</td>
                <td>${getSiteVisitStatus(visit)}</td>
                <td>${visit.lastReminderSentDate || "-"}</td>
            `;

            tbody.appendChild(row);
        });

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align:center; color:red;">Failed to load site visit reminders.</td>
            </tr>
        `;
    }
}

async function sendSiteVisitReminderCheckNow() {
    try {
        const response = await fetch(`${SITE_VISIT_API_URL}/send-now`, {
            method: "POST"
        });

        if (!response.ok) {
            throw new Error("Failed to run site visit reminder check");
        }
        showCustomAlert("Reminder check completed.", "Success", "✅");
        fetchSiteVisitReminders();

        if (typeof fetchNotifications === "function") {
            fetchNotifications();
        }

    } catch (error) {
        console.error(error);
        showCustomAlert("Failed to send site visit reminders.", "Error", "⚠️");
    }
}

function getSiteVisitStatus(visit) {
    if (visit.checkedIn) {
        return `<span class="status completed">Checked In</span>`;
    }

    if (visit.status === "In Progress") {
        return `<span class="status progress">In Progress</span>`;
    }

    return `<span class="status pending">${escapeHtml(visit.status || "Scheduled")}</span>`;
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