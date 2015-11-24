package com.orion.sampler.tools.program;

import com.orion.sampler.midi.Percussion;
import net.beadsproject.beads.data.Sample;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class PercussionProgramMakerTest {

  private PercussionProgramMaker maker;
  private List<Sample> samples;

  @Before
  public void before() throws Exception {
    final File sampleDir = new File(ClassLoader.getSystemResource("samples/snare/").getFile());
    samples = new ArrayList<>();
    Collections.reverse(samples);
    final String[] filenames = sampleDir.list((dir, name) -> name.endsWith(".wav"));
    for (String filename : filenames) {
      samples.add(new Sample(new File(sampleDir, filename).getAbsolutePath()));
    }
    assertEquals(8, samples.size());
    maker = new PercussionProgramMaker();
  }

  @Test
  public void test() throws Exception {
    final String region = maker.createRegion(Percussion.SNARE_1, samples);
    info(region);
  }

  private void info(String s) {
    System.out.println(s);
  }

}