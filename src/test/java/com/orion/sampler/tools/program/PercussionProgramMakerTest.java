package com.orion.sampler.tools.program;

import com.orion.sampler.io.Sandbox;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class PercussionProgramMakerTest {

  private PercussionProgramMaker maker;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private File destDir;

  @Before
  public void before() throws Exception {
    final File sourceDir = new File(ClassLoader.getSystemResource("audio/drumsource/").getFile());

    final int preroll = 93;
    destDir = new Sandbox().getNewSandbox();
    maker = new PercussionProgramMaker("program", preroll, sourceDir, destDir);
  }

  @Test
  public void testWriteProgram() throws Exception {
    maker.writeProgramFromSource();
    assertTrue(new File(destDir, "program.sfz").exists());
  }

  private void info(String s) {
    System.out.println(s);
  }

}