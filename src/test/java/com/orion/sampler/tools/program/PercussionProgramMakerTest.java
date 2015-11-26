package com.orion.sampler.tools.program;

import com.orion.sampler.io.PercussionSourceSampleSelector;
import com.orion.sampler.midi.Percussion;
import net.beadsproject.beads.data.Sample;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class PercussionProgramMakerTest {

  private PercussionProgramMaker maker;
  private List<Sample> samples;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private PercussionSourceSampleSelector selector;

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

    final File root = new File(ClassLoader.getSystemResource("program/drum/").getFile());
    selector = new PercussionSourceSampleSelector(root);
    maker = new PercussionProgramMaker(selector);
  }

  @Test
  public void test() throws Exception {
    final String region = maker.createRegion(Percussion.SNARE_1, samples);
    info(region);
  }

  @Test
  public void testMultiple() throws Exception {

    String program = maker.createProgram();
    info(program);
    final String[] split = program.split("<region>");
    assertEquals(32, split.length);

  }

  @Test
  public void testWriteProgram() throws Exception {
    final File dest = temp.newFolder();
    maker.writeProgram("test_program", dest);
    assertTrue(new File(dest, "test_program.sfz").exists());
  }

  private void info(String s) {
    System.out.println(s);
  }

}