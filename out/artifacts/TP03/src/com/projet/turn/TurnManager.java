package com.projet.turn;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private static final TurnManager INSTANCE = new TurnManager();
    private static final int TURN_DURATION = 30;
    private static final long TICK_INTERVAL_MS = 1000;

    private final List<Integer> activePlayers = new ArrayList<>();
    private int currentIndex = 0;
    private int timeLeft = 0;

    private long lastTickTime = 0L;

    private TurnManager(){}

    public static TurnManager getInstance(){ return INSTANCE; }

    public synchronized void setActivePlayers(List<Integer> playerIds) {
        this.activePlayers.clear();
        this.activePlayers.addAll(playerIds);

        // Si currentIndex >= nb joueurs, on le ramène à 0
        if (currentIndex >= activePlayers.size()) {
            currentIndex = 0;
        }
        // Si la liste est vide => plus de tours
        if (activePlayers.isEmpty()) {
            timeLeft = 0;
        }
    }

    public synchronized int getCurrentPlayerId(){
        if (activePlayers.isEmpty()) return -1;
        return activePlayers.get(currentIndex);
    }

    public synchronized int getTimeLeft(){
        return timeLeft;
    }

    public synchronized void startTurnCycle(){
        if(!activePlayers.isEmpty()){
            timeLeft = TURN_DURATION;
        } else {
            timeLeft = 0;
        }
        lastTickTime = System.currentTimeMillis();
    }

    public synchronized void tickIfNeeded(){
        long now = System.currentTimeMillis();
        if(now - lastTickTime >= TICK_INTERVAL_MS){
            // On décrémente de 1
            timeLeft--;
            if(timeLeft <= 0){
                passToNextPlayer();
            }
            lastTickTime = now;
        }
    }

    public synchronized void endCurrentPlayerTurnImmediately(){
        passToNextPlayer();
    }

    private void passToNextPlayer(){
        if(activePlayers.isEmpty()){
            timeLeft=0;
            return;
        }
        currentIndex = (currentIndex+1) % activePlayers.size();
        timeLeft = TURN_DURATION;
        lastTickTime = System.currentTimeMillis();
    }
}

