package com.projet.controller;

import com.projet.model.*;
import com.projet.turn.TurnManager;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TurnController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Joueur current = (Joueur) session.getAttribute("playerData");

        // 1) Mise à jour de la liste des joueurs connectés
        //    (On lit la table "player" is_connected=1)
        try (Connection conn = DatabaseServlet.getConnection()) {
            List<Integer> connectedIds = new ArrayList<>();
            String sql = "SELECT id FROM player WHERE is_connected=1";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    connectedIds.add(rs.getInt("id"));
                }
            }

            TurnManager manager = TurnManager.getInstance();
            manager.setActivePlayers(connectedIds);

            // 2) On appelle tickOneSecond() pour décrémenter le timer
            manager.tickIfNeeded();

            // 3) Récup info
            int curPlayerId = manager.getCurrentPlayerId();
            int timeLeft = manager.getTimeLeft();

            // S’il n’y a aucun joueur
            if (curPlayerId == -1) {
                // Pas de joueur => return something
                response.getWriter().write("{\"status\":\"no_players\"}");
                return;
            }

            // 4) canPlay = (current != null && current.id == curPlayerId)
            boolean canPlay = false;
            if (current!=null && current.getId() == curPlayerId) {
                canPlay = true;
            }

            // 5) On renvoie le JSON
            String json = String.format(
                    "{\"status\":\"turn_info\",\"currentPlayerId\":%d,"
                            + "\"timeLeft\":%d,\"canPlay\":%b}",
                    curPlayerId, timeLeft, canPlay
            );
            response.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\",\"message\":\""+ e.getMessage()+"\"}");
        }
    }
}
