package com.orion.sampler.tools.ui.progress;

public class FractionalProgressObserver implements ProgressObserver {
  private final ProgressObserver parent;
  private final float progressFactor;

  public FractionalProgressObserver(final ProgressObserver parent, final float progressFactor) {
    this.parent = parent;
    this.progressFactor = progressFactor;
  }

  @Override
  public void notifyProgress(float progress, String message) {
    parent.notifyProgress(progress * progressFactor, message);
  }
}
