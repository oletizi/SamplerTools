package com.orion.sampler.midi;

import org.jfugue.theory.Note;

public enum Percussion {

  CHINA(new Note(52), 0.1f),
  CLAP(new Note(39), 0.02f),
  CLOSEDHH(new Note(42), 0.001f),
  COWBELL(new Note(56), 0.02f),
  CRASH1(new Note(49), 0.02f),
  CRASH2(new Note(57), 0.00001f),
  HIMIDTOM(new Note(47), 0.02f),
  HITOM1(new Note(48), 0.02f),
  KICK(new Note(36), 0.02f),
  LOWFLOORTOM(new Note(41), 0.02f),
  MARACAS(new Note(70), 0.001f),
  OPENHH(new Note(46), 0.001f),
  PEDALHH(new Note(44), 0.001f),
  RIDE1(new Note(51), 0.001f),
  RIDE2(new Note(59), 0.001f),
  RIDEBELL(new Note(53), 0.01f),
  SIDESTICK(new Note(37), 0.01f),
  SNARE1(new Note(38), 0.02f),
  TAMBO(new Note(54), 0.001f);

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
