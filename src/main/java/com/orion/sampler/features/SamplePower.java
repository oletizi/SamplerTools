package com.orion.sampler.features;

import net.beadsproject.beads.analysis.featureextractors.Power;
import net.beadsproject.beads.data.Sample;

public class SamplePower {
  public float getPower(Sample sample) {

    float[] sampleData = new float[(int) sample.getNumFrames()];
    float[] buffer = new float[(int) sample.getNumFrames()];
    for (int i = 0; i < sample.getNumFrames(); i++) {
      sample.getFrame(i, buffer);
      // TODO: Figure out how to deal with multi-channel audio
      sampleData[i] = buffer[0];
    }

    final Power power = new Power();
    power.process(null, null, sampleData);
    return power.getFeatures();
  }
}
