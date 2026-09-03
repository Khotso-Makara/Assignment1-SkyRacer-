package com.limkokwing.skyracer;

import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sky Racer - DMSE Interactive Multimedia GUI");

        // LAYOUT resizing to various screens
        primaryStage.setMinWidth(SceneManager.DESIGN_WIDTH * SceneManager.MIN_SCALE + 16);
        primaryStage.setMinHeight(SceneManager.DESIGN_HEIGHT * SceneManager.MIN_SCALE + 39);

        SceneManager.getInstance().init(primaryStage);
        SceneManager.getInstance().showMainMenu();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
