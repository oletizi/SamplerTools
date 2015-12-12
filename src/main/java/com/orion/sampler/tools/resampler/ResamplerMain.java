package com.orion.sampler.tools.resampler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ResamplerMain extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    final String myPackage = getClass().getPackage().getName().replaceAll("\\.", "/");
    final String fxmlPath = myPackage + "/resampler.fxml";
    final URL fxmlUrl = ClassLoader.getSystemResource(fxmlPath);
    info("fxmlPath: " + fxmlPath + ", fxmlUrl: " + fxmlUrl);

    Parent root = FXMLLoader.load(fxmlUrl);

    Scene scene = new Scene(root);

    stage.setTitle("Resampler Tools");
    stage.setScene(scene);
    stage.show();
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
