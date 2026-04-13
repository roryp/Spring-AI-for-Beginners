async function callPattern(endpoint, requestBody) {
    const responseDiv = document.getElementById('response');
    const loadingDiv = document.getElementById('loading');
    const submitBtn = document.getElementById('submit-btn');
    
    try {
        // Show loading state
        loadingDiv.style.display = 'block';
        responseDiv.innerHTML = '';
        submitBtn.disabled = true;
        
        // Call API
        const response = await fetch(`/api/gpt5/${endpoint}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestBody)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        
        // Display response
        displayResponse(data.result || data.response || JSON.stringify(data));
        
    } catch (error) {
        displayError('Failed to get response: ' + error.message);
    } finally {
        loadingDiv.style.display = 'none';
        submitBtn.disabled = false;
    }
}

function displayResponse(text) {
    const responseDiv = document.getElementById('response');
    const escapedText = escapeHtml(text);
    responseDiv.innerHTML = `
        <div class="response-card">
            <div class="response-header">
                <span class="response-label">AI Response</span>
                <button onclick="copyToClipboard(\`${escapedText}\`)" class="btn-copy">📋 Copy</button>
            </div>
            <div class="response-body">
                <pre>${escapedText}</pre>
            </div>
            <div class="response-footer">
                <small>Generated at ${new Date().toLocaleTimeString()}</small>
            </div>
        </div>
    `;
}

function displayError(message) {
    const responseDiv = document.getElementById('response');
    responseDiv.innerHTML = `
        <div class="error-message">
            <strong>Error:</strong> ${escapeHtml(message)}
        </div>
    `;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function copyToClipboard(text) {
    // Unescape HTML entities first
    const textarea = document.createElement('textarea');
    textarea.innerHTML = text;
    const plainText = textarea.value;
    
    navigator.clipboard.writeText(plainText).then(() => {
        alert('Copied to clipboard!');
    }).catch(err => {
        console.error('Failed to copy:', err);
    });
}

function setExample(text) {
    // Try to find the main input field by common IDs first
    const input = document.getElementById('code') || 
                  document.getElementById('task') || 
                  document.getElementById('problem') ||
                  document.getElementById('requirement') ||
                  document.getElementById('question') ||
                  document.getElementById('topic') ||
                  document.querySelector('textarea:not([readonly]), input[type="text"]:not([readonly])');
    
    if (input) {
        input.value = text;
        input.focus();
    }
}

/**
 * Generic streaming function for all patterns.
 * Calls POST /api/gpt5/{endpoint}/stream with SSE and renders tokens in real-time.
 *
 * Tokens from the AI model may arrive in rapid bursts (the model thinks first,
 * then outputs fast). A render queue drains tokens at a visible pace so the user
 * sees a smooth typing animation instead of the entire answer appearing at once.
 */
function callPatternStreaming(endpoint, requestBody) {
    const responseDiv = document.getElementById('response');
    const loadingDiv = document.getElementById('loading');
    const submitBtn = document.getElementById('submit-btn');

    loadingDiv.style.display = 'block';
    submitBtn.disabled = true;
    responseDiv.innerHTML = `
        <div class="response-card">
            <div class="response-header">
                <span class="response-label">\u{1F534} Streaming AI Response</span>
                <span id="token-counter" style="font-size:12px; color:#888;">0 tokens</span>
            </div>
            <div class="response-body">
                <pre id="stream-output" style="white-space: pre-wrap;"></pre>
            </div>
            <div class="response-footer">
                <small id="stream-status">Connecting...</small>
            </div>
        </div>
    `;

    const outputEl = document.getElementById('stream-output');
    const statusEl = document.getElementById('stream-status');
    const counterEl = document.getElementById('token-counter');

    // ---- Token render queue ----
    const tokenQueue = [];
    let tokenCount = 0;
    let streamDone = false;
    let rendering = false;
    const MIN_TOKEN_INTERVAL_MS = 15; // minimum ms between rendered tokens

    function startRendering() {
        if (rendering) return;
        rendering = true;
        let lastRender = 0;

        function renderLoop(ts) {
            // Drain tokens at a visible pace
            if (tokenQueue.length > 0 && ts - lastRender >= MIN_TOKEN_INTERVAL_MS) {
                // Render more tokens per frame if the queue is large (catch up)
                const batch = tokenQueue.length > 50 ? Math.ceil(tokenQueue.length / 30) : 1;
                for (let i = 0; i < batch && tokenQueue.length > 0; i++) {
                    outputEl.textContent += tokenQueue.shift();
                    tokenCount++;
                }
                counterEl.textContent = tokenCount + ' tokens';
                lastRender = ts;
            }

            if (tokenQueue.length > 0 || !streamDone) {
                requestAnimationFrame(renderLoop);
            } else {
                // Queue drained and stream finished
                rendering = false;
                statusEl.textContent = `Complete at ${new Date().toLocaleTimeString()} \u2014 ${tokenCount} tokens`;
                submitBtn.disabled = false;
            }
        }
        requestAnimationFrame(renderLoop);
    }

    // ---- SSE parser ----
    let sseBuffer = '';

    function processSSEEvents(text) {
        sseBuffer += text;
        const events = sseBuffer.split('\n\n');
        sseBuffer = events.pop() || '';

        for (const event of events) {
            if (!event.trim()) continue;
            const lines = event.split('\n');
            for (const line of lines) {
                if (line.startsWith('data:')) {
                    let token = line.substring(5);
                    if (token.startsWith('"') && token.endsWith('"')) {
                        try { token = JSON.parse(token); } catch(e) { /* use raw */ }
                    }
                    tokenQueue.push(token);
                }
            }
        }
        startRendering();
    }

    // ---- Fetch + ReadableStream ----
    fetch(`/api/gpt5/${endpoint}/stream`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
    }).then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        loadingDiv.style.display = 'none';
        statusEl.textContent = 'Streaming...';

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        function read() {
            reader.read().then(({ done, value }) => {
                if (done) {
                    if (sseBuffer.trim()) processSSEEvents('\n\n');
                    streamDone = true;
                    // If no tokens queued, mark complete now
                    if (tokenQueue.length === 0 && !rendering) {
                        statusEl.textContent = `Complete at ${new Date().toLocaleTimeString()} \u2014 ${tokenCount} tokens`;
                        submitBtn.disabled = false;
                    }
                    return;
                }
                processSSEEvents(decoder.decode(value, { stream: true }));
                read();
            }).catch(err => {
                streamDone = true;
                statusEl.textContent = 'Error: ' + err.message;
                submitBtn.disabled = false;
            });
        }
        read();
    }).catch(err => {
        loadingDiv.style.display = 'none';
        responseDiv.innerHTML = `<div class="error-message"><strong>Error:</strong> ${escapeHtml(err.message)}</div>`;
        submitBtn.disabled = false;
    });
}
