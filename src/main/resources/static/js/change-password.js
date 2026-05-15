document.getElementById("changePasswordForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const currentPassword = document.getElementById("currentPassword").value.trim();
    const newPassword = document.getElementById("newPassword").value.trim();
    const confirmPassword = document.getElementById("confirmPassword").value.trim();
    const messageBox = document.getElementById("messageBox");

    messageBox.style.display = "none";
    messageBox.textContent = "";
    messageBox.className = "message-box";

    const strongPasswordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;

    if (newPassword !== confirmPassword) {
        showMessage("New password and confirm password do not match.", "error");
        return;
    }

    if (!strongPasswordPattern.test(newPassword)) {
        showMessage("Password must be at least 8 characters and include uppercase, lowercase, number, and special character.", "error");
        return;
    }

    if (currentPassword === newPassword) {
        showMessage("New password must be different from current password.", "error");
        return;
    }

    try {
        const response = await fetch("/api/auth/change-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "same-origin",
            body: JSON.stringify({
                currentPassword,
                newPassword,
                confirmPassword
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            showMessage(data.message || "Password updated successfully.", "success");

            document.getElementById("changePasswordForm").reset();

            setTimeout(() => {
                window.location.href = "/login";
            }, 2000);
        } else {
            showMessage(data.message || "Password update failed.", "error");
        }

    } catch (error) {
        showMessage("Something went wrong. Please try again.", "error");
    }
});

function showMessage(message, type) {
    const messageBox = document.getElementById("messageBox");

    messageBox.style.display = "block";
    messageBox.textContent = message;
    messageBox.className = "message-box";
    messageBox.classList.add(type);
}

function togglePassword(inputId, button) {

    const input = document.getElementById(inputId);
    const icon = button.querySelector("i");

    if (input.type === "password") {

        input.type = "text";

        icon.classList.remove("fa-eye-slash");
        icon.classList.add("fa-eye");

    } else {

        input.type = "password";

        icon.classList.remove("fa-eye");
        icon.classList.add("fa-eye-slash");
    }
}