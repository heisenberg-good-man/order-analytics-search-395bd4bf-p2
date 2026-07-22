const API_BASE = 'http://localhost:8080/api';

async function request(url, options = {}) {
    const fullUrl = url.startsWith('http') ? url : API_BASE + url;
    const resp = await fetch(fullUrl, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    });
    const data = await resp.json();
    if (data.code !== 200) {
        throw new Error(data.message || '请求失败');
    }
    return data.data;
}

const PROFESSION_MAP = {
    BABYSITTER: '保姆',
    REPAIRMAN: '维修工',
    MATERNAL_NURSE: '月嫂',
    CLEANER: '保洁',
    COOK: '厨师',
    DRIVER: '司机',
    NURSING_WORKER: '护工',
    TUTOR: '家教'
};

const STATUS_MAP = {
    NOT_SUBMITTED: { label: '未提交', class: 'status-gray' },
    PENDING: { label: '待审核', class: 'status-yellow' },
    APPROVED: { label: '已认证', class: 'status-green' },
    REJECTED: { label: '已驳回', class: 'status-red' }
};

function formatTime(isoStr) {
    if (!isoStr) return '-';
    const d = new Date(isoStr);
    const pad = n => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function maskPhone(phone) {
    if (!phone || phone.length < 11) return phone || '-';
    return phone.slice(0, 3) + '****' + phone.slice(7);
}

function maskIdCard(id) {
    if (!id || id.length < 10) return id || '-';
    return id.slice(0, 4) + '**********' + id.slice(-4);
}

function getProfessionLabel(val) {
    return PROFESSION_MAP[val] || val;
}

function getStatusInfo(status) {
    return STATUS_MAP[status] || { label: status, class: 'status-gray' };
}

function showToast(msg, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 2500);
}

function showModal(title, contentHtml, onConfirm, confirmText = '确定') {
    const modal = document.createElement('div');
    modal.className = 'modal-mask';
    modal.innerHTML = `
        <div class="modal">
            <div class="modal-header">${title}</div>
            <div class="modal-body">${contentHtml}</div>
            <div class="modal-footer">
                <button class="btn btn-default" data-action="cancel">取消</button>
                <button class="btn btn-primary" data-action="confirm">${confirmText}</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);

    modal.querySelector('[data-action="cancel"]').onclick = () => modal.remove();
    modal.querySelector('[data-action="confirm"]').onclick = async () => {
        if (onConfirm) {
            const result = await onConfirm(modal);
            if (result !== false) modal.remove();
        } else {
            modal.remove();
        }
    };
    modal.onclick = (e) => {
        if (e.target === modal) modal.remove();
    };
    return modal;
}
