package com.orion.sampler.tools;

import com.orion.sampler.features.Slice;
import com.orion.sampler.features.Transient;
import com.orion.sampler.features.TransientLocator;
import com.orion.sampler.tools.ui.progress.ProgressObserver;
import com.orion.sampler.tools.ui.progress.ProgressObserverAdapter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;

import java.util.ArrayList;
import java.util.List;

public class Slicer {
  private final AudioContext ac;
  private final TransientLocator locator;
  private final ProgressObserver progressObserver;
  private final Sample sourceSample;

  public Slicer(final AudioContext ac, final TransientLocator locator) {
    this(ac, locator, new ProgressObserverAdapter());
  }

  public Slicer(AudioContext ac, TransientLocator locator, final ProgressObserver progressObserver) {
    this.ac = ac;
    this.locator = locator;
    this.progressObserver = progressObserver;
    this.sourceSample = locator.getSample();
  }

  public List<Sample> slice(int prerollMs) {
    final int preRollSamples = (int) sourceSample.msToSamples(prerollMs);
    info("Preroll of " + prerollMs + " becomes " + preRollSamples + " samples.");
    final List<Slice> slices = locator.getSlices();
    final List<Sample> rv = new ArrayList<>(slices.size());
    if (slices.isEmpty()) return rv;
    float progress = 0;
    final float progressStep = 1 / slices.size();
    for (int i = 0; i < slices.size(); i++) {
      final Slice slice = slices.get(0);
      final Transient start = slice.getStartTransient();
      progressObserver.notifyProgress(progress, "Creating slice at sample: " + start.getFrameIndex());
      final int startIndex = calculateSampleIndex(preRollSamples, start.getFrameIndex());
      int endIndex = slice.getEndFrame();
      if (endIndex == 0) {
        // the level didn't go to zero before the next transient or the end of the source file
        if (i + 1 == slices.size()) {
          // this is the last slice. The end index is the end of the source sammple
          endIndex = (int) sourceSample.getNumFrames();
        } else {
          // the end will be the onset of the next slice
          endIndex = calculateSampleIndex(preRollSamples, slices.get(i + 1).getStartTransient().getFrameIndex() - 1);
        }
      }
      info("Creating slice: startIndex: " + startIndex + ", endIndex: " + endIndex);
      rv.add(createSlice(startIndex, endIndex));
      progressObserver.notifyProgress(progress += progressStep, "Done creating slice.");
    }

    return rv;
  }

  private int calculateSampleIndex(int preRollSamples, int transientStart) {
    return Math.max(0, transientStart - preRollSamples);
  }

  private Sample createSlice(int startFrame, int endFrame) {
    final int bufferSize = endFrame - startFrame;
    final int channelCount = sourceSample.getNumChannels();
    float[][] buffer = new float[channelCount][bufferSize];

    // write the frames of the current slice into the buffer
    info("startFrame: " + startFrame + ", endFrame: " + endFrame + ", bufferSize: " + bufferSize);
    sourceSample.getFrames(startFrame, buffer);

    //info("current frame: " + currentFrame + ", buffer size: " + bufferSize + ", buffer: " + Arrays.deepToString(buffer));

    // create a new Sample for the current slice
    final double length = sourceSample.samplesToMs(bufferSize);
    final Sample slice = new Sample(length, channelCount, sourceSample.getSampleRate());

    // write the buffer into the current slice
    slice.putFrames(0, buffer);
    return slice;
  }

  public void info(String m) {
    System.out.println(getClass().getSimpleName() + ": " + m);
  }
}
