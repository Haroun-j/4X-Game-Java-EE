<%@ page import="com.projet.model.Joueur" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>4X Game Arena</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="styles/game.css">
</head>
<body>
<header class="game-header">
    <h1>4X Game Arena</h1>
    <button type="button" onclick="sendAction('logout')">logout</button>
</header>

<div class="game-wrapper">
    <!-- Player Info -->
    <div class="player-info">
        <h2>Joueur</h2>
        <% Joueur joueur = (Joueur) session.getAttribute("playerData"); %>
        <% if (joueur != null) { %>
        <p><strong>Nom :</strong> <%= joueur.getName() %>
        </p>
        <p><strong>Points de Vie :</strong> <span id="health-points"></span></p>
        <p><strong>Score :</strong> <span id="score"><%= joueur.getScore() %></span></p>
        <p><strong>Production :</strong> <span id="production-points"><%= joueur.getProductionPoints() %></span></p>
        <% } else { %>
        <p>Player data unavailable. <a href="login.jsp">Log in.</a></p>
        <% } %>

        <h3>Tour en cours</h3>
        <p>Temps restant : <span id="time-left">--</span></p>
        <p id="turn-status" style="font-weight: bold; color: red;">...</p>
    </div>

    <!-- Game Arena -->
    <div class="arena">
        <table class="game-grid"></table>
    </div>

    <!-- Actions -->
    <div class="actions">
        <h2>Actions</h2>
        <div class="movement-buttons">
            <button onclick="sendAction('move_north')">
                <i class="fas fa-arrow-up"></i>
                <span>Up</span>
            </button>
            <div class="side-buttons">
                <button onclick="sendAction('move_west')">
                    <i class="fas fa-arrow-left"></i>
                    <span>Left</span>
                </button>
                <button onclick="sendAction('move_east')">
                    <i class="fas fa-arrow-right"></i>
                    <span>Right</span>
                </button>
            </div>
            <button onclick="sendAction('move_south')">
                <i class="fas fa-arrow-down"></i>
                <span>Down</span>
            </button>
        </div>
        <div class="action-buttons">
            <button onclick="sendAction('heal')">
                <i class="fas fa-heart"></i>
                <span>Heal</span>
            </button>
            <button onclick="sendAction('produce_unit')">
                <i class="fas fa-cogs"></i>
                <span>Produce</span>
            </button>
            <button onclick="sendAction('add_soldier')">
                <i class="fa-solid fa-user-shield"></i>
                <span>Add soldier</span>
            </button>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    const colorMap = {};
    const colors = ["#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#F3FF33", "#33FFF7"];
    let colorIndex = 0;

    function getColorForOwner(ownerId) {
        if (!colorMap[ownerId]) {
            colorMap[ownerId] = colors[colorIndex % colors.length];
            colorIndex++;
        }
        return colorMap[ownerId];
    }

    const timeLeftDisplay = document.getElementById("time-left");
    const turnStatusDisplay = document.getElementById("turn-status");

    let canPlay = false;

    setInterval(() => {
        fetch("TurnController")
            .then(r => r.json())
            .then(data => {
                if (data.status === "turn_info") {
                    const secLeft = data.timeLeft;
                    canPlay = data.canPlay;

                    // Affiche le timer
                    timeLeftDisplay.textContent = secLeft + "s";

                    // Affiche si c'est notre tour ou pas
                    if (canPlay) {
                        turnStatusDisplay.textContent = "À vous de jouer !";
                        turnStatusDisplay.style.color = "green";
                    } else {
                        turnStatusDisplay.textContent = "En attente...";
                        turnStatusDisplay.style.color = "red";
                    }

                    // Active ou désactive les boutons
                    toggleActionButtons(canPlay);
                }
            })
            .catch(err => console.error(err));
    }, 1000);

    function toggleActionButtons(enable) {
        let buttons = document.querySelectorAll(".movement-buttons button, .action-buttons button");
        buttons.forEach(btn => {
            btn.disabled = !enable;
        });
    }

    function sendAction(action) {
        if (!canPlay && action !== 'force_win') return;
        fetch("ActionsController", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: 'action=' + action,
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === "success") {
                    toggleActionButtons(false);

                    if (action === 'logout') {
                        window.location.href = data.redirectUrl;
                    } else {
                        refreshMap();
                    }
                } else if (data.status === "game_over") {
                    Swal.fire({
                        title: "Game Over!",
                        text: data.message,
                        icon: "error",
                        confirmButtonText: "View Stats",
                    }).then(() => {
                        window.location.href = data.redirectUrl;
                    });
                } else {
                    console.log(data.message);
                }
            })
    }

    let refreshInterval = setInterval(refreshMap, 500);

    function refreshMap() {
        fetch("GameStateController")
            .then(response => response.json())
            .then(data => {
                if (data.status === "you_win") {
                    // Show the alert once
                    Swal.fire({
                        title: "Félicitations !",
                        text: data.message,
                        icon: "success",
                        confirmButtonText: "Voir Score"
                    }).then(() => {
                        // Stop refreshing
                        clearInterval(refreshInterval);
                        refreshInterval = null;
                        sendAction("force_win");

                        window.location.href = data.redirectUrl;
                    });
                    return;
                }
                if (data.playerData) {
                    refreshUserData(data.playerData);
                }

                const map = document.querySelector(".game-grid");
                map.innerHTML = ""; // Clear the current map

                for (let i = 0; i < 8; i++) {
                    const row = document.createElement("tr");

                    for (let j = 0; j < 8; j++) {
                        const cell = document.createElement("td");
                        cell.classList.add("cell");

                        // Get tiles at the current position
                        const tiles = data.map.filter(tile => tile.x === j && tile.y === i);

                        tiles.forEach(tile => {
                            const img = document.createElement("img");
                            img.src = tile.image;
                            img.alt = tile.type;
                            img.classList.add("tile-image");
                            cell.appendChild(img);

                            if (tile.type === "SOLDAT") {
                                const color = getColorForOwner(tile.playerId);
                                cell.style.backgroundColor = color;
                                img.closest('.cell').style.border = '2px solid ' + color;
                                console.log(tile.playerId + color);
                                if (tile.playerId === data.playerData.id) {
                                    document.querySelector("#health-points").textContent = tile.healthPoints;
                                }
                            }

                            if (tile.type === "VILLE") {
                                const defensePoints = document.createElement("span");
                                defensePoints.classList.add("defense-points");
                                defensePoints.textContent = tile.defensePoints;
                                cell.appendChild(defensePoints);

                                if (tile.ownerId !== -1) {
                                    const color = getColorForOwner(tile.ownerId);
                                    cell.style.backgroundColor = color;
                                    cell.classList.add("owned-ville");
                                    console.log(tile.ownerId + color);
                                }
                            }

                        });

                        row.appendChild(cell);
                    }

                    map.appendChild(row);
                }
            })
            .catch(error => {
                console.error("Failed to refresh the map:", error);
                // alert("Failed to refresh the map.");
            });
    }

    function updatePlayerInfo(playerInfo) {
        const playerName = document.querySelector(".player-info strong:nth-child(1)");
        const playerHealth = document.querySelector(".player-info strong:nth-child(2)");
        const playerScore = document.querySelector(".player-info strong:nth-child(3)");

        if (playerInfo) {
            playerName.textContent = playerInfo.name;
            playerHealth.textContent = playerInfo.health;
            playerScore.textContent = playerInfo.score;
        }
    }

    function refreshUserData(playerData) {
        if (playerData) {
            document.querySelector("#production-points").textContent = playerData.productionPoints;
            // document.querySelector("#health-points").textContent = playerData.healthPoints; //soldats[0].healthPoints
            document.querySelector("#score").textContent = playerData.score;
        }
    }

    document.addEventListener("keydown", function (event) {
        switch (event.key) {
            case "ArrowUp":
                sendAction("move_north");
                break;
            case "ArrowDown":
                sendAction("move_south");
                break;
            case "ArrowLeft":
                sendAction("move_west");
                break;
            case "ArrowRight":
                sendAction("move_east");
                break;
            case "h":
                sendAction("heal");
                break;
            case "f":
                sendAction("heal");
                break;
        }
    });
    refreshMap();
</script>

</body>
</html>
