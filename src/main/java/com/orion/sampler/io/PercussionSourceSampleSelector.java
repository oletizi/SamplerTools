package com.orion.sampler.io;

import com.orion.sampler.midi.Percussion;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PercussionSourceSampleSelector {
  private final File root;
  private final Map<Percussion, Set<Sample>> instrumentMap = new HashMap<>();
  private int sampleCount;

  public PercussionSourceSampleSelector(final File root) throws IOException {
    this.root = root;

    for (String filename : root.list((dir, name) -> name.endsWith(".wav"))) {
      Percussion key = null;
      final Percussion[] values = Percussion.values();
      for (Percussion value : values) {
        if (filename.matches("(?i:" + value.name() + ".*)")) {
          info("Adding sample: key: " + value + ", value: " + filename);
          addSample(value, filename);
        }
      }
    }
  }

  private void addSample(final Percussion key, final String filename) throws IOException {
    getSamples(key).add(newSample(filename));
    sampleCount++;
  }

  private Set<Sample> getSamples(final Percussion key) {
    Set<Sample> samples = instrumentMap.get(key);
    if (samples == null) {
      samples = new HashSet<>();
      instrumentMap.put(key, samples);
    }
    return samples;
  }

  private Sample newSample(String filename) throws IOException {
    return new Sample(new File(root, filename).getAbsolutePath());
  }

  public boolean hasSamplesFor(Percussion key) {
    return !getSamples(key).isEmpty();
  }

  public Set<Sample> getSamplesFor(final Percussion key) {
    return new HashSet<>(getSamples(key));
  }

  public void copyTo(File destination) throws IOException {
    if (!root.equals(destination)) {
      for (Map.Entry<Percussion, Set<Sample>> entry : instrumentMap.entrySet()) {
        final Set<Sample> set = entry.getValue();
        for (Sample sample : set) {
          FileUtils.copyFileToDirectory(new File(sample.getFileName()), destination);
        }
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  public Map<Percussion, Set<Sample>> getAllSamples() {
    return new HashMap(instrumentMap);
  }

  public int getSampleCount() {
    return sampleCount;
  }
}
