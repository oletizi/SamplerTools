package com.orion.sampler.features;

public class Transient {
  private final double time;
  private final int sampleIndex;

  public Transient(final double time, final int sampleIndex) {
    this.time = time;
    this.sampleIndex = sampleIndex;
  }

  public int getSampleIndex() {
    return sampleIndex;
  }

  public double getSample() {
    return sampleIndex;
  }
}
