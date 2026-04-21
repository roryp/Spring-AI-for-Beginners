// --- SSE-based Advisor Log Streaming ---

let logEventSource = null;

function connectLogStream() {
    if (logEventSource) {
        logEventSource.close();
    }
    const logPanel = document.getElementById('advisor-logs');
    if (logPanel) {
        logPanel.innerHTML = '';
    }
    logEventSource = new EventSource('/api/agents/logs');
    logEventSource.addEventListener('advisor-log', function (event) {
        const data = JSON.parse(event.data);
        appendLogEntry(data);
    });
    logEventSource.onerror = function () {
        // reconnect silently if the connection drops
    };
}

function disconnectLogStream() {
    if (logEventSource) {
        logEventSource.close();
        logEventSource = null;
    }
}

function appendLogEntry(entry) {
    const logPanel = document.getElementById('advisor-logs');
    if (!logPanel) return;

    const div = document.createElement('div');
    div.className = 'log-entry log-' + entry.direction.toLowerCase();

    const ts = new Date(entry.timestamp).toLocaleTimeString();
    const badge = entry.direction === 'REQUEST' ? '⬆ REQUEST' : '⬇ RESPONSE';

    div.innerHTML =
        '<span class="log-badge">' + badge + '</span>' +
        '<span class="log-time">' + ts + '</span>' +
        '<pre class="log-message">' + escapeHtml(entry.message) + '</pre>';

    logPanel.appendChild(div);
    logPanel.scrollTop = logPanel.scrollHeight;
}

// --- Pattern API Call ---

async function callPattern(endpoint, requestBody) {
    const responseDiv = document.getElementById('response');
    const loadingDiv = document.getElementById('loading');
    const submitBtn = document.getElementById('submit-btn');

    try {
        loadingDiv.style.display = 'block';
        responseDiv.innerHTML = '';
        submitBtn.disabled = true;
        connectLogStream();

        const response = await fetch('/api/agents/' + endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error('HTTP ' + response.status + ': ' + response.statusText);
        }

        const data = await response.json();
        displayResponse(data.result || data.response || JSON.stringify(data));
    } catch (error) {
        displayError('Failed to get response: ' + error.message);
    } finally {
        loadingDiv.style.display = 'none';
        submitBtn.disabled = false;
        // keep the log stream open so late events still arrive
    }
}

function displayResponse(text) {
    const responseDiv = document.getElementById('response');
    const escapedText = escapeHtml(text);
    responseDiv.innerHTML =
        '<div class="response-card">' +
            '<div class="response-header">' +
                '<span class="response-label">AI Response</span>' +
                '<button onclick="copyToClipboard()" class="btn-copy">📋 Copy</button>' +
            '</div>' +
            '<div class="response-body">' +
                '<pre>' + escapedText + '</pre>' +
            '</div>' +
            '<div class="response-footer">' +
                '<small>Generated at ' + new Date().toLocaleTimeString() + '</small>' +
            '</div>' +
        '</div>';
}

function displayError(message) {
    const responseDiv = document.getElementById('response');
    responseDiv.innerHTML =
        '<div class="error-message">' +
            '<strong>Error:</strong> ' + escapeHtml(message) +
        '</div>';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function copyToClipboard() {
    const pre = document.querySelector('.response-body pre');
    if (pre) {
        navigator.clipboard.writeText(pre.textContent).then(function () {
            alert('Copied to clipboard!');
        });
    }
}

function setExample(text) {
    const input = document.getElementById('input') ||
                  document.getElementById('task') ||
                  document.getElementById('problem') ||
                  document.querySelector('textarea');
    if (input) {
        input.value = text;
        input.focus();
    }
}
