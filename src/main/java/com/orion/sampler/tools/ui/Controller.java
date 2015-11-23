package com.orion.sampler.tools.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import net.beadsproject.beads.data.Sample;

import java.io.File;
import java.io.IOException;


public class Controller implements EventHandler<Event>, ChangeListener<Number> {
  private final Scene scene;
  private final Group root;
  private final Canvas canvas;
  private final File sampleFile;
  private final Sample sample;
  private final long frameCount;
  private final int channelCount;
  private final ScrollPane scrollPane;
  private int samplesPerPixel = 1000;
  private int vertScale = 10000;
  private ChangeListener<? super Number> widthListener = new ChangeListener<Number>() {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      updateView();
    }
  };
  private ChangeListener<? super Number> heightListener = widthListener;

  public Controller(Scene scene, Group root, Canvas canvas, File sampleFile) throws IOException {
    this.scene = scene;
    this.root = root;
    this.canvas = canvas;
    this.sampleFile = sampleFile;
    sample = new Sample(sampleFile.getAbsolutePath());
    frameCount = sample.getNumFrames();
    channelCount = sample.getNumChannels();
    canvas.setWidth(frameCount / channelCount / samplesPerPixel);
    scrollPane = new ScrollPane();
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setContent(canvas);
    scrollPane.setHmax(scene.getWidth());
    root.getChildren().add(scrollPane);
    drawWave();
  }


  public void updateView() {
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefWidth(scene.getWidth());
  }

  private void drawWave() {
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    final double vCenter = canvas.getHeight() / 2;
    info("vCenter: " + vCenter);
    gc.setFill(Color.BLACK);
    gc.fillRect(0, vCenter, canvas.getWidth(), 1);

    float[] buffer = new float[channelCount];
    int x = 0;
    float total = 0;
    for (int i = 0; i < frameCount; i++) {
      sample.getFrame(i, buffer);
      double value = Math.abs(buffer[0]);
      total += value;

      if (i % samplesPerPixel == 0) {
        double avg = total / samplesPerPixel;
        ++x;
        double h = avg * vertScale;
        double y = vCenter - h;
        double w = 1;
        gc.fillRect(x, y, w, h);
        total = 0;
      }
    }
  }

  private void info(String s) {
    System.out.println(s);
  }


  @Override
  public void handle(Event event) {
    if ((event instanceof MouseEvent) || event instanceof ScrollEvent) {
    } else {
      info("Event: " + event);
    }
  }

  @Override
  public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
    info("observable: " + observable + ", oldValue: " + oldValue + ", newValue: " + newValue);
  }

  public ChangeListener<? super Number> getWidthListener() {
    return widthListener;
  }

  public ChangeListener<? super Number> getHeightListener() {
    return heightListener;
  }
}
