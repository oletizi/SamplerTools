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

  public List<Sample> slice() {
    final List<Sample> rv = new ArrayList<>(locator.getTransients().size());

    float currentFrame = 0;
    for (Transient slicepoint : locator.getTransients()) {

      final int sampleIndex = slicepoint.getSampleIndex();

      final float bufferSize = sampleIndex - currentFrame;
      final int channelCount = sourceSample.getNumChannels();
      float[][] buffer = new float[(int) bufferSize][channelCount];

      // write the frames of the current slice into the buffer
      sourceSample.getFrames((int) currentFrame, buffer);

      // create a new Sample for the current slice
      final double length = sourceSample.samplesToMs(bufferSize);
      final Sample slice = new Sample(length, channelCount, sourceSample.getSampleRate());

      // write the buffer into the current slice
      slice.putFrames(0, buffer);

      rv.add(slice);

      // update the current frame
      currentFrame = sampleIndex;
    }

    return rv;
  }
}
