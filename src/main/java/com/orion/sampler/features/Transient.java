package com.orion.sampler.features;

import net.beadsproject.beads.data.Sample;

public class Transient {
  private final Sample source;
  private final int frameIndex;

  public Transient(final Sample source, final int frameIndex) {
    this.source = source;
    this.frameIndex = frameIndex;
  }

  public int getFrameIndex() {
    return frameIndex;
  }

  public Sample getSource() {
    return source;
  }
}
