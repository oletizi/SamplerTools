package com.orion.sampler.tools.resampler.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

  @FXML
  private Button selectProgramButton;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    selectProgramButton.setOnAction(event -> {
      info("You selected me!");
    });
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}
