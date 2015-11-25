package com.orion.sampler.io;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
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

    assertTrue(selector.hasChina());
    assertEquals(getPath("china.wav"), selector.getChina().getFileName());

    assertTrue(selector.hasClap());
    assertEquals(getPath("clap.wav"), selector.getClap().getFileName());

    assertTrue(selector.hasCowbell());
    assertEquals(getPath("cowbell.wav"), selector.getCowbell().getFileName());

    assertTrue(selector.hasCrash1());
    assertEquals(getPath("crash1.wav"), selector.getCrash1().getFileName());

    assertTrue(selector.hasCrash2());
    assertEquals(getPath("crash2.wav"), selector.getCrash2().getFileName());

    assertTrue(selector.hasHimidtom());
    assertEquals(getPath("himidtom.wav"), selector.getHimidtom().getFileName());

  }

  private String getPath(String resource) {
    return getFile(root + resource).getAbsolutePath();
  }

  private File getFile(String resource) {
    return new File(ClassLoader.getSystemResource(resource).getFile());
  }

}