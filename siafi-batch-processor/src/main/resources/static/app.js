let activeSection = 'overview';
let activeBatchesTab = 'dh';
let activeRecordsTab = 'dh';
let simulatedRowIndex = 1025;
let realPollInterval = null;
let knownNotifications = new Set();
let unreadCount = 0;

// Initialize layout
document.addEventListener('DOMContentLoaded', () => {
    if (sessionStorage.getItem('logged_in') === 'true') {
        showDashboard();
    }
    
    initDropzone();
    loadRealData();

    // Close notifications dropdown if clicking outside
    document.addEventListener('click', (e) => {
        const dropdown = document.getElementById('notif-dropdown');
        const bell = document.querySelector('.icon-bell');
        if (dropdown && dropdown.classList.contains('show') && !dropdown.contains(e.target) && !bell.contains(e.target)) {
            dropdown.classList.remove('show');
        }
    });
});

// LOGIN SYSTEM
function handleLogin(e) {
    e.preventDefault();
    const user = document.getElementById('username').value;
    sessionStorage.setItem('logged_in', 'true');
    sessionStorage.setItem('user_name', user || 'ALEX R.');
    
    document.getElementById('login-screen').style.transition = 'opacity 0.4s ease';
    document.getElementById('login-screen').style.opacity = '0';
    
    setTimeout(() => {
        document.getElementById('login-screen').style.display = 'none';
        showDashboard();
        showToast("Bem-vindo ao SIAFI, " + (user.split('@')[0].toUpperCase()));
    }, 400);
}

function handleLogout() {
    sessionStorage.clear();
    clearInterval(realPollInterval);
    document.getElementById('dashboard-app').style.display = 'none';
    document.getElementById('login-screen').style.display = 'flex';
    document.getElementById('login-screen').style.opacity = '1';
}

function showDashboard() {
    document.getElementById('dashboard-app').style.display = 'grid';
    const storedUser = sessionStorage.getItem('user_name');
    if (storedUser) {
        let name = storedUser.split('@')[0].replace('.', ' ').toUpperCase();
        document.querySelector('.user-profile span').innerText = name.length > 10 ? name.substring(0, 10) + '.' : name;
    }
    
    realPollInterval = setInterval(loadRealData, 3000);
    loadRealData();
}

// NAVIGATION
function navigate(sectionId, e) {
    if (e) e.preventDefault();
    activeSection = sectionId;
    
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
    if (e && e.currentTarget) {
        e.currentTarget.classList.add('active');
    }
    
    document.getElementById('section-title').innerText = sectionId.charAt(0).toUpperCase() + sectionId.slice(1);
    document.querySelectorAll('.section-container').forEach(el => el.style.display = 'none');
    document.getElementById(`section-${sectionId}`).style.display = 'block';

    loadRealData();
}

// BATCH AND RECORD EXPLORER INTERNAL TABS
function toggleBatchSubTab(type) {
    activeBatchesTab = type;
    document.getElementById('btn-tab-dh').classList.toggle('active', type === 'dh');
    document.getElementById('btn-tab-pf').classList.toggle('active', type === 'pf');
    loadRealData();
}

function toggleRecordsSubTab(type) {
    activeRecordsTab = type;
    document.getElementById('btn-tab-records-dh').classList.toggle('active', type === 'dh');
    document.getElementById('btn-tab-records-pf').classList.toggle('active', type === 'pf');
    
    document.getElementById('full-dh-records-table').style.display = type === 'dh' ? 'table' : 'none';
    document.getElementById('full-pf-records-table').style.display = type === 'pf' ? 'table' : 'none';
    loadRealData();
}

