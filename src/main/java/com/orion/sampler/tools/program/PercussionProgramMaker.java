package com.orion.sampler.tools.program;

import com.orion.sampler.features.TransientLocator;
import com.orion.sampler.io.PercussionSourceSampleSelector;
import com.orion.sampler.midi.Percussion;
import com.orion.sampler.tools.Slicer;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.*;
import java.util.*;

public class PercussionProgramMaker {

  private static final int HHGRP = 3;

  private final String programName;
  private final File sourceDir;
  private final File destDir;
  private final AudioContext ac;
  private int preroll;

  public PercussionProgramMaker(final String programName, final int preroll,
                                final File sourceDir, final File destDir) throws IOException {
    this.programName = programName;
    this.preroll = preroll;
    this.sourceDir = sourceDir;
    this.destDir = destDir;
    ac = new AudioContext(new NonrealtimeIO());
  }

  public String createRegion(final Percussion key, final List<Sample> samples) {
    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    // TODO: debug the power sorting
//    Collections.sort(samples, (o1, o2) -> {
//      final SamplePower samplePower = new SamplePower();
//      return (int) (samplePower.getPower(o1) - samplePower.getPower(o2));
//    });

    final int velspan = 128 / samples.size();

    int currentVelocity = 0;
    for (int i = 0; i < samples.size(); i++) {
      currentVelocity += velspan;
      final Sample sample = samples.get(i);
      out.println("<region>");
      out.println("sample=" + new File(sample.getFileName()).getName());
      out.println("key=" + key.getKey().getValue());
      //out.println("lovel=" + currentVelocity);
      out.println("hivel=" + ((i + 1 == samples.size()) ? 127 : currentVelocity));
      out.println();
    }

    return sbw.toString();
  }

  private String createProgramFromSamples(final PercussionSourceSampleSelector selector) {

    final StringBuilderWriter sbw = new StringBuilderWriter();
    final PrintWriter out = new PrintWriter(sbw);

    out.println("#define $HHGRP " + HHGRP);
    out.println();
    out.println("<global>");
    out.println("loop_mode=one_shot");

    final Map<Percussion, StringBuilderWriter> sections = new HashMap<>();
    final Map<Percussion, List<Sample>> samples = selector.getAllSamples();
    info("Creating program from " + samples.size() + " samples...");
    final List<Percussion> sortedKeys = new ArrayList<>(samples.keySet());
    Collections.sort(sortedKeys);
    for (Percussion key : sortedKeys) {
      final List<Sample> value = samples.get(key);
      out.println("<group>");
      out.println("key=" + key.getKey().getValue());
      switch (key) {
        case CLOSEDHH:
        case PEDALHH:
        case OPENHH:
          out.println("group=$HHGRP");
          out.println("off_by=$HHGRP");
        default:
      }
      out.println(createRegion(key, value));
    }


    return sbw.toString();
  }

  public void writeProgramFromSamples() throws IOException {
    if (!sourceDir.equals(destDir)) {
      info("Copying samples from " + sourceDir + " to " + destDir);
      new PercussionSourceSampleSelector(sourceDir).copyTo(destDir);
    }
    writeProgram();
  }

  public void writeProgramFromSource() throws IOException {
    sliceSource();
    writeProgram();
  }

  private void writeProgram() throws IOException {
    final Writer out = new OutputStreamWriter(new FileOutputStream(new File(destDir, programName + ".sfz")));
    final String program = createProgramFromSamples(new PercussionSourceSampleSelector(destDir));
    info("Writing program: " + program);
    out.write(program);
    out.flush();
    out.close();
  }

  private void sliceSource() throws IOException {
    if (destDir.exists() && !destDir.isDirectory()) {
      throw new IOException("Destination is not a directory: " + destDir);
    }
    FileUtils.forceMkdir(destDir);

    final Map<Percussion, List<Sample>> sourceSamples = new PercussionSourceSampleSelector(sourceDir).getAllSamples();
    for (Map.Entry<Percussion, List<Sample>> entry : sourceSamples.entrySet()) {
      // slice the source audio...
      //info("Slicing: getKey: " + entry.getKey());
      for (Sample source : entry.getValue()) {
        final TransientLocator thisLocator = new TransientLocator(ac, source, entry.getKey().getTransientThreshold());
        final List<Sample> slices = new Slicer(ac, thisLocator).slice(preroll);
        for (int i = 0; i < slices.size(); i++) {
          final String filename = new File(destDir, entry.getKey().name() + "-" + i + ".wav").getAbsolutePath();
          //info("Writing slice: " + filename);
          slices.get(i).write(filename);
        }
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}
