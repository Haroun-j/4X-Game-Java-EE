<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Statistiques</title>
  <link rel="stylesheet" href="styles/stat.css">
</head>
<body>
<header class="stat-header">
  <h1>Vos Statistiques</h1>
  <button onclick="window.location.href='mainMenu.jsp'" class="back-button">Retour au Menu</button>
</header>
<div class="stat-container">
  <div class="stat-card">
    <h2>Score Total</h2>
    <p><%= session.getAttribute("totalScore") != null ? session.getAttribute("totalScore") : 0 %></p>
  </div>
  <div class="stat-card">
    <h2>Points de Production Totaux</h2>
    <p><%= session.getAttribute("totalProductionPoints") != null ? session.getAttribute("totalProductionPoints") : 0 %></p>
  </div>
  <div class="stat-card">
    <h2>Nombre Total d'Unités</h2>
    <p><%= session.getAttribute("totalUnits") != null ? session.getAttribute("totalUnits") : 0 %></p>
  </div>
</div>
</body>
</html>
