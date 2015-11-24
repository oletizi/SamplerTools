package com.orion.sampler.util;

import net.beadsproject.beads.data.Sample;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SampleUtil {

  public List<Sample> loadSamples(final URL resource) throws IOException {
    return loadSamples(new File(resource.getFile()));
  }

  public List<Sample> loadSamples(final File directory) throws IOException {
    final List<Sample> rv = new ArrayList<>();

    // TODO: Add support for other file formats
    final String[] filenames = directory.list((dir, name) -> name.endsWith(".wav"));
    for (String filename : filenames) {
      rv.add(new Sample(new File(directory, filename).getAbsolutePath()));
    }

    return rv;
  }
}
