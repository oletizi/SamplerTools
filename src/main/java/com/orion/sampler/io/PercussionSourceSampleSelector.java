package com.orion.sampler.io;

import net.beadsproject.beads.data.Sample;

import java.io.File;
import java.io.IOException;

public class PercussionSourceSampleSelector {
  private final File root;
  private Sample china;
  private Sample clap;
  private Sample cowbell;
  private Sample crash1;

  public PercussionSourceSampleSelector(final File root) throws IOException {
    this.root = root;

    for (String filename : root.list((dir, name) -> name.endsWith(".wav"))) {
      if (filename.matches("china.*")) {
        china = newSample(filename);
      } else if (filename.matches("clap.*")) {
        clap = newSample(filename);
      } else if (filename.matches("cowbell.*")) {
        cowbell = newSample(filename);
      } else if (filename.matches("crash1.*")) {
        crash1 = newSample(filename);
      }
    }
  }

  private Sample newSample(String filename) throws IOException {
    return new Sample(new File(root, filename).getAbsolutePath());
  }

  public boolean hasChina() {
    return china != null;
  }

  public Sample getChina() {
    return china;
  }

  public boolean hasClap() {
    return clap != null;
  }

  public Sample getClap() {
    return clap;
  }

  public boolean hasCowbell() {
    return cowbell != null;
  }

  public Sample getCowbell() {
    return cowbell;
  }

  public Sample getCrash1() {
    return crash1;
  }

  public boolean hasCrash1() {
    return crash1 != null;
  }

  private enum Instrument {
    china,
    clap,
    cowbell,
    crash1,
    crash2,
    himidtom,
    hitom,
    kick,
    lowfloortom,
    maracas,
    ride1,
    ride2,
    ridebell,
    sidestick,
    snare,
    tambo
  }

}
