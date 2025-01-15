package com.projet.controller;

import com.projet.model.*;
import com.projet.turn.TurnManager;
import com.projet.utils.GameUtils;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ActionsController extends HttpServlet {
    private static final int MAP_SIZE = 8;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Joueur joueur = (Joueur) session.getAttribute("playerData");
        if (joueur == null) {
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Player data not found in session.\"}");
            return;
        }

        Integer playerId = joueur.getId();
        String playerName = joueur.getName();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Action not specified.\"}");
            return;
        }

        try (Connection conn = DatabaseServlet.getConnection()) {

            if ("logout".equals(action)) {
                // 3. Remove playerData from the session
                session.removeAttribute("playerData");

                // 4. Return JSON telling front-end to redirect
                response.getWriter().write(
                        "{\"status\":\"success\", " +
                                "\"message\":\"Logout successful.\", " +
                                "\"redirectUrl\":\"login.jsp\"}"
                );
                return;
            }

            if ("force_win".equals(action)) {
                finalizeAndRemove(conn, joueur, "you_win", "Félicitations, vous avez gagné !", response, session);
                return;
            }

            // Step 1: Get the current position of the player's soldier
            if ("heal".equals(action)) {
                if (joueur.getProductionPoints() >= 10) {
                    joueur.spendProductionPoints(10);
                    healthManager(conn,playerId,"heal");
                    session.setAttribute("playerData", joueur);
                    response.getWriter().write("{\"status\":\"success\", \"message\":\"You have been healed.\", \"playerData\": {\"productionPoints\": " + joueur.getProductionPoints() + "}}");
                    TurnManager.getInstance().endCurrentPlayerTurnImmediately();
                } else {
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Not enough production points to heal.\"}");
                }

                return;
            }

            int currentX = 0, currentY = 0;
            String getPositionSql = "SELECT x, y FROM soldier WHERE player_id = ?";
            try (PreparedStatement getStmt = conn.prepareStatement(getPositionSql)) {
                getStmt.setInt(1, playerId);
                try (ResultSet rs = getStmt.executeQuery()) {
                    if (rs.next()) {
                        currentX = rs.getInt("x");
                        currentY = rs.getInt("y");
                    } else {
                        response.getWriter().write("{\"status\":\"error\", \"message\":\"Player soldier not found.\"}");
                        return;
                    }
                }
            }
            if ("add_soldier".equals(action)) {
                if (joueur.getProductionPoints() < 15) {
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Not enough production points to add a soldier.\"}");
                    return;
                }

                joueur.spendProductionPoints(15); // Deduct production points

                // Place a new soldier
                try {
                    GameUtils.placeNewSoldier(conn, GameStateController.carte, playerId, joueur);
                    session.setAttribute("playerData", joueur);

                    response.getWriter().write("{\"status\":\"success\", \"message\":\"New soldier added.\", \"playerData\": {\"productionPoints\": "
                            + joueur.getProductionPoints() + "}}");
                    TurnManager.getInstance().endCurrentPlayerTurnImmediately();
                } catch (Exception e) {
                    e.printStackTrace();
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to add a new soldier.\"}");
                }

                return;
            }


            if ("produce_unit".equals(action)) {
                // Get the tile where the soldier currently is
                TileContainer currentTile = GameStateController.carte.getTileContainer(currentX, currentY);

                if (currentTile != null && currentTile.hasForet()) {
                    joueur.addScore(10);
                    joueur.addProductionPoints(5);

                    session.setAttribute("playerData", joueur);

                    response.getWriter().write("{\"status\":\"success\", "
                            + "\"message\":\"Unit produced on forest tile.\", "
                            + "\"playerData\": {"
                            + "\"productionPoints\": " + joueur.getProductionPoints() + ", "
                            + "\"score\": " + joueur.getScore()
                            + "}}");
                    TurnManager.getInstance().endCurrentPlayerTurnImmediately();

                } else {
                    response.getWriter().write("{\"status\":\"error\", "
                            + "\"message\":\"Cannot produce a unit here (not a forest tile).\"}");
                }

                return;
            }

            // Step 2: Determine the new position based on the action
            int newX = currentX, newY = currentY;
            switch (action) {
                case "move_north":
                    if (currentY > 0) newY--;
                    break;
                case "move_south":
                    if (currentY < MAP_SIZE - 1) newY++;
                    break;
                case "move_east":
                    if (currentX < MAP_SIZE - 1) newX++;
                    break;
                case "move_west":
                    if (currentX > 0) newX--;
                    break;
                default:
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Invalid action specified.\"}");
                    return;
            }

            // Step 3: Check the new position in the container
            TileContainer targetTile = GameStateController.carte.getTileContainer(newX, newY);

            if (targetTile == null) {
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Target position is invalid.\"}");
                return;
            }

            if (targetTile.hasMontagne()) {
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Cannot move through Montagne.\"}");
                return;
            }
            if (targetTile.hasVille()) {
                Ville ville = targetTile.getVille();
                if (ville.getOwnerId() == playerId || ville.getOwnerId() == -1) {
                    if (ville.isCaptured()) {
                        if (ville.getOwnerId() == -1) {
                            // This means you're the first to take ownership -> one-time capture bonus
                            ville.setOwnerId(playerId);
                            String updateOwnerSql = "UPDATE ville SET owner_id = ? WHERE x = ? AND y = ?";
                            try (PreparedStatement stmt = conn.prepareStatement(updateOwnerSql)) {
                                stmt.setInt(1, playerId);
                                stmt.setInt(2, newX);
                                stmt.setInt(3, newY);
                                stmt.executeUpdate();
                            }

                            // Give capture rewards only once
                            joueur.addScore(50);
                            joueur.addProductionPoints(15);
                            session.setAttribute("playerData", joueur);

                            moveSoldier(conn, playerId, currentX, currentY, newX, newY);
                            response.getWriter().write("{\"status\":\"success\", \"message\":\"Ville captured successfully.\", "
                                    + "\"playerData\": {\"productionPoints\": " + joueur.getProductionPoints() + ", \"score\": " + joueur.getScore() + "}}");
                            TurnManager.getInstance().endCurrentPlayerTurnImmediately();

                            return;
                        } else {
                            // If the Ville is already captured and owned by you, just move in. No extra points.
                            moveSoldier(conn, playerId, currentX, currentY, newX, newY);
                            response.getWriter().write("{\"status\":\"info\", \"message\":\"You have entered your already-captured Ville.\", "
                                    + "\"playerData\": {\"productionPoints\": " + joueur.getProductionPoints() +
                                      ", \"score\": " + joueur.getScore() + "}}");
                            TurnManager.getInstance().endCurrentPlayerTurnImmediately();

                            return;
                        }
                    } else {
                        // Ville has defense points left; deal damage but prevent movement
                        int randomDamage = ThreadLocalRandom.current().nextInt(1, 7);
                        ville.takeDamage(randomDamage);
                        healthManager(conn, playerId, "ville");
                        joueur.addProductionPoints(2);
                        session.setAttribute("playerData", joueur);

                        if (isSoldierDead(conn, playerId)) {

                            finalizeAndRemove(conn, joueur, "game_over", "Game Over!", response, session);
                            return;
                        }

                        String updateDefenseSql = "UPDATE ville SET defense_points = ? WHERE x = ? AND y = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateDefenseSql)) {
                            stmt.setInt(1, ville.getDefensePoints());
                            stmt.setInt(2, newX);
                            stmt.setInt(3, newY);
                            stmt.executeUpdate();
                        }
                        response.getWriter().write("{\"status\":\"info\", \"message\":\"Ville damaged. Defense remaining: " + ville.getDefensePoints() + "\"}");
                        TurnManager.getInstance().endCurrentPlayerTurnImmediately();

                        return;
                    }
                } else {
                    // Ville owned by another player
                    response.getWriter().write("{\"status\":\"info\", \"message\":\"Ville owned by another player.\"}");
                    return;
                }
            }

            if (targetTile.getSoldat() != null) {
                healthManager(conn, playerId, "otherSoldat");
                healthManager(conn, targetTile.getSoldat().getPlayerId(), "attackSoldier");

                if (isSoldierDead(conn, playerId)) {
                    finalizeAndRemove(conn, joueur, "game_over", "Game Over!", response, session);

                    return;
                }
                session.setAttribute("playerData", joueur);
                response.getWriter().write("{\"status\":\"info\", \"message\":\"You took damage from another player.}");
                TurnManager.getInstance().endCurrentPlayerTurnImmediately();

                return;
            }

            if (targetTile.hasForet()) {
                // Allow movement into a Foret
                moveSoldier(conn, playerId, currentX, currentY, newX, newY);
                response.getWriter().write("{\"status\":\"success\", \"message\":\"Moved into Foret.\"}");
                TurnManager.getInstance().endCurrentPlayerTurnImmediately();

                return;
            }

            // Step 4: Move the soldier
            moveSoldier(conn, playerId, currentX, currentY, newX, newY);
            response.getWriter().write(String.format("{\"status\":\"success\", \"x\":%d, \"y\":%d}", newX, newY));
            TurnManager.getInstance().endCurrentPlayerTurnImmediately();


        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\", \"message\":\"An error occurred: " + e.getMessage() + "\"}");
        }
    }

    private void healthManager(Connection conn, Integer playerId, String type) throws Exception {
        if (playerId == null || type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments: playerId and type must be provided.");
        }

        // Query to find and update the soldier's health
        String selectSoldierSql = "SELECT id, health_points FROM soldier WHERE player_id = ? LIMIT 1";
        String updateHealthSql = "UPDATE soldier SET health_points = ? WHERE id = ?";

        // Generate the adjustment amount based on the type
        int adjustment;
        if ("ville".equalsIgnoreCase(type)) {
            adjustment = -5; // Subtract 5 points for damage
        } else if ("attackSoldier".equalsIgnoreCase(type)) {
            adjustment = -(new Random().nextInt(21) + 10);
        } else if ("heal".equalsIgnoreCase(type)) {
            adjustment = 15; // Add 15 points for healing
        } else {
            adjustment = 0;
        }

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSoldierSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateHealthSql)) {

            // Step 1: Find the soldier
            selectStmt.setInt(1, playerId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int soldierId = rs.getInt("id");
                    int currentHealth = rs.getInt("health_points");

                    // Step 2: Calculate the new health
                    int newHealth;
                    if ("heal".equalsIgnoreCase(type)) {
                        newHealth = Math.min(100, currentHealth + adjustment); // Cap health at 100 for healing
                    } else {
                        newHealth = Math.max(0, currentHealth + adjustment); // Ensure health does not drop below 0
                    }

                    // Step 3: Update the soldier's health in the database
                    updateStmt.setInt(1, newHealth);
                    updateStmt.setInt(2, soldierId);
                    updateStmt.executeUpdate();

                    System.out.printf("Soldier %d's health updated: %d -> %d (Adjustment: %d)%n",
                            soldierId, currentHealth, newHealth, adjustment);
                } else {
                    throw new Exception("No soldier found for the given playerId: " + playerId);
                }
            }
        }
    }


    private boolean isSoldierDead(Connection conn, Integer playerId) throws Exception {
        if (playerId == null) {
            throw new IllegalArgumentException("Invalid argument: playerId must be provided.");
        }

        // Query to get the soldier's health points for the given player ID
        String selectSoldierSql = "SELECT health_points FROM soldier WHERE player_id = ? LIMIT 1";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSoldierSql)) {
            selectStmt.setInt(1, playerId);

            // Execute the query and process the result
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int healthPoints = rs.getInt("health_points");

                    // Return true if health is <= 0, false otherwise
                    return healthPoints <= 0;
                } else {
                    throw new Exception("No soldier found for the given playerId: " + playerId);
                }
            }
        }
    }


    /**
     * If a player is dead or forced to end, we finalize stats and remove them from the game.
     */
    private void finalizeAndRemove(
            Connection conn, Joueur joueur,
            String status, String msg,
            HttpServletResponse response,
            HttpSession session
    ) throws Exception {
        updatePlayerTotals(conn,joueur);

        // read final DB
        int finalScore=0, finalProd=0, finalUnits=0;
        try (PreparedStatement st=conn.prepareStatement(
                "SELECT score, production_points, unités_et_villes FROM player WHERE id=?"
        )) {
            st.setInt(1, joueur.getId());
            try(ResultSet rs=st.executeQuery()) {
                if (rs.next()) {
                    finalScore=rs.getInt("score");
                    finalProd=rs.getInt("production_points");
                    finalUnits=rs.getInt("unités_et_villes");
                }
            }
        }

        String endReason=("you_win".equals(status))?"win":"lose";

        // single game stats
        int gScore   = joueur.getScore();
        int gProd    = joueur.getProductionPoints();
        int gCities  = joueur.getVilles().size();

        StatsSnapshot finalData = new StatsSnapshot(
                joueur.getName(),
                gScore,         // single-game
                gProd,          // single-game
                gCities,        // single-game
                finalScore,     // DB total
                finalProd,      // DB total
                finalUnits,     // DB total
                endReason       // "win" or "lose"
        );
        session.setAttribute("finalData", finalData);

        removePlayerFromGame(conn, joueur.getId(), session);

        response.getWriter().write("{\"status\":\""+status+"\",\"message\":\""+msg+"\",\"redirectUrl\":\"game-over.jsp\"}");
    }

    private void updatePlayerTotals(Connection conn, Joueur joueur) throws Exception {
        // get old
        int oldScore=0, oldProd=0, oldUnits=0;
        try(PreparedStatement s=conn.prepareStatement(
                "SELECT score,production_points,unités_et_villes FROM player WHERE id=?"
        )) {
            s.setInt(1, joueur.getId());
            try(ResultSet rs=s.executeQuery()){
                if(rs.next()){
                    oldScore=rs.getInt("score");
                    oldProd=rs.getInt("production_points");
                    oldUnits=rs.getInt("unités_et_villes");
                }
            }
        }
        int newScore = oldScore + joueur.getScore();
        int newProd  = oldProd  + joueur.getProductionPoints();
        int newUnits = oldUnits + joueur.getVilles().size();

        try(PreparedStatement st=conn.prepareStatement(
                "UPDATE player SET score=?, production_points=?, unités_et_villes=? WHERE id=?"
        )){
            st.setInt(1, newScore);
            st.setInt(2, newProd);
            st.setInt(3, newUnits);
            st.setInt(4, joueur.getId());
            st.executeUpdate();
        }
    }
    // -----------------------------------------------------------
    // HELPER: Remove player from game when dead
    // -----------------------------------------------------------
    private void removePlayerFromGame(Connection conn, int pid, HttpSession session) throws Exception {
        try(PreparedStatement st=conn.prepareStatement("DELETE FROM soldier WHERE player_id=?")){
            st.setInt(1, pid);
            st.executeUpdate();
        }
        try(PreparedStatement st=conn.prepareStatement(
                "UPDATE player SET is_connected=0 WHERE id=?"
        )) {
            st.setInt(1, pid);
            st.executeUpdate();
        }
        session.removeAttribute("playerData");
    }


    private void moveSoldier(Connection conn, int playerId, int oldX, int oldY, int newX, int newY) throws Exception {
        // Update soldier position in the database
        String updatePositionSql = "UPDATE soldier SET x = ?, y = ? WHERE player_id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updatePositionSql)) {
            updateStmt.setInt(1, newX);
            updateStmt.setInt(2, newY);
            updateStmt.setInt(3, playerId);
            updateStmt.executeUpdate();
        }

        // Update soldier position in the session map
        synchronized (GameStateController.carte) {
            GameStateController.carte.updateSoldatPosition(oldX, oldY, newX, newY);
        }
    }
}