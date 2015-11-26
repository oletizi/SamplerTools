package com.orion.sampler.tools.program;

import com.orion.sampler.features.SamplePower;
import com.orion.sampler.io.PercussionSourceSampleSelector;
import com.orion.sampler.midi.Percussion;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.*;
import java.util.*;

public class PercussionProgramMaker {

  private final PercussionSourceSampleSelector selector;

  public PercussionProgramMaker(PercussionSourceSampleSelector selector) {
    this.selector = selector;
  }

  public PercussionProgramMaker() {
    // TODO: Create a null program maker
    selector = null;
  }

  public String createRegion(final Percussion key, final List<Sample> samples) {
    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    // sort samples by power
    Collections.sort(samples, (o1, o2) -> {
      final SamplePower samplePower = new SamplePower();
      return (int) (samplePower.getPower(o1) - samplePower.getPower(o2));
    });

    final int velspan = 128 / samples.size();

    int currentVelocity = 0;
    for (Sample sample : samples) {
      out.println("<region>");
      out.println("sample=" + new File(sample.getFileName()).getName());
      out.println("key=" + key.key().getValue());
      out.println("lovel=" + currentVelocity);
      out.println("hivel=" + (currentVelocity + velspan - 1));
      out.println();
      currentVelocity += velspan;
    }

    return sbw.toString();
  }

  public String createProgram() {
    if (selector == null) {
      return "";
    }
    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    final Map<Percussion, Set<Sample>> samples = selector.getAllSamples();
    info("Creating program from " + samples.size() + " samples...");
    for (Map.Entry<Percussion, Set<Sample>> entry : samples.entrySet()) {
      out.append(createRegion(entry.getKey(), new ArrayList<>(entry.getValue())));
    }

    return sbw.toString();
  }

  public void writeProgram(final String name, final File destination) throws IOException {
    if (selector == null) {
      throw new IllegalStateException("You must set a PercussionSourceSampleSelector.");
    }
    if (destination.exists() && !destination.isDirectory()) {
      throw new IOException("Destination is not a directory: " + destination);
    }
    FileUtils.forceMkdir(destination);
    selector.copyTo(destination);

    final Writer out = new OutputStreamWriter(new FileOutputStream(new File(destination, name + ".sfz")));
    final String program = createProgram();
    info("Writing program: " + program);
    out.write(program);
    out.flush();
    out.close();
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}
