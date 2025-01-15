/* =========================================================================
   StatsSnapshot.java
   (Place it in com.projet.model)
   ========================================================================= */
package com.projet.model;

public class StatsSnapshot {

    // (A) Stats for this single game
    private final int gameScore;
    private final int gameProduction;
    private final int gameCities;

    // (B) Accumulated stats in DB
    private final int totalScore;
    private final int totalProduction;
    private final int totalUnitsEtVilles;

    // (C) End reason: "win" or "lose"
    private final String endReason;

    // (D) Player name
    private final String playerName;

    public StatsSnapshot(String playerName,
                         int gameScore,
                         int gameProduction,
                         int gameCities,
                         int totalScore,
                         int totalProduction,
                         int totalUnitsEtVilles,
                         String endReason) {
        this.playerName = playerName;
        this.gameScore = gameScore;
        this.gameProduction = gameProduction;
        this.gameCities = gameCities;
        this.totalScore = totalScore;
        this.totalProduction = totalProduction;
        this.totalUnitsEtVilles = totalUnitsEtVilles;
        this.endReason = endReason;
    }

    public String getPlayerName() { return playerName; }
    public int getGameScore() { return gameScore; }
    public int getGameProduction() { return gameProduction; }
    public int getGameCities() { return gameCities; }
    public int getTotalScore() { return totalScore; }
    public int getTotalProduction() { return totalProduction; }
    public int getTotalUnitsEtVilles() { return totalUnitsEtVilles; }
    public String getEndReason() { return endReason; }
}
