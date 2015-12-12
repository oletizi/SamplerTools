package com.orion.sampler.tools.percussion.ui.progress;

public interface ProgressObserver {

  void notifyProgress(float progress, String message);
}
