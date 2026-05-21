// companies.js - Company Management Frontend Logic

const API_URL = '/api/companies';

// ===== INITIALIZE ON PAGE LOAD =====
document.addEventListener('DOMContentLoaded', () => {

    fetchCompanies();

    const companyForm =
        document.getElementById('companyForm');

    if (companyForm) {

        companyForm.addEventListener(
            'submit',
            handleFormSubmit
        );
    }

    const viewAllBtn =
        document.getElementById('viewAllBtn');

    if (viewAllBtn) {

        viewAllBtn.addEventListener('click', () => {

            showAll = !showAll;

            renderCompanies();
        });
    }
});

// ===== GLOBAL VARIABLES =====
let allCompanies = [];
let showAll = false;

// ===== CUSTOM MODAL =====
function openCustomModal(
    title,
    message,
    confirmText,
    confirmClass,
    onConfirm
) {

    const modal =
        document.getElementById('customModal');

    const modalTitle =
        document.getElementById('modalTitle');

    const modalMessage =
        document.getElementById('modalMessage');

    const confirmBtn =
        document.getElementById('modalConfirmBtn');

    modalTitle.textContent = title;

    modalMessage.innerHTML = message;

    confirmBtn.textContent = confirmText;

    confirmBtn.className =
        `btn ${confirmClass}`;

    modal.style.display = 'flex';

    confirmBtn.onclick = () => {

        modal.style.display = 'none';

        if (onConfirm) {
            onConfirm();
        }
    };
}

function closeCustomModal() {

    const modal =
        document.getElementById('customModal');

    modal.style.display = 'none';
}

// ===== FETCH COMPANIES =====
async function fetchCompanies() {

    try {

        const response =
            await fetch(API_URL);

        const data =
            await response.json();

        allCompanies = data;

        renderCompanies();

    } catch (err) {

        console.error(
            'Error fetching companies:',
            err
        );

        showToast(
            'Failed to load companies',
            false
        );
    }
}

// ===== RENDER COMPANIES =====
function renderCompanies() {

    const tbody =
        document.getElementById(
            'companyTableBody'
        );

    const viewAllBtn =
        document.getElementById(
            'viewAllBtn'
        );

    if (!tbody) return;

    tbody.innerHTML = '';

    if (!allCompanies || allCompanies.length === 0) {

        tbody.innerHTML = `
            <tr>
                <td colspan="5"
                    style="text-align:center;
                           padding:2rem;">
                    No companies registered yet.
                </td>
            </tr>
        `;

        return;
    }

    const companiesToShow = showAll
        ? allCompanies
        : allCompanies.slice(0, 5);

    companiesToShow.forEach(c => {

        const row =
            document.createElement('tr');

        row.innerHTML = `
    <td>${escapeHtml(c.regNumber)}</td>

    <td>
        <strong>${escapeHtml(c.name)}</strong>
    </td>

    <td>${escapeHtml(c.email)}</td>

    <td>${escapeHtml(c.phone)}</td>

    <td>${escapeHtml(c.address)}</td>

    <td class="actions-cell">

        <div class="action-group">

            <button
                class="action-btn btn-edit"
                onclick="prepEdit(${c.id})">

                Edit

            </button>

            <button
                class="action-btn btn-delete"
                onclick="deleteCompany(${c.id})">

                Delete

            </button>

        </div>

    </td>
`;

        tbody.appendChild(row);
    });

    if (allCompanies.length <= 5) {

        viewAllBtn.style.display = 'none';

    } else {

        viewAllBtn.style.display =
            'inline-flex';

        viewAllBtn.innerText = showAll
            ? 'View Less'
            : 'View All';
    }
}

