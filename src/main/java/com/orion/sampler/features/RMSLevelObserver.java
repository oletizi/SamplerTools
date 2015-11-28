package com.orion.sampler.features;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

import java.util.HashSet;
import java.util.Set;

public class RMSLevelObserver extends UGen {

  private final Set<LevelObserver> observers = new HashSet<>();
  private float currentLevel = -1;
  private boolean isAtZero = false;

  public RMSLevelObserver(AudioContext ac, int outputs, int inputs) {
    super(ac, outputs, inputs);
  }

  @Override
  public void calculateBuffer() {

    // this is a little odd and dependent on the implementation of RMS--RMS only writes its values into the 0th
    // element of its output array, so we only need to examine the 0th element of our input array.
    if (bufIn.length > 0) {
      float level = -1;
      for (int i = 0; i < bufIn[0].length; i++) {
        level = bufIn[0][i];
        if (level == 0 && !isAtZero) {
          isAtZero = true;
          // we only need to fire off a message in the first frame of the first buffer that reaches zero level. All subsequent zero-level
          // buffers (until the level increases from 0) are redundant.
          for (LevelObserver observer : observers) {
            observer.notifyZeroLevel();
          }
        }
        currentLevel = level;
        if (level == 0) {
          break;
        }
      }
      isAtZero = (currentLevel == 0);
      for (LevelObserver observer : observers) {
        // TODO: this is not quite right--this notifies observers of the last level in the buffer--not the RMS level
        // of the buffer. Too bad I can't math.
        observer.notifyLevel(level);
      }
    }
  }

  public void addObserver(final LevelObserver observer) {
    observers.add(observer);
  }

  public float getLevel() {
    return currentLevel;
  }

  private void info(String s) {
    //System.out.println(getClass().getSimpleName() + ": " + s);
  }

}
