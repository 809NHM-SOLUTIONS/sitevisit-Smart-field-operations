document.addEventListener("DOMContentLoaded", () => {
    loadCompanies();
    loadMembers();

    const memberForm = document.getElementById("memberForm");
    const cancelEditBtn = document.getElementById("cancelEditBtn");
    const cancelDeleteBtn = document.getElementById("cancelDeleteBtn");
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    const deleteModal = document.getElementById("deleteModal");

    memberForm.addEventListener("submit", saveOrUpdateMember);
    cancelEditBtn.addEventListener("click", resetForm);

    cancelDeleteBtn.addEventListener("click", closeDeleteModal);
    confirmDeleteBtn.addEventListener("click", confirmDeleteMember);

    deleteModal.addEventListener("click", function (e) {
        if (e.target === deleteModal) {
            closeDeleteModal();
        }
    });
});
let memberIdToDelete = null;
function showMessage(message, color) {
    const messageBox = document.getElementById("messageBox");
    if (messageBox) {
        messageBox.textContent = message;
        messageBox.style.color = color;
    }
}

async function loadCompanies() {
    const companySelect = document.getElementById("company");

    try {
        const response = await fetch("/api/companies");

        if (!response.ok) {
            throw new Error("Failed to load companies");
        }

        const companies = await response.json();

        companySelect.innerHTML = '<option value="">Select company</option>';

        companies.forEach(company => {
            const option = document.createElement("option");
            option.value = company.id;
            option.textContent = company.name;
            companySelect.appendChild(option);
        });
    } catch (error) {
        console.error("Error loading companies:", error);
        showMessage("Failed to load companies.", "red");
        companySelect.innerHTML = '<option value="">Failed to load companies</option>';
    }
}

async function loadMembers() {
    const tableBody = document.getElementById("membersTableBody");

    try {
        const response = await fetch("/api/members");

        if (!response.ok) {
            throw new Error("Failed to load members");
        }

        const members = await response.json();

        tableBody.innerHTML = "";

        if (members.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align:center;">No members found.</td>
                </tr>
            `;
            return;
        }

        members.forEach(member => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${member.id}</td>
                <td>${member.fullName}</td>
                <td>${member.email}</td>
                <td>${member.phoneNumber}</td>
                <td>${member.company ? member.company.name : 'No Company'}</td>
                <td>
    <div class="action-buttons">
        <button 
            type="button" 
            class="edit-btn"
            onclick="editMember(
                ${member.id},
                '${escapeHtml(member.fullName)}',
                '${escapeHtml(member.email)}',
                '${escapeHtml(member.phoneNumber)}',
                '${member.company ? member.company.id : ""}'
            )">
            Update
        </button>

        <button 
            type="button" 
            class="delete-btn-table"
            onclick="openDeleteModal(${member.id})">
            Delete
        </button>
    </div>
</td>
            `;

            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Error loading members:", error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align:center; color:red;">Failed to load members.</td>
            </tr>
        `;
    }
}

async function saveOrUpdateMember(event) {
    event.preventDefault();

    const memberId = document.getElementById("memberId").value;

    const memberData = {
        fullName: document.getElementById("fullName").value.trim(),
        email: document.getElementById("email").value.trim(),
        phoneNumber: document.getElementById("phoneNumber").value.trim(),
        companyId: document.getElementById("company").value
    };

    try {
        let response;

        if (memberId) {
            response = await fetch(`/api/members/${memberId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(memberData)
            });
        } else {
            response = await fetch("/api/members", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(memberData)
            });
        }

        if (!response.ok) {
            throw new Error(memberId ? "Failed to update member" : "Failed to save member");
        }

        showMessage(memberId ? "Member updated successfully." : "Member saved successfully.", "green");
        resetForm();
        loadMembers();
    } catch (error) {
        console.error("Error saving/updating member:", error);
        showMessage(error.message, "red");
    }
}

function editMember(id, fullName, email, phoneNumber, companyId) {
    document.getElementById("memberId").value = id;
    document.getElementById("fullName").value = fullName;
    document.getElementById("email").value = email;
    document.getElementById("phoneNumber").value = phoneNumber;
    document.getElementById("company").value = companyId;

    document.getElementById("saveMemberBtn").textContent = "Update Member";
    document.getElementById("cancelEditBtn").style.display = "inline-block";

    window.scrollTo({ top: 0, behavior: "smooth" });

    document.getElementById("saveMemberBtn").style.background = "#2563eb";
    document.getElementById("saveMemberBtn").style.color = "#ffffff";
}

function openDeleteModal(id) {
    memberIdToDelete = id;
    document.getElementById("deleteModal").classList.add("show");
}

function closeDeleteModal() {
    memberIdToDelete = null;
    document.getElementById("deleteModal").classList.remove("show");
}

async function confirmDeleteMember() {
    if (!memberIdToDelete) return;

    try {
        const response = await fetch(`/api/members/${memberIdToDelete}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Failed to delete member");
        }

        showMessage("Member deleted successfully.", "green");
        closeDeleteModal();
        loadMembers();
    } catch (error) {
        console.error("Error deleting member:", error);
        showMessage("Failed to delete member.", "red");
        closeDeleteModal();
    }
}

function resetForm() {
    document.getElementById("memberForm").reset();
    document.getElementById("memberId").value = "";

    document.getElementById("saveMemberBtn").textContent = "+ Save Member";
    document.getElementById("saveMemberBtn").style.background = "";
    document.getElementById("saveMemberBtn").style.color = "";

    document.getElementById("cancelEditBtn").style.display = "none";
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/'/g, "&#39;")
        .replace(/"/g, "&quot;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");

    document.getElementById("saveMemberBtn").textContent = "+ Save Member";
    document.getElementById("saveMemberBtn").style.background = "";
    document.getElementById("saveMemberBtn").style.color = "";
}

    function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('show');
}

    document.addEventListener('click', function (event) {

    const sidebar = document.querySelector('.sidebar');
    const menuBtn = document.querySelector('.mobile-menu-btn');

    if (
    sidebar.classList.contains('show') &&
    !sidebar.contains(event.target) &&
    !menuBtn.contains(event.target)
    ) {
    sidebar.classList.remove('show');
}
});

    document.querySelectorAll('.sidebar a').forEach(link => {
    link.addEventListener('click', () => {
        document.querySelector('.sidebar').classList.remove('show');
    });
});
