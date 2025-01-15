package com.projet.utils;

import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private static final ConcurrentHashMap<String, PlayerPosition> playerPositions = new ConcurrentHashMap<>();

    // Add a new player to the map
    public static void addPlayer(String playerName, int x, int y) {
        playerPositions.put(playerName, new PlayerPosition(x, y));
    }

    // Get a player's position
    public static PlayerPosition getPlayerPosition(String playerName) {
        return playerPositions.get(playerName);
    }

    // Update a player's position
    public static void updatePlayerPosition(String playerName, int x, int y) {
        playerPositions.put(playerName, new PlayerPosition(x, y));
    }

    // Get all player positions
    public static ConcurrentHashMap<String, PlayerPosition> getAllPlayerPositions() {
        return playerPositions;
    }
}