// ===== CREATE / UPDATE COMPANY =====
async function handleFormSubmit(e) {

    e.preventDefault();

    const id =
        document.getElementById('compId')
            ?.value || '';

    const companyData = {

        name:
            document.getElementById('name')
                ?.value.trim() || '',

        regNumber:
            document.getElementById('regNumber')
                ?.value.trim() || '',

        email:
            document.getElementById('email')
                ?.value.trim() || '',

        phone:
            document.getElementById('phone')
                ?.value.trim() || '',

        address:
            document.getElementById('address')
                ?.value.trim() || ''
    };

    if (
        !companyData.name ||
        !companyData.regNumber ||
        !companyData.email
    ) {

        showToast(
            'Please fill in all required fields',
            false
        );

        return;
    }

    try {

        const method =
            id ? 'PUT' : 'POST';

        const url =
            id
                ? `${API_URL}/${id}`
                : API_URL;

        const response = await fetch(url, {

            method: method,

            headers: {
                'Content-Type':
                    'application/json'
            },

            body:
                JSON.stringify(companyData)
        });

        if (!response.ok) {
            throw new Error('Save failed');
        }

        openCustomModal(

            id
                ? 'Company Updated'
                : 'Company Saved',

            id
                ? 'The company has been updated successfully.'
                : 'The company has been saved successfully.',

            'Continue',

            'btn-primary',

            async () => {

                resetForm();

                await fetchCompanies();
            }
        );

    } catch (err) {

        console.error(
            'Error saving company:',
            err
        );

        showToast(
            'Failed to save company',
            false
        );
    }
}

// ===== DELETE COMPANY =====
async function deleteCompany(id) {

    openCustomModal(

        'Delete Company',

        `
        Are you sure you want to delete this company?
        
        <p class="modal-warning">
            This action cannot be undone.
        </p>
        `,

        'Delete',

        'btn-danger',

        async () => {

            try {

                const response =
                    await fetch(
                        `${API_URL}/${id}`,
                        {
                            method: 'DELETE'
                        }
                    );

                if (!response.ok) {
                    throw new Error(
                        'Delete failed'
                    );
                }

                showToast(
                    'Company deleted successfully',
                    true
                );

                await fetchCompanies();

            } catch (err) {

                console.error(
                    'Error deleting company:',
                    err
                );

                showToast(
                    'Failed to delete company',
                    false
                );
            }
        }
    );
}

// ===== EDIT COMPANY =====
function prepEdit(id) {

    const c =
        allCompanies.find(
            company => company.id === id
        );

    if (!c) {

        showToast(
            'Company not found',
            false
        );

        return;
    }

    document.getElementById(
        'compId'
    ).value = c.id || '';

    document.getElementById(
        'name'
    ).value = c.name || '';

    document.getElementById(
        'regNumber'
    ).value = c.regNumber || '';

    document.getElementById(
        'email'
    ).value = c.email || '';

    document.getElementById(
        'phone'
    ).value = c.phone || '';

    document.getElementById(
        'address'
    ).value = c.address || '';

    const saveBtn =
        document.getElementById('saveBtn');

    if (saveBtn) {

        saveBtn.innerHTML =
            'Update Company';
    }

    openCustomModal(

        'Edit Company',

        'The selected company has been loaded into the form for editing.',

        'Continue',

        'btn-primary'
    );

    document.querySelector('.panel')
        ?.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
}

// ===== RESET FORM =====
function resetForm() {

    document.getElementById(
        'companyForm'
    )?.reset();

    document.getElementById(
        'compId'
    ).value = '';

    const saveBtn =
        document.getElementById('saveBtn');

    if (saveBtn) {

        saveBtn.innerHTML =
            'Save Company';
    }
}

// ===== TOAST =====
function showToast(message, success = true) {

    let toast =
        document.getElementById('toast');

    if (!toast) {

        toast =
            document.createElement('div');

        toast.id = 'toast';

        toast.style.position = 'fixed';
        toast.style.top = '20px';
        toast.style.right = '20px';
        toast.style.padding = '14px 18px';
        toast.style.borderRadius = '12px';
        toast.style.color = '#fff';
        toast.style.fontWeight = '600';
        toast.style.zIndex = '99999';
        toast.style.boxShadow =
            '0 10px 30px rgba(0,0,0,0.15)';

        document.body.appendChild(toast);
    }

    toast.innerText = message;

    toast.style.background = success
        ? '#16a34a'
        : '#dc2626';

    toast.style.display = 'block';

    setTimeout(() => {

        toast.style.display = 'none';

    }, 3000);
}

// ===== SECURITY =====
function escapeHtml(text) {

    if (
        text === null ||
        text === undefined
    ) return '';

    return String(text).replace(
        /[&<>"']/g,
        function (m) {

            const map = {

                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#039;'
            };

            return map[m];
        }
    );
}

// ===== GLOBAL ACCESS =====
window.deleteCompany = deleteCompany;
window.prepEdit = prepEdit;
window.fetchCompanies = fetchCompanies;
window.closeCustomModal = closeCustomModal;