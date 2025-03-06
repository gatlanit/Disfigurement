package com.niting.main;

public class GameStats {
    public int numGenerators = 0;
    public boolean isAlive = false;

    public GameStats() {
        reset();
    }

    public void reset() {
        numGenerators = 0;
        isAlive = false;
    }
}
