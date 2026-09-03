package com.limkokwing.skyracer;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;


public final class AudioManager {

    private static final AudioManager INSTANCE = new AudioManager();

    private MediaPlayer musicPlayer;
    private AudioClip clickClip;
    private AudioClip hitClip;
    private AudioClip gameOverClip;
    private AudioClip winClip;

    private AudioManager() {
        clickClip = loadClip("click.wav");
        hitClip = loadClip("hit.wav");
        gameOverClip = loadClip("gameover.wav");
        winClip = loadClip("win.wav");

        URL musicUrl = getResource("background.wav");
        if (musicUrl != null) {
            try {
                Media media = new Media(musicUrl.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.setVolume(GameState.getInstance().getVolume());
            } catch (Exception e) {
                musicPlayer = null;
            }
        }
    }

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    private URL getResource(String name) {
        return getClass().getResource("/com/limkokwing/skyracer/sounds/" + name);
    }

    private AudioClip loadClip(String name) {
        URL url = getResource(name);
        if (url == null) {
            return null;
        }
        try {
            return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            return null;
        }
    }

    public void startMusic() {
        if (musicPlayer != null && GameState.getInstance().isMusicOn()) {
            musicPlayer.play();
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    public void onMusicToggle(boolean on) {
        if (musicPlayer == null) return;
        if (on) {
            musicPlayer.play();
        } else {
            musicPlayer.pause();
        }
    }

    public void onVolumeChange(double volume) {
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume);
        }
    }

    private void play(AudioClip clip) {
        if (clip != null && GameState.getInstance().isSoundOn()) {
            clip.play(GameState.getInstance().getVolume());
        }
    }

    public void playClick() {
        play(clickClip);
    }

    public void playHit() {
        play(hitClip);
    }

    public void playGameOver() {
        play(gameOverClip);
    }

    public void playWin() {
        play(winClip);
    }
}
