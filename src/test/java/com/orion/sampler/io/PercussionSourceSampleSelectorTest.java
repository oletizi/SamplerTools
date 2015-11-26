package com.orion.sampler.io;

import net.beadsproject.beads.data.Sample;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static com.orion.sampler.midi.Percussion.*;
import static org.junit.Assert.assertTrue;

public class PercussionSourceSampleSelectorTest {

  private String root;

  @Before
  public void before() throws Exception {
    root = "audio/drumsource/";
  }

  @Test
  public void test() throws Exception {
    final File folder = getFile(root);
    final PercussionSourceSampleSelector selector = new PercussionSourceSampleSelector(folder);

    assertTrue(selector.hasSamplesFor(CHINA));
    assertTrue(contains("china.wav", selector.getSamplesFor(CHINA)));

    assertTrue(selector.hasSamplesFor(CLAP));
    assertTrue(contains("clap.wav", selector.getSamplesFor(CLAP)));

    assertTrue(selector.hasSamplesFor(COWBELL));
    assertTrue(contains("cowbell.wav", selector.getSamplesFor(COWBELL)));

    assertTrue(selector.hasSamplesFor(CRASH1));
    assertTrue(contains("crash1.wav", selector.getSamplesFor(CRASH1)));

    assertTrue(selector.hasSamplesFor(CRASH2));
    assertTrue(contains("crash2.wav", selector.getSamplesFor(CRASH2)));

    assertTrue(selector.hasSamplesFor(HIMIDTOM));
    assertTrue(contains("himidtom.wav", selector.getSamplesFor(HIMIDTOM)));

  }

  private boolean contains(String s, Set<Sample> set) {
    for (Sample sample : set) {
      if (sample.getFileName().contains(s)) {
        return true;
      }
    }
    return false;
  }

  private File getFile(String resource) {
    return new File(ClassLoader.getSystemResource(resource).getFile());
  }

}