<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Menu Principal</title>
    <link rel="stylesheet" href="styles/mainMenu.css">
</head>
<body>
<header class="main-header">
    <h1>Menu Principal</h1>
</header>
<div class="menu-container">
    <button class="menu-button jouer" onclick="navigate('jouer')">Jouer</button>
    <button class="menu-button stats" onclick="navigate('stats')">Statistiques</button>
    <button class="menu-button logout" onclick="navigate('logout')">Se Déconnecter</button>
</div>

<script>
    function navigate(action) {
        fetch('MainMenuController?action=' + action, {
            method: 'GET'
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === "success") {
                    if (action === 'logout') {
                        window.location.href = data.redirectUrl;
                    } else if (action === 'jouer') {
                        window.location.href = "game.jsp";
                    } else if (action === 'stats') {
                        window.location.href = data.redirectUrl;
                    }
                } else {
                    alert(data.message || "Une erreur est survenue.");
                }
            })
            .catch(err => console.error("Erreur:", err));
    }
</script>
</body>
</html>
