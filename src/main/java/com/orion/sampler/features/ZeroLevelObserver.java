package com.orion.sampler.features;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;

import java.util.HashSet;
import java.util.Set;

public class ZeroLevelObserver extends UGen {

  private final Set<Bead> listeners = new HashSet<>();
  private float previousBufferLevel = -1;

  public ZeroLevelObserver(AudioContext ac, int outputs, int inputs) {
    super(ac, outputs, inputs);
  }


  @Override
  public synchronized void addInput(UGen sourceUGen) {
    super.addInput(sourceUGen);
    info("Adding input: " + sourceUGen);
    info("ins: " + getIns() + ", connected inputs: " + getConnectedInputs());
  }

  public synchronized void addInput(int inputIndex, UGen sourceUGen, int sourceOutputIndex) {
    super.addInput(inputIndex, sourceUGen, sourceOutputIndex);
    info("Adding input: inputIndex: " + inputIndex + ", source: " + sourceUGen + ", sourceOutputIndex: " + sourceOutputIndex);
  }


  @Override
  public void calculateBuffer() {

    // this is a little odd and dependent on the implementation of RMS--RMS only writes its values into the 0th
    // element of its output array, so we only need to examine the 0th element of our input array.
    if (bufIn.length > 0) {
      float previousValue = -1;
      for (int i = 0; i < bufIn[0].length; i++) {
        float val = bufIn[0][i];
        if (val == 0 && previousBufferLevel != 0 && previousValue != 0) {
          //previousValue = val;
          // we only need to fire off a message in the first buffer that reaches zero level. All subsequent zero-level
          // buffers (until the level increases from 0) are redundant.
          for (Bead listener : listeners) {
            listener.message(this);
          }
        }
        previousValue = val;
        if (val == 0) {
          break;
        }
      }
      previousBufferLevel = previousValue;
    }
  }


  public void addZeroListener(final Bead bead) {
    listeners.add(bead);
  }

  private void info(String s) {
    //System.out.println(getClass().getSimpleName() + ": " + s);
  }

}
