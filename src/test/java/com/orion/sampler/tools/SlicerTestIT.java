package com.orion.sampler.tools;

import net.beadsproject.beads.data.Sample;
import org.junit.Test;

import java.util.List;

public class SlicerTestIT extends SlicerTest {

  @Test
  public void testWithWrite() throws Exception {
    final List<Sample> slices = slicer.slice(55);

    int i = 0;
    for (Sample slice : slices) {
      slice.write("/tmp/slice" + i++ + "-" + System.currentTimeMillis() + ".wav");
    }

  }

}
