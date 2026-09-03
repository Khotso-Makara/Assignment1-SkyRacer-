package com.limkokwing.skyracer;

/**
 * Simple Settings screen, Game screen and
 * Game Over screen can all agree on sound/music/difficulty
 */
public final class GameState {

    public enum Difficulty { EASY, NORMAL, HARD }

    private static final GameState INSTANCE = new GameState();

    private boolean soundOn = true;
    private boolean musicOn = true;
    private double volume = 0.6;          // 0.0 - 1.0
    private Difficulty difficulty = Difficulty.NORMAL;

    private int lastScore = 0;
    private int highScore = 0;
    private boolean lastRunWasWin = false;

    private GameState() {
    }

    public static GameState getInstance() {
        return INSTANCE;
    }

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    public boolean isMusicOn() {
        return musicOn;
    }

    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;
        AudioManager.getInstance().onMusicToggle(musicOn);
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
        AudioManager.getInstance().onVolumeChange(volume);
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getLastScore() {
        return lastScore;
    }

    public boolean isLastRunWasWin() {
        return lastRunWasWin;
    }

    public int getHighScore() {
        return highScore;
    }

    public void reportRunResult(int score, boolean win) {
        this.lastScore = score;
        this.lastRunWasWin = win;
        if (score > highScore) {
            highScore = score;
        }
    }
}
