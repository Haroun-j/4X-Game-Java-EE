-- Database used by the 4X game.
-- The three tables below are the ones the servlets query. The original dump
-- was not kept, so this file recreates them from the SQL in the controllers.

CREATE DATABASE IF NOT EXISTS `4x_game` CHARACTER SET utf8mb4;
USE `4x_game`;

-- One row per registered player. Score, production and captured cities are
-- accumulated across games by ActionsController.updatePlayerTotals().
CREATE TABLE IF NOT EXISTS player (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    username          VARCHAR(100) NOT NULL,
    pseudo_name       VARCHAR(100) NOT NULL,
    password          CHAR(64)     NOT NULL,          -- SHA-256 hex digest
    sexe              VARCHAR(10)  NOT NULL,
    session_id        VARCHAR(255),
    is_connected      TINYINT(1)   NOT NULL DEFAULT 0,
    score             INT          NOT NULL DEFAULT 0,
    production_points INT          NOT NULL DEFAULT 0,
    `unités_et_villes` INT         NOT NULL DEFAULT 0
);

-- Position and health of each soldier on the 8x8 map.
-- GameStateController creates one soldier per connected player.
CREATE TABLE IF NOT EXISTS soldier (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    player_id     INT NOT NULL,
    x             INT NOT NULL,
    y             INT NOT NULL,
    health_points INT NOT NULL DEFAULT 100,
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE
);

-- Cities are generated in memory by Carte; this table only receives the
-- UPDATEs sent when a city is damaged or captured, so it can stay empty.
CREATE TABLE IF NOT EXISTS ville (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    x              INT NOT NULL,
    y              INT NOT NULL,
    defense_points INT NOT NULL DEFAULT 30,
    owner_id       INT NULL,
    FOREIGN KEY (owner_id) REFERENCES player(id) ON DELETE SET NULL
);
