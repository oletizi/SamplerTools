package com.orion.sampler.midi;

import org.jfugue.theory.Note;
import org.junit.Test;

import static com.orion.sampler.midi.Percussion.CHINA;
import static junit.framework.TestCase.assertEquals;

public class PercussionTest {

  @Test
  public void test() throws Exception {
    assertEquals(new Note("E4"), CHINA.getKey());
    assertEquals(52, CHINA.getKey().getValue());
  }

}