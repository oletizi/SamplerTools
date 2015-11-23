package com.orion.sampler.tools;

import com.orion.sampler.features.Transient;
import com.orion.sampler.features.TransientLocator;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;

import java.util.ArrayList;
import java.util.List;

public class Slicer {
  private final AudioContext ac;
  private final TransientLocator locator;
  private final Sample sourceSample;

  public Slicer(AudioContext ac, TransientLocator locator) {
    this.ac = ac;
    this.locator = locator;
    this.sourceSample = locator.getSample();
  }

  public List<Sample> slice(int prerollMs) {
//    float[][] buff = new float[(int) sourceSample.getNumFrames()][sourceSample.getNumChannels()];
//    info("sourceSample frameCount: " + sourceSample.getNumFrames() + ", data: " + Arrays.deepToString(buff));
    final int preRollSamples = (int) sourceSample.msToSamples(prerollMs);
    info("Preroll of " + prerollMs + " becomes " + preRollSamples + " samples.");
    final List<Sample> rv = new ArrayList<>(locator.getTransients().size());
    int currentFrame = 0;
    for (Transient slicepoint : locator.getTransients()) {

      final int sampleIndex = calculateSampleIndex(preRollSamples, slicepoint);

      final int bufferSize = sampleIndex - currentFrame;
      if (currentFrame > 0 && bufferSize > 0) {
        final Sample slice = createSlice(currentFrame, bufferSize);
        rv.add(slice);
      }
      // update the current frame
      currentFrame = sampleIndex;
    }

    // grab the tail of the sample
    final int bufferSize = (int) sourceSample.getNumFrames() - currentFrame;
    rv.add(createSlice(currentFrame, bufferSize));

    return rv;
  }

  private int calculateSampleIndex(int preRollSamples, Transient slicepoint) {
    return Math.max(0, slicepoint.getSampleIndex() - preRollSamples);
  }

  private Sample createSlice(int currentFrame, int bufferSize) {
    final int channelCount = sourceSample.getNumChannels();
    float[][] buffer = new float[channelCount][bufferSize];

    // write the frames of the current slice into the buffer
    info("currentFrame: " + currentFrame + ", bufferSize: " + bufferSize);
    sourceSample.getFrames(currentFrame, buffer);

    //info("current frame: " + currentFrame + ", buffer size: " + bufferSize + ", buffer: " + Arrays.deepToString(buffer));

    // create a new Sample for the current slice
    final double length = sourceSample.samplesToMs(bufferSize);
    final Sample slice = new Sample(length, channelCount, sourceSample.getSampleRate());

    // write the buffer into the current slice
    slice.putFrames(0, buffer);
    return slice;
  }

  public void info(String m) {
    System.out.println(m);
  }
}
