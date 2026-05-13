document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("resetPasswordForm");
    const messageBox = document.getElementById("messageBox");

    // Autofill saved email
    const savedEmail = localStorage.getItem("resetEmail");

    const emailInput = document.getElementById("email");

    if (savedEmail && emailInput) {
        emailInput.value = savedEmail;
    }

    form.addEventListener("submit", async function (e) {

        e.preventDefault();

        const email = document.getElementById("email").value;

        const otp = document.getElementById("otp").value;

        const newPassword =
            document.getElementById("newPassword").value;

        messageBox.style.display = "none";
        messageBox.textContent = "";

        try {

            const response = await fetch("/api/auth/reset-password", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email,
                    otp,
                    newPassword
                })
            });

            const data = await response.json();

            messageBox.style.display = "block";
            messageBox.textContent = data.message;

            if (response.ok && data.success) {

                localStorage.removeItem("resetEmail");

                setTimeout(() => {
                    window.location.href = "/login";
                }, 1500);
            }

        } catch (error) {

            console.error("Reset password error:", error);

            messageBox.style.display = "block";

            messageBox.textContent =
                "Something went wrong. Please try again.";
        }
    });
});