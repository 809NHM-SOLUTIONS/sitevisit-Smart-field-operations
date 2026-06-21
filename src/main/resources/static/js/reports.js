/**
 * reports.js - Reports Management Frontend Logic
 * Handles CRUD operations for reports via REST API
 */

const API_URL = '/api/reports';

// ===== CSRF HELPERS (Spring Security) =====
function getCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]')?.content;
    if (meta) return meta;
    const input = document.querySelector('input[name="_csrf"]')?.value;
    if (input) return input;
    const cookies = document.cookie.split(';');
    for (let c of cookies) {
        const [name, val] = c.trim().split('=');
        if (name === 'XSRF-TOKEN') return decodeURIComponent(val);
    }
    return null;
}

function getAuthHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = getCsrfToken();
    if (token) headers['X-CSRF-TOKEN'] = token;
    return headers;
}

// ===== AUTH CHECK =====
function checkAuth() {
    if (localStorage.getItem('isLoggedIn') !== 'true') {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// ===== MODAL FUNCTIONS =====
function openModal(report = null) {
    const modal = document.getElementById('reportModal');
    const form = document.getElementById('reportForm');
    const titleEl = document.getElementById('modalTitle');
    
    if (report) {
        // Edit mode
        titleEl.textContent = 'Edit Report';
        document.getElementById('reportId').value = report.id || '';
        document.getElementById('title').value = report.title || '';
        document.getElementById('siteVisitId').value = report.siteVisitId || '';
        document.getElementById('summary').value = report.summary || '';
        document.getElementById('findings').value = report.findings || '';
        document.getElementById('issues').value = report.issues || '';
        document.getElementById('actionsTaken').value = report.actionsTaken || '';
        document.getElementById('recommendations').value = report.recommendations || '';
        document.getElementById('notes').value = report.notes || '';
    } else {
        // Create mode
        titleEl.textContent = 'Create New Report';
        form.reset();
        document.getElementById('reportId').value = '';
    }
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('reportModal').style.display = 'none';
}

// Close modal on outside click
document.getElementById('reportModal')?.addEventListener('click', (e) => {
    if (e.target.id === 'reportModal') closeModal();
});

// ===== VIEW REPORT MODAL =====
function viewReport(id) {
    fetch(`${API_URL}/${id}`)
        .then(res => {
            if (!res.ok) throw new Error('Failed to load report');
            return res.json();
        })
        .then(r => {
            const modal = document.createElement('div');
            modal.id = 'viewModal';
            modal.className = 'modal';
            modal.style.cssText = 'display:flex;position:fixed;z-index:1000;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,0.5);overflow-y:auto;align-items:center;justify-content:center;';
            
            modal.innerHTML = `
                <div style="background:white;margin:3% auto;padding:25px;width:70%;max-width:800px;border-radius:12px;box-shadow:0 5px 15px rgba(0,0,0,0.3);">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;padding-bottom:15px;border-bottom:1px solid #e5e7eb;">
                        <h3 style="margin:0;color:#1a3f3a;">📄 Report #${r.id}</h3>
                        <button onclick="closeViewModal()" style="background:none;border:none;font-size:24px;cursor:pointer;color:#6b7280;">&times;</button>
                    </div>
                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:15px;margin-bottom:20px;">
                        <div><strong style="color:#6b7280;font-size:13px;">Title</strong><p style="margin:5px 0 0 0;font-weight:600;color:#1a3f3a;">${escapeHtml(r.title)}</p></div>
                        <div><strong style="color:#6b7280;font-size:13px;">Site Visit ID</strong><p style="margin:5px 0 0 0;font-weight:600;color:#1a3f3a;">${r.siteVisitId || '-'}</p></div>
                        <div><strong style="color:#6b7280;font-size:13px;">Created</strong><p style="margin:5px 0 0 0;font-weight:600;color:#1a3f3a;">${formatDate(r.createdAt)}</p></div>
                        <div><strong style="color:#6b7280;font-size:13px;">Updated</strong><p style="margin:5px 0 0 0;font-weight:600;color:#1a3f3a;">${r.updatedAt ? formatDate(r.updatedAt) : '-'}</p></div>
                    </div>
                    <div style="margin-bottom:15px;"><strong style="color:#6b7280;font-size:13px;">Summary</strong><p style="margin:5px 0 0 0;line-height:1.6;color:#374151;white-space:pre-wrap;">${escapeHtml(r.summary)}</p></div>
                    ${r.findings ? `<div style="margin-bottom:15px;"><strong style="color:#6b7280;font-size:13px;">Findings</strong><p style="margin:5px 0 0 0;line-height:1.6;color:#374151;white-space:pre-wrap;">${escapeHtml(r.findings)}</p></div>` : ''}
                    ${r.issues ? `<div style="margin-bottom:15px;"><strong style="color:#6b7280;font-size:13px;">Issues</strong><p style="margin:5px 0 0 0;line-height:1.6;color:#374151;white-space:pre-wrap;">${escapeHtml(r.issues)}</p></div>` : ''}
                    ${r.actionsTaken ? `<div style="margin-bottom:15px;"><strong style="color:#6b7280;font-size:13px;">Actions Taken</strong><p style="margin:5px 0 0 0;line-height:1.6;color:#374151;white-space:pre-wrap;">${escapeHtml(r.actionsTaken)}</p></div>` : ''}
                    ${r.recommendations ? `<div style="margin-bottom:15px;"><strong style="color:#6b7280;font-size:13px;">Recommendations</strong><p style="margin:5px 0 0 0;line-height:1.6;color:#374151;white-space:pre-wrap;">${escapeHtml(r.recommendations)}</p></div>` : ''}
                    ${r.notes ? `<div style="margin-bottom:15px;"><strong style="color:#6b7280;font-size:13px;">Notes</strong><p style="margin:5px 0 0 0;line-height:1.6;color:#374151;white-space:pre-wrap;">${escapeHtml(r.notes)}</p></div>` : ''}
                    <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:25px;padding-top:15px;border-top:1px solid #e5e7eb;">
                        <button onclick="closeViewModal()" style="background:#6b7280;color:white;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-weight:600;">Close</button>
                        <button onclick="closeViewModal(); openModal(${JSON.stringify(r).replace(/"/g, '&quot;')})" style="background:#2563eb;color:white;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-weight:600;">✏️ Edit</button>
                        <button onclick="closeViewModal(); downloadPdf(${r.id})" style="background:#d97706;color:white;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-weight:600;">📄 Download PDF</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            modal.addEventListener('click', (e) => { if (e.target === modal) closeViewModal(); });
        })
        .catch(err => { console.error('Error viewing report:', err); alert('❌ Failed to load report details'); });
}

function closeViewModal() {
    const modal = document.getElementById('viewModal');
    if (modal) modal.remove();
}

// ===== FETCH & DISPLAY REPORTS - ✅ NO ICONS, LIGHT-BLACK TEXT =====
async function fetchReports() {
    const tbody = document.getElementById('reportsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem;">
        <i class='bx bx-loader-alt bx-spin' style="font-size:24px;"></i> Loading...
    </td></tr>`;

    try {
        const res = await fetch(API_URL);
        if (res.status === 401 || res.status === 403) {
            window.location.href = '/login';
            return;
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        
        const reports = await res.json();
        tbody.innerHTML = '';
        
        if (reports.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:#6b7280;">
                No reports yet. Create one above!
            </td></tr>`;
            return;
        }
        
        reports.forEach(r => {
            const row = document.createElement('tr');
            
            // ✅ UPDATED: Action buttons with NO icons, light-black text (#333333)
            row.innerHTML = `
                <td><strong>#${r.id}</strong></td>
                <td>${escapeHtml(r.title)}</td>
                <td>${escapeHtml(r.summary?.substring(0, 50) + '...' || '-')}</td>
                <td>${formatDate(r.createdAt)}</td>
                <td><span class="status completed">Submitted</span></td>
                <td>
                    <button class="action-btn" style="
                        background:#CBA345;
                        color:#333333;
                        border:none;
                        padding:8px 16px;
                        border-radius:8px;
                        cursor:pointer;
                        font-weight:700;
                        font-size:13px;
                        margin-right:6px;
                        transition:all 0.2s;
                    " onmouseover="this.style.background='#b8933d';this.style.transform='translateY(-1px)';" 
                      onmouseout="this.style.background='#CBA345';this.style.transform='translateY(0)';"
                      onclick="viewReport(${r.id})">View</button>
                    
                    <button class="action-btn" style="
                        background:#CBA345;
                        color:#333333;
                        border:none;
                        padding:8px 16px;
                        border-radius:8px;
                        cursor:pointer;
                        font-weight:700;
                        font-size:13px;
                        margin-right:6px;
                        transition:all 0.2s;
                    " onmouseover="this.style.background='#b8933d';this.style.transform='translateY(-1px)';" 
                      onmouseout="this.style.background='#CBA345';this.style.transform='translateY(0)';"
                      onclick="editReport(${r.id})">Edit</button>
                    
                    <button class="action-btn" style="
                        background:#CBA345;
                        color:#333333;
                        border:none;
                        padding:8px 16px;
                        border-radius:8px;
                        cursor:pointer;
                        font-weight:700;
                        font-size:13px;
                        margin-right:6px;
                        transition:all 0.2s;
                    " onmouseover="this.style.background='#b8933d';this.style.transform='translateY(-1px)';" 
                      onmouseout="this.style.background='#CBA345';this.style.transform='translateY(0)';"
                      onclick="deleteReport(${r.id})">Delete</button>
                    
                    <button class="action-btn" style="
                        background:#CBA345;
                        color:#333333;
                        border:none;
                        padding:8px 16px;
                        border-radius:8px;
                        cursor:pointer;
                        font-weight:700;
                        font-size:13px;
                        transition:all 0.2s;
                    " onmouseover="this.style.background='#b8933d';this.style.transform='translateY(-1px)';" 
                      onmouseout="this.style.background='#CBA345';this.style.transform='translateY(0)';"
                      onclick="downloadPdf(${r.id})">PDF</button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
    } catch (err) {
        console.error('Error fetching reports:', err);
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:#dc2626;">
            ❌ Failed to load reports
        </td></tr>`;
    }
}

// ===== EDIT REPORT (Fetch by ID then open modal) =====
async function editReport(id) {
    try {
        const res = await fetch(`${API_URL}/${id}`);
        if (!res.ok) throw new Error('Failed to load report');
        const report = await res.json();
        openModal(report);
    } catch (err) {
        console.error('Error loading report for edit:', err);
        alert('❌ Failed to load report details');
    }
}

// ===== CREATE / UPDATE REPORT =====
document.getElementById('reportForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!checkAuth()) return;
    
    const id = document.getElementById('reportId').value;
    
    const reportData = {
        title: document.getElementById('title').value.trim(),
        siteVisitId: document.getElementById('siteVisitId').value 
            ? parseInt(document.getElementById('siteVisitId').value) 
            : null,
        siteVisitRef: document.getElementById('siteVisitRef')?.value?.trim() || null,
        summary: document.getElementById('summary').value.trim(),
        findings: document.getElementById('findings').value.trim(),
        issues: document.getElementById('issues').value.trim(),
        actionsTaken: document.getElementById('actionsTaken').value.trim(),
        recommendations: document.getElementById('recommendations').value.trim(),
        notes: document.getElementById('notes').value.trim()
    };
    
    if (!reportData.title || !reportData.summary) {
        alert('Please fill required fields: Title and Summary');
        return;
    }
    
    try {
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_URL}/${id}` : API_URL;
        
        console.log(`Sending ${method} to ${url}:`, reportData);
        
        const res = await fetch(url, {
            method: method,
            headers: getAuthHeaders(),
            body: JSON.stringify(reportData),
            credentials: 'same-origin'
        });
        
        console.log('Response status:', res.status);
        
        if (res.status === 401 || res.status === 403) {
            alert('⚠️ Session expired. Please login again.');
            window.location.href = '/login';
            return;
        }
        
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Server error:', errorText);
            throw new Error(`Save failed: ${res.status}`);
        }
        
        const saved = await res.json();
        console.log('✅ Saved report:', saved);
        
        alert(`Report ${id ? 'updated' : 'created'} successfully!`);
        closeModal();
        document.getElementById('reportForm').reset();
        fetchReports();
        
        // ✅ AUTO-VIEW: Open view modal after successful save
        if (saved.id) {
            setTimeout(() => viewReport(saved.id), 300);
        }
        
    } catch (err) {
        console.error('❌ Error saving report:', err);
        alert(`Failed to save: ${err.message}\n\nCheck browser console for details.`);
    }
});

// ===== DELETE REPORT =====
async function deleteReport(id) {
    if (!confirm('Delete this report? This cannot be undone.')) return;
    
    try {
        const res = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
            credentials: 'same-origin'
        });
        
        if (res.status === 401 || res.status === 403) {
            alert('⚠️ Session expired. Please login again.');
            window.location.href = '/login';
            return;
        }
        if (!res.ok) throw new Error('Delete failed');
        
        alert('✅ Report deleted');
        fetchReports();
        
    } catch (err) {
        console.error('❌ Error deleting report:', err);
        alert('Failed to delete report');
    }
}

// ===== DOWNLOAD PDF =====
function downloadPdf(id) {
    window.open(`${API_URL}/${id}/pdf`, '_blank');
}

// ===== HELPERS =====
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(iso) {
    if (!iso) return '-';
    return new Date(iso).toLocaleDateString('en-ZA', {
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ===== GLOBAL EXPOSURE =====
window.fetchReports = fetchReports;
window.openModal = openModal;
window.closeModal = closeModal;
window.viewReport = viewReport;
window.closeViewModal = closeViewModal;
window.editReport = editReport;
window.deleteReport = deleteReport;
window.downloadPdf = downloadPdf;

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    fetchReports();
    console.log('✅ Reports page initialized');
});

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