// REAL DATA SYNC (FROM SPRING BOOT BACKEND)
async function loadRealData() {
    if (sessionStorage.getItem('logged_in') !== 'true') return;
    
    try {
        const statusRes = await fetch('/api/siafi/status');
        if (!statusRes.ok) return;
        const status = await statusRes.json();
        
        document.getElementById('watch-folder-path').innerText = status.incomingDir || 'Initializing...';
        
        // Fetch all batch history
        const dhRes = await fetch('/api/siafi/dh-batches');
        const dhBatches = await dhRes.json();

        const pfRes = await fetch('/api/siafi/pf-batches');
        const pfBatches = await pfRes.json();

        // Calculate actual statistics from H2 DB
        let successCount = 0;
        let processingCount = 0;
        let pendingCount = 0;
        let totalDbRecords = 0;

        const allLoads = [...dhBatches, ...pfBatches];

        allLoads.forEach(b => {
            if (b.status === 'SUCCESS') successCount++;
            else if (b.status === 'PROCESSING') processingCount++;
            else if (b.status === 'FAILED' || b.status === 'PENDING') pendingCount++;
            
            if (b.detalhes) {
                totalDbRecords += b.detalhes.length;
            }
        });

        // Sync statistics cards
        document.getElementById('stat-total-processed').innerText = successCount;
        document.getElementById('stat-active-loads').innerText = processingCount;
        document.getElementById('stat-files-pending').innerText = pendingCount;
        document.getElementById('stat-database-records').innerText = totalDbRecords;

        // Render Overview Active Batch Load History (Merged & Sorted)
        renderOverviewHistory(allLoads);

        // Render Overview Database Record Explorer
        renderOverviewExplorer(dhBatches, pfBatches);

        // Pull notifications
        const notifRes = await fetch('/api/siafi/notifications');
        if (notifRes.ok) {
            const notifications = await notifRes.json();
            handleNotificationsSync(notifications);
        }

        // Sub sections
        if (activeSection === 'batches') {
            const batches = activeBatchesTab === 'dh' ? dhBatches : pfBatches;
            renderBatchesTable(batches, activeBatchesTab);
        }

        if (activeSection === 'records') {
            renderDhRecords(dhBatches);
            renderPfRecords(pfBatches);
        }

    } catch (err) {
        console.error("Error fetching REST data: ", err);
    }
}

// Render active history table in Overview
function renderOverviewHistory(loads) {
    const tbody = document.getElementById('active-history-rows');
    if (loads.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">Nenhum lote processado. Envie um arquivo para iniciar.</td></tr>`;
        return;
    }

    // Sort descending by id
    loads.sort((a, b) => b.id - a.id);
    
    // Take top 5
    const topLoads = loads.slice(0, 5);

    tbody.innerHTML = topLoads.map(b => {
        const badgeClass = b.status === 'SUCCESS' ? 'badge-success' : (b.status === 'FAILED' ? 'badge-danger' : 'badge-processing');
        const displayStatus = b.status === 'SUCCESS' ? 'Success' : (b.status === 'FAILED' ? 'Failed' : 'Processing');
        
        let timeStr = '10:30 AM';
        if (b.dataProcessamento) {
            let date = new Date(b.dataProcessamento);
            timeStr = date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        }

        return `
            <tr>
                <td class="code-font">#SIB${b.id}</td>
                <td>${b.fileName}</td>
                <td><span class="badge ${badgeClass}">${displayStatus}</span></td>
                <td>${timeStr}</td>
            </tr>
        `;
    }).join('');
}

// Render Database Record Explorer in Overview
function renderOverviewExplorer(dhBatches, pfBatches) {
    const tbody = document.getElementById('db-explorer-rows');
    let allRecords = [];

    dhBatches.forEach(b => {
        if (b.detalhes) {
            b.detalhes.forEach(d => {
                allRecords.push({
                    id: d.id,
                    name: `DH_ENTRY_${d.numDh}`,
                    val: d.vlr ? d.vlr.toFixed(2) : '0.00',
                    date: d.dtEmis || '2026-07-12'
                });
            });
        }
    });

    pfBatches.forEach(b => {
        if (b.detalhes) {
            b.detalhes.forEach(d => {
                allRecords.push({
                    id: d.id,
                    name: `PF_ENTRY_${d.numeroDocumento || d.id}`,
                    val: d.vlr ? d.vlr.toFixed(2) : '0.00',
                    date: '2026-07-12'
                });
            });
        }
    });

    if (allRecords.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Nenhum registro inserido no banco H2.</td></tr>`;
        return;
    }

    // Sort descending
    allRecords.sort((a, b) => b.id - a.id);
    const topRecords = allRecords.slice(0, 5);

    tbody.innerHTML = topRecords.map(r => {
        return `
            <tr>
                <td class="code-font">${r.id}</td>
                <td>${r.name}</td>
                <td class="code-font">${r.val}</td>
                <td><span class="badge badge-success">SUCCESS</span></td>
                <td>${r.date}</td>
            </tr>
        `;
    }).join('');
}

// NOTIFICATION LOGIC
function handleNotificationsSync(notifications) {
    let freshNotifications = [];
    
    notifications.forEach(n => {
        if (!knownNotifications.has(n.id)) {
            knownNotifications.add(n.id);
            if (!n.read) {
                unreadCount++;
                freshNotifications.push(n);
            }
        }
    });

    freshNotifications.forEach(n => {
        showToast(n.message, n.type === 'ERROR');
    });

    updateNotificationBadge();
}

function updateNotificationBadge() {
    const badge = document.getElementById('notif-badge');
    if (unreadCount > 0) {
        badge.innerText = unreadCount;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

function toggleNotificationDropdown(e) {
    if (e) e.stopPropagation();
    const dropdown = document.getElementById('notif-dropdown');
    dropdown.classList.toggle('show');
    
    if (dropdown.classList.contains('show')) {
        renderNotificationsDropdown();
    }
}

async function renderNotificationsDropdown() {
    try {
        const res = await fetch('/api/siafi/notifications');
        const notifs = await res.json();
        const listContainer = document.getElementById('notif-list');
        
        if (notifs.length === 0) {
            listContainer.innerHTML = `<div class="notif-empty">Nenhuma notificação nova</div>`;
            return;
        }

        notifs.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

        listContainer.innerHTML = notifs.map(n => {
            const date = new Date(n.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', second:'2-digit'});
            return `
                <div class="notif-item ${n.type}">
                    <div style="font-weight: 600;">${n.message}</div>
                    <div class="notif-time">${date}</div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error("Failed to render notifications: ", err);
    }
}

