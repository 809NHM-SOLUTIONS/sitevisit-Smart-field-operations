const API_URL = "/api/payment-reminders";

document.addEventListener("DOMContentLoaded", () => {
    fetchPaymentReminders();

    const form = document.getElementById("paymentReminderForm");

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const editingId = document.getElementById("editingReminderId").value;

            const reminder = {
                title: document.getElementById("title").value.trim(),
                paymentDate: document.getElementById("paymentDate").value,
                message: document.getElementById("message").value.trim()
            };

            if (!reminder.title || !reminder.paymentDate) {
                alert("Please fill in title and payment date.");
                return;
            }

            try {
                const url = editingId ? `${API_URL}/${editingId}` : API_URL;
                const method = editingId ? "PUT" : "POST";

                const response = await fetch(url, {
                    method: method,
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(reminder)
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(errorText || "Failed to save reminder");
                }

                alert(editingId ? "Payment reminder updated successfully." : "Payment reminder saved successfully.");

                form.reset();
                resetFormMode();
                fetchPaymentReminders();
                refreshNotificationBadge();

            } catch (error) {
                console.error(error);
                alert(error.message || "Something went wrong.");

                if (error.message.includes("already have a payment reminder")) {
                    autoEditFirstReminder();
                }
            }
        });
    }
});

async function fetchPaymentReminders() {
    const tbody = document.getElementById("paymentReminderTableBody");

    if (!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="6" style="text-align:center;">Loading reminders...</td>
        </tr>
    `;

    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load reminders");
        }

        const reminders = await response.json();

        tbody.innerHTML = "";

        if (reminders.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align:center;">No payment reminders yet.</td>
                </tr>
            `;
            return;
        }

        reminders.forEach(reminder => {
            const row = document.createElement("tr");
            const status = getReminderStatus(reminder);

            row.innerHTML = `
            <td>${escapeHtml(reminder.title)}</td>
            <td>${reminder.paymentDate || "-"}</td>
            <td>${status}</td>
            <td>${reminder.lastReminderSentDate || "-"}</td>
            <td>
                <button class="btn small-btn" onclick='editReminder(${JSON.stringify(reminder)})'>Edit</button>
                ${
                        reminder.paid
                            ? `<span class="status completed">Paid</span>`
                            : `<button class="btn btn-primary small-btn" onclick="markAsPaid(${reminder.id})">Mark as Paid</button>`
                    }
            </td>
        `;

            tbody.appendChild(row);
        });

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align:center; color:red;">Failed to load reminders.</td>
            </tr>
        `;
    }
}

function editReminder(reminder) {
    document.getElementById("editingReminderId").value = reminder.id;
    document.getElementById("title").value = reminder.title || "";
    document.getElementById("paymentDate").value = reminder.paymentDate || "";
    document.getElementById("message").value = reminder.message || "";
    document.getElementById("saveReminderBtn").textContent = "Update Reminder";
    document.getElementById("cancelEditBtn").style.display = "inline-block";

    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function autoEditFirstReminder() {
    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load existing reminder.");
        }

        const reminders = await response.json();

        if (reminders.length > 0) {
            editReminder(reminders[0]);
        }

    } catch (err) {
        console.error("Failed to auto-load reminder for editing", err);
    }
}

function cancelEdit() {
    document.getElementById("paymentReminderForm").reset();
    resetFormMode();
}

function resetFormMode() {
    document.getElementById("editingReminderId").value = "";
    document.getElementById("saveReminderBtn").textContent = "Save Reminder";
    document.getElementById("cancelEditBtn").style.display = "none";
}

function getReminderStatus(reminder) {
    if (reminder.paid) {
        return `<span class="status completed">Paid</span>`;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const paymentDate = new Date(reminder.paymentDate);
    paymentDate.setHours(0, 0, 0, 0);

    const diffTime = paymentDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
        return `<span class="status pending">Overdue</span>`;
    }

    if (diffDays <= 7) {
        return `<span class="status progress">Due Soon</span>`;
    }

    return `<span class="status completed">Upcoming</span>`;
}
function showConfirmAlert(message, title = "Confirm Action", icon = "❓", onConfirm) {
    const oldAlert = document.querySelector(".custom-alert-overlay");
    if (oldAlert) oldAlert.remove();

    const overlay = document.createElement("div");
    overlay.className = "custom-alert-overlay";

    overlay.innerHTML = `
        <div class="custom-alert-box">
            <div class="custom-alert-icon">${icon}</div>
            <h3>${escapeHtml(title)}</h3>
            <p>${escapeHtml(message)}</p>

            <div style="display:flex; gap:10px; justify-content:center;">
                <button id="customConfirmBtn">Confirm</button>
                <button id="customCancelBtn" style="background:#e5e7eb; color:#111;">Cancel</button>
            </div>
        </div>
    `;

    document.body.appendChild(overlay);

    document.getElementById("customConfirmBtn").onclick = () => {
        overlay.remove();
        onConfirm();
    };

    document.getElementById("customCancelBtn").onclick = () => {
        overlay.remove();
    };
}
async function markAsPaid(id) {
    showConfirmAlert(
        "Mark this stipend payment as paid?",
        "Confirm Payment",
        "✅",
        async () => {
            try {
                const response = await fetch(`${API_URL}/${id}/paid`, {
                    method: "PUT"
                });

                if (!response.ok) {
                    throw new Error("Failed to mark as paid");
                }

                showCustomAlert("Payment marked as paid. Reminder moved to next month.", "Success", "✅");
                fetchPaymentReminders();
                refreshNotificationBadge();

            } catch (error) {
                console.error(error);
                showCustomAlert("Failed to mark payment as paid.", "Error", "⚠️");
            }
        }
    );
}

async function sendReminderCheckNow() {
    try {
        const response = await fetch(`${API_URL}/send-now`, {
            method: "POST"
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Failed to run reminder check");
        }

        alert("Reminder check completed.");
        fetchPaymentReminders();
        refreshNotificationBadge();

    } catch (error) {
        console.error(error);
        alert(error.message || "Failed to send reminder check.");
    }
}

function refreshNotificationBadge() {
    if (typeof fetchNotifications === "function") {
        fetchNotifications();
    }
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