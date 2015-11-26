package com.orion.sampler.tools.ui.progress;

public interface ProgressObserver {

  void notifyProgress(float progress, String message);
}
