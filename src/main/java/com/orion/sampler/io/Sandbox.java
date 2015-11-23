package com.orion.sampler.io;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Sandbox {

  private final File root;

  public Sandbox() throws IOException {
    root = new File(System.getProperty("user.home"));
    FileUtils.forceMkdir(root);
  }

  public File getNewSandbox() throws IOException {
    final File sandbox = new File(root, "sandbox-" + System.currentTimeMillis());
    FileUtils.forceMkdir(sandbox);
    return sandbox;
  }
}
