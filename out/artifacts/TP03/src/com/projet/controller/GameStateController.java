//
//package com.projet.controller;
//
//import com.projet.model.*;
//import jakarta.servlet.http.*;
//
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class GameStateController extends HttpServlet {
//    private static final int MAP_WIDTH = 8;
//    private static final int MAP_HEIGHT = 8;
//    private static final int MAX_PLAYERS = 4;
//    static final Carte carte = new Carte(MAP_WIDTH, MAP_HEIGHT); // Map object
//    private static int maxDistinctPlayersEver = 0;
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        try (Connection conn = DatabaseServlet.getConnection()) {
//            HttpSession session = request.getSession();
//            // Step 1: Ensure all players are placed
//            manageConnectedPlayers(conn, session);
//
//            // Step 2: Retrieve updated map and player data
//            String json = buildGameStateJson(conn, session);
//
//            // Step 3: Send the game state to the client
//            response.getWriter().write(json);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to retrieve game state.\"}");
//        }
//    }
//
//    private void manageConnectedPlayers(Connection conn, HttpSession session) throws Exception {
//        String fetchConnectedPlayersSql = "SELECT id, username FROM player WHERE is_connected = TRUE LIMIT ?";
//        String fetchSoldiersSql = "SELECT player_id, x, y, health_points FROM soldier WHERE player_id IN (SELECT id FROM player WHERE is_connected = TRUE)";
//
//        carte.resetSoldierPositions(); // Clear the map to avoid duplicates
//        Joueur joueur = (Joueur) session.getAttribute("playerData");
//        // Fetch and replace existing soldiers
//        try (PreparedStatement stmt = conn.prepareStatement(fetchSoldiersSql)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    int playerId = rs.getInt("player_id");
//                    int x = rs.getInt("x");
//                    int y = rs.getInt("y");
//                    int health = rs.getInt("health_points");
//                    String username = fetchUsernameById(conn, playerId);
//
//                    try {
//                        Soldat soldier = new Soldat(x, y, username, playerId);
//                        carte.placeSoldier(x, y, soldier);
//
//                        if (joueur != null && joueur.getId() == playerId) {
//                            // Check if soldier already exists in session
//                            Soldat existingSoldier = joueur.getSoldats().stream()
//                                    .filter(s -> s.getPlayerId() == playerId)
//                                    .findFirst()
//                                    .orElse(null);
//
//                            if (existingSoldier != null) {
//                                // Update the soldier's attributes
//                                existingSoldier.setX(x);
//                                existingSoldier.setY(y);
//                            } else {
//                                // Add a new soldier if not already present
//                                Soldat newSoldat = new Soldat(x, y, username, playerId);
//                                joueur.addSoldat(newSoldat);
//                            }
//
//                            session.setAttribute("playerData", joueur);
//                        }
//
//                    } catch (IllegalArgumentException e) {
//                        logError(String.format("Failed to place existing soldier for player %d at (%d, %d): %s", playerId, x, y, e.getMessage()));
//                    }
//                }
//            }
//        }
//
//        // Place new soldiers for players without existing ones
//        List<Integer> connectedPlayerIds = getConnectedPlayerIds(conn, fetchConnectedPlayersSql);
//        for (int playerId : connectedPlayerIds) {
//            if (!isSoldierExists(conn, playerId)) {
//                try {
//                    placeNewSoldier(conn, playerId, session);
//                } catch (Exception e) {
//                    logError(String.format("Failed to place new soldier for player %d: %s", playerId, e.getMessage()));
//                }
//            }
//        }
//        java.util.Set<Integer> distinctPlayerIds = new java.util.HashSet<>();
//        TileContainer[][] grid = carte.getGrid();
//        for (int x = 0; x < MAP_WIDTH; x++) {
//            for (int y = 0; y < MAP_HEIGHT; y++) {
//                if (grid[x][y].hasSoldat()) {
//                    distinctPlayerIds.add(grid[x][y].getSoldat().getPlayerId());
//                }
//            }
//        }
//        // If the current count is bigger than the existing max, update it
//        if (distinctPlayerIds.size() > maxDistinctPlayersEver) {
//            maxDistinctPlayersEver = distinctPlayerIds.size();
//        }
//    }
//
//    private List<Integer> getConnectedPlayerIds(Connection conn, String sql) throws Exception {
//        List<Integer> connectedPlayerIds = new ArrayList<>();
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, MAX_PLAYERS);
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    connectedPlayerIds.add(rs.getInt("id"));
//                }
//            }
//        }
//        return connectedPlayerIds;
//    }
//
//    private boolean isSoldierExists(Connection conn, int playerId) throws Exception {
//        String sql = "SELECT 1 FROM soldier WHERE player_id = ?";
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, playerId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                return rs.next();
//            }
//        }
//    }
//
//    private void placeNewSoldier(Connection conn, int playerId, HttpSession session) throws Exception {
//        String username = fetchUsernameById(conn, playerId);
//        int x, y;
//
//        boolean placed = false;
//        while (!placed) {
//            x = (int) (Math.random() * MAP_WIDTH);
//            y = (int) (Math.random() * MAP_HEIGHT);
//
//            TileContainer container = carte.getTileContainer(x, y);
//            if (container.isEmpty() || container.hasVille()) {
//                Joueur joueur = (Joueur) session.getAttribute("playerData");
//
//                if (joueur != null && joueur.getId() == playerId) {
//                    // Check if the player already has a soldier in the session
//                    Soldat existingSoldier = joueur.getSoldats().stream()
//                            .filter(s -> s.getPlayerId() == playerId)
//                            .findFirst()
//                            .orElse(null);
//
//                    if (existingSoldier != null) {
//                        existingSoldier.setX(x);
//                        existingSoldier.setY(y);
//                    } else {
//                        Soldat newSoldat = new Soldat(x, y, username, playerId);
//                        joueur.addSoldat(newSoldat);
//
//                        String insertSoldierSql = "INSERT INTO soldier (player_id, x, y, health_points) VALUES (?, ?, ?, ?)";
//                        try (PreparedStatement stmt = conn.prepareStatement(insertSoldierSql)) {
//                            stmt.setInt(1, playerId);
//                            stmt.setInt(2, x);
//                            stmt.setInt(3, y);
//                            stmt.setInt(4, newSoldat.getHealthPoints());
//                            stmt.executeUpdate();
//                        }
//
//                        placed = true;
//                    }
//                    session.setAttribute("playerData", joueur);
//                }
//                Soldat newSoldat = new Soldat(x, y, username, playerId);
//                carte.placeSoldier(x, y, newSoldat);
////                placed = true;
//
////                String insertSoldierSql = "INSERT INTO soldier (player_id, x, y, health_points) VALUES (?, ?, ?, ?)";
////                try (PreparedStatement stmt = conn.prepareStatement(insertSoldierSql)) {
////                    stmt.setInt(1, playerId);
////                    stmt.setInt(2, x);
////                    stmt.setInt(3, y);
////                    stmt.setInt(4, newSoldat.getHealthPoints());
////                    stmt.executeUpdate();
////                }
//            }
//        }
//    }
//
//    private String fetchUsernameById(Connection conn, int playerId) throws Exception {
//        String sql = "SELECT username FROM player WHERE id = ?";
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, playerId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getString("username");
//                } else {
//                    throw new Exception("Player not found: " + playerId);
//                }
//            }
//        }
//    }
//
//    private String buildGameStateJson(Connection conn, HttpSession session) {
//        StringBuilder json = new StringBuilder("{ \"map\": [");
//
//        TileContainer[][] grid = carte.getGrid();
//        for (int i = 0; i < MAP_WIDTH; i++) {
//            for (int j = 0; j < MAP_HEIGHT; j++) {
//                TileContainer container = grid[i][j];
//
//                if (container.hasVille()) {
//                    Ville ville = container.getVille();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"VILLE\", \"defensePoints\":%d, \"ownerId\":%d, \"image\":\"images/city.png\"},",
//                            i, j, ville.getDefensePoints(), ville.getOwnerId()
//                    ));
//                }
//
//                if (container.hasSoldat()) {
//                    Soldat soldat = container.getSoldat();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"SOLDAT\", \"name\":\"%s\", \"playerId\":%d,\"healthPoints\":%d, \"image\":\"images/soldier.png\"},",
//                            i, j, soldat.getName(), soldat.getPlayerId(), soldat.getHealthPoints()
//                    ));
//                }
//
//                if (container.hasForet()) {
//                    Foret foret = container.getForet();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"FORET\", \"resourceAmount\":%d, \"image\":\"images/forest.png\"},",
//                            i, j, foret.getResourceAmount()
//                    ));
//                }
//
//                if (container.hasMontagne()) {
//                    Montagne montagne = container.getMontagne();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"MONTAGNE\", \"image\":\"images/mountain.png\"},",
//                            i, j
//                    ));
//                }
//            }
//        }
//
//        if (json.charAt(json.length() - 1) == ',') {
//            json.deleteCharAt(json.length() - 1);
//        }
//        json.append("], ");
//
//        // Add player data from session
////        Joueur currentPlayer = (Joueur) session.getAttribute("playerData");
////        json.append("\"playerData\": ");
////        if (currentPlayer != null) {
////            json.append(String.format(
////                    "{\"id\":%d, \"name\":\"%s\", \"score\":%d, \"productionPoints\":%d, \"healthPoints\":%d}",
////                    currentPlayer.getId(), currentPlayer.getName(), currentPlayer.getScore(),
////                    currentPlayer.getProductionPoints(), currentPlayer.getHealthPoints()
////            ));
////        } else {
////            json.append("null");
////        }
//
//
//        Joueur currentPlayer = (Joueur) session.getAttribute("playerData");
//
//        json.append("\"playerData\": ");
//        if (currentPlayer != null) {
//            json.append(String.format(
//                    "{\"id\":%d, \"name\":\"%s\", \"score\":%d, \"productionPoints\":%d, \"healthPoints\":%d, \"soldats\":[",
//                    currentPlayer.getId(), currentPlayer.getName(), currentPlayer.getScore(),
//                    currentPlayer.getProductionPoints(), currentPlayer.getHealthPoints()
//            ));
//
//            // Include soldier details in the JSON
//            for (Soldat soldat : currentPlayer.getSoldats()) {
//                json.append(String.format(
//                        "{\"soldierId\":%d, \"name\":\"%s\", \"playerId\":%d, \"healthPoints\":%d, \"x\":%d, \"y\":%d},",
//                        soldat.getSoldierId(), soldat.getName(), soldat.getPlayerId(),
//                        soldat.getHealthPoints(), soldat.getX(), soldat.getY()
//                ));
//            }
//
//            // Remove trailing comma if there are soldiers
//            if (!currentPlayer.getSoldats().isEmpty()) {
//                json.deleteCharAt(json.length() - 1);
//            }
//
//            json.append("]}"); // Close the soldats array and playerData object
//        } else {
//            json.append("null");
//        }
//
//
//        if (currentPlayer != null && maxDistinctPlayersEver > 1) {
//            // 1) See how many distinct player IDs are in the map now
//            java.util.Set<Integer> alivePlayers = new java.util.HashSet<>();
//            for (int x = 0; x < MAP_WIDTH; x++) {
//                for (int y = 0; y < MAP_HEIGHT; y++) {
//                    if (grid[x][y].hasSoldat()) {
//                        alivePlayers.add(grid[x][y].getSoldat().getPlayerId());
//                    }
//                }
//            }
//            // 2) If exactly 1 left and it's me => "you_win"
//            if (alivePlayers.size() == 1 && alivePlayers.contains(currentPlayer.getId())) {
//                json.append(", \"status\":\"you_win\", "
//                        + "\"message\":\"Félicitations, vous êtes le dernier joueur !\", "
//                        + "\"redirectUrl\":\"game-over.jsp\"");
//            }
//        }
//
//        json.append("}");
//
//        return json.toString();
//    }
//
//    private void logError(String message) {
//        System.err.println("[ERROR] " + message);
//    }
//}






package com.projet.controller;

import com.projet.model.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameStateController extends HttpServlet {
    private static final int MAP_WIDTH = 8;
    private static final int MAP_HEIGHT = 8;
    private static final int MAX_PLAYERS = 4;
    static final Carte carte = new Carte(MAP_WIDTH, MAP_HEIGHT); // Map object
    private static int maxDistinctPlayersEver = 0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DatabaseServlet.getConnection()) {
            HttpSession session = request.getSession();
            // Step 1: Ensure all players are placed
            manageConnectedPlayers(conn);

            // Step 2: Retrieve updated map and player data
            String json = buildGameStateJson(conn, session);

            // Step 3: Send the game state to the client
            response.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to retrieve game state.\"}");
        }
    }

    private void manageConnectedPlayers(Connection conn) throws Exception {
        String fetchConnectedPlayersSql = "SELECT id, username FROM player WHERE is_connected = TRUE LIMIT ?";
        String fetchSoldiersSql = "SELECT player_id, x, y, health_points FROM soldier WHERE player_id IN (SELECT id FROM player WHERE is_connected = TRUE)";

        carte.resetSoldierPositions(); // Clear the map to avoid duplicates

        // Fetch and replace existing soldiers
        try (PreparedStatement stmt = conn.prepareStatement(fetchSoldiersSql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int playerId = rs.getInt("player_id");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int health = rs.getInt("health_points");
                    String username = fetchUsernameById(conn, playerId);

                    try {
                        Soldat soldier = new Soldat(x, y, username, playerId);
                        soldier.setHealthPoints(health);
                        carte.placeSoldier(x, y, soldier);
                    } catch (IllegalArgumentException e) {
                        logError(String.format("Failed to place existing soldier for player %d at (%d, %d): %s", playerId, x, y, e.getMessage()));
                    }
                }
            }
        }

        // Place new soldiers for players without existing ones
        List<Integer> connectedPlayerIds = getConnectedPlayerIds(conn, fetchConnectedPlayersSql);
        for (int playerId : connectedPlayerIds) {
            if (!isSoldierExists(conn, playerId)) {
                try {
                    placeNewSoldier(conn, playerId);
                } catch (Exception e) {
                    logError(String.format("Failed to place new soldier for player %d: %s", playerId, e.getMessage()));
                }
            }
        }
        java.util.Set<Integer> distinctPlayerIds = new java.util.HashSet<>();
        TileContainer[][] grid = carte.getGrid();
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                if (grid[x][y].hasSoldat()) {
                    distinctPlayerIds.add(grid[x][y].getSoldat().getPlayerId());
                }
            }
        }
        // If the current count is bigger than the existing max, update it
        if (distinctPlayerIds.size() > maxDistinctPlayersEver) {
            maxDistinctPlayersEver = distinctPlayerIds.size();
        }
    }

    private List<Integer> getConnectedPlayerIds(Connection conn, String sql) throws Exception {
        List<Integer> connectedPlayerIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, MAX_PLAYERS);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    connectedPlayerIds.add(rs.getInt("id"));
                }
            }
        }
        return connectedPlayerIds;
    }

    private boolean isSoldierExists(Connection conn, int playerId) throws Exception {
        String sql = "SELECT 1 FROM soldier WHERE player_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void placeNewSoldier(Connection conn, int playerId) throws Exception {
        String username = fetchUsernameById(conn, playerId);
        int x, y;

        boolean placed = false;
        while (!placed) {
            x = (int) (Math.random() * MAP_WIDTH);
            y = (int) (Math.random() * MAP_HEIGHT);

            TileContainer container = carte.getTileContainer(x, y);
            if (container.isEmpty() || container.hasVille()) {
                Soldat newSoldat = new Soldat(x, y, username, playerId);
                carte.placeSoldier(x, y, newSoldat);
                placed = true;

                String insertSoldierSql = "INSERT INTO soldier (player_id, x, y, health_points) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertSoldierSql)) {
                    stmt.setInt(1, playerId);
                    stmt.setInt(2, x);
                    stmt.setInt(3, y);
                    stmt.setInt(4, newSoldat.getHealthPoints());
                    stmt.executeUpdate();
                }
            }
        }
    }

    private String fetchUsernameById(Connection conn, int playerId) throws Exception {
        String sql = "SELECT username FROM player WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                } else {
                    throw new Exception("Player not found: " + playerId);
                }
            }
        }
    }

    private String buildGameStateJson(Connection conn, HttpSession session) {
        StringBuilder json = new StringBuilder("{ \"map\": [");

        TileContainer[][] grid = carte.getGrid();
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                TileContainer container = grid[i][j];

                if (container.hasVille()) {
                    Ville ville = container.getVille();
                    json.append(String.format(
                            "{\"x\":%d, \"y\":%d, \"type\":\"VILLE\", \"defensePoints\":%d, \"ownerId\":%d, \"image\":\"images/city.png\"},",
                            i, j, ville.getDefensePoints(), ville.getOwnerId()
                    ));
                }

                if (container.hasSoldat()) {
                    Soldat soldat = container.getSoldat();
                    json.append(String.format(
                            "{\"x\":%d, \"y\":%d, \"type\":\"SOLDAT\", \"name\":\"%s\", \"playerId\":%d,\"healthPoints\":%d, \"image\":\"images/soldier.png\"},",
                            i, j, soldat.getName(), soldat.getPlayerId(), soldat.getHealthPoints()
                    ));
                }

                if (container.hasForet()) {
                    Foret foret = container.getForet();
                    json.append(String.format(
                            "{\"x\":%d, \"y\":%d, \"type\":\"FORET\", \"resourceAmount\":%d, \"image\":\"images/forest.png\"},",
                            i, j, foret.getResourceAmount()
                    ));
                }

                if (container.hasMontagne()) {
                    Montagne montagne = container.getMontagne();
                    json.append(String.format(
                            "{\"x\":%d, \"y\":%d, \"type\":\"MONTAGNE\", \"image\":\"images/mountain.png\"},",
                            i, j
                    ));
                }
            }
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("], ");

        // Add player data from session
        Joueur currentPlayer = (Joueur) session.getAttribute("playerData");
        json.append("\"playerData\": ");
        if (currentPlayer != null) {
            json.append(String.format(
                    "{\"id\":%d, \"name\":\"%s\", \"score\":%d, \"productionPoints\":%d}",
                    currentPlayer.getId(), currentPlayer.getName(), currentPlayer.getScore(),
                    currentPlayer.getProductionPoints()
            ));
        } else {
            json.append("null");
        }


        if (currentPlayer != null && maxDistinctPlayersEver > 1) {
            // 1) See how many distinct player IDs are in the map now
            java.util.Set<Integer> alivePlayers = new java.util.HashSet<>();
            for (int x = 0; x < MAP_WIDTH; x++) {
                for (int y = 0; y < MAP_HEIGHT; y++) {
                    if (grid[x][y].hasSoldat()) {
                        alivePlayers.add(grid[x][y].getSoldat().getPlayerId());
                    }
                }
            }
            // 2) If exactly 1 left and it's me => "you_win"
            if (alivePlayers.size() == 1 && alivePlayers.contains(currentPlayer.getId())) {
                json.append(", \"status\":\"you_win\", "
                        + "\"message\":\"Félicitations, vous êtes le dernier joueur !\", "
                        + "\"redirectUrl\":\"game-over.jsp\"");
            }
        }

        json.append("}");

        return json.toString();
    }
    private void logError(String message) {
        System.err.println("[ERROR] " + message);
    }
}