async function markAllNotificationsAsRead(e) {
    if (e) e.stopPropagation();
    try {
        await fetch('/api/siafi/notifications/read', { method: 'POST' });
        unreadCount = 0;
        updateNotificationBadge();
        renderNotificationsDropdown();
        showToast("Notificações marcadas como lidas.");
    } catch (err) {
        console.error(err);
    }
}

// SIMULATION LAB SCENARIOS
let simulatedActiveJob = null;

function triggerSimulation(status) {
    if (status === 'PROCESSING') {
        if (simulatedActiveJob) {
            showToast("Um job de simulação já está em andamento!", true);
            return;
        }

        injectSimulatedNotification(`Iniciando simulação de ingestão: sim_load_${simulatedRowIndex}.xml`, 'INFO');
        
        let activeLoads = parseInt(document.getElementById('stat-active-loads').innerText) || 0;
        document.getElementById('stat-active-loads').innerText = activeLoads + 1;
        
        logActivity("[SIMULATOR] Recebido novo arquivo xml: sim_load_" + simulatedRowIndex + ".xml");

        // Add row manually
        const historyTable = document.getElementById('active-history-rows');
        const newRow = document.createElement('tr');
        newRow.id = `sim-row-${simulatedRowIndex}`;
        newRow.innerHTML = `
            <td class="code-font">#SIB${simulatedRowIndex}</td>
            <td>sim_load_${simulatedRowIndex}.xml</td>
            <td><span class="badge badge-processing" id="sim-badge-${simulatedRowIndex}">Processing</span></td>
            <td>${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
        `;
        historyTable.insertBefore(newRow, historyTable.firstChild);

        document.getElementById('stat-progress-bar').style.width = '0%';
        document.getElementById('stat-progress-percent').innerText = '0%';
        
        let progress = 0;
        simulatedActiveJob = setInterval(() => {
            progress += 17;
            if (progress >= 85) {
                progress = 85;
                clearInterval(simulatedActiveJob);
                simulatedActiveJob = null;
                logActivity("[SIMULATOR] Leitura de chunks concluída. Aguardando comando de commit...");
            }
            document.getElementById('stat-progress-bar').style.width = progress + '%';
            document.getElementById('stat-progress-percent').innerText = progress + '%';
        }, 600);

        simulatedRowIndex++;
    } else {
        let targetId = simulatedRowIndex - 1;
        let badge = document.getElementById(`sim-badge-${targetId}`);
        if (!badge) {
            showToast("Nenhum job em andamento para finalizar. Inicie uma Ingestão primeiro!", true);
            return;
        }

        if (simulatedActiveJob) {
            clearInterval(simulatedActiveJob);
            simulatedActiveJob = null;
        }

        let activeLoads = parseInt(document.getElementById('stat-active-loads').innerText) || 0;
        if (activeLoads > 0) document.getElementById('stat-active-loads').innerText = activeLoads - 1;

        if (status === 'SUCCESS') {
            badge.className = 'badge badge-success';
            badge.innerText = 'Success';
            document.getElementById('stat-progress-bar').style.width = '100%';
            document.getElementById('stat-progress-percent').innerText = '100%';
            
            let processed = parseInt(document.getElementById('stat-total-processed').innerText) || 0;
            document.getElementById('stat-total-processed').innerText = processed + 1;
            
            let dbRecords = parseInt(document.getElementById('stat-database-records').innerText) || 0;
            document.getElementById('stat-database-records').innerText = dbRecords + 5;

            logActivity(`[SIMULATOR] Job #SIB${targetId} finalizado com sucesso. 5 registros inseridos.`);
            injectSimulatedNotification(`Lote #${targetId} processado com sucesso via Spring Batch!`, 'SUCCESS');

            const explorerTable = document.getElementById('db-explorer-rows');
            explorerTable.innerHTML = `
                <tr>
                    <td class="code-font">${targetId}</td>
                    <td>FINANCIAL_ENTRY_${targetId}</td>
                    <td class="code-font">${(Math.random()*10000).toFixed(2)}</td>
                    <td><span class="badge badge-success">SUCCESS</span></td>
                    <td>${new Date().toISOString().slice(0, 10)}</td>
                </tr>
            ` + explorerTable.innerHTML;

        } else if (status === 'PENDING') {
            badge.className = 'badge badge-warning';
            badge.innerText = 'Pending';
            logActivity(`[SIMULATOR] Job #SIB${targetId} marcado como pendente: aguardando liberação do ordenador.`);
            injectSimulatedNotification(`Lote #${targetId} pendente de autorização no SIAFI.`, 'INFO');
        } else if (status === 'FAILED') {
            badge.className = 'badge badge-danger';
            badge.innerText = 'Failed';
            
            let errors = parseInt(document.getElementById('stat-processing-errors').innerText) || 0;
            document.getElementById('stat-processing-errors').innerText = errors + 1;

            logActivity(`[SIMULATOR] Job #SIB${targetId} FALHOU. Causa: UG emissora sem saldo.`);
            injectSimulatedNotification(`Lote #${targetId} FALHOU: Limite de saque insuficiente.`, 'ERROR');
        }
    }
}

