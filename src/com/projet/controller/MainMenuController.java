package com.projet.controller;

import com.projet.model.Joueur;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainMenuController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Joueur joueur = (Joueur) session.getAttribute("playerData");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (joueur == null) {
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Utilisateur non connecté.\"}");
            return;
        }

        try (Connection conn = DatabaseServlet.getConnection()) {
            if ("jouer".equals(action)) {
                response.getWriter().write("{\"status\":\"success\", \"redirectUrl\":\"game.jsp\"}");
            } else if ("stats".equals(action)) {
                String query = "SELECT score AS totalScore, production_points AS totalProductionPoints, " +
                        "unités_et_villes AS totalUnits " +
                        "FROM player WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, joueur.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int totalScore = rs.getInt("totalScore");
                            int totalProductionPoints = rs.getInt("totalProductionPoints");
                            int totalUnits = rs.getInt("totalUnits");

                            // Sauvegarde des stats dans la session pour les afficher sur stat.jsp
                            session.setAttribute("totalScore", totalScore);
                            session.setAttribute("totalProductionPoints", totalProductionPoints);
                            session.setAttribute("totalUnits", totalUnits);

                            response.getWriter().write("{\"status\":\"success\", \"redirectUrl\":\"stat.jsp\"}");
                        }
                    }
                }
            } else if ("logout".equals(action)) {
                session.removeAttribute("playerData");
                response.getWriter().write("{\"status\":\"success\", \"redirectUrl\":\"login.jsp\"}");
            } else {
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Action invalide.\"}");
            }
        } catch (Exception e) {
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Une erreur est survenue.\"}");
            e.printStackTrace();
        }
    }
}