//package com.projet.controller;
//
//import com.projet.model.*;
//import jakarta.servlet.http.*;
//
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class GameStateController extends HttpServlet {
//    private static final int MAP_WIDTH = 8;
//    private static final int MAP_HEIGHT = 8;
//    private static final int MAX_PLAYERS = 4;
//    static final Carte carte = new Carte(MAP_WIDTH, MAP_HEIGHT); // Map object
//    private static int maxDistinctPlayersEver = 0;
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        try (Connection conn = DatabaseServlet.getConnection()) {
//            HttpSession session = request.getSession();
//            // Step 1: Ensure all players are placed
//            manageConnectedPlayers(conn);
//
//            // Step 2: Retrieve updated map and player data
//            String json = buildGameStateJson(conn, session);
//
//            // Step 3: Send the game state to the client
//            response.getWriter().write(json);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to retrieve game state.\"}");
//        }
//    }
//
//    private void manageConnectedPlayers(Connection conn) throws Exception {
//        String fetchConnectedPlayersSql = "SELECT id, username FROM player WHERE is_connected = TRUE LIMIT ?";
//        String fetchSoldiersSql = "SELECT player_id, x, y FROM soldier WHERE player_id IN (SELECT id FROM player WHERE is_connected = TRUE)";
//
//        carte.resetSoldierPositions(); // Clear the map to avoid duplicates
//
//        // Fetch and replace existing soldiers
//        try (PreparedStatement stmt = conn.prepareStatement(fetchSoldiersSql)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    int playerId = rs.getInt("player_id");
//                    int x = rs.getInt("x");
//                    int y = rs.getInt("y");
//                    String username = fetchUsernameById(conn, playerId);
//
//                    try {
//                        carte.placeSoldier(x, y, new Soldat(x, y, username, playerId));
//                    } catch (IllegalArgumentException e) {
//                        logError(String.format("Failed to place existing soldier for player %d at (%d, %d): %s", playerId, x, y, e.getMessage()));
//                    }
//                }
//            }
//        }
//
//        // Place new soldiers for players without existing ones
//        List<Integer> connectedPlayerIds = getConnectedPlayerIds(conn, fetchConnectedPlayersSql);
//        for (int playerId : connectedPlayerIds) {
//            if (!isSoldierExists(conn, playerId)) {
//                try {
//                    placeNewSoldier(conn, playerId);
//                } catch (Exception e) {
//                    logError(String.format("Failed to place new soldier for player %d: %s", playerId, e.getMessage()));
//                }
//            }
//        }
//        java.util.Set<Integer> distinctPlayerIds = new java.util.HashSet<>();
//        TileContainer[][] grid = carte.getGrid();
//        for (int x = 0; x < MAP_WIDTH; x++) {
//            for (int y = 0; y < MAP_HEIGHT; y++) {
//                if (grid[x][y].hasSoldat()) {
//                    distinctPlayerIds.add(grid[x][y].getSoldat().getPlayerId());
//                }
//            }
//        }
//        // If the current count is bigger than the existing max, update it
//        if (distinctPlayerIds.size() > maxDistinctPlayersEver) {
//            maxDistinctPlayersEver = distinctPlayerIds.size();
//        }
//    }
//
//    private List<Integer> getConnectedPlayerIds(Connection conn, String sql) throws Exception {
//        List<Integer> connectedPlayerIds = new ArrayList<>();
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, MAX_PLAYERS);
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    connectedPlayerIds.add(rs.getInt("id"));
//                }
//            }
//        }
//        return connectedPlayerIds;
//    }
//
//    private boolean isSoldierExists(Connection conn, int playerId) throws Exception {
//        String sql = "SELECT 1 FROM soldier WHERE player_id = ?";
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, playerId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                return rs.next();
//            }
//        }
//    }
//
//    private void placeNewSoldier(Connection conn, int playerId) throws Exception {
//        String username = fetchUsernameById(conn, playerId);
//        int x, y;
//
//        boolean placed = false;
//        while (!placed) {
//            x = (int) (Math.random() * MAP_WIDTH);
//            y = (int) (Math.random() * MAP_HEIGHT);
//
//            TileContainer container = carte.getTileContainer(x, y);
//            if (container.isEmpty() || container.hasVille()) {
//                carte.placeSoldier(x, y, new Soldat(x, y, username, playerId));
//                placed = true;
//
//                String insertSoldierSql = "INSERT INTO soldier (player_id, x, y) VALUES (?, ?, ?)";
//                try (PreparedStatement stmt = conn.prepareStatement(insertSoldierSql)) {
//                    stmt.setInt(1, playerId);
//                    stmt.setInt(2, x);
//                    stmt.setInt(3, y);
//                    stmt.executeUpdate();
//                }
//            }
//        }
//    }
//
//    private String fetchUsernameById(Connection conn, int playerId) throws Exception {
//        String sql = "SELECT username FROM player WHERE id = ?";
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, playerId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getString("username");
//                } else {
//                    throw new Exception("Player not found: " + playerId);
//                }
//            }
//        }
//    }
//
//    private String buildGameStateJson(Connection conn, HttpSession session) {
//        StringBuilder json = new StringBuilder("{ \"map\": [");
//
//        TileContainer[][] grid = carte.getGrid();
//        for (int i = 0; i < MAP_WIDTH; i++) {
//            for (int j = 0; j < MAP_HEIGHT; j++) {
//                TileContainer container = grid[i][j];
//
//                if (container.hasVille()) {
//                    Ville ville = container.getVille();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"VILLE\", \"defensePoints\":%d, \"ownerId\":%d, \"image\":\"images/city.png\"},",
//                            i, j, ville.getDefensePoints(), ville.getOwnerId()
//                    ));
//                }
//
//                if (container.hasSoldat()) {
//                    Soldat soldat = container.getSoldat();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"SOLDAT\", \"name\":\"%s\", \"playerId\":%d, \"image\":\"images/soldier.png\"},",
//                            i, j, soldat.getName(), soldat.getPlayerId()
//                    ));
//                }
//
//                if (container.hasForet()) {
//                    Foret foret = container.getForet();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"FORET\", \"resourceAmount\":%d, \"image\":\"images/forest.png\"},",
//                            i, j, foret.getResourceAmount()
//                    ));
//                }
//
//                if (container.hasMontagne()) {
//                    Montagne montagne = container.getMontagne();
//                    json.append(String.format(
//                            "{\"x\":%d, \"y\":%d, \"type\":\"MONTAGNE\", \"image\":\"images/mountain.png\"},",
//                            i, j
//                    ));
//                }
//            }
//        }
//
//        if (json.charAt(json.length() - 1) == ',') {
//            json.deleteCharAt(json.length() - 1);
//        }
//        json.append("], ");
//
//        // Add player data from session
//        Joueur currentPlayer = (Joueur) session.getAttribute("playerData");
//        json.append("\"playerData\": ");
//        if (currentPlayer != null) {
//            json.append(String.format(
//                    "{\"id\":%d, \"name\":\"%s\", \"score\":%d, \"productionPoints\":%d, \"healthPoints\":%d}",
//                    currentPlayer.getId(), currentPlayer.getName(), currentPlayer.getScore(),
//                    currentPlayer.getProductionPoints(), currentPlayer.getHealthPoints()
//            ));
//        } else {
//            json.append("null");
//        }
//
//
//        if (currentPlayer != null && maxDistinctPlayersEver > 1) {
//            // 1) See how many distinct player IDs are in the map now
//            java.util.Set<Integer> alivePlayers = new java.util.HashSet<>();
//            for (int x = 0; x < MAP_WIDTH; x++) {
//                for (int y = 0; y < MAP_HEIGHT; y++) {
//                    if (grid[x][y].hasSoldat()) {
//                        alivePlayers.add(grid[x][y].getSoldat().getPlayerId());
//                    }
//                }
//            }
//            // 2) If exactly 1 left and it's me => "you_win"
//            if (alivePlayers.size() == 1 && alivePlayers.contains(currentPlayer.getId())) {
//                json.append(", \"status\":\"you_win\", "
//                        + "\"message\":\"Félicitations, vous êtes le dernier joueur !\", "
//                        + "\"redirectUrl\":\"game-over.jsp\"");
//            }
//        }
//
//        json.append("}");
//
//        return json.toString();
//    }
//    private void logError(String message) {
//        System.err.println("[ERROR] " + message);
//    }
//
//}
