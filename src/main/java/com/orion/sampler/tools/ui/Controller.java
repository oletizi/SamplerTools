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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.SamplePlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Controller implements EventHandler<Event>, ChangeListener<Number> {
  private final Scene scene;
  private final Group root;
  private final Canvas canvas;
  private final File sampleFile;
  private final Sample sample;
  private final long frameCount;
  private final int channelCount;
  private final ScrollPane scrollPane;
  private final TransientLocator locator;
  private int samplesPerPixel = 1000;
  private int zoomFactor = 10;
  private int vertScale = 10000;
  private ChangeListener<? super Number> widthListener = new ChangeListener<Number>() {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      updateView();
    }
  };
  private ChangeListener<? super Number> heightListener = widthListener;

  private Map<Integer, Canvas> canvases = new HashMap<>();
  private Set<KeyCode> pressedKeys = new HashSet<>();

  public Controller(Scene scene, Group root, Canvas canvas, File sampleFile) throws IOException {
    this.scene = scene;
    this.root = root;
    this.canvas = canvas;
    this.sampleFile = sampleFile;
    sample = new Sample(sampleFile.getAbsolutePath());
    locator = new TransientLocator(sample);
    frameCount = sample.getNumFrames();
    channelCount = sample.getNumChannels();
    canvas.setWidth(frameCount / channelCount / samplesPerPixel);
    scrollPane = new ScrollPane();
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setContent(canvas);
    scrollPane.setHmax(scene.getWidth());
    root.getChildren().add(scrollPane);
    drawCanvas();

    canvases.put(samplesPerPixel, canvas);
  }


  public void updateView() {
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefWidth(scene.getWidth());
  }

  public void drawCanvas() {
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    drawWave();
    drawTransientMarkers();
  }

  private void drawTransientMarkers() {

    final List<Transient> transients = locator.getTransients();
    final double vCenter = canvas.getHeight() / 2;
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLUE);
    for (Transient t : transients) {
      double x = t.sample / samplesPerPixel;
      double y = 0;
      double w = 1;
      double h = vCenter;
      gc.fillRect(x, y, w, h);
    }

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
    } else if (event instanceof KeyEvent) {
      final KeyEvent keyEvent = (KeyEvent) event;
      final KeyCode code = keyEvent.getCode();

      if (keyEvent.getEventType().equals(KeyEvent.KEY_PRESSED)) {
        pressedKeys.add(code);

        if (shouldZoomIn()) {
          zoomIn();
        } else if (shouldZoomOut()) {
          zoomOut();
        }

      } else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
        final boolean removed = pressedKeys.remove(code);
        assert removed;
      }

    } else {
      info("Event: " + event);
    }
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

  private class Transient {
    double time;
    double sample;

    public Transient(double time, double sample) {
      this.time = time;
      this.sample = sample;
    }
  }

  private class TransientLocator extends Bead {
    private final AudioIO io = new NonrealtimeIO();
    private final AudioContext ac = new AudioContext(io);
    private final PeakDetector od;
    private final SamplePlayer player;
    private UGen out = ac.out;
    private boolean running = true;
    private final List<Transient> transients = new ArrayList<>();

    public TransientLocator(final Sample sample) {
      super();
      player = new SamplePlayer(ac, sample);
      player.setEndListener(this);

      BiquadFilter filter = new BiquadFilter(ac, 2, BiquadFilter.Type.HP);
      filter.setFrequency(4000f);
      filter.addInput(player);
      out.addInput(filter);
      ac.out.addInput(out);

      /*
       * To analyse a signal, build an analysis chain.
       * We also manually set parameters of the sfs.
       */
      ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
      sfs.setChunkSize(2048);
      sfs.setHopSize(441);
      sfs.addInput(ac.out);
      FFT fft = new FFT();
      PowerSpectrum ps = new PowerSpectrum();
      sfs.addListener(fft);
      fft.addListener(ps);

      /*
       * Given the power spectrum we can now detect changes in spectral energy.
       */
      SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
      ps.addListener(sd);
      od = new PeakDetector();
      sd.addListener(od);

      /*
       * These parameters will need to be adjusted based on the
       * type of music. This demo uses the mouse position to adjust
       * them dynamically.
       * mouse.x controls Threshold, mouse.y controls Alpha
       */
      od.setThreshold(0.009f);
      od.setAlpha(.9f);

      /*
       * OnsetDetector sends messages whenever it detects an onset.
       */
      od.addMessageListener(this);

      ac.out.addDependent(sfs);

      //and begin
      ac.start();
    }

    public List<Transient> getTransients() {
      return new ArrayList(transients);
    }

    @Override
    protected void messageReceived(Bead message) {
      super.messageReceived(message);
      if (message == player && running) {
        info("stopping player.");
        ac.stop();
        running = false;
      }
      if (message == od) {
        //info("transient at: " + ac.getTime() + "ms, sample: " + ac.msToSamples(ac.getTime()));
        transients.add(new Transient(ac.getTime(), ac.msToSamples(ac.getTime())));
      }
    }
  }
}
