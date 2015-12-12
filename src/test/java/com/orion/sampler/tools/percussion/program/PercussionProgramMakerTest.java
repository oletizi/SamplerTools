package com.orion.sampler.tools.percussion.program;

import com.orion.sampler.io.Sandbox;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertTrue;

@Ignore
public class PercussionProgramMakerTest {

  private PercussionProgramMaker maker;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private File destDir;

  private void setup(final String source, final File destDir) throws Exception {
    setup(new File(ClassLoader.getSystemResource(source).getFile()), destDir);
  }

  private void setup(final File sourceDir, final File destDir) throws Exception {
    final int preroll = 93;
    this.destDir = destDir;
    info("dest dir: " + destDir);
    maker = new PercussionProgramMaker("program", preroll, sourceDir, destDir);
  }

  @Test
  public void testSliceAndWriteProgram() throws Exception {
    setup("audio/drumsource/", new Sandbox().getNewSandbox());
    maker.writeProgramFromSource();
    assertTrue(new File(destDir, "program.sfz").exists());
  }

  @Test
  public void testWriteProgramFromExistingSamples() throws Exception {
    //setup("program/drum/", new Sandbox().getNewSandbox());
    final File dir = new File("/Users/orion/sandbox-1448747324910");
    setup(dir, dir);
    maker.writeProgramFromSamples();
    final File programFile = new File(destDir, "program.sfz");
    assertTrue("program file doesn't exist: " + programFile, programFile.exists());
  }

  private void info(String s) {
    System.out.println(s);
  }

}