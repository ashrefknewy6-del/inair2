package com.example.network

object WebDashboardAssets {

    val INDEX_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>InAir — Your phone, in the air.</title>
    <style>
        :root {
            --bg: #0D0507;
            --surface: #1A0A0D;
            --surface-card: #240E13;
            --surface-hover: #32141A;
            --primary: #FF2D3F;
            --primary-dark: #B3001B;
            --secondary: #FF6B6B;
            --text: #FBEAEA;
            --text-muted: #D4A5AB;
            --border: rgba(255, 45, 63, 0.2);
            --gradient: linear-gradient(135deg, #FF2D3F, #B3001B);
            --glow: 0 8px 32px rgba(255, 45, 63, 0.25);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
        body { background: var(--bg); color: var(--text); min-height: 100vh; display: flex; flex-direction: column; overflow-x: hidden; }
        
        /* Top Navigation Header */
        header {
            background: rgba(26, 10, 13, 0.85);
            backdrop-filter: blur(16px);
            border-bottom: 1px solid var(--border);
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        .logo-wrap { display: flex; align-items: center; gap: 12px; }
        .logo-icon {
            width: 38px; height: 38px; border-radius: 12px;
            background: var(--gradient);
            display: flex; align-items: center; justify-content: center;
            box-shadow: 0 4px 16px rgba(255, 45, 63, 0.4);
        }
        .logo-icon svg { width: 22px; height: 22px; fill: white; }
        .brand-text h1 { font-size: 20px; font-weight: 700; letter-spacing: -0.5px; }
        .brand-text p { font-size: 11px; color: var(--secondary); font-weight: 500; }
        
        .header-stats { display: flex; align-items: center; gap: 16px; }
        .status-badge {
            display: inline-flex; align-items: center; gap: 8px;
            background: rgba(255, 45, 63, 0.15); border: 1px solid var(--border);
            padding: 6px 14px; border-radius: 999px; font-size: 13px; font-weight: 600; color: var(--secondary);
        }
        .pulse-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary); box-shadow: 0 0 10px var(--primary); animation: pulse 1.8s infinite; }
        @keyframes pulse { 0% { opacity: 0.4; transform: scale(0.9); } 50% { opacity: 1; transform: scale(1.2); } 100% { opacity: 0.4; transform: scale(0.9); } }

        /* Main Workspace */
        main { flex: 1; max-width: 1400px; width: 100%; margin: 0 auto; padding: 24px; display: grid; grid-template-columns: 1fr 420px; gap: 24px; }
        @media (max-width: 1024px) { main { grid-template-columns: 1fr; } }

        /* Screen View Pane */
        .screen-pane { display: flex; flex-direction: column; align-items: center; gap: 16px; }
        .screen-container {
            position: relative;
            background: #000;
            border-radius: 28px;
            border: 2px solid var(--border);
            box-shadow: var(--glow);
            overflow: hidden;
            display: flex; align-items: center; justify-content: center;
            max-height: 78vh;
            cursor: crosshair;
            user-select: none;
        }
        .screen-img {
            max-width: 100%;
            max-height: 78vh;
            display: block;
            object-fit: contain;
            pointer-events: auto;
        }

        /* Remote Control Bar */
        .control-bar {
            display: flex; align-items: center; gap: 12px;
            background: var(--surface);
            border: 1px solid var(--border);
            padding: 10px 20px;
            border-radius: 999px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.5);
        }
        .btn-ctrl {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 45, 63, 0.2);
            color: var(--text);
            padding: 8px 16px;
            border-radius: 999px;
            cursor: pointer;
            font-size: 13px; font-weight: 600;
            display: flex; align-items: center; gap: 6px;
            transition: all 0.2s ease;
        }
        .btn-ctrl:hover { background: var(--gradient); border-color: transparent; transform: translateY(-2px); }
        .btn-ctrl:active { transform: translateY(0); }

        /* Right Panel: File Hub & Info */
        .side-pane { display: flex; flex-direction: column; gap: 20px; }
        .card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 24px;
            padding: 20px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
        }
        .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .card-title { font-size: 16px; font-weight: 700; color: var(--text); }
        
        /* Drag-and-drop Upload Zone */
        .drop-zone {
            border: 2px dashed rgba(255, 45, 63, 0.4);
            border-radius: 18px;
            padding: 30px 16px;
            text-align: center;
            background: rgba(255, 45, 63, 0.03);
            cursor: pointer;
            transition: all 0.2s;
        }
        .drop-zone:hover, .drop-zone.dragover { background: rgba(255, 45, 63, 0.08); border-color: var(--primary); }
        .drop-zone p { font-size: 13px; color: var(--text-muted); margin-top: 8px; }
        .drop-zone .btn-upload {
            margin-top: 12px;
            background: var(--gradient);
            border: none; color: white;
            padding: 8px 18px; border-radius: 999px;
            font-size: 13px; font-weight: 600; cursor: pointer;
        }
        
        /* Upload Progress Bar */
        .progress-box { margin-top: 14px; display: none; }
        .progress-bar-bg { width: 100%; height: 8px; background: rgba(255,255,255,0.1); border-radius: 999px; overflow: hidden; }
        .progress-bar-fill { width: 0%; height: 100%; background: var(--gradient); transition: width 0.1s; }
        .progress-text { font-size: 12px; color: var(--secondary); margin-top: 6px; display: flex; justify-content: space-between; }

        /* File List */
        .file-list { display: flex; flex-direction: column; gap: 8px; max-height: 320px; overflow-y: auto; margin-top: 12px; }
        .file-item {
            background: var(--surface-card);
            border: 1px solid rgba(255, 45, 63, 0.12);
            border-radius: 14px;
            padding: 10px 14px;
            display: flex; align-items: center; justify-content: space-between;
            transition: background 0.2s;
        }
        .file-item:hover { background: var(--surface-hover); }
        .file-meta { overflow: hidden; }
        .file-name { font-size: 13px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px; }
        .file-size { font-size: 11px; color: var(--text-muted); }
        .btn-dl {
            background: rgba(255, 45, 63, 0.15);
            border: 1px solid var(--border);
            color: var(--secondary);
            padding: 6px 12px; border-radius: 999px;
            font-size: 12px; font-weight: 600;
            text-decoration: none;
            transition: all 0.2s;
        }
        .btn-dl:hover { background: var(--gradient); color: white; }

        /* PIN Modal */
        .modal-overlay {
            position: fixed; top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(13, 5, 7, 0.9);
            backdrop-filter: blur(12px);
            display: none; align-items: center; justify-content: center;
            z-index: 200;
        }
        .modal-box {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 28px;
            padding: 32px;
            max-width: 360px; width: 90%;
            text-align: center;
            box-shadow: var(--glow);
        }
        .pin-input {
            width: 100%; padding: 12px; text-align: center;
            font-size: 24px; letter-spacing: 8px; font-weight: bold;
            background: rgba(0,0,0,0.4); border: 2px solid var(--border);
            color: var(--text); border-radius: 16px; margin: 20px 0;
        }
    </style>
