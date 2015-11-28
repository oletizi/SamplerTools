package com.orion.sampler.features;

public interface LevelObserver {

  void notifyZeroLevel();

  void notifyLevel(float level);
}
