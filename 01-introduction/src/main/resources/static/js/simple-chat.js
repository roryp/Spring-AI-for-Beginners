const messagesDiv = document.getElementById('messages');
const chatForm = document.getElementById('chat-form');
const messageInput = document.getElementById('message-input');
const sendBtn = document.getElementById('send-btn');

chatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const message = messageInput.value.trim();
    if (!message) return;

    // Add user message to UI
    addMessage('user', message);
    messageInput.value = '';
    sendBtn.disabled = true;

    // Show loading indicator
    const loadingId = addLoadingMessage();

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ message })
        });

        const data = await response.json();
        
        // Remove loading indicator
        removeLoadingMessage(loadingId);

        if (response.ok) {
            addMessage('ai', data.answer);
        } else {
            addMessage('error', data.error || 'An error occurred');
        }
    } catch (error) {
        removeLoadingMessage(loadingId);
        addMessage('error', 'Failed to connect to server');
    } finally {
        sendBtn.disabled = false;
        messageInput.focus();
    }
});

function addMessage(type, text) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    
    const label = document.createElement('div');
    label.className = 'message-label';
    label.textContent = type === 'user' ? 'You' : type === 'error' ? 'Error' : 'AI';
    
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';
    bubble.textContent = text;
    
    messageDiv.appendChild(label);
    messageDiv.appendChild(bubble);
    messagesDiv.appendChild(messageDiv);
    
    scrollToBottom();
}

function addLoadingMessage() {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message ai';
    messageDiv.id = 'loading-message';
    
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble loading';
    bubble.textContent = 'AI is thinking';
    
    messageDiv.appendChild(bubble);
    messagesDiv.appendChild(messageDiv);
    
    scrollToBottom();
    return 'loading-message';
}

function removeLoadingMessage(id) {
    const loadingMsg = document.getElementById(id);
    if (loadingMsg) {
        loadingMsg.remove();
    }
}

function scrollToBottom() {
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// Focus input on load
messageInput.focus();
