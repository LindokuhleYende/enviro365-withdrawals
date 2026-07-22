(() => {
    const API_BASE = '/api';

    const state = {
        investors: [],
        currentInvestorId: null,
        products: [],
    };

    const el = {
        investorSelect: document.getElementById('investorSelect'),
        investorMeta: document.getElementById('investorMeta'),
        totalBalance: document.getElementById('totalBalance'),
        productCount: document.getElementById('productCount'),
        productsTableBody: document.querySelector('#productsTable tbody'),
        productSelect: document.getElementById('productSelect'),
        amountInput: document.getElementById('amountInput'),
        notesInput: document.getElementById('notesInput'),
        withdrawalForm: document.getElementById('withdrawalForm'),
        formError: document.getElementById('formError'),
        historyTableBody: document.querySelector('#historyTable tbody'),
        historyEmpty: document.getElementById('historyEmpty'),
        refreshHistoryBtn: document.getElementById('refreshHistoryBtn'),
        downloadCsvBtn: document.getElementById('downloadCsvBtn'),
        filterStatus: document.getElementById('filterStatus'),
        filterFrom: document.getElementById('filterFrom'),
        filterTo: document.getElementById('filterTo'),
        toast: document.getElementById('toast'),
    };

    const currency = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR' });
    const dateFmt = (iso) => {
        const d = new Date(iso);
        return d.toLocaleString('en-ZA', { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' });
    };

    function showToast(message, type = 'success') {
        el.toast.textContent = message;
        el.toast.className = `toast ${type}`;
        el.toast.classList.remove('hidden');
        setTimeout(() => el.toast.classList.add('hidden'), 4000);
    }

    async function apiFetch(path, options = {}) {
        const res = await fetch(`${API_BASE}${path}`, {
            headers: { 'Content-Type': 'application/json' },
            ...options,
        });
        if (!res.ok) {
            let message = `Request failed (${res.status})`;
            try {
                const body = await res.json();
                if (body.details && body.details.length) {
                    message = body.details.join(' ');
                } else if (body.message) {
                    message = body.message;
                }
            } catch (_) { /* non-JSON error body */ }
            throw new Error(message);
        }
        if (res.status === 204) return null;
        return res.json();
    }

    // ---------- Investors & Portfolio ----------

    async function loadInvestors() {
        state.investors = await apiFetch('/investors');
        el.investorSelect.innerHTML = state.investors
            .map((inv) => `<option value="${inv.id}">${inv.fullName}</option>`)
            .join('');
        if (state.investors.length > 0) {
            state.currentInvestorId = state.investors[0].id;
            el.investorSelect.value = state.currentInvestorId;
            await refreshAll();
        }
    }

    async function loadPortfolio(investorId) {
        const portfolio = await apiFetch(`/investors/${investorId}/portfolio`);
        state.products = portfolio.products;

        el.investorMeta.textContent = `${portfolio.email} · Age ${portfolio.age}`;
        el.totalBalance.textContent = currency.format(portfolio.totalBalance);
        el.productCount.textContent = portfolio.products.length;

        el.productsTableBody.innerHTML = portfolio.products
            .map((p) => `
                <tr>
                    <td>${p.productName}</td>
                    <td>${formatType(p.type)}</td>
                    <td>${currency.format(p.balance)}</td>
                </tr>
            `).join('');

        el.productSelect.innerHTML = portfolio.products
            .map((p) => `<option value="${p.id}" data-balance="${p.balance}">${p.productName} (${currency.format(p.balance)})</option>`)
            .join('');
    }

    function formatType(type) {
        return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
    }

    // ---------- Withdrawal History ----------

    async function loadHistory(investorId) {
        const notices = await apiFetch(`/withdrawals?investorId=${investorId}`);
        if (notices.length === 0) {
            el.historyTableBody.innerHTML = '';
            el.historyEmpty.classList.remove('hidden');
            return;
        }
        el.historyEmpty.classList.add('hidden');
        el.historyTableBody.innerHTML = notices.map((n) => `
            <tr>
                <td>${dateFmt(n.requestDate)}</td>
                <td>${n.productName}</td>
                <td>${currency.format(n.amountRequested)}</td>
                <td>${currency.format(n.balanceBeforeWithdrawal)}</td>
                <td>${currency.format(n.balanceAfterWithdrawal)}</td>
                <td><span class="status-pill status-${n.status}">${n.status}</span></td>
                <td>${n.notes ? escapeHtml(n.notes) : '<span class="muted">—</span>'}</td>
            </tr>
        `).join('');
    }

    function escapeHtml(str) {
        const d = document.createElement('div');
        d.textContent = str;
        return d.innerHTML;
    }

    // ---------- Withdrawal Form ----------

    async function submitWithdrawal(ev) {
        ev.preventDefault();
        el.formError.classList.add('hidden');

        const productId = Number(el.productSelect.value);
        const amount = Number(el.amountInput.value);
        const notes = el.notesInput.value.trim();

        if (!productId) {
            return showFormError('Please select a product.');
        }
        if (!amount || amount <= 0) {
            return showFormError('Please enter a valid withdrawal amount.');
        }

        const submitBtn = el.withdrawalForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;

        try {
            await apiFetch('/withdrawals', {
                method: 'POST',
                body: JSON.stringify({
                    investorId: state.currentInvestorId,
                    productId,
                    amount,
                    notes: notes || null,
                }),
            });
            showToast('Withdrawal notice submitted successfully.', 'success');
            el.withdrawalForm.reset();
            await refreshAll();
        } catch (err) {
            showFormError(err.message);
        } finally {
            submitBtn.disabled = false;
        }
    }

    function showFormError(message) {
        el.formError.textContent = message;
        el.formError.classList.remove('hidden');
    }

    // ---------- CSV Export ----------

    function downloadCsv() {
        const params = new URLSearchParams({ investorId: state.currentInvestorId });
        if (el.filterStatus.value) params.set('status', el.filterStatus.value);
        if (el.filterFrom.value) params.set('from', `${el.filterFrom.value}T00:00:00`);
        if (el.filterTo.value) params.set('to', `${el.filterTo.value}T23:59:59`);

        // Trigger a browser download via a temporary anchor tag.
        const url = `${API_BASE}/reports/withdrawals/csv?${params.toString()}`;
        const a = document.createElement('a');
        a.href = url;
        a.download = '';
        document.body.appendChild(a);
        a.click();
        a.remove();
    }

    // ---------- Orchestration ----------

    async function refreshAll() {
        try {
            await Promise.all([
                loadPortfolio(state.currentInvestorId),
                loadHistory(state.currentInvestorId),
            ]);
        } catch (err) {
            showToast(err.message, 'error');
        }
    }

    el.investorSelect.addEventListener('change', async (ev) => {
        state.currentInvestorId = Number(ev.target.value);
        await refreshAll();
    });

    el.withdrawalForm.addEventListener('submit', submitWithdrawal);
    el.refreshHistoryBtn.addEventListener('click', () => loadHistory(state.currentInvestorId));
    el.downloadCsvBtn.addEventListener('click', downloadCsv);

    loadInvestors().catch((err) => showToast(err.message, 'error'));
})();
