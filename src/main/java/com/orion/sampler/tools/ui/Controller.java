package com.orion.sampler.tools.ui;

import com.orion.sampler.features.Transient;
import com.orion.sampler.features.TransientLocator;
import com.orion.sampler.features.TransientObserver;
import com.orion.sampler.io.Sandbox;
import com.orion.sampler.tools.Slicer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.SamplePlayer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Controller implements EventHandler<Event>, ChangeListener<Number> {
  private final Scene scene;
  private final Canvas canvas;
  private final Sample sample;
  private final long frameCount;
  private final int channelCount;
  private final ScrollPane scrollPane;
  private final Player player;
  private final BiquadFilter filter;
  private final BiquadFilter offlineFilter;
  private final AudioContext ac;
  private final AudioContext offlineAc;
  private final float sampleRateFactor;
  private TransientLocator locator;
  private int samplesPerPixel = 1000;
  private int zoomFactor = 2;
  private int vertScale = 1000;
  private float transientThreshold = 0.01f;
  private float transientZoomFactor = 1.5f;

  private float filterCutoff = 8 * 1000f;

  private ChangeListener<? super Number> widthListener = (observable, oldValue, newValue) -> updateView();

  private Set<KeyCode> pressedKeys = new HashSet<>();
  private final Sandbox sandbox;

  public Controller(final Sandbox sandbox, final Scene scene, final Group root, final Canvas canvas, final File sampleFile) throws IOException {
    this.sandbox = sandbox;
    this.scene = scene;
    this.canvas = canvas;
    sample = new Sample(sampleFile.getAbsolutePath());
    offlineAc = new AudioContext(new NonrealtimeIO());
    ac = new AudioContext(new JavaSoundAudioIO());
    sampleRateFactor = sample.getSampleRate() / ac.getSampleRate();

    filter = new BiquadFilter(ac, 2, BiquadFilter.Type.HP);
    offlineFilter = new BiquadFilter(offlineAc, 2, BiquadFilter.Type.HP);

    filter.setFrequency(filterCutoff);
    offlineFilter.setFrequency(filterCutoff);


    ac.out.addInput(filter);
    player = new Player(ac, sample);
    locator = new TransientLocator(offlineAc, sample, transientThreshold, offlineFilter, player);
    frameCount = sample.getNumFrames();
    channelCount = sample.getNumChannels();
    //
    scrollPane = new ScrollPane();
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setContent(canvas);
    //scrollPane.setPrefWidth(scene.getWidth());
    scrollPane.setHmax(sample.getNumFrames() / (double) samplesPerPixel);

    final Button sliceButton = new Button("Slice");
    final HBox controlRow1 = new HBox(20);
    controlRow1.getChildren().add(sliceButton);

    final VBox controlBox = new VBox();
    controlBox.getChildren().add(controlRow1);

    final VBox containerBox = new VBox(20);

    containerBox.setPrefWidth(700);
    containerBox.getChildren().add(scrollPane);
    containerBox.getChildren().add(controlBox);

    //root.getChildren().add(scrollPane);
    root.getChildren().add(containerBox);

    // set up actions
    sliceButton.setOnAction(event -> slice());

    drawCanvas();
    //updateView();
  }


  private void slice() {
    final List<Sample> slices = new Slicer(offlineAc, locator).slice(0);
    try {
      final File sandbox = this.sandbox.getNewSandbox();
      info("Writing slices to: " + sandbox);
      for (Sample slice : slices) {
        slice.write(new File(sandbox, "slice-" + System.currentTimeMillis() + ".wav").getAbsolutePath());
      }
    } catch (IOException e) {
      // TODO: add error handling
      e.printStackTrace();
    }
  }

  public void updateView() {
    info("updateView: screen width: " + scene.getWidth());
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefWidth(scene.getWidth());
  }

  public void drawCanvas() {
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    info("drawCanvas: width: " + canvas.getWidth());
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    drawWave();
    drawTransientMarkers();
  }

  private void drawTransientMarkers() {
    final List<Transient> transients = locator.getTransients();
    final double vCenter = canvas.getHeight() / 2;
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLUE);
    info("Transient locator: " + locator + ", Drawing transient markers. size: " + transients.size());
    for (Transient t : transients) {
      double x = t.getSampleIndex() / samplesPerPixel;
      double y = 0;
      double w = 1;
      gc.fillRect(x, y, w, vCenter);
    }

  }

  private void drawWave() {
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    canvas.setWidth(frameCount / samplesPerPixel);
    final double vCenter = canvas.getHeight() / 2;
    info("vCenter: " + vCenter);
    gc.setFill(Color.BLACK);
    gc.fillRect(0, vCenter, canvas.getWidth(), 1);

    float[] buffer = new float[channelCount];
    int x = 0;
    float total = 0;

    info("sample rate: " + sample.getSampleRate() + " ac sample rate: " + ac.getSampleRate() + ", sample length: " + sample.getLength() + "ms, frames: " + frameCount);

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
    if (!((event instanceof MouseEvent) || event instanceof ScrollEvent)) {
      info("Event");
    }
    if (event instanceof KeyEvent) {
      final KeyEvent keyEvent = (KeyEvent) event;
      final KeyCode code = keyEvent.getCode();

      if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
        pressedKeys.add(code);

        if (shouldZoomIn()) {
          zoomIn();
        } else if (shouldZoomOut()) {
          zoomOut();
        } else if (shouldIncreaseTransientThreshold()) {
          increaseTransientThreshold();
        } else if (shouldDecreaseTransientThreshold()) {
          decreaseTransientThreshold();
        } else if (shouldPlay()) {
          player.play();
        } else if (shouldStop()) {
          player.pause();
        } else if (shouldReset()) {
          player.reset();
        }

      } else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
        final boolean removed = pressedKeys.remove(code);
        assert removed;
      }
    }
  }

  private boolean shouldReset() {
    return pressedKeys.size() == 1 && pressedKeys.contains(KeyCode.ENTER);
  }

  private boolean shouldStop() {
    return player.playing() && pressedKeys.size() == 1 && pressedKeys.contains(KeyCode.SPACE);
  }

  private boolean shouldPlay() {
    return !player.playing() && pressedKeys.size() == 1 && pressedKeys.contains(KeyCode.SPACE);
  }

  private void decreaseTransientThreshold() {
    this.transientThreshold = transientThreshold / transientZoomFactor;
    info("decreaseTransientThreshold: " + transientThreshold);
    locator = new TransientLocator(offlineAc, sample, transientThreshold, offlineFilter, player);
    drawCanvas();
  }

  private void increaseTransientThreshold() {
    this.transientThreshold = transientThreshold * transientZoomFactor;
    info("increaseTransientThreshold: " + transientThreshold);
    locator = new TransientLocator(offlineAc, sample, transientThreshold, offlineFilter, player);
    drawCanvas();
  }

  private void zoomOut() {
    samplesPerPixel = samplesPerPixel * zoomFactor;
    info("zoomOut! samplesPerPixel: " + samplesPerPixel);
    drawCanvas();
  }

  private void zoomIn() {
    samplesPerPixel = samplesPerPixel / zoomFactor;
    info("zoomIn! samplesPerPixel: " + samplesPerPixel);
    drawCanvas();
  }

  private boolean commandOn() {
    return pressedKeys.contains(KeyCode.COMMAND);
  }

  private boolean shouldZoomIn() {
    return pressedKeys.size() == 2 && commandOn() && pressedKeys.contains(KeyCode.CLOSE_BRACKET);
  }

  private boolean shouldZoomOut() {
    return pressedKeys.size() == 2 && commandOn() && pressedKeys.contains(KeyCode.OPEN_BRACKET);
  }

  private boolean shouldIncreaseTransientThreshold() {
    return pressedKeys.size() == 1 && pressedKeys.contains(KeyCode.UP);
  }

  private boolean shouldDecreaseTransientThreshold() {
    return pressedKeys.size() == 1 && pressedKeys.contains(KeyCode.DOWN);
  }

  @Override
  public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
    info("observable: " + observable + ", oldValue: " + oldValue + ", newValue: " + newValue);
  }

  public ChangeListener<? super Number> getWidthListener() {
    return widthListener;
  }

  private class Player extends UGen implements Runnable, TransientObserver {
    private final AudioContext ac;
    private final SamplePlayer player;
    private final BlockingQueue<Object> updateQueue;
    private boolean playing;
    private Sample click;
    private int currentSample = 0;

    public Player(AudioContext ac, Sample sample) {
      super(ac);
      this.ac = ac;
      ac.out.addDependent(this);

      player = new SamplePlayer(ac, sample);
      player.setEndListener(this);
      player.reset();
      filter.addInput(player);
      try {
        click = new Sample(ClassLoader.getSystemResource("audio/click.wav").getFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
      playing = false;
      updateQueue = new ArrayBlockingQueue<>(1);
    }

    public void updateTransientLocator() {
      new TransientLocator(ac, transientThreshold, filter, this);
    }

    public void play() {
      info("play!");
      if (!ac.isRunning()) {
        ac.start();
      }
      updateTransientLocator();
      playing = true;
      player.start();
    }

    @Override
    public void notifyTransient() {
      final SamplePlayer clickPlayer = new SamplePlayer(ac, click);
      ac.out.addInput(clickPlayer);
      player.start();
    }

    public void pause() {
      info("pause!");
      ac.stop();
      playing = false;
      player.pause(true);
    }

    public void reset() {
      info("reset!");
      player.reset();
      pause();
      currentSample = 0;
      drawCanvas();
      scrollPane.setHvalue(0);
    }

    @Override
    protected void messageReceived(Bead message) {
      super.messageReceived(message);
      pause();
      reset();
    }

    public boolean playing() {
      return playing;
    }

    @Override
    public void run() {
      double x = currentSample * sampleRateFactor / samplesPerPixel;
      double y = 0;
      double w = 1;
      double h = canvas.getHeight();
      canvas.getGraphicsContext2D().fillRect(x, y, w, h);
      final double hvalue = scrollPane.getHvalue();

      final double paneWidth = scrollPane.getWidth();
      if (hvalue + paneWidth < x) {
        scrollPane.setHvalue(x);
      }

      try {
        updateQueue.put(this);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void calculateBuffer() {
      if (currentSample % (this.bufferSize * 4) == 0) {
        //info("currentSample: " + currentSample);
        Platform.runLater(this);
        try {
          updateQueue.take();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      currentSample += this.bufferSize;
    }
  }

}
