package com.projet.model;

import java.util.Random;

public class Carte {
    private final int width;
    private final int height;
    private final TileContainer[][] grid;

    public Carte(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new TileContainer[width][height];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new TileContainer();
            }
        }
        placeRandomObjects(TuileType.VILLE, 3);
        placeRandomObjects(TuileType.MONTAGNE, 5);
        placeRandomObjects(TuileType.FORET, 5);
    }

    private void placeRandomObjects(TuileType type, int count) {
        Random random = new Random();
        int placed = 0;

        while (placed < count) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            TileContainer container = grid[x][y];

            if (container.isEmpty()) {
                switch (type) {
                    case VILLE -> container.setVille(new Ville(x, y, 30));
                    case MONTAGNE -> container.setMontagne(new Montagne(x, y, "images/mountain.png"));
                    case FORET -> container.setForet(new Foret(x, y, 20, "images/forest.png"));
                }
                placed++;
            }
        }
    }

    public boolean isTileEmpty(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return false;
        }
        return grid[x][y].isEmpty();
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public TileContainer getTileContainer(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return grid[x][y];
    }

    public TileContainer[][] getGrid() {
        return grid;
    }

    public void placeSoldier(int x, int y, Soldat soldier) {
        if (!isWithinBounds(x, y)) {
            throw new IllegalArgumentException("Position out of bounds.");
        }
        TileContainer container = grid[x][y];
        if (container.getSoldat() != null) {
            throw new IllegalArgumentException("Tile already occupied by another soldier.");
        }
        container.setSoldat(soldier);
        logInfo(String.format("Soldier placed at (%d, %d)", x, y));
    }

    public void updateSoldatPosition(int oldX, int oldY, int newX, int newY) {
        if (!isWithinBounds(oldX, oldY) || !isWithinBounds(newX, newY)) {
            throw new IllegalArgumentException("Invalid move: Position out of bounds.");
        }

        TileContainer oldContainer = grid[oldX][oldY];
        TileContainer newContainer = grid[newX][newY];

        Soldat soldier = oldContainer.getSoldat();
        if (soldier == null) {
            logWarning(String.format("No soldier at (%d, %d)", oldX, oldY));
            return;
        }

        if (newContainer.getSoldat() != null) {
            logError(String.format("Tile (%d, %d) already occupied by another soldier.", newX, newY));
            throw new IllegalArgumentException("Target tile is already occupied.");
        }

        oldContainer.setSoldat(null); // Remove from old position
        newContainer.setSoldat(soldier); // Place at new position
        soldier.setX(newX);
        soldier.setY(newY);
        logInfo(String.format("Soldier moved from (%d, %d) to (%d, %d)", oldX, oldY, newX, newY));
    }

    public void resetSoldierPositions() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y].setSoldat(null);
            }
        }
        logInfo("All soldier positions have been reset.");
    }

    public void logInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public void logWarning(String message) {
        System.out.println("[WARNING] " + message);
    }

    public void logError(String message) {
        System.err.println("[ERROR] " + message);
    }
}
