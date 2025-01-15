package com.projet.utils;

import com.projet.model.Carte;
import com.projet.model.Joueur;
import com.projet.model.Soldat;
import com.projet.model.TileContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class GameUtils {
    public static void placeNewSoldier(Connection conn, Carte carte, Integer playerId, Joueur joueur) throws Exception {
        String username = joueur.getName();
        int x, y;

        boolean placed = false;
        while (!placed) {
            x = new Random().nextInt(8);
            y = new Random().nextInt(8);

            TileContainer container = carte.getTileContainer(x, y);
            if (container.isEmpty() || container.hasVille()) {
                Soldat newSoldat = new Soldat(x, y, username, playerId);
                joueur.addSoldat(newSoldat); // Add soldier to player

                // Save soldier to the database
                String insertSoldierSql = "INSERT INTO soldier (player_id, x, y, health_points) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertSoldierSql)) {
                    stmt.setInt(1, playerId);
                    stmt.setInt(2, x);
                    stmt.setInt(3, y);
                    stmt.setInt(4, newSoldat.getHealthPoints());
                    stmt.executeUpdate();
                }

                // Place soldier on the map
                carte.placeSoldier(x, y, newSoldat);
                placed = true;
            }
        }
    }
}
