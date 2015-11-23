package com.orion.sampler.tools.ui;

import javafx.application.Application;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    //Parent parent = FXMLLoader.load(getClass().getResource("com/orion/sampler/tools/ui/sample.fxml"));
    final Group root = new Group();
    primaryStage.setTitle("Hello World");
    final Scene scene = new Scene(root, 700, 700);

    final Canvas canvas = new Canvas(700, 300);
    //root.getChildren().add(canvas);

    primaryStage.setScene(scene);
    final URL systemResource = ClassLoader.getSystemResource("audio/gt.wav");
    final Controller controller = new Controller(scene, root, canvas, new File(systemResource.getFile()));

    scene.addEventFilter(EventType.ROOT, controller);
    scene.widthProperty().addListener(controller.getWidthListener());

    primaryStage.show();
    controller.updateView();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
