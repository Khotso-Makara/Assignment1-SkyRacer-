package com.limkokwing.skyracer;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameScreen extends BorderPane {

    private static final double PLAYER_W = 46;
    private static final double PLAYER_H = 28;
    private static final int LEVEL_UP_SCORE = 100;
    private static final int WIN_LEVEL = 5;

    private final Random random = new Random();
    private final List<Obstacle> obstacles = new ArrayList<>();

    private final Canvas gameCanvas = new Canvas(900, 500);
    private final Label scoreLabel = new Label("Score: 0");
    private final Label livesLabel = new Label("Lives: \u2764\u2764\u2764");
    private final Label levelLabel = new Label("Level: 1");
    private final Label pauseOverlayLabel = new Label("PAUSED");
    private final Button pauseButton = new Button("Pause");

    private double playerX;
    private double playerY;
    private boolean movingLeft;
    private boolean movingRight;

    private double score;
    private int lives = 3;
    private int level = 1;
    private double invulnerableSeconds;
    private double spawnCooldown;

    private double baseSpawnInterval = 1.1;
    private double baseObstacleSpeed = 140;

    private boolean paused;
    private boolean gameEnded;
    private long lastNanoTime = -1;
    private AnimationTimer timer;

    public GameScreen() {
        getStyleClass().add("screen-root");
        setPadding(new Insets(14, 20, 20, 20));

        HBox hud = new HBox(26, scoreLabel, livesLabel, levelLabel);
        hud.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(scoreLabel, javafx.scene.layout.Priority.ALWAYS);
        pauseButton.getStyleClass().add("game-button");
        pauseButton.setOnAction(event -> togglePause());
        hud.getChildren().add(pauseButton);
        hud.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                label.getStyleClass().add("hud-text");
            }
        });

        pauseOverlayLabel.getStyleClass().add("title-text");
        pauseOverlayLabel.setStyle("-fx-font-size: 40px;");
        pauseOverlayLabel.setVisible(false);

        StackPane playArea = new StackPane(gameCanvas, pauseOverlayLabel);
        playArea.setAlignment(Pos.CENTER);
        playArea.setPadding(new Insets(10, 0, 0, 0));

        setTop(hud);
        setCenter(playArea);

        playerX = gameCanvas.getWidth() / 2 - PLAYER_W / 2;
        playerY = gameCanvas.getHeight() - PLAYER_H - 12;

        sceneProperty().addListener((obs, oldScene, newScene) -> attachKeyboardListeners(newScene));
        setFocusTraversable(true);
        setOnMouseClicked(event -> requestFocus());

        applyDifficulty();
        updateHud();
        startTimer();
    }

    private void attachKeyboardListeners(Scene scene) {
        if (scene == null) return;
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
        requestFocus();
    }

    private void applyDifficulty() {
        switch (GameState.getInstance().getDifficulty()) {
            case EASY -> {
                baseSpawnInterval = 1.4;
                baseObstacleSpeed = 110;
            }
            case HARD -> {
                baseSpawnInterval = 0.75;
                baseObstacleSpeed = 190;
            }
            default -> {
                baseSpawnInterval = 1.1;
                baseObstacleSpeed = 140;
            }
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT, A -> movingLeft = true;
            case RIGHT, D -> movingRight = true;
            case SPACE -> togglePause();
            default -> {
            }
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT, A -> movingLeft = false;
            case RIGHT, D -> movingRight = false;
            default -> {
            }
        }
    }

    private void togglePause() {
        if (gameEnded) return;
        paused = !paused;
        pauseOverlayLabel.setVisible(paused);
        pauseButton.setText(paused ? "Resume" : "Pause");
        AudioManager.getInstance().playClick();
    }

    private void startTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNanoTime < 0) {
                    lastNanoTime = now;
                    return;
                }
                double dt = (now - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = now;
                if (!paused && !gameEnded) {
                    update(dt);
                }
                render();
            }
        };
        timer.start();
    }

    private void update(double dt) {
        double speed = 320;
        if (movingLeft) playerX -= speed * dt;
        if (movingRight) playerX += speed * dt;
        playerX = Math.max(4, Math.min(gameCanvas.getWidth() - PLAYER_W - 4, playerX));

        if (invulnerableSeconds > 0) {
            invulnerableSeconds -= dt;
        }

        score += dt * 20;
        int newLevel = Math.min(WIN_LEVEL, 1 + (int) (score / LEVEL_UP_SCORE));
        if (newLevel != level) {
            level = newLevel;
            if (level >= WIN_LEVEL) {
                endGame(true);
                return;
            }
        }

        spawnCooldown -= dt;
        double interval = Math.max(0.35, baseSpawnInterval - (level - 1) * 0.12);
        if (spawnCooldown <= 0) {
            spawnObstacle();
            spawnCooldown = interval;
        }

        double fallSpeed = baseObstacleSpeed + (level - 1) * 25;
        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle obstacle = it.next();
            obstacle.y += fallSpeed * dt;
            if (invulnerableSeconds <= 0 && obstacle.intersects(playerX, playerY, PLAYER_W, PLAYER_H)) {
                it.remove();
                onHit();
                continue;
            }
            if (obstacle.y > gameCanvas.getHeight()) {
                it.remove();
            }
        }

        updateHud();
    }

    private void spawnObstacle() {
        double width = 30 + random.nextDouble() * 28;
        double x = random.nextDouble() * (gameCanvas.getWidth() - width);
        obstacles.add(new Obstacle(x, -30, width, 26, baseObstacleSpeed));
    }

    private void onHit() {
        lives--;
        invulnerableSeconds = 1.2;
        AudioManager.getInstance().playHit();
        if (lives <= 0) {
            endGame(false);
        }
    }

    private void endGame(boolean win) {
        gameEnded = true;
        timer.stop();
        GameState.getInstance().reportRunResult((int) score, win);
        if (win) {
            AudioManager.getInstance().playWin();
        } else {
            AudioManager.getInstance().playGameOver();
        }
        SceneManager.getInstance().showGameOver();
    }

    private void updateHud() {
        scoreLabel.setText("Score: " + (int) score);
        levelLabel.setText("Level: " + level);
        StringBuilder hearts = new StringBuilder("Lives: ");
        for (int i = 0; i < lives; i++) hearts.append('\u2764');
        for (int i = lives; i < 3; i++) hearts.append('\u2661');
        livesLabel.setText(hearts.toString());
    }

    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        double w = gameCanvas.getWidth();
        double h = gameCanvas.getHeight();

        gc.setFill(Color.web("#101b33"));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#ffffff", 0.18));
        for (int i = 0; i < 40; i++) {
            double sx = (i * 53) % w;
            double sy = (i * 97 + (System.currentTimeMillis() / 20) % 600) % h;
            gc.fillOval(sx, sy, 2, 2);
        }

        gc.setFill(Color.web("#ef476f"));
        for (Obstacle obstacle : obstacles) {
            gc.fillRoundRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height, 8, 8);
        }

        boolean visible = invulnerableSeconds <= 0 || ((int) (invulnerableSeconds * 10) % 2 == 0);
        if (visible) {
            gc.setFill(Color.web("#3a86ff"));
            gc.fillRoundRect(playerX, playerY, PLAYER_W, PLAYER_H, 10, 10);
            gc.setFill(Color.web("#ffd166"));
            gc.fillOval(playerX + PLAYER_W / 2 - 4, playerY - 6, 8, 8);
        }
    }
}
