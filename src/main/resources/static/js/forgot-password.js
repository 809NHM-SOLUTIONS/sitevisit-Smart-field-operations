document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("forgotPasswordForm");
    const messageBox = document.getElementById("messageBox");

    form.addEventListener("submit", async function (e) {

        e.preventDefault();

        const email = document.getElementById("email").value;

        messageBox.style.display = "none";
        messageBox.textContent = "";

        try {

            const response = await fetch("/api/auth/forgot-password", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            messageBox.style.display = "block";
            messageBox.textContent = data.message;

            if (response.ok && data.success) {

                // Save email for reset page
                localStorage.setItem("resetEmail", email);

                setTimeout(() => {
                    window.location.href = "/reset-password";
                }, 1500);
            }

        } catch (error) {

            console.error(error);

            messageBox.style.display = "block";
            messageBox.textContent =
                "Something went wrong. Please try again.";
        }
    });
});