</head>
<body>
    <header>
        <div class="logo-wrap">
            <div class="logo-icon">
                <svg viewBox="0 0 24 24"><path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"/></svg>
            </div>
            <div class="brand-text">
                <h1>InAir</h1>
                <p>Your phone, in the air.</p>
            </div>
        </div>
        <div class="header-stats">
            <div class="status-badge">
                <span class="pulse-dot"></span>
                <span id="conn-status">Broadcasting</span>
            </div>
        </div>
    </header>

    <main>
        <section class="screen-pane">
            <div class="screen-container" id="screenBox">
                <img id="streamView" class="screen-img" src="/stream.mjpg" alt="Screen Stream" />
            </div>

            <!-- Remote Navigation Toolbar -->
            <div class="control-bar">
                <button class="btn-ctrl" onclick="sendAction('back')">◀ Back</button>
                <button class="btn-ctrl" onclick="sendAction('home')">● Home</button>
                <button class="btn-ctrl" onclick="sendAction('recents')">■ Recents</button>
                <button class="btn-ctrl" onclick="sendAction('notifications')">🔔 Panel</button>
            </div>
        </section>

        <section class="side-pane">
            <!-- File Hub Card -->
            <div class="card">
                <div class="card-header">
                    <span class="card-title">Wireless File Hub</span>
                    <button class="btn-ctrl" style="padding: 4px 10px; font-size: 11px;" onclick="loadFiles()">↻ Refresh</button>
                </div>

                <div class="drop-zone" id="dropZone">
                    <svg style="width:36px;height:36px;fill:var(--secondary);margin:0 auto;" viewBox="0 0 24 24"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
                    <p>Drag &amp; drop files here to send to phone</p>
                    <input type="file" id="fileInput" style="display:none;" />
                    <button class="btn-upload" onclick="document.getElementById('fileInput').click()">Select File</button>
                </div>

                <div class="progress-box" id="progressBox">
                    <div class="progress-bar-bg"><div class="progress-bar-fill" id="progressFill"></div></div>
                    <div class="progress-text"><span id="progressFile">Sending...</span><span id="progressPct">0%</span></div>
                </div>

                <div class="file-list" id="fileList">
                    <!-- Populated dynamically -->
                </div>
            </div>

            <!-- Session & Network Card -->
            <div class="card">
                <div class="card-header">
                    <span class="card-title">Device Session</span>
                </div>
                <div style="font-size: 13px; line-height: 1.6; color: var(--text-muted);">
                    <div>Local IP: <span style="color:var(--text); font-weight:600;" id="clientIp">...</span></div>
                    <div>Latency: <span style="color:var(--secondary); font-weight:600;" id="latencyMs">&lt; 15ms</span></div>
                    <div>Security: <span style="color:#4CAF50; font-weight:600;">Authorized Session</span></div>
                </div>
            </div>
        </section>
    </main>

    <!-- PIN Authentication Modal -->
    <div class="modal-overlay" id="pinModal">
        <div class="modal-box">
            <h2>PIN Verification</h2>
            <p style="color:var(--text-muted); font-size:13px; margin-top:8px;">Enter the 4-digit PIN displayed on your InAir app.</p>
            <input type="password" id="pinField" class="pin-input" maxlength="4" placeholder="••••" />
            <button class="btn-ctrl" style="width:100%; justify-content:center; background:var(--gradient);" onclick="submitPin()">Authorize</button>
        </div>
    </div>

    <script>
        let isMouseDown = false;
        let startX = 0, startY = 0;
        const img = document.getElementById('streamView');
        const box = document.getElementById('screenBox');

        // Touch & Click Mapping to Normalized Coordinates
        function getNormCoords(e) {
            const rect = img.getBoundingClientRect();
            const clientX = e.clientX || (e.touches && e.touches[0].clientX);
            const clientY = e.clientY || (e.touches && e.touches[0].clientY);
            const x = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
            const y = Math.max(0, Math.min(1, (clientY - rect.top) / rect.height));
            return { x, y };
        }

        img.addEventListener('mousedown', (e) => {
            isMouseDown = true;
            const c = getNormCoords(e);
            startX = c.x; startY = c.y;
            e.preventDefault();
        });

        img.addEventListener('mouseup', (e) => {
            if (!isMouseDown) return;
            isMouseDown = false;
            const c = getNormCoords(e);
            const dist = Math.hypot(c.x - startX, c.y - startY);
            if (dist < 0.03) {
                // Single tap
                sendAction('tap', { x: c.x, y: c.y });
            } else {
                // Swipe gesture
                sendAction('swipe', { x1: startX, y1: startY, x2: c.x, y2: c.y });
            }
        });

        function sendAction(action, params = {}) {
            const query = new URLSearchParams({ action, ...params }).toString();
            fetch('/api/control?' + query, { method: 'POST' }).catch(err => console.error(err));
        }

        // File uploads
        const dropZone = document.getElementById('dropZone');
        const fileInput = document.getElementById('fileInput');

        ['dragenter', 'dragover'].forEach(n => dropZone.addEventListener(n, (e) => { e.preventDefault(); dropZone.classList.add('dragover'); }));
        ['dragleave', 'drop'].forEach(n => dropZone.addEventListener(n, (e) => { e.preventDefault(); dropZone.classList.remove('dragover'); }));

        dropZone.addEventListener('drop', (e) => {
            if (e.dataTransfer.files.length > 0) uploadFile(e.dataTransfer.files[0]);
        });
        fileInput.addEventListener('change', () => {
            if (fileInput.files.length > 0) uploadFile(fileInput.files[0]);
        });

        function uploadFile(file) {
            const pBox = document.getElementById('progressBox');
            const pFill = document.getElementById('progressFill');
            const pPct = document.getElementById('progressPct');
            const pFile = document.getElementById('progressFile');

            pBox.style.display = 'block';
            pFile.textContent = file.name;

            const xhr = new XMLHttpRequest();
            xhr.open('POST', '/api/files/upload?name=' + encodeURIComponent(file.name));
            xhr.upload.onprogress = (e) => {
                if (e.lengthComputable) {
                    const pct = Math.round((e.loaded / e.total) * 100);
                    pFill.style.width = pct + '%';
                    pPct.textContent = pct + '%';
                }
            };
            xhr.onload = () => {
                setTimeout(() => { pBox.style.display = 'none'; loadFiles(); }, 1000);
            };
            xhr.send(file);
        }

        function loadFiles() {
            fetch('/api/files')
                .then(r => r.json())
                .then(files => {
                    const list = document.getElementById('fileList');
                    list.innerHTML = '';
                    if (!files || files.length === 0) {
                        list.innerHTML = '<div style="color:var(--text-muted); font-size:12px; text-align:center; padding:12px;">No files shared yet</div>';
                        return;
                    }
                    files.forEach(f => {
                        const item = document.createElement('div');
                        item.className = 'file-item';
                        item.innerHTML = '<div class="file-meta">' +
                            '<div class="file-name">' + f.name + '</div>' +
                            '<div class="file-size">' + formatBytes(f.size) + '</div>' +
                            '</div>' +
                            '<a class="btn-dl" href="/api/files/download?file=' + encodeURIComponent(f.name) + '" download>Download</a>';
                        list.appendChild(item);
                    });
                }).catch(err => console.error(err));
        }

        function formatBytes(bytes) {
            if (bytes === 0) return '0 B';
            const k = 1024, dm = 1;
            const sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
        }

        // Check authentication & security
        fetch('/api/status')
            .then(r => r.json())
            .then(data => {
                if (data.requiresPin && !data.authorized) {
                    document.getElementById('pinModal').style.display = 'flex';
                }
                loadFiles();
            }).catch(e => loadFiles());

        function submitPin() {
            const pin = document.getElementById('pinField').value;
            fetch('/api/auth', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ pin }) })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        document.getElementById('pinModal').style.display = 'none';
                        document.getElementById('streamView').src = '/stream.mjpg?' + Date.now();
                        loadFiles();
                    } else {
                        alert('Incorrect PIN');
                    }
                });
        }
    </script>
</body>
</html>
    """.trimIndent()
}
