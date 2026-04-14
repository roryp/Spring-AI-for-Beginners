// Tic-Tac-Toe with AI - Spring AI MCP Demo

let gameId = null;
let gameActive = false;
let scores = { player: 0, ai: 0, draws: 0 };

// Load saved scores from localStorage
const savedScores = localStorage.getItem('tictactoe-scores');
if (savedScores) {
    scores = JSON.parse(savedScores);
    updateScoreDisplay();
}

// --- Game Actions ---

async function newGame() {
    const btn = document.getElementById('newGameBtn');
    btn.disabled = true;
    updateStatus('Starting new game...', '');

    try {
        const response = await fetch('/api/game/new', { method: 'POST' });
        const data = await response.json();

        if (data.error) {
            updateStatus('Error: ' + data.error, '');
            return;
        }

        gameId = data.gameId;
        gameActive = true;
        renderBoard(data.board, []);
        updateStatus('Your turn! Place your <strong>X</strong>', '');
        enableBoard();
    } catch (error) {
        updateStatus('Failed to start game. Is the MCP server running on port 8080?', '');
    } finally {
        btn.disabled = false;
    }
}

async function makeMove(position) {
    if (!gameActive || !gameId) return;

    const cell = document.querySelector(`[data-pos="${position}"]`);
    if (cell.classList.contains('occupied')) return;

    // Optimistic update: show X immediately
    cell.textContent = 'X';
    cell.classList.add('x', 'occupied', 'new-move');
    gameActive = false;
    disableBoard();
    updateStatus('🤖 AI is thinking...', 'thinking');

    try {
        const response = await fetch('/api/game/move', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ gameId, position })
        });

        const data = await response.json();

        if (data.error) {
            updateStatus('Error: ' + data.error, '');
            // Revert optimistic update
            cell.textContent = '';
            cell.classList.remove('x', 'occupied', 'new-move');
            gameActive = true;
            enableBoard();
            return;
        }

        renderBoard(data.board, data.winningCells || []);
        handleGameEnd(data);
    } catch (error) {
        updateStatus('Connection error: ' + error.message, '');
        gameActive = true;
        enableBoard();
    }
}

// --- Board Rendering ---

function renderBoard(board, winningCells) {
    for (let i = 0; i < 9; i++) {
        const cell = document.querySelector(`[data-pos="${i}"]`);
        const prevContent = cell.textContent;
        cell.textContent = board[i] || '';
        cell.className = 'cell';

        if (board[i] === 'X') {
            cell.classList.add('x', 'occupied');
        } else if (board[i] === 'O') {
            cell.classList.add('o', 'occupied');
            // Animate new AI move
            if (prevContent !== 'O') {
                cell.classList.add('new-move');
            }
        }

        if (winningCells && winningCells.includes(i)) {
            cell.classList.add('winner-cell');
        }
    }
}

// --- Game State Handling ---

function handleGameEnd(data) {
    if (data.status === 'WON') {
        if (data.winner === 'X') {
            scores.player++;
            updateStatus('🎉 You win! Great move!', 'win');
        } else {
            scores.ai++;
            updateStatus('🤖 AI wins! Better luck next time.', 'lose');
        }
        gameActive = false;
        saveScores();
        updateScoreDisplay();
    } else if (data.status === 'DRAW') {
        scores.draws++;
        updateStatus('🤝 It\'s a draw! Well played.', 'draw');
        // Add draw styling to all cells
        for (let i = 0; i < 9; i++) {
            const cell = document.querySelector(`[data-pos="${i}"]`);
            if (!cell.classList.contains('winner-cell')) {
                cell.classList.add('draw-cell');
            }
        }
        gameActive = false;
        saveScores();
        updateScoreDisplay();
    } else {
        // Game continues — player's turn
        gameActive = true;
        enableBoard();
        updateStatus('Your turn! Place your <strong>X</strong>', '');
    }
}

// --- UI Updates ---

function updateStatus(message, statusClass) {
    const statusEl = document.getElementById('gameStatus');
    statusEl.innerHTML = message;
    statusEl.className = 'game-status';
    if (statusClass) {
        statusEl.classList.add(statusClass);
    }
}

function updateScoreDisplay() {
    document.getElementById('playerScore').textContent = scores.player;
    document.getElementById('aiScore').textContent = scores.ai;
    document.getElementById('drawScore').textContent = scores.draws;
}

function enableBoard() {
    document.querySelectorAll('.cell').forEach(cell => {
        cell.classList.remove('disabled');
    });
}

function disableBoard() {
    document.querySelectorAll('.cell').forEach(cell => {
        if (!cell.classList.contains('occupied')) {
            cell.classList.add('disabled');
        }
    });
}

function resetScores() {
    scores = { player: 0, ai: 0, draws: 0 };
    saveScores();
    updateScoreDisplay();
}

function saveScores() {
    localStorage.setItem('tictactoe-scores', JSON.stringify(scores));
}
