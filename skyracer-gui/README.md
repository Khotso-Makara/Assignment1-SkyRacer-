# Sky Racer - Interactive Multimedia GUI (DIIM3212)

A responsive JavaFX GUI for a simple dodging/arcade game, built for the DIIM3212 Interactive Multimedia individual assignment. The focus is on the GUI and interactive experience: navigation, layout, responsiveness, sound, animation, and controls.

## Requirements

- JDK 17+
- Maven 3.8+
- Internet access on first build so Maven can download JavaFX dependencies

## How to run

```bash
mvn clean javafx:run
```

Run the command from inside the `skyracer-gui` folder.

## Project structure

```text
skyracer-gui/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/limkokwing/skyracer/
    │   ├── App.java
    │   ├── SceneManager.java
    │   ├── GameScreen.java
    │   ├── GameState.java
    │   ├── AudioManager.java
    │   └── Obstacle.java
    └── resources/com/limkokwing/skyracer/
        ├── css/style.css
        └── sounds/
```

## Assignment requirements covered

- Five screens: Main Menu, Game Screen, Instructions, Settings, and Game Over/Completion.
- No FXML files. The interface is built in Java code.
- Maven project setup remains intact.
- Responsive stage behavior: contents scale when the stage is enlarged, and the stage has a readable minimum size when reduced.
- Interactive elements: buttons, keyboard controls, pause action, toggles, volume slider, and difficulty dropdown.
- Multimedia elements: JavaFX canvas graphics, animation, background music, sound effects, and text effects.

## Notes

The game is intentionally lightweight because the assignment focuses on the graphical interface and interactive multimedia experience rather than a complete game engine.
