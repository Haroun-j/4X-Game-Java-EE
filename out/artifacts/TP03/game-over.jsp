<!-- =========================================================================
game-over.jsp
(in your webapp directory)
========================================================================= -->
<%@ page import="com.projet.model.StatsSnapshot" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Fin de Partie</title>
  <link rel="stylesheet" href="styles/game.css">
</head>
<body>
<%
  StatsSnapshot data = (StatsSnapshot) session.getAttribute("finalData");
  if (data != null) {
%>
<div class="game-over-container">
  <h1>
    <%= "win".equals(data.getEndReason())
            ? "Vous avez GAGNÉ !"
            : "Game Over..."
    %>
  </h1>

  <!-- BLOC 1: Statistiques de cette partie -->
  <h2>Statistiques de la Partie</h2>
  <p><strong>Nom du Joueur :</strong> <%= data.getPlayerName() %></p>
  <p><strong>Score (cette partie) :</strong> <%= data.getGameScore() %></p>
  <p><strong>Production (cette partie) :</strong> <%= data.getGameProduction() %></p>
  <p><strong>Unités/Villes (cette partie) :</strong> <%= data.getGameCities() %></p>

  <!-- BLOC 2: Historique global (valeurs cumulées en base) -->
  <h2>Historique Global</h2>
  <p><strong>Score Total :</strong> <%= data.getTotalScore() %></p>
  <p><strong>Production Totale :</strong> <%= data.getTotalProduction() %></p>
  <p><strong>Unités/Villes Totales :</strong> <%= data.getTotalUnitsEtVilles() %></p>

  <br/>
  <a href="mainMenu.jsp">Revenir au Menu Principal</a>
</div>
<%
} else {
%>
<div class="game-over-container">
  <h1>Game Over</h1>
  <p>Aucune donnée finale trouvée. <a href="mainMenu.jsp">Se connecter</a></p>
</div>
<%
  }
%>
</body>
</html>