// Simulated Inject notification
function injectSimulatedNotification(message, type) {
    const id = "sim-notif-" + Math.random().toString(36).substr(2, 9);
    knownNotifications.add(id);
    unreadCount++;
    updateNotificationBadge();
    showToast(message, type === 'ERROR');

    const dropdown = document.getElementById('notif-dropdown');
    if (dropdown.classList.contains('show')) {
        renderNotificationsDropdown();
    }
}

// LOGGING UTILITY
function logActivity(text) {
    const logs = document.getElementById('full-logs-viewer');
    if (logs) {
        logs.innerHTML += `\n[${new Date().toLocaleTimeString()}] ${text}`;
        logs.scrollTop = logs.scrollHeight;
    }
}

// DRAG AND DROP HANDLERS
function initDropzone() {
    const dropzone = document.getElementById('file-dropzone');
    const input = document.getElementById('file-input');
    const browseBtn = document.getElementById('browse-btn');

    browseBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        input.click();
    });
    dropzone.addEventListener('click', () => input.click());

    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dragover');
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dragover');
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
        if (e.dataTransfer.files.length > 0) {
            handleFileUpload(e.dataTransfer.files[0]);
        }
    });

    input.addEventListener('change', () => {
        if (input.files.length > 0) {
            handleFileUpload(input.files[0]);
        }
    });
}

// Upload file to Backend
async function handleFileUpload(file) {
    showToast("Fazendo upload de " + file.name + "...");
    
    let activeLoads = parseInt(document.getElementById('stat-active-loads').innerText) || 0;
    document.getElementById('stat-active-loads').innerText = activeLoads + 1;
    
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/siafi/upload', {
            method: 'POST',
            body: formData
        });
        
        let activeLoadsAfter = parseInt(document.getElementById('stat-active-loads').innerText) || 0;
        if (activeLoadsAfter > 0) document.getElementById('stat-active-loads').innerText = activeLoadsAfter - 1;

        if (response.ok) {
            showToast("Arquivo importado com sucesso via Spring Batch!");
            loadRealData();
        } else {
            const errResult = await response.json();
            showToast("Erro no processamento batch: " + (errResult.error || "Formato inválido"), true);
        }
    } catch (err) {
        showToast("Erro de rede ao processar arquivo.", true);
    }
}

// Clear Database history
async function triggerClearDb() {
    if (confirm("Deseja realmente limpar todo o histórico de integrações do banco H2?")) {
        try {
            await fetch('/api/siafi/clear', { method: 'DELETE' });
            showToast("Histórico limpo!");
            loadRealData();
        } catch (err) {
            showToast("Falha ao limpar histórico.", true);
        }
    }
}

