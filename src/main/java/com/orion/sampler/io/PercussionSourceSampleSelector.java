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

import static com.orion.sampler.midi.Percussion.*;

public class PercussionSourceSampleSelector {
  private final File root;
  private final Map<Percussion, Set<Sample>> instrumentMap = new HashMap<>();

  public PercussionSourceSampleSelector(final File root) throws IOException {
    this.root = root;

    for (String filename : root.list((dir, name) -> name.endsWith(".wav"))) {
      Percussion key = null;
      if (filename.matches("china.*")) {
        key = CHINA;
      } else if (filename.matches("clap.*")) {
        key = CLAP;
      } else if (filename.matches("cowbell.*")) {
        key = COWBELL;
      } else if (filename.matches("crash1.*")) {
        key = CRASH_1;
      } else if (filename.matches("crash2.*")) {
        key = CRASH_2;
      } else if (filename.matches("himidtom.*")) {
        key = HIMIDTOM;
      }
      if (key != null) {
        addSample(key, filename);
      }
    }
  }

  private void addSample(final Percussion key, final String filename) throws IOException {
    getSamples(key).add(newSample(filename));
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
    for (Map.Entry<Percussion, Set<Sample>> entry : instrumentMap.entrySet()) {
      final Set<Sample> set = entry.getValue();
      for (Sample sample : set) {
        FileUtils.copyFileToDirectory(new File(sample.getFileName()), destination);
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  public Map<Percussion, Set<Sample>> getAllSamples() {
    return new HashMap(instrumentMap);
  }
}
