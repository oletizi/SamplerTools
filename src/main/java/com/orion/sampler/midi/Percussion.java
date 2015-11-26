package com.orion.sampler.midi;

import org.jfugue.theory.Note;

public enum Percussion {

  CHINA(new Note(52)),
  CLAP(new Note(39)),
  COWBELL(new Note(56)),
  CRASH1(new Note(49)),
  CRASH2(new Note(57)),
  HIMIDTOM(new Note(47)),
  SNARE1(new Note(38));

  private final Note key;

  Percussion(Note key) {
    this.key = key;
  }

  public Note key() {
    return key;
  }
}
