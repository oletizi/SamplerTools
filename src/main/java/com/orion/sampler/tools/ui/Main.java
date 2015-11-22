package com.orion.sampler.tools.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent parent = FXMLLoader.load(getClass().getResource("sample.fxml"));
    final Group root = new Group(parent);
    primaryStage.setTitle("Hello World");
    final Scene scene = new Scene(root, 700, 700);
    primaryStage.setScene(scene);

    final Canvas canvas = new Canvas(700, 300);
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    draw2DShapes(gc);

    root.getChildren().add(canvas);

    primaryStage.show();
  }

  private void draw2DShapes(GraphicsContext gc) {
    double width = gc.getCanvas().getWidth();
    double height = gc.getCanvas().getHeight();

    Random random = new Random(System.currentTimeMillis());

    gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255),
        random.nextInt(255), 0.9));
    gc.translate(width / 2, height / 2);

    for (int i = 0; i < 60; i++) {
      gc.rotate(6.0);
      gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255),
          random.nextInt(255), 0.9));
      gc.fillOval(10, 60, 30, 30);
      gc.strokeOval(60, 60, 30, 30);
      gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255),
          random.nextInt(255), 0.9));
      gc.fillRoundRect(110, 60, 30, 30, 10, 10);
      gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255),
          random.nextInt(255), 0.9));
      gc.fillPolygon(
          new double[]{105, 117, 159, 123, 133, 105, 77, 87, 51, 93},
          new double[]{150, 186, 186, 204, 246, 222, 246, 204, 186, 186}, 10);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
