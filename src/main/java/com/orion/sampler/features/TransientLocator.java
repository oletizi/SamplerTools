package com.orion.sampler.features;

import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.IIRFilter;
import net.beadsproject.beads.ugens.SamplePlayer;

import java.util.ArrayList;
import java.util.List;

public class TransientLocator extends UGen {
  private final AudioContext ac;
  private final PeakDetector od;
  private final List<Transient> transients = new ArrayList<>();
  private SamplePlayer player;
  private boolean running = true;
  private final TransientObserver observer;
  private final float sampleRateFactor;

  public TransientLocator(final AudioContext ac, final Sample sample, float threshold, final IIRFilter filter, final TransientObserver observer) {
    this(ac, sample.getSampleRate() / ac.getSampleRate(), threshold, filter, observer);
    player = new SamplePlayer(ac, sample);
    player.setEndListener(this);
    ac.out.addDependent(player);

    filter.addInput(player);
    //and begin
    info("Starting non-realtime io...");
    ac.start();
    info("Starting sample player...");
    player.start();
  }

  public TransientLocator(final AudioContext ac, float threshold, final IIRFilter filter, final TransientObserver observer) {
    this(ac, 1, threshold, filter, observer);
  }

  private TransientLocator(final AudioContext ac, final float sampleRateFactor, float threshold, final IIRFilter filter, final TransientObserver observer) {

    super(ac);
    this.sampleRateFactor = sampleRateFactor;
    this.observer = observer;
    this.ac = ac;
    /*
     * To analyse a signal, build an analysis chain.
     * We also manually set parameters of the sfs.
     */
    ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
    sfs.setChunkSize(2048);
    sfs.setHopSize(441);

    //out.addInput(filter);
    sfs.addInput(filter);
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

  public List<Transient> getTransients() {
    return new ArrayList<>(transients);
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
      info(ac.getAudioIO() + " transient at: " + ac.getTime() + "ms, sample: " + ac.msToSamples(ac.getTime()));
      observer.notifyTransient();

      transients.add(new Transient(ac.getTime(), (int) (ac.msToSamples(ac.getTime()) * sampleRateFactor)));
    }
  }

  @Override
  public void calculateBuffer() {
    info("buffer!");
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  public Sample getSample() {
    return player == null ? null : player.getSample();
  }
}
