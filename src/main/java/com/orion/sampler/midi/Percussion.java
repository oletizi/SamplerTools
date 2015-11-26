package com.orion.sampler.midi;

import org.jfugue.theory.Note;

public enum Percussion {

  CHINA(new Note("E2")),
  CLAP(new Note("Eb1")),
  COWBELL(new Note("Ab2")),
  CRASH_1(new Note("C#2")),
  CRASH_2(new Note("A2")),
  HIMIDTOM(new Note("C2")),
  SNARE_1(new Note("D1"));

  private final Note key;

  Percussion(Note key) {
    this.key = key;
  }

  public Note key() {
    return key;
  }
}
