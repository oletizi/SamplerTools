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
import net.beadsproject.beads.ugens.RMS;
import net.beadsproject.beads.ugens.SamplePlayer;

import java.util.ArrayList;
import java.util.List;

public class TransientLocator extends UGen implements LevelObserver {
  private final AudioContext ac;
  private final PeakDetector od;
  private final SliceList<Slice> slices = new SliceList<>();
  private final ShortFrameSegmenter sfs;
  private RMSLevelObserver rmsLevelObserver;
  private SamplePlayer player;
  private boolean running = true;
  private final float transientThreshold;
  private final TransientObserver observer;
  private final float sampleRateFactor;

  public TransientLocator(final AudioContext ac, final Sample sample, float threshold) {
    this(ac, sample, threshold, () -> {
      return;
    });
  }

  public TransientLocator(final AudioContext ac, final Sample sample, float threshold, final TransientObserver observer) {
    this(ac, sample, threshold, null, observer);
  }

  public TransientLocator(final AudioContext ac, final Sample sample, float threshold, final IIRFilter filter,
                          final TransientObserver observer) {
    this(ac, sample.getSampleRate() / ac.getSampleRate(), threshold, filter, observer);
    player = new SamplePlayer(ac, sample);
    player.setEndListener(this);
    ac.out.addDependent(player);
    if (filter != null) {
      filter.addInput(player);
      sfs.addInput(filter);
    } else {
      ac.out.addInput(player);
      sfs.addInput(player);
    }

    // TODO: Make the time range adjustable
    final RMS rms = new RMS(ac, 2, (int) ac.msToSamples(100));
    ac.out.addInput(player);
    rms.addInput(player);
    player.addDependent(rms);
    info("RMS inputs: " + rms.getIns() + ", " + rms.getConnectedInputs());

    rmsLevelObserver = new RMSLevelObserver(ac, 2, 2);
    rmsLevelObserver.addInput(rms);
    rms.addDependent(rmsLevelObserver);
    rmsLevelObserver.addObserver(this);
    info("RMS outputs: " + rms.getOuts());
    info("rmsLevelObserver ins: " + rmsLevelObserver.getIns());


    //and begin
    info("Starting non-realtime io...");
    ac.start();
    info("Starting sample player...");
    player.start();
  }

  public TransientLocator(final AudioContext ac, float threshold, final IIRFilter filter, final TransientObserver observer) {
    this(ac, 1, threshold, filter, observer);
  }

  private TransientLocator(final AudioContext ac, final float sampleRateFactor, float threshold, final IIRFilter filter,
                           final TransientObserver observer) {

    super(ac);
    this.sampleRateFactor = sampleRateFactor;
    transientThreshold = threshold;
    this.observer = observer;
    this.ac = ac;


    /*
     * To analyse a signal, build an analysis chain.
     * We also manually set parameters of the sfs.
     */
    sfs = new ShortFrameSegmenter(ac);
    sfs.setChunkSize(2048);
    sfs.setHopSize(441);

    if (filter != null) {
      sfs.addInput(filter);
    }
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

    od.setThreshold(threshold);
    // TODO: Provide access to the alpha control. Not exactly sure what the alpha does, but it seems to have
    // something to do with momentum--how fast the transient detection triggers.
    od.setAlpha(0.9999999f);

    /*
     * OnsetDetector sends messages whenever it detects an onset.
     */
    od.addMessageListener(this);

    ac.out.addDependent(sfs);

  }

  public List<Slice> getSlices() {
    return new ArrayList<>(slices);
  }

  @Override
  protected void messageReceived(Bead message) {
    super.messageReceived(message);

    final int frameIndex = getCurrentFrameIndex();
    if (message == player && running) {
      info("stopping player.");
      ac.stop();
      running = false;
      info("comitting zero levels...");
      slices.commitZeroLevels();
    }
    if (message == od) {
      newTransient(frameIndex);
    }
  }

  private void newTransient(int frameIndex) {
    info("transient at frameIndex: " + frameIndex);
    observer.notifyTransient();
    slices.add(new Slice(new Transient(this.getSample(), frameIndex)));
  }

  private int getCurrentFrameIndex() {
    return (int) (ac.msToSamples(ac.getTime()) * sampleRateFactor);
  }

  @Override
  public void calculateBuffer() {
    info("buffer!");
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + "[" + getCurrentFrameIndex() + "]: " + s);
  }

  public Sample getSample() {
    return player == null ? null : player.getSample();
  }

  @Override
  public void notifyZeroLevel() {
    final int frameIndex = getCurrentFrameIndex();
    info("notifying slices of zero level at " + frameIndex);
    slices.notifyZeroLevel(frameIndex);
  }

  @Override
  public void notifyLevel(float level) {
    info("Level: " + level);
    // TODO: This is probably not quite right
    // The idea is that if the level is above the threshold at the beginning of the source audio,
    // the PeakDetector doesn't count it as a transient (because--I think--the level change is
    // below the threshold)
    // This is here to add a transient at the beginning, if the RMS level at the beginning is greater than
    // the threshold.
    // I'm not sure, though, if the PeakDetector threshold scale matches the RMS level scale, so this algorithm
    // could be totally wrong. Too bad I can't math.
    if (slices.isEmpty() && level > transientThreshold) {
      info("creating a slice at the beginning of the audio stream...");
      newTransient(getCurrentFrameIndex());
    }
  }
}
