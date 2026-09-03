package com.limkokwing.skyracer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.net.URL;


public final class SceneManager {

    public static final double DESIGN_WIDTH = 960;
    public static final double DESIGN_HEIGHT = 600;
    public static final double MIN_SCALE = 0.65;

    private static final SceneManager INSTANCE = new SceneManager();

    private Stage stage;
    private Scene scene;
    private StackPane sceneRoot;
    private Group scaleGroup;
    private Scale scale;
    private Label sizeMessage;

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.stage = stage;

        sceneRoot = new StackPane();
        sceneRoot.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        sceneRoot.setMinSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        sceneRoot.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);

        scale = new Scale(1, 1, 0, 0);
        scaleGroup = new Group(sceneRoot);
        scaleGroup.getTransforms().add(scale);

        sizeMessage = new Label("Minimum readable size reached");
        sizeMessage.getStyleClass().add("size-message");
        sizeMessage.setVisible(false);

        StackPane outer = new StackPane(scaleGroup, sizeMessage);
        StackPane.setAlignment(sizeMessage, Pos.BOTTOM_CENTER);
        StackPane.setMargin(sizeMessage, new Insets(0, 0, 12, 0));
        outer.setStyle("-fx-background-color: #0b1220;");

        scene = new Scene(outer, 960, 600);
        scene.getStylesheets().add(getResource("css/style.css").toExternalForm());

        scene.widthProperty().addListener((obs, oldV, newV) -> updateScale());
        scene.heightProperty().addListener((obs, oldV, newV) -> updateScale());
        stage.widthProperty().addListener((obs, oldV, newV) -> updateScale());
        stage.heightProperty().addListener((obs, oldV, newV) -> updateScale());

        stage.setScene(scene);
    }

    private void updateScale() {
        double sx = scene.getWidth() / DESIGN_WIDTH;
        double sy = scene.getHeight() / DESIGN_HEIGHT;
        double rawScale = Math.min(sx, sy);
        double s = Math.max(MIN_SCALE, rawScale);
        scale.setX(s);
        scale.setY(s);
        sizeMessage.setVisible(rawScale <= MIN_SCALE + 0.01);
    }

    private void setRoot(Parent newRoot) {
        sceneRoot.getChildren().setAll(newRoot);
        updateScale();
    }

    private URL getResource(String path) {
        return getClass().getResource("/com/limkokwing/skyracer/" + path);
    }

    public void showMainMenu() {
        AudioManager.getInstance().startMusic();
        VBox menu = centeredScreen(34);

        Label title = title("SKY RACER", 56);
        Label subtitle = subtitle("Dodge. Survive. Beat your high score.");

        VBox buttons = new VBox(14,
                navButton("Play Game", this::showGameScreen),
                navButton("Instructions", this::showInstructions),
                navButton("Settings", this::showSettings),
                exitButton()
        );
        buttons.setMaxWidth(300);
        buttons.setAlignment(Pos.CENTER);

        Label course = subtitle("DMSE - Interactive Multimedia - Individual Assignment");
        menu.getChildren().addAll(title, subtitle, buttons, course);
        setRoot(menu);
    }

    public void showGameScreen() {
        setRoot(new GameScreen());
    }

    public void showInstructions() {
        VBox screen = baseScreen(18);
        screen.setPadding(new Insets(38, 70, 34, 70));
        screen.getChildren().add(title("How to Play", 38));

        VBox content = new VBox(18,
                card("Description", "Sky Racer is a fast, one-screen dodging game. You pilot a small craft across the bottom of the screen while obstacles fall from above."),
                card("Objective", "Survive as long as possible, build your score, and reach Level 5 before you run out of lives."),
                card("Controls", "Move left with Left Arrow or A.\nMove right with Right Arrow or D.\nPause or resume with the Pause button or Spacebar.\nUse mouse/touch on all menu buttons."),
                card("Rules", "You start with 3 lives.\nHitting a falling obstacle costs 1 life.\nYour score rises while you survive.\nEvery 100 points raises the level and speed.\nThe run ends at 0 lives or when Level 5 is completed.")
        );
        content.setMaxWidth(680);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("clean-scroll");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        Button back = navButton("Back to Menu", this::showMainMenu);
        screen.getChildren().addAll(scrollPane, back);
        setRoot(screen);
    }

    public void showSettings() {
        GameState state = GameState.getInstance();
        VBox screen = centeredScreen(22);
        screen.setPadding(new Insets(46, 90, 46, 90));
        screen.getChildren().add(title("Settings", 38));

        ToggleButton soundToggle = toggle(state.isSoundOn());
        soundToggle.setOnAction(event -> {
            state.setSoundOn(soundToggle.isSelected());
            soundToggle.setText(soundToggle.isSelected() ? "ON" : "OFF");
            AudioManager.getInstance().playClick();
        });

        ToggleButton musicToggle = toggle(state.isMusicOn());
        musicToggle.setOnAction(event -> {
            state.setMusicOn(musicToggle.isSelected());
            musicToggle.setText(musicToggle.isSelected() ? "ON" : "OFF");
        });

        Slider volumeSlider = new Slider(0, 1, state.getVolume());
        volumeSlider.getStyleClass().add("game-slider");
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> state.setVolume(newValue.doubleValue()));

        ComboBox<GameState.Difficulty> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().setAll(GameState.Difficulty.values());
        difficultyCombo.setValue(state.getDifficulty());
        difficultyCombo.getStyleClass().add("game-combo");
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);
        difficultyCombo.setOnAction(event -> {
            state.setDifficulty(difficultyCombo.getValue());
            AudioManager.getInstance().playClick();
        });

        VBox panel = new VBox(18,
                settingRow("Sound Effects", soundToggle),
                settingRow("Background Music", musicToggle),
                labeledControl("Volume", volumeSlider),
                labeledControl("Difficulty", difficultyCombo)
        );
        panel.getStyleClass().add("card-panel");
        panel.setMaxWidth(560);

        Button back = navButton("Back to Menu", this::showMainMenu);
        screen.getChildren().addAll(panel, back);
        setRoot(screen);
    }

    public void showGameOver() {
        GameState state = GameState.getInstance();
        VBox screen = centeredScreen(22);
        boolean win = state.isLastRunWasWin();

        screen.getChildren().addAll(
                title(win ? "LEVEL 5 COMPLETE!" : "GAME OVER", 48),
                hud("Final Score: " + state.getLastScore(), 24),
                body("High Score: " + state.getHighScore() + (win ? " - Congratulations, you beat Sky Racer!" : "")),
                new HBox(14, navButton("Play Again", this::showGameScreen), navButton("Main Menu", this::showMainMenu))
        );
        ((HBox) screen.getChildren().get(3)).setAlignment(Pos.CENTER);
        setRoot(screen);
    }

    public void exitGame() {
        if (stage != null) {
            stage.close();
        }
    }

    public Stage getStage() {
        return stage;
    }

    private VBox baseScreen(double spacing) {
        VBox box = new VBox(spacing);
        box.getStyleClass().add("screen-root");
        box.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    private VBox centeredScreen(double spacing) {
        VBox box = baseScreen(spacing);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(38));
        return box;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("game-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            AudioManager.getInstance().playClick();
            action.run();
        });
        return button;
    }

    private Button exitButton() {
        Button button = new Button("Exit");
        button.getStyleClass().addAll("game-button", "exit-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            AudioManager.getInstance().playClick();
            exitGame();
        });
        return button;
    }

    private ToggleButton toggle(boolean selected) {
        ToggleButton button = new ToggleButton(selected ? "ON" : "OFF");
        button.getStyleClass().add("icon-button");
        button.setSelected(selected);
        return button;
    }

    private HBox settingRow(String labelText, Parent control) {
        HBox row = new HBox(18, body(labelText), control);
        row.getStyleClass().add("toggle-row");
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
        return row;
    }

    private VBox labeledControl(String labelText, Parent control) {
        VBox box = new VBox(8, body(labelText), control);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private VBox card(String heading, String content) {
        Label headingLabel = heading(heading);
        Label bodyLabel = body(content);
        bodyLabel.setWrapText(true);
        VBox card = new VBox(8, headingLabel, bodyLabel);
        card.getStyleClass().add("card-panel");
        return card;
    }

    private Label title(String text, double size) {
        Label label = new Label(text);
        label.getStyleClass().add("title-text");
        label.setStyle("-fx-font-size: " + size + "px;");
        return label;
    }

    private Label subtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("subtitle-text");
        return label;
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-heading");
        return label;
    }

    private Label body(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("body-text");
        return label;
    }

    private Label hud(String text, double size) {
        Label label = new Label(text);
        label.getStyleClass().add("hud-text");
        label.setStyle("-fx-font-size: " + size + "px;");
        return label;
    }
}
