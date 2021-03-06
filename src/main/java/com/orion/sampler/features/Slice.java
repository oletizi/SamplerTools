package com.orion.sampler.features;

import net.beadsproject.beads.data.Sample;

public class Slice {
  private final Transient start;
  private final int startFrame;
  private int endFrame;

  public Slice(final Transient start) {
    this.start = start;
    this.startFrame = start.getFrameIndex();
  }

  public void notifyZeroLevelIndex(int frameIndex) {
    if (frameIndex > startFrame && endFrame == 0) {
      info("Setting zero level frame: start index: " + start.getFrameIndex() + ", frameIndex: " + frameIndex);
      this.endFrame = frameIndex;
    }
  }

  public Transient getStartTransient() {
    return start;
  }

  public int getStartFrame() {
    return startFrame;
  }

  public int getEndFrame() {
    return endFrame;
  }

  public void setEndFrame(int endFrame) {
    assert endFrame > startFrame;
    this.endFrame = endFrame;
  }

  private void info(String s) {
    System.out.println(this + ": " + s);
  }

  @Override
  public String toString() {
    String sourceFrames = "";
    final Sample source = getStartTransient().getSource();
    if (source != null) {
      sourceFrames = " sourceFrames: " + source.getNumFrames();
    }
    return getClass().getSimpleName() + "<startFrame: " + startFrame + ", endFrame: " + endFrame + sourceFrames + ">";
  }

}
