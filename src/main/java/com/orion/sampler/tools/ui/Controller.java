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
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
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
  private final Player player;
  private TransientLocator locator;
  private int samplesPerPixel = 1000;
  private int zoomFactor = 2;
  private int vertScale = 10000;
  private float transientThreshold = 0.01f;
  private float transientZoomFactor = 1.5f;

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
    player = new Player(sample);
    locator = new TransientLocator(sample, transientThreshold);
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
    info("Drawing transient markers...");
    final List<Transient> transients = locator.getTransients();
    final double vCenter = canvas.getHeight() / 2;
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLUE);
    for (Transient t : transients) {
      double x = t.sample / samplesPerPixel;
      double y = 0;
      double w = 1;
      double h = vCenter;
      info("Marker at: " + x + ", y: " + y + ", w: " + w + ", h: " + h);
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
        } else if (shouldIncreaseTransientThreshold()) {
          increaseTransientThreshold();
        } else if (shouldDecreaseTransientThreshold()) {
          decreaseTransientThreshold();
        } else if (shouldPlay()) {
          player.play();
        } else if (shouldStop()) {
          player.pause();
        }

      } else if (keyEvent.getEventType().equals(KeyEvent.KEY_RELEASED)) {
        final boolean removed = pressedKeys.remove(code);
        assert removed;
      }

    } else {
      info("Event: " + event);
    }
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
    locator = new TransientLocator(sample, transientThreshold);
    drawCanvas();
  }

  private void increaseTransientThreshold() {
    this.transientThreshold = transientThreshold * transientZoomFactor;
    info("increaseTransientThreshold: " + transientThreshold);
    locator = new TransientLocator(sample, transientThreshold);
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

  public ChangeListener<? super Number> getHeightListener() {
    return heightListener;
  }

  private class Player extends Bead {
    private final AudioContext ac;
    private final Sample sample;
    private final SamplePlayer player;
    private final TransientLocator transientLocator;
    private boolean playing = false;
    private Sample click;

    public Player(Sample sample) {
      this(new AudioContext(new JavaSoundAudioIO()), sample);

    }

    public Player(AudioContext ac, Sample sample) {
      super();
      this.ac = ac;
      this.sample = sample;
      player = new SamplePlayer(ac, sample);
      player.setEndListener(this);
      ac.out.addInput(player);
      transientLocator = new TransientLocator(ac, transientThreshold, this);
      //ac.out.addDependent(player);
      ac.start();
      player.reset();
      try {
        click = new Sample(ClassLoader.getSystemResource("audio/click.wav").getFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void play() {
      info("play!");
      playing = true;
      player.start();
    }

    public void playClick() {
      final SamplePlayer clickPlayer = new SamplePlayer(ac, click);
      ac.out.addInput(clickPlayer);
      player.start();
    }

    public void pause() {
      info("pause!");
      playing = false;
      player.pause(true);
    }

    public void reset() {
      info("reset!");
      player.reset();
      pause();
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
    private final AudioContext ac;
    private final float threshold;
    private final PeakDetector od;
    private final BiquadFilter filter;
    private final List<Transient> transients = new ArrayList<>();
    private final Player clickPlayer;
    private SamplePlayer player;
    private UGen out;
    private boolean running = true;

    public TransientLocator(final AudioContext ac, float threshold, final Player clickPlayer) {
      super();
      this.ac = ac;
      this.threshold = threshold;
      this.clickPlayer = clickPlayer;

      out = ac.out;

      filter = new BiquadFilter(ac, 2, BiquadFilter.Type.HP);
      filter.setFrequency(4000f);
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
      od.setThreshold(threshold);
      od.setAlpha(.9f);

      /*
       * OnsetDetector sends messages whenever it detects an onset.
       */
      od.addMessageListener(this);

      ac.out.addDependent(sfs);

    }

    public TransientLocator(final Sample sample, float threshold) {
      //super();
      this(new AudioContext(new NonrealtimeIO()), threshold, null);
      player = new SamplePlayer(ac, sample);
      player.setEndListener(this);
      out.addDependent(player);

      filter.addInput(player);
      out.addInput(filter);
      ac.out.addInput(out);
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
        info("transient at: " + ac.getTime() + "ms, sample: " + ac.msToSamples(ac.getTime()));
        if (clickPlayer != null) {
          clickPlayer.playClick();
        }
        transients.add(new Transient(ac.getTime(), ac.msToSamples(ac.getTime())));
      }
    }
  }
}
