package com.projet.controller;

import com.projet.model.Joueur;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("subscribe".equals(action)) {
            handleSubscription(request, response);
        } else if ("login".equals(action)) {
            handleLogin(request, response);
        } else {
            response.sendRedirect("login.jsp?error=invalid_action");
        }
    }

    private void handleSubscription(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String pseudo = request.getParameter("pseudo");
        String password = request.getParameter("password");
        String sexe = request.getParameter("sexe");
        String sessionId = request.getParameter("sessionId");

        if (email == null || username == null || pseudo == null || password == null || sexe == null || sessionId == null ||
                email.isEmpty() || username.isEmpty() || pseudo.isEmpty() || password.isEmpty() || sexe.isEmpty() || sessionId.isEmpty()) {
            response.sendRedirect("login.jsp?error=missing_fields");
            return;
        }

        try (Connection conn = DatabaseServlet.getConnection()) {
            String hashedPassword = hashPassword(password);

            String sqlPlayer = "INSERT INTO player (email, username, pseudo_name, password, sexe, session_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmtPlayer = conn.prepareStatement(sqlPlayer, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmtPlayer.setString(1, email);
                stmtPlayer.setString(2, username);
                stmtPlayer.setString(3, pseudo);
                stmtPlayer.setString(4, hashedPassword);
                stmtPlayer.setString(5, sexe);
                stmtPlayer.setString(6, sessionId);
                stmtPlayer.executeUpdate();
            }
            response.sendRedirect("login.jsp?success=1");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp?error=subscription_failed");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.sendRedirect("login.jsp?error=missing_fields");
            return;
        }

        try (Connection conn = DatabaseServlet.getConnection()) {
            String hashedPassword = hashPassword(password);

            // Check if the maximum number of players is reached
            String countConnectedSql = "SELECT COUNT(*) AS connected_count FROM player WHERE is_connected = TRUE";
            try (PreparedStatement countStmt = conn.prepareStatement(countConnectedSql);
                 ResultSet rs = countStmt.executeQuery()) {
                if (rs.next() && rs.getInt("connected_count") >= 4) {
                    response.sendRedirect("login.jsp?error=max_players_reached");
                    return;
                }
            }

            String sql = "SELECT id, username, session_id FROM player WHERE email = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, hashedPassword);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int playerId = rs.getInt("id");
                        String username = rs.getString("username");
                        String currentSessionId = rs.getString("session_id");

                        HttpSession session = request.getSession();
                        String newSessionId = session.getId();

                        // Update session ID and connection status only if necessary
                        if (!newSessionId.equals(currentSessionId)) {
                            String updateSessionSql = "UPDATE player SET session_id = ?, is_connected = TRUE WHERE id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateSessionSql)) {
                                updateStmt.setString(1, newSessionId);
                                updateStmt.setInt(2, playerId);
                                updateStmt.executeUpdate();
                            }
                        }

                        Joueur joueur = new Joueur(playerId, username);
                        session.setAttribute("playerData", joueur);

                        response.sendRedirect("mainMenu.jsp");
                    } else {
                        response.sendRedirect("login.jsp?error=invalid_credentials");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp?error=server_error");
        }
    }


    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}