// RENDERERS FOR DTO TABLES
function renderBatchesTable(batches, tabType) {
    const tbody = document.getElementById('full-batches-rows');
    if (batches.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" style="text-align: center; color: var(--text-muted);">Nenhum lote carregado para o layout selecionado.</td></tr>`;
        return;
    }

    batches.sort((a, b) => b.id - a.id);
    tbody.innerHTML = batches.map(b => {
        const badgeClass = b.status === 'SUCCESS' ? 'badge-success' : (b.status === 'FAILED' ? 'badge-danger' : 'badge-warning');
        return `
            <tr>
                <td class="code-font">#${b.id}</td>
                <td style="font-weight: 500;">${b.fileName}</td>
                <td class="code-font">${b.codigoLayout}</td>
                <td class="code-font">${b.sequencialGeracao || 'N/A'}</td>
                <td>${b.dataGeracao || 'N/A'}</td>
                <td class="code-font">${b.ugResponsavel || 'N/A'}</td>
                <td style="text-align: center;">${b.quantidadeDetalhesXml}</td>
                <td style="text-align: center;">${b.quantidadeDetalhesProcessados}</td>
                <td><span class="badge ${badgeClass}">${b.status}</span></td>
                <td>
                    <button class="log-viewer-btn" onclick="openLogs('Lote #${b.id} - ${b.fileName}', \`${escapeLogs(b.logProcessamento)}\`)">Exibir Logs</button>
                </td>
            </tr>
        `;
    }).join('');
}

function renderDhRecords(batches) {
    const tbody = document.getElementById('full-dh-records-rows');
    let rows = [];
    batches.forEach(b => {
        if (b.detalhes) {
            b.detalhes.forEach(d => {
                rows.push(`
                    <tr>
                        <td class="code-font">#${b.id}</td>
                        <td class="code-font">${d.codUgEmit || 'N/A'}</td>
                        <td class="code-font" style="color: var(--primary);">${d.numDh || 'N/A'}</td>
                        <td style="font-weight: 600;">R$ ${d.vlr ? d.vlr.toFixed(2) : '0.00'}</td>
                        <td>${d.codCredorDevedor || 'N/A'}</td>
                        <td style="font-size: 0.8rem;">${d.txtProcesso || 'N/A'}</td>
                        <td>${d.dtVenc || 'N/A'}</td>
                    </tr>
                `);
            });
        }
    });

    tbody.innerHTML = rows.length === 0 ? 
        `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Nenhum detalhe de Documento Hábil no banco.</td></tr>` : 
        rows.join('');
}

function renderPfRecords(batches) {
    const tbody = document.getElementById('full-pf-records-rows');
    let rows = [];
    batches.forEach(b => {
        if (b.detalhes) {
            b.detalhes.forEach(d => {
                rows.push(`
                    <tr>
                        <td class="code-font">#${b.id}</td>
                        <td class="code-font" style="color: var(--secondary);">${d.tipoPf || 'N/A'}</td>
                        <td class="code-font">${d.codUgEmit || 'N/A'}</td>
                        <td class="code-font">${d.codUgFavorecida || 'N/A'}</td>
                        <td style="font-weight: 600;">R$ ${d.vlr ? d.vlr.toFixed(2) : '0.00'}</td>
                        <td class="code-font">${d.codFontRecur || 'N/A'}</td>
                        <td class="code-font">${d.numeroDocumento || 'N/A'}</td>
                    </tr>
                `);
            });
        }
    });

    tbody.innerHTML = rows.length === 0 ? 
        `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Nenhum detalhe de Programação Financeira no banco.</td></tr>` : 
        rows.join('');
}

// Side Logs Drawer control
function openLogs(title, log) {
    document.getElementById('drawer-subtitle').innerText = title;
    document.getElementById('drawer-content').innerText = log || "Não há mensagens registradas para esse lote.";
    document.getElementById('logs-drawer').classList.add('open');
}

function closeDrawer() {
    document.getElementById('logs-drawer').classList.remove('open');
}

// Show Toast messages
function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.style.borderColor = isError ? 'var(--danger)' : 'var(--primary)';
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 4000);
}

function escapeLogs(log) {
    if (!log) return '';
    return log.replace(/\\/g, '\\\\').replace(/`/g, '\\`').replace(/\$/g, '\\$');
}
