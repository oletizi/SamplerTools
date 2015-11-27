package com.orion.sampler.midi;

import org.jfugue.theory.Note;

public enum Percussion {

  CHINA(new Note(52), 0.1f),
  CLAP(new Note(39), 0.02f),
  COWBELL(new Note(56), 0.02f),
  CRASH1(new Note(49), 0.02f),
  CRASH2(new Note(57), 0.03f),
  HIMIDTOM(new Note(47), 0.02f),
  SNARE1(new Note(38), 0.02f);

  private final Note key;
  private final float percussionThresold;

  Percussion(Note key, float transientThreshold) {
    this.key = key;
    this.percussionThresold = transientThreshold;
  }

  public float getTransientThreshold() {
    return percussionThresold;
  }

  public Note getKey() {
    return key;
  }
